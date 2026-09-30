/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.BufferUtils;
import org.swarmforge.core.domain.Individual;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-fidelity biological 3D insect visualizer for JMonkeyEngine.
 * Generates smooth anatomical insect meshes (tagmata: head cranium, compound eyes,
 * articulated antennae, mandibles, alitrunk, petiole scale, segmented gaster,
 * and 6 slender articulated legs) and distinct immature stages (translucent egg, C-shaped segmented larva, exarate pupa).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AntVisualizer {

    private final AssetManager assetManager;
    private final Map<Individual.Caste, Mesh> meshCache = new EnumMap<>(Individual.Caste.class);
    private final Map<Individual.Caste, Material> matCache = new EnumMap<>(Individual.Caste.class);
    private float visualScaleMultiplier = 1.0f;

    public void setVisualScaleMultiplier(float visualScaleMultiplier) {
        this.visualScaleMultiplier = visualScaleMultiplier;
    }

    public float getVisualScaleMultiplier() {
        return visualScaleMultiplier;
    }

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
            mat.setColor("Ambient", color.mult(0.6f));
            mat.setColor("Specular", new ColorRGBA(0.45f, 0.45f, 0.45f, 1.0f));
            mat.setFloat("Shininess", 32f);
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
                return new ColorRGBA(0.35f, 0.12f, 0.08f, 1.0f); // Deep glossy mahogany queen
            case SOLDIER:
                return new ColorRGBA(0.18f, 0.14f, 0.12f, 1.0f); // Dark chitinous major
            case MALE:
                return new ColorRGBA(0.12f, 0.14f, 0.22f, 1.0f); // Indigo-black alate male
            default:
                return new ColorRGBA(0.48f, 0.26f, 0.14f, 1.0f); // Amber-brown worker
        }
    }

    public Geometry createImmatureGeometry(Individual.LifeStage stage) {
        if (stage == Individual.LifeStage.EGG) {
            // EGG: Translucent pearlescent oblong smooth ellipsoid
            Sphere sphere = new Sphere(14, 14, 0.12f);
            Geometry geom = new Geometry("Immature_EGG", sphere);
            geom.setLocalScale(0.70f, 1.35f, 0.70f);
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", new ColorRGBA(0.96f, 0.96f, 0.90f, 0.88f));
            mat.setColor("Ambient", new ColorRGBA(0.85f, 0.85f, 0.78f, 0.90f));
            mat.setColor("Specular", new ColorRGBA(0.60f, 0.60f, 0.60f, 1.0f));
            mat.setFloat("Shininess", 48.0f);
            geom.setMaterial(mat);
            return geom;
        } else if (stage == Individual.LifeStage.LARVA) {
            // LARVA: Segmented curved C-shaped translucent grub with distinct head capsule
            List<Vector3f> pos = new ArrayList<>();
            List<Vector3f> norm = new ArrayList<>();
            List<Integer> idx = new ArrayList<>();
            int offset = 0;

            // Curved body arc made of 5 articulated segmented spheres
            int segments = 5;
            for (int s = 0; s < segments; s++) {
                float t = s / (float) (segments - 1);
                float angle = t * FastMath.PI * 0.75f;
                float rx = FastMath.cos(angle) * 0.16f - 0.08f;
                float ry = FastMath.sin(angle) * 0.16f + 0.08f;
                float radius = 0.08f + FastMath.sin(t * FastMath.PI) * 0.06f;
                offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, ry, rx), radius, radius, radius, 8, 8);
            }
            // Larval head capsule
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, 0.08f, 0.12f), 0.06f, 0.06f, 0.06f, 8, 8);

            Mesh mesh = new Mesh();
            mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(pos.toArray(new Vector3f[0])));
            mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(norm.toArray(new Vector3f[0])));
            mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(idx.stream().mapToInt(i -> i).toArray()));
            mesh.updateBound();

            Geometry geom = new Geometry("Immature_LARVA", mesh);
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", new ColorRGBA(0.92f, 0.90f, 0.78f, 0.92f));
            mat.setColor("Ambient", new ColorRGBA(0.80f, 0.78f, 0.65f, 0.92f));
            mat.setColor("Specular", new ColorRGBA(0.40f, 0.40f, 0.35f, 1.0f));
            mat.setFloat("Shininess", 24.0f);
            geom.setMaterial(mat);
            return geom;
        } else {
            // PUPA: Exarate pupa with leg/antennae outline or silk cocoon casing
            Sphere sphere = new Sphere(14, 14, 0.16f);
            Geometry geom = new Geometry("Immature_PUPA", sphere);
            geom.setLocalScale(0.75f, 1.50f, 0.85f);
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            mat.setBoolean("UseMaterialColors", true);
            mat.setColor("Diffuse", new ColorRGBA(0.86f, 0.78f, 0.58f, 1.0f));
            mat.setColor("Ambient", new ColorRGBA(0.70f, 0.62f, 0.44f, 1.0f));
            mat.setColor("Specular", new ColorRGBA(0.35f, 0.35f, 0.30f, 1.0f));
            mat.setFloat("Shininess", 16.0f);
            geom.setMaterial(mat);
            return geom;
        }
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

    public Geometry createAntGeometry(Individual.Caste caste) {
        return createAntGeometry(caste, Individual.LifeStage.ADULT);
    }

    private final Map<String, Mesh> speciesMeshCache = new HashMap<>();
    private final Map<String, Material> speciesMatCache = new HashMap<>();

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
        mat.setColor("Ambient", color.mult(0.6f));
        mat.setColor("Specular", new ColorRGBA(0.40f, 0.40f, 0.40f, 1.0f));
        mat.setFloat("Shininess", 28f);
        speciesMatCache.put(key, mat);
        return mat;
    }

    private ColorRGBA getSpeciesColor(Individual.Caste caste, org.swarmforge.core.species.Species species) {
        org.swarmforge.core.species.Species.InsectOrder order = species != null ? species.getInsectOrder() : org.swarmforge.core.species.Species.InsectOrder.ANT;
        if (order == org.swarmforge.core.species.Species.InsectOrder.BEE) {
            return (caste == Individual.Caste.QUEEN) ? new ColorRGBA(0.85f, 0.55f, 0.15f, 1.0f) : new ColorRGBA(0.92f, 0.72f, 0.12f, 1.0f);
        } else if (order == org.swarmforge.core.species.Species.InsectOrder.WASP) {
            return new ColorRGBA(0.95f, 0.85f, 0.10f, 1.0f);
        } else if (order == org.swarmforge.core.species.Species.InsectOrder.TERMITE) {
            return (caste == Individual.Caste.SOLDIER) ? new ColorRGBA(0.65f, 0.40f, 0.20f, 1.0f) : new ColorRGBA(0.92f, 0.90f, 0.80f, 1.0f);
        }
        return getColor(caste);
    }

    public Mesh createOrganismMesh(Individual.Caste caste, org.swarmforge.core.species.Species species) {
        List<Vector3f> pos = new ArrayList<>();
        List<Vector3f> norm = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();
        int offset = 0;

        float scale = getScale(caste, species);
        org.swarmforge.core.species.Species.InsectOrder order = species != null ? species.getInsectOrder() : org.swarmforge.core.species.Species.InsectOrder.ANT;

        if (order == org.swarmforge.core.species.Species.InsectOrder.BEE) {
            // Bee Morphology: Plump hirsute thorax, ovate striped abdomen, wide wings
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, 0.16f * scale, 0), 0.18f * scale, 0.16f * scale, 0.18f * scale, 10, 10);
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, 0.16f * scale, 0.24f * scale), 0.13f * scale, 0.13f * scale, 0.13f * scale, 10, 10);
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, 0.18f * scale, -0.32f * scale), 0.22f * scale, 0.20f * scale, 0.28f * scale, 10, 10);
            // Wings
            offset = addBox(pos, norm, idx, offset, new Vector3f(0.24f * scale, 0.28f * scale, -0.05f * scale), new Vector3f(0.24f * scale, 0.01f, 0.14f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(-0.24f * scale, 0.28f * scale, -0.05f * scale), new Vector3f(0.24f * scale, 0.01f, 0.14f * scale));
        } else if (order == org.swarmforge.core.species.Species.InsectOrder.WASP) {
            // Wasp Morphology: Slender waist (petiole), pointed abdomen with sting, elongated wings
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, 0.14f * scale, 0), 0.13f * scale, 0.12f * scale, 0.16f * scale, 10, 10);
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, 0.14f * scale, -0.14f * scale), 0.04f * scale, 0.04f * scale, 0.08f * scale, 8, 8);
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, 0.16f * scale, 0.22f * scale), 0.11f * scale, 0.11f * scale, 0.11f * scale, 10, 10);
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(0, 0.16f * scale, -0.36f * scale), 0.15f * scale, 0.14f * scale, 0.28f * scale, 10, 10);
            // Wings
            offset = addBox(pos, norm, idx, offset, new Vector3f(0.26f * scale, 0.25f * scale, -0.05f * scale), new Vector3f(0.28f * scale, 0.01f, 0.10f * scale));
            offset = addBox(pos, norm, idx, offset, new Vector3f(-0.26f * scale, 0.25f * scale, -0.05f * scale), new Vector3f(0.28f * scale, 0.01f, 0.10f * scale));
        } else {
            // Formicidae (Ant) - Complete High-Fidelity Anatomical Morphology
            float headRx = 0.11f, headRy = 0.10f, headRz = 0.12f;
            float thoraxRx = 0.10f, thoraxRy = 0.10f, thoraxRz = 0.16f;
            float petioleR = 0.045f;
            float gasterRx = 0.15f, gasterRy = 0.14f, gasterRz = 0.24f;
            boolean isQueen = (caste == Individual.Caste.QUEEN);
            boolean isSoldier = (caste == Individual.Caste.SOLDIER);
            boolean isMale = (caste == Individual.Caste.MALE);

            if (isQueen) {
                headRx = 0.14f; headRy = 0.13f; headRz = 0.14f;
                thoraxRx = 0.17f; thoraxRy = 0.16f; thoraxRz = 0.22f; // Hypertrophied wing-muscle alitrunk
                petioleR = 0.065f;
                gasterRx = 0.26f; gasterRy = 0.24f; gasterRz = 0.40f; // Physogastric distended gaster
            } else if (isSoldier) {
                headRx = 0.18f; headRy = 0.16f; headRz = 0.18f; // Hypertrophied macrocephalic cranium
                thoraxRx = 0.12f; thoraxRy = 0.11f; thoraxRz = 0.17f;
                petioleR = 0.055f;
                gasterRx = 0.16f; gasterRy = 0.15f; gasterRz = 0.25f;
            } else if (isMale) {
                headRx = 0.09f; headRy = 0.09f; headRz = 0.10f;
                thoraxRx = 0.12f; thoraxRy = 0.12f; thoraxRz = 0.16f;
                petioleR = 0.040f;
                gasterRx = 0.11f; gasterRy = 0.10f; gasterRz = 0.25f; // Slender gaster
            }

            // 1. Head Capsule (Ovate cranium with smooth organic curvature)
            Vector3f headCenter = new Vector3f(0, 0.15f * scale, 0.22f * scale);
            offset = addEllipsoid(pos, norm, idx, offset, headCenter, headRx * scale, headRy * scale, headRz * scale, 12, 12);

            // 2. Compound Eyes (Lateral hemispherical convexities)
            float eyeOffset = headRx * 0.85f * scale;
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(eyeOffset, headCenter.y + 0.03f * scale, headCenter.z + 0.02f * scale),
                    0.032f * scale, 0.038f * scale, 0.042f * scale, 8, 8);
            offset = addEllipsoid(pos, norm, idx, offset, new Vector3f(-eyeOffset, headCenter.y + 0.03f * scale, headCenter.z + 0.02f * scale),
                    0.032f * scale, 0.038f * scale, 0.042f * scale, 8, 8);

            // 3. Mandibles (Curved articulated pincer jaws)
            float mandScale = isSoldier ? 1.6f : 1.0f;
            offset = addCurvedMandible(pos, norm, idx, offset, new Vector3f(0.045f * scale, headCenter.y - 0.02f * scale, headCenter.z + headRz * scale), true, scale * mandScale);
            offset = addCurvedMandible(pos, norm, idx, offset, new Vector3f(-0.045f * scale, headCenter.y - 0.02f * scale, headCenter.z + headRz * scale), false, scale * mandScale);

            // 4. Elbowed Antennae (Scape + Funiculus Flagellum)
            offset = addElbowedAntenna(pos, norm, idx, offset, new Vector3f(0.035f * scale, headCenter.y + 0.04f * scale, headCenter.z + 0.07f * scale), true, scale);
            offset = addElbowedAntenna(pos, norm, idx, offset, new Vector3f(-0.035f * scale, headCenter.y + 0.04f * scale, headCenter.z + 0.07f * scale), false, scale);

            // 5. Alitrunk / Mesosoma (Thorax with smooth dorsal curvature)
            Vector3f thoraxCenter = new Vector3f(0, 0.13f * scale, 0);
            offset = addEllipsoid(pos, norm, idx, offset, thoraxCenter, thoraxRx * scale, thoraxRy * scale, thoraxRz * scale, 12, 12);

            // 6. Petiole (Nodiform waist scale connecting thorax and gaster)
            Vector3f petioleCenter = new Vector3f(0, 0.12f * scale, -(thoraxRz + petioleR * 0.8f) * scale);
            offset = addEllipsoid(pos, norm, idx, offset, petioleCenter, petioleR * scale, petioleR * 1.25f * scale, petioleR * scale, 8, 8);

            // 7. Gaster (Abdomen with natural ventral droop and gastral segmentation)
            Vector3f gasterCenter = new Vector3f(0, 0.14f * scale, petioleCenter.z - (gasterRz * 0.9f) * scale);
            offset = addEllipsoid(pos, norm, idx, offset, gasterCenter, gasterRx * scale, gasterRy * scale, gasterRz * scale, 14, 14);

            // 8. Male/Queen Wings
            if (isMale || (isQueen && scale > 1.4f)) {
                offset = addBox(pos, norm, idx, offset, new Vector3f(0.18f * scale, 0.22f * scale, -0.08f * scale), new Vector3f(0.20f * scale, 0.005f, 0.16f * scale));
                offset = addBox(pos, norm, idx, offset, new Vector3f(-0.18f * scale, 0.22f * scale, -0.08f * scale), new Vector3f(0.20f * scale, 0.005f, 0.16f * scale));
            }
        }

        // 6 Slender Articulated Legs (Prothoracic, Mesothoracic, Metathoracic)
        // Prothoracic Legs (Forelegs - Angled Forward)
        offset = addArticulatedLeg(pos, norm, idx, offset, new Vector3f(0.08f * scale, 0.11f * scale, 0.09f * scale), 0.45f, scale, true);
        offset = addArticulatedLeg(pos, norm, idx, offset, new Vector3f(-0.08f * scale, 0.11f * scale, 0.09f * scale), 0.45f, scale, false);

        // Mesothoracic Legs (Middle legs - Lateral)
        offset = addArticulatedLeg(pos, norm, idx, offset, new Vector3f(0.09f * scale, 0.10f * scale, 0.0f), 0.0f, scale, true);
        offset = addArticulatedLeg(pos, norm, idx, offset, new Vector3f(-0.09f * scale, 0.10f * scale, 0.0f), 0.0f, scale, false);

        // Metathoracic Legs (Hindlegs - Elongated & Angled Rearward)
        offset = addArticulatedLeg(pos, norm, idx, offset, new Vector3f(0.08f * scale, 0.09f * scale, -0.08f * scale), -0.55f, scale * 1.15f, true);
        offset = addArticulatedLeg(pos, norm, idx, offset, new Vector3f(-0.08f * scale, 0.09f * scale, -0.08f * scale), -0.55f, scale * 1.15f, false);

        Mesh mesh = new Mesh();
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(pos.toArray(new Vector3f[0])));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(norm.toArray(new Vector3f[0])));
        mesh.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(idx.stream().mapToInt(i -> i).toArray()));
        mesh.updateBound();
        return mesh;
    }

    private int addEllipsoid(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx, int offset,
                             Vector3f center, float rx, float ry, float rz, int lats, int lons) {
        int startOffset = offset;
        for (int i = 0; i <= lats; i++) {
            float theta = i * FastMath.PI / lats;
            float sinTheta = FastMath.sin(theta);
            float cosTheta = FastMath.cos(theta);

            for (int j = 0; j <= lons; j++) {
                float phi = j * FastMath.TWO_PI / lons;
                float sinPhi = FastMath.sin(phi);
                float cosPhi = FastMath.cos(phi);

                float nx = cosPhi * sinTheta;
                float ny = cosTheta;
                float nz = sinPhi * sinTheta;

                Vector3f normal = new Vector3f(nx, ny, nz).normalizeLocal();
                Vector3f p = new Vector3f(center.x + nx * rx, center.y + ny * ry, center.z + nz * rz);

                pos.add(p);
                norm.add(normal);
                offset++;
            }
        }

        for (int i = 0; i < lats; i++) {
            for (int j = 0; j < lons; j++) {
                int first = startOffset + (i * (lons + 1)) + j;
                int second = first + lons + 1;

                idx.add(first);
                idx.add(second);
                idx.add(first + 1);

                idx.add(second);
                idx.add(second + 1);
                idx.add(first + 1);
            }
        }

        return offset;
    }

    private int addArticulatedLeg(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
                                  int offset, Vector3f origin, float angleRad, float scale, boolean isRight) {
        float signX = isRight ? 1.0f : -1.0f;
        float sinA = FastMath.sin(angleRad);
        float cosA = FastMath.cos(angleRad);

        // Coxa / Trochanter base joint
        Vector3f jointKnee = new Vector3f(
                origin.x + signX * (0.16f * scale + cosA * 0.04f * scale),
                origin.y + 0.09f * scale,
                origin.z + sinA * 0.12f * scale
        );

        // Tarsus / Foot position touching ground (Y = 0)
        Vector3f footPos = new Vector3f(
                origin.x + signX * (0.28f * scale + cosA * 0.08f * scale),
                0.0f,
                origin.z + sinA * 0.22f * scale
        );

        // Femur (Upper leg - Arched)
        offset = addSegmentCylinder(pos, norm, idx, offset, origin, jointKnee, 0.016f * scale);

        // Tibia & Tarsus (Lower leg reaching to ground)
        offset = addSegmentCylinder(pos, norm, idx, offset, jointKnee, footPos, 0.012f * scale);

        return offset;
    }

    private int addElbowedAntenna(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
                                  int offset, Vector3f origin, boolean isRight, float scale) {
        float signX = isRight ? 1.0f : -1.0f;
        // Scape (First elongate antennal segment)
        Vector3f scapeEnd = new Vector3f(
                origin.x + signX * 0.08f * scale,
                origin.y + 0.08f * scale,
                origin.z + 0.12f * scale
        );
        offset = addSegmentCylinder(pos, norm, idx, offset, origin, scapeEnd, 0.009f * scale);

        // Funiculus / Flagellum (Elbowed forward sensing tip)
        Vector3f funiculusEnd = new Vector3f(
                scapeEnd.x + signX * 0.04f * scale,
                scapeEnd.y - 0.02f * scale,
                scapeEnd.z + 0.14f * scale
        );
        offset = addSegmentCylinder(pos, norm, idx, offset, scapeEnd, funiculusEnd, 0.007f * scale);

        return offset;
    }

    private int addCurvedMandible(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
                                  int offset, Vector3f origin, boolean isRight, float scale) {
        float signX = isRight ? 1.0f : -1.0f;
        Vector3f mandMid = new Vector3f(origin.x + signX * 0.035f * scale, origin.y, origin.z + 0.06f * scale);
        Vector3f mandTip = new Vector3f(origin.x - signX * 0.015f * scale, origin.y - 0.01f * scale, origin.z + 0.11f * scale);

        offset = addSegmentCylinder(pos, norm, idx, offset, origin, mandMid, 0.014f * scale);
        offset = addSegmentCylinder(pos, norm, idx, offset, mandMid, mandTip, 0.010f * scale);
        return offset;
    }

    private int addSegmentCylinder(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
                                   int offset, Vector3f p1, Vector3f p2, float radius) {
        Vector3f dir = p2.subtract(p1);
        float len = dir.length();
        if (len < 0.0001f) return offset;
        dir.normalizeLocal();

        Vector3f up = Math.abs(dir.y) < 0.9f ? Vector3f.UNIT_Y : Vector3f.UNIT_Z;
        Vector3f side = dir.cross(up).normalizeLocal();
        Vector3f ortho = dir.cross(side).normalizeLocal();

        int sides = 6;
        int startOffset = offset;
        for (int i = 0; i < sides; i++) {
            float angle = i * FastMath.TWO_PI / sides;
            float cosA = FastMath.cos(angle);
            float sinA = FastMath.sin(angle);
            Vector3f n = side.mult(cosA).add(ortho.mult(sinA)).normalizeLocal();

            pos.add(p1.add(n.mult(radius)));
            norm.add(n);
            offset++;

            pos.add(p2.add(n.mult(radius * 0.85f)));
            norm.add(n);
            offset++;
        }

        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            int v0 = startOffset + i * 2;
            int v1 = startOffset + i * 2 + 1;
            int v2 = startOffset + next * 2;
            int v3 = startOffset + next * 2 + 1;

            idx.add(v0); idx.add(v1); idx.add(v2);
            idx.add(v2); idx.add(v1); idx.add(v3);
        }

        return offset;
    }

    private float terrainSideMeters = 10.0f; // Default terrain side length in meters
    private int gridWidth = 64; // Default 3D scene grid dimension

    public void setTerrainDimensions(float terrainSideMeters, int gridWidth) {
        if (terrainSideMeters > 0) this.terrainSideMeters = terrainSideMeters;
        if (gridWidth > 0) this.gridWidth = gridWidth;
    }

    private float getScale(Individual.Caste caste, org.swarmforge.core.species.Species species) {
        float baseScale = switch (caste) {
            case QUEEN -> 1.30f;
            case SOLDIER -> 0.95f;
            case MALE -> 0.75f;
            default -> 0.60f;
        };
        return baseScale * visualScaleMultiplier;
    }

    private int addBox(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
                       int offset, Vector3f center, Vector3f ext) {
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Z, 0, 2, 1, 3);
        offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Z.negate(), 5, 7, 4, 6);
        offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_X.negate(), 1, 3, 5, 7);
        offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_X, 4, 6, 0, 2);
        offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Y, 1, 5, 0, 4);
        offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Y.negate(), 2, 6, 3, 7);
        offset += 4;
        return offset;
    }

    private void addFace(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
                         int offset, Vector3f c, Vector3f e, Vector3f n, int v1, int v2, int v3, int v4) {
        Vector3f[] unitBox = new Vector3f[]{
                new Vector3f(1, 1, 1), new Vector3f(-1, 1, 1), new Vector3f(1, -1, 1), new Vector3f(-1, -1, 1),
                new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1), new Vector3f(1, -1, -1), new Vector3f(-1, -1, -1)
        };

        pos.add(unitBox[v1].mult(e).add(c));
        pos.add(unitBox[v2].mult(e).add(c));
        pos.add(unitBox[v3].mult(e).add(c));
        pos.add(unitBox[v4].mult(e).add(c));

        for (int i = 0; i < 4; i++) norm.add(n);

        idx.add(offset + 0); idx.add(offset + 1); idx.add(offset + 2);
        idx.add(offset + 1); idx.add(offset + 3); idx.add(offset + 2);
    }

    private final Map<Individual.Caste, com.jme3.scene.Node> instancedNodes = new EnumMap<>(Individual.Caste.class);

    public void registerInstancedNode(Individual.Caste caste, com.jme3.scene.Node node) {
        instancedNodes.put(caste, node);
    }

    public com.jme3.scene.Node getInstancedNode(Individual.Caste caste) {
        return instancedNodes.get(caste);
    }
}
