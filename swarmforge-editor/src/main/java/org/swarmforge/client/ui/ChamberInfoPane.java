/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.TunnelNetwork;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Locale;

/**
 * Dedicated HUD & Inspection Overlay Pane for Subterranean Nest Chambers.
 * Displays biological occupants, microclimate, volume geometry, and stored resources
 * with independent toggle and viewport centering.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ChamberInfoPane extends VBox {

    private final Label titleLabel;
    private final Button btnClose;

    // Telemetry Labels
    private final Label lblSpeciesColony;
    private final Label lblOccupantsText;
    private final ProgressBar occupancyBar;
    private final Label lblResourcesRadius;
    private final Label lblSpecialty;
    private final Label lblPositionDepth;
    private final Label lblClimate;
    private final Label lblAtmosphere;
    private final Label lblVentilationHygiene;
    private final Label lblArchitecture;
    private final Label lblDimensions;
    private final Label lblLightPressure;
    private final Label lblStability;

    // Action Controls
    private final Button btnCenter;

    private TunnelNetwork.TunnelNode currentNode = null;
    private Colony currentColony = null;
    private Runnable onCenterHandler;
    private Runnable onCloseHandler;

    public ChamberInfoPane() {
        setSpacing(6);
        setPadding(new Insets(10, 12, 10, 12));
        setPrefWidth(360);
        setMaxWidth(380);
        setStyle("-fx-background-color: rgba(15, 23, 42, 0.94); " +
                "-fx-border-color: #38bdf8; -fx-border-width: 1.5; " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");

        // Header
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        titleLabel = new Label("🏛️ Chambre Souterraine");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnClose = new Button("✕");
        btnClose.setTooltip(new Tooltip("Fermer le panneau de la chambre"));
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> {
            setVisible(false);
            if (onCloseHandler != null) {
                onCloseHandler.run();
            }
        });

        headerBox.getChildren().addAll(titleLabel, spacer, btnClose);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: rgba(56, 189, 248, 0.3);");

        // Telemetry Box
        VBox telemetryBox = new VBox(5);

        // 0. Species & Colony Row
        lblSpeciesColony = new Label("🧬 Espèce: - | 🏛️ Colonie: -");
        lblSpeciesColony.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        // 1. Occupants Breakdown
        HBox occRow = new HBox(8);
        occRow.setAlignment(Pos.CENTER_LEFT);
        lblOccupantsText = new Label("👥 Occupants : 0");
        lblOccupantsText.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        Region occSpacer = new Region();
        HBox.setHgrow(occSpacer, Priority.ALWAYS);

        occupancyBar = new ProgressBar(0.0);
        occupancyBar.setPrefWidth(90);
        occupancyBar.setPrefHeight(10);
        occupancyBar.setStyle("-fx-accent: #38bdf8;");
        occRow.getChildren().addAll(lblOccupantsText, occSpacer, occupancyBar);

        // 2. Resources & Radius
        lblResourcesRadius = new Label("📦 Nourriture: 0.0 | Rayon: 0.0 m");
        lblResourcesRadius.setStyle("-fx-font-size: 11px; -fx-text-fill: #22c55e;");

        // 3. Specialty Function
        lblSpecialty = new Label("🍄 Fonction : Standard");
        lblSpecialty.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b;");

        // 4. Position & Depth
        lblPositionDepth = new Label("📐 Position: X=0.0m, Y=0.0m, Profondeur: 0.0m");
        lblPositionDepth.setStyle("-fx-font-size: 11px; -fx-text-fill: #e2e8f0;");

        // 5. Microclimate (Temp & Humidity)
        lblClimate = new Label("🌡️ Température: 20.0°C | 💧 Humidité: 65%");
        lblClimate.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");

        // 6. Atmospheric Gases (CO2 & O2)
        lblAtmosphere = new Label("💨 Gaz : CO₂: 420 ppm (0.042%) | O₂: 20.9%");
        lblAtmosphere.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");

        // 7. Ventilation & Sanitation Hygiene
        lblVentilationHygiene = new Label("🌬️ Aération: 0.10 m/s | 🛡️ Salubrité: 98% (🟢 Saine)");
        lblVentilationHygiene.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");

        // 8. Architecture
        lblArchitecture = new Label("🏛️ Type: Standard");
        lblArchitecture.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa;");

        // 9. Dimensions
        lblDimensions = new Label("📏 Cavité: Rx=0.0m, Ry=0.0m, Rz=0.0m");
        lblDimensions.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");

        // 10. Light & Pressure
        lblLightPressure = new Label("🌑 Régime: Aphotique (0 lux) | 🌪️ Pression: 1013 hPa");
        lblLightPressure.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        // 11. Stability & Status
        lblStability = new Label("🟢 Qualité d'air optimale & Structure stable");
        lblStability.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4ade80;");

        telemetryBox.getChildren().addAll(
                lblSpeciesColony,
                occRow,
                lblResourcesRadius,
                lblSpecialty,
                lblPositionDepth,
                lblClimate,
                lblAtmosphere,
                lblVentilationHygiene,
                lblArchitecture,
                lblDimensions,
                lblLightPressure,
                lblStability
        );

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-background-color: rgba(56, 189, 248, 0.3);");

        // Actions: Center Viewport
        btnCenter = new Button("🎯 Centrer la vue sur la chambre");
        btnCenter.setMaxWidth(Double.MAX_VALUE);
        btnCenter.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 6 10; -fx-background-radius: 4;");
        btnCenter.setOnAction(e -> {
            if (onCenterHandler != null) {
                onCenterHandler.run();
            }
        });

        getChildren().addAll(headerBox, sep1, telemetryBox, sep2, btnCenter);
        org.swarmforge.client.util.ThemeManager.getInstance().currentThemeProperty().addListener((obs, o, n) -> applyThemeStyle());
        applyThemeStyle();
        setNoChamberSelectedState();
    }

    public void applyThemeStyle() {
        boolean isDark = org.swarmforge.client.util.ThemeManager.getInstance().isDarkMode();
        if (isDark) {
            setStyle("-fx-background-color: rgba(15, 23, 42, 0.94); " +
                    "-fx-border-color: #38bdf8; -fx-border-width: 1.5; " +
                    "-fx-border-radius: 10; -fx-background-radius: 10;");
            titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
            btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
            lblSpeciesColony.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");
            lblOccupantsText.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");
            lblResourcesRadius.setStyle("-fx-font-size: 11px; -fx-text-fill: #22c55e;");
            lblSpecialty.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b;");
            lblPositionDepth.setStyle("-fx-font-size: 11px; -fx-text-fill: #e2e8f0;");
            lblClimate.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");
            lblVentilationHygiene.setStyle("-fx-font-size: 11px; -fx-text-fill: #38bdf8;");
            lblArchitecture.setStyle("-fx-font-size: 11px; -fx-text-fill: #a78bfa;");
            lblDimensions.setStyle("-fx-font-size: 11px; -fx-text-fill: #cbd5e1;");
            lblLightPressure.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        } else {
            setStyle("-fx-background-color: rgba(255, 255, 255, 0.96); " +
                    "-fx-border-color: #0284c7; -fx-border-width: 1.5; " +
                    "-fx-border-radius: 10; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");
            titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0369a1;");
            btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand;");
            lblSpeciesColony.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
            lblOccupantsText.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
            lblResourcesRadius.setStyle("-fx-font-size: 11px; -fx-text-fill: #15803d;");
            lblSpecialty.setStyle("-fx-font-size: 11px; -fx-text-fill: #b45309;");
            lblPositionDepth.setStyle("-fx-font-size: 11px; -fx-text-fill: #1e293b;");
            lblClimate.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");
            lblVentilationHygiene.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");
            lblArchitecture.setStyle("-fx-font-size: 11px; -fx-text-fill: #6d28d9;");
            lblDimensions.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
            lblLightPressure.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        }
        if (currentNode != null) {
            updateChamber(currentNode, currentColony);
        }
    }

    public void setNoChamberSelectedState() {
        this.currentNode = null;
        this.currentColony = null;
        titleLabel.setText("🏛️ Aucune chambre sélectionnée");
        lblSpeciesColony.setText("🧬 Espèce: - | 🏛️ Colonie: -");
        lblOccupantsText.setText("👥 Occupants : Aucun");
        occupancyBar.setProgress(0);
        lblResourcesRadius.setText("📦 Réserves: -");
        lblSpecialty.setText("🍄 Spécialité: -");
        lblPositionDepth.setText("📐 Position: -");
        lblClimate.setText("🌡️ Climat: -");
        lblAtmosphere.setText("💨 Gaz: -");
        lblVentilationHygiene.setText("🌬️ Aération: - | 🛡️ Salubrité: -");
        lblArchitecture.setText("🏛️ Type: -");
        lblDimensions.setText("📏 Cavité: -");
        lblLightPressure.setText("🌑 Régime: - | 🌪️ Pression: -");
        lblStability.setText("🟢 Structure Stable");
        boolean isDark = org.swarmforge.client.util.ThemeManager.getInstance().isDarkMode();
        lblAtmosphere.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (isDark ? "#4ade80;" : "#16a34a;"));
        lblStability.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + (isDark ? "#4ade80;" : "#16a34a;"));
        btnCenter.setDisable(true);
    }

    public void updateChamber(TunnelNetwork.TunnelNode node, Colony colony) {
        this.currentNode = node;
        this.currentColony = colony;

        if (node == null) {
            setNoChamberSelectedState();
            return;
        }

        titleLabel.setText("🏛️ Chambre : " + formatChamberTypeName(node.type()));

        String speciesName = (colony != null && colony.getSpecies() != null) ? colony.getSpecies().getScientificName() : "Formicidae sp.";
        String colonyIdStr = (colony != null && colony.getId() != null) ? colony.getId().toString().substring(0, Math.min(8, colony.getId().toString().length())) : "N/A";
        lblSpeciesColony.setText(String.format("🧬 %s | 🏛️ Colonie #%s", speciesName, colonyIdStr));

        // Capacity and occupant count
        int occupants = 0;
        int queens = 0, workers = 0, soldiers = 0, males = 0, brood = 0;
        if (colony != null) {
            for (Individual ind : colony.getLivingIndividuals()) {
                double dist = Math.hypot(ind.getX() - node.x(), Math.hypot(ind.getY() - node.y(), ind.getZ() - node.z()));
                if (dist <= Math.max(node.radiusX(), node.radiusZ()) * 1.8) {
                    occupants++;
                    if (ind.getLifeStage() != Individual.LifeStage.ADULT) {
                        brood++;
                    } else if (ind.getCaste() == Individual.Caste.QUEEN) {
                        queens++;
                    } else if (ind.getCaste() == Individual.Caste.SOLDIER) {
                        soldiers++;
                    } else if (ind.getCaste() == Individual.Caste.MALE) {
                        males++;
                    } else {
                        workers++;
                    }
                }
            }
        }

        lblOccupantsText.setText(String.format(Locale.US, "👥 Occupants : %d (👑%d | ⚒️%d | ⚔️%d | 🥚%d)", occupants, queens, workers, soldiers, brood));
        occupancyBar.setProgress(Math.min(1.0, occupants / 40.0));

        float foodStored = (colony != null) ? colony.getFoodStored() : 0.0f;
        lblResourcesRadius.setText(String.format(Locale.US, "📦 Nourriture Colonie: %.1f | Rayon: %.2f m", foodStored, node.radiusX()));
        lblPositionDepth.setText(String.format(Locale.US, "📐 Pos: (%.1fm, %.1fm) | Profondeur: -%.2f m", node.x(), node.y(), -node.z()));

        // Chamber specialty biological cargo
        String specialtyText = switch (node.type()) {
            case FUNGUS_GARDEN -> String.format(Locale.US, "🍄 Jardin: %.1fg mycélium | 🍃 %.1fg mulch", Math.max(10.0f, occupants * 8.5f), Math.max(5.0f, occupants * 4.2f));
            case FOOD_STORAGE -> String.format(Locale.US, "🌾 Grenier: %.1f graines & sucres stockés", foodStored);
            case BROOD_CHAMBER -> String.format(Locale.US, "🥚 Couvain: %d unités | 🌡️ Taux survie: 98%%", brood);
            case QUEEN_CHAMBER -> String.format(Locale.US, "👑 Chambre Royale: %d reine(s) | Phéromone Q forte", queens);
            case WASTE_DUMP -> String.format(Locale.US, "🗑️ Dépotoir: Déchets confinés | Risque pathogène");
            case VENTILATION_CHIMNEY -> "🌪️ Cheminée: Tirage convectif thermo-régulé";
            case ENTRANCE -> "🚪 Vestibule: Contrôle d'accès & phéromone de garde";
            case HIBERNATION -> "❄️ Diapause: Ralentissement métabolique hivernal";
            default -> "🏛️ Chambre standard polyvalente";
        };
        lblSpecialty.setText(specialtyText);

        // Atmospheric microclimate & gas concentration calculations
        float co2Ppm = 420.0f;
        float o2Pct = 20.95f;
        float tempC = 20.0f;
        float humPct = 65.0f;

        if (colony != null && colony.getTerrarium() != null) {
            int tw = Math.max(1, colony.getTerrarium().getWidth());
            int th = Math.max(1, colony.getTerrarium().getHeight());
            int gx = (int) Math.max(0, Math.min(127, (node.x() / (double) tw) * 128));
            int gy = (int) Math.max(0, Math.min(127, (node.y() / (double) th) * 128));
            int gz = (int) Math.max(0, Math.min(31, (-node.z() / 3.0) * 31));
            var cell = colony.getTerrarium().getCell(gx, gy, gz);
            if (cell != null) {
                if (cell.co2() > 0) co2Ppm = cell.co2();
                if (cell.o2() > 0) o2Pct = cell.o2();
                if (cell.temperature() != 0) tempC = cell.temperature();
                if (cell.humidity() > 0) humPct = cell.humidity() <= 1.0f ? cell.humidity() * 100.0f : cell.humidity();
            }
        }

        // Subterranean depth accumulation & biological respiration
        double depthM = Math.abs(node.z());
        co2Ppm += (float) (depthM * 140.0 + occupants * 50.0);
        float airFlowSpeed = 0.08f;
        int hygieneScore = 98;
        if (node.type() == TunnelNetwork.ChamberType.FUNGUS_GARDEN) {
            co2Ppm += 750.0f; // High fungus respiration
            humPct = Math.max(humPct, 88.0f);
            airFlowSpeed = 0.14f;
            hygieneScore = 95;
        } else if (node.type() == TunnelNetwork.ChamberType.BROOD_CHAMBER) {
            humPct = Math.max(humPct, 80.0f);
            hygieneScore = 99; // intensive nursing hygiene
        } else if (node.type() == TunnelNetwork.ChamberType.VENTILATION_CHIMNEY) {
            co2Ppm = Math.max(420.0f, co2Ppm * 0.45f); // High convective ventilation
            airFlowSpeed = 0.35f;
        } else if (node.type() == TunnelNetwork.ChamberType.WASTE_DUMP) {
            hygieneScore = 45; // contaminated waste area
        }

        o2Pct = Math.max(12.0f, 20.95f - (co2Ppm - 400.0f) * 0.0006f);
        float co2Pct = co2Ppm / 10000.0f;

        boolean isDark = org.swarmforge.client.util.ThemeManager.getInstance().isDarkMode();

        // Dynamic Color-coding for Humidity: Green (55-90%), Orange (35-54% or 91-95%), Red (<35% or >95%)
        String humStatus;
        String humColor;
        if (humPct >= 55.0f && humPct <= 90.0f) {
            humStatus = "🟢 Idéale";
            humColor = isDark ? "#4ade80" : "#16a34a";
        } else if ((humPct >= 35.0f && humPct < 55.0f) || (humPct > 90.0f && humPct <= 95.0f)) {
            humStatus = humPct < 55.0f ? "🟠 Sèche" : "🟠 Saturée";
            humColor = isDark ? "#f59e0b" : "#d97706";
        } else {
            humStatus = humPct < 35.0f ? "🔴 Dessiccation critique" : "🔴 Inondation / Saturation";
            humColor = isDark ? "#ef4444" : "#dc2626";
        }

        lblClimate.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + humColor + ";");
        lblClimate.setText(String.format(Locale.US, "🌡️ Temp: %.1f°C | 💧 Humidité: %.0f%% (%s)", tempC, humPct, humStatus));
        lblAtmosphere.setText(String.format(Locale.US, "💨 CO₂: %.0f ppm (%.3f%%) | O₂: %.1f%%", co2Ppm, co2Pct, o2Pct));

        String hygBadge = hygieneScore >= 80 ? "🟢 Saine" : (hygieneScore >= 60 ? "🟠 Modérée" : "🔴 Pathogène");
        lblVentilationHygiene.setText(String.format(Locale.US, "🌬️ Aération: %.2f m/s | 🛡️ Salubrité: %d%% (%s)", airFlowSpeed, hygieneScore, hygBadge));

        // Dynamic Color-coding: Green / Orange / Red according to biological gas thresholds
        if (co2Ppm < 1500.0f && o2Pct >= 19.5f) {
            // GREEN: Optimal air quality
            String greenColor = isDark ? "#4ade80" : "#16a34a";
            lblAtmosphere.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + greenColor + ";");
            lblStability.setText("🟢 Qualité d'air optimale & Structure stable");
            lblStability.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + greenColor + ";");
        } else if (co2Ppm < 5000.0f && o2Pct >= 18.0f) {
            // ORANGE: Elevated CO2, active ventilation required
            String orangeColor = isDark ? "#f59e0b" : "#d97706";
            lblAtmosphere.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + orangeColor + ";");
            lblStability.setText(String.format(Locale.US, "🟠 CO₂ Élevé (%.0f ppm) - Ventilation active requise", co2Ppm));
            lblStability.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + orangeColor + ";");
        } else {
            // RED: Critical Hypoxia / Toxic CO2 level
            String redColor = isDark ? "#ef4444" : "#dc2626";
            lblAtmosphere.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + redColor + ";");
            lblStability.setText(String.format(Locale.US, "🔴 Hypoxie Critique (CO₂ %.2f%%) - Danger asphyxie", co2Pct));
            lblStability.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + redColor + ";");
        }

        lblArchitecture.setText("🏛️ Architecture : " + node.type().name());
        double cavityVolume = (4.0 / 3.0) * Math.PI * node.radiusX() * node.radiusY() * node.radiusZ();
        lblDimensions.setText(String.format(Locale.US, "📏 Cavité: Rx=%.2fm, Ry=%.2fm, Rz=%.2fm (~%.2f m³)", node.radiusX(), node.radiusY(), node.radiusZ(), cavityVolume));

        float baroPressure = (float) (1013.25 + (depthM * 0.12));
        String lightRegime = node.z() >= -0.2f ? "☀️ Crépusculaire (50 lux)" : "🌑 Aphotique (0 lux)";
        lblLightPressure.setText(String.format(Locale.US, "%s | 🌪️ Pression: %.1f hPa", lightRegime, baroPressure));

        btnCenter.setDisable(false);
    }

    private String formatChamberTypeName(TunnelNetwork.ChamberType type) {
        if (type == null) return "Standard";
        return switch (type) {
            case QUEEN_CHAMBER -> "Chambre Royale";
            case BROOD_CHAMBER -> "Chambre de Couvain";
            case FOOD_STORAGE -> "Grenier / Stockage";
            case FUNGUS_GARDEN -> "Jardin Champignonniste";
            case HIBERNATION -> "Chambre d'Hivernage";
            case WASTE_DUMP -> "Dépotoir";
            case ENTRANCE -> "Entrée / Vestibule";
            case VENTILATION_CHIMNEY -> "Cheminée d'Aération";
            case SOLARIUM -> "Solarium Épigé";
            case BIVOUAC_CORE -> "Coeur du Bivouac";
            case HONEYCOMB -> "Rayons de Miel";
            case POLLEN_POT -> "Pots de Pollen";
            case LEAF_CACHE -> "Cache de Feuilles";
            case GALL_NURSERY -> "Galle Arboricole";
            case TUNNEL -> "Galerie / Conduit";
            default -> type.name();
        };
    }

    public void setOnCenter(Runnable handler) {
        this.onCenterHandler = handler;
    }

    public void setOnClose(Runnable handler) {
        this.onCloseHandler = handler;
    }

    public TunnelNetwork.TunnelNode getCurrentNode() {
        return currentNode;
    }

    public Colony getCurrentColony() {
        return currentColony;
    }
}
