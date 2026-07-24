/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.compute;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComputeNodeApp configuration and startup defaults.
 */
public class ComputeNodeAppTest {

    @Test
    void testComputeNodeInstantiation() {
        ComputeNodeApp app = new ComputeNodeApp("localhost", 50051, 50059, 4, false);
        assertNotNull(app, "ComputeNodeApp instance should be created successfully");
    }
}
