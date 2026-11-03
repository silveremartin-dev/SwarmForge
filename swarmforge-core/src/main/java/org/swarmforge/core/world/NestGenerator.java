/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

/**
 * Procedural nest generator using L-systems and specialized biological voxel templates.
 * Creates realistic ant, termite, wasp, and bee nest structures with chambers and tunnels.
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
    private final SpecializedNestVoxelGenerator specializedGenerator;

    // L-system parameters
    private float tunnelRadius = 0.5f;
    private float chamberRadius = 2.0f;
    private float branchAngle = 30f;
    private int maxDepth = 50;
    private int branchingFactor = 2;
    private final Map<String, Integer> chamberCounts = new HashMap<>();

    public NestGenerator(Terrarium terrarium) {
        this(terrarium, 1337L);
    }

    public NestGenerator(Terrarium terrarium, long seed) {
        this.terrarium = terrarium;
        this.random = new Random(seed);
        this.specializedGenerator = new SpecializedNestVoxelGenerator(terrarium);
    }

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
            case TREE -> generateHollowTreeTrunk(x, y, z, size);
            default -> {
                String axiom = getAxiom(type);
                String rules = applyRules(axiom, type, (int) (3 * size));
                yield executeL(x, y, z, rules);
            }
        };
    }

    // === Specialized Nest Generator Delegations ===

    public int generateWaxCombHexagonal(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateWaxCombHexagonal(startX, startY, startZ, scale);
    }

    public int generateCathedralMound(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateCathedralMound(startX, startY, startZ, scale);
    }

    public int generateFormicaMound(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateFormicaMound(startX, startY, startZ, scale);
    }

    public int generateArborealSilkLeaf(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateArborealSilkLeaf(startX, startY, startZ, scale);
    }

    public int generatePaperPedunculate(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generatePaperPedunculate(startX, startY, startZ, scale);
    }

    public int generateWaxPotsCluster(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateWaxPotsCluster(startX, startY, startZ, scale);
    }

    public int generateSubterraneanFungiVault(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateSubterraneanFungiVault(startX, startY, startZ, scale);
    }

    public int generateCartonNest(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateCartonNest(startX, startY, startZ, scale);
    }

    public int generateBambooStemNest(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateBambooStemNest(startX, startY, startZ, scale);
    }

    public int generateBivouacLivingNest(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateBivouacLivingNest(startX, startY, startZ, scale);
    }

    public int generateHollowTreeTrunk(int startX, int startY, int startZ, float scale) {
        return specializedGenerator.generateHollowTreeTrunk(startX, startY, startZ, scale);
    }

    // === L-System Grammar & Voxel Carving ===

    private String applyRules(String input, NestType type, int iterations) {
        String result = input;
        for (int i = 0; i < iterations; i++) {
            StringBuilder sb = new StringBuilder();
            for (char c : result.toCharArray()) {
                sb.append(expandRule(c, type));
            }
            result = sb.toString();
            if (result.length() > 1000) break;
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

    private int executeL(int startX, int startY, int startZ, String lstring) {
        Stack<TurtleState> stack = new Stack<>();
        TurtleState turtle = new TurtleState(startX, startY, startZ, 0, -90);
        int chambers = 0;

        for (char c : lstring.toCharArray()) {
            switch (c) {
                case 'F' -> {
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
                    if (!stack.isEmpty()) turtle = stack.pop();
                }
                case 'C' -> {
                    carveChamber((int) turtle.x, (int) turtle.y, (int) turtle.z);
                    chambers++;
                }
            }

            if (turtle.z < 0) {
                turtle.z = 0;
            } else if (terrarium != null && turtle.z >= terrarium.getDepth()) {
                turtle.z = terrarium.getDepth() - 1;
            }
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
        for (int dx = -(int) chamberRadius; dx <= (int) chamberRadius; dx++) {
            for (int dy = -(int) chamberRadius; dy <= (int) chamberRadius; dy++) {
                for (int dz = -(int) chamberRadius; dz <= (int) chamberRadius; dz++) {
                    float dist = dx * dx + dy * dy + dz * dz;
                    if (dist <= chamberRadius * chamberRadius) {
                        setChamber(cx + dx, cy + dy, cz + dz);
                    }
                }
            }
        }
    }

    private void setAir(int x, int y, int z) {
        if (terrarium != null && terrarium.inBounds(x, y, z)) {
            terrarium.setCell(TerrariumCell.air(x, y, z));
        }
    }

    private void setChamber(int x, int y, int z) {
        if (terrarium != null && terrarium.inBounds(x, y, z)) {
            TerrariumCell cell = new TerrariumCell(
                    x, y, z, TerrariumCell.Material.CHAMBER,
                    new float[TerrariumCell.PHEROMONE_TYPES], 18f, 80f);
            terrarium.setCell(cell);
        }
    }

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

    public NestGenerator branchingFactor(int f) {
        this.branchingFactor = Math.max(1, f);
        return this;
    }

    public void setChamberCounts(Map<String, Integer> counts) {
        if (counts != null) {
            this.chamberCounts.putAll(counts);
        }
    }

    private String getAxiom(NestType type) {
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
