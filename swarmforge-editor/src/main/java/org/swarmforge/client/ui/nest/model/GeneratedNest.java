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

import java.util.ArrayList;
import java.util.List;

/**
 * Procedurally generated nest architecture graph consisting of chambers (nodes)
 * and interconnecting gallery tunnels (edges).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GeneratedNest {
    public List<NestNode> nodes = new ArrayList<>();
    public List<NestEdge> edges = new ArrayList<>();
    public double minX, maxX, minY, maxY, minZ, maxZ;
    public double surfaceY = 0;
    public double totalVolume = 0;
    public double totalCapacity = 0;
    public double totalTunnelsLength = 0;
}
