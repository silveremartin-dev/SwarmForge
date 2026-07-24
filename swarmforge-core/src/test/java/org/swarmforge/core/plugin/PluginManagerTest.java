/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PluginManager lifecycle and isolation.
 */
public class PluginManagerTest {

    private PluginManager pluginManager;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        pluginManager = new PluginManager();
        Terrarium terrarium = new Terrarium(10, 10, 10);
        simulation = new Simulation(terrarium);
        pluginManager.setContext(new PluginContext(simulation, pluginManager));
    }

    @Test
    void testRegisterAndNotifyTick() {
        AtomicBoolean loaded = new AtomicBoolean(false);
        AtomicLong tickReceived = new AtomicLong(-1);

        SwarmForgePlugin mockPlugin = new SwarmForgePlugin() {
            @Override
            public String getId() { return "test-plugin"; }

            @Override
            public String getName() { return "Test Plugin"; }

            @Override
            public String getVersion() { return "1.0.0"; }

            @Override
            public void onLoad(PluginContext context) {
                loaded.set(true);
            }

            @Override
            public void onUnload() {}

            @Override
            public void onTick(long tickNumber) {
                tickReceived.set(tickNumber);
            }
        };

        pluginManager.registerPlugin(mockPlugin);

        assertTrue(loaded.get(), "Plugin should be loaded");
        assertEquals(1, pluginManager.getLoadedPlugins().size());

        pluginManager.notifyTick(42L);
        assertEquals(42L, tickReceived.get(), "Plugin should receive tick notification");

        pluginManager.unloadPlugin("test-plugin");
        assertEquals(0, pluginManager.getLoadedPlugins().size());
    }

    @Test
    void testPluginExceptionIsolation() {
        AtomicBoolean secondPluginTicked = new AtomicBoolean(false);

        SwarmForgePlugin crashingPlugin = new SwarmForgePlugin() {
            @Override public String getId() { return "crashing-plugin"; }
            @Override public String getName() { return "Crashing Plugin"; }
            @Override public String getVersion() { return "1.0.0"; }
            @Override public void onLoad(PluginContext context) {}
            @Override public void onUnload() {}
            @Override public void onTick(long tickNumber) {
                throw new RuntimeException("Simulated plugin crash!");
            }
        };

        SwarmForgePlugin healthyPlugin = new SwarmForgePlugin() {
            @Override public String getId() { return "healthy-plugin"; }
            @Override public String getName() { return "Healthy Plugin"; }
            @Override public String getVersion() { return "1.0.0"; }
            @Override public void onLoad(PluginContext context) {}
            @Override public void onUnload() {}
            @Override public void onTick(long tickNumber) {
                secondPluginTicked.set(true);
            }
        };

        pluginManager.registerPlugin(crashingPlugin);
        pluginManager.registerPlugin(healthyPlugin);

        assertDoesNotThrow(() -> pluginManager.notifyTick(100L),
                "Plugin crash should be isolated and not throw to caller");
        assertTrue(secondPluginTicked.get(),
                "Healthy plugin should still execute tick even if another plugin crashed");
    }

    @Test
    void testNonExistentDirectoryHandling() {
        File fakeDir = new File("non_existent_plugin_dir_xyz123");
        assertDoesNotThrow(() -> pluginManager.loadPluginsFromDirectory(fakeDir),
                "Should safely handle non-existent plugin directory");
    }
}
