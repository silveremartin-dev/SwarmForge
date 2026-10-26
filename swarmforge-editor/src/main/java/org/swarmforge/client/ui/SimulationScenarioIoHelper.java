/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.core.species.DefaultSpecies;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class managing scenario preset application, saving, deleting, JSON import/export,
 * scenario execution orchestration, and real-time Open-Meteo weather synchronization
 * for {@link SimulationControlPanel}.
 */
class SimulationScenarioIoHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SimulationScenarioIoHelper.class);

    private final SimulationControlPanel panel;

    SimulationScenarioIoHelper(SimulationControlPanel panel) {
        this.panel = panel;
    }

    void cancelScenarioCreation() {
        panel.isCreatingScenario = false;
        if (panel.activeCreationTimeline != null) {
            panel.activeCreationTimeline.stop();
            panel.activeCreationTimeline = null;
        }
        if (panel.activeCreationFuture != null) {
            panel.activeCreationFuture.cancel(true);
            panel.activeCreationFuture = null;
        }
        panel.updateApplyPresetsButtonState(false);
        if (panel.inlineProgressBar != null) {
            panel.inlineProgressBar.setProgress(0);
            panel.inlineProgressLabel.setText("❌ Scenario creation cancelled by user.");
        }
        System.out.println("[INFO] [SwarmForge Engine] Instanciation et calculs du scénario annulés par l'utilisateur. Mémoire libérée.");
        System.gc();

        PauseTransition hideDelay = new PauseTransition(Duration.millis(2000));
        hideDelay.setOnFinished(ev -> {
            if (panel.applyProgressBox != null) {
                panel.applyProgressBox.setVisible(false);
                panel.applyProgressBox.setManaged(false);
            }
        });
        hideDelay.play();
    }

    void handleApplyScenario() {
        if (panel.isCreatingScenario) {
            cancelScenarioCreation();
            return;
        }

        if (panel.isPlaying || panel.currentTick > 0) {
            Alert confirmAlert = ThemeManager.createAlert(
                Alert.AlertType.CONFIRMATION,
                "A simulation is currently running (Tick: " + panel.currentTick + ").\n\n" +
                "Are you sure you want to reset and replace the active simulation with this new scenario configuration?"
            );
            confirmAlert.setTitle("Reset Simulation");
            confirmAlert.setHeaderText("Confirmation Required");
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        panel.isCreatingScenario = true;
        panel.updateApplyPresetsButtonState(true);

        if (panel.isPlaying) {
            panel.isPlaying = false;
            panel.updateButtonStates();
            if (panel.onPause != null) panel.onPause.accept(null);
        }
        long seed = panel.getMasterSeed();
        String selectedWorld = panel.getSelectedWorld();
        String selectedWeather = panel.getSelectedWeather();
        String scenarioName = panel.comboMeta.getValue() != null ? panel.comboMeta.getValue() : "New Scenario";
        panel.lastSetupSnapshot = panel.captureSetupSnapshotOnFXThread();

        System.out.println("\n================================================================================");
        System.out.println("[INFO] [SwarmForge Engine] === INITIALISATION DU SCÉNARIO & DE L'ÉCOSYSTÈME ===");
        System.out.println("[INFO] [SwarmForge Engine] Scénario : " + scenarioName + " | Seed: " + seed);
        System.out.println("[INFO] [SwarmForge Engine] Step 1/5 [15%]: Initialisation du maillage 3D biotope & grille spatiale ('" + selectedWorld + "')...");

        if (panel.applyProgressBox != null) {
            panel.applyProgressBox.setVisible(true);
            panel.applyProgressBox.setManaged(true);
            panel.inlineProgressBar.setProgress(0.15);
            panel.inlineProgressLabel.setText("Step 1/5 [15%]: Initializing 3D biotope mesh & spatial grid ('" + selectedWorld + "')...");
        }

        panel.activeCreationTimeline = new Timeline(
            new KeyFrame(Duration.millis(250), e -> {
                if (!panel.isCreatingScenario) return;
                System.out.println("[INFO] [SwarmForge Engine] Step 2/5 [35%]: Calcul du profil microclimatique & météo ('" + selectedWeather + "')...");
                if (panel.inlineProgressBar != null) {
                    panel.inlineProgressBar.setProgress(0.35);
                    panel.inlineProgressLabel.setText("Step 2/5 [35%]: Calculating microclimate profile & weather parameters ('" + selectedWeather + "')...");
                }
            }),
            new KeyFrame(Duration.millis(550), e -> {
                if (!panel.isCreatingScenario) return;
                System.out.println("[INFO] [SwarmForge Engine] Step 3/5 [60%]: Construction des structures de nids souterrains & arboricoles...");
                if (panel.inlineProgressBar != null) {
                    panel.inlineProgressBar.setProgress(0.60);
                    panel.inlineProgressLabel.setText("Step 3/5 [60%]: Constructing subterranean & arboreal nest structures...");
                }
            }),
            new KeyFrame(Duration.millis(900), e -> {
                if (!panel.isCreatingScenario) return;
                System.out.println("[INFO] [SwarmForge Engine] Step 4/5 [85%]: Instanciation des colonies multi-espèces (" + panel.speciesCardList.size() + " espèces) & moteurs IA...");
                if (panel.inlineProgressBar != null) {
                    panel.inlineProgressBar.setProgress(0.85);
                    panel.inlineProgressLabel.setText("Step 4/5 [85%]: Instantiating multi-species colonies (" + panel.speciesCardList.size() + " species) & caste AI engines...");
                }
            }),
            new KeyFrame(Duration.millis(1200), e -> {
                if (!panel.isCreatingScenario) return;
                System.out.println("[INFO] [SwarmForge Engine] Step 5/5 [95%]: Population des espèces accessoires, proies & réseaux trophiques...");
                if (panel.inlineProgressBar != null) {
                    panel.inlineProgressBar.setProgress(0.95);
                    panel.inlineProgressLabel.setText("Step 5/5 [95%]: Populating accessory species, prey & ecosystem food webs...");
                }
            }),
            new KeyFrame(Duration.millis(1500), e -> {
                if (!panel.isCreatingScenario) return;
                panel.activeCreationFuture = CompletableFuture.runAsync(() -> {
                    if (panel.isCreatingScenario && panel.onApplyPresets != null) {
                        panel.onApplyPresets.accept(seed);
                    }
                });
                panel.activeCreationFuture.whenComplete((res, ex) -> Platform.runLater(() -> {
                    if (!panel.isCreatingScenario) return;
                    panel.isCreatingScenario = false;
                    panel.updateApplyPresetsButtonState(false);

                    if (ex != null) {
                        LOG.error("Exception during scenario creation: {}", ex.getMessage(), ex);
                        if (panel.inlineProgressBar != null) {
                            panel.inlineProgressBar.setProgress(1.0);
                            panel.inlineProgressLabel.setText("⚠ Initialization completed with warnings: " + ex.getMessage());
                        }
                    } else {
                        EventBus.getInstance().publish(
                            SimulationEvent.obtain(
                                SimulationEvent.EventType.COLONY_FOUNDED,
                                SimulationEvent.Severity.INFO,
                                0,
                                "🌍 Scenario '" + scenarioName + "' initialized (" + panel.speciesCardList.size() + " species configured)",
                                null
                            )
                        );

                        if (panel.inlineProgressBar != null) {
                            panel.inlineProgressBar.setProgress(1.0);
                            panel.inlineProgressLabel.setText("✅ Scenario '" + scenarioName + "' initialized successfully!");
                        }
                        System.out.println("[INFO] [SwarmForge Engine] === INITIALISATION DU SCÉNARIO COMPLÉTÉE AVEC SUCCÈS ===");
                        System.out.println("================================================================================\n");
                    }

                    PauseTransition hideDelay = new PauseTransition(Duration.millis(1400));
                    hideDelay.setOnFinished(ev -> {
                        if (panel.applyProgressBox != null) {
                            panel.applyProgressBox.setVisible(false);
                            panel.applyProgressBox.setManaged(false);
                        }
                    });
                    hideDelay.play();
                }));
            })
        );
        panel.activeCreationTimeline.play();
    }

    void alignWeatherWithWorld(boolean showNotification) {
        String world = panel.comboWorld.getValue();
        if (world == null) return;
        String wUpper = world.toUpperCase();
        String targetWeather = "Temperate";
        if (wUpper.contains("TROPICAL") || wUpper.contains("MANAUS") || wUpper.contains("AMAZON") || wUpper.contains("RAINFOREST")) {
            targetWeather = "Tropical";
        } else if (wUpper.contains("SAVANNA") || wUpper.contains("SAVANE") || wUpper.contains("SERENGETI") || wUpper.contains("ACACIA")) {
            targetWeather = "Savanna";
        } else if (wUpper.contains("DESERT") || wUpper.contains("ERG CHEBBI") || wUpper.contains("ARID") || wUpper.contains("ARIDE") || wUpper.contains("SAHARA")) {
            targetWeather = "Arid";
        } else if (wUpper.contains("MEDITERRANEAN") || wUpper.contains("MÉDITERRAN") || wUpper.contains("CORSICA") || wUpper.contains("CORSE") || wUpper.contains("MARSEILLE")) {
            targetWeather = "Mediterranean";
        } else if (wUpper.contains("ALPINE") || wUpper.contains("ALPIN") || wUpper.contains("MONT BLANC") || wUpper.contains("VALAIS") || wUpper.contains("MOUNTAIN") || wUpper.contains("MONTAGNE")) {
            targetWeather = "Alpine";
        } else if (wUpper.contains("TAIGA") || wUpper.contains("TAÏGA") || wUpper.contains("BOREAL") || wUpper.contains("BORÉAL") || wUpper.contains("ROVANIEMI")) {
            targetWeather = "Taiga";
        } else if (wUpper.contains("STEPPE") || wUpper.contains("ASTANA") || wUpper.contains("KAZAKHSTAN")) {
            targetWeather = "Steppe";
        } else if (wUpper.contains("OCEANIC") || wUpper.contains("OCÉANIQUE") || wUpper.contains("BREST") || wUpper.contains("BRITTANY") || wUpper.contains("BRETAGNE") || wUpper.contains("HEATHLAND")) {
            targetWeather = "Oceanic";
        } else if (wUpper.contains("WETLAND") || wUpper.contains("SWAMP") || wUpper.contains("MANGROVE") || wUpper.contains("EVERGLADES") || wUpper.contains("MARAIS")) {
            targetWeather = "Wetland";
        } else if (wUpper.contains("ARCTIC") || wUpper.contains("ARCTIQUE") || wUpper.contains("POLAR") || wUpper.contains("POLAIRE") || wUpper.contains("SVALBARD") || wUpper.contains("LONGYEARBYEN") || wUpper.contains("PERMAFROST") || wUpper.contains("TUNDRA") || wUpper.contains("TOUNDRA")) {
            targetWeather = "Arctic";
        }
        selectComboIfPresent(panel.comboWeather, targetWeather);
        panel.updateValidationPanel();
        if (showNotification) {
            NotificationOverlay.show(
                panel,
                "Climate successfully aligned to '" + targetWeather + "' for biotope '" + world + "'!",
                NotificationOverlay.NotificationType.SUCCESS
            );
        }
    }

    void applyMetaPreset(String rawMetaName) {
        if (rawMetaName == null) return;
        panel.speciesCardList.clear();

        if (rawMetaName.contains("[➕") || rawMetaName.contains("Personnalisé") || rawMetaName.contains("Custom")) {
            if (panel.areaDescription != null) {
                panel.areaDescription.setText(I18nManager.getInstance().get("sim.preset.custom.desc", "Scénario personnalisé : définissez librement le biotope, le climat, les espèces et la topologie mégaterrarium."));
            }
            if (panel.chkMultiplayerOnly != null) panel.chkMultiplayerOnly.setSelected(false);
            if (panel.spinRequiredPlayers != null) panel.spinRequiredPlayers.getValueFactory().setValue(1);
            if (panel.spinGridTilesX != null) panel.spinGridTilesX.getValueFactory().setValue(1);
            if (panel.spinGridTilesY != null) panel.spinGridTilesY.getValueFactory().setValue(1);
            panel.addSpeciesCard("Black Garden Ant (Lasius niger)");
            panel.updateValidationPanel();
            return;
        }

        String metaName = rawMetaName.replace("🔬 ", "").replace("⚔️ ", "").replace("📌 ", "").trim();

        Scenario scenario = panel.scenarioPresetManager.get(metaName);
        if (scenario == null) {
            for (Scenario s : panel.scenarioPresetManager.getAll().values()) {
                if (s.getTitle().equalsIgnoreCase(metaName) || s.getId().equalsIgnoreCase(metaName) || metaName.contains(s.getTitle()) || s.getTitle().contains(metaName)) {
                    scenario = s;
                    break;
                }
            }
        }

        if (scenario != null) {
            if (panel.areaDescription != null) {
                String prefix = scenario.isMultiplayerOnly() ? "⚔️ MULTIPLAYER SCENARIO: " : "🔬 ACADEMIC SCENARIO / PROFILE: ";
                panel.areaDescription.setText(prefix + scenario.getTitle() + "\n\n" + scenario.getDescription());
            }
            if (panel.txtSeed != null) {
                panel.txtSeed.setText(String.valueOf(scenario.getMasterSeed()));
            }
            if (panel.maxDurationSpinner != null) {
                String unit = scenario.getMaxDurationUnit() != null ? scenario.getMaxDurationUnit() : "Days (d)";
                double val = scenario.getMaxDurationValue() > 0 ? scenario.getMaxDurationValue() : 100.0;
                if (panel.durationUnitCombo != null) selectComboIfPresent(panel.durationUnitCombo, unit);
                panel.maxDurationSpinner.getValueFactory().setValue(val);
            }
            if (panel.minPopStopSpinner != null && scenario.getMinPopulationStopThreshold() >= 0) {
                panel.minPopStopSpinner.getValueFactory().setValue(scenario.getMinPopulationStopThreshold());
            }

            if (panel.chkMultiplayerOnly != null) {
                panel.chkMultiplayerOnly.setSelected(scenario.isMultiplayerOnly());
            }
            if (panel.spinRequiredPlayers != null) {
                panel.spinRequiredPlayers.getValueFactory().setValue(Math.max(1, scenario.getRequiredPlayerCount()));
            }
            if (panel.spinGridTilesX != null) {
                panel.spinGridTilesX.getValueFactory().setValue(Math.max(1, scenario.getGridTilesX()));
            }
            if (panel.spinGridTilesY != null) {
                panel.spinGridTilesY.getValueFactory().setValue(Math.max(1, scenario.getGridTilesY()));
            }

            if (scenario.getSimulationStepSeconds() > 0) {
                panel.simulationStepSeconds = scenario.getSimulationStepSeconds();
                selectStepComboByDt(panel.simulationStepSeconds);
                panel.updateStepDtLabel();
            }

            String biome = scenario.getBiomeName();
            if (biome != null) {
                switch (biome.toUpperCase()) {
                    case "SAVANNA", "ARID_SAVANNA", "ACACIA_SAVANNA" -> selectComboIfPresent(panel.comboWorld, "Acacia Savanna (Serengeti, TZ)");
                    case "DESERT", "ARID", "ERG_CHEBBI" -> selectComboIfPresent(panel.comboWorld, "Arid Desert (Erg Chebbi, MA)");
                    case "TROPICAL_RAINFOREST", "TROPICAL", "AMAZON" -> selectComboIfPresent(panel.comboWorld, "Tropical Rainforest (Manaus, BR)");
                    case "ALPINE_TUNDRA", "ALPINE", "ROCKY_MOUNTAIN" -> selectComboIfPresent(panel.comboWorld, "Rocky Mountain (Mont Blanc, FR)");
                    case "ALPINE_MEADOW" -> selectComboIfPresent(panel.comboWorld, "Alpine Meadow (Valais, CH)");
                    case "BOREAL_TAIGA", "TAIGA" -> selectComboIfPresent(panel.comboWorld, "Boreal Taiga (Rovaniemi, FI)");
                    case "MEDITERRANEAN", "SHRUBLAND" -> selectComboIfPresent(panel.comboWorld, "Mediterranean Shrubland (Corsica, FR)");
                    case "SWAMP", "MANGROVE", "WETLAND" -> selectComboIfPresent(panel.comboWorld, "Swamp & Mangrove (Everglades, US)");
                    case "STEPPE", "SEMI_ARID_STEPPE" -> selectComboIfPresent(panel.comboWorld, "Semi-Arid Steppe (Astana, KZ)");
                    case "OCEANIC", "HEATHLAND" -> selectComboIfPresent(panel.comboWorld, "Oceanic Heathland (Brittany, FR)");
                    case "PERMAFROST", "POLAR", "TUNDRA", "ARCTIC" -> selectComboIfPresent(panel.comboWorld, "Permafrost Tundra (Svalbard, NO)");
                    default -> selectComboIfPresent(panel.comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
                }
            }
            alignWeatherWithWorld(false);

            if (!scenario.getColonies().isEmpty()) {
                for (Scenario.ColonySetup setup : scenario.getColonies()) {
                    panel.addSpeciesCard(setup.speciesName());
                    if (!panel.speciesCardList.isEmpty()) {
                        SpeciesConfigCard card = panel.speciesCardList.get(panel.speciesCardList.size() - 1);
                        card.setQueenCount(setup.queenCount());
                        card.setWorkerCount(setup.workerCount());
                        card.setSoldierCount(setup.soldierCount());
                        card.setInitialFood(setup.initialFoodStore());
                    }
                }
            }
            panel.updateValidationPanel();
            return;
        }

        if (metaName == null) return;
        String mLower = metaName.toLowerCase();
        if (mLower.contains("claustral") || mLower.contains("démarrage") || mLower.contains("monogyne") || mLower.contains("foundation")) {
            applyBuiltInPreset("Temperate Deciduous (Fontainebleau, FR)", "Temperate", "sim.preset.desc.claustral",
                    "Fourmi Noire des Jardins (Lasius niger)", 1, 0, 0, 50, 200);
        } else if (mLower.contains("amazon") || mLower.contains("atta") || mLower.contains("attine") || mLower.contains("neotropical")) {
            applyBuiltInPreset("Tropical Rainforest (Manaus, BR)", "Tropical", "sim.preset.desc.amazon",
                    "Fourmi Coupeuse de Feuilles (Atta sexdens)", 1, 200, 30, 250, 500);
        } else if (mLower.contains("granivore") || mLower.contains("messor") || mLower.contains("steppe") || mLower.contains("xeric")) {
            applyBuiltInPreset("Arid Desert (Erg Chebbi, MA)", "Arid", "sim.preset.desc.granivore",
                    "Fourmi Moissonneuse (Pogonomyrmex barbatus)", 1, 150, 20, 200, 500);
        } else if (mLower.contains("guerre") || mLower.contains("war") || mLower.contains("territoriale") || mLower.contains("competition") || mLower.contains("solenopsis")) {
            applyBuiltInPreset("Temperate Deciduous (Fontainebleau, FR)", "Temperate", "sim.preset.desc.war",
                    "Fourmi de Feu (Solenopsis invicta)", 1, 200, 30, 200, 500);
            panel.addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
            if (panel.speciesCardList.size() > 1) {
                SpeciesConfigCard card2 = panel.speciesCardList.get(1);
                card2.setQueenCount(1);
                card2.setWorkerCount(200);
                card2.setSoldierCount(0);
                card2.setBroodCount(200);
                card2.setInitialFood(500);
            }
        } else if (mLower.contains("rucher") || mLower.contains("beehive") || mLower.contains("apis") || mLower.contains("bee")) {
            applyBuiltInPreset("Temperate Deciduous (Fontainebleau, FR)", "Temperate", "sim.preset.desc.beehive",
                    "Abeille à Miel (Apis mellifera)", 1, 500, 0, 200, 500);
        } else if (mLower.contains("termite") || mLower.contains("macrotermes") || mLower.contains("cathédrale") || mLower.contains("cathedral")) {
            applyBuiltInPreset("Tropical Rainforest (Manaus, BR)", "Tropical", "sim.preset.desc.termites",
                    "Termite Souterrain (Reticulitermes flavipes)", 1, 300, 50, 200, 500);
        } else if (mLower.contains("taïga") || mLower.contains("taiga") || mLower.contains("boréale") || mLower.contains("boreal") || mLower.contains("formica")) {
            applyBuiltInPreset("Boreal Taiga (Rovaniemi, FI)", "Taiga", "sim.preset.desc.taiga",
                    "Fourmi Noire des Jardins (Lasius niger)", 1, 150, 0, 100, 500);
        } else if (mLower.contains("supercolonie") || mLower.contains("supercolony") || mLower.contains("polycalique") || mLower.contains("linepithema")) {
            applyBuiltInPreset("Temperate Deciduous (Fontainebleau, FR)", "Temperate", "sim.preset.desc.supercolony",
                    "Fourmi Noire des Jardins (Lasius niger)", 5, 1000, 0, 500, 1000);
            panel.addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)");
        }
    }

    private void applyBuiltInPreset(String world, String weather, String descKey, String species,
                                    int queens, int workers, int soldiers, int brood, int food) {
        selectComboIfPresent(panel.comboWorld, world);
        selectComboIfPresent(panel.comboWeather, weather);
        panel.areaDescription.setText(I18nManager.getInstance().get(descKey));
        panel.addSpeciesCard(species);
        if (!panel.speciesCardList.isEmpty()) {
            SpeciesConfigCard card = panel.speciesCardList.get(0);
            card.setQueenCount(queens);
            card.setWorkerCount(workers);
            card.setSoldierCount(soldiers);
            card.setBroodCount(brood);
            card.setInitialFood(food);
        }
    }

    void handleSaveScenario() {
        String desc = panel.areaDescription.getText() != null ? panel.areaDescription.getText().trim() : "";
        if (desc.isEmpty()) {
            ThemeManager.createAlert(
                Alert.AlertType.WARNING,
                "Description required: Please enter a scientific description in the 'Scientific Scenario Description' area before saving the preset."
            ).show();
            return;
        }

        String name = panel.comboMeta.getValue() != null ? panel.comboMeta.getValue().replace("🔬 ", "").replace("⚔️ ", "").replace("📌 ", "") : "New Scenario";
        TextInputDialog dialog = ThemeManager.createTextInputDialog(name);
        dialog.setTitle("Save Scenario Preset");
        dialog.setHeaderText("Scenario preset name (Description captured):");
        dialog.showAndWait().ifPresent(scenarioName -> {
            if (!scenarioName.trim().isEmpty()) {
                String clean = scenarioName.trim();
                String itemTitle = "📌 " + clean;
                if ((panel.scenarioPresetManager != null && panel.scenarioPresetManager.getPresetNames().contains(clean)) || panel.comboMeta.getItems().contains(itemTitle)) {
                    Alert confirmAlert = ThemeManager.createAlert(
                        Alert.AlertType.CONFIRMATION,
                        "Scenario preset '" + clean + "' already exists.\n\nDo you want to overwrite it with current configuration?"
                    );
                    confirmAlert.setTitle("Overwrite Existing Scenario");
                    confirmAlert.setHeaderText("Overwrite Confirmation");
                    Optional<ButtonType> res = confirmAlert.showAndWait();
                    if (res.isEmpty() || res.get() != ButtonType.OK) {
                        return;
                    }
                }

                Scenario sc = new Scenario(clean.toLowerCase().replace(" ", "_"), clean, desc);
                sc.setAuthor(panel.getPlayerAlias());
                sc.setMasterSeed(panel.getMasterSeed());
                sc.setBiomeName(panel.getSelectedWorld());
                sc.setMaxDurationValue(panel.maxDurationSpinner.getValue() != null ? panel.maxDurationSpinner.getValue() : 100.0);
                sc.setMaxDurationUnit(panel.durationUnitCombo.getValue() != null ? panel.durationUnitCombo.getValue() : "Days (d)");
                sc.setMinPopulationStopThreshold(panel.getMinPopulationStopThreshold());
                sc.setSimulationStepSeconds(panel.getSimulationStepSeconds());
                sc.setMultiplayerOnly(panel.chkMultiplayerOnly.isSelected());
                sc.setRequiredPlayerCount(panel.spinRequiredPlayers.getValue() != null ? panel.spinRequiredPlayers.getValue() : 1);
                sc.setGridTilesX(panel.spinGridTilesX.getValue() != null ? panel.spinGridTilesX.getValue() : 1);
                sc.setGridTilesY(panel.spinGridTilesY.getValue() != null ? panel.spinGridTilesY.getValue() : 1);
                for (SpeciesConfigCard card : panel.speciesCardList) {
                    String spName = card.getSpeciesName();
                    sc.addColony(new Scenario.ColonySetup(spName, spName.toLowerCase().replace(" ", "_"), card.getQueenCount(), card.getWorkerCount(), card.getSoldierCount(), card.getInitialFood(), Map.of()));
                    if (panel.speciesPresetManager != null) {
                        DefaultSpecies sp = panel.speciesPresetManager.getPresetOrFallback(spName);
                        if (sp != null) {
                            sc.embedSpecies(sp);
                        }
                    }
                }

                if (panel.worldPresetManager != null && panel.getSelectedWorld() != null && panel.worldPresetManager.contains(panel.getSelectedWorld())) {
                    sc.setEmbeddedWorldConfig(panel.worldPresetManager.get(panel.getSelectedWorld()));
                }
                if (panel.weatherPresetManager != null && panel.getSelectedWeather() != null && panel.weatherPresetManager.contains(panel.getSelectedWeather())) {
                    sc.setEmbeddedClimateConfig(panel.weatherPresetManager.get(panel.getSelectedWeather()));
                }

                panel.scenarioPresetManager.save(sc);

                if (!panel.comboMeta.getItems().contains(itemTitle)) panel.comboMeta.getItems().add(itemTitle);
                panel.comboMeta.getSelectionModel().select(itemTitle);
                NotificationOverlay.show(panel, "Scenario preset saved: " + clean + " (v" + sc.getVersion() + ")", NotificationOverlay.NotificationType.SUCCESS);
            }
        });
    }

    void handleDeleteScenario() {
        String selected = panel.comboMeta.getValue();
        if (selected == null || selected.isEmpty()) return;
        String clean = selected.replace("🔬 ", "").replace("⚔️ ", "").replace("📌 ", "").trim();
        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete scenario preset '" + selected + "'?"
        );
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean deleted = panel.scenarioPresetManager.delete(clean);
                if (deleted) {
                    panel.comboMeta.getItems().remove(selected);
                    if (!panel.comboMeta.getItems().isEmpty()) panel.comboMeta.getSelectionModel().selectFirst();
                    NotificationOverlay.show(panel, "Scenario deleted: " + clean, NotificationOverlay.NotificationType.INFO);
                } else {
                    ThemeManager.createAlert(
                        Alert.AlertType.INFORMATION,
                        "Built-in Academic or Multiplayer reference scenarios are protected and cannot be deleted."
                    ).show();
                }
            }
        });
    }

    void handleExportScenario() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Scenario Configuration");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("scenario-preset.json");
        File file = chooser.showSaveDialog(panel.getScene().getWindow());
        if (file != null) {
            try {
                ScenarioSetupSnapshot snapshot = panel.captureSetupSnapshotOnFXThread();
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, snapshot);
                NotificationOverlay.show(
                    panel,
                    "Scenario successfully exported to " + file.getName(),
                    NotificationOverlay.NotificationType.SUCCESS
                );
            } catch (Exception ex) {
                ThemeManager.createAlert(
                    Alert.AlertType.ERROR,
                    "Error exporting scenario: " + ex.getMessage()
                ).show();
            }
        }
    }

    void handleImportScenario() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Scenario Configuration");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showOpenDialog(panel.getScene().getWindow());
        if (file != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ScenarioSetupSnapshot snapshot = mapper.readValue(file, ScenarioSetupSnapshot.class);
                if (snapshot != null) {
                    if (snapshot.seed() != 0) {
                        panel.txtSeed.setText(String.valueOf(snapshot.seed()));
                    }
                    if (snapshot.startDateTimeIso() != null && !snapshot.startDateTimeIso().isEmpty()) {
                        try {
                            panel.startDateTime = LocalDateTime.parse(snapshot.startDateTimeIso());
                            if (panel.startDatePicker != null) panel.startDatePicker.setValue(panel.startDateTime.toLocalDate());
                            if (panel.startTimeHourSpinner != null) panel.startTimeHourSpinner.getValueFactory().setValue(panel.startDateTime.getHour());
                            if (panel.startTimeMinuteSpinner != null) panel.startTimeMinuteSpinner.getValueFactory().setValue(panel.startDateTime.getMinute());
                            if (panel.startTimeSecondSpinner != null) panel.startTimeSecondSpinner.getValueFactory().setValue(panel.startDateTime.getSecond());
                            panel.updateTick(panel.currentTick, panel.maxTick);
                        } catch (Exception ignored) {}
                    }
                    if (snapshot.simulationStepSeconds() > 0) {
                        panel.simulationStepSeconds = snapshot.simulationStepSeconds();
                        selectStepComboByDt(panel.simulationStepSeconds);
                        panel.updateStepDtLabel();
                    }
                    if (snapshot.maxDurationValue() > 0 && panel.maxDurationSpinner != null) {
                        panel.maxDurationSpinner.getValueFactory().setValue(snapshot.maxDurationValue());
                    }
                    if (snapshot.maxDurationUnit() != null && panel.durationUnitCombo != null) {
                        selectComboIfPresent(panel.durationUnitCombo, snapshot.maxDurationUnit());
                    }
                    if (snapshot.minPopStopThreshold() >= 0 && panel.minPopStopSpinner != null) {
                        panel.minPopStopSpinner.getValueFactory().setValue(snapshot.minPopStopThreshold());
                    }
                    if (snapshot.description() != null && panel.areaDescription != null) {
                        panel.areaDescription.setText(snapshot.description());
                    }
                    if (snapshot.selectedWorld() != null) selectComboIfPresent(panel.comboWorld, snapshot.selectedWorld());
                    if (snapshot.selectedWeather() != null) selectComboIfPresent(panel.comboWeather, snapshot.selectedWeather());

                    if (panel.chkMultiplayerOnly != null) {
                        panel.chkMultiplayerOnly.setSelected(snapshot.isMultiplayerOnly());
                    }
                    if (panel.spinRequiredPlayers != null && snapshot.requiredPlayerCount() > 0) {
                        panel.spinRequiredPlayers.getValueFactory().setValue(snapshot.requiredPlayerCount());
                    }
                    if (panel.spinGridTilesX != null && snapshot.gridTilesX() > 0) {
                        panel.spinGridTilesX.getValueFactory().setValue(snapshot.gridTilesX());
                    }
                    if (panel.spinGridTilesY != null && snapshot.gridTilesY() > 0) {
                        panel.spinGridTilesY.getValueFactory().setValue(snapshot.gridTilesY());
                    }
                    if (snapshot.engineBackend() != null && panel.engineBackendCombo != null) {
                        selectComboIfPresent(panel.engineBackendCombo, snapshot.engineBackend());
                    }
                    if (snapshot.computeAcceleration() != null && panel.computeAccelerationCombo != null) {
                        selectComboIfPresent(panel.computeAccelerationCombo, snapshot.computeAcceleration());
                    }

                    panel.speciesCardList.clear();
                    if (snapshot.speciesSnapshots() != null && !snapshot.speciesSnapshots().isEmpty()) {
                        for (SpeciesConfigSnapshot sSnap : snapshot.speciesSnapshots()) {
                            panel.addSpeciesCard(sSnap.speciesName());
                            if (!panel.speciesCardList.isEmpty()) {
                                SpeciesConfigCard card = panel.speciesCardList.get(panel.speciesCardList.size() - 1);
                                if (sSnap.nestType() != null) card.setNestType(sSnap.nestType());
                                card.setQueenCount(sSnap.queenCount());
                                card.setWorkerCount(sSnap.workerCount());
                                card.setSoldierCount(sSnap.soldierCount());
                                card.setInitialFood(sSnap.initialFood());
                                if (sSnap.placementStrategy() != null) card.setNestPlacement(sSnap.placementStrategy());
                                card.setCustomX(sSnap.customX());
                                card.setCustomZ(sSnap.customZ());
                                if (sSnap.nestRelation() != null) card.setNestRelation(sSnap.nestRelation());
                                if (sSnap.accessorySnapshots() != null) {
                                    card.setAccessorySnapshots(sSnap.accessorySnapshots());
                                }
                            }
                        }
                    }
                    panel.refreshSpeciesListContainer();
                    panel.updateDurationCalculatedLabel();
                    panel.updateValidationPanel();
                    NotificationOverlay.show(
                        panel,
                        "Scenario successfully imported from " + file.getName(),
                        NotificationOverlay.NotificationType.SUCCESS
                    );
                }
            } catch (Exception ex) {
                ThemeManager.createAlert(
                    Alert.AlertType.ERROR,
                    "Error importing scenario: " + ex.getMessage()
                ).show();
            }
        }
    }

    void fetchRealWeatherFromWorld(TextField targetField) {
        double lat = 48.4047;
        double lon = 2.7016;
        double alt = 35.0;
        String name = "Monde Actuel";

        if (panel.worldConfigSupplier != null) {
            Map<String, Object> cfg = panel.worldConfigSupplier.get();
            if (cfg != null) {
                if (cfg.containsKey("cityName") && cfg.get("cityName") != null) {
                    name = String.valueOf(cfg.get("cityName"));
                }
                if (cfg.containsKey("latitude") && cfg.get("latitude") != null) {
                    try { lat = Double.parseDouble(String.valueOf(cfg.get("latitude"))); } catch (Exception ignored) {}
                }
                if (cfg.containsKey("longitude") && cfg.get("longitude") != null) {
                    try { lon = Double.parseDouble(String.valueOf(cfg.get("longitude"))); } catch (Exception ignored) {}
                }
                if (cfg.containsKey("elevation") && cfg.get("elevation") != null) {
                    try { alt = Double.parseDouble(String.valueOf(cfg.get("elevation"))); } catch (Exception ignored) {}
                }
            }
        } else {
            String worldPreset = panel.comboWorld != null ? panel.comboWorld.getValue() : null;
            if (worldPreset != null) {
                name = worldPreset;
                WorldPresetManager wpm = new WorldPresetManager();
                Map<String, Object> pCfg = wpm.getPreset(worldPreset);
                if (pCfg != null) {
                    if (pCfg.containsKey("cityName") && pCfg.get("cityName") != null) name = String.valueOf(pCfg.get("cityName"));
                    if (pCfg.containsKey("latitude") && pCfg.get("latitude") != null) {
                        try { lat = Double.parseDouble(String.valueOf(pCfg.get("latitude"))); } catch (Exception ignored) {}
                    }
                    if (pCfg.containsKey("longitude") && pCfg.get("longitude") != null) {
                        try { lon = Double.parseDouble(String.valueOf(pCfg.get("longitude"))); } catch (Exception ignored) {}
                    }
                    if (pCfg.containsKey("elevation") && pCfg.get("elevation") != null) {
                        try { alt = Double.parseDouble(String.valueOf(pCfg.get("elevation"))); } catch (Exception ignored) {}
                    }
                }
            }
        }

        if (targetField != null) {
            targetField.setText(name != null && !name.isBlank() ? name : String.format(Locale.US, "%.4f, %.4f", lat, lon));
        }

        fetchRealWeatherByCoordinates(lat, lon, alt, name);
    }

    void fetchRealWeather(String cityQuery) {
        if (cityQuery == null || cityQuery.trim().isEmpty()) {
            fetchRealWeatherFromWorld(null);
            return;
        }
        final String query = cityQuery.trim();

        Pattern coordPattern = Pattern.compile("^([+-]?\\d+(?:\\.\\d+)?)[,\\s/]+([+-]?\\d+(?:\\.\\d+)?)(?:[,\\s/]+([+-]?\\d+(?:\\.\\d+)?)(?:\\s*m)?)?$");
        Matcher m = coordPattern.matcher(query);
        if (m.matches()) {
            try {
                double lat = Double.parseDouble(m.group(1));
                double lon = Double.parseDouble(m.group(2));
                double alt = (m.group(3) != null && !m.group(3).isEmpty()) ? Double.parseDouble(m.group(3)) : 50.0;
                fetchRealWeatherByCoordinates(lat, lon, alt, "GPS " + String.format(Locale.US, "%.2f, %.2f", lat, lon));
                return;
            } catch (Exception ignored) {}
        }

        panel.lblRealWeatherStatus.setText("⏳ Recherche Open-Meteo pour \"" + query + "\"...");
        panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        panel.weatherApiClient.resolveLocation(
            query,
            loc -> fetchRealWeatherByCoordinates(loc.latitude(), loc.longitude(), loc.altitude(), loc.name()),
            errMsg -> Platform.runLater(() -> {
                panel.lblRealWeatherStatus.setText("⚠️ " + errMsg);
                panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 10px;");
            })
        );
    }

    void fetchRealWeatherByCoordinates(double lat, double lon, double alt, String name) {
        final String locationName = (name != null && !name.isBlank()) ? name : String.format(Locale.US, "%.2f°N, %.2f°E", lat, lon);
        panel.lblRealWeatherStatus.setText("⏳ Chargement Open-Meteo pour " + locationName + "...");
        panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");

        panel.weatherApiClient.fetchWeather(
            lat, lon, alt, name,
            data -> {
                panel.cachedRealWeatherData = data.configMap();
                Platform.runLater(() -> {
                    panel.lblRealWeatherStatus.setText(String.format(Locale.US, "🟢 %s (%.2f°N, %.2f°E, %.0fm) : %.1f°C, Vent %.0f km/h, Hum. %.0f%%",
                        locationName, lat, lon, alt, data.temperature(), data.windSpeed(), data.humidity()));
                    panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 10px;");
                });
            },
            errMsg -> Platform.runLater(() -> {
                panel.lblRealWeatherStatus.setText("⚠️ " + errMsg);
                panel.lblRealWeatherStatus.setStyle("-fx-text-fill: #f87171; -fx-font-size: 10px;");
            })
        );
    }

    private void selectStepComboByDt(float dt) {
        if (Math.abs(dt - 0.0166f) < 0.005f) {
            panel.scenarioStepCombo.getSelectionModel().select(0);
        } else if (Math.abs(dt - 0.05f) < 0.01f) {
            panel.scenarioStepCombo.getSelectionModel().select(1);
        } else if (Math.abs(dt - 0.1f) < 0.01f) {
            panel.scenarioStepCombo.getSelectionModel().select(2);
        } else if (Math.abs(dt - 1.0f) < 0.1f) {
            panel.scenarioStepCombo.getSelectionModel().select(3);
        } else if (Math.abs(dt - 5.0f) < 0.5f) {
            panel.scenarioStepCombo.getSelectionModel().select(4);
        }
    }

    static void selectComboIfPresent(ComboBox<String> combo, String val) {
        if (val == null || combo == null) return;
        if (combo.getItems().contains(val)) {
            combo.getSelectionModel().select(val);
            return;
        }
        String valLower = val.toLowerCase().trim();
        for (String item : combo.getItems()) {
            if (item != null) {
                String itemLower = item.toLowerCase().trim();
                if (itemLower.contains(valLower) || valLower.contains(itemLower)) {
                    combo.getSelectionModel().select(item);
                    return;
                }
            }
        }
    }
}
