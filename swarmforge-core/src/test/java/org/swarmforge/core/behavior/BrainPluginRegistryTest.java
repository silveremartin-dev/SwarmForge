/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.behavior.json.JsonBrainArchitecture;
import org.swarmforge.core.simulation.SimulationContext;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit and integration tests for BrainPluginRegistry and dynamic cognitive architectures.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class BrainPluginRegistryTest {

    private BrainPluginRegistry registry;

    @BeforeEach
    void setUp() {
        registry = BrainPluginRegistry.getInstance();
    }

    @Test
    @DisplayName("Verify default built-in architectures are registered on startup")
    void testBuiltInArchitecturesRegistered() {
        assertTrue(registry.hasBrain("BDI"));
        assertTrue(registry.hasBrain("BEHAVIOR_TREE"));
        assertTrue(registry.hasBrain("FINITE_STATE_MACHINE"));
        assertTrue(registry.hasBrain("FUZZY_LOGIC"));
        assertTrue(registry.hasBrain("NEURAL_NETWORK"));
        assertTrue(registry.hasBrain("HYBRID"));
        assertTrue(registry.hasBrain("BLACKBOARD"));

        ReasoningArchitecture bdi = registry.createBrain("BDI");
        assertNotNull(bdi);
        assertEquals(ReasoningArchitecture.ArchitectureType.BDI, bdi.getType());
    }

    @Test
    @DisplayName("Verify declarative .sfbrain / JSON behavior tree parsing and evaluation")
    void testJsonBrainParsingAndEvaluation() throws Exception {
        String jsonContent = """
        {
          "id": "FORAGER_HARVEST_TREE",
          "name": "Aggressive Forager Behavior Tree",
          "description": "High-priority food harvesting and territorial defense",
          "root": {
            "type": "SELECTOR",
            "children": [
              {
                "type": "CONDITION",
                "check": "IS_SOLDIER",
                "action": "ATTACK",
                "parameter": 1.0
              },
              {
                "type": "CONDITION",
                "check": "CARRYING_LOAD",
                "action": "RETURN_HOME",
                "parameter": 1.0
              },
              {
                "type": "ACTION",
                "action": "FORAGE",
                "parameter": 1.0
              }
            ]
          }
        }
        """;

        File tempFile = File.createTempFile("test_brain_", ".sfbrain");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(jsonContent);
        }

        CustomBrainDescriptor descriptor = registry.importJsonBrain(tempFile);
        assertNotNull(descriptor);
        assertEquals(CustomBrainDescriptor.BrainSourceType.DECLARATIVE_JSON_GRAPH, descriptor.getSourceType());
        assertTrue(registry.hasBrain(descriptor.getId()));

        ReasoningArchitecture brain = registry.createBrain(descriptor.getId());
        assertNotNull(brain);
        assertEquals("Aggressive Forager Behavior Tree", brain.getName());

        AgentView mockAgent = mock(AgentView.class);
        SimulationContext mockContext = mock(SimulationContext.class);

        // Case 1: Soldier -> should ATTACK
        when(mockAgent.isSoldier()).thenReturn(true);
        ReasoningArchitecture.Action action1 = brain.decide(mockAgent, mockContext);
        assertNotNull(action1);
        assertEquals(ReasoningArchitecture.Action.ActionType.ATTACK, action1.type());

        // Case 2: Not soldier, carrying load -> should RETURN_HOME
        when(mockAgent.isSoldier()).thenReturn(false);
        when(mockAgent.isCarryingFood()).thenReturn(true);
        ReasoningArchitecture.Action action2 = brain.decide(mockAgent, mockContext);
        assertNotNull(action2);
        assertEquals(ReasoningArchitecture.Action.ActionType.RETURN_HOME, action2.type());

        // Case 3: Not soldier, no load -> should FORAGE
        when(mockAgent.isCarryingFood()).thenReturn(false);
        ReasoningArchitecture.Action action3 = brain.decide(mockAgent, mockContext);
        assertNotNull(action3);
        assertEquals(ReasoningArchitecture.Action.ActionType.FORAGE, action3.type());
    }

    @Test
    @DisplayName("Verify custom brain descriptor dynamic registration and unregistration")
    void testRegisterCustomBrain() {
        String customId = "CUSTOM_TEST_BRAIN_V1";
        CustomBrainDescriptor custom = new CustomBrainDescriptor(
                customId,
                "Custom Test Brain",
                "Test cognitive module",
                CustomBrainDescriptor.BrainSourceType.JAVA_PLUGIN,
                "test.jar",
                "Test Author",
                "1.0.0",
                Map.of(),
                FSMArchitecture::new
        );

        registry.registerBrain(custom);
        assertTrue(registry.hasBrain(customId));

        ReasoningArchitecture created = registry.createBrain(customId);
        assertNotNull(created);

        registry.unregisterBrain(customId);
        assertFalse(registry.hasBrain(customId));
    }
}
