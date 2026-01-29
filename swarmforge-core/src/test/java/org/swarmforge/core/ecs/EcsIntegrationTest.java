/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecs;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.ecs.components.*;
import org.swarmforge.core.ecs.systems.*;
import static org.junit.jupiter.api.Assertions.*;

public class EcsIntegrationTest {

    @Test
    public void testFullEcsPipeline() {
        java.util.UUID colonyId = java.util.UUID.randomUUID();
        WorldConfiguration config = new WorldConfigurationBuilder()
                // Core
                .with(new MovementSystem())
                .with(new MetabolismSystem())
                .with(new AgingSystem())
                // AI
                .with(new AiSystem())
                // Ecology
                .with(new SoilSystem())
                .build();

        World world = new World(config);

        // Create Entity (Worker)
        int entityId = world.create();
        world.edit(entityId)
             .create(PositionComponent.class).x = 10;
        
        world.edit(entityId).create(PositionComponent.class).y = 0;
        world.edit(entityId).create(PositionComponent.class).z = 10;
        
        world.edit(entityId)
             .create(VelocityComponent.class).speed = 1.0f;
             
        world.edit(entityId)
             .create(MetabolismComponent.class);
             
        world.edit(entityId)
             .create(AiComponent.class).type = AiComponent.AiType.SIMPLE_FORAGER; // Test Switch
             
        world.edit(entityId)
             .create(ColonyComponent.class).colonyId = colonyId;
             
        // Register in ColonyRegistry to avoid NPE in ForagingSystem
        org.swarmforge.core.domain.Colony mockColony = new org.swarmforge.core.domain.Colony(new org.swarmforge.core.species.LasiusNiger(), 50, 0, 50);
        // Force the ID to match
        java.lang.reflect.Field idField;
        try {
            idField = org.swarmforge.core.domain.Colony.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockColony, colonyId);
        } catch (Exception e) {}
        org.swarmforge.core.ecs.ColonyRegistry.register(mockColony);

        // Create Soil Entity
        int soilEntityId = world.create();
        world.edit(soilEntityId).create(SoilComponent.class).moisture = 0.5f;

        world.process();
        
        // Assert initial state
        PositionComponent pos = world.getMapper(PositionComponent.class).get(entityId);
        assertNotNull(pos);
        
        // Run loop
        for (int i = 0; i < 100; i++) {
            world.setDelta(0.1f);
            world.process();
        }
        
        // Assert movement happened (Random walk will move it somewhere)
        assertFalse(pos.x == 10 && pos.z == 10, "Entity should have moved");
        
        // Assert Metadata
        MetabolismComponent meta = world.getMapper(MetabolismComponent.class).get(entityId);
        assertTrue(meta.energy < 100f, "Entity should have consumed energy");

        // Assert Soil dynamics
        SoilComponent soil = world.getMapper(SoilComponent.class).get(soilEntityId);
        assertTrue(soil.moisture < 0.5f, "Soil moisture should decrease via evaporation");
    }
}
