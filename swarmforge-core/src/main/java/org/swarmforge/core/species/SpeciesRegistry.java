/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal Data-Driven Species Registry loading and caching species presets
 * from JSON files following the universal naming pattern `swarmforge-species-[id].json`.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesRegistry {

    private static final Logger log = LoggerFactory.getLogger(SpeciesRegistry.class);
    private static final SpeciesRegistry INSTANCE = new SpeciesRegistry();

    private final Map<String, CustomSpecies> speciesMap = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private SpeciesRegistry() {
        loadDefaultPresets();
    }

    public static SpeciesRegistry getInstance() {
        return INSTANCE;
    }

    public void registerSpecies(String id, CustomSpecies species) {
        if (id != null && species != null) {
            speciesMap.put(id.toLowerCase(), species);
            log.info("Registered species JSON preset [{}] -> {}", id, species.getCommonName());
        }
    }

    public void register(CustomSpecies species) {
        if (species != null) {
            String id = species.getScientificName() != null ? species.getScientificName() : species.getPresetName();
            if (id != null) {
                registerSpecies(id.toLowerCase().replaceAll("[^a-z0-9]+", "-"), species);
            }
        }
    }

    public Optional<Species> get(String name) {
        if (name == null) return Optional.empty();
        String key = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        CustomSpecies found = speciesMap.get(key);
        if (found == null) {
            // Search by scientific name or common name
            for (CustomSpecies s : speciesMap.values()) {
                if (s.getScientificName() != null && s.getScientificName().equalsIgnoreCase(name)) {
                    return Optional.of(s);
                }
                if (s.getCommonName() != null && s.getCommonName().equalsIgnoreCase(name)) {
                    return Optional.of(s);
                }
            }
        }
        return Optional.ofNullable(found);
    }

    public CustomSpecies getSpecies(String id) {
        if (id == null || id.isBlank()) return getFallbackSpecies();
        // 1. Direct match with lowercased key
        String rawKey = id.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        CustomSpecies found = speciesMap.get(rawKey);
        if (found != null) return found;

        // 2. Strip parenthetical descriptors (e.g. "Polyergus rufescens (Amazon Raiding Party)" -> "Polyergus rufescens")
        String cleaned = id.replaceAll("\\s*\\([^)]*\\)", "").trim();
        String cleanedKey = cleaned.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        found = speciesMap.get(cleanedKey);
        if (found != null) return found;

        // 3. Search by scientific name, common name, or preset name matching
        for (CustomSpecies s : speciesMap.values()) {
            if (s.getScientificName() != null) {
                String sc = s.getScientificName().toLowerCase();
                if (cleaned.toLowerCase().contains(sc) || sc.contains(cleaned.toLowerCase())) return s;
            }
            if (s.getCommonName() != null) {
                String cn = s.getCommonName().toLowerCase();
                if (cleaned.toLowerCase().contains(cn) || cn.contains(cleaned.toLowerCase())) return s;
            }
            if (s.getPresetName() != null) {
                String pn = s.getPresetName().toLowerCase();
                if (cleaned.toLowerCase().contains(pn) || pn.contains(cleaned.toLowerCase())) return s;
            }
        }
        return getFallbackSpecies();
    }

    public Collection<CustomSpecies> getAllSpecies() {
        return Collections.unmodifiableCollection(speciesMap.values());
    }

    public Set<String> getAvailableSpeciesIds() {
        return Collections.unmodifiableSet(speciesMap.keySet());
    }

    private void loadDefaultPresets() {
        // Builtin eusocial species registry
        register(new AttaCephalotes());
        register(new ApisMellifera());
        register(new BombusTerrestris());
        register(new VespulaGermanica());
        register(new VespaCrabro());
        register(new FormicaRufa());
        register(new LasiusNiger());
        register(new SolenopsisInvicta());
        register(new CamponotusPennsylvanicus());
        register(new LinepithemaHumile());
        register(new ReticulitermesFlavipes());
        register(new PseudoregmaBambucicola());
        register(new KladothripsHarteri());
        register(new AustroplatypusIncompertus());
        register(new PolyergusRufescens());
        register(new FormicaFusca());

        // Load custom user JSON files from ~/.swarmforge/presets/species/
        try {
            Path userDir = Path.of(System.getProperty("user.home"), ".swarmforge", "presets", "species");
            if (Files.exists(userDir)) {
                try (var stream = Files.list(userDir)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                        try {
                            CustomSpecies s = mapper.readValue(path.toFile(), CustomSpecies.class);
                            String filename = path.getFileName().toString();
                            String id = filename.replace("swarmforge-species-", "").replace(".json", "");
                            registerSpecies(id, s);
                        } catch (Exception ex) {
                            log.warn("Failed to parse species JSON preset from {}", path, ex);
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.warn("Could not scan user species presets directory", e);
        }
    }

    private CustomSpecies getFallbackSpecies() {
        CustomSpecies fallback = new CustomSpecies();
        fallback.setCommonName("Fourmi Générique");
        fallback.setScientificName("Formica genericus");
        return fallback;
    }
}
