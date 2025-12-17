/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.compute;

import org.swarmforge.protocol.grpc.ComputeServiceGrpc;

import org.swarmforge.protocol.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Manages connected compute nodes and dispatches tasks.
 */
public class ComputeClusterManager implements org.swarmforge.core.compute.ComputeCluster {

    private static final Logger LOG = Logger.getLogger(ComputeClusterManager.class.getName());

    private final Map<String, ComputeNodeInfo> nodes = new ConcurrentHashMap<>();
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public record ComputeNodeInfo(
            String id,
            String address,
            int port,
            boolean hasGpu,
            ManagedChannel channel,
            ComputeServiceGrpc.ComputeServiceBlockingStub stub) {
    }

    public void registerNode(String id, String address, int port, boolean hasGpu) {
        // Parse address just in case it contains port "host:port"
        String host = address;
        if (address.contains(":")) {
            host = address.split(":")[0];
        }

        LOG.info("Registering compute node: " + id + " at " + host + ":" + port + " GPU=" + hasGpu);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                // .keepAliveTime(30, TimeUnit.SECONDS)
                .build();

        ComputeNodeInfo info = new ComputeNodeInfo(
                id, host, port, hasGpu,
                channel,
                ComputeServiceGrpc.newBlockingStub(channel));

        nodes.put(id, info);
    }

    public void unregisterNode(String id) {
        ComputeNodeInfo info = nodes.remove(id);
        if (info != null) {
            info.channel.shutdown();
        }
    }

    public ComputeNodeInfo getNextNode() {
        if (nodes.isEmpty())
            return null;
        // Simple Round Robin
        var nodeList = nodes.values().stream().toList();
        int idx = roundRobinCounter.getAndIncrement() % nodeList.size();
        return nodeList.get(Math.abs(idx));
    }

    public ComputeNodeInfo getGpuNode() {
        return nodes.values().stream()
                .filter(ComputeNodeInfo::hasGpu)
                .findFirst()
                .orElse(null);
    }

    /**
     * Dispatch pheromone task to a node (preferably GPU).
     * 
     * @return true if dispatched and successful
     */
    @Override
    public boolean dispatchPheromoneTask(int w, int h, int d, float[] data) {
        ComputeNodeInfo node = getGpuNode();
        if (node == null) {
            node = getNextNode();
        }

        if (node == null)
            return false;

        try {
            PheromoneTaskRequest.Builder req = PheromoneTaskRequest.newBuilder()
                    .setWidth(w).setHeight(h).setDepth(d);

            for (float f : data)
                req.addPheromones(f);

            PheromoneTaskResponse resp = node.stub.processPheromones(req.build());

            if (resp.getSuccess()) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = resp.getNewPheromones(i);
                }
                return true;
            }
        } catch (Exception e) {
            LOG.warning("Failed to dispatch pheromone task to " + node.id + ": " + e.getMessage());
        }
        return false;
    }

    @Override
    public java.util.List<int[]> dispatchPathfindingTask(int startX, int startY, int startZ,
            int goalX, int goalY, int goalZ,
            int w, int h, int d,
            byte[] walkableData) {
        ComputeNodeInfo node = getNextNode();
        if (node == null)
            return null;

        try {
            PathfindingRequest req = PathfindingRequest.newBuilder()
                    .setStart(Vec3i.newBuilder().setX(startX).setY(startY).setZ(startZ))
                    .setGoal(Vec3i.newBuilder().setX(goalX).setY(goalY).setZ(goalZ))
                    .setTerrainWidth(w)
                    .setTerrainHeight(h)
                    .setTerrainDepth(d)
                    .setTerrainWalkable(com.google.protobuf.ByteString.copyFrom(walkableData))
                    .build();

            PathfindingResponse resp = node.stub.processPathfinding(req);

            if (resp.getSuccess()) {
                java.util.List<int[]> path = new java.util.ArrayList<>();
                for (Vec3i point : resp.getPathList()) {
                    path.add(new int[] { point.getX(), point.getY(), point.getZ() });
                }
                return path;
            }
        } catch (Exception e) {
            LOG.warning("Failed to dispatch pathfinding task to " + node.id + ": " + e.getMessage());
        }
        return null;
    }

    // === Heartbeat ===

    private final java.util.Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();

    public void handleHeartbeat(String nodeId, float cpuLoad, float gpuLoad, int tasksCompleted) {
        lastHeartbeat.put(nodeId, System.currentTimeMillis());
        LOG.fine("Heartbeat from " + nodeId + " CPU=" + cpuLoad + " GPU=" + gpuLoad);
    }

    /**
     * Check node health and remove dead nodes (no heartbeat for 60s).
     * Should be called periodically (e.g., every 30s).
     */
    public void checkHealth() {
        long now = System.currentTimeMillis();
        long timeout = 60_000; // 60 seconds

        nodes.keySet().forEach(id -> {
            Long last = lastHeartbeat.get(id);
            if (last != null && (now - last) > timeout) {
                LOG.warning("Node " + id + " timed out, removing");
                unregisterNode(id);
                lastHeartbeat.remove(id);
            }
        });
    }

    public int getNodeCount() {
        return nodes.size();
    }
}
