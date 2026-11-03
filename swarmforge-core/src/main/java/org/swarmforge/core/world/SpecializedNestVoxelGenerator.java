/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

/**
 * Procedural voxel generator for specialized biological nest architectures
 * (Hexagonal wax comb, Termite cathedral, Formica mound, Weaver silk capsule,
 * Leafcutter fungal vault, Carton nest, etc.).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpecializedNestVoxelGenerator {

    private final Terrarium terrarium;

    public SpecializedNestVoxelGenerator(Terrarium terrarium) {
        this.terrarium = terrarium;
    }

    public void setMaterial(int x, int y, int z, TerrariumCell.Material material) {
        if (terrarium != null && terrarium.inBounds(x, y, z)) {
            TerrariumCell cell = new TerrariumCell(
                    x, y, z, material,
                    new float[TerrariumCell.PHEROMONE_TYPES], 20f, 60f);
            terrarium.setCell(cell);
        }
    }

    public void carveEllipsoidChamber(int cx, int cy, int cz, int rx, int ry, int rz) {
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dy = -ry; dy <= ry; dy++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    float val = (dx * dx) / (float) (rx * rx) +
                                (dy * dy) / (float) (ry * ry) +
                                (dz * dz) / (float) (rz * rz);
                    if (val <= 1.0f) {
                        setMaterial(cx + dx, cy + dy, cz + dz, TerrariumCell.Material.CHAMBER);
                    }
                }
            }
        }
    }

    public int generateWaxCombHexagonal(int startX, int startY, int startZ, float scale) {
        int numFrames = Math.max(1, (int) (3 * scale));
        int frameSpacing = 6;
        int frameWidth = (int) (20 * scale);
        int frameHeight = (int) (26 * scale);
        float cellRadius = 2.2f;
        int cellCount = 0;

        for (int f = 0; f < numFrames; f++) {
            int fy = startY - ((numFrames - 1) * frameSpacing) / 2 + f * frameSpacing;

            for (int r = 0; r < frameHeight / 3; r++) {
                for (int c = 0; c < frameWidth / 3; c++) {
                    float cx = startX - frameWidth / 2f + c * (cellRadius * 1.732f);
                    float cz = startZ - frameHeight / 2f + r * (cellRadius * 1.5f) + (c % 2 == 1 ? cellRadius * 0.75f : 0f);

                    for (int angle = 0; angle < 360; angle += 60) {
                        float rad = (float) Math.toRadians(angle);
                        float wx = cx + (float) Math.cos(rad) * cellRadius;
                        float wz = cz + (float) Math.sin(rad) * cellRadius;
                        setMaterial((int) wx, fy, (int) wz, TerrariumCell.Material.BEESWAX);
                        setMaterial((int) wx, fy + 1, (int) wz, TerrariumCell.Material.BEESWAX);
                    }

                    setMaterial((int) cx, fy, (int) cz, TerrariumCell.Material.CHAMBER);
                    setMaterial((int) cx, fy + 1, (int) cz, TerrariumCell.Material.CHAMBER);
                    cellCount++;

                    if (r >= (frameHeight / 3) * 0.8) {
                        setMaterial((int) cx, fy + 2, (int) cz, TerrariumCell.Material.BEESWAX);
                    }
                }
            }
        }
        return cellCount;
    }

    public int generateCathedralMound(int startX, int startY, int startZ, float scale) {
        int spireHeight = (int) (30 * scale);
        int baseRadius = (int) (12 * scale);
        int chamberCount = 0;

        for (int dz = 0; dz <= spireHeight; dz++) {
            float taper = 1.0f - ((float) dz / spireHeight);
            int currentRadius = Math.max(1, (int) (baseRadius * Math.pow(taper, 0.7)));

            for (int dx = -currentRadius; dx <= currentRadius; dx++) {
                for (int dy = -currentRadius; dy <= currentRadius; dy++) {
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist <= currentRadius) {
                        setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.STERCORAL_CEMENT);
                    }
                }
            }
            if (dz > 2 && dz < spireHeight - 3) {
                setMaterial(startX, startY, startZ + dz, TerrariumCell.Material.AIR);
                setMaterial(startX + 1, startY, startZ + dz, TerrariumCell.Material.AIR);
            }
        }

        int royalZ = startZ - 12;
        carveEllipsoidChamber(startX, startY, royalZ, 8, 8, 3);
        setMaterial(startX, startY, royalZ, TerrariumCell.Material.CHAMBER);
        chamberCount++;

        int[][] offset = {{10, 0}, {-10, 0}, {0, 10}, {0, -10}};
        for (int[] o : offset) {
            carveEllipsoidChamber(startX + o[0], startY + o[1], royalZ - 4, 5, 5, 4);
            chamberCount++;
        }

        return chamberCount;
    }

    public int generateFormicaMound(int startX, int startY, int startZ, float scale) {
        int domeHeight = (int) (16 * scale);
        int domeRadius = (int) (20 * scale);
        int chamberCount = 0;

        for (int dz = 0; dz <= domeHeight; dz++) {
            float normZ = (float) dz / domeHeight;
            int rAtZ = (int) (domeRadius * Math.sqrt(1.0f - normZ));

            for (int dx = -rAtZ; dx <= rAtZ; dx++) {
                for (int dy = -rAtZ; dy <= rAtZ; dy++) {
                    if (dx * dx + dy * dy <= rAtZ * rAtZ) {
                        setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.LEAF_LITTER);
                    }
                }
            }
        }

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                carveEllipsoidChamber(startX + i * 8, startY + j * 8, startZ + 6, 4, 4, 3);
                chamberCount++;
            }
        }

        for (int depth = 1; depth <= 4; depth++) {
            int subZ = Math.max(1, startZ - depth * 8);
            carveEllipsoidChamber(startX, startY, subZ, 6, 6, 3);
            chamberCount++;
        }

        return chamberCount;
    }

    public int generateArborealSilkLeaf(int startX, int startY, int startZ, float scale) {
        int radiusX = (int) (10 * scale);
        int radiusY = (int) (12 * scale);
        int radiusZ = (int) (9 * scale);
        int chamberCount = 0;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    float dist = (dx * dx) / (float) (radiusX * radiusX) +
                                 (dy * dy) / (float) (radiusY * radiusY) +
                                 (dz * dz) / (float) (radiusZ * radiusZ);
                    if (dist >= 0.75f && dist <= 1.0f) {
                        setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.SILK_WEAVE);
                    } else if (dist < 0.75f) {
                        setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.CHAMBER);
                    }
                }
            }
        }
        chamberCount += 4;
        return chamberCount;
    }

    public int generatePaperPedunculate(int startX, int startY, int startZ, float scale) {
        int height = (int) (18 * scale);
        int radius = (int) (12 * scale);
        int chamberCount = 0;

        for (int pz = startZ; pz < startZ + 6; pz++) {
            setMaterial(startX, startY, pz, TerrariumCell.Material.WOOD_PULP_PAPER);
        }

        for (int dz = -height; dz <= 0; dz++) {
            float norm = (float) Math.abs(dz) / height;
            int rAtZ = (int) (radius * Math.sin(norm * Math.PI));

            for (int dx = -rAtZ; dx <= rAtZ; dx++) {
                for (int dy = -rAtZ; dy <= rAtZ; dy++) {
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (Math.abs(dist - rAtZ) <= 1.2f) {
                        setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.WOOD_PULP_PAPER);
                    }
                }
            }
        }

        for (int tier = 1; tier <= 3; tier++) {
            int tierZ = startZ - tier * 5;
            int tierRadius = radius - 3;
            for (int dx = -tierRadius; dx <= tierRadius; dx += 2) {
                for (int dy = -tierRadius; dy <= tierRadius; dy += 2) {
                    if (dx * dx + dy * dy <= tierRadius * tierRadius) {
                        setMaterial(startX + dx, startY + dy, tierZ, TerrariumCell.Material.CHAMBER);
                        setMaterial(startX + dx + 1, startY + dy, tierZ, TerrariumCell.Material.WOOD_PULP_PAPER);
                        chamberCount++;
                    }
                }
            }
        }

        return chamberCount;
    }

    public int generateWaxPotsCluster(int startX, int startY, int startZ, float scale) {
        int numPots = Math.max(4, (int) (12 * scale));
        int potRadius = 3;
        int potCount = 0;

        for (int p = 0; p < numPots; p++) {
            float angle = p * (360f / numPots);
            float dist = (p % 3) * 4f;
            int px = startX + (int) (Math.cos(Math.toRadians(angle)) * dist);
            int py = startY + (int) (Math.sin(Math.toRadians(angle)) * dist);
            int pz = startZ + (p / 3) * 3;

            for (int dx = -potRadius; dx <= potRadius; dx++) {
                for (int dy = -potRadius; dy <= potRadius; dy++) {
                    for (int dz = -potRadius; dz <= potRadius; dz++) {
                        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (Math.abs(d - potRadius) <= 0.8f) {
                            setMaterial(px + dx, py + dy, pz + dz, TerrariumCell.Material.BEESWAX);
                        } else if (d < potRadius - 0.8f) {
                            setMaterial(px + dx, py + dy, pz + dz, TerrariumCell.Material.CHAMBER);
                        }
                    }
                }
            }
            setMaterial(px, py, pz + potRadius, TerrariumCell.Material.AIR);
            potCount++;
        }
        return potCount;
    }

    public int generateSubterraneanFungiVault(int startX, int startY, int startZ, float scale) {
        int numVaults = Math.max(3, (int) (8 * scale));
        int chamberCount = 0;

        int minZ = Math.max(0, startZ - 35);
        for (int z = startZ; z >= minZ; z--) {
            setMaterial(startX, startY, z, TerrariumCell.Material.AIR);
            setMaterial(startX + 1, startY, z, TerrariumCell.Material.AIR);
        }

        for (int v = 0; v < numVaults; v++) {
            int depthZ = Math.max(2, startZ - 10 - (v * 5));
            float angle = v * (360f / numVaults);
            int dist = 12;
            int vx = startX + (int) (Math.cos(Math.toRadians(angle)) * dist);
            int vy = startY + (int) (Math.sin(Math.toRadians(angle)) * dist);

            int steps = 8;
            for (int s = 0; s <= steps; s++) {
                int tx = startX + (int) ((vx - startX) * (s / (float) steps));
                int ty = startY + (int) ((vy - startY) * (s / (float) steps));
                setMaterial(tx, ty, depthZ, TerrariumCell.Material.AIR);
            }

            carveEllipsoidChamber(vx, vy, depthZ, 6, 6, 4);
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = -4; dy <= 4; dy++) {
                    setMaterial(vx + dx, vy + dy, depthZ - 1, TerrariumCell.Material.FUNGUS_GARDEN);
                    setMaterial(vx + dx, vy + dy, depthZ - 2, TerrariumCell.Material.FUNGUS_GARDEN);
                }
            }
            chamberCount++;
        }
        return chamberCount;
    }

    public int generateCartonNest(int startX, int startY, int startZ, float scale) {
        int radiusX = (int) (12 * scale);
        int radiusY = (int) (10 * scale);
        int radiusZ = (int) (14 * scale);
        int chamberCount = 0;

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    float dist = (dx * dx) / (float) (radiusX * radiusX) +
                                 (dy * dy) / (float) (radiusY * radiusY) +
                                 (dz * dz) / (float) (radiusZ * radiusZ);
                    if (dist <= 1.0f) {
                        if ((dx + dy + dz) % 3 == 0) {
                            setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.WOOD_PULP_PAPER);
                        } else {
                            setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.CHAMBER);
                            chamberCount++;
                        }
                    }
                }
            }
        }
        return Math.max(1, chamberCount / 20);
    }

    public int generateBambooStemNest(int startX, int startY, int startZ, float scale) {
        int height = (int) (30 * scale);
        int outerRadius = 3;
        int chamberCount = 0;

        for (int z = startZ; z < startZ + height; z++) {
            for (int dx = -outerRadius; dx <= outerRadius; dx++) {
                for (int dy = -outerRadius; dy <= outerRadius; dy++) {
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist >= outerRadius - 0.8f && dist <= outerRadius) {
                        setMaterial(startX + dx, startY + dy, z, TerrariumCell.Material.BAMBOO_STEM);
                    } else if (dist < outerRadius - 0.8f) {
                        if (z % 10 == 0) {
                            setMaterial(startX + dx, startY + dy, z, TerrariumCell.Material.BAMBOO_STEM);
                        } else {
                            setMaterial(startX + dx, startY + dy, z, TerrariumCell.Material.CHAMBER);
                            chamberCount++;
                        }
                    }
                }
            }
        }
        setMaterial(startX + outerRadius, startY, startZ + 5, TerrariumCell.Material.AIR);
        return Math.max(1, chamberCount / 10);
    }

    public int generateBivouacLivingNest(int startX, int startY, int startZ, float scale) {
        int radius = (int) (10 * scale);
        int chamberCount = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist >= radius - 2 && dist <= radius) {
                        setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.NEST_WALL);
                    } else if (dist < radius - 2) {
                        setMaterial(startX + dx, startY + dy, startZ + dz, TerrariumCell.Material.CHAMBER);
                        chamberCount++;
                    }
                }
            }
        }
        return Math.max(1, chamberCount / 15);
    }

    public int generateHollowTreeTrunk(int startX, int startY, int startZ, float scale) {
        int height = (int) (24 * scale);
        int outerRadius = Math.max(4, (int) (9 * scale));
        int innerRadius = Math.max(2, outerRadius - 3);
        int chamberCount = 0;

        for (int dz = 0; dz < height; dz++) {
            int currentZ = startZ + dz;
            float taper = 1.0f - (dz / (float) height) * 0.2f;
            int curOuterR = Math.max(3, (int) (outerRadius * taper));
            int curInnerR = Math.max(1, (int) (innerRadius * taper));

            for (int dx = -curOuterR; dx <= curOuterR; dx++) {
                for (int dy = -curOuterR; dy <= curOuterR; dy++) {
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);

                    if (dist <= curOuterR) {
                        if (dist > curOuterR - 1.2f) {
                            setMaterial(startX + dx, startY + dy, currentZ, TerrariumCell.Material.TREE_TRUNK);
                        } else if (dist > curInnerR) {
                            setMaterial(startX + dx, startY + dy, currentZ, TerrariumCell.Material.DEAD_WOOD);
                        } else {
                            if (dz % 6 == 0 && dist < curInnerR * 0.8f) {
                                setMaterial(startX + dx, startY + dy, currentZ, TerrariumCell.Material.PROPOLIS);
                            } else {
                                setMaterial(startX + dx, startY + dy, currentZ, TerrariumCell.Material.CHAMBER);
                                chamberCount++;
                            }
                        }
                    }
                }
            }
        }

        int entranceZ1 = startZ + Math.max(2, height / 3);
        int entranceZ2 = startZ + Math.max(4, (2 * height) / 3);
        for (int r = innerRadius; r <= outerRadius + 1; r++) {
            setMaterial(startX + r, startY, entranceZ1, TerrariumCell.Material.AIR);
            setMaterial(startX - r, startY, entranceZ2, TerrariumCell.Material.AIR);
        }

        return Math.max(1, chamberCount / 12);
    }
}
