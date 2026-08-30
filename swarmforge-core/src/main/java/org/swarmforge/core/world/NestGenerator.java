/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import java.util.Random;
import java.util.Stack;

/**
 * Procedural nest generator using L-systems.
 * Creates realistic ant nest structures with chambers and tunnels.
 * 
 * L-system rules:
 * - F: Move forward, dig tunnel
 * - +: Turn right
 * - -: Turn left
 * - ^: Pitch up
 * - v: Pitch down
 * - [: Push state (branch)
 * - ]: Pop state (return from branch)
 * - C: Create chamber
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestGenerator {

    public enum NestType {
        SIMPLE, // Small founding nest
        MATURE, // Full colony nest
        MOUND, // Surface mound construction (Formica rufa)
        TREE, // Arboreal nest
        WAX_COMB_HEXAGONAL, // Honeybee vertical hexagonal comb (Apis mellifera)
        WAX_POTS_CLUSTER,   // Bumblebee pot cluster (Bombus)
        PAPER_PEDUNCULATE,  // Wasp/Hornet paper nest (Vespidae)
        CATHEDRAL_MOUND,    // Termite cathedral mound (Macrotermes/Nasutitermes)
        ARBOREAL_SILK_LEAF, // Weaver ant stitched leaf nest (Oecophylla)
        SUBTERRANEAN_FUNGI_VAULT, // Leafcutter fungus cavern nest (Atta/Acromyrmex)
        CARTON_NEST,        // Chewed wood carton tree nest (Crematogaster/Azteca)
        BAMBOO_STEM_NEST,   // Hollow plant stem nest (Pseudomyrmex/Cataulacus)
        BIVOUAC_LIVING_NEST // Army ant living body bivouac (Eciton/Dorylus)
    }

    private final Random random;
    private final Terrarium terrarium;

    // L-system parameters
    private float tunnelRadius = 0.5f;
    private float chamberRadius = 2.0f;
    private float branchAngle = 30f;
    private int maxDepth = 50;

    public NestGenerator(Terrarium terrarium) {
        this.terrarium = terrarium;
        this.random = new Random();
    }

    public NestGenerator(Terrarium terrarium, long seed) {
        this.terrarium = terrarium;
        this.random = new Random(seed);
    }

    /**
     * Generate a nest at the specified position.
     *
     * @param x    Entrance X position
     * @param y    Entrance Y position
     * @param z    Entrance Z position (ground level)
     * @param type Type of nest to generate
     * @param size Size multiplier (1.0 = normal)
     * @return Number of chambers created
     */
    public int generate(int x, int y, int z, NestType type, float size) {
        return switch (type) {
            case WAX_COMB_HEXAGONAL -> generateWaxCombHexagonal(x, y, z, size);
            case WAX_POTS_CLUSTER -> generateWaxPotsCluster(x, y, z, size);
            case CATHEDRAL_MOUND -> generateCathedralMound(x, y, z, size);
            case MOUND -> generateFormicaMound(x, y, z, size);
            case ARBOREAL_SILK_LEAF -> generateArborealSilkLeaf(x, y, z, size);
            case PAPER_PEDUNCULATE -> generatePaperPedunculate(x, y, z, size);
            case SUBTERRANEAN_FUNGI_VAULT -> generateSubterraneanFungiVault(x, y, z, size);
            case CARTON_NEST -> generateCartonNest(x, y, z, size);
            case BAMBOO_STEM_NEST -> generateBambooStemNest(x, y, z, size);
            case BIVOUAC_LIVING_NEST -> generateBivouacLivingNest(x, y, z, size);
            default -> {
                String axiom = getAxiom(type);
                String rules = applyRules(axiom, type, (int) (3 * size));
                yield executeL(x, y, z, rules);
            }
        };
    }

    /**
     * Generates a realistic vertical honeybee comb structure composed of parallel wax sheets
     * with double-sided 2D hexagonal cell tessellation.
     */
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

    /**
     * Generates a termite cathedral mound with central ventilation spires,
     * thermoregulatory chimneys, royal chamber, and subterranean nursery vaults.
     */
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

    /**
     * Generates a Formica rufa pine-needle mound with a parabolic solar-thatch dome
     * and subterranean wintering galleries.
     */
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
            int subZ = startZ - depth * 8;
            carveEllipsoidChamber(startX, startY, subZ, 6, 6, 3);
            chamberCount++;
        }

        return chamberCount;
    }

    /**
     * Generates an arboreal silk-stitched leaf capsule nest (Weaver ants).
     */
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

    /**
     * Generates a pedunculate paper nest (Wasps / Hornets) with hanging peduncle
     * and horizontal paper comb tiers.
     */
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

    /**
     * Generates a bumblebee wax pot cluster (Bombus) consisting of irregular wax/propolis pots
     * for honey/pollen storage and larval rearing.
     */
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

    /**
     * Generates a leafcutter ant nest (Atta / Acromyrmex) with cavernous subterranean vaults
     * containing cultivated fungus gardens.
     */
    public int generateSubterraneanFungiVault(int startX, int startY, int startZ, float scale) {
        int numVaults = Math.max(3, (int) (8 * scale));
        int chamberCount = 0;

        for (int z = startZ; z >= startZ - 35; z--) {
            setMaterial(startX, startY, z, TerrariumCell.Material.AIR);
            setMaterial(startX + 1, startY, z, TerrariumCell.Material.AIR);
        }

        for (int v = 0; v < numVaults; v++) {
            int depthZ = startZ - 10 - (v * 5);
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

    /**
     * Generates a chewed wood carton nest (Crematogaster / Azteca) attached to trees.
     */
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

    /**
     * Generates a hollow plant/bamboo stem nest (Pseudomyrmex / Cataulacus).
     */
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

    /**
     * Generates a living body bivouac nest (Eciton / Dorylus army ants).
     */
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

    private void carveEllipsoidChamber(int cx, int cy, int cz, int rx, int ry, int rz) {
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

    private String applyRules(String input, NestType type, int iterations) {
        String result = input;
        for (int i = 0; i < iterations; i++) {
            StringBuilder sb = new StringBuilder();
            for (char c : result.toCharArray()) {
                sb.append(expandRule(c, type));
            }
            result = sb.toString();
            if (result.length() > 1000)
                break; // Limit complexity
        }
        return result;
    }

    private String expandRule(char c, NestType type) {
        return switch (c) {
            case 'F' -> type == NestType.MATURE ? "FF" : "F";
            case 'C' -> "C";
            case '[', ']', '+', '-', '^', 'v' -> String.valueOf(c);
            default -> "";
        };
    }

    /**
     * Execute L-system string and carve the nest.
     */
    private int executeL(int startX, int startY, int startZ, String lstring) {
        Stack<TurtleState> stack = new Stack<>();
        TurtleState turtle = new TurtleState(startX, startY, startZ, 0, -90); // Start going down

        int chambers = 0;

        for (char c : lstring.toCharArray()) {
            switch (c) {
                case 'F' -> {
                    // Move forward and dig tunnel
                    float dx = (float) (Math.cos(Math.toRadians(turtle.yaw)) * Math.cos(Math.toRadians(turtle.pitch)));
                    float dy = (float) (Math.sin(Math.toRadians(turtle.yaw)) * Math.cos(Math.toRadians(turtle.pitch)));
                    float dz = (float) Math.sin(Math.toRadians(turtle.pitch));

                    for (int step = 0; step < 3; step++) {
                        turtle.x += dx;
                        turtle.y += dy;
                        turtle.z += dz;
                        carveTunnel((int) turtle.x, (int) turtle.y, (int) turtle.z);
                    }
                }
                case '+' -> turtle.yaw += branchAngle + random.nextFloat() * 10;
                case '-' -> turtle.yaw -= branchAngle + random.nextFloat() * 10;
                case '^' -> turtle.pitch += 20 + random.nextFloat() * 10;
                case 'v' -> turtle.pitch -= 20 + random.nextFloat() * 10;
                case '[' -> stack.push(turtle.copy());
                case ']' -> {
                    if (!stack.isEmpty())
                        turtle = stack.pop();
                }
                case 'C' -> {
                    carveChamber((int) turtle.x, (int) turtle.y, (int) turtle.z);
                    chambers++;
                }
            }

            // Clamp depth
            if (turtle.z < -maxDepth)
                turtle.z = -maxDepth;
        }

        return chambers;
    }

    private void carveTunnel(int cx, int cy, int cz) {
        int r = (int) Math.ceil(tunnelRadius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                if (dx * dx + dy * dy <= tunnelRadius * tunnelRadius) {
                    setAir(cx + dx, cy + dy, cz);
                }
            }
        }
    }

    private void carveChamber(int cx, int cy, int cz) {
        int r = (int) Math.ceil(chamberRadius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    float dist = dx * dx + dy * dy + dz * dz;
                    if (dist <= chamberRadius * chamberRadius) {
                        setChamber(cx + dx, cy + dy, cz + dz);
                    }
                }
            }
        }
    }

    private void setAir(int x, int y, int z) {
        if (terrarium.inBounds(x, y, z)) {
            TerrariumCell cell = TerrariumCell.air(x, y, z);
            terrarium.setCell(cell);
        }
    }

    private void setChamber(int x, int y, int z) {
        if (terrarium.inBounds(x, y, z)) {
            TerrariumCell cell = new TerrariumCell(
                    x, y, z, TerrariumCell.Material.CHAMBER,
                    new float[TerrariumCell.PHEROMONE_TYPES], 18f, 80f);
            terrarium.setCell(cell);
        }
    }

    private void setMaterial(int x, int y, int z, TerrariumCell.Material material) {
        if (terrarium.inBounds(x, y, z)) {
            TerrariumCell cell = new TerrariumCell(
                    x, y, z, material,
                    new float[TerrariumCell.PHEROMONE_TYPES], 20f, 60f);
            terrarium.setCell(cell);
        }
    }



    // Turtle state for L-system execution
    private static class TurtleState {
        float x, y, z;
        float yaw, pitch;

        TurtleState(float x, float y, float z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        TurtleState copy() {
            return new TurtleState(x, y, z, yaw, pitch);
        }
    }

    // Builder pattern for configuration
    public NestGenerator tunnelRadius(float r) {
        this.tunnelRadius = r;
        return this;
    }

    public NestGenerator chamberRadius(float r) {
        this.chamberRadius = r;
        return this;
    }

    public NestGenerator branchAngle(float a) {
        this.branchAngle = a;
        return this;
    }

    public NestGenerator maxDepth(int d) {
        this.maxDepth = d;
        return this;
    }

    // New config options
    private int branchingFactor = 2;
    private java.util.Map<String, Integer> chamberCounts = new java.util.HashMap<>();

    public NestGenerator branchingFactor(int f) {
        this.branchingFactor = Math.max(1, f);
        return this;
    }

    public void setChamberCounts(java.util.Map<String, Integer> counts) {
        if (counts != null) {
            this.chamberCounts.putAll(counts);
        }
    }

    // Override execute method to respect new params somewhat more accurately
    // For now we stick to the L-System but we can tweak rules based on branching
    // factor
    private String getAxiom(NestType type) {
        // Adjust complexity based on branching factor
        if (branchingFactor > 3) {
            return switch (type) {
                case SIMPLE -> "F[C]F[-FC][+FC][^FC][vFC]";
                case MATURE -> "FC[--F[C]F[+FC][-FC]][++F[C]F[+FC][-FC]][^F[C]][vF[C]]";
                case MOUND -> "^F^F[C]vvF[-FC][+FC]vF[C][^FC]";
                case TREE -> "F^F[C][+F^FC][-F^FC][+FvFC][-FvFC]";
                case WAX_COMB_HEXAGONAL -> "FC[+C][-C][^C][vC]";
                case WAX_POTS_CLUSTER -> "C[+C][-C][^C][vC]";
                case PAPER_PEDUNCULATE -> "^F[C][+C][-C][vC]";
                case CATHEDRAL_MOUND -> "^F^F^FC[+FC][-FC][^FC]";
                case ARBOREAL_SILK_LEAF -> "^FC[+C][-C]";
                default -> "FC[+C][-C][^C][vC]";
            };
        }
        return switch (type) {
            case SIMPLE -> "F[C]F[-FC][+FC]";
            case MATURE -> "FC[--F[C]F[+FC][-FC]][++F[C]F[+FC][-FC]]vFvFC";
            case MOUND -> "^F^F[C]vvF[-FC][+FC]vF[C]";
            case TREE -> "F^F[C][+F^FC][-F^FC]";
            case WAX_COMB_HEXAGONAL -> "FC[+C][-C]";
            case WAX_POTS_CLUSTER -> "C[+C][-C]";
            case PAPER_PEDUNCULATE -> "^F[C][+C]";
            case CATHEDRAL_MOUND -> "^F^FC[+FC]";
            case ARBOREAL_SILK_LEAF -> "^FC[+C]";
            default -> "FC[+C][-C]";
        };
    }
}
