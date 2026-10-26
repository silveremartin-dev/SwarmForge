/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.net;

import com.google.gson.JsonObject;

/**
 * Sealed hierarchy of typed client commands received by {@link SwarmForgeWebSocketServer}.
 * Implements modern Java 21+ records and exhaustive pattern matching.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public sealed interface ServerWebSocketCommand permits
        ServerWebSocketCommand.SubscribeCommand,
        ServerWebSocketCommand.ListScenariosCommand,
        ServerWebSocketCommand.ExportScenarioCommand,
        ServerWebSocketCommand.SaveScenarioCommand,
        ServerWebSocketCommand.DeployScenarioCommand,
        ServerWebSocketCommand.JoinSessionCommand,
        ServerWebSocketCommand.PlayerReadyCommand,
        ServerWebSocketCommand.StartMatchCommand,
        ServerWebSocketCommand.SelectScenarioCommand,
        ServerWebSocketCommand.ControlCommand,
        ServerWebSocketCommand.GodModeInterventionCommand,
        ServerWebSocketCommand.ScheduleEventCommand,
        ServerWebSocketCommand.UnknownCommand {

    record SubscribeCommand(String simulationId, String participantTag, String species, String role) implements ServerWebSocketCommand {}
    record ListScenariosCommand() implements ServerWebSocketCommand {}
    record ExportScenarioCommand(String scenarioId) implements ServerWebSocketCommand {}
    record SaveScenarioCommand(JsonObject scenario) implements ServerWebSocketCommand {}
    record DeployScenarioCommand(String simulationId, JsonObject scenario) implements ServerWebSocketCommand {}
    record JoinSessionCommand(String simulationId, String participantTag, String species, String role) implements ServerWebSocketCommand {}
    record PlayerReadyCommand(boolean ready) implements ServerWebSocketCommand {}
    record StartMatchCommand() implements ServerWebSocketCommand {}
    record SelectScenarioCommand(String scenarioId) implements ServerWebSocketCommand {}
    record ControlCommand(String action, double speed) implements ServerWebSocketCommand {}
    record GodModeInterventionCommand(String category, String type, JsonObject fullObject) implements ServerWebSocketCommand {}
    record ScheduleEventCommand(long targetTick, String description, JsonObject fullObject) implements ServerWebSocketCommand {}
    record UnknownCommand(String rawType, JsonObject payload) implements ServerWebSocketCommand {}

    static ServerWebSocketCommand parse(JsonObject json) {
        if (json == null) return new UnknownCommand("EMPTY", new JsonObject());
        String type = json.has("type") ? json.get("type").getAsString().toUpperCase() : "UNKNOWN";
        return switch (type) {
            case "SUBSCRIBE", "INIT_SESSION" -> {
                String simId = json.has("simulationId") ? json.get("simulationId").getAsString() : "main";
                String tag = json.has("participantTag") ? json.get("participantTag").getAsString() : "Joueur";
                String species = json.has("species") ? json.get("species").getAsString() : "Formica fusca";
                String role = json.has("role") ? json.get("role").getAsString() : "JOIN";
                yield new SubscribeCommand(simId, tag, species, role);
            }
            case "LIST_SERVER_SCENARIOS", "GET_SCENARIOS" -> new ListScenariosCommand();
            case "GET_SCENARIO", "EXPORT_SCENARIO" -> {
                String scId = json.has("scenarioId") ? json.get("scenarioId").getAsString() : "";
                yield new ExportScenarioCommand(scId);
            }
            case "SAVE_SCENARIO", "CREATE_SCENARIO" -> {
                JsonObject scObj = json.has("scenario") ? json.getAsJsonObject("scenario") : json;
                yield new SaveScenarioCommand(scObj);
            }
            case "DEPLOY_SCENARIO" -> {
                String simId = json.has("simulationId") ? json.get("simulationId").getAsString() : "main";
                JsonObject scObj = json.has("scenario") ? json.getAsJsonObject("scenario") : json;
                yield new DeployScenarioCommand(simId, scObj);
            }
            case "JOIN_SESSION" -> {
                String simId = json.has("simulationId") ? json.get("simulationId").getAsString() : "main";
                String tag = json.has("participantTag") ? json.get("participantTag").getAsString() : "Joueur";
                String species = json.has("species") ? json.get("species").getAsString() : "Formica fusca";
                String role = json.has("role") ? json.get("role").getAsString() : "JOIN";
                yield new JoinSessionCommand(simId, tag, species, role);
            }
            case "PLAYER_READY" -> {
                boolean ready = !json.has("ready") || json.get("ready").getAsBoolean();
                yield new PlayerReadyCommand(ready);
            }
            case "START_MATCH" -> new StartMatchCommand();
            case "SELECT_SCENARIO" -> {
                String scId = json.has("scenarioId") ? json.get("scenarioId").getAsString() : "";
                yield new SelectScenarioCommand(scId);
            }
            case "CONTROL_COMMAND", "CONTROL" -> {
                String action = json.has("action") ? json.get("action").getAsString().toUpperCase() : "";
                double spd = json.has("value") ? json.get("value").getAsDouble() : (json.has("speed") ? json.get("speed").getAsDouble() : 1.0);
                yield new ControlCommand(action, spd);
            }
            case "GOD_MODE_INTERVENTION" -> {
                JsonObject intervention = json.has("intervention") ? json.getAsJsonObject("intervention") : json;
                String cat = intervention.has("category") ? intervention.get("category").getAsString() : "";
                String t = intervention.has("type") ? intervention.get("type").getAsString() : "";
                yield new GodModeInterventionCommand(cat, t, intervention);
            }
            case "SCHEDULE_EVENT" -> {
                JsonObject evt = json.has("scheduledEvent") ? json.getAsJsonObject("scheduledEvent") : json;
                long tick = evt.has("targetTick") ? evt.get("targetTick").getAsLong() : 0;
                String desc = evt.has("description") ? evt.get("description").getAsString() : "Événement planifié";
                yield new ScheduleEventCommand(tick, desc, evt);
            }
            default -> new UnknownCommand(type, json);
        };
    }
}
