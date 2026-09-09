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
 * Parses 3D vertex positions, normalizes submesh geometries around their local origin at Y=0 ground,
 * and allows instant lookup by submesh name.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class ObjModelLoader {

    public static class ObjMesh {
        public String name = "default";
        public final List<float[]> vertices = new ArrayList<>();
        public final List<int[]> faces = new ArrayList<>();
        public float height = 1.0f;
        public float radius = 0.5f;

        /**
         * Normalizes vertex coordinates using global model origin so sub-parts
         * (like tree trunk base and canopy crown) maintain exact relative positions and heights.
         */
        public void normalizeWithGlobalBounds(float globalCenterX, float globalMinY, float globalCenterZ, float globalHeight, float globalRadius) {
            if (faces.isEmpty() || vertices.isEmpty()) return;
            this.height = globalHeight;
            this.radius = globalRadius;

            Map<Integer, Integer> indexMap = new HashMap<>();
            List<float[]> localVerts = new ArrayList<>();

            for (int f = 0; f < faces.size(); f++) {
                int[] face = faces.get(f);
                int[] newFace = new int[face.length];
                for (int i = 0; i < face.length; i++) {
                    int oldIdx = face[i];
                    if (oldIdx >= 0 && oldIdx < vertices.size()) {
                        if (!indexMap.containsKey(oldIdx)) {
                            float[] v = vertices.get(oldIdx);
                            int newIdx = localVerts.size();
                            localVerts.add(new float[]{
                                v[0] - globalCenterX,
                                v[1] - globalMinY,
                                v[2] - globalCenterZ
                            });
                            indexMap.put(oldIdx, newIdx);
                        }
                        newFace[i] = indexMap.get(oldIdx);
                    }
                }
                faces.set(f, newFace);
            }

            vertices.clear();
            vertices.addAll(localVerts);
        }
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
                        if (!currentMesh.faces.isEmpty()) {
                            currentMesh = new ObjMesh();
                            currentMesh.name = name;
                            meshes.add(currentMesh);
                        } else {
                            currentMesh.name = name;
                        }
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

                // Compute global model bounding box across all vertices referenced by all faces
                float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
                float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
                float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
                boolean hasAnyVerts = false;

                for (ObjMesh mesh : meshes) {
                    for (int[] face : mesh.faces) {
                        for (int vIdx : face) {
                            if (vIdx >= 0 && vIdx < globalVertices.size()) {
                                float[] v = globalVertices.get(vIdx);
                                if (v[0] < minX) minX = v[0];
                                if (v[0] > maxX) maxX = v[0];
                                if (v[1] < minY) minY = v[1];
                                if (v[1] > maxY) maxY = v[1];
                                if (v[2] < minZ) minZ = v[2];
                                if (v[2] > maxZ) maxZ = v[2];
                                hasAnyVerts = true;
                            }
                        }
                    }
                }

                float gCenterX = hasAnyVerts ? (minX + maxX) * 0.5f : 0f;
                float gMinY = hasAnyVerts ? minY : 0f;
                float gCenterZ = hasAnyVerts ? (minZ + maxZ) * 0.5f : 0f;
                float gHeight = hasAnyVerts ? Math.max(0.01f, maxY - minY) : 1f;
                float gRadius = hasAnyVerts ? Math.max(0.01f, Math.max(maxX - minX, maxZ - minZ) * 0.5f) : 0.5f;

                // Normalize all sub-meshes with global reference
                List<ObjMesh> validMeshes = new ArrayList<>();
                for (ObjMesh mesh : meshes) {
                    if (!mesh.faces.isEmpty()) {
                        mesh.vertices.addAll(globalVertices);
                        mesh.normalizeWithGlobalBounds(gCenterX, gMinY, gCenterZ, gHeight, gRadius);
                        validMeshes.add(mesh);
                    }
                }
                meshes = validMeshes;
            }
        } catch (Exception e) {
            System.err.println("[ObjModelLoader] Error loading " + resourcePath + ": " + e.getMessage());
        }

        cache.put(resourcePath, meshes);
        return meshes;
    }

    public static ObjMesh findSubmesh(List<ObjMesh> list, String keyword) {
        if (list == null || list.isEmpty() || keyword == null) return null;
        String kw = keyword.toLowerCase();
        for (ObjMesh m : list) {
            if (m.name != null && m.name.toLowerCase().contains(kw)) {
                return m;
            }
        }
        return list.get(0);
    }
}
