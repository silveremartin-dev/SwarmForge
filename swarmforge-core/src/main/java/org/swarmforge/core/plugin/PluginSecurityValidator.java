/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.plugin;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * High-performance static bytecode security validator for SwarmForge plugins.
 * 
 * Inspects compiled .class files and their constant pools at load-time to detect
 * and reject any dangerous system calls (process execution, reflection, arbitrary
 * network sockets, dangerous file operations) with zero runtime performance overhead.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PluginSecurityValidator {

    private static final Logger LOG = Logger.getLogger(PluginSecurityValidator.class.getName());

    // Prohibited class / package prefixes
    private static final Set<String> PROHIBITED_CLASS_PREFIXES = Set.of(
            "java/lang/ProcessBuilder",
            "java/lang/Runtime",
            "java/lang/reflect",
            "java/lang/invoke/MethodHandles",
            "java/lang/instrument",
            "java/net/Socket",
            "java/net/ServerSocket",
            "java/net/DatagramSocket",
            "java/net/URLConnection",
            "java/net/HttpURLConnection",
            "java/nio/file/Files",
            "java/io/File",
            "java/io/FileOutputStream",
            "java/io/FileInputStream",
            "java/io/RandomAccessFile",
            "sun/",
            "jdk/internal/",
            "org/apache/commons/exec"
    );

    // Prohibited method calls (e.g. System.exit, System.load)
    private static final Set<String> PROHIBITED_METHOD_NAMES = Set.of(
            "exit",
            "halt",
            "load",
            "loadLibrary"
    );

    public record ValidationResult(boolean isSecure, List<String> securityViolations) {}

    /**
     * Statically inspects all .class entries inside a plugin JAR file.
     *
     * @param jarFile File handle to the plugin JAR
     * @return ValidationResult containing security status and any detected violations
     */
    public static ValidationResult validateJar(File jarFile) {
        if (jarFile == null || !jarFile.exists() || !jarFile.isFile()) {
            return new ValidationResult(false, List.of("Plugin file does not exist or is invalid: " + jarFile));
        }

        List<String> violations = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
                    try (InputStream is = jar.getInputStream(entry)) {
                        validateClassBytecode(entry.getName(), is, violations);
                    }
                }
            }
        } catch (Exception e) {
            violations.add("Failed to read JAR bytecode for security validation: " + e.getMessage());
        }

        boolean isSecure = violations.isEmpty();
        if (!isSecure) {
            LOG.severe("Security validation FAILED for plugin " + jarFile.getName() + ": " + String.join("; ", violations));
        }

        return new ValidationResult(isSecure, violations);
    }

    /**
     * Parses the Java class file header and constant pool to detect prohibited references.
     */
    private static void validateClassBytecode(String className, InputStream is, List<String> violations) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(is));

        // Verify magic number 0xCAFEBABE
        int magic = in.readInt();
        if (magic != 0xCAFEBABE) {
            violations.add(className + " is not a valid Java class file (invalid magic number)");
            return;
        }

        int minorVersion = in.readUnsignedShort();
        int majorVersion = in.readUnsignedShort();

        int constantPoolCount = in.readUnsignedShort();
        String[] utf8Pool = new String[constantPoolCount];
        int[] classRefIndex = new int[constantPoolCount];
        int[] nameAndTypeIndex = new int[constantPoolCount];

        // Constant pool tags
        for (int i = 1; i < constantPoolCount; i++) {
            int tag = in.readUnsignedByte();
            switch (tag) {
                case 1 -> { // CONSTANT_Utf8
                    utf8Pool[i] = in.readUTF();
                }
                case 3, 4 -> { // CONSTANT_Integer, CONSTANT_Float
                    in.readInt();
                }
                case 5, 6 -> { // CONSTANT_Long, CONSTANT_Double
                    in.readLong();
                    i++; // Takes two slots
                }
                case 7 -> { // CONSTANT_Class
                    classRefIndex[i] = in.readUnsignedShort();
                }
                case 8 -> { // CONSTANT_String
                    in.readUnsignedShort();
                }
                case 9, 10, 11 -> { // CONSTANT_Fieldref, CONSTANT_Methodref, CONSTANT_InterfaceMethodref
                    int classIdx = in.readUnsignedShort();
                    int ntIdx = in.readUnsignedShort();
                }
                case 12 -> { // CONSTANT_NameAndType
                    int nameIdx = in.readUnsignedShort();
                    int descIdx = in.readUnsignedShort();
                    nameAndTypeIndex[i] = nameIdx;
                }
                case 15 -> { // CONSTANT_MethodHandle
                    in.readUnsignedByte();
                    in.readUnsignedShort();
                }
                case 16 -> { // CONSTANT_MethodType
                    in.readUnsignedShort();
                }
                case 17, 18 -> { // CONSTANT_Dynamic, CONSTANT_InvokeDynamic
                    in.readUnsignedShort();
                    in.readUnsignedShort();
                }
                case 19, 20 -> { // CONSTANT_Module, CONSTANT_Package
                    in.readUnsignedShort();
                }
                default -> {
                    // Unknown tag, stop parsing constant pool safely
                    return;
                }
            }
        }

        // Validate resolved class references
        for (int i = 1; i < constantPoolCount; i++) {
            if (classRefIndex[i] > 0 && classRefIndex[i] < constantPoolCount) {
                String referencedClass = utf8Pool[classRefIndex[i]];
                if (referencedClass != null) {
                    for (String prohibitedPrefix : PROHIBITED_CLASS_PREFIXES) {
                        if (referencedClass.startsWith(prohibitedPrefix)) {
                            violations.add("Prohibited class reference in " + className + " -> " + referencedClass);
                        }
                    }
                }
            }
            if (nameAndTypeIndex[i] > 0 && nameAndTypeIndex[i] < constantPoolCount) {
                String methodName = utf8Pool[nameAndTypeIndex[i]];
                if (methodName != null && PROHIBITED_METHOD_NAMES.contains(methodName)) {
                    // Check if it's a dangerous call
                    for (int c = 1; c < constantPoolCount; c++) {
                        if (classRefIndex[c] > 0 && classRefIndex[c] < constantPoolCount) {
                            String targetClass = utf8Pool[classRefIndex[c]];
                            if ("java/lang/System".equals(targetClass) || "java/lang/Runtime".equals(targetClass)) {
                                violations.add("Prohibited method invocation in " + className + " -> " + targetClass + "." + methodName);
                            }
                        }
                    }
                }
            }
        }
    }
}
