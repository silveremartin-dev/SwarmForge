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

    private static final List<String> BUILTIN_SPECIES_FILES = List.of(
            "apis-mellifera.json",
            "atta-cephalotes.json",
            "atta-sexdens.json",
            "austroplatypus-incompertus.json",
            "bombus-terrestris.json",
            "camponotus-ligniperda.json",
            "camponotus-pennsylvanicus.json",
            "cataglyphis-bombycina.json",
            "formica-fusca.json",
            "formica-rufa.json",
            "kladothrips-harteri.json",
            "lasius-niger.json",
            "linepithema-humile.json",
            "messor-barbarus.json",
            "odontomachus-bauri.json",
            "pogonomyrmex-barbatus.json",
            "polyergus-rufescens.json",
            "pseudoregma-bambucicola.json",
            "reticulitermes-flavipes.json",
            "solenopsis-invicta.json",
            "vespa-crabro.json",
            "vespula-germanica.json",
            "vespula-vulgaris.json"
    );

    private static final SpeciesRegistry INSTANCE = new SpeciesRegistry();

    private final Map<String, DefaultSpecies> speciesMap = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final java.util.concurrent.atomic.AtomicBoolean defaultsLoaded = new java.util.concurrent.atomic.AtomicBoolean(false);

    private SpeciesRegistry() {
        loadDefaultPresets();
    }

    public static SpeciesRegistry getInstance() {
        return INSTANCE;
    }

    public void registerSpecies(String id, DefaultSpecies species) {
        if (id != null && species != null) {
            species.applyTaxonomicArchetypes();
            String key = id.toLowerCase();
            boolean isNew = !speciesMap.containsKey(key);
            speciesMap.put(key, species);
            if (isNew) {
                log.info("Registered species preset [{}] -> {}", id, species.getCommonName());
            } else {
                log.debug("Updated existing species preset [{}] -> {}", id, species.getCommonName());
            }
        }
    }

    public void register(DefaultSpecies species) {
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
        DefaultSpecies found = speciesMap.get(key);
        if (found == null) {
            // Search by scientific name or common name
            for (DefaultSpecies s : speciesMap.values()) {
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

    public DefaultSpecies getSpecies(String id) {
        if (id == null || id.isBlank()) return getFallbackSpecies();
        // 1. Direct match with lowercased key
        String rawKey = id.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        DefaultSpecies found = speciesMap.get(rawKey);
        if (found != null) return found;

        // 2. Try camelCase to kebab-case (e.g. "AttaCephalotes" -> "atta-cephalotes")
        String kebab = id.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
        found = speciesMap.get(kebab);
        if (found != null) return found;

        // 3. Strip parenthetical descriptors (e.g. "Polyergus rufescens (Amazon Raiding Party)" -> "Polyergus rufescens")
        String cleaned = id.replaceAll("\\s*\\([^)]*\\)", "").trim();
        String cleanedKey = cleaned.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        found = speciesMap.get(cleanedKey);
        if (found != null) return found;

        // 4. Normalized alphanumeric matching (stripping all punctuation/spaces)
        String normInput = cleaned.toLowerCase().replaceAll("[^a-z0-9]", "");
        for (Map.Entry<String, DefaultSpecies> entry : speciesMap.entrySet()) {
            String normK = entry.getKey().replaceAll("[^a-z0-9]", "");
            if (normK.equals(normInput)) return entry.getValue();
        }

        // 5. Search by scientific name, common name, or preset name matching
        for (DefaultSpecies s : speciesMap.values()) {
            if (s.getScientificName() != null) {
                String scNorm = s.getScientificName().toLowerCase().replaceAll("[^a-z0-9]", "");
                if (scNorm.equals(normInput) || normInput.contains(scNorm) || scNorm.contains(normInput)) return s;
            }
            if (s.getCommonName() != null) {
                String cnNorm = s.getCommonName().toLowerCase().replaceAll("[^a-z0-9]", "");
                if (cnNorm.equals(normInput) || normInput.contains(cnNorm) || cnNorm.contains(normInput)) return s;
            }
            if (s.getPresetName() != null) {
                String pnNorm = s.getPresetName().toLowerCase().replaceAll("[^a-z0-9]", "");
                if (pnNorm.equals(normInput) || normInput.contains(pnNorm) || pnNorm.contains(normInput)) return s;
            }
        }
        return getFallbackSpecies();
    }

    public Collection<DefaultSpecies> getAllSpecies() {
        return Collections.unmodifiableCollection(speciesMap.values());
    }

    public Set<String> getAvailableSpeciesIds() {
        return Collections.unmodifiableSet(speciesMap.keySet());
    }

    private InputStream openSpeciesResource(String filename) {
        InputStream in = getClass().getResourceAsStream("/presets/species/" + filename);
        if (in == null) {
            in = getClass().getClassLoader().getResourceAsStream("presets/species/" + filename);
        }
        if (in == null && Thread.currentThread().getContextClassLoader() != null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream("presets/species/" + filename);
        }
        return in;
    }

    private void loadDefaultPresets() {
        if (!defaultsLoaded.compareAndSet(false, true)) {
            return;
        }
        // Load built-in species from JSON classpath resources
        for (String filename : BUILTIN_SPECIES_FILES) {
            try {
                try (InputStream in = openSpeciesResource(filename)) {
                    if (in != null) {
                        DefaultSpecies species = mapper.readValue(in, DefaultSpecies.class);
                        species.setBuiltIn(true);
                        species.setAuthor("Academic Reference");
                        String id = filename.replace(".json", "");
                        registerSpecies(id, species);
                        if (species.getScientificName() != null) {
                            registerSpecies(species.getScientificName().toLowerCase().replaceAll("[^a-z0-9]+", "-"), species);
                        }
                        continue;
                    }
                }

                // Try relative filesystem fallback for dev/test environments
                File[] candidates = {
                        new File("src/main/resources/presets/species", filename),
                        new File("swarmforge-core/src/main/resources/presets/species", filename),
                        new File("../swarmforge-core/src/main/resources/presets/species", filename)
                };
                for (File candidate : candidates) {
                    if (candidate.exists()) {
                        DefaultSpecies species = mapper.readValue(candidate, DefaultSpecies.class);
                        species.setBuiltIn(true);
                        species.setAuthor("Academic Reference");
                        String id = filename.replace(".json", "");
                        registerSpecies(id, species);
                        if (species.getScientificName() != null) {
                            registerSpecies(species.getScientificName().toLowerCase().replaceAll("[^a-z0-9]+", "-"), species);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to load species JSON preset [{}]", filename, e);
            }
        }

        // Load custom user JSON files from ~/.swarmforge/presets/species/
        try {
            Path userDir = Path.of(System.getProperty("user.home"), ".swarmforge", "presets", "species");
            if (Files.exists(userDir)) {
                try (var stream = Files.list(userDir)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                        try {
                            DefaultSpecies s = mapper.readValue(path.toFile(), DefaultSpecies.class);
                            String filename = path.getFileName().toString();
                            String id = filename.replace("swarmforge-species-", "").replace(".json", "");
                            registerSpecies(id, s);
                        } catch (Exception ex) {
                            log.warn("Failed to parse user species JSON preset from {}", path, ex);
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.warn("Could not scan user species presets directory", e);
        }
    }

    private DefaultSpecies getFallbackSpecies() {
        DefaultSpecies fallback = new DefaultSpecies();
        fallback.setCommonName("Fourmi Générique");
        fallback.setScientificName("Formica genericus");
        return fallback;
    }
}
