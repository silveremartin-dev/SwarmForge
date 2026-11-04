/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view.atmosphere;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.Random;

/**
 * Data structures for atmospheric particles, airborne agents, and riparian micro-fauna.
 */
public final class AtmosphereParticleModels {

    private AtmosphereParticleModels() {}

    public static class FallingLeafParticle {
        public final Geometry geom;
        public final Vector3f spawnPos;
        public final float groundY;
        public float currentY;
        public final float fallSpeed;
        public final float swayFrequency;
        public final float swayRadius;
        public final float phase;

        public FallingLeafParticle(Geometry geom, Vector3f spawnPos, float groundY, float fallSpeed, float swayFrequency, float swayRadius, float phase) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.groundY = groundY;
            this.currentY = spawnPos.y;
            this.fallSpeed = fallSpeed;
            this.swayFrequency = swayFrequency;
            this.swayRadius = swayRadius;
            this.phase = phase;
        }
    }

    public static class ButterflyAgent {
        public final Node node;
        public final Spatial leftWing;
        public final Spatial rightWing;
        public final Vector3f anchor;
        public final float flightSpeed;
        public final float flightRadius;
        public final float phaseOffset;
        public final float flapSpeed;

        public ButterflyAgent(Node node, Spatial leftWing, Spatial rightWing, Vector3f anchor, float flightSpeed, float flightRadius, float phaseOffset, float flapSpeed) {
            this.node = node;
            this.leftWing = leftWing;
            this.rightWing = rightWing;
            this.anchor = anchor;
            this.flightSpeed = flightSpeed;
            this.flightRadius = flightRadius;
            this.phaseOffset = phaseOffset;
            this.flapSpeed = flapSpeed;
        }
    }

    public static class FireflyParticle {
        public final Geometry geom;
        public final Vector3f basePos;
        public final float speed;
        public final float phase;

        public FireflyParticle(Geometry geom, Vector3f basePos, float speed, float phase) {
            this.geom = geom;
            this.basePos = basePos;
            this.speed = speed;
            this.phase = phase;
        }
    }

    public static class BirdAgent {
        public final Geometry geom;
        public final Vector3f center;
        public final float radius;
        public final float speed;
        public final float altitude;
        public float currentAngle;

        public BirdAgent(Geometry geom, Vector3f center, float radius, float speed, float altitude, float startAngle) {
            this.geom = geom;
            this.center = center;
            this.radius = radius;
            this.speed = speed;
            this.altitude = altitude;
            this.currentAngle = startAngle;
        }
    }

    public static class PollenParticle {
        public final Geometry geom;
        public final Vector3f spawnPos;
        public final float groundY;
        public final float driftFreq;
        public final float phase;

        public PollenParticle(Geometry geom, Vector3f spawnPos, float groundY, float driftFreq, float phase) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.groundY = groundY;
            this.driftFreq = driftFreq;
            this.phase = phase;
        }
    }

    public static class DesertDustParticle {
        public final Geometry geom;
        public final Vector3f spawnPos;
        public final float groundY;
        public final float phase;

        public DesertDustParticle(Geometry geom, Vector3f spawnPos, float groundY, float phase) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.groundY = groundY;
            this.phase = phase;
        }
    }

    public static class WaterStriderAgent {
        public final Node node;
        public final Vector3f currentPos;
        public final float waterY;
        public final int gridW, gridH;
        public float burstTimer;
        public float burstInterval;
        public float vx, vz;
        public float heading;

        public WaterStriderAgent(Node node, Vector3f spawnPos, int gridW, int gridH, Random rand) {
            this.node = node;
            this.currentPos = new Vector3f(spawnPos);
            this.waterY = spawnPos.y;
            this.gridW = gridW;
            this.gridH = gridH;
            this.burstTimer = rand.nextFloat() * 2.0f;
            this.burstInterval = 1.2f + rand.nextFloat() * 2.0f;
            this.heading = rand.nextFloat() * FastMath.TWO_PI;
        }

        public void update(float tpf, float swayTime) {
            burstTimer += tpf;
            if (burstTimer >= burstInterval) {
                burstTimer = 0.0f;
                heading += (FastMath.nextRandomFloat() - 0.5f) * 1.8f;
                float speed = 0.8f + FastMath.nextRandomFloat() * 0.7f;
                vx = FastMath.sin(heading) * speed;
                vz = FastMath.cos(heading) * speed;
            }

            vx *= (1.0f - 2.8f * tpf);
            vz *= (1.0f - 2.8f * tpf);

            currentPos.x += vx * tpf;
            currentPos.z += vz * tpf;

            if (currentPos.x < 1.5f || currentPos.x > gridW - 1.5f) {
                vx = -vx;
                heading = FastMath.atan2(vx, vz);
            }
            if (currentPos.z < 1.5f || currentPos.z > gridH - 1.5f) {
                vz = -vz;
                heading = FastMath.atan2(vx, vz);
            }

            node.setLocalTranslation(currentPos.x, waterY + FastMath.sin(swayTime * 2.5f) * 0.005f, currentPos.z);
            node.setLocalRotation(new Quaternion().fromAngles(0, heading, 0));
        }
    }
}
