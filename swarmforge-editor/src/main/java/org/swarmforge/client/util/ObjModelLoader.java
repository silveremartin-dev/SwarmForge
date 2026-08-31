/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight Wavefront OBJ 3D Model Loader for JavaFX Canvas 3D rendering.
 * Parses 3D vertex positions and polygonal face indices from .obj files.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class ObjModelLoader {

    public static class ObjMesh {
        public String name = "default";
        public final List<float[]> vertices = new ArrayList<>();
        public final List<int[]> faces = new ArrayList<>();
    }

    private static final Map<String, List<ObjMesh>> cache = new HashMap<>();

    public static synchronized List<ObjMesh> loadObjModel(String resourcePath) {
        if (cache.containsKey(resourcePath)) {
            return cache.get(resourcePath);
        }

        List<ObjMesh> meshes = new ArrayList<>();
        try (InputStream is = ObjModelLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[ObjModelLoader] Resource not found: " + resourcePath);
                return meshes;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                ObjMesh currentMesh = new ObjMesh();
                meshes.add(currentMesh);

                List<float[]> globalVertices = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#") || line.isEmpty()) continue;

                    if (line.startsWith("o ") || line.startsWith("g ")) {
                        String name = line.substring(2).trim();
                        currentMesh = new ObjMesh();
                        currentMesh.name = name;
                        meshes.add(currentMesh);
                    } else if (line.startsWith("v ")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 4) {
                            try {
                                float x = Float.parseFloat(parts[1]);
                                float y = Float.parseFloat(parts[2]);
                                float z = Float.parseFloat(parts[3]);
                                globalVertices.add(new float[]{x, y, z});
                            } catch (NumberFormatException ignored) {}
                        }
                    } else if (line.startsWith("f ")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 4) {
                            int[] faceIndices = new int[parts.length - 1];
                            for (int i = 1; i < parts.length; i++) {
                                String[] vertInfo = parts[i].split("/");
                                try {
                                    int vIdx = Integer.parseInt(vertInfo[0]);
                                    if (vIdx < 0) {
                                        vIdx = globalVertices.size() + vIdx;
                                    } else {
                                        vIdx = vIdx - 1; // Convert 1-based OBJ index to 0-based
                                    }
                                    faceIndices[i - 1] = Math.max(0, Math.min(globalVertices.size() - 1, vIdx));
                                } catch (NumberFormatException ignored) {}
                            }
                            currentMesh.faces.add(faceIndices);
                        }
                    }
                }

                // Copy global vertices reference to all sub-meshes
                for (ObjMesh mesh : meshes) {
                    mesh.vertices.addAll(globalVertices);
                }
            }
        } catch (Exception e) {
            System.err.println("[ObjModelLoader] Error loading " + resourcePath + ": " + e.getMessage());
        }

        cache.put(resourcePath, meshes);
        return meshes;
    }
}
