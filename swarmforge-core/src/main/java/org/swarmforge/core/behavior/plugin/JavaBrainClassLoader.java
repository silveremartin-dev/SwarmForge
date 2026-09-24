/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.plugin;

import org.swarmforge.core.behavior.ReasoningArchitecture;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dynamic isolated class loader for Java bytecode insect brain plugins (.jar / .class).
 * Safely inspects and verifies compliance with {@link ReasoningArchitecture}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class JavaBrainClassLoader {

    private static final Logger LOG = Logger.getLogger(JavaBrainClassLoader.class.getName());

    private JavaBrainClassLoader() {}

    /**
     * Inspects a JAR archive and finds all classes implementing {@link ReasoningArchitecture}.
     *
     * @param jarFile the JAR file to inspect
     * @return list of discovered brain classes
     */
    @SuppressWarnings("unchecked")
    public static List<Class<? extends ReasoningArchitecture>> findBrainClasses(File jarFile) throws IOException {
        List<Class<? extends ReasoningArchitecture>> classes = new ArrayList<>();
        if (jarFile == null || !jarFile.exists()) return classes;

        URL[] urls = new URL[]{jarFile.toURI().toURL()};
        URLClassLoader classLoader = new URLClassLoader(urls, ReasoningArchitecture.class.getClassLoader());

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class") || entry.getName().contains("$")) {
                    continue;
                }

                String className = entry.getName()
                        .replace('/', '.')
                        .replace('\\', '.')
                        .substring(0, entry.getName().length() - 6);

                try {
                    Class<?> loadedClass = Class.forName(className, false, classLoader);
                    if (ReasoningArchitecture.class.isAssignableFrom(loadedClass) && !loadedClass.isInterface() && !java.lang.reflect.Modifier.isAbstract(loadedClass.getModifiers())) {
                        classes.add((Class<? extends ReasoningArchitecture>) loadedClass);
                    }
                } catch (Throwable t) {
                    LOG.log(Level.FINE, "Skipping non-loadable class: " + className, t);
                }
            }
        }

        return classes;
    }

    /**
     * Loads a specific class by name from a JAR file.
     *
     * @param jarFile the JAR file
     * @param className fully qualified class name
     * @return instance of the loaded brain
     */
    public static ReasoningArchitecture loadBrainFromJar(File jarFile, String className) throws Exception {
        URL[] urls = new URL[]{jarFile.toURI().toURL()};
        URLClassLoader classLoader = new URLClassLoader(urls, ReasoningArchitecture.class.getClassLoader());
        Class<?> clazz = Class.forName(className, true, classLoader);

        if (!ReasoningArchitecture.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class " + className + " does not implement " + ReasoningArchitecture.class.getName());
        }

        return (ReasoningArchitecture) clazz.getDeclaredConstructor().newInstance();
    }
}
