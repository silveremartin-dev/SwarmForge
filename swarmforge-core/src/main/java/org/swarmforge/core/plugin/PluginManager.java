/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Manages loading and lifecycle of plugins.
 *
 * @author Gemini AI Assistant
 */
public class PluginManager {

    private static final Logger LOG = Logger.getLogger(PluginManager.class.getName());

    private final Map<String, SwarmForgePlugin> loadedPlugins = new ConcurrentHashMap<>();
    private final Map<String, URLClassLoader> classLoaders = new ConcurrentHashMap<>();
    private PluginContext context;

    public void setContext(PluginContext context) {
        this.context = context;
    }

    /**
     * Load all plugins from a directory.
     */
    public void loadPluginsFromDirectory(File pluginDir) {
        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            LOG.warning("Plugin directory does not exist: " + pluginDir);
            return;
        }

        File[] jarFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null)
            return;

        for (File jar : jarFiles) {
            try {
                loadPlugin(jar);
            } catch (Exception e) {
                LOG.warning("Failed to load plugin: " + jar.getName() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Load a single plugin JAR with strict path and class validation.
     */
    public void loadPlugin(File jarFile) throws Exception {
        if (jarFile == null || !jarFile.exists() || !jarFile.isFile() || !jarFile.getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Invalid plugin file: " + jarFile);
        }

        File canonicalJar = jarFile.getCanonicalFile();
        URL jarUrl = canonicalJar.toURI().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[] { jarUrl }, getClass().getClassLoader());

        // Look for plugin manifest or service loader
        try (JarFile jar = new JarFile(canonicalJar)) {
            var manifest = jar.getManifest();
            if (manifest != null) {
                String pluginClass = manifest.getMainAttributes().getValue("Plugin-Class");
                if (pluginClass != null && !pluginClass.trim().isEmpty()) {
                    Class<?> clazz = loader.loadClass(pluginClass.trim());
                    if (SwarmForgePlugin.class.isAssignableFrom(clazz)) {
                        SwarmForgePlugin plugin = (SwarmForgePlugin) clazz.getDeclaredConstructor().newInstance();
                        loadedPlugins.put(plugin.getId(), plugin);
                        classLoaders.put(plugin.getId(), loader);

                        if (context != null) {
                            plugin.onLoad(context);
                        }

                        LOG.info("Loaded plugin: " + plugin.getName() + " v" + plugin.getVersion());
                    } else {
                        loader.close();
                        LOG.warning("Class " + pluginClass + " does not implement SwarmForgePlugin.");
                    }
                } else {
                    loader.close();
                }
            } else {
                loader.close();
            }
        }
    }

    /**
     * Programmatically register an in-memory plugin instance.
     */
    public void registerPlugin(SwarmForgePlugin plugin) {
        loadedPlugins.put(plugin.getId(), plugin);
        if (context != null) {
            plugin.onLoad(context);
        }
        LOG.info("Registered plugin: " + plugin.getName() + " v" + plugin.getVersion());
    }

    /**
     * Unload a plugin.
     */
    public void unloadPlugin(String pluginId) {
        SwarmForgePlugin plugin = loadedPlugins.remove(pluginId);
        if (plugin != null) {
            plugin.onUnload();
            try {
                URLClassLoader loader = classLoaders.remove(pluginId);
                if (loader != null)
                    loader.close();
            } catch (Exception e) {
                LOG.warning("Error closing plugin classloader: " + e.getMessage());
            }
        }
    }

    /**
     * Notify all plugins of a tick.
     */
    public void notifyTick(long tickNumber) {
        for (SwarmForgePlugin plugin : loadedPlugins.values()) {
            try {
                plugin.onTick(tickNumber);
            } catch (Exception e) {
                LOG.warning("Plugin " + plugin.getId() + " tick error: " + e.getMessage());
            }
        }
    }

    /**
     * Get all loaded plugins.
     */
    public Collection<SwarmForgePlugin> getLoadedPlugins() {
        return Collections.unmodifiableCollection(loadedPlugins.values());
    }

    /**
     * Unload all plugins.
     */
    public void unloadAll() {
        for (String id : new ArrayList<>(loadedPlugins.keySet())) {
            unloadPlugin(id);
        }
    }
}
