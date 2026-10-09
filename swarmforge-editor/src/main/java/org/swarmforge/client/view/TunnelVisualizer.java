package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.core.simulation.TunnelNetwork;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelEdge;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Multi-mode visualizer for the underground tunnel network:
 * - REALISTIC: Organic lenticular ellipsoidal chambers under lithostatic soil pressure & textured galleries
 * - SCIENTIFIC: Metric volumetric nodes with functional chamber markers, exact diameter calibration & pipe gauges
 * - GAMIFIED: Cubic Minecraft voxel blocks and square corridor shafts
 */
public class TunnelVisualizer {

    private final AssetManager assetManager;
    private final Node rootNode;
    private final Map<java.util.UUID, Geometry> nodeGeometries = new HashMap<>();
    private org.swarmforge.core.domain.Terrarium terrarium;
    private RenderMode currentRenderMode = RenderMode.REALISTIC;

    public TunnelVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.rootNode = new Node("TunnelNetwork");
    }

    public void setTerrarium(org.swarmforge.core.domain.Terrarium terrarium) {
        this.terrarium = terrarium;
    }

    public void setRenderMode(RenderMode mode) {
        if (this.currentRenderMode != mode) {
            this.currentRenderMode = mode;
            rootNode.detachAllChildren();
            nodeGeometries.clear();
        }
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void update(TunnelNetwork network) {
        if (network == null)
            return;

        if (network.getNodeCount() == nodeGeometries.size()) {
            return;
        }

        for (TunnelNode node : network.getNodes()) {
            if (!nodeGeometries.containsKey(node.id())) {
                Geometry geom = createNodeGeometry(node);
                rootNode.attachChild(geom);
                nodeGeometries.put(node.id(), geom);
            }
        }

        // Edges (draw as cylinders or voxel prisms)
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

    private float terrainSideMeters = 10.0f; // Default terrain side length in meters
    private int gridWidth = 64; // Default 3D scene grid dimension

    public void setTerrainDimensions(float terrainSideMeters, int gridWidth) {
        if (terrainSideMeters > 0) this.terrainSideMeters = terrainSideMeters;
        if (gridWidth > 0) this.gridWidth = gridWidth;
    }

    private Geometry createNodeGeometry(TunnelNode node) {
        float mmPerWorldUnit = (terrainSideMeters * 1000.0f) / Math.max(1, gridWidth);
        
        float rx, ry, rz;
        if (node.radiusX() > 0.01f && node.radiusY() > 0.01f && node.radiusZ() > 0.01f) {
            rx = node.radiusX();
            ry = node.radiusY();
            rz = node.radiusZ();
        } else {
            // Biological chamber sizes calibrated in mm to real-world nest architectures
            float radiusMm = switch (node.type()) {
                case QUEEN_CHAMBER -> 22.0f; // 44mm diameter royal chamber
                case FUNGUS_GARDEN -> 20.0f; // 40mm diameter fungus vault
                case HIBERNATION -> 18.0f;
                case BROOD_CHAMBER, FOOD_STORAGE -> 14.0f; // 28mm diameter brood/granary chamber
                case ENTRANCE -> 10.0f; // 20mm diameter entrance vestibule
                default -> 8.0f;
            };
            float radius3D = radiusMm / mmPerWorldUnit;
            rx = radius3D;
            ry = radius3D;
            rz = radius3D * 0.60f;
        }

        Geometry geom;
        if (currentRenderMode == RenderMode.GAMIFIED) {
            // Gamified Voxel Cube Block
            Box box = new Box(rx, rz, ry);
            geom = new Geometry("Node_" + node.id(), box);
        } else {
            // Realistic Mode: Lenticular Ellipsoidal Chamber
            if (node.type() == TunnelNetwork.ChamberType.TUNNEL) {
                // Do not render intermediate tunnel pathway waypoints as spheres in realistic mode
                // This completely prevents the "spheres stuck to each other" artifact across galleries
                geom = new Geometry("Node_" + node.id(), new Box(0.001f, 0.001f, 0.001f));
                geom.setCullHint(com.jme3.scene.Spatial.CullHint.Always);
            } else {
                // Realistic Lenticular Ellipsoidal Chamber (flattened horizontally under lithostatic soil pressure)
                Sphere shape = new Sphere(16, 16, 1.0f);
                geom = new Geometry("Node_" + node.id(), shape);
                geom.setLocalScale(rx, rz, ry);
            }
        }

        float surfaceY = terrarium != null ? terrarium.getSurfaceElevation(node.x(), node.y()) : 0f;
        float posY = (node.z() <= 0) ? (surfaceY + node.z()) : node.z();
        geom.setLocalTranslation(node.x(), posY, node.y());
        geom.setUserData("ChamberID", node.id().toString());
        geom.setUserData("ChamberType", node.type().name());

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        ColorRGBA col = getNodeColor(node.type());
        mat.setColor("Diffuse", col);
        mat.setColor("Ambient", col.mult(0.65f));

        // Saliva Masonry Lining & Cuticular SSS Translucency
        if (currentRenderMode == RenderMode.REALISTIC) {
            mat.setColor("Specular", new ColorRGBA(0.35f, 0.28f, 0.20f, 1.0f));
            mat.setFloat("Shininess", 16.0f); // Polished saliva-bonded earthen sheen
            if (node.type() == TunnelNetwork.ChamberType.BROOD_CHAMBER || node.type() == TunnelNetwork.ChamberType.QUEEN_CHAMBER) {
                // Cuticular Subsurface Scattering (SSS) translucent ivory warmth
                mat.setColor("Diffuse", new ColorRGBA(0.82f, 0.74f, 0.62f, 1.0f));
                mat.setColor("Ambient", new ColorRGBA(0.55f, 0.48f, 0.38f, 1.0f));
            }
        }

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
        // Real biological gallery diameter (typically 6-10 mm for standard workers/queens)
        float galleryDiameterMm = customGalleryDiameterMm > 0 ? customGalleryDiameterMm : 8.0f;
        float galleryRadius3D = (galleryDiameterMm * 0.5f) / mmPerWorldUnit;

        Geometry geom;
        if (currentRenderMode == RenderMode.GAMIFIED) {
            // Square Corridor Shaft
            Box box = new Box(galleryRadius3D, galleryRadius3D, Math.max(0.01f, len * 0.5f));
            geom = new Geometry(name, box);
        } else {
            // Cylindrical Pipe / Natural Gallery (smooth continuous organic tube)
            Cylinder shape = new Cylinder(8, 16, galleryRadius3D, Math.max(0.01f, len), true);
            geom = new Geometry(name, shape);
        }

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
        mat.setColor("Ambient", col.mult(0.55f));
        if (currentRenderMode == RenderMode.REALISTIC) {
            // Saliva mucus gallery smoothing sheen
            mat.setColor("Specular", new ColorRGBA(0.32f, 0.25f, 0.18f, 1.0f));
            mat.setFloat("Shininess", 12.0f);
        }
        geom.setMaterial(mat);

        return geom;
    }

    private ColorRGBA getNodeColor(TunnelNetwork.ChamberType type) {
        if (currentRenderMode == RenderMode.REALISTIC) {
            return switch (type) {
                case QUEEN_CHAMBER -> new ColorRGBA(0.52f, 0.38f, 0.28f, 1.0f);
                case BROOD_CHAMBER -> new ColorRGBA(0.56f, 0.44f, 0.34f, 1.0f);
                case FOOD_STORAGE, LEAF_CACHE -> new ColorRGBA(0.48f, 0.40f, 0.26f, 1.0f);
                case FUNGUS_GARDEN -> new ColorRGBA(0.45f, 0.35f, 0.38f, 1.0f);
                case HIBERNATION -> new ColorRGBA(0.38f, 0.32f, 0.28f, 1.0f);
                case WASTE_DUMP -> new ColorRGBA(0.28f, 0.22f, 0.16f, 1.0f);
                case ENTRANCE -> new ColorRGBA(0.42f, 0.32f, 0.22f, 1.0f);
                default -> new ColorRGBA(0.40f, 0.30f, 0.20f, 1.0f);
            };
        }
        return switch (type) {
            case QUEEN_CHAMBER -> ColorRGBA.Magenta;
            case BROOD_CHAMBER -> new ColorRGBA(0.95f, 0.92f, 0.85f, 1.0f);
            case FOOD_STORAGE, LEAF_CACHE -> ColorRGBA.Green;
            case FUNGUS_GARDEN -> new ColorRGBA(0.66f, 0.33f, 0.97f, 1.0f);
            case HIBERNATION -> ColorRGBA.Cyan;
            case WASTE_DUMP -> ColorRGBA.DarkGray;
            case ENTRANCE -> ColorRGBA.Yellow;
            default -> ColorRGBA.Brown;
        };
    }
}
