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
 * Represents a connection or tunnel segment between two nest nodes.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestEdge {
    public int from;
    public int to;
    public double width;

    public NestEdge() {
        this(0, 0, 1.0);
    }

    public NestEdge(int from, int to, double width) {
        this.from = from;
        this.to = to;
        this.width = width;
    }
}
