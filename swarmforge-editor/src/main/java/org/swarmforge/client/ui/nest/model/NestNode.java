/*
 * SwarmForge - Ant Colony & Insect Simulation Platform
 * Copyright (c) 2024-2026 Silvère Martin-Michiellot & SwarmForge Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.swarmforge.client.ui.nest.model;

/**
 * Represents a single chamber or junction node in the procedural nest architecture.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestNode {
    public double x, y, z;
    public String type;
    public double r;
    public double currentCapacity = 0.0;
    public double maxCapacity = 100.0;
    public double moisturePct = 50.0;
    public double co2Ppm = 400.0;

    public NestNode() {
        this(0, 0, 0, "tunnel", 1.0);
    }

    public NestNode(double x, double y, double z, String type, double r) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.r = r;
    }
}
