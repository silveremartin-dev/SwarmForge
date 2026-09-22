/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpeciesBrainTrainingTest {

    @Test
    void testEndToEndUniversalMultiCasteTrainingAndValidation(@TempDir Path tempDir) throws Exception {
        System.out.println("=================================================================");
        System.out.println("🧠 Démarrage de l'Entraînement Approfondi du Cerveau Multi-Espèces ONNX");
        System.out.println("=================================================================");

        long startTotal = System.currentTimeMillis();

        // 1. Initialisation de la micro-simulation headless (24 entrées, 14 actions)
        HeadlessTrainingScenario scenario = new HeadlessTrainingScenario();
        scenario.setupColony(1 /* Queen */, 20 /* FSM Workers */, 5 /* Learner Ants */);

        // 2. Étape 1 : Imitation multi-castes approfondie (30 000 transitions simulées)
        long startImitation = System.currentTimeMillis();
        int imitationSteps = 30_000;
        scenario.runImitationPhase(imitationSteps);
        long timeImitation = System.currentTimeMillis() - startImitation;

        float imitationLoss = scenario.getImitationLoss();
        System.out.printf("👑 Phase 1 (Imitation Multi-Castes & Espèces) terminée en %d ms | Perte: %.4f%n",
                timeImitation, imitationLoss);
        assertTrue(imitationLoss < 1.0f, "La perte d'imitation doit être stable et converger (Loss < 1.0)");

        // 3. Étape 2 : Fine-Tuning Double-DQN (15 000 pas fermés)
        long startDqn = System.currentTimeMillis();
        int dqnSteps = 15_000;
        scenario.runDqnPhase(dqnSteps);
        long timeDqn = System.currentTimeMillis() - startDqn;

        System.out.printf("🎯 Phase 2 (Fine-Tuning Double-DQN) terminée en %d ms | Buffer: %d transitions%n",
                timeDqn, scenario.getSampleLearner() != null ? dqnSteps : 0);

        // 4. Étape 3 : Export ONNX du modèle entraîné
        File onnxFile = tempDir.resolve("species_brain.onnx").toFile();
        scenario.exportToOnnx(onnxFile);

        assertTrue(onnxFile.exists(), "Le fichier ONNX exporté doit exister");
        assertTrue(onnxFile.length() > 0, "La taille du fichier ONNX doit être supérieure à 0");
        System.out.printf("📦 Modèle ONNX universel exporté : %s (Taille: %d octets)%n", onnxFile.getName(), onnxFile.length());

        // Sauvegarde canonique dans resources/models/species_brain.onnx
        File resourceFile = new File("src/main/resources/models/species_brain.onnx");
        if (!resourceFile.getParentFile().exists()) {
            resourceFile.getParentFile().mkdirs();
        }
        scenario.exportToOnnx(resourceFile);
        System.out.printf("💾 Modèle ONNX sauvegardé dans les ressources de SwarmForge : %s%n", resourceFile.getPath());

        // 5. Étape 4 : Validation Comportementale Détaillée par Caste
        try (OnnxBrainArchitecture onnxBrain = new OnnxBrainArchitecture(onnxFile.getAbsolutePath())) {
            HeadlessTrainingScenario.MockSimulationContext ctx = (HeadlessTrainingScenario.MockSimulationContext) scenario.getContext();

            // A. Test Reine (QUEEN) : Dans le nid avec de l'énergie -> Ponte d'œufs (LAY_EGG) ou Soin
            Individual queen = new Individual(UUID.randomUUID(), Individual.Caste.QUEEN, 0, 0, 0);
            queen.setHomePosition(0, 0, 0);
            queen.setEnergy(90.0f);
            ReasoningArchitecture.Action queenAction = onnxBrain.decide(queen, ctx);
            assertNotNull(queenAction);
            System.out.printf("👑 [Reine] Décision dans la chambre royale : %s (attendu: LAY_EGG ou REST)%n", queenAction.type());

            // B. Test Soldat (SOLDIER) : Alarme / Ennemi détecté -> Attaque (ATTACK) ou Patrouille défensive
            ctx.alarmIntensity = 0.8f;
            ctx.enemyNearby = true;
            Individual soldier = new Individual(UUID.randomUUID(), Individual.Caste.SOLDIER, 10, 10, 0);
            ReasoningArchitecture.Action soldierAction = onnxBrain.decide(soldier, ctx);
            assertNotNull(soldierAction);
            System.out.printf("⚔️ [Soldat] Décision en situation d'alarme/ennemi : %s (attendu: ATTACK ou DEFEND_PATROL)%n", soldierAction.type());
            ctx.alarmIntensity = 0.0f;
            ctx.enemyNearby = false;

            // C. Test Nourrice (NURSE) : Dans le nid avec couvain -> Soin du couvain (TEND_BROOD / NURSE)
            Individual nurse = new Individual(UUID.randomUUID(), Individual.Caste.NURSE, 0, 0, 0);
            nurse.setHomePosition(0, 0, 0);
            nurse.setEnergy(85.0f);
            ReasoningArchitecture.Action nurseAction = onnxBrain.decide(nurse, ctx);
            assertNotNull(nurseAction);
            System.out.printf("🍼 [Nourrice] Décision dans la nurserie : %s (attendu: TEND_BROOD ou NURSE)%n", nurseAction.type());

            // D. Test Ouvrière / Récolteuse (WORKER) :
            // D1. Portant de la nourriture au nid -> Dépôt (DEPOSIT_FOOD)
            Individual workerAtNest = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 0, 0, 0);
            workerAtNest.setHomePosition(0, 0, 0);
            workerAtNest.setCarriedItem(Individual.CarriedItem.FOOD);
            ReasoningArchitecture.Action workerDeposit = onnxBrain.decide(workerAtNest, ctx);
            assertNotNull(workerDeposit);
            System.out.printf("🌾 [Ouvrière] Décision au nid avec nourriture : %s (attendu: DEPOSIT_FOOD)%n", workerDeposit.type());

            // D2. En quête de nourriture -> Foraging
            ctx.foodNearby = true;
            Individual workerForager = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 12, 12, 0);
            workerForager.setCarriedItem(Individual.CarriedItem.NONE);
            ReasoningArchitecture.Action workerForage = onnxBrain.decide(workerForager, ctx);
            assertNotNull(workerForage);
            System.out.printf("🌱 [Ouvrière] Décision devant une source de nourriture : %s (attendu: FORAGE)%n", workerForage.type());
            ctx.foodNearby = false;

            // E. Test Espèces différentes (Abeille / Apidae & Termite / Isoptera)
            org.swarmforge.core.species.CustomSpecies beeSpecies = new org.swarmforge.core.species.CustomSpecies();
            beeSpecies.setInsectType("BEE");
            Individual beeWorker = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 15, 15, 0);
            beeWorker.setSpecies(beeSpecies);
            ReasoningArchitecture.Action beeAction = onnxBrain.decide(beeWorker, ctx);
            assertNotNull(beeAction);
            System.out.printf("🐝 [Apidae / Abeille] Décision comportementale : %s%n", beeAction.type());
        }

        // 6. Étape 5 : Test du constructeur Classpath par défaut
        try (OnnxBrainArchitecture defaultBrain = new OnnxBrainArchitecture()) {
            Individual ant = new Individual(UUID.randomUUID(), Individual.Caste.WORKER, 0, 0, 0);
            ReasoningArchitecture.Action defAction = defaultBrain.decide(ant, scenario.getContext());
            assertNotNull(defAction);
            System.out.println("🌟 Constructeur par défaut (Classpath ONNX) fonctionnel : " + defAction.type());
        }

        long totalTime = System.currentTimeMillis() - startTotal;
        System.out.printf("🏁 Entraînement multi-castes/espèces complet et validation ONNX exécutés en %d ms !%n", totalTime);
        System.out.println("=================================================================");
    }
}
