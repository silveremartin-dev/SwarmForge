/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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
        MOUND, // Surface mound construction
        TREE // Arboreal nest
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
        String axiom = getAxiom(type);
        String rules = applyRules(axiom, type, (int) (3 * size));
        return executeL(x, y, z, rules);
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
            };
        }
        return switch (type) {
            case SIMPLE -> "F[C]F[-FC][+FC]";
            case MATURE -> "FC[--F[C]F[+FC][-FC]][++F[C]F[+FC][-FC]]vFvFC";
            case MOUND -> "^F^F[C]vvF[-FC][+FC]vF[C]";
            case TREE -> "F^F[C][+F^FC][-F^FC]";
        };
    }
}
