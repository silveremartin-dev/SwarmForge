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

    private float terrainSideMeters = 10.0f; // Default terrain side length in meters
    private int gridWidth = 64; // Default 3D scene grid dimension

    public void setTerrainDimensions(float terrainSideMeters, int gridWidth) {
        if (terrainSideMeters > 0) this.terrainSideMeters = terrainSideMeters;
        if (gridWidth > 0) this.gridWidth = gridWidth;
    }

    private Geometry createNodeGeometry(TunnelNode node) {
        float mmPerWorldUnit = (terrainSideMeters * 1000.0f) / Math.max(1, gridWidth);
        // Biological chamber sizes (Queen: ~60-80mm, Brood: ~40-50mm, Entrance: ~30-35mm)
        float radiusMm = switch (node.type()) {
            case QUEEN_CHAMBER -> 65.0f;
            case FUNGUS_GARDEN -> 55.0f;
            case HIBERNATION -> 50.0f;
            case BROOD_CHAMBER, FOOD_STORAGE -> 40.0f;
            case ENTRANCE -> 30.0f;
            default -> 20.0f;
        };

        // If node has explicit dimensions from lenticular model
        float baseRadius3D = (node.radius() > 0 && node.radius() < 5.0f)
                ? node.radius() * 0.45f
                : (radiusMm / mmPerWorldUnit) * 1.3f;
        float radius3D = Math.max(0.12f, baseRadius3D);

        Sphere shape = new Sphere(8, 8, radius3D);
        Geometry geom = new Geometry("Node_" + node.id(), shape);
        geom.setLocalTranslation(node.x(), node.z(), node.y());

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = getNodeColor(node.type());
        mat.setColor("Diffuse", col);
        mat.setColor("Ambient", col.mult(0.6f));
        geom.setMaterial(mat);

        return geom;
    }

    private float customGalleryDiameterMm = -1.0f;

    public void setCustomGalleryDiameterMm(float customGalleryDiameterMm) {
        this.customGalleryDiameterMm = customGalleryDiameterMm;
    }

    private Geometry createEdgeGeometry(TunnelNode n1, TunnelNode n2, String name) {
        Vector3f p1 = new Vector3f(n1.x(), n1.z(), n1.y());
        Vector3f p2 = new Vector3f(n2.x(), n2.z(), n2.y());
        Vector3f diff = p2.subtract(p1);
        float len = diff.length();

        float mmPerWorldUnit = (terrainSideMeters * 1000.0f) / Math.max(1, gridWidth);
        float galleryRadiusMm = customGalleryDiameterMm > 0 ? customGalleryDiameterMm / 2.0f : 10.0f;
        float galleryRadius3D = Math.max(0.04f, (galleryRadiusMm / mmPerWorldUnit) * 1.25f);

        // Cylinder aligned Z
        Cylinder shape = new Cylinder(4, 8, galleryRadius3D, len, true);
        Geometry geom = new Geometry(name, shape);

        // Position at midpoint
        geom.setLocalTranslation(p1.add(diff.mult(0.5f)));

        // Rotate to match direction
        geom.lookAt(p2, Vector3f.UNIT_Y);

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = new ColorRGBA(0.48f, 0.32f, 0.20f, 1.0f);
        mat.setColor("Diffuse", col);
        mat.setColor("Ambient", col.mult(0.5f));
        geom.setMaterial(mat);

        return geom;
    }

    private ColorRGBA getNodeColor(TunnelNetwork.ChamberType type) {
        return switch (type) {
            case QUEEN_CHAMBER -> ColorRGBA.Magenta;
            case BROOD_CHAMBER -> ColorRGBA.White; // Eggs/Larvae
            case FOOD_STORAGE -> ColorRGBA.Green;
            case FUNGUS_GARDEN -> new ColorRGBA(0.66f, 0.33f, 0.97f, 1.0f); // Purple
            case HIBERNATION -> new ColorRGBA(0.22f, 0.74f, 0.97f, 1.0f); // Cyan
            case WASTE_DUMP -> ColorRGBA.DarkGray;
            case ENTRANCE -> ColorRGBA.Yellow;
            default -> ColorRGBA.Brown;
        };
    }
}
