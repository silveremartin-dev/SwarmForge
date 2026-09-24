/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult;
import org.swarmforge.core.simulation.SimulationContext;

import java.io.File;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Declarative JSON-driven / .sfbrain Behavior Tree & FSM Reasoning Architecture.
 * Allows computational biologists to configure and exchange complex decision hierarchies
 * without compiling Java code.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class JsonBrainArchitecture implements ReasoningArchitecture, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(JsonBrainArchitecture.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public enum NodeType {
        SELECTOR,
        SEQUENCE,
        CONDITION,
        ACTION
    }

    public static final class Node implements Serializable {
        private static final long serialVersionUID = 1L;

        private final NodeType type;
        private final String check;
        private final Action.ActionType action;
        private final double parameter;
        private final List<Node> children;

        public Node(NodeType type, String check, Action.ActionType action, double parameter, List<Node> children) {
            this.type = type != null ? type : NodeType.ACTION;
            this.check = check != null ? check.toUpperCase().trim() : "";
            this.action = action != null ? action : Action.ActionType.REST;
            this.parameter = parameter;
            this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
        }

        public NodeType getType() { return type; }
        public String getCheck() { return check; }
        public Action.ActionType getAction() { return action; }
        public double getParameter() { return parameter; }
        public List<Node> getChildren() { return children; }
    }

    private final String brainId;
    private final String name;
    private final String description;
    private final Node root;

    public JsonBrainArchitecture(String brainId, String name, String description, Node root) {
        this.brainId = brainId != null ? brainId : "custom_json_brain";
        this.name = name != null ? name : "Declarative Brain";
        this.description = description != null ? description : "";
        this.root = root != null ? root : new Node(NodeType.ACTION, "", Action.ActionType.FORAGE, 1.0, null);
    }

    public static JsonBrainArchitecture fromFile(File file) throws Exception {
        JsonNode node = MAPPER.readTree(file);
        return parseJsonObject(node, file.getName());
    }

    public static JsonBrainArchitecture fromJsonString(String json, String defaultId) throws Exception {
        JsonNode node = MAPPER.readTree(json);
        return parseJsonObject(node, defaultId);
    }

    public static JsonBrainArchitecture fromReader(Reader reader, String defaultId) throws Exception {
        JsonNode node = MAPPER.readTree(reader);
        return parseJsonObject(node, defaultId);
    }

    private static JsonBrainArchitecture parseJsonObject(JsonNode obj, String defaultId) {
        String id = obj.has("id") ? obj.get("id").asText() : (defaultId != null ? defaultId : "json_brain");
        String name = obj.has("name") ? obj.get("name").asText() : id;
        String desc = obj.has("description") ? obj.get("description").asText() : "";
        Node root = null;
        if (obj.has("root")) {
            root = parseNode(obj.get("root"));
        } else if (obj.has("nodes")) {
            root = parseNode(obj.get("nodes"));
        }
        return new JsonBrainArchitecture(id, name, desc, root);
    }

    private static Node parseNode(JsonNode nodeObj) {
        if (nodeObj == null || !nodeObj.isObject()) return new Node(NodeType.ACTION, "", Action.ActionType.FORAGE, 1.0, null);

        String typeStr = nodeObj.has("type") ? nodeObj.get("type").asText().toUpperCase() : "ACTION";
        NodeType nodeType;
        try {
            nodeType = NodeType.valueOf(typeStr);
        } catch (Exception e) {
            nodeType = NodeType.ACTION;
        }

        String check = nodeObj.has("check") ? nodeObj.get("check").asText() : (nodeObj.has("condition") ? nodeObj.get("condition").asText() : "");
        String actStr = nodeObj.has("action") ? nodeObj.get("action").asText().toUpperCase() : "EXPLORE";
        Action.ActionType actType;
        try {
            actType = Action.ActionType.valueOf(actStr);
        } catch (Exception e) {
            actType = Action.ActionType.EXPLORE;
        }

        double param = nodeObj.has("parameter") ? nodeObj.get("parameter").asDouble() : 1.0;
        List<Node> children = new ArrayList<>();
        if (nodeObj.has("children") && nodeObj.get("children").isArray()) {
            for (JsonNode el : nodeObj.get("children")) {
                if (el.isObject()) {
                    children.add(parseNode(el));
                }
            }
        }

        return new Node(nodeType, check, actType, param, children);
    }

    @Override
    public ArchitectureType getType() {
        return ArchitectureType.BEHAVIOR_TREE;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getBrainId() {
        return brainId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void initialize(AgentView agent) {
        // Stateless declarative evaluation
    }

    @Override
    public Action decide(AgentView agent, SimulationContext context) {
        if (agent == null) return Action.rest();
        Action evaluated = evaluateNode(root, agent, context);
        return evaluated != null ? evaluated : Action.forage();
    }

    @Override
    public void update(AgentView agent, Action executedAction, ActionResult result) {
        // No-op for standard static tree
    }

    @Override
    public void reset() {
        // No-op
    }

    @Override
    public ReasoningArchitecture clone() {
        return new JsonBrainArchitecture(brainId, name, description, root);
    }

    private Action evaluateNode(Node node, AgentView ant, SimulationContext ctx) {
        if (node == null) return null;

        switch (node.getType()) {
            case SELECTOR:
                for (Node child : node.getChildren()) {
                    Action result = evaluateNode(child, ant, ctx);
                    if (result != null) return result;
                }
                return null;

            case SEQUENCE:
                Action lastAction = null;
                for (Node child : node.getChildren()) {
                    Action result = evaluateNode(child, ant, ctx);
                    if (result == null) return null;
                    lastAction = result;
                }
                return lastAction;

            case CONDITION:
                if (checkCondition(node.getCheck(), node.getParameter(), ant, ctx)) {
                    if (!node.getChildren().isEmpty()) {
                        for (Node child : node.getChildren()) {
                            Action res = evaluateNode(child, ant, ctx);
                            if (res != null) return res;
                        }
                    }
                    return new Action(node.getAction(), 0, 0, 0, 1.0f, null);
                }
                return null;

            case ACTION:
                return new Action(node.getAction(), 0, 0, 0, (float) node.getParameter(), null);

            default:
                return null;
        }
    }

    private boolean checkCondition(String condition, double param, AgentView ant, SimulationContext ctx) {
        if (condition == null || condition.isBlank()) return true;

        return switch (condition) {
            case "ENERGY_LOW", "LOW_ENERGY" -> ant.getEnergyLevel() < (param > 0 ? (float) param : 0.3f);
            case "ENERGY_HIGH" -> ant.getEnergyLevel() >= (param > 0 ? (float) param : 0.7f);
            case "CARRYING_LOAD", "HAS_CARGO", "CARRYING_FOOD" -> ant.isCarryingFood();
            case "IS_EMPTY", "NO_CARGO" -> !ant.isCarryingFood();
            case "IS_HUNGRY" -> ant.getHunger() > (param > 0 ? (float) param : 0.5f);
            case "IS_NEAR_NEST", "AT_HOME", "IS_AT_NEST" -> ant.isAtNest();
            case "IS_SOLDIER" -> ant.isSoldier();
            case "IS_QUEEN" -> ant.isQueen();
            case "IS_NURSE" -> ant.isNurse();
            case "CAN_FLY" -> ant.canFly();
            default -> true;
        };
    }
}
