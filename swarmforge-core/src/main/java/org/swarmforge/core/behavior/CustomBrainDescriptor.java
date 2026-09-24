/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Metadata descriptor for cognitive reasoning architectures and brain plugins.
 * Encapsulates identification, category, operational capabilities, and factory instantiation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class CustomBrainDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum BrainSourceType {
        BUILTIN,
        ONNX_MODEL,
        JAVA_PLUGIN,
        DECLARATIVE_JSON_GRAPH
    }

    private final String id;
    private final String displayName;
    private final String description;
    private final BrainSourceType sourceType;
    private final String sourcePath;
    private final String author;
    private final String version;
    private final Map<String, String> metadata;
    private final transient Supplier<ReasoningArchitecture> factory;

    public CustomBrainDescriptor(
            String id,
            String displayName,
            String description,
            BrainSourceType sourceType,
            String sourcePath,
            String author,
            String version,
            Map<String, String> metadata,
            Supplier<ReasoningArchitecture> factory) {
        this.id = Objects.requireNonNull(id, "id must not be null").trim();
        this.displayName = displayName != null && !displayName.isBlank() ? displayName : id;
        this.description = description != null ? description : "";
        this.sourceType = Objects.requireNonNull(sourceType, "sourceType must not be null");
        this.sourcePath = sourcePath != null ? sourcePath : "";
        this.author = author != null ? author : "SwarmForge Lab";
        this.version = version != null ? version : "1.0.0";
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.factory = factory;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public BrainSourceType getSourceType() {
        return sourceType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public Supplier<ReasoningArchitecture> getFactory() {
        return factory;
    }

    public ReasoningArchitecture createInstance() {
        if (factory != null) {
            return factory.get();
        }
        return ReasoningArchitecture.create(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomBrainDescriptor that = (CustomBrainDescriptor) o;
        return id.equalsIgnoreCase(that.id);
    }

    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s [%s v%s] (%s)", displayName, id, version, sourceType);
    }
}
