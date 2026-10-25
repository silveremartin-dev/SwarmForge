/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import org.swarmforge.client.ui.InterventionPanel;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.PheromoneType;
import org.swarmforge.core.domain.PredatorType;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.simulation.disasters.DisasterEvent;
import org.swarmforge.core.simulation.disasters.DroughtDisaster;
import org.swarmforge.core.simulation.disasters.FireDisaster;
import org.swarmforge.core.simulation.disasters.FloodDisaster;
import org.swarmforge.core.simulation.disasters.HeatwaveDisaster;
import org.swarmforge.core.simulation.disasters.StormDisaster;

/**
 * Bridges God-Mode {@link InterventionPanel.InterventionCallback} commands to the active
 * local simulation and network client for {@link SwarmForgeClient}.
 */
class ClientInterventionBridge implements InterventionPanel.InterventionCallback {

    private final SwarmForgeClient client;

    ClientInterventionBridge(SwarmForgeClient client) {
        this.client = client;
    }

    @Override
    public void spawnAnts(String colonyId, String caste, int count, float x, float y, float z) {
        if (client.localSimulation != null) {
            Colony targetColony = null;
            for (Colony c : client.localSimulation.getColonies()) {
                String name = c.getSpeciesName();
                if (colonyId != null && (name.contains(colonyId) || colonyId.contains(name))) {
                    targetColony = c;
                    break;
                }
            }
            if (targetColony == null && !client.localSimulation.getColonies().isEmpty()) {
                targetColony = client.localSimulation.getColonies().get(0);
            }
            if (targetColony != null) {
                for (int i = 0; i < count; i++) {
                    if (caste != null && (caste.contains("Reine") || caste.contains("Queen"))) {
                        targetColony.createQueen();
                    } else if (caste != null && (caste.contains("Soldat") || caste.contains("Soldier"))) {
                        targetColony.createSoldier();
                    } else if (caste != null && (caste.contains("Mâle") || caste.contains("Male"))) {
                        targetColony.createMale();
                    } else {
                        targetColony.createWorker();
                    }
                }
            }
        }
        if (client.networkClient != null && client.networkClient.isConnected()) {
            client.networkClient.spawnFood(x, y, z, count);
        }
    }

    @Override
    public void killAnts(String colonyId, String caste, int count) {
        if (client.localSimulation != null && !client.localSimulation.getColonies().isEmpty()) {
            Colony colony = client.localSimulation.getColonies().get(0);
            int killed = 0;
            for (Individual ind : colony.getLivingIndividuals()) {
                if (killed >= count) break;
                ind.die();
                killed++;
            }
            colony.removeDeadIndividuals();
        }
    }

    @Override
    public void spawnFood(float x, float y, float z, float amount) {
        if (client.localSimulation != null) {
            client.localSimulation.spawnFood(x, y, z, amount, ResourceType.SUGAR);
        }
        if (client.networkClient != null && client.networkClient.isConnected()) {
            client.networkClient.spawnFood(x, y, z, amount);
        }
    }

    @Override
    public void triggerDisaster(String type, float intensity) {
        if (client.networkClient != null && client.networkClient.isConnected()) {
            client.networkClient.triggerDisaster(type, intensity);
        }
        if (client.localSimulation != null && client.lastGeneratedTerrarium != null) {
            DisasterEvent event;
            String upper = type != null ? type.toUpperCase() : "FIRE";
            if (upper.contains("FIRE") || upper.contains("INCENDIE")) {
                event = new FireDisaster(-1, -1, -1, intensity);
                if (client.worldEditorPane != null) {
                    client.worldEditorPane.triggerWildfire(32, 16, (int)(10 + intensity * 15));
                }
            } else if (upper.contains("FLOOD") || upper.contains("INONDATION")) {
                event = new FloodDisaster(intensity, (int)(intensity * 50));
                if (client.worldEditorPane != null) {
                    client.worldEditorPane.triggerFlood(32, 16, (int)(12 + intensity * 15));
                }
            } else if (upper.contains("DROUGHT") || upper.contains("SECHERESSE")) {
                event = new DroughtDisaster(intensity);
                if (client.worldEditorPane != null) {
                    client.worldEditorPane.triggerDrought();
                }
            } else if (upper.contains("HEAT") || upper.contains("CHALEUR")) {
                event = new HeatwaveDisaster(intensity, (int)(intensity * 100));
            } else {
                event = new StormDisaster();
            }
            client.localSimulation.triggerDisaster(event);
            if (client.simWorldViewer != null) {
                client.simWorldViewer.repaintAllViews();
            }
        }
    }

    @Override
    public void stopDisasters() {
    }

    @Override
    public void modifyParameter(String param, Object value) {
        if (client.localSimulation != null && value instanceof Number val) {
            float fVal = val.floatValue();
            if ("foodRate".equals(param)) {
                client.localSimulation.setDiffusionInterval(Math.max(1, (int) (10 * fVal)));
            } else if ("temperatureCelsius".equals(param) || "temperature".equals(param)) {
                if (client.localSimulation.getWeather() != null) client.localSimulation.getWeather().setTemperature(fVal);
            } else if ("humidityPercent".equals(param) || "humidity".equals(param)) {
                if (client.localSimulation.getWeather() != null) client.localSimulation.getWeather().setHumidity(fVal);
            } else if ("windSpeed".equals(param) || "wind".equals(param)) {
                if (client.localSimulation.getWeather() != null) client.localSimulation.getWeather().setWindSpeed(fVal);
            } else if ("solarRadiation".equals(param) || "solar".equals(param)) {
                if (client.localSimulation.getWeather() != null) client.localSimulation.getWeather().setTemperatureOffset(fVal / 100.0f - 4.5f);
            }
        }
    }

    @Override
    public void triggerInvasionEvent(String type, float x, float y, float z, int count, float durationMinutes) {
        if (client.localSimulation != null && client.localSimulation.getPredatorManager() != null) {
            boolean isSuppression = count == 0 || (type != null && (type.contains("Protection") || type.contains("Absence") || type.contains("Sanctuary")));
            if (isSuppression) {
                int durationTicks = (int) (durationMinutes * 60.0 * 60.0);
                client.localSimulation.getPredatorManager().suppressPredators(durationTicks);
            } else {
                PredatorType pType = PredatorType.AARDVARK_MOUND_BREAKER;
                if (type != null) {
                    String u = type.toUpperCase();
                    if (u.contains("ARMY") || u.contains("LEGION")) pType = PredatorType.MEGAPONERA_RAIDER;
                    else if (u.contains("MITE") || u.contains("ACARIEN")) pType = PredatorType.VARROA_MITE;
                    else if (u.contains("LOCUST") || u.contains("SAUTERELLE")) pType = PredatorType.BEETLE;
                    else if (u.contains("WASP") || u.contains("GUEPE")) pType = PredatorType.WASP;
                    else if (u.contains("SPIDER") || u.contains("ARAIGNEE")) pType = PredatorType.SPIDER;
                    else if (u.contains("MANTIS") || u.contains("MANTE")) pType = PredatorType.BEETLE;
                }
                int spawnCount = Math.max(1, count);
                for (int i = 0; i < spawnCount; i++) {
                    client.localSimulation.getPredatorManager().spawnPredator(pType, x, y, z);
                }
            }
        }
    }

    @Override
    public void triggerPheromoneEvent(String type, float x, float y, float z, float intensity, float radius, float durationMinutes) {
        if (client.localSimulation != null && client.localSimulation.getPheromoneGrid() != null) {
            int index = PheromoneType.HOME_TRAIL.getIndex();
            if (type != null) {
                String u = type.toUpperCase();
                if (u.contains("ALARM") || u.contains("ALERTE")) index = PheromoneType.ALARM.getIndex();
                else if (u.contains("FOOD") || u.contains("ATTRACTANT") || u.contains("BIOMASS")) index = PheromoneType.FOOD_TRAIL.getIndex();
                else if (u.contains("QUEEN") || u.contains("REINE")) index = PheromoneType.QUEEN_SCENT.getIndex();
                else if (u.contains("RECRUIT") || u.contains("RECRUTEMENT")) index = PheromoneType.RECRUITMENT.getIndex();
                else if (u.contains("ERASER") || u.contains("DISPERSION")) {
                    int r = Math.max(1, (int) radius);
                    for (int dx = (int)(x - r); dx <= (int)(x + r); dx++) {
                        for (int dy = (int)(y - r); dy <= (int)(y + r); dy++) {
                            for (int dz = (int)(z - r); dz <= (int)(z + r); dz++) {
                                client.localSimulation.getPheromoneGrid().clearAt(dx, dy, dz);
                            }
                        }
                    }
                    return;
                }
            }
            int r = Math.max(1, (int) radius);
            for (int dx = (int)(x - r); dx <= (int)(x + r); dx++) {
                for (int dy = (int)(y - r); dy <= (int)(y + r); dy++) {
                    double dist = Math.hypot(dx - x, dy - y);
                    if (dist <= radius) {
                        float conc = (float) (intensity * (1.0 - dist / (radius + 0.1)));
                        client.localSimulation.getPheromoneGrid().deposit(dx, dy, (int)z, index, conc);
                    }
                }
            }
        }
    }
}
