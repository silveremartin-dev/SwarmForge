package org.swarmforge.server.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.util.JsonFormat;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.protocol.grpc.IndividualDelta;
import org.swarmforge.protocol.grpc.SimulationUpdate;
import org.swarmforge.protocol.grpc.Vec3;
import org.swarmforge.server.simulation.SimulationManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles high-throughput periodic serialization and streaming of simulation frames
 * (individuals, nests, food, environment, predators, terrain) over WebSockets using Virtual Threads.
 */
public final class WebSocketTelemetryBroadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketTelemetryBroadcaster.class);

    private WebSocketTelemetryBroadcaster() {
        // Utility class
    }

    public static void startBroadcaster(SwarmForgeWebSocketServer server, SimulationManager simulationManager,
                                        Map<WebSocket, String> clientSubscriptions) {
        Thread.ofVirtual().name("ws-broadcaster").start(() -> {
            JsonFormat.Printer printer = JsonFormat.printer().includingDefaultValueFields();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    broadcastUpdates(server.getConnections(), simulationManager, clientSubscriptions, printer);
                    Thread.sleep(50); // 20 FPS
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    LOG.error("Broadcast error", e);
                }
            }
        });
    }

    public static void broadcastUpdates(Collection<WebSocket> connections, SimulationManager simulationManager,
                                        Map<WebSocket, String> clientSubscriptions, JsonFormat.Printer printer) {
        if (connections.isEmpty()) {
            return;
        }

        // Cache serialized JSON per simulation ID to avoid redundant formatting
        Map<String, String> cachedJsonPerSim = new HashMap<>();

        for (WebSocket conn : connections) {
            String simId = clientSubscriptions.getOrDefault(conn, "main");

            String json = cachedJsonPerSim.computeIfAbsent(simId, id -> {
                Simulation sim = simulationManager.getSimulation(id).orElse(null);
                if (sim != null && sim.getState() == Simulation.State.RUNNING) {
                    return serializeSimulationFrame(sim, printer, id);
                }
                return null;
            });

            if (json != null && conn.isOpen()) {
                try {
                    conn.send(json);
                } catch (Exception e) {
                    // ignore connection drop
                }
            }
        }
    }

    private static String serializeSimulationFrame(Simulation sim, JsonFormat.Printer printer, String simId) {
        try {
            SimulationUpdate.Builder update = SimulationUpdate.newBuilder()
                    .setTick(sim.getTickCount());

            int count = 0;
            for (Colony colony : sim.getColonies()) {
                if (count < 1000) {
                    for (Individual ind : colony.getLivingIndividuals()) {
                        if (count++ >= 1000)
                            break;

                        update.addIndividuals(IndividualDelta.newBuilder()
                                .setId(ind.getId().toString())
                                .setPosition(Vec3.newBuilder()
                                        .setX(ind.getX())
                                        .setY(ind.getY())
                                        .setZ(ind.getZ())
                                        .build())
                                .setHeading(ind.getHeading())
                                .setAlive(ind.isAlive())
                                .setHealth((float) ind.getHealth())
                                .setEnergy((float) ind.getEnergy())
                                .setJob(ind.getJob() != null ? ind.getJob().name() : (ind.getCaste() != null ? ind.getCaste().name() : "WORKER"))
                                .setCurrentAction(ind.getCaste() != null ? ind.getCaste().name() : "WORKER")
                                .build());
                    }
                }

                // Add Nest Structure
                org.swarmforge.core.structure.Nest nest = colony.getNest();
                org.swarmforge.protocol.grpc.NestStructure.Builder nestBuilder = org.swarmforge.protocol.grpc.NestStructure
                        .newBuilder()
                        .setId(colony.getId().toString());

                for (org.swarmforge.core.structure.Chamber chamber : nest.getChambers()) {
                    nestBuilder.addChambers(org.swarmforge.protocol.grpc.ChamberInfo.newBuilder()
                            .setId(chamber.getId())
                            .setType(chamber.getType().name())
                            .setPosition(Vec3.newBuilder().setX(chamber.getX()).setY(chamber.getY())
                                    .setZ(chamber.getZ()).build())
                            .setCapacity(chamber.getCapacity())
                            .setCurrentLoading(chamber.getCurrentLoad())
                            .build());
                }

                for (org.swarmforge.core.structure.Tunnel tunnel : nest.getTunnels()) {
                    nestBuilder.addTunnels(org.swarmforge.protocol.grpc.TunnelInfo.newBuilder()
                            .setStartChamberId(tunnel.getStart().getId())
                            .setEndChamberId(tunnel.getEnd().getId())
                            .setLength(tunnel.getLength())
                            .build());
                }

                update.addNests(nestBuilder);
            }

            // Add Food Sources
            for (org.swarmforge.core.domain.FoodSource food : sim.getFoodSources()) {
                if (!food.isDepleted()) {
                    update.addFood(org.swarmforge.protocol.grpc.FoodSourceInfo.newBuilder()
                            .setPosition(Vec3.newBuilder().setX(food.getX()).setY(food.getY()).setZ(food.getZ()).build())
                            .setQuantity(food.getQuantity())
                            .setType(food.getType() != null ? food.getType().name() : "SUGAR")
                            .build());
                }
            }

            // Environment
            org.swarmforge.core.world.DayNightCycle cycle = sim.getDayNightCycle();
            org.swarmforge.core.world.WeatherSystem weather = sim.getWeather();
            org.swarmforge.core.world.SeasonManager seasons = sim.getSeasonManager();

            update.setEnvironment(org.swarmforge.protocol.grpc.Environment.newBuilder()
                    .setLightLevel(cycle.getLightLevel())
                    .setTimeOfDay(cycle.getTimeOfDay().name())
                    .setSunAngle(cycle.getSunAngle())
                    .setTemperature(weather.getTemperature())
                    .setHumidity(weather.getHumidity())
                    .setRainIntensity(weather.getRainfall())
                    .setWindSpeed(weather.getWindSpeed())
                    .setSeason(seasons.getCurrentSeason().name())
                    .build());

            String protoJson = printer.print(update.build());
            JsonObject fullUpdate = JsonParser.parseString(protoJson).getAsJsonObject();

            // Add live predators
            JsonArray predArray = new JsonArray();
            if (sim.getPredatorManager() != null) {
                for (org.swarmforge.core.domain.Predator pred : sim.getPredatorManager().getPredators()) {
                    if (pred.isAlive()) {
                        JsonObject pObj = new JsonObject();
                        pObj.addProperty("id", pred.getId().toString());
                        pObj.addProperty("type", pred.getType() != null ? pred.getType().name() : "SPIDER");
                        pObj.addProperty("x", pred.getX());
                        pObj.addProperty("y", pred.getY());
                        pObj.addProperty("z", pred.getZ());
                        pObj.addProperty("state", pred.getState() != null ? pred.getState().name() : "HUNTING");
                        pObj.addProperty("health", pred.getHealth());
                        predArray.add(pObj);
                    }
                }
            }
            fullUpdate.add("predators", predArray);

            // Add terrain dimensions
            JsonObject terrainObj = new JsonObject();
            terrainObj.addProperty("width", sim.getTerrarium().getWidth());
            terrainObj.addProperty("height", sim.getTerrarium().getHeight());
            terrainObj.addProperty("depth", sim.getTerrarium().getDepth());
            fullUpdate.add("terrain", terrainObj);

            return fullUpdate.toString();
        } catch (Exception e) {
            LOG.warn("Error serializing simulation update for " + simId + ": " + e.getMessage());
            return null;
        }
    }
}
