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
    private org.swarmforge.core.domain.Terrarium terrarium;

    public TunnelVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("TunnelNetwork");
    }

    public void setTerrarium(org.swarmforge.core.domain.Terrarium terrarium) {
        this.terrarium = terrarium;
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
        for (TunnelEdge edge : network.getEdges()) {
            String edgeName = "Edge_" + edge.fromNode() + "_" + edge.toNode();
            if (rootNode.getChild(edgeName) == null) {
                TunnelNode n1 = network.getNode(edge.fromNode());
                TunnelNode n2 = network.getNode(edge.toNode());
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

        // Calibrated biological chamber sizes (Queen: ~60-80mm, Brood: ~40-50mm, Entrance: ~30-35mm)
        float baseRadius3D = (radiusMm / mmPerWorldUnit) * 1.15f;
        float radius3D = Math.max(0.12f, Math.min(0.70f, baseRadius3D));

        Sphere shape = new Sphere(8, 8, radius3D);
        Geometry geom = new Geometry("Node_" + node.id(), shape);
        float surfaceY = terrarium != null ? terrarium.getSurfaceElevation(node.x(), node.y()) : 0f;
        float posY = (node.z() <= 0) ? (surfaceY + node.z()) : node.z();
        geom.setLocalTranslation(node.x(), posY, node.y());
        geom.setUserData("ChamberID", node.id().toString());
        geom.setUserData("ChamberType", node.type().name());

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
        float surfaceY1 = terrarium != null ? terrarium.getSurfaceElevation(n1.x(), n1.y()) : 0f;
        float posY1 = (n1.z() <= 0) ? (surfaceY1 + n1.z()) : n1.z();
        float surfaceY2 = terrarium != null ? terrarium.getSurfaceElevation(n2.x(), n2.y()) : 0f;
        float posY2 = (n2.z() <= 0) ? (surfaceY2 + n2.z()) : n2.z();

        Vector3f p1 = new Vector3f(n1.x(), posY1, n1.y());
        Vector3f p2 = new Vector3f(n2.x(), posY2, n2.y());
        Vector3f diff = p2.subtract(p1);
        float len = diff.length();

        float mmPerWorldUnit = (terrainSideMeters * 1000.0f) / Math.max(1, gridWidth);
        float galleryRadiusMm = customGalleryDiameterMm > 0 ? customGalleryDiameterMm / 2.0f : 10.0f;
        float galleryRadius3D = Math.max(0.04f, (galleryRadiusMm / mmPerWorldUnit) * 1.25f);

        // Cylinder aligned Z
        Cylinder shape = new Cylinder(4, 8, galleryRadius3D, Math.max(0.05f, len), true);
        Geometry geom = new Geometry(name, shape);

        // Position at midpoint
        geom.setLocalTranslation(p1.add(diff.mult(0.5f)));

        // Rotate to match direction
        if (len > 0.001f) {
            geom.lookAt(p2, Vector3f.UNIT_Y);
        }

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
