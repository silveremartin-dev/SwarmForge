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

    public Material getMaterial(Individual.Caste caste) {
        return matCache.get(caste);
    }

    private Mesh createAntMesh(Individual.Caste caste) {
        return createOrganismMesh(caste, null);
    }

    public ColorRGBA getColor(Individual.Caste caste) {
        switch (caste) {
            case QUEEN:
                return new ColorRGBA(0.4f, 0.1f, 0.1f, 1.0f);
            case SOLDIER:
                return new ColorRGBA(0.15f, 0.15f, 0.15f, 1.0f);
            case MALE:
                return new ColorRGBA(0.2f, 0.2f, 0.3f, 1.0f);
            default:
                return new ColorRGBA(0.6f, 0.3f, 0.1f, 1.0f);
        }
    }

    public Geometry createImmatureGeometry(Individual.LifeStage stage) {
        Mesh mesh;
        ColorRGBA color;
        if (stage == Individual.LifeStage.EGG) {
            mesh = new Sphere(8, 8, 0.15f);
            color = new ColorRGBA(0.95f, 0.95f, 0.85f, 0.9f);
        } else if (stage == Individual.LifeStage.LARVA) {
            mesh = new Sphere(8, 8, 0.25f);
            color = new ColorRGBA(0.9f, 0.9f, 0.75f, 1.0f);
        } else {
            mesh = new Sphere(8, 8, 0.35f);
            color = new ColorRGBA(0.85f, 0.75f, 0.55f, 1.0f);
        }
        Geometry geom = new Geometry("Immature_" + stage, mesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
        geom.setMaterial(mat);
        return geom;
    }

    public Geometry createAntGeometry(Individual.Caste caste, Individual.LifeStage stage) {
        return createOrganismGeometry(caste, stage, null);
    }

    public Geometry createOrganismGeometry(Individual.Caste caste, Individual.LifeStage stage, org.swarmforge.core.species.Species species) {
        if (stage != Individual.LifeStage.ADULT) {
            return createImmatureGeometry(stage);
        }

        Mesh mesh = getOrCreateSpeciesMesh(caste, species);
        Geometry geom = new Geometry("Organism_" + (species != null ? species.getCommonName() : "Ant") + "_" + caste, mesh);
        
        Material mat = getOrCreateSpeciesMaterial(caste, species);
        geom.setMaterial(mat);
        return geom;
    }

    // Helper to keep signature if used elsewhere
    public Geometry createAntGeometry(Individual.Caste caste) {
        return createAntGeometry(caste, Individual.LifeStage.ADULT);
    }

    private final java.util.Map<String, Mesh> speciesMeshCache = new java.util.HashMap<>();
    private final java.util.Map<String, Material> speciesMatCache = new java.util.HashMap<>();

    private Mesh getOrCreateSpeciesMesh(Individual.Caste caste, org.swarmforge.core.species.Species species) {
        String key = (species != null ? species.getInsectOrder().name() : "ANT") + "_" + caste.name();
        if (speciesMeshCache.containsKey(key)) {
            return speciesMeshCache.get(key);
        }

        Mesh mesh = createOrganismMesh(caste, species);
        speciesMeshCache.put(key, mesh);
        return mesh;
    }

    private Material getOrCreateSpeciesMaterial(Individual.Caste caste, org.swarmforge.core.species.Species species) {
        String key = (species != null ? species.getInsectOrder().name() : "ANT") + "_" + caste.name();
        if (speciesMatCache.containsKey(key)) {
            return speciesMatCache.get(key);
        }

        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setBoolean("UseMaterialColors", true);
        ColorRGBA color = getSpeciesColor(caste, species);
        mat.setColor("Diffuse", color);
        mat.setColor("Ambient", color);
        speciesMatCache.put(key, mat);
        return mat;
    }

    private ColorRGBA getSpeciesColor(Individual.Caste caste, org.swarmforge.core.species.Species species) {
        org.swarmforge.core.species.Species.InsectOrder order = species != null ? species.getInsectOrder() : org.swarmforge.core.species.Species.InsectOrder.ANT;
        
        switch (order) {
            case TERMITE -> {
                if (caste == Individual.Caste.QUEEN) return new ColorRGBA(0.95f, 0.90f, 0.75f, 1.0f); // Cream physogastric
                if (caste == Individual.Caste.SOLDIER) return new ColorRGBA(0.9f, 0.45f, 0.1f, 1.0f); // Orange head soldier
                return new ColorRGBA(0.9f, 0.88f, 0.82f, 1.0f); // Pale milky white worker
            }
            case BEE, WASP -> {
                if (caste == Individual.Caste.QUEEN) return new ColorRGBA(1.0f, 0.7f, 0.0f, 1.0f); // Golden Queen
                if (caste == Individual.Caste.MALE) return new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f); // Dark Drone
                return new ColorRGBA(0.95f, 0.75f, 0.1f, 1.0f); // Black & Gold Worker
            }
            case APHID -> {
                return new ColorRGBA(0.3f, 0.85f, 0.3f, 1.0f); // Lime green
            }
            case BEETLE -> {
                return new ColorRGBA(0.2f, 0.15f, 0.10f, 1.0f); // Dark chitin
            }
            default -> { // Formicidae (Ant)
                return getColor(caste);
            }
        }
    }

    private Mesh createOrganismMesh(Individual.Caste caste, org.swarmforge.core.species.Species species) {
        org.swarmforge.core.species.Species.InsectOrder order = species != null ? species.getInsectOrder() : org.swarmforge.core.species.Species.InsectOrder.ANT;

        float scale = getScale(caste, species);
        java.util.List<Vector3f> pos = new java.util.ArrayList<>();
        java.util.List<Vector3f> norm = new java.util.ArrayList<>();
        java.util.List<Integer> idx = new java.util.ArrayList<>();
        int offset = 0;

        if (order == org.swarmforge.core.species.Species.InsectOrder.TERMITE) {
            // Termite Morphology: Soft pale thorax, large armored head for soldiers, swollen abdomen for queens
            float headScale = (caste == Individual.Caste.SOLDIER) ? 1.4f : 1.0f;
            float abdLength = (caste == Individual.Caste.QUEEN) ? 2.5f : 1.0f;

            // Thorax
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.1f * scale, 0),
                    new Vector3f(0.14f * scale, 0.1f * scale, 0.18f * scale));
            // Head
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, 0.28f * scale),
                    new Vector3f(0.14f * scale * headScale, 0.14f * scale * headScale, 0.16f * scale * headScale));
            // Abdomen (Physogastric for Queen)
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, -0.4f * scale * abdLength),
                    new Vector3f(0.22f * scale * (abdLength > 1 ? 1.4f : 1f), 0.22f * scale, 0.3f * scale * abdLength));

            // Mandibles for Soldier
            if (caste == Individual.Caste.SOLDIER) {
                offset = addBox(pos, norm, idx, offset, new Vector3f(0.08f * scale, 0.15f * scale, 0.48f * scale),
                        new Vector3f(0.03f * scale, 0.03f * scale, 0.12f * scale));
                offset = addBox(pos, norm, idx, offset, new Vector3f(-0.08f * scale, 0.15f * scale, 0.48f * scale),
                        new Vector3f(0.03f * scale, 0.03f * scale, 0.12f * scale));
            }
        } else if (order == org.swarmforge.core.species.Species.InsectOrder.BEE) {
            // Bee Morphology: Plump fuzzy thorax, ovate abdomen, wide dual wings
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.14f * scale, 0),
                    new Vector3f(0.24f * scale, 0.20f * scale, 0.24f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, 0.28f * scale),
                    new Vector3f(0.16f * scale, 0.16f * scale, 0.16f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.16f * scale, -0.38f * scale),
                    new Vector3f(0.26f * scale, 0.24f * scale, 0.32f * scale));

            // Wide Translucent Wings
            offset = addBox(pos, norm, idx, offset, new Vector3f(0.28f * scale, 0.30f * scale, -0.05f * scale),
                    new Vector3f(0.28f * scale, 0.01f, 0.15f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(-0.28f * scale, 0.30f * scale, -0.05f * scale),
                    new Vector3f(0.28f * scale, 0.01f, 0.15f * scale));
        } else if (order == org.swarmforge.core.species.Species.InsectOrder.WASP) {
            // Wasp Morphology: Slender waist (petiole), pointed abdomen with sting, elongated wings
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.12f * scale, 0),
                    new Vector3f(0.16f * scale, 0.14f * scale, 0.20f * scale));
            // Thin petiole waist
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.12f * scale, -0.16f * scale),
                    new Vector3f(0.05f * scale, 0.05f * scale, 0.10f * scale));
            // Head
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, 0.26f * scale),
                    new Vector3f(0.14f * scale, 0.14f * scale, 0.14f * scale));
            // Pointed Abdomen
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.16f * scale, -0.42f * scale),
                    new Vector3f(0.18f * scale, 0.18f * scale, 0.36f * scale));

            // Long Wings
            offset = addBox(pos, norm, idx, offset, new Vector3f(0.30f * scale, 0.28f * scale, -0.05f * scale),
                    new Vector3f(0.32f * scale, 0.01f, 0.12f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(-0.30f * scale, 0.28f * scale, -0.05f * scale),
                    new Vector3f(0.32f * scale, 0.01f, 0.12f * scale));
        } else if (order == org.swarmforge.core.species.Species.InsectOrder.APHID) {
            // Aphid Morphology: Pear-shaped soft body with twin rear cornicles (siphunculi)
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.10f * scale, 0.14f * scale),
                    new Vector3f(0.12f * scale, 0.10f * scale, 0.14f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.12f * scale, 0.24f * scale),
                    new Vector3f(0.08f * scale, 0.08f * scale, 0.08f * scale));
            // Large rounded abdomen
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, -0.22f * scale),
                    new Vector3f(0.26f * scale, 0.24f * scale, 0.32f * scale));
            // Cornicles / Siphunculi
            offset = addBox(pos, norm, idx, offset, new Vector3f(0.10f * scale, 0.30f * scale, -0.32f * scale),
                    new Vector3f(0.02f * scale, 0.12f * scale, 0.02f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(-0.10f * scale, 0.30f * scale, -0.32f * scale),
                    new Vector3f(0.02f * scale, 0.12f * scale, 0.02f * scale));
        } else if (order == org.swarmforge.core.species.Species.InsectOrder.BEETLE) {
            // Beetle Morphology: Broad pronotum thorax shield, hard chitin elytra wing covers
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.12f * scale, 0.20f * scale),
                    new Vector3f(0.14f * scale, 0.10f * scale, 0.12f * scale));
            // Pronotum
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.14f * scale, 0.06f * scale),
                    new Vector3f(0.24f * scale, 0.14f * scale, 0.16f * scale));
            // Elytra Carapace
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.16f * scale, -0.26f * scale),
                    new Vector3f(0.28f * scale, 0.18f * scale, 0.38f * scale));
        } else if (order == org.swarmforge.core.species.Species.InsectOrder.THRIPS) {
            // Thrips Morphology: Elongated narrow body, fringed micro-wings
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.08f * scale, 0.25f * scale),
                    new Vector3f(0.08f * scale, 0.08f * scale, 0.10f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.08f * scale, 0.10f * scale),
                    new Vector3f(0.09f * scale, 0.08f * scale, 0.16f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.09f * scale, -0.25f * scale),
                    new Vector3f(0.10f * scale, 0.09f * scale, 0.42f * scale));
            // Fringed micro-wings
            offset = addBox(pos, norm, idx, offset, new Vector3f(0.08f * scale, 0.16f * scale, -0.15f * scale),
                    new Vector3f(0.06f * scale, 0.01f, 0.35f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(-0.08f * scale, 0.16f * scale, -0.15f * scale),
                    new Vector3f(0.06f * scale, 0.01f, 0.35f * scale));
        } else {
            // Standard Ant / Formicidae morphology (Default Fallback)
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.1f * scale, 0),
                    new Vector3f(0.15f * scale, 0.1f * scale, 0.2f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, 0.3f * scale),
                    new Vector3f(0.12f * scale, 0.12f * scale, 0.12f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(0, 0.15f * scale, -0.35f * scale),
                    new Vector3f(0.2f * scale, 0.2f * scale, 0.25f * scale));
        }

        // 6 Legs (Shared across all hexapods)
        float legLen = 0.3f * scale;
        float legThick = 0.03f * scale;
        float legY = 0.1f * scale;

        offset = addBox(pos, norm, idx, offset, new Vector3f(0.2f * scale, legY - 0.1f * scale, 0.1f * scale), new Vector3f(legLen, legThick, legThick));
        offset = addBox(pos, norm, idx, offset, new Vector3f(-0.2f * scale, legY - 0.1f * scale, 0.1f * scale), new Vector3f(legLen, legThick, legThick));
        offset = addBox(pos, norm, idx, offset, new Vector3f(0.25f * scale, legY - 0.1f * scale, 0), new Vector3f(legLen, legThick, legThick));
        offset = addBox(pos, norm, idx, offset, new Vector3f(-0.25f * scale, legY - 0.1f * scale, 0), new Vector3f(legLen, legThick, legThick));
        offset = addBox(pos, norm, idx, offset, new Vector3f(0.2f * scale, legY - 0.1f * scale, -0.1f * scale), new Vector3f(legLen, legThick, legThick));
        offset = addBox(pos, norm, idx, offset, new Vector3f(-0.2f * scale, legY - 0.1f * scale, -0.1f * scale), new Vector3f(legLen, legThick, legThick));

        Mesh mesh = new Mesh();
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Position, 3, com.jme3.util.BufferUtils.createFloatBuffer(pos.toArray(new Vector3f[0])));
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Normal, 3, com.jme3.util.BufferUtils.createFloatBuffer(norm.toArray(new Vector3f[0])));
        mesh.setBuffer(com.jme3.scene.VertexBuffer.Type.Index, 1, com.jme3.util.BufferUtils.createIntBuffer(idx.stream().mapToInt(i -> i).toArray()));
        mesh.updateBound();
        return mesh;
    }

    private float terrainSideMeters = 10.0f; // Default terrain side length in meters
    private int gridWidth = 64; // Default 3D scene grid dimension

    public void setTerrainDimensions(float terrainSideMeters, int gridWidth) {
        if (terrainSideMeters > 0) this.terrainSideMeters = terrainSideMeters;
        if (gridWidth > 0) this.gridWidth = gridWidth;
    }

    private float getScale(Individual.Caste caste, org.swarmforge.core.species.Species species) {
        float lengthMm = 0.0f;
        if (species != null && species.getCastes() != null) {
            for (org.swarmforge.core.domain.CasteTemplate template : species.getCastes()) {
                if (template != null && template.getName() != null && 
                    (template.getName().equalsIgnoreCase(caste.name()) || template.getName().toUpperCase().contains(caste.name()))) {
                    if (template.getBodyLengthMm() > 0.0f) {
                        lengthMm = template.getBodyLengthMm();
                        break;
                    }
                }
            }
        }
        if (lengthMm <= 0.0f) {
            lengthMm = switch (caste) {
                case QUEEN -> 15.0f;  // 15 mm queen body length
                case SOLDIER -> 10.0f; // 10 mm soldier body length
                case MALE -> 8.0f;    // 8 mm male body length
                default -> 6.0f;      // 6 mm worker body length
            };
        }

        // Millimeters per 3D scene voxel unit = (terrainSideMeters * 1000 mm) / gridWidth
        float mmPerWorldUnit = (terrainSideMeters * 1000.0f) / Math.max(1, gridWidth);
        float physicalScale = lengthMm / mmPerWorldUnit;

        // Apply a visual contrast multiplier (3.5x) so small insects remain clearly visible at camera zoom while maintaining exact caste ratios
        return Math.max(0.12f, physicalScale * 3.5f);
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
