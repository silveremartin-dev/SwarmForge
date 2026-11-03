/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import java.util.List;

/**
 * Procedural geometric construction helpers for 3D insect anatomy
 * (cylindrical articulated legs, elbowed antennae, curved mandibles, and bounding boxes).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class InsectGeometryBuilder {

    private InsectGeometryBuilder() {
    }

    static int addArticulatedLeg(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
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

    static int addElbowedAntenna(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
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

    static int addCurvedMandible(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
                                 int offset, Vector3f origin, boolean isRight, float scale) {
        float signX = isRight ? 1.0f : -1.0f;
        Vector3f mandMid = new Vector3f(origin.x + signX * 0.035f * scale, origin.y, origin.z + 0.06f * scale);
        Vector3f mandTip = new Vector3f(origin.x - signX * 0.015f * scale, origin.y - 0.01f * scale, origin.z + 0.11f * scale);

        offset = addSegmentCylinder(pos, norm, idx, offset, origin, mandMid, 0.014f * scale);
        offset = addSegmentCylinder(pos, norm, idx, offset, mandMid, mandTip, 0.010f * scale);
        return offset;
    }

    static int addSegmentCylinder(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx,
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

    static int addBox(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx, int offset, Vector3f center, Vector3f ext) {
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Z, 0, 2, 1, 3); offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Z.negate(), 5, 7, 4, 6); offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_X.negate(), 1, 3, 5, 7); offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_X, 4, 6, 0, 2); offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Y, 1, 5, 0, 4); offset += 4;
        addFace(pos, norm, idx, offset, center, ext, Vector3f.UNIT_Y.negate(), 2, 6, 3, 7); offset += 4;
        return offset;
    }

    private static void addFace(List<Vector3f> pos, List<Vector3f> norm, List<Integer> idx, int offset, Vector3f c, Vector3f e, Vector3f n, int v1, int v2, int v3, int v4) {
        Vector3f[] unitBox = new Vector3f[]{
                new Vector3f(1, 1, 1), new Vector3f(-1, 1, 1), new Vector3f(1, -1, 1), new Vector3f(-1, -1, 1),
                new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1), new Vector3f(1, -1, -1), new Vector3f(-1, -1, -1)
        };
        pos.add(unitBox[v1].mult(e).add(c)); pos.add(unitBox[v2].mult(e).add(c));
        pos.add(unitBox[v3].mult(e).add(c)); pos.add(unitBox[v4].mult(e).add(c));
        for (int i = 0; i < 4; i++) norm.add(n);
        idx.add(offset); idx.add(offset + 1); idx.add(offset + 2);
        idx.add(offset + 1); idx.add(offset + 3); idx.add(offset + 2);
    }
}
