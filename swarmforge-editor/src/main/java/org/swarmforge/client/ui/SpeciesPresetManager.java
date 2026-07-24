/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.swarmforge.core.domain.CasteTemplate;
import org.swarmforge.core.species.CustomSpecies;

import java.io.File;
import java.util.*;

/**
 * Manages Species presets: built-in biological defaults + user custom presets.
 * Persists presets to {@code species_presets.json} in the working directory.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesPresetManager {

    public static final File PRESETS_FILE = new File("species_presets.json");

    private final Map<String, CustomSpecies> presets = new LinkedHashMap<>();

    public SpeciesPresetManager() {
        loadAll();
    }

    private void loadAll() {
        presets.clear();
        presets.putAll(createBuiltins());

        if (PRESETS_FILE.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, CustomSpecies> saved = mapper.readValue(PRESETS_FILE, new TypeReference<LinkedHashMap<String, CustomSpecies>>() {});
                presets.putAll(saved);
            } catch (Exception e) {
                System.err.println("[SpeciesPresetManager] Could not load " + PRESETS_FILE + ": " + e.getMessage());
            }
        }
    }

    private Map<String, CustomSpecies> createBuiltins() {
        Map<String, CustomSpecies> map = new LinkedHashMap<>();

        // 1. Fourmi Noire des Jardins (Lasius niger)
        CustomSpecies lasius = new CustomSpecies();
        lasius.setPresetName("Fourmi Noire des Jardins (Lasius niger)");
        lasius.setCommonName("Fourmi Noire des Jardins");
        lasius.setScientificName("Lasius niger");
        lasius.setInsectType("ANT");
        lasius.setDescription("Espèce monogyne très répandue en Europe. Élevage de pucerons et récolte de miellat.");
        lasius.setQueenCountMode("MONOGYNE");
        lasius.setQueenCount(1);
        lasius.setQueenLifespan(30000);
        lasius.setQueenEggLayingRate(25.0f);
        lasius.setWorkerLifespan(6000);
        lasius.setWorkerSpeed(0.5f);
        lasius.setTypicalColonySize(15000);
        lasius.setPrimaryDiet("HONEYDEW");
        lasius.setSecondaryDiet("INSECTS_MEAT");
        lasius.setNestType("UNDERGROUND_BURROW");
        lasius.setVenomType("FORMIC_ACID");
        lasius.setAggression(0.3f);

        CasteTemplate lasiusQueen = new CasteTemplate("Reine", 500f, 10f);
        lasiusQueen.setDescription("Reine fondatrice (9mm)");
        lasiusQueen.setLifespan(30000);
        lasiusQueen.setBodyLengthMm(9.0f);
        lasiusQueen.setHeadWidthMm(2.4f);

        CasteTemplate lasiusWorker = new CasteTemplate("Ouvrière", 80f, 4f);
        lasiusWorker.setDescription("Ouvrière généraliste (4mm)");
        lasiusWorker.setLifespan(6000);
        lasiusWorker.setCanDig(true);
        lasiusWorker.setCanCarry(true);
        lasiusWorker.setBodyLengthMm(4.0f);
        lasiusWorker.setHeadWidthMm(1.0f);

        lasius.setCasteTemplates(List.of(lasiusQueen, lasiusWorker));
        map.put(lasius.getPresetName(), lasius);

        // 2. Fourmi de Feu (Solenopsis invicta)
        CustomSpecies solenopsis = new CustomSpecies();
        solenopsis.setPresetName("Fourmi de Feu (Solenopsis invicta)");
        solenopsis.setCommonName("Fourmi de Feu Red Imported");
        solenopsis.setScientificName("Solenopsis invicta");
        solenopsis.setInsectType("ANT");
        solenopsis.setDescription("Espèce très agressive à piqure venimeuse, colonies souvent polygynes formant des dômes élevés.");
        solenopsis.setQueenCountMode("POLYGYNE");
        solenopsis.setQueenCount(12);
        solenopsis.setQueenLifespan(20000);
        solenopsis.setQueenEggLayingRate(80.0f);
        solenopsis.setWorkerLifespan(4000);
        solenopsis.setWorkerSpeed(0.75f);
        solenopsis.setTypicalColonySize(250000);
        solenopsis.setFormsMegaColonies(true);
        solenopsis.setPrimaryDiet("INSECTS_MEAT");
        solenopsis.setSecondaryDiet("SUGARS_NECTAR");
        solenopsis.setNestType("MOUND");
        solenopsis.setVenomType("VENOMOUS_STING");
        solenopsis.setAggression(0.9f);

        CasteTemplate fireQueen = new CasteTemplate("Reine", 450f, 15f);
        fireQueen.setLifespan(20000);
        fireQueen.setBodyLengthMm(8.0f);
        fireQueen.setHeadWidthMm(2.0f);

        CasteTemplate fireMinor = new CasteTemplate("Ouvrière Mineure", 60f, 8f);
        fireMinor.setLifespan(3500);
        fireMinor.setCanDig(true);
        fireMinor.setBodyLengthMm(3.0f);
        fireMinor.setHeadWidthMm(0.8f);

        CasteTemplate fireMajor = new CasteTemplate("Ouvrière Majeure / Soldat", 150f, 25f);
        fireMajor.setLifespan(5000);
        fireMajor.setBaseDefense(5f);
        fireMajor.setBodyLengthMm(6.0f);
        fireMajor.setHeadWidthMm(1.8f);

        solenopsis.setCasteTemplates(List.of(fireQueen, fireMinor, fireMajor));
        map.put(solenopsis.getPresetName(), solenopsis);

        // 3. Fourmi Coupeuse de Feuilles (Atta sexdens)
        CustomSpecies atta = new CustomSpecies();
        atta.setPresetName("Fourmi Coupeuse de Feuilles (Atta sexdens)");
        atta.setCommonName("Fourmi Coupeuse de Feuilles / Champignonniste");
        atta.setScientificName("Atta sexdens");
        atta.setInsectType("ANT");
        atta.setDescription("Récolte du feuillage pour cultiver un champignon symbiotique au sein d'immenses nids souterrains.");
        atta.setQueenCountMode("MONOGYNE");
        atta.setQueenCount(1);
        atta.setQueenLifespan(35000);
        atta.setQueenEggLayingRate(100.0f);
        atta.setWorkerLifespan(7000);
        atta.setWorkerSpeed(0.6f);
        atta.setTypicalColonySize(500000);
        atta.setPrimaryDiet("FUNGUS");
        atta.setSecondaryDiet("SUGARS_NECTAR");
        atta.setNestType("UNDERGROUND_BURROW");
        atta.setVenomType("POWERFUL_MANDIBLES");
        atta.setAggression(0.5f);

        CasteTemplate attaQueen = new CasteTemplate("Reine Géante", 1000f, 20f);
        attaQueen.setLifespan(35000);
        attaQueen.setBodyLengthMm(30.0f);
        attaQueen.setHeadWidthMm(7.0f);

        CasteTemplate attaMinima = new CasteTemplate("Ouvrière Minime (Nourrice)", 40f, 2f);
        attaMinima.setBodyLengthMm(2.0f);
        attaMinima.setHeadWidthMm(0.6f);

        CasteTemplate attaMedia = new CasteTemplate("Ouvrière Média (Coupeuse)", 100f, 10f);
        attaMedia.setCanCarry(true);
        attaMedia.setBodyLengthMm(7.0f);
        attaMedia.setHeadWidthMm(2.2f);

        CasteTemplate attaMajor = new CasteTemplate("Soldat Majeur (Garde)", 300f, 45f);
        attaMajor.setBaseDefense(8f);
        attaMajor.setBodyLengthMm(16.0f);
        attaMajor.setHeadWidthMm(6.0f);

        atta.setCasteTemplates(List.of(attaQueen, attaMinima, attaMedia, attaMajor));
        map.put(atta.getPresetName(), atta);

        // 4. Abeille à Miel (Apis mellifera)
        CustomSpecies apis = new CustomSpecies();
        apis.setPresetName("Abeille à Miel (Apis mellifera)");
        apis.setCommonName("Abeille Européenne à Miel");
        apis.setScientificName("Apis mellifera");
        apis.setInsectType("BEE");
        apis.setDescription("Insecte eusocial volant construisant des rayons de cire. Butine nectar et pollen.");
        apis.setQueenCountMode("MONOGYNE");
        apis.setQueenCount(1);
        apis.setQueenLifespan(18000);
        apis.setQueenEggLayingRate(150.0f);
        apis.setNuptialFlightType("SWARM_DIVISION");
        apis.setWorkerLifespan(3000);
        apis.setWorkerSpeed(1.2f);
        apis.setWorkersCanFly(true);
        apis.setTypicalColonySize(50000);
        apis.setPrimaryDiet("SUGARS_NECTAR");
        apis.setSecondaryDiet("SEEDS");
        apis.setNestType("WAX_COMB");
        apis.setVenomType("VENOMOUS_STING");
        apis.setAggression(0.2f);

        CasteTemplate apisQueen = new CasteTemplate("Reine Abeille", 400f, 5f);
        apisQueen.setLifespan(18000);
        apisQueen.setCanFly(true);
        apisQueen.setBodyLengthMm(20.0f);
        apisQueen.setHeadWidthMm(4.0f);

        CasteTemplate apisWorker = new CasteTemplate("Ouvrière Butineuse", 90f, 12f);
        apisWorker.setLifespan(3000);
        apisWorker.setCanFly(true);
        apisWorker.setCanCarry(true);
        apisWorker.setBodyLengthMm(14.0f);
        apisWorker.setHeadWidthMm(3.5f);

        CasteTemplate apisDrone = new CasteTemplate("Faux-Bourdon (Mâle)", 120f, 0f);
        apisDrone.setLifespan(2500);
        apisDrone.setCanFly(true);
        apisDrone.setBodyLengthMm(16.0f);
        apisDrone.setHeadWidthMm(4.2f);

        apis.setCasteTemplates(List.of(apisQueen, apisWorker, apisDrone));
        map.put(apis.getPresetName(), apis);

        // 5. Guêpe Commune (Vespula vulgaris)
        CustomSpecies vespula = new CustomSpecies();
        vespula.setPresetName("Guêpe Commune (Vespula vulgaris)");
        vespula.setCommonName("Guêpe Commune");
        vespula.setScientificName("Vespula vulgaris");
        vespula.setInsectType("WASP");
        vespula.setDescription("Chasseresse carnivore volante construisant des nids en papier d'origine ligneuse.");
        vespula.setQueenCountMode("MONOGYNE");
        vespula.setQueenCount(1);
        vespula.setQueenLifespan(12000);
        vespula.setQueenEggLayingRate(40.0f);
        vespula.setWorkerLifespan(2500);
        vespula.setWorkerSpeed(1.4f);
        vespula.setWorkersCanFly(true);
        vespula.setTypicalColonySize(8000);
        vespula.setPrimaryDiet("INSECTS_MEAT");
        vespula.setSecondaryDiet("SUGARS_NECTAR");
        vespula.setNestType("PAPER_NEST");
        vespula.setVenomType("VENOMOUS_STING");
        vespula.setAggression(0.85f);

        CasteTemplate vespulaQueen = new CasteTemplate("Fondatrice (Reine)", 450f, 20f);
        vespulaQueen.setCanFly(true);
        vespulaQueen.setBodyLengthMm(19.0f);
        vespulaQueen.setHeadWidthMm(4.5f);

        CasteTemplate vespulaWorker = new CasteTemplate("Ouvrière Chasseresse", 100f, 22f);
        vespulaWorker.setLifespan(2500);
        vespulaWorker.setCanFly(true);
        vespulaWorker.setBodyLengthMm(13.0f);
        vespulaWorker.setHeadWidthMm(3.2f);

        vespula.setCasteTemplates(List.of(vespulaQueen, vespulaWorker));
        map.put(vespula.getPresetName(), vespula);

        // 6. Termite Souterrain (Reticulitermes flavipes)
        CustomSpecies termite = new CustomSpecies();
        termite.setPresetName("Termite Souterrain (Reticulitermes flavipes)");
        termite.setCommonName("Termite Souterrain d'Amérique");
        termite.setScientificName("Reticulitermes flavipes");
        termite.setInsectType("TERMITE");
        termite.setDescription("Eusocial de l'ordre des Isoptères. Reine et Roi présents dans la cellule royale, nutrition cellulosique.");
        termite.setQueenCountMode("MONOGYNE");
        termite.setQueenCount(1);
        termite.setHasKing(true);
        termite.setKingLifespan(20000);
        termite.setQueenLifespan(30000);
        termite.setQueenEggLayingRate(60.0f);
        termite.setWorkerLifespan(5000);
        termite.setWorkerSpeed(0.4f);
        termite.setTypicalColonySize(100000);
        termite.setPrimaryDiet("WOOD_CELLULOSE");
        termite.setNestType("WOOD_TUNNELS");
        termite.setVenomType("POWERFUL_MANDIBLES");
        termite.setAggression(0.4f);

        CasteTemplate termiteQueen = new CasteTemplate("Reine Physogastre", 600f, 2f);
        termiteQueen.setBodyLengthMm(22.0f);
        termiteQueen.setHeadWidthMm(3.0f);

        CasteTemplate termiteKing = new CasteTemplate("Roi Reproducteur", 300f, 5f);
        termiteKing.setBodyLengthMm(10.0f);
        termiteKing.setHeadWidthMm(2.0f);

        CasteTemplate termiteWorker = new CasteTemplate("Ouvrier Termite", 50f, 3f);
        termiteWorker.setCanDig(true);
        termiteWorker.setBodyLengthMm(5.0f);
        termiteWorker.setHeadWidthMm(1.2f);

        CasteTemplate termiteSoldier = new CasteTemplate("Soldat à Mandiboles", 200f, 35f);
        termiteSoldier.setBaseDefense(6f);
        termiteSoldier.setBodyLengthMm(7.0f);
        termiteSoldier.setHeadWidthMm(2.5f);

        termite.setCasteTemplates(List.of(termiteQueen, termiteKing, termiteWorker, termiteSoldier));
        map.put(termite.getPresetName(), termite);

        // 7. Fourmi Moissonneuse (Pogonomyrmex barbatus)
        CustomSpecies harvester = new CustomSpecies();
        harvester.setPresetName("Fourmi Moissonneuse (Pogonomyrmex barbatus)");
        harvester.setCommonName("Fourmi Moissonneuse Rouge");
        harvester.setScientificName("Pogonomyrmex barbatus");
        harvester.setInsectType("ANT");
        harvester.setDescription("Espèce granivore récoltant des graines et stockant des greniers souterrains.");
        harvester.setQueenCountMode("MONOGYNE");
        harvester.setQueenCount(1);
        harvester.setQueenLifespan(22000);
        harvester.setWorkerLifespan(5500);
        harvester.setWorkerSpeed(0.55f);
        harvester.setTypicalColonySize(12000);
        harvester.setPrimaryDiet("SEEDS");
        harvester.setSecondaryDiet("SUGARS_NECTAR");
        harvester.setNestType("UNDERGROUND_BURROW");
        harvester.setVenomType("VENOMOUS_STING");
        harvester.setAggression(0.6f);

        CasteTemplate harvestQueen = new CasteTemplate("Reine", 450f, 10f);
        harvestQueen.setBodyLengthMm(12.0f);
        harvestQueen.setHeadWidthMm(3.0f);

        CasteTemplate harvestWorker = new CasteTemplate("Ouvrière Moissonneuse", 90f, 14f);
        harvestWorker.setCanCarry(true);
        harvestWorker.setCanDig(true);
        harvestWorker.setBodyLengthMm(7.0f);
        harvestWorker.setHeadWidthMm(2.0f);

        harvester.setCasteTemplates(List.of(harvestQueen, harvestWorker));
        map.put(harvester.getPresetName(), harvester);

        return map;
    }

    public Map<String, CustomSpecies> getAll() {
        return Collections.unmodifiableMap(presets);
    }

    public Set<String> getPresetNames() {
        return presets.keySet();
    }

    public CustomSpecies getPreset(String name) {
        return presets.get(name);
    }

    public boolean contains(String name) {
        return presets.containsKey(name);
    }

    public void addPreset(String name, CustomSpecies species) {
        species.setPresetName(name);
        presets.put(name, species);
        persist();
    }

    public void persist() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(PRESETS_FILE, presets);
        } catch (Exception e) {
            System.err.println("[SpeciesPresetManager] Could not save " + PRESETS_FILE + ": " + e.getMessage());
        }
    }

    public void saveToFile(File file, CustomSpecies species) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, species);
    }

    public CustomSpecies loadFromFile(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, CustomSpecies.class);
    }
}
