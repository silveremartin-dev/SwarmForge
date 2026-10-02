/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Utility test to generate and export all academic accessory presets
 * (Preys, Predators, Mutualists, Flora, Fungi, Detritivores, Pathogens) to JSON.
 */
public class ExportAccessoryPresetsTest {

    @Test
    void exportAllAccessoryPresets() throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        List<AccessoryPreset> list = new ArrayList<>();

        // 1. FLORA
        AccessoryPreset grasses = new AccessoryPreset("flora-grasses-messor", "Seed-Bearing Grasses (Messor)", AccessoryPreset.Category.FLORA);
        grasses.setScientificName("Poaceae spp.");
        grasses.setDescription("High-yielding seed flora providing carbohydrate and lipid grain caches for harvester ants.");
        grasses.setBiome("TEMPERATE_DECIDUOUS");
        grasses.setLatitude(45.0);
        grasses.setMinTempCelsius(5.0f); grasses.setOptimalTempCelsius(22.0f); grasses.setMaxTempCelsius(35.0f);
        grasses.setGrowthRate(1.2f); grasses.setInitialBiomassDensity(150.0f); grasses.setInitialPopulationDensity(25.0f);
        grasses.setDiapause(true);
        grasses.setSeasonMultiplierSpring(0.8f); grasses.setSeasonMultiplierSummer(1.0f); grasses.setSeasonMultiplierAutumn(0.6f); grasses.setSeasonMultiplierWinter(0.1f);
        grasses.setLeafBiomassYield(20.0f); grasses.setProteinYield(5.0f);
        list.add(grasses);

        AccessoryPreset moss = new AccessoryPreset("flora-humid-moss", "Humid Moss (Polytrichum / Substrate)", AccessoryPreset.Category.FLORA);
        moss.setScientificName("Polytrichum commune");
        moss.setDescription("Water-retaining bryophyte layer maintaining humid subterranean microclimates.");
        moss.setBiome("TAIGA_BOREAL");
        moss.setLatitude(60.0);
        moss.setMinTempCelsius(2.0f); moss.setOptimalTempCelsius(18.0f); moss.setMaxTempCelsius(28.0f);
        moss.setGrowthRate(0.8f); moss.setInitialBiomassDensity(300.0f); moss.setInitialPopulationDensity(10.0f);
        moss.setDiapause(true);
        moss.setSeasonMultiplierSpring(0.9f); moss.setSeasonMultiplierSummer(0.7f); moss.setSeasonMultiplierAutumn(0.4f); moss.setSeasonMultiplierWinter(0.1f);
        list.add(moss);

        AccessoryPreset nectaries = new AccessoryPreset("flora-nectar-flowers", "Nectar Flowers & Nectaries (Acacia EFN)", AccessoryPreset.Category.FLORA);
        nectaries.setScientificName("Acacia collinsii");
        nectaries.setDescription("Flowering shrubs bearing extrafloral nectaries and Beltian protein bodies.");
        nectaries.setBiome("MEDITERRANEAN");
        nectaries.setLatitude(35.0);
        nectaries.setMinTempCelsius(10.0f); nectaries.setOptimalTempCelsius(25.0f); nectaries.setMaxTempCelsius(38.0f);
        nectaries.setGrowthRate(1.8f); nectaries.setInitialBiomassDensity(180.0f); nectaries.setInitialPopulationDensity(40.0f);
        nectaries.setSugarHoneydewYield(50.0f); nectaries.setProteinYield(15.0f);
        list.add(nectaries);

        // 2. APHID_MUTUALIST
        AccessoryPreset pineAphids = new AccessoryPreset("mutualist-aphid-cinara", "Pine Aphids (Cinara / Honeydew)", AccessoryPreset.Category.APHID_MUTUALIST);
        pineAphids.setScientificName("Cinara pinea");
        pineAphids.setDescription("Conifer phloem sap-sucking aphids tended by Formica and Lasius ants for rich honeydew secretions.");
        pineAphids.setBiome("TEMPERATE_DECIDUOUS");
        pineAphids.setLatitude(48.5);
        pineAphids.setMinTempCelsius(8.0f); pineAphids.setOptimalTempCelsius(20.0f); pineAphids.setMaxTempCelsius(30.0f);
        pineAphids.setGrowthRate(2.5f); pineAphids.setInitialBiomassDensity(80.0f); pineAphids.setInitialPopulationDensity(100.0f);
        pineAphids.setDiapause(true);
        pineAphids.setSugarHoneydewYield(35.0f); pineAphids.setProteinYield(8.0f);
        list.add(pineAphids);

        AccessoryPreset beanAphid = new AccessoryPreset("mutualist-aphid-fabae", "Black Bean Aphid (Aphis fabae)", AccessoryPreset.Category.APHID_MUTUALIST);
        beanAphid.setScientificName("Aphis fabae");
        beanAphid.setDescription("Herbaceous sap feeder cultivated on crop stems by Lasius niger colonies.");
        beanAphid.setBiome("TEMPERATE_DECIDUOUS");
        beanAphid.setLatitude(47.0);
        beanAphid.setMinTempCelsius(7.0f); beanAphid.setOptimalTempCelsius(22.0f); beanAphid.setMaxTempCelsius(32.0f);
        beanAphid.setGrowthRate(3.0f); beanAphid.setInitialBiomassDensity(70.0f); beanAphid.setInitialPopulationDensity(120.0f);
        beanAphid.setSugarHoneydewYield(40.0f); beanAphid.setProteinYield(6.0f);
        list.add(beanAphid);

        AccessoryPreset mealybugs = new AccessoryPreset("mutualist-mealybug-eurhizococcus", "Root Mealybugs (Eurhizococcus)", AccessoryPreset.Category.APHID_MUTUALIST);
        mealybugs.setScientificName("Eurhizococcus brasiliensis");
        mealybugs.setDescription("Subterranean root-dwelling scale insects farmed inside ant galleries.");
        mealybugs.setBiome("TEMPERATE_DECIDUOUS");
        mealybugs.setLatitude(44.0);
        mealybugs.setMinTempCelsius(6.0f); mealybugs.setOptimalTempCelsius(21.0f); mealybugs.setMaxTempCelsius(32.0f);
        mealybugs.setGrowthRate(1.5f); mealybugs.setInitialBiomassDensity(60.0f); mealybugs.setInitialPopulationDensity(120.0f);
        mealybugs.setSugarHoneydewYield(30.0f); mealybugs.setProteinYield(10.0f);
        list.add(mealybugs);

        // 3. PREY_INSECT
        AccessoryPreset mealworm = new AccessoryPreset("prey-mealworm", "Mealworm Larvae (Protein Prey)", AccessoryPreset.Category.PREY_INSECT);
        mealworm.setScientificName("Tenebrio molitor");
        mealworm.setDescription("High-protein soft cuticle larvae harvested by ant foraging patrols.");
        mealworm.setBiome("TEMPERATE_DECIDUOUS");
        mealworm.setLatitude(43.0);
        mealworm.setMinTempCelsius(10.0f); mealworm.setOptimalTempCelsius(25.0f); mealworm.setMaxTempCelsius(38.0f);
        mealworm.setGrowthRate(3.0f); mealworm.setInitialBiomassDensity(200.0f); mealworm.setInitialPopulationDensity(50.0f);
        mealworm.setProteinYield(80.0f); mealworm.setKillRate(1.5f);
        list.add(mealworm);

        AccessoryPreset caterpillar = new AccessoryPreset("prey-caterpillar-pieris", "Cabbage White Caterpillar (Pieris brassicae)", AccessoryPreset.Category.PREY_INSECT);
        caterpillar.setScientificName("Pieris brassicae");
        caterpillar.setDescription("Foliar herbivore caterpillar prey attacked by predatory and territorial ants.");
        caterpillar.setBiome("TEMPERATE_DECIDUOUS");
        caterpillar.setLatitude(46.0);
        caterpillar.setMinTempCelsius(8.0f); caterpillar.setOptimalTempCelsius(22.0f); caterpillar.setMaxTempCelsius(34.0f);
        caterpillar.setGrowthRate(2.4f); caterpillar.setInitialBiomassDensity(120.0f); caterpillar.setInitialPopulationDensity(40.0f);
        caterpillar.setProteinYield(60.0f);
        list.add(caterpillar);

        AccessoryPreset termitePrey = new AccessoryPreset("prey-termite-microtermes", "Termite Prey (Microtermes)", AccessoryPreset.Category.PREY_INSECT);
        termitePrey.setScientificName("Microtermes spp.");
        termitePrey.setDescription("Subterranean termite workers and alates raided by specialized ant predators.");
        termitePrey.setBiome("TROPICAL_RAINFOREST");
        termitePrey.setLatitude(5.0);
        termitePrey.setMinTempCelsius(18.0f); termitePrey.setOptimalTempCelsius(28.0f); termitePrey.setMaxTempCelsius(36.0f);
        termitePrey.setGrowthRate(3.5f); termitePrey.setInitialBiomassDensity(250.0f); termitePrey.setInitialPopulationDensity(300.0f);
        termitePrey.setProteinYield(70.0f);
        list.add(termitePrey);

        // 4. PREDATORS
        AccessoryPreset antlion = new AccessoryPreset("predator-antlion-myrmeleon", "Antlion Pitfall (Myrmeleon formicarius)", AccessoryPreset.Category.PREDATOR);
        antlion.setScientificName("Myrmeleon formicarius");
        antlion.setDescription("Sit-and-wait pitfall predator digging sand funnels that collapse under ant footsteps.");
        antlion.setBiome("MEDITERRANEAN");
        antlion.setLatitude(38.0);
        antlion.setMinTempCelsius(12.0f); antlion.setOptimalTempCelsius(28.0f); antlion.setMaxTempCelsius(42.0f);
        antlion.setGrowthRate(0.5f); antlion.setInitialBiomassDensity(20.0f); antlion.setInitialPopulationDensity(5.0f);
        antlion.setDiapause(true);
        antlion.setHuntMode(AccessoryPreset.HuntMode.TRAP_FUNNEL);
        antlion.setTargetCaste(AccessoryPreset.TargetCaste.WORKERS);
        antlion.setKillRate(5.0f); antlion.setAttackDamage(25.0f); antlion.setVisionRange(6.0f);
        list.add(antlion);

        AccessoryPreset spider = new AccessoryPreset("predator-spider-salticidae", "Jumping Spider (Salticidae)", AccessoryPreset.Category.PREDATOR);
        spider.setScientificName("Salticus scenicus");
        spider.setDescription("Agile stalk-and-ambush predator with stereoscopic visual acuity attacking solitary foragers.");
        spider.setBiome("TEMPERATE_DECIDUOUS");
        spider.setLatitude(46.0);
        spider.setMinTempCelsius(8.0f); spider.setOptimalTempCelsius(23.0f); spider.setMaxTempCelsius(35.0f);
        spider.setGrowthRate(0.6f); spider.setInitialBiomassDensity(15.0f); spider.setInitialPopulationDensity(8.0f);
        spider.setHuntMode(AccessoryPreset.HuntMode.AMBUSH);
        spider.setTargetCaste(AccessoryPreset.TargetCaste.WORKERS);
        spider.setKillRate(2.5f); spider.setAttackDamage(18.0f); spider.setVisionRange(8.0f);
        list.add(spider);

        AccessoryPreset asianHornet = new AccessoryPreset("predator-asian-hornet", "Asian Hornet (Vespa velutina)", AccessoryPreset.Category.PREDATOR);
        asianHornet.setScientificName("Vespa velutina");
        asianHornet.setDescription("Hovering apex predator specialized in intercepting returning honeybee foragers at hive entrance.");
        asianHornet.setBiome("TEMPERATE_DECIDUOUS");
        asianHornet.setLatitude(44.5);
        asianHornet.setMinTempCelsius(10.0f); asianHornet.setOptimalTempCelsius(25.0f); asianHornet.setMaxTempCelsius(36.0f);
        asianHornet.setGrowthRate(1.4f); asianHornet.setInitialBiomassDensity(25.0f); asianHornet.setInitialPopulationDensity(12.0f);
        asianHornet.setHuntMode(AccessoryPreset.HuntMode.DIRECT_ATTACK);
        asianHornet.setTargetCaste(AccessoryPreset.TargetCaste.WORKERS);
        asianHornet.setKillRate(8.0f); asianHornet.setAttackDamage(30.0f);
        list.add(asianHornet);

        AccessoryPreset beeEaterWasp = new AccessoryPreset("predator-bee-eater-wasp", "European Bee-eater Wasp (Philanthus)", AccessoryPreset.Category.PREDATOR);
        beeEaterWasp.setScientificName("Philanthus triangulum");
        beeEaterWasp.setDescription("Solitary digger wasp paralyzing foraging worker bees to provision underground larval cells.");
        beeEaterWasp.setBiome("MEDITERRANEAN");
        beeEaterWasp.setLatitude(42.0);
        beeEaterWasp.setMinTempCelsius(12.0f); beeEaterWasp.setOptimalTempCelsius(26.0f); beeEaterWasp.setMaxTempCelsius(38.0f);
        beeEaterWasp.setGrowthRate(1.0f); beeEaterWasp.setInitialBiomassDensity(10.0f); beeEaterWasp.setInitialPopulationDensity(6.0f);
        beeEaterWasp.setHuntMode(AccessoryPreset.HuntMode.AMBUSH);
        beeEaterWasp.setTargetCaste(AccessoryPreset.TargetCaste.WORKERS);
        beeEaterWasp.setKillRate(6.0f); beeEaterWasp.setAttackDamage(22.0f);
        list.add(beeEaterWasp);

        AccessoryPreset honeyBuzzard = new AccessoryPreset("predator-honey-buzzard", "European Honey Buzzard (Raptor Wasp Hunter)", AccessoryPreset.Category.PREDATOR);
        honeyBuzzard.setScientificName("Pernis apivorus");
        honeyBuzzard.setDescription("Specialized raptor bird excavating underground wasp and bee nests for larval comb consumption.");
        honeyBuzzard.setBiome("TEMPERATE_DECIDUOUS");
        honeyBuzzard.setLatitude(49.0);
        honeyBuzzard.setMinTempCelsius(8.0f); honeyBuzzard.setOptimalTempCelsius(22.0f); honeyBuzzard.setMaxTempCelsius(32.0f);
        honeyBuzzard.setGrowthRate(0.2f); honeyBuzzard.setInitialBiomassDensity(2.0f); honeyBuzzard.setInitialPopulationDensity(2.0f);
        honeyBuzzard.setHuntMode(AccessoryPreset.HuntMode.DIRECT_ATTACK);
        honeyBuzzard.setTargetCaste(AccessoryPreset.TargetCaste.ALL_CASTES);
        honeyBuzzard.setKillRate(35.0f); honeyBuzzard.setAttackDamage(80.0f);
        list.add(honeyBuzzard);

        AccessoryPreset megaponera = new AccessoryPreset("predator-megaponera", "Megaponera Termite Raider (Megaponera analis)", AccessoryPreset.Category.PREDATOR);
        megaponera.setScientificName("Megaponera analis");
        megaponera.setDescription("Column-raiding predatory ant specialized exclusively on termite mounds with paramedic rescue behavior.");
        megaponera.setBiome("TROPICAL_RAINFOREST");
        megaponera.setLatitude(-1.0);
        megaponera.setMinTempCelsius(20.0f); megaponera.setOptimalTempCelsius(29.0f); megaponera.setMaxTempCelsius(38.0f);
        megaponera.setGrowthRate(2.5f); megaponera.setInitialBiomassDensity(80.0f); megaponera.setInitialPopulationDensity(150.0f);
        megaponera.setHuntMode(AccessoryPreset.HuntMode.DIRECT_ATTACK);
        megaponera.setTargetCaste(AccessoryPreset.TargetCaste.WORKERS);
        megaponera.setKillRate(12.0f); megaponera.setAttackDamage(28.0f);
        list.add(megaponera);

        AccessoryPreset woodpecker = new AccessoryPreset("predator-woodpecker", "Black Woodpecker (Bark Beetle Predator)", AccessoryPreset.Category.PREDATOR);
        woodpecker.setScientificName("Dryocopus martius");
        woodpecker.setDescription("Avian predator chiseling dead wood to forage carpenter ants and bark beetles.");
        woodpecker.setBiome("TAIGA_BOREAL");
        woodpecker.setLatitude(58.0);
        woodpecker.setMinTempCelsius(-5.0f); woodpecker.setOptimalTempCelsius(18.0f); woodpecker.setMaxTempCelsius(30.0f);
        woodpecker.setGrowthRate(0.3f); woodpecker.setInitialBiomassDensity(4.0f); woodpecker.setInitialPopulationDensity(3.0f);
        woodpecker.setHuntMode(AccessoryPreset.HuntMode.DIRECT_ATTACK);
        woodpecker.setTargetCaste(AccessoryPreset.TargetCaste.WORKERS);
        woodpecker.setKillRate(20.0f); woodpecker.setAttackDamage(50.0f);
        list.add(woodpecker);

        AccessoryPreset tamandua = new AccessoryPreset("predator-tamandua", "Tamandua Anteater (Direct Nest Raid)", AccessoryPreset.Category.PREDATOR);
        tamandua.setScientificName("Tamandua tetradactyla");
        tamandua.setDescription("Arboreal and terrestrial anteater breaching nest chambers with long vermiform tongue.");
        tamandua.setBiome("TROPICAL_RAINFOREST");
        tamandua.setLatitude(-2.0);
        tamandua.setMinTempCelsius(20.0f); tamandua.setOptimalTempCelsius(30.0f); tamandua.setMaxTempCelsius(40.0f);
        tamandua.setGrowthRate(0.1f); tamandua.setInitialBiomassDensity(1.0f); tamandua.setInitialPopulationDensity(1.0f);
        tamandua.setHuntMode(AccessoryPreset.HuntMode.DIRECT_ATTACK);
        tamandua.setTargetCaste(AccessoryPreset.TargetCaste.ALL_CASTES);
        tamandua.setKillRate(25.0f); tamandua.setAttackDamage(90.0f);
        list.add(tamandua);

        // 5. PATHOGEN_PARASITE
        AccessoryPreset cordyceps = new AccessoryPreset("parasite-cordyceps", "Entomopathogenic Fungus (Cordyceps)", AccessoryPreset.Category.PATHOGEN_PARASITE);
        cordyceps.setScientificName("Ophiocordyceps unilateralis");
        cordyceps.setDescription("Zombie ant fungus altering host phototaxis and biting behavior prior to perithecia emergence.");
        cordyceps.setBiome("TROPICAL_RAINFOREST");
        cordyceps.setLatitude(3.0);
        cordyceps.setMinTempCelsius(15.0f); cordyceps.setOptimalTempCelsius(26.0f); cordyceps.setMaxTempCelsius(34.0f);
        cordyceps.setGrowthRate(4.0f); cordyceps.setInitialBiomassDensity(10.0f); cordyceps.setInitialPopulationDensity(30.0f);
        cordyceps.setHuntMode(AccessoryPreset.HuntMode.PARASITOID);
        cordyceps.setTargetCaste(AccessoryPreset.TargetCaste.WORKERS);
        cordyceps.setPathogenVector(AccessoryPreset.PathogenVector.AIRBORNE_SPORES);
        cordyceps.setTransmissionR0(3.8f); cordyceps.setIncubationDays(3.0f); cordyceps.setMortalityRate(25.0f);
        list.add(cordyceps);

        AccessoryPreset varroa = new AccessoryPreset("parasite-varroa", "Parasitic Mite (Varroa destructor)", AccessoryPreset.Category.PATHOGEN_PARASITE);
        varroa.setScientificName("Varroa destructor");
        varroa.setDescription("Ectoparasitic mite consuming honeybee fat bodies and transmitting Deformed Wing Virus (DWV).");
        varroa.setBiome("TEMPERATE_DECIDUOUS");
        varroa.setLatitude(45.0);
        varroa.setMinTempCelsius(12.0f); varroa.setOptimalTempCelsius(24.0f); varroa.setMaxTempCelsius(36.0f);
        varroa.setGrowthRate(2.8f); varroa.setInitialBiomassDensity(5.0f); varroa.setInitialPopulationDensity(80.0f);
        varroa.setHuntMode(AccessoryPreset.HuntMode.PARASITOID);
        varroa.setTargetCaste(AccessoryPreset.TargetCaste.BROOD_PUPAE);
        varroa.setPathogenVector(AccessoryPreset.PathogenVector.SOIL_CONTACT);
        varroa.setTransmissionR0(2.8f); varroa.setIncubationDays(2.0f); varroa.setMortalityRate(15.0f);
        list.add(varroa);

        AccessoryPreset eucharitid = new AccessoryPreset("parasite-eucharitid", "Parasitoid Wasp (Eucharitidae)", AccessoryPreset.Category.PATHOGEN_PARASITE);
        eucharitid.setScientificName("Kapala spp.");
        eucharitid.setDescription("Planidial larvae carried phoretically into ant brood chambers to parasitize pupae.");
        eucharitid.setBiome("TROPICAL_RAINFOREST");
        eucharitid.setLatitude(8.0);
        eucharitid.setMinTempCelsius(16.0f); eucharitid.setOptimalTempCelsius(27.0f); eucharitid.setMaxTempCelsius(35.0f);
        eucharitid.setGrowthRate(2.0f); eucharitid.setInitialBiomassDensity(5.0f); eucharitid.setInitialPopulationDensity(40.0f);
        eucharitid.setHuntMode(AccessoryPreset.HuntMode.PARASITOID);
        eucharitid.setTargetCaste(AccessoryPreset.TargetCaste.BROOD_PUPAE);
        eucharitid.setPathogenVector(AccessoryPreset.PathogenVector.GROOMING);
        eucharitid.setTransmissionR0(1.8f); eucharitid.setIncubationDays(4.0f); eucharitid.setMortalityRate(20.0f);
        list.add(eucharitid);

        AccessoryPreset nosema = new AccessoryPreset("parasite-nosema", "Intestinal Microsporidian (Nosema bombi)", AccessoryPreset.Category.PATHOGEN_PARASITE);
        nosema.setScientificName("Nosema bombi");
        nosema.setDescription("Obligate intracellular parasite affecting bumblebee midgut epithelium and drone fecundity.");
        nosema.setBiome("TEMPERATE_DECIDUOUS");
        nosema.setLatitude(50.0);
        nosema.setMinTempCelsius(4.0f); nosema.setOptimalTempCelsius(18.0f); nosema.setMaxTempCelsius(30.0f);
        nosema.setGrowthRate(3.2f); nosema.setInitialBiomassDensity(2.0f); nosema.setInitialPopulationDensity(150.0f);
        nosema.setHuntMode(AccessoryPreset.HuntMode.PARASITOID);
        nosema.setTargetCaste(AccessoryPreset.TargetCaste.WORKERS);
        nosema.setPathogenVector(AccessoryPreset.PathogenVector.CONTAMINATED_FOOD);
        nosema.setTransmissionR0(2.5f); nosema.setIncubationDays(5.0f); nosema.setMortalityRate(12.0f);
        list.add(nosema);

        // 6. FUNGI
        AccessoryPreset leucoagaricus = new AccessoryPreset("fungi-leucoagaricus", "Atta Symbiotic Fungus (Leucoagaricus)", AccessoryPreset.Category.FUNGI);
        leucoagaricus.setScientificName("Leucoagaricus gongylophorus");
        leucoagaricus.setDescription("Mutualistic gongylidia-bearing basidiomycete fungus cultivated by leafcutter ants.");
        leucoagaricus.setBiome("TROPICAL_RAINFOREST");
        leucoagaricus.setLatitude(0.0);
        leucoagaricus.setMinTempCelsius(18.0f); leucoagaricus.setOptimalTempCelsius(26.0f); leucoagaricus.setMaxTempCelsius(32.0f);
        leucoagaricus.setGrowthRate(4.5f); leucoagaricus.setInitialBiomassDensity(400.0f); leucoagaricus.setInitialPopulationDensity(1.0f);
        leucoagaricus.setProteinYield(50.0f); leucoagaricus.setSugarHoneydewYield(30.0f);
        list.add(leucoagaricus);

        AccessoryPreset termitomyces = new AccessoryPreset("fungi-termitomyces", "Termite Cultivated Fungus (Termitomyces)", AccessoryPreset.Category.FUNGI);
        termitomyces.setScientificName("Termitomyces titanicus");
        termitomyces.setDescription("Comb-dwelling symbiotic fungus digesting lignin and cellulose for Macrotermitinae.");
        termitomyces.setBiome("TROPICAL_RAINFOREST");
        termitomyces.setLatitude(-5.0);
        termitomyces.setMinTempCelsius(19.0f); termitomyces.setOptimalTempCelsius(27.0f); termitomyces.setMaxTempCelsius(33.0f);
        termitomyces.setGrowthRate(4.0f); termitomyces.setInitialBiomassDensity(350.0f); termitomyces.setInitialPopulationDensity(1.0f);
        termitomyces.setProteinYield(45.0f);
        list.add(termitomyces);

        // 7. DETRITIVORES
        AccessoryPreset springtails = new AccessoryPreset("detritivore-springtails", "Garbage Springtails (Detritivore Cleaner)", AccessoryPreset.Category.DETRITIVORE);
        springtails.setScientificName("Collembola spp.");
        springtails.setDescription("Commensal micro-arthropods cleaning nest waste dumps and consuming decomposing frass.");
        springtails.setBiome("TEMPERATE_DECIDUOUS");
        springtails.setLatitude(48.0);
        springtails.setMinTempCelsius(5.0f); springtails.setOptimalTempCelsius(19.0f); springtails.setMaxTempCelsius(28.0f);
        springtails.setGrowthRate(2.2f); springtails.setInitialBiomassDensity(50.0f); springtails.setInitialPopulationDensity(200.0f);
        springtails.setProteinYield(15.0f);
        list.add(springtails);

        AccessoryPreset woodlouse = new AccessoryPreset("prey-woodlouse-porcellio", "Common Rough Woodlouse (Porcellio scaber)", AccessoryPreset.Category.DETRITIVORE);
        woodlouse.setScientificName("Porcellio scaber");
        woodlouse.setDescription("Terrestrial isopod detritivore breaking down dead wood and leaf litter.");
        woodlouse.setBiome("TEMPERATE_DECIDUOUS");
        woodlouse.setLatitude(47.5);
        woodlouse.setMinTempCelsius(4.0f); woodlouse.setOptimalTempCelsius(18.0f); woodlouse.setMaxTempCelsius(28.0f);
        woodlouse.setGrowthRate(1.8f); woodlouse.setInitialBiomassDensity(60.0f); woodlouse.setInitialPopulationDensity(80.0f);
        woodlouse.setProteinYield(25.0f);
        list.add(woodlouse);

        AccessoryPreset lomechusa = new AccessoryPreset("detritivore-lomechusa", "Myrmecophilous Beetle (Lomechusa Commensal)", AccessoryPreset.Category.DETRITIVORE);
        lomechusa.setScientificName("Lomechusa strumosa");
        lomechusa.setDescription("Staphylinid beetle secreting appeasing trichome exudates to trick ant hosts into brood feeding.");
        lomechusa.setBiome("TEMPERATE_DECIDUOUS");
        lomechusa.setLatitude(47.0);
        lomechusa.setMinTempCelsius(7.0f); lomechusa.setOptimalTempCelsius(20.0f); lomechusa.setMaxTempCelsius(30.0f);
        lomechusa.setGrowthRate(1.1f); lomechusa.setInitialBiomassDensity(15.0f); lomechusa.setInitialPopulationDensity(30.0f);
        list.add(lomechusa);

        File[] dirs = {
                new File("data/presets/accessories"),
                new File("swarmforge-core/src/main/resources/presets/accessories"),
                new File("src/main/resources/presets/accessories")
        };

        for (File d : dirs) {
            d.mkdirs();
            for (AccessoryPreset p : list) {
                File target = new File(d, p.getId() + ".json");
                mapper.writeValue(target, p);
            }
        }

        assertTrue(list.size() >= 26);
    }
}
