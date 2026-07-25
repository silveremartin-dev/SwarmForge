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
import org.swarmforge.core.domain.CasteTemplate;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Centralized data-driven registry for all biological species definitions.
 * Eliminates the need for hardcoded Java class files for each species, enabling
 * dynamic JSON loading, preset management, and user extensions.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(SpeciesRegistry.class);
    private static final SpeciesRegistry INSTANCE = new SpeciesRegistry();

    private final Map<String, Species> speciesMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private SpeciesRegistry() {
        registerBuiltinDefaults();
    }

    public static SpeciesRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a species definition in the global registry.
     *
     * @param species The species instance to register.
     */
    public void register(Species species) {
        if (species == null) return;
        String key = normalizeKey(species.getScientificName());
        speciesMap.put(key, species);
        if (species.getCommonName() != null) {
            speciesMap.putIfAbsent(normalizeKey(species.getCommonName()), species);
        }
        logger.info("Registered species: {} ({}) [{}]", species.getScientificName(), species.getCommonName(), species.getCategory());
    }

    /**
     * Retrieves a species by key, scientific name, or common name.
     */
    public Optional<Species> get(String identifier) {
        if (identifier == null || identifier.isBlank()) return Optional.empty();
        String key = normalizeKey(identifier);
        Species sp = speciesMap.get(key);
        if (sp != null) return Optional.of(sp);

        // Fallback search by substring or exact match
        return speciesMap.values().stream()
                .filter(s -> s.getScientificName().equalsIgnoreCase(identifier) || s.getCommonName().equalsIgnoreCase(identifier))
                .findFirst();
    }

    /**
     * Returns all registered species.
     */
    public Collection<Species> getAll() {
        return Collections.unmodifiableCollection(speciesMap.values().stream().distinct().collect(Collectors.toList()));
    }

    /**
     * Returns all primary eusocial colony-building species.
     */
    public List<Species> getEusocialSpecies() {
        return speciesMap.values().stream()
                .distinct()
                .filter(s -> s.getCategory() == SpeciesCategory.EUSOCIAL_PRIMARY)
                .collect(Collectors.toList());
    }

    /**
     * Returns all accessory fauna (prey, commensals, honeydew producers, soil fauna, parasites).
     */
    public List<Species> getAccessorySpecies() {
        return speciesMap.values().stream()
                .distinct()
                .filter(s -> s.getCategory() != SpeciesCategory.EUSOCIAL_PRIMARY)
                .collect(Collectors.toList());
    }

    /**
     * Returns all species belonging to a specific category.
     */
    public List<Species> getByCategory(SpeciesCategory category) {
        return speciesMap.values().stream()
                .distinct()
                .filter(s -> s.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Loads and registers a species definition from a JSON file.
     */
    public Optional<Species> loadFromFile(File jsonFile) {
        try {
            CustomSpecies species = objectMapper.readValue(jsonFile, CustomSpecies.class);
            register(species);
            return Optional.of(species);
        } catch (Exception e) {
            logger.error("Failed to load species from JSON file: " + jsonFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    /**
     * Loads and registers a species definition from an InputStream (e.g. JAR resource).
     */
    public Optional<Species> loadFromStream(InputStream inputStream) {
        try {
            CustomSpecies species = objectMapper.readValue(inputStream, CustomSpecies.class);
            register(species);
            return Optional.of(species);
        } catch (Exception e) {
            logger.error("Failed to load species from InputStream", e);
            return Optional.empty();
        }
    }

    private String normalizeKey(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "_").replaceAll("_+", "_");
    }

    /**
     * Built-in dynamic species defaults (Primary Eusocial + Accessory Fauna).
     */
    private void registerBuiltinDefaults() {
        // --- 1. PRIMARY EUSOCIAL SPECIES ---

        CustomSpecies lasius = new CustomSpecies();
        lasius.setPresetName("Fourmi Noire des Jardins (Lasius niger)");
        lasius.setScientificName("Lasius niger");
        lasius.setCommonName("Black Garden Ant");
        lasius.setInsectType("ANT");
        lasius.setCategory(SpeciesCategory.EUSOCIAL_PRIMARY);
        lasius.setQueenCountMode("MONOGYNE");
        lasius.setQueenCount(1);
        lasius.setQueenLifespan(365 * 15);
        lasius.setWorkerLifespan(365 * 3);
        lasius.setWorkerSpeed(0.5f);
        lasius.setViewDistance(3.0f);
        lasius.setTypicalColonySize(15000);
        lasius.setPrimaryDiet("HONEYDEW");
        lasius.setSecondaryDiet("INSECTS_MEAT");
        lasius.setNestType("UNDERGROUND_BURROW");
        lasius.setVenomType("FORMIC_ACID");
        lasius.setAggression(0.3f);
        register(lasius);

        CustomSpecies reticulitermes = new CustomSpecies();
        reticulitermes.setPresetName("Termite Souterrain (Reticulitermes flavipes)");
        reticulitermes.setScientificName("Reticulitermes flavipes");
        reticulitermes.setCommonName("Eastern Subterranean Termite");
        reticulitermes.setInsectType("TERMITE");
        reticulitermes.setCategory(SpeciesCategory.EUSOCIAL_PRIMARY);
        reticulitermes.setQueenCountMode("MONOGYNE");
        reticulitermes.setHasKing(true);
        reticulitermes.setKingLifespan(365 * 20);
        reticulitermes.setQueenLifespan(365 * 25);
        reticulitermes.setWorkerLifespan(365 * 2);
        reticulitermes.setWorkerSpeed(0.35f);
        reticulitermes.setViewDistance(1.5f);
        reticulitermes.setTypicalColonySize(250000);
        reticulitermes.setPrimaryDiet("WOOD_CELLULOSE");
        reticulitermes.setSecondaryDiet("FUNGUS");
        reticulitermes.setNestType("WOOD_TUNNELS");
        reticulitermes.setVenomType("POWERFUL_MANDIBLES");
        reticulitermes.setAggression(0.4f);
        register(reticulitermes);

        CustomSpecies apis = new CustomSpecies();
        apis.setPresetName("Abeille à Miel (Apis mellifera)");
        apis.setScientificName("Apis mellifera");
        apis.setCommonName("Western Honey Bee");
        apis.setInsectType("BEE");
        apis.setCategory(SpeciesCategory.EUSOCIAL_PRIMARY);
        apis.setQueenCountMode("MONOGYNE");
        apis.setQueenLifespan(365 * 4);
        apis.setWorkerLifespan(60 * 24 * 45);
        apis.setWorkerSpeed(1.2f);
        apis.setViewDistance(8.0f);
        apis.setWorkersCanFly(true);
        apis.setTypicalColonySize(50000);
        apis.setPrimaryDiet("SUGARS_NECTAR");
        apis.setNestType("WAX_COMB");
        apis.setVenomType("VENOMOUS_STING");
        apis.setAggression(0.2f);
        register(apis);

        CustomSpecies vespula = new CustomSpecies();
        vespula.setPresetName("Guêpe Commune (Vespula germanica)");
        vespula.setScientificName("Vespula germanica");
        vespula.setCommonName("European Yellowjacket Wasp");
        vespula.setInsectType("WASP");
        vespula.setCategory(SpeciesCategory.EUSOCIAL_PRIMARY);
        vespula.setQueenCountMode("MONOGYNE");
        vespula.setQueenLifespan(365);
        vespula.setWorkerLifespan(30 * 24);
        vespula.setWorkerSpeed(1.4f);
        vespula.setViewDistance(6.0f);
        vespula.setWorkersCanFly(true);
        vespula.setTypicalColonySize(4000);
        vespula.setPrimaryDiet("INSECTS_MEAT");
        vespula.setSecondaryDiet("SUGARS_NECTAR");
        vespula.setNestType("PAPER_NEST");
        vespula.setVenomType("VENOMOUS_STING");
        vespula.setAggression(0.85f);
        register(vespula);

        CustomSpecies atta = new CustomSpecies();
        atta.setPresetName("Fourmi Coupeuse de Feuilles (Atta cephalotes)");
        atta.setScientificName("Atta cephalotes");
        atta.setCommonName("Leafcutter Ant");
        atta.setInsectType("ANT");
        atta.setCategory(SpeciesCategory.EUSOCIAL_PRIMARY);
        atta.setQueenLifespan(365 * 20);
        atta.setWorkerLifespan(365 * 2);
        atta.setWorkerSpeed(0.6f);
        atta.setViewDistance(4.0f);
        atta.setTypicalColonySize(500000);
        atta.setPrimaryDiet("FUNGUS");
        atta.setNestType("UNDERGROUND_BURROW");
        atta.setAggression(0.5f);
        register(atta);

        CustomSpecies solenopsis = new CustomSpecies();
        solenopsis.setPresetName("Fourmi de Feu (Solenopsis invicta)");
        solenopsis.setScientificName("Solenopsis invicta");
        solenopsis.setCommonName("Fire Ant");
        solenopsis.setInsectType("ANT");
        solenopsis.setCategory(SpeciesCategory.EUSOCIAL_PRIMARY);
        solenopsis.setQueenCountMode("POLYGYNE");
        solenopsis.setQueenCount(12);
        solenopsis.setQueenLifespan(365 * 7);
        solenopsis.setWorkerLifespan(180);
        solenopsis.setWorkerSpeed(0.7f);
        solenopsis.setTypicalColonySize(250000);
        solenopsis.setFormsMegaColonies(true);
        solenopsis.setPrimaryDiet("INSECTS_MEAT");
        solenopsis.setNestType("MOUND");
        solenopsis.setVenomType("VENOMOUS_STING");
        solenopsis.setAggression(0.9f);
        register(solenopsis);

        // --- 2. ACCESSORY ENVIRONMENTAL FAUNA ---

        CustomSpecies aphid = new CustomSpecies();
        aphid.setPresetName("Puceron du Sorbier (Aphis pomi)");
        aphid.setScientificName("Aphis pomi");
        aphid.setCommonName("Green Apple Aphid");
        aphid.setInsectType("OTHER");
        aphid.setCategory(SpeciesCategory.HONEYDEW_PRODUCER);
        aphid.setDescription("Espèce symbiotique sécrétant du miellat sucré pour les fourmis en échange de protection.");
        aphid.setWorkerLifespan(30);
        aphid.setWorkerSpeed(0.1f);
        aphid.setViewDistance(1.0f);
        aphid.setTypicalColonySize(500);
        aphid.setPrimaryDiet("SUGARS_NECTAR");
        aphid.setAggression(0.0f);
        register(aphid);

        CustomSpecies earthworm = new CustomSpecies();
        earthworm.setPresetName("Ver de Terre (Lumbricus terrestris)");
        earthworm.setScientificName("Lumbricus terrestris");
        earthworm.setCommonName("Common Earthworm");
        earthworm.setInsectType("OTHER");
        earthworm.setCategory(SpeciesCategory.SOIL_FAUNA);
        earthworm.setDescription("Faune du sol aérant la terre et consommant la matière organique en décomposition.");
        earthworm.setWorkerLifespan(365 * 6);
        earthworm.setWorkerSpeed(0.15f);
        earthworm.setViewDistance(0.5f);
        earthworm.setTypicalColonySize(50);
        earthworm.setPrimaryDiet("WOOD_CELLULOSE");
        earthworm.setAggression(0.0f);
        register(earthworm);

        CustomSpecies caterpillar = new CustomSpecies();
        caterpillar.setPresetName("Chenille Chassée (Pieris brassicae)");
        caterpillar.setScientificName("Pieris brassicae");
        caterpillar.setCommonName("Large White Caterpillar");
        caterpillar.setInsectType("OTHER");
        caterpillar.setCategory(SpeciesCategory.PREY_ORGANISM);
        caterpillar.setDescription("Proie riche en protéines chassée par les guêpes et les fourmis prédatrices.");
        caterpillar.setWorkerLifespan(20);
        caterpillar.setWorkerSpeed(0.2f);
        caterpillar.setViewDistance(2.0f);
        caterpillar.setTypicalColonySize(20);
        caterpillar.setPrimaryDiet("SEEDS");
        caterpillar.setAggression(0.1f);
        register(caterpillar);

        CustomSpecies woodlouse = new CustomSpecies();
        woodlouse.setPresetName("Cloporte Commun (Armadillidium vulgare)");
        woodlouse.setScientificName("Armadillidium vulgare");
        woodlouse.setCommonName("Pill Bug / Woodlouse");
        woodlouse.setInsectType("OTHER");
        woodlouse.setCategory(SpeciesCategory.SOIL_FAUNA);
        woodlouse.setDescription("Isopode détritivore consommant le bois mort et la litière forestière.");
        woodlouse.setWorkerLifespan(365 * 3);
        woodlouse.setWorkerSpeed(0.3f);
        woodlouse.setViewDistance(1.5f);
        woodlouse.setTypicalColonySize(100);
        woodlouse.setPrimaryDiet("WOOD_CELLULOSE");
        woodlouse.setAggression(0.0f);
        register(woodlouse);

        CustomSpecies varroa = new CustomSpecies();
        varroa.setPresetName("Varroa Parasite (Varroa destructor)");
        varroa.setScientificName("Varroa destructor");
        varroa.setCommonName("Varroa Mite");
        varroa.setInsectType("OTHER");
        varroa.setCategory(SpeciesCategory.PARASITE_PREDATOR);
        varroa.setDescription("Acarien parasite externe ciblant le couvain des abeilles mellifères.");
        varroa.setWorkerLifespan(90);
        varroa.setWorkerSpeed(0.4f);
        varroa.setViewDistance(1.0f);
        varroa.setTypicalColonySize(200);
        varroa.setPrimaryDiet("INSECTS_MEAT");
        varroa.setAggression(0.8f);
        register(varroa);
    }
}
