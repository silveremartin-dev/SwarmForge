package org.swarmforge.server.net;

import com.google.protobuf.util.JsonFormat;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.protocol.grpc.IndividualDelta;
import org.swarmforge.protocol.grpc.SimulationUpdate;
import org.swarmforge.protocol.grpc.Vec3;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Server for streaming simulation state to web clients.
 * Uses JSON format for easier browser consumption in MVP.
 */
public class SwarmForgeWebSocketServer extends WebSocketServer {

    private static final Logger LOG = LoggerFactory.getLogger(SwarmForgeWebSocketServer.class);
    private final org.swarmforge.server.simulation.SimulationManager simulationManager;
    private final Map<WebSocket, String> clientSubscriptions = new ConcurrentHashMap<>();
    private final Map<WebSocket, java.util.concurrent.atomic.AtomicInteger> clientMessageCounters = new ConcurrentHashMap<>();
    private final Map<WebSocket, Long> clientWindowTimestamps = new ConcurrentHashMap<>();

    public SwarmForgeWebSocketServer(int port, org.swarmforge.server.simulation.SimulationManager simulationManager) {
        super(new InetSocketAddress(port));
        this.simulationManager = simulationManager;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOG.info("New WebSocket connection: " + conn.getRemoteSocketAddress());
        // Default subscription to main
        clientSubscriptions.put(conn, "main");
        clientMessageCounters.put(conn, new java.util.concurrent.atomic.AtomicInteger(0));
        clientWindowTimestamps.put(conn, System.currentTimeMillis());
        conn.send("{\"type\": \"WELCOME\", \"message\": \"Connected to SwarmForge Server\"}");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOG.info("Closed WebSocket connection: " + conn.getRemoteSocketAddress());
        clientSubscriptions.remove(conn);
        clientMessageCounters.remove(conn);
        clientWindowTimestamps.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message == null || message.length() > 65536) {
            conn.close(1009, "Payload too large (>64KB)");
            return;
        }

        // Rate Limiter: Max 100 messages per 1-second sliding window per connection
        long now = System.currentTimeMillis();
        long windowStart = clientWindowTimestamps.computeIfAbsent(conn, k -> now);
        if (now - windowStart > 1000) {
            clientWindowTimestamps.put(conn, now);
            clientMessageCounters.computeIfAbsent(conn, k -> new java.util.concurrent.atomic.AtomicInteger(0)).set(0);
        }
        var counter = clientMessageCounters.computeIfAbsent(conn, k -> new java.util.concurrent.atomic.AtomicInteger(0));
        if (counter.incrementAndGet() > 100) {
            conn.close(1008, "Rate limit exceeded (Max 100 msg/sec)");
            return;
        }
        try {
            if (message.contains("SUBSCRIBE")) {
                int idx = message.indexOf("\"simulationId\":");
                if (idx != -1) {
                    int start = message.indexOf("\"", idx + 15);
                    int end = (start != -1) ? message.indexOf("\"", start + 1) : -1;
                    if (start != -1 && end != -1) {
                        String simId = message.substring(start + 1, end).trim();
                        if (!simId.isEmpty() && simId.length() <= 64) {
                            clientSubscriptions.put(conn, simId);
                            return;
                        }
                    }
                }
                clientSubscriptions.put(conn, "main");
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse WebSocket message from {}: {}", conn.getRemoteSocketAddress(), e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOG.error("WebSocket error", ex);
    }

    @Override
    public void onStart() {
        LOG.info("WebSocket Server started on port: " + getPort());
        startBroadcaster();
    }

    private void startBroadcaster() {
        Thread.ofVirtual().name("ws-broadcaster").start(() -> {
            JsonFormat.Printer printer = JsonFormat.printer().includingDefaultValueFields();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    broadcastUpdates(printer);
                    Thread.sleep(50); // 20 FPS
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    LOG.error("Broadcast error", e);
                }
            }
        });
    }

    private void broadcastUpdates(JsonFormat.Printer printer) {
        var connections = getConnections();
        if (connections.isEmpty())
            return;

        // Cache serialized JSON per simulation ID to avoid redundant formatting
        Map<String, String> cachedJsonPerSim = new HashMap<>();

        for (WebSocket conn : connections) {
            String simId = clientSubscriptions.getOrDefault(conn, "main");

            String json = cachedJsonPerSim.computeIfAbsent(simId, id -> {
                Simulation sim = simulationManager.getSimulation(id).orElse(null);
                if (sim != null && sim.getState() == Simulation.State.RUNNING) {
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

                        return printer.print(update.build());
                    } catch (Exception e) {
                        LOG.warn("Error serializing simulation update for " + id + ": " + e.getMessage());
                        return null;
                    }
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
}
