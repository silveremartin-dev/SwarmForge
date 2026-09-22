/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Pure-Java ONNX Model Serializer.
 * Encodes a SimpleNeuralNetwork directly into a valid, standard binary ONNX ModelProto file (Protobuf).
 * Fully compatible with Microsoft ONNX Runtime (CPU / DirectML / CUDA).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class OnnxModelSerializer {

    /**
     * Serializes a SimpleNeuralNetwork to a standard .onnx file.
     *
     * @param network   Trained neural network
     * @param modelName Human-readable model name
     * @param file      Target output file
     * @throws IOException On write error
     */
    public static void serializeToFile(SimpleNeuralNetwork network, String modelName, File file) throws IOException {
        byte[] onnxBytes = serializeToBytes(network, modelName);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(onnxBytes);
        }
    }

    /**
     * Serializes a SimpleNeuralNetwork to a standard ONNX protobuf byte array.
     */
    public static byte[] serializeToBytes(SimpleNeuralNetwork network, String modelName) {
        ProtobufWriter model = new ProtobufWriter();

        // 1. ir_version = 8 (ONNX standard)
        model.writeInt64(1, 8);
        // 2. producer_name = "SwarmForge-NN"
        model.writeString(2, "SwarmForge-NN");
        // 3. producer_version = "2.0"
        model.writeString(3, "2.0");
        // 4. domain = "ai.swarmforge"
        model.writeString(4, "ai.swarmforge");
        // 5. model_version = 1
        model.writeInt64(5, 1);

        // 7. graph = GraphProto
        byte[] graphBytes = buildGraph(network, modelName);
        model.writeBytes(7, graphBytes);

        // 8. opset_import = [OperatorSetIdProto(version=13)]
        ProtobufWriter opset = new ProtobufWriter();
        opset.writeString(1, ""); // default domain
        opset.writeInt64(2, 13);  // opset version 13
        model.writeBytes(8, opset.toByteArray());

        return model.toByteArray();
    }

    private static byte[] buildGraph(SimpleNeuralNetwork net, String graphName) {
        ProtobufWriter graph = new ProtobufWriter();

        int inDim = net.layerSizes[0];
        int outDim = net.layerSizes[net.numLayers];

        // 1. Nodes (Gemm + Relu for each layer)
        for (int l = 0; l < net.numLayers; l++) {
            String inTensor = (l == 0) ? "observation" : "act_" + (l - 1);
            String outGemm = (l == net.numLayers - 1) ? "action_logits" : "gemm_" + l;
            String wName = "W_" + l;
            String bName = "B_" + l;

            // Gemm Node: Y = alpha * A * B^T + beta * C (transB = 1)
            ProtobufWriter gemmNode = new ProtobufWriter();
            gemmNode.writeString(1, inTensor);
            gemmNode.writeString(1, wName);
            gemmNode.writeString(1, bName);
            gemmNode.writeString(2, outGemm);
            gemmNode.writeString(3, "gemm_node_" + l);
            gemmNode.writeString(4, "Gemm");

            // Attributes: transB=1, alpha=1.0, beta=1.0
            gemmNode.writeBytes(5, buildIntAttribute("transB", 1));
            gemmNode.writeBytes(5, buildFloatAttribute("alpha", 1.0f));
            gemmNode.writeBytes(5, buildFloatAttribute("beta", 1.0f));

            graph.writeBytes(1, gemmNode.toByteArray());

            // Activation (LeakyRelu) for hidden layers
            if (l < net.numLayers - 1) {
                String outAct = "act_" + l;
                ProtobufWriter reluNode = new ProtobufWriter();
                reluNode.writeString(1, outGemm);
                reluNode.writeString(2, outAct);
                reluNode.writeString(3, "leaky_relu_node_" + l);
                reluNode.writeString(4, "LeakyRelu");
                reluNode.writeBytes(5, buildFloatAttribute("alpha", 0.01f));
                graph.writeBytes(1, reluNode.toByteArray());
            }
        }

        // 2. Graph Name
        graph.writeString(2, graphName != null ? graphName : "species_brain_graph");

        // 5. Initializers (Weights & Biases TensorProto)
        for (int l = 0; l < net.numLayers; l++) {
            int layerOut = net.layerSizes[l + 1];
            int layerIn = net.layerSizes[l];

            // Weight Tensor [layerOut, layerIn]
            byte[] wBytes = buildTensorProto("W_" + l, new long[]{layerOut, layerIn}, net.weights[l]);
            graph.writeBytes(5, wBytes);

            // Bias Tensor [layerOut]
            byte[] bBytes = buildTensorProto("B_" + l, new long[]{layerOut}, net.biases[l]);
            graph.writeBytes(5, bBytes);
        }

        // 11. Input ValueInfoProto ("observation", [1, inDim])
        graph.writeBytes(11, buildValueInfoProto("observation", new long[]{1, inDim}));

        // 12. Output ValueInfoProto ("action_logits", [1, outDim])
        graph.writeBytes(12, buildValueInfoProto("action_logits", new long[]{1, outDim}));

        return graph.toByteArray();
    }

    private static byte[] buildValueInfoProto(String name, long[] shape) {
        ProtobufWriter vi = new ProtobufWriter();
        vi.writeString(1, name);

        ProtobufWriter typeProto = new ProtobufWriter();
        ProtobufWriter tensorType = new ProtobufWriter();
        tensorType.writeInt32(1, 1); // elem_type = FLOAT (1)

        ProtobufWriter shapeProto = new ProtobufWriter();
        for (long dim : shape) {
            ProtobufWriter dimProto = new ProtobufWriter();
            dimProto.writeInt64(1, dim); // dim_value
            shapeProto.writeBytes(1, dimProto.toByteArray());
        }
        tensorType.writeBytes(2, shapeProto.toByteArray());
        typeProto.writeBytes(1, tensorType.toByteArray());
        vi.writeBytes(2, typeProto.toByteArray());

        return vi.toByteArray();
    }

    private static byte[] buildTensorProto(String name, long[] dims, float[][] data2d) {
        int totalElements = 0;
        for (float[] row : data2d) totalElements += row.length;

        ByteBuffer buf = ByteBuffer.allocate(totalElements * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float[] row : data2d) {
            for (float val : row) {
                buf.putFloat(val);
            }
        }
        return buildTensorProtoRaw(name, dims, buf.array());
    }

    private static byte[] buildTensorProto(String name, long[] dims, float[] data1d) {
        ByteBuffer buf = ByteBuffer.allocate(data1d.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float val : data1d) {
            buf.putFloat(val);
        }
        return buildTensorProtoRaw(name, dims, buf.array());
    }

    private static byte[] buildTensorProtoRaw(String name, long[] dims, byte[] rawBytes) {
        ProtobufWriter tensor = new ProtobufWriter();
        for (long d : dims) {
            tensor.writeInt64(1, d); // dims
        }
        tensor.writeInt32(2, 1); // data_type = FLOAT (1)
        tensor.writeString(8, name); // name
        tensor.writeBytes(9, rawBytes); // raw_data (field 9 in TensorProto)
        return tensor.toByteArray();
    }

    private static byte[] buildIntAttribute(String name, long value) {
        ProtobufWriter attr = new ProtobufWriter();
        attr.writeString(1, name);
        attr.writeInt32(20, 2); // type = INT (2)
        attr.writeInt64(3, value); // i = value
        return attr.toByteArray();
    }

    private static byte[] buildFloatAttribute(String name, float value) {
        ProtobufWriter attr = new ProtobufWriter();
        attr.writeString(1, name);
        attr.writeInt32(20, 1); // type = FLOAT (1)
        attr.writeFloat(2, value); // f = value
        return attr.toByteArray();
    }

    /**
     * Fast, lightweight binary Protocol Buffer stream builder.
     */
    private static class ProtobufWriter {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        public void writeInt32(int fieldNumber, int value) {
            writeVarint((fieldNumber << 3));
            writeVarint(value);
        }

        public void writeInt64(int fieldNumber, long value) {
            writeVarint((fieldNumber << 3));
            writeVarint(value);
        }

        public void writeFloat(int fieldNumber, float value) {
            writeVarint((fieldNumber << 3) | 5); // 32-bit wire type
            int bits = Float.floatToRawIntBits(value);
            out.write(bits & 0xFF);
            out.write((bits >> 8) & 0xFF);
            out.write((bits >> 16) & 0xFF);
            out.write((bits >> 24) & 0xFF);
        }

        public void writeString(int fieldNumber, String value) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            writeBytes(fieldNumber, bytes);
        }

        public void writeBytes(int fieldNumber, byte[] bytes) {
            writeVarint((fieldNumber << 3) | 2); // length-delimited wire type
            writeVarint(bytes.length);
            out.write(bytes, 0, bytes.length);
        }

        public void writeVarint(long value) {
            while ((value & ~0x7FL) != 0) {
                out.write((int) ((value & 0x7F) | 0x80));
                value >>>= 7;
            }
            out.write((int) (value & 0x7F));
        }

        public byte[] toByteArray() {
            return out.toByteArray();
        }
    }
}
