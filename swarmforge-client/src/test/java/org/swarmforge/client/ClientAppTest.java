/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SwarmForge Client application properties and configuration.
 */
public class ClientAppTest {

    @Test
    void testClientAppInstantiation() {
        ClientApp clientApp = new ClientApp();
        assertNotNull(clientApp, "ClientApp instance should be created");
    }
}
