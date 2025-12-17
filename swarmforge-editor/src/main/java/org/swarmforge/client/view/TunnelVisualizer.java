package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.core.simulation.TunnelNetwork;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelEdge;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Visualizer for the underground tunnel network.
 */
public class TunnelVisualizer {

    private final AssetManager assetManager;
    private final Node rootNode;
    private final Map<java.util.UUID, Geometry> nodeGeometries = new HashMap<>();

    public TunnelVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("TunnelNetwork");
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void update(TunnelNetwork network) {
        if (network == null)
            return;

        // Simple approach: Rebuild if count changed (optimization possible)
        // For now, just check if we have new nodes
        if (network.getNodeCount() == nodeGeometries.size()) {
            return;
        }

        // Full rebuild for simplicity or add missing
        for (TunnelNode node : network.getNodes()) {
            if (!nodeGeometries.containsKey(node.id())) {
                Geometry geom = createNodeGeometry(node);
                rootNode.attachChild(geom);
                nodeGeometries.put(node.id(), geom);
            }
        }

        // Edges (draw as cylinders)
        // Ideally we cache edges too, but for now just clear and redraw edges or check
        // count
        // Let's assume edges are static once added
        // Draw edges
        for (TunnelEdge edge : network.getEdges()) {
            // Check if edge already drawn? naming convention?
            String edgeName = "Edge_" + edge.fromNode() + "_" + edge.toNode();
            if (rootNode.getChild(edgeName) == null) {
                TunnelNode n1 = findNode(network, edge.fromNode());
                TunnelNode n2 = findNode(network, edge.toNode());
                if (n1 != null && n2 != null) {
                    Geometry edgeGeom = createEdgeGeometry(n1, n2, edgeName);
                    rootNode.attachChild(edgeGeom);
                }
            }
        }
    }

    private TunnelNode findNode(TunnelNetwork network, java.util.UUID id) {
        // network.getNodes() is a list, maybe slow.
        // TunnelNetwork could expose map or getById
        // For now loop
        for (TunnelNode n : network.getNodes()) {
            if (n.id().equals(id))
                return n;
        }
        return null;
    }

    private Geometry createNodeGeometry(TunnelNode node) {
        float radius = switch (node.type()) {
            case QUEEN_CHAMBER -> 1.5f;
            case BROOD_CHAMBER, FOOD_STORAGE -> 1.0f;
            case ENTRANCE -> 0.8f;
            default -> 0.4f;
        };

        Sphere shape = new Sphere(8, 8, radius);
        Geometry geom = new Geometry("Node_" + node.id(), shape);
        geom.setLocalTranslation(node.x(), node.y(), node.z());

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", getNodeColor(node.type()));
        geom.setMaterial(mat);

        return geom;
    }

    private Geometry createEdgeGeometry(TunnelNode n1, TunnelNode n2, String name) {
        Vector3f p1 = new Vector3f(n1.x(), n1.y(), n1.z());
        Vector3f p2 = new Vector3f(n2.x(), n2.y(), n2.z());
        Vector3f diff = p2.subtract(p1);
        float len = diff.length();

        // Cylinder aligned Z
        Cylinder shape = new Cylinder(4, 8, 0.2f, len, true);
        Geometry geom = new Geometry(name, shape);

        // Position at midpoint
        geom.setLocalTranslation(p1.add(diff.mult(0.5f)));

        // Rotate to match direction
        geom.lookAt(p2, Vector3f.UNIT_Y);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Brown);
        geom.setMaterial(mat);

        return geom;
    }

    private ColorRGBA getNodeColor(TunnelNetwork.ChamberType type) {
        return switch (type) {
            case QUEEN_CHAMBER -> ColorRGBA.Magenta;
            case BROOD_CHAMBER -> ColorRGBA.White; // Eggs/Larvae
            case FOOD_STORAGE -> ColorRGBA.Green;
            case WASTE_DUMP -> ColorRGBA.DarkGray;
            case ENTRANCE -> ColorRGBA.Yellow;
            default -> ColorRGBA.Brown;
        };
    }
}
