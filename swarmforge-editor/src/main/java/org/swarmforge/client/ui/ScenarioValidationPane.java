/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.swarmforge.client.util.I18nManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent scenario validation pane and dynamic biological diagnostic engine.
 * Real-time checks for demographic viability, thermal/hygrometric stress tolerances,
 * trophic food-web balances, geotechnical ground intersection, and network connectivity.
 */
public class ScenarioValidationPane {

    public record ScenarioWarning(
        String category,
        String severity,
        String message,
        String fixButtonText,
        Runnable fixAction
    ) {}

    private final SimulationControlPanel panel;
    private final VBox container = new VBox(6);
    private VBox warningsContainer;

    public ScenarioValidationPane(SimulationControlPanel panel) {
        this.panel = panel;
        buildUi();
    }

    public VBox getContainer() {
        return container;
    }

    private void buildUi() {
        container.getStyleClass().add("card-pane");
        container.setStyle("-fx-border-color: #38bdf8; -fx-border-width: 1px; -fx-border-radius: 5px;");

        Label lblTitle = new Label(I18nManager.getInstance().get("sim.validation.title"));
        lblTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label lblSubtitle = new Label(I18nManager.getInstance().get("sim.validation.subtitle"));
        lblSubtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 9.5px;");

        warningsContainer = new VBox(6);
        container.getChildren().addAll(lblTitle, lblSubtitle, new Separator(), warningsContainer);
    }

    public void updateValidationPanel() {
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(this::updateValidationPanel);
            return;
        }
        if (warningsContainer == null) return;
        warningsContainer.getChildren().clear();

        List<ScenarioWarning> warnings = evaluateScenarioWarnings();
        if (warnings.isEmpty()) {
            HBox okBox = new HBox(8);
            okBox.setAlignment(Pos.CENTER_LEFT);
            okBox.setStyle("-fx-background-color: rgba(34, 197, 94, 0.1); -fx-border-color: #22c55e; -fx-border-radius: 4px; -fx-padding: 6 10;");

            Label lblOkIcon = new Label("✅");
            lblOkIcon.setStyle("-fx-font-size: 12px;");

            Label lblOkText = new Label(I18nManager.getInstance().get("sim.validation.valid"));
            lblOkText.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 11px;");

            okBox.getChildren().addAll(lblOkIcon, lblOkText);
            warningsContainer.getChildren().add(okBox);
        } else {
            for (ScenarioWarning warn : warnings) {
                HBox warnRow = new HBox(10);
                warnRow.setAlignment(Pos.CENTER_LEFT);
                String styleClass = "HIGH".equals(warn.severity()) ? "warn-row-high" : ("MEDIUM".equals(warn.severity()) ? "warn-row-medium" : "warn-row-info");
                warnRow.getStyleClass().add(styleClass);

                Label lblMsg = new Label(warn.message());
                lblMsg.getStyleClass().add("warn-msg-text");
                lblMsg.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(lblMsg, Priority.ALWAYS);

                Button btnFix = new Button(warn.fixButtonText());
                btnFix.getStyleClass().add("btn-primary");
                btnFix.setOnAction(e -> {
                    if (warn.fixAction() != null) {
                        warn.fixAction().run();
                    }
                    updateValidationPanel();
                });

                warnRow.getChildren().addAll(lblMsg, btnFix);
                warningsContainer.getChildren().add(warnRow);
            }
        }
    }

    public List<ScenarioWarning> evaluateScenarioWarnings() {
        List<ScenarioWarning> list = new ArrayList<>();

        // 0. Mode Serveur & Connexion Réseau
        if (panel.isServerExecutionMode() && !panel.isServerConnected()) {
            list.add(new ScenarioWarning(
                "RÉSEAU", "HIGH",
                I18nManager.getInstance().get("sim.warn.server_disconnected", "⚠️ Mode Serveur sélectionné mais aucun serveur gRPC n'est connecté."),
                I18nManager.getInstance().get("sim.warn.server_connect_action", "🚀 Démarrer / Connecter Serveur"),
                () -> {
                    if (panel.getOnServerConnectAction() != null) panel.getOnServerConnectAction().run();
                }
            ));
        }

        // In Server Join mode, the physical world & local species cards are bypassed (governed by the remote server)
        if (panel.isServerExecutionMode() && panel.isServerJoinMode()) {
            return list;
        }

        List<SpeciesConfigCard> speciesCardList = panel.speciesCardList;
        if (speciesCardList == null) return list;

        // 1. Population & Espèces
        if (speciesCardList.isEmpty()) {
            list.add(new ScenarioWarning(
                "ESPÈCES", "HIGH",
                "🔴 No insect species configured. Simulation will start in an empty terrarium without colony.",
                "➕ Add (Lasius niger)",
                () -> panel.addSpeciesCard("Fourmi Noire des Jardins (Lasius niger)")
            ));
        } else {
            int totalAnts = 0;
            for (SpeciesConfigCard card : speciesCardList) {
                int cardAnts = card.getQueenCount() + card.getWorkerCount() + card.getSoldierCount() + card.getBroodCount();
                totalAnts += cardAnts;

                if (card.getQueenCount() == 0) {
                    list.add(new ScenarioWarning(
                        "DÉMOGRAPHIE", "MEDIUM",
                        String.format("🟡 Colonie '%s' : Aucune Reine (0 reine). La colonie s'éteindra sans renouvellement du couvain.", card.getSpeciesName()),
                        "👑 Add 1 Queen",
                        () -> card.setQueenCount(1)
                    ));
                }

                if (card.getQueenCount() > 0 && card.getBroodCount() <= 0) {
                    list.add(new ScenarioWarning(
                        "DÉMOGRAPHIE", "MEDIUM",
                        String.format("🟡 Colonie '%s' : Reine présente mais couvain nul (0 couvain). Renouvellement de la colonie compromis.", card.getSpeciesName()),
                        "🥚 Add 500 Brood",
                        () -> card.setBroodCount(500)
                    ));
                }

                String nestType = card.getNestType() != null ? card.getNestType() : panel.getSelectedNestType();
                int estCap = SpeciesConfigCard.estimateNestCapacity(nestType);
                if (cardAnts > estCap) {
                    list.add(new ScenarioWarning(
                        "NID", "MEDIUM",
                        String.format("🟡 Colonie '%s' : Population (%,d ind.) > Capacité du nid '%s' (~%,d ind.). %,d ind. émergeront en surface.",
                            card.getSpeciesName(), cardAnts, nestType, estCap, (cardAnts - estCap)),
                        "🏠 Expand Nest",
                        () -> {
                            List<String> compats = SpeciesConfigCard.getCompatibleNestTypes(card.getSpeciesName());
                            if (compats.size() > 1) {
                                card.setNestType(compats.get(compats.size() - 1));
                            } else {
                                card.setWorkerCount(Math.min(card.getWorkerCount(), estCap));
                            }
                        }
                    ));
                }

                // Biological Nest Incompatibility check
                List<String> compats = SpeciesConfigCard.getCompatibleNestTypes(card.getSpeciesName());
                if (card.getNestType() != null && !compats.isEmpty() && !compats.contains(card.getNestType())) {
                    list.add(new ScenarioWarning(
                        "NID", "MEDIUM",
                        String.format("🟡 Le nid '%s' n'est pas biologiquement adapté à l'espèce '%s'.", card.getNestType(), card.getSpeciesName()),
                        "🔄 Align with Adapted Nest",
                        () -> card.setNestType(compats.get(0))
                    ));
                }

                // Initial food reserve check
                if (card.getInitialFood() <= 0) {
                    list.add(new ScenarioWarning(
                        "RESSOURCES", "INFO",
                        String.format("💡 Colonie '%s' : Réserve initiale de nourriture nulle (0 unités). Risque de famine rapide.", card.getSpeciesName()),
                        "🍖 Allocate 500 Food",
                        () -> card.setInitialFood(500)
                    ));
                }
            }

            if (totalAnts == 0) {
                list.add(new ScenarioWarning(
                    "ESPÈCES", "HIGH",
                    "🔴 Total insect count zero (0 workers, 0 queens).",
                    "➕ Add 500 Workers",
                    () -> {
                        if (!speciesCardList.isEmpty()) {
                            speciesCardList.get(0).setWorkerCount(500);
                            speciesCardList.get(0).setQueenCount(1);
                        }
                    }
                ));
            }

            // Geotechnical, Hydrological, and Multi-Nest Diagnostics
            String selWorld = panel.comboWorld != null ? panel.comboWorld.getValue() : null;
            float wtDepth = estimateWaterTableDepth(selWorld);
            float bedrockDepth = 25.0f;
            for (SpeciesConfigCard card : speciesCardList) {
                float estDepth = 20.0f;
                if (estDepth > wtDepth) {
                    list.add(new ScenarioWarning(
                        "HYDROLOGIE", "MEDIUM",
                        String.format("🟡 Colonie '%s' : Le nid (~%.0fm) croise la Nappe Phréatique (%.1fm dans '%s'). Chambres profondes inondées.", card.getSpeciesName(), estDepth, wtDepth, (selWorld != null ? selWorld : "Monde")),
                        "💧 Adapt Depth",
                        this::updateValidationPanel
                    ));
                }
                if (estDepth > bedrockDepth) {
                    list.add(new ScenarioWarning(
                        "GÉOLOGIE", "MEDIUM",
                        String.format("🟡 Colonie '%s' : Le nid (~%.0fm) croise la Roche Mère (%.0fm). Galeries défléchies latéralement.", card.getSpeciesName(), estDepth, bedrockDepth),
                        "⛏️ Cap to Bedrock",
                        this::updateValidationPanel
                    ));
                }
            }

            if (speciesCardList.size() >= 2) {
                for (int i = 0; i < speciesCardList.size(); i++) {
                    for (int j = i + 1; j < speciesCardList.size(); j++) {
                        SpeciesConfigCard c1 = speciesCardList.get(i);
                        SpeciesConfigCard c2 = speciesCardList.get(j);
                        list.add(new ScenarioWarning(
                            "TERRITOIRE", "INFO",
                            String.format("💡 Proximité Nids : '%s' et '%s'. Galeries souterraines interconnectées à l'instanciation.", c1.getSpeciesName(), c2.getSpeciesName()),
                            "🛡️ Chokepoint",
                            this::updateValidationPanel
                        ));
                    }
                }
            }
        }

        // 2. Climat vs Biotope Alignment
        String world = panel.comboWorld != null ? panel.comboWorld.getValue() : null;
        String weather = panel.comboWeather != null ? panel.comboWeather.getValue() : null;
        if (world != null && weather != null) {
            String wLower = world.toLowerCase();
            String weatherLower = weather.toLowerCase();

            boolean isAridWorld = wLower.contains("désert") || wLower.contains("aride") || wLower.contains("arid") || wLower.contains("erg");
            boolean isPolarWorld = wLower.contains("alpin") || wLower.contains("toundra") || wLower.contains("arctic") || wLower.contains("taïga");
            boolean isTropicalWorld = wLower.contains("tropical") || wLower.contains("amazon") || wLower.contains("jungle");

            if (isAridWorld && (weatherLower.contains("arctic") || weatherLower.contains("polar"))) {
                list.add(new ScenarioWarning(
                    "CLIMAT", "HIGH",
                    String.format("🔴 Incompatibilité Climat/Biotope : Biotope aride ('%s') associé à un climat polaire ('%s').", world, weather),
                    "🌤️ Align Climate",
                    () -> panel.alignWeatherWithWorld()
                ));
            } else if (isPolarWorld && (weatherLower.contains("arid") || weatherLower.contains("tropical"))) {
                list.add(new ScenarioWarning(
                    "CLIMAT", "HIGH",
                    String.format("🔴 Incompatibilité Climat/Biotope : Biotope nordique/alpin ('%s') associé à un climat aride/tropical ('%s').", world, weather),
                    "🌤️ Align Climate",
                    () -> panel.alignWeatherWithWorld()
                ));
            } else if (isTropicalWorld && weatherLower.contains("arctic")) {
                list.add(new ScenarioWarning(
                    "CLIMAT", "HIGH",
                    String.format("🔴 Incompatibilité Climat/Biotope : Jungle tropicale ('%s') sous un climat arctique ('%s').", world, weather),
                    "🌤️ Align Climate",
                    () -> panel.alignWeatherWithWorld()
                ));
            }
        }

        // 2b. Species Thermal & Hygrometric Tolerance vs Biome Climate Cross-Reference
        if (world != null || weather != null) {
            float estimatedTemp = estimateAmbientTemperature(world, weather);
            float estimatedHum = estimateAmbientHumidity(world, weather);
            for (SpeciesConfigCard card : speciesCardList) {
                String speciesName = card.getSpeciesName();
                org.swarmforge.core.species.Species sp = panel.speciesPresetManager.getPresetOrFallback(speciesName);
                if (sp == null) {
                    sp = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies(speciesName);
                }
                if (sp != null) {
                    float minT = sp.getMinTempCelsius();
                    float maxT = sp.getMaxTempCelsius();
                    if (estimatedTemp < minT || estimatedTemp > maxT) {
                        final org.swarmforge.core.species.Species targetSp = sp;
                        list.add(new ScenarioWarning(
                            "STRESS THERMIQUE", "HIGH",
                            String.format("⚠️ Risque de Stress Thermique : Température estimée (%.1f°C) hors des tolérances de '%s' [%.1f°C - %.1f°C].",
                                estimatedTemp, card.getSpeciesName(), minT, maxT),
                            "🌡️ Adjust Compatible Climate",
                            () -> autoSelectCompatibleEnvironmentForSpecies(targetSp)
                        ));
                    }

                    float minH = sp.getMinHumidityPercent();
                    float maxH = sp.getMaxHumidityPercent();
                    if (estimatedHum < minH || estimatedHum > maxH) {
                        final org.swarmforge.core.species.Species targetSp = sp;
                        list.add(new ScenarioWarning(
                            "STRESS HYGROMÉTRIQUE", "MEDIUM",
                            String.format("💧 Risque de Stress Hygrométrique : Humidité estimée (%.0f%%) hors des tolérances de '%s' [%.0f%% - %.0f%%].",
                                estimatedHum, card.getSpeciesName(), minH, maxH),
                            "💧 Adjust Compatible Humidity",
                            () -> autoSelectCompatibleEnvironmentForSpecies(targetSp)
                        ));
                    }
                }
            }
        }

        // 3. Pas de calcul Δt vs Population scale
        int grandTotalAnts = 0;
        for (SpeciesConfigCard card : speciesCardList) {
            grandTotalAnts += card.getQueenCount() + card.getWorkerCount() + card.getSoldierCount() + card.getBroodCount();
        }
        if (panel.getSimulationStepSeconds() >= 5.0f && grandTotalAnts > 50000) {
            list.add(new ScenarioWarning(
                "PHYSIQUE", "INFO",
                String.format("💡 Pas de calcul Δt très élevé (5.0 s) pour %,d individus. Des imprécisions d'intégration éthologique peuvent se produire.", grandTotalAnts),
                "⏱️ Set Δt to 50 ms",
                () -> {
                    if (panel.scenarioStepCombo != null) panel.scenarioStepCombo.getSelectionModel().select(1);
                    panel.setSimulationStepSeconds(0.05f);
                }
            ));
        }

        // 4. Durée Max
        String dUnit = panel.durationUnitCombo != null ? panel.durationUnitCombo.getValue() : "Days (d)";
        if ("Days (d)".equals(dUnit) && panel.maxDurationSpinner != null && panel.maxDurationSpinner.getValue() != null && panel.maxDurationSpinner.getValue() > 365) {
            list.add(new ScenarioWarning(
                "DURÉE", "INFO",
                String.format("💡 Durée maximale très élevée (%.0f jours). Une durée de 30 à 100 jours est généralement recommandée.", panel.maxDurationSpinner.getValue()),
                "⏱️ Set to 100 days",
                () -> {
                    if (panel.maxDurationSpinner != null) panel.maxDurationSpinner.getValueFactory().setValue(100.0);
                }
            ));
        } else if ("Ticks".equals(dUnit) && panel.maxDurationSpinner != null && panel.maxDurationSpinner.getValue() != null && panel.maxDurationSpinner.getValue() < 1000) {
            list.add(new ScenarioWarning(
                "DURÉE", "INFO",
                String.format("💡 Nombre de ticks très court (%.0f). La simulation risque de s'arrêter prématurément.", panel.maxDurationSpinner.getValue()),
                "⏱️ Set to 100,000 ticks",
                () -> {
                    if (panel.maxDurationSpinner != null) panel.maxDurationSpinner.getValueFactory().setValue(100000.0);
                }
            ));
        }

        // 5. Seuil Arrêt Population Min
        if (panel.minPopStopSpinner != null && panel.minPopStopSpinner.getValue() != null && panel.minPopStopSpinner.getValue() > 0 && panel.minPopStopSpinner.getValue() >= grandTotalAnts && grandTotalAnts > 0) {
            list.add(new ScenarioWarning(
                "ARRÊT", "MEDIUM",
                String.format("🟡 Seuil d'arrêt de population (%,d ind.) >= population initiale (%,d ind.). La simulation s'arrêtera immédiatement.", panel.minPopStopSpinner.getValue(), grandTotalAnts),
                "🛑 Disable Threshold (0)",
                () -> {
                    if (panel.minPopStopSpinner != null) panel.minPopStopSpinner.getValueFactory().setValue(0);
                }
            ));
        }

        // 6. Seed Validity
        if (panel.txtSeed != null) {
            try {
                Long.parseLong(panel.txtSeed.getText().trim());
            } catch (Exception e) {
                list.add(new ScenarioWarning(
                    "SEED", "INFO",
                    "💡 Graine aléatoire (Seed) non numérique ou vide. Un seed par défaut sera attribué.",
                    "🎲 New Seed",
                    () -> {
                        if (panel.txtSeed != null) panel.txtSeed.setText(String.valueOf((long)(Math.random() * 900000 + 100000)));
                    }
                ));
            }
        }

        // 7. Dynamic Trophic & Food Web Checks (Predator vs Prey availability)
        for (SpeciesConfigCard card : speciesCardList) {
            if (card.hasActivePredatorAccessory() && !card.hasActivePreyAccessory() && card.getInitialFood() <= 0) {
                list.add(new ScenarioWarning(
                    "ÉCOSYSTÈME / TROPHIQUE", "MEDIUM",
                    String.format("🟡 Incompatibilité Trophique : L'écosystème de '%s' comporte des prédateurs sans proies d'appoint ni réserve initiale de nourriture.", card.getSpeciesName()),
                    "🦗 Enable Prey (Arthropods / Mealworms)",
                    () -> {
                        card.enableAccessorySpeciesRole("PROIE");
                        card.setInitialFood(500);
                    }
                ));
            }
        }

        return list;
    }

    private float estimateWaterTableDepth(String world) {
        if (world == null) return 15.0f;
        String s = world.toLowerCase();
        if (s.contains("marécage") || s.contains("swamp") || s.contains("wetland") || s.contains("mangrove")) {
            return 4.0f;
        } else if (s.contains("tropical") || s.contains("jungle") || s.contains("rainforest") || s.contains("amazon")) {
            return 8.0f;
        } else if (s.contains("arid") || s.contains("desert") || s.contains("désert") || s.contains("savanna")) {
            return 35.0f;
        } else if (s.contains("alpin") || s.contains("toundra") || s.contains("taïga")) {
            return 10.0f;
        }
        return 15.0f;
    }

    private float estimateAmbientTemperature(String world, String weather) {
        String wStr = (world != null ? world : "") + " " + (weather != null ? weather : "");
        String s = wStr.toLowerCase();

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*°?C").matcher(wStr);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        if (s.contains("arctic") || s.contains("polar") || s.contains("toundra") || s.contains("alpin") || s.contains("taïga") || s.contains("taiga")) {
            return 8.0f;
        } else if (s.contains("arid") || s.contains("desert") || s.contains("désert") || s.contains("chebbi") || s.contains("savanna")) {
            return 32.0f;
        } else if (s.contains("tropical") || s.contains("manaus") || s.contains("rainforest") || s.contains("jungle") || s.contains("amazon")) {
            return 28.0f;
        } else if (s.contains("mediterran") || s.contains("garrigue")) {
            return 25.0f;
        } else {
            return 20.0f;
        }
    }

    private float estimateAmbientHumidity(String world, String weather) {
        String wStr = (world != null ? world : "") + " " + (weather != null ? weather : "");
        String s = wStr.toLowerCase();

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)\\s*%").matcher(wStr);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        if (s.contains("arid") || s.contains("desert") || s.contains("désert") || s.contains("chebbi")) {
            return 25.0f;
        } else if (s.contains("tropical") || s.contains("manaus") || s.contains("rainforest") || s.contains("jungle") || s.contains("amazon") || s.contains("humid")) {
            return 85.0f;
        } else if (s.contains("savanna") || s.contains("mediterran") || s.contains("garrigue")) {
            return 45.0f;
        } else if (s.contains("arctic") || s.contains("polar") || s.contains("toundra") || s.contains("taïga") || s.contains("taiga")) {
            return 50.0f;
        } else {
            return 65.0f;
        }
    }

    private void autoSelectCompatibleEnvironmentForSpecies(org.swarmforge.core.species.Species sp) {
        if (sp == null) return;
        float minT = sp.getMinTempCelsius();
        float maxT = sp.getMaxTempCelsius();
        float minH = sp.getMinHumidityPercent();

        if (minT >= 26.0f || minH <= 25.0f) {
            SpeciesConfigCard.selectComboIfPresent(panel.comboWorld, "Acacia Savanna (Serengeti, TZ)");
            SpeciesConfigCard.selectComboIfPresent(panel.comboWeather, "Savanna");
        } else if (maxT <= 18.0f) {
            SpeciesConfigCard.selectComboIfPresent(panel.comboWorld, "Boreal Taiga (Rovaniemi, FI)");
            SpeciesConfigCard.selectComboIfPresent(panel.comboWeather, "Taiga");
        } else if (minH >= 65.0f || minT >= 22.0f) {
            SpeciesConfigCard.selectComboIfPresent(panel.comboWorld, "Tropical Rainforest (Manaus, BR)");
            SpeciesConfigCard.selectComboIfPresent(panel.comboWeather, "Tropical");
        } else {
            SpeciesConfigCard.selectComboIfPresent(panel.comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
            SpeciesConfigCard.selectComboIfPresent(panel.comboWeather, "Temperate");
        }
    }
}
