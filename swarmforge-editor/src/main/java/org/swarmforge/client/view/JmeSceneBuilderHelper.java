/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import com.jme3.util.TangentBinormalGenerator;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class encapsulating terrain mesh generation, environment visual synchronization,
 * ommatidial compound-eye overlay construction, and timelapse frame recording for {@link JmeGameApp}.
 */
class JmeSceneBuilderHelper {

    private final JmeGameApp app;

    JmeSceneBuilderHelper(JmeGameApp app) {
        this.app = app;
    }

    void renderTerrarium(Terrarium terrarium) {
        app.lastTerrarium = terrarium;
        app.enqueueTask(() -> {
            if (app.terrainNode != null) {
                app.terrainNode.removeFromParent();
            }
            app.terrainNode = new Node("Terrain");

            int w = terrarium.getWidth();
            int h = terrarium.getHeight();

            Material soilMat;
            if (app.isGamifiedVoxelMode) {
                soilMat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
                soilMat.setBoolean("UseMaterialColors", true);
                soilMat.setBoolean("UseVertexColor", false);
                soilMat.setColor("Diffuse", ColorRGBA.White);
                soilMat.setColor("Ambient", new ColorRGBA(0.90f, 0.90f, 0.90f, 1f));
                Texture atlasTex = VoxelTextureAtlas.getAtlasTexture();
                soilMat.setTexture("DiffuseMap", atlasTex);
            } else {
                soilMat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
                soilMat.setBoolean("UseMaterialColors", true);
                soilMat.setBoolean("UseVertexColor", true);
                soilMat.setColor("Diffuse", ColorRGBA.White);
                soilMat.setColor("Ambient", new ColorRGBA(0.70f, 0.72f, 0.75f, 1f));
                soilMat.setColor("Specular", new ColorRGBA(0.20f, 0.20f, 0.22f, 1f));
                soilMat.setFloat("Shininess", 16f);

                double lat = Math.abs(terrarium.getLatitude());
                int sandCount = 0, rockCount = 0, grassCount = 0;
                int sampleStep = Math.max(1, terrarium.getWidth() / 16);
                for (int sx = 0; sx < terrarium.getWidth(); sx += sampleStep) {
                    for (int sy = 0; sy < terrarium.getHeight(); sy += sampleStep) {
                        float elev = terrarium.getSurfaceElevation(sx, sy);
                        int sz = Math.max(0, Math.min(terrarium.getDepth() - 1, (int) elev));
                        TerrariumCell cell = terrarium.getCell(sx, sy, sz);
                        if (cell != null) {
                            switch (cell.material()) {
                                case SAND -> sandCount++;
                                case ROCK, GRAVEL -> rockCount++;
                                default -> grassCount++;
                            }
                        }
                    }
                }

                String diffusePath;
                String normalPath;
                if (lat > 60.0) {
                    diffusePath = "textures/3td_RockGround_01.JPG";
                    normalPath = "textures/3td_RockGround_01_NRM.png";
                } else if (sandCount > grassCount && sandCount > rockCount) {
                    diffusePath = "textures/3td_sand_01_256.png";
                    normalPath = "textures/3td_ground_03_DET.png";
                } else if (rockCount > grassCount) {
                    diffusePath = "textures/3td_RockGround_01.JPG";
                    normalPath = "textures/3td_RockGround_01_NRM.png";
                } else {
                    diffusePath = "textures/3td_ground_03.jpg";
                    normalPath = "textures/3td_ground_03_DET.png";
                }

                try {
                    Texture diffuseTex = app.getAssetManager().loadTexture(diffusePath);
                    diffuseTex.setWrap(Texture.WrapMode.Repeat);
                    diffuseTex.setMinFilter(Texture.MinFilter.Trilinear);
                    diffuseTex.setMagFilter(Texture.MagFilter.Bilinear);
                    soilMat.setTexture("DiffuseMap", diffuseTex);
                } catch (Exception e) {
                    try {
                        Texture fallbackTex = app.getAssetManager().loadTexture("textures/3td_AfricaGrass_Dry.png");
                        fallbackTex.setWrap(Texture.WrapMode.Repeat);
                        soilMat.setTexture("DiffuseMap", fallbackTex);
                    } catch (Exception ignored) {}
                }
                try {
                    Texture normalTex = app.getAssetManager().loadTexture(normalPath);
                    normalTex.setWrap(Texture.WrapMode.Repeat);
                    soilMat.setTexture("NormalMap", normalTex);
                } catch (Exception ignored) {}
            }

            soilMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

            TerrainMeshGenerator generator = new TerrainMeshGenerator();
            Mesh terrainMesh = generator.generateMesh(terrarium, app.slicePlaneRatio, app.showSkirt, app.isGamifiedVoxelMode);
            if (!app.isGamifiedVoxelMode) {
                try {
                    Logger tbLog = Logger.getLogger("com.jme3.util.TangentBinormalGenerator");
                    tbLog.setLevel(Level.OFF);
                    tbLog.setUseParentHandlers(false);
                    TangentBinormalGenerator.generate(terrainMesh);
                } catch (Exception ignored) {}
            }
            Geometry terrainGeom = new Geometry("TerrainMesh", terrainMesh);
            terrainGeom.setMaterial(soilMat);
            terrainGeom.setShadowMode(RenderQueue.ShadowMode.Receive);
            app.terrainNode.attachChild(terrainGeom);

            if (!app.isGamifiedVoxelMode) {
                Mesh waterMesh = generator.generateWaterMesh(terrarium, app.slicePlaneRatio);
                if (waterMesh != null) {
                    Geometry waterGeom = new Geometry("WaterMesh", waterMesh);
                    Material waterMat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
                    waterMat.setBoolean("UseMaterialColors", true);
                    waterMat.setColor("Diffuse", new ColorRGBA(0.18f, 0.58f, 0.92f, 0.85f));
                    waterMat.setColor("Ambient", new ColorRGBA(0.15f, 0.45f, 0.75f, 0.85f));
                    waterMat.setColor("Specular", new ColorRGBA(0.95f, 0.98f, 1.0f, 1.0f));
                    waterMat.setFloat("Shininess", 64f);
                    waterMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
                    waterMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
                    waterGeom.setMaterial(waterMat);
                    waterGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
                    app.terrainNode.attachChild(waterGeom);
                }
            }

            if (app.showElevationIsolines) {
                Mesh isoMesh = generator.generateIsolinesMesh(terrarium, app.slicePlaneRatio, 2.0f);
                if (isoMesh != null) {
                    Geometry isoGeom = new Geometry("ElevationIsolines", isoMesh);
                    Material isoMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                    isoMat.setColor("Color", new ColorRGBA(0.35f, 0.85f, 1.0f, 0.9f));
                    isoGeom.setMaterial(isoMat);
                    app.terrainNode.attachChild(isoGeom);
                }
            }

            app.getRootNode().attachChild(app.terrainNode);

            if (app.weatherVisualizer != null) {
                app.weatherVisualizer.setWorldDimensions(w, h);
            }

            if (app.vegetationVisualizer != null) {
                app.vegetationVisualizer.setLatitude(terrarium.getLatitude());
                app.vegetationVisualizer.rebuildVegetation(w, h, terrarium, app.simulation != null ? app.simulation.getVegetationSystem() : null);
            }

            if (app.tunnelVisualizer != null) {
                app.tunnelVisualizer.setTerrarium(terrarium);
                app.tunnelVisualizer.setTerrainDimensions(10.0f, w);
            }
            if (app.insectVisualizer != null) {
                app.insectVisualizer.setTerrainDimensions(10.0f, w);
            }

            float surfaceY = terrarium.getSurfaceElevation(w / 2f, h / 2f);
            if (app.pheromoneVisualizer == null) {
                app.pheromoneVisualizer = new PheromoneVisualizer(app.getAssetManager());
                app.pheromoneVisualizer.initialize(w, h, surfaceY);
                app.getRootNode().attachChild(app.pheromoneVisualizer.getRootNode());
            } else {
                app.pheromoneVisualizer.initialize(w, h, surfaceY);
            }

            if (!app.initialCameraConfigured) {
                app.initialCameraConfigured = true;
                float targetX = w / 2f;
                float targetZ = h / 2f;
                float targetY = surfaceY;
                if (app.simulation != null && !app.simulation.getColonies().isEmpty()) {
                    Colony colony = app.simulation.getColonies().get(0);
                    targetX = colony.getNestX();
                    targetZ = colony.getNestY();
                    targetY = terrarium.getSurfaceElevation(targetX, targetZ);
                }
                app.cameraTarget.set(targetX, targetY, targetZ);
                Camera cam = app.getCamera();
                cam.setLocation(new Vector3f(targetX, targetY + 16.0f, targetZ + 22.0f));
                cam.lookAt(new Vector3f(targetX, targetY + 0.5f, targetZ), Vector3f.UNIT_Y);
            }
        });
    }

    void checkAndUpdateNestArchitectures() {
        if (app.vegetationVisualizer == null || app.simulation == null) return;
        List<Colony> colonies = app.simulation.getColonies();
        int signature = (colonies != null ? colonies.size() * 31 : 0);
        if (colonies != null) {
            for (Colony c : colonies) {
                signature = signature * 31 + Float.floatToIntBits(c.getNestX()) + Float.floatToIntBits(c.getNestY());
                if (c.getSpecies() != null && c.getSpecies().getNestType() != null) {
                    signature = signature * 31 + c.getSpecies().getNestType().hashCode();
                }
            }
        }
        if (signature != app.lastColonySignature || app.currentRenderMode != app.lastNestRenderMode) {
            app.lastColonySignature = signature;
            app.lastNestRenderMode = app.currentRenderMode;
            app.vegetationVisualizer.updateNestArchitectures(colonies, app.currentRenderMode);
            if (colonies != null) {
                for (Colony colony : colonies) {
                    String spName = colony.getSpecies() != null ? colony.getSpecies().getCommonName().toLowerCase() : "";
                    String arch = colony.getSpecies() != null && colony.getSpecies().getNestType() != null ? colony.getSpecies().getNestType().toUpperCase() : "";
                    if (spName.contains("wasp") || spName.contains("guêpe") || spName.contains("vespula") || arch.contains("PAPER") || arch.contains("ARBOREAL")) {
                        app.vegetationVisualizer.ensureHostTreeForWaspNest(colony.getNestX(), colony.getNestY(), colony.getNestZ());
                    }
                }
            }
        }
    }

    void updateEnvironmentVisuals(float tpf) {
        if (app.tunnelVisualizer == null) {
            app.tunnelVisualizer = new TunnelVisualizer(app.getAssetManager());
            app.getRootNode().attachChild(app.tunnelVisualizer.getRootNode());
        }
        if (app.weatherVisualizer == null) {
            app.weatherVisualizer = new WeatherVisualizer(app.getAssetManager(), app.sunLight);
            app.getRootNode().attachChild(app.weatherVisualizer.getRootNode());
        }
        if (app.vegetationVisualizer == null) {
            app.vegetationVisualizer = new VegetationVisualizer(app.getAssetManager());
            if (app.simulation != null && app.simulation.getTerrarium() != null) {
                app.vegetationVisualizer.setLatitude(app.simulation.getTerrarium().getLatitude());
            }
            app.vegetationVisualizer.rebuildVegetation(
                app.simulation != null && app.simulation.getTerrarium() != null ? app.simulation.getTerrarium().getWidth() : 64,
                app.simulation != null && app.simulation.getTerrarium() != null ? app.simulation.getTerrarium().getHeight() : 64,
                app.simulation != null ? app.simulation.getTerrarium() : null,
                app.simulation != null ? app.simulation.getVegetationSystem() : null
            );
            app.getRootNode().attachChild(app.vegetationVisualizer.getRootNode());
        }

        if (app.simulation != null) {
            if (app.simulation.getTerrarium() != null) {
                app.vegetationVisualizer.setLatitude(app.simulation.getTerrarium().getLatitude());
            }
            if (app.simulation.getSeasonManager() != null) {
                app.vegetationVisualizer.setSeason(app.simulation.getSeasonManager().getCurrentSeason());
            }
            checkAndUpdateNestArchitectures();
            for (Colony colony : app.simulation.getColonies()) {
                if (colony.getTunnelNetwork() != null) {
                    if (colony.getSpecies() != null) {
                        float reqDiam = colony.getSpecies().getRequiredTunnelDiameterMm();
                        if (reqDiam > 0) {
                            app.tunnelVisualizer.setCustomGalleryDiameterMm(Math.max(4.0f, reqDiam));
                        }
                    }
                    app.tunnelVisualizer.update(colony.getTunnelNetwork());
                }
            }
            if (app.simulation.getWeather() != null) {
                app.weatherVisualizer.update(app.simulation.getWeather(), tpf);
            } else if (app.weatherVisualizer != null) {
                app.weatherVisualizer.update(null, tpf);
            }
            if (app.vegetationVisualizer != null) {
                app.vegetationVisualizer.update(app.simulation.getWeather(), tpf);
            }
        } else {
            if (app.weatherVisualizer != null) {
                app.weatherVisualizer.update(null, tpf);
            }
            if (app.vegetationVisualizer != null) {
                app.vegetationVisualizer.update(null, tpf);
            }
        }
    }

    Node createOmmatidialGuiOverlay() {
        Node ommatidialGuiNode = new Node("OmmatidialScreenOverlay");

        float screenW = app.width > 0 ? (float) app.width : 1280f;
        float screenH = app.height > 0 ? (float) app.height : 800f;

        float hexRadius = 22.0f;
        float hexWidth = hexRadius * 1.73205f;
        float rowHeight = hexRadius * 1.5f;

        int cols = (int) Math.ceil(screenW / hexWidth) + 2;
        int rows = (int) Math.ceil(screenH / rowHeight) + 2;

        List<Float> verts = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int indexOffset = 0;

        Material facetMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        facetMat.setBoolean("VertexColor", true);
        facetMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        facetMat.getAdditionalRenderState().setDepthWrite(false);

        for (int r = 0; r < rows; r++) {
            float yCenter = r * rowHeight;
            float xOffset = (r % 2 == 1) ? hexWidth * 0.5f : 0.0f;
            for (int c = 0; c < cols; c++) {
                float xCenter = c * hexWidth + xOffset;

                float dx = (xCenter - screenW * 0.5f) / (screenW * 0.5f);
                float dy = (yCenter - screenH * 0.5f) / (screenH * 0.5f);
                float distSq = dx * dx + dy * dy;
                float borderAlpha = Math.min(0.50f, 0.16f + distSq * 0.32f);

                for (int i = 0; i < 6; i++) {
                    float angle1 = (float) (Math.PI / 3.0 * i + Math.PI / 6.0);
                    float angle2 = (float) (Math.PI / 3.0 * ((i + 1) % 6) + Math.PI / 6.0);

                    float x1 = xCenter + hexRadius * (float) Math.cos(angle1);
                    float y1 = yCenter + hexRadius * (float) Math.sin(angle1);
                    float x2 = xCenter + hexRadius * (float) Math.cos(angle2);
                    float y2 = yCenter + hexRadius * (float) Math.sin(angle2);

                    float perpX = -(y2 - y1);
                    float perpY = (x2 - x1);
                    float len = (float) Math.sqrt(perpX * perpX + perpY * perpY);
                    if (len > 0.001f) {
                        perpX = (perpX / len) * 0.75f;
                        perpY = (perpY / len) * 0.75f;
                    }

                    verts.add(x1 + perpX); verts.add(y1 + perpY); verts.add(0.0f);
                    verts.add(x1 - perpX); verts.add(y1 - perpY); verts.add(0.0f);
                    verts.add(x2 + perpX); verts.add(y2 + perpY); verts.add(0.0f);
                    verts.add(x2 - perpX); verts.add(y2 - perpY); verts.add(0.0f);

                    for (int k = 0; k < 4; k++) {
                        colors.add(0.12f); colors.add(0.22f); colors.add(0.35f); colors.add(borderAlpha);
                    }

                    indices.add(indexOffset + 0); indices.add(indexOffset + 1); indices.add(indexOffset + 2);
                    indices.add(indexOffset + 2); indices.add(indexOffset + 1); indices.add(indexOffset + 3);
                    indexOffset += 4;
                }
            }
        }

        if (!verts.isEmpty()) {
            Mesh facetMesh = new Mesh();
            facetMesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(toFloatArray(verts)));
            facetMesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(toFloatArray(colors)));
            facetMesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(toIntArray(indices)));
            facetMesh.updateBound();

            Geometry facetGeom = new Geometry("FacetGridScreen", facetMesh);
            facetGeom.setMaterial(facetMat);
            facetGeom.setQueueBucket(RenderQueue.Bucket.Gui);
            ommatidialGuiNode.attachChild(facetGeom);
        }
        return ommatidialGuiNode;
    }

    void saveFrame(byte[] pixelData, int width, int height, File recordingDir, int frameNumber) {
        final byte[] frameData = pixelData.clone();
        Thread saveThread = new Thread(() -> {
            try {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                int[] intPixels = new int[width * height];

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int i = ((height - 1 - y) * width + x) * 4;
                        int b = frameData[i] & 0xFF;
                        int g = frameData[i + 1] & 0xFF;
                        int r = frameData[i + 2] & 0xFF;
                        int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                        intPixels[y * width + x] = argb;
                    }
                }
                image.setRGB(0, 0, width, height, intPixels, 0, width);

                File file = new File(recordingDir, String.format("frame_%05d.png", frameNumber));
                ImageIO.write(image, "png", file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "SwarmForge-FrameSaver-" + frameNumber);
        saveThread.setDaemon(true);
        saveThread.start();
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}
