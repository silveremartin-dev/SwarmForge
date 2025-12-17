package org.swarmforge.client.view;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.asset.AssetManager;
import org.swarmforge.core.domain.Individual;

/**
 * Procedural visualizer for Ants.
 * Creates a Node containing shapes for Head, Thorax, and Abdomen.
 */
public class AntVisualizer {

    private final AssetManager assetManager;
    private final java.util.Map<Individual.Caste, Mesh> meshCache = new java.util.EnumMap<>(Individual.Caste.class);
    private final java.util.Map<Individual.Caste, Material> matCache = new java.util.EnumMap<>(Individual.Caste.class);

    public AntVisualizer(AssetManager assetManager) {
        this.assetManager = assetManager;
        precomputeMeshes();
    }

    private void precomputeMeshes() {
        for (Individual.Caste caste : Individual.Caste.values()) {
            meshCache.put(caste, createAntMesh(caste));

            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setBoolean("UseMaterialColors", true);
            ColorRGBA color = getColor(caste);
            mat.setColor("Diffuse", color);
            mat.setColor("Ambient", color);
            matCache.put(caste, mat);
        }
    }

    public Geometry createAntGeometry(Individual.Caste caste, Individual.LifeStage stage) {
        // Cache key needs to include stage now? Or separate methods?
        // Let's assume we handle stages dynamically or cache them too.
        // For simplicity, generate non-adults on fly or simple cache.

        if (stage == Individual.LifeStage.ADULT) {
            return new Geometry("Ant_" + caste, meshCache.get(caste));
        } else {
            return createImmatureGeometry(stage);
        }
    }

    // Helper to keep signature if used elsewhere, but we need stage
    public Geometry createAntGeometry(Individual.Caste caste) {
        return createAntGeometry(caste, Individual.LifeStage.ADULT);
    }

    private Geometry createImmatureGeometry(Individual.LifeStage stage) {
        Geometry geom;
        switch (stage) {
            case EGG:
                geom = new Geometry("Egg", new Sphere(6, 6, 0.2f));
                geom.setMaterial(getImmatureMaterial(ColorRGBA.White));
                break;
            case LARVA:
                geom = new Geometry("Larva", new com.jme3.scene.shape.Cylinder(6, 6, 0.2f, 0.6f, true));
                geom.setMaterial(getImmatureMaterial(new ColorRGBA(0.9f, 0.9f, 0.9f, 1f))); // Off-white
                break;
            case PUPA:
                geom = new Geometry("Pupa", new Sphere(8, 8, 0.35f));
                geom.setLocalScale(1f, 1f, 1.5f); // Oval
                geom.setMaterial(getImmatureMaterial(new ColorRGBA(0.6f, 0.4f, 0.2f, 1f))); // Brown
                break;
            default:
                geom = new Geometry("Unknown", new Box(0.1f, 0.1f, 0.1f));
        }
        return geom;
    }

    private Material getImmatureMaterial(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
        return mat;
    }

    public Material getMaterial(Individual.Caste caste) {
        return matCache.get(caste);
    }

    private ColorRGBA getColor(Individual.Caste caste) {
        switch (caste) {
            case QUEEN:
                return new ColorRGBA(1.0f, 0.8f, 0.0f, 1.0f);
            case SOLDIER:
                return ColorRGBA.Red;
            case MALE:
                return ColorRGBA.Blue;
            default:
                return new ColorRGBA(0.4f, 0.2f, 0.0f, 1.0f);
        }
    }

    private Mesh createAntMesh(Individual.Caste caste) {
        // Simple manual mesh merge of 3 boxes
        // Thorax, Head, Abdomen
        float scale = getScale(caste);

        java.util.List<Vector3f> pos = new java.util.ArrayList<>();
        java.util.List<Vector3f> norm = new java.util.ArrayList<>();
        java.util.List<Integer> idx = new java.util.ArrayList<>();

        int offset = 0;

        // Thorax
        offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.1f * scale, 0),
                new Vector3f(0.15f * scale, 0.1f * scale, 0.2f * scale));

        // Head
        offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, 0.3f * scale),
                new Vector3f(0.12f * scale, 0.12f * scale, 0.12f * scale));

        // Abdomen
        offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, -0.35f * scale),
                new Vector3f(0.2f * scale, 0.2f * scale, 0.25f * scale));

        // Legs (Attached to Thorax)
        float legLen = 0.3f * scale;
        float legThick = 0.03f * scale;
        float legY = 0.1f * scale; // Thorax height center

        // Front Legs
        offset = addBox(pos, norm, idx, offset, new Vector3f(0.2f * scale, legY - 0.1f * scale, 0.1f * scale),
                new Vector3f(legLen, legThick, legThick));
        offset = addBox(pos, norm, idx, offset, new Vector3f(-0.2f * scale, legY - 0.1f * scale, 0.1f * scale),
                new Vector3f(legLen, legThick, legThick));

        // Middle Legs
        offset = addBox(pos, norm, idx, offset, new Vector3f(0.25f * scale, legY - 0.1f * scale, 0),
                new Vector3f(legLen, legThick, legThick));
        offset = addBox(pos, norm, idx, offset, new Vector3f(-0.25f * scale, legY - 0.1f * scale, 0),
                new Vector3f(legLen, legThick, legThick));

        // Back Legs
        offset = addBox(pos, norm, idx, offset, new Vector3f(0.2f * scale, legY - 0.1f * scale, -0.1f * scale),
                new Vector3f(legLen, legThick, legThick));
        offset = addBox(pos, norm, idx, offset, new Vector3f(-0.2f * scale, legY - 0.1f * scale, -0.1f * scale),
                new Vector3f(legLen, legThick, legThick));

        Mesh mesh = new Mesh();
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Position, 3,
                com.jme3.util.BufferUtils.createFloatBuffer(pos.toArray(new Vector3f[0])));
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Normal, 3,
                com.jme3.util.BufferUtils.createFloatBuffer(norm.toArray(new Vector3f[0])));
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Index, 1,
                com.jme3.util.BufferUtils.createIntBuffer(idx.stream().mapToInt(i -> i).toArray()));
        mesh.updateBound();
        return mesh;
    }

    private float getScale(Individual.Caste caste) {
        switch (caste) {
            case QUEEN:
                return 1.5f;
            case SOLDIER:
                return 1.2f;
            case MALE:
                return 1.1f;
            default:
                return 0.8f;
        }
    }

    private int addBox(java.util.List<Vector3f> pos, java.util.List<Vector3f> norm, java.util.List<Integer> idx,
            int offset, Vector3f center, Vector3f ext) {
        // 24 verts total (4 per face x 6 faces) for proper normals
        // Unit box vertices are created in addFace for each face

        // Front (+Z)
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Z, 0, 2, 1, 3);
        offset += 4;
        // Back (-Z)
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Z.negate(), 5, 7, 4, 6);
        offset += 4;
        // Left (-X)
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_X.negate(), 1, 3, 5, 7);
        offset += 4;
        // Right (+X)
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_X, 4, 6, 0, 2);
        offset += 4;
        // Top (+Y)
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Y, 1, 5, 0, 4);
        offset += 4;
        // Bottom (-Y)
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Y.negate(), 2, 6, 3, 7);
        offset += 4;

        return offset;
    }

    private void addFace(java.util.List<Vector3f> pos, java.util.List<Vector3f> norm, java.util.List<Integer> idx,
            int offset, Vector3f c, Vector3f e, Vector3f n, int v1, int v2, int v3, int v4) {
        Vector3f[] unitBox = new Vector3f[] {
                new Vector3f(1, 1, 1), new Vector3f(-1, 1, 1), new Vector3f(1, -1, 1), new Vector3f(-1, -1, 1),
                new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1), new Vector3f(1, -1, -1), new Vector3f(-1, -1, -1)
        };

        pos.add(unitBox[v1].mult(e).add(c));
        pos.add(unitBox[v2].mult(e).add(c));
        pos.add(unitBox[v3].mult(e).add(c));
        pos.add(unitBox[v4].mult(e).add(c));

        for (int i = 0; i < 4; i++)
            norm.add(n);

        // v1, v2, v3 | v2 v4 v3
        // 0, 1, 2 | 1, 3, 2
        idx.add(offset + 0);
        idx.add(offset + 1);
        idx.add(offset + 2);
        idx.add(offset + 1);
        idx.add(offset + 3);
        idx.add(offset + 2);
    }

    private final java.util.Map<Individual.Caste, com.jme3.scene.instancing.InstancedNode> instancedNodes = new java.util.EnumMap<>(
            Individual.Caste.class);

    public void registerInstancedNode(Individual.Caste caste, com.jme3.scene.instancing.InstancedNode node) {
        instancedNodes.put(caste, node);
    }

    public com.jme3.scene.instancing.InstancedNode getInstancedNode(Individual.Caste caste) {
        return instancedNodes.get(caste);
    }
}
