/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view.atmosphere;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.world.Season;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Biological agent, micro-faunal, aerobiological and hydrological particle entities
 * simulating micro-ecological interactions in the 3D terrarium environment.
 */
public final class AtmosphereEntities {

    private AtmosphereEntities() {}

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

    public static class OrbWebAgent {
        public final Geometry geom;
        public final Vector3f basePos;
        public final float size;
        public final float phase;
        public final float maxLifespan;
        public float currentAge;
        public float turnoverTimer;
        public boolean isDestroyed;
        public final int gridW, gridH;
        public final Terrarium terrarium;

        public OrbWebAgent(Geometry geom, Vector3f pos, float size, float phase, float maxLifespan, int gridW, int gridH, Terrarium terrarium) {
            this.geom = geom;
            this.basePos = new Vector3f(pos);
            this.size = size;
            this.phase = phase;
            this.maxLifespan = maxLifespan;
            this.currentAge = 0.0f;
            this.turnoverTimer = 0.0f;
            this.isDestroyed = false;
            this.gridW = gridW;
            this.gridH = gridH;
            this.terrarium = terrarium;
        }

        public void update(float tpf, float swayTime, float rainWetness, boolean isDay, float windSpeed) {
            if (!isDestroyed) {
                currentAge += tpf;
                if (currentAge >= maxLifespan || windSpeed > 8.0f) {
                    isDestroyed = true;
                    turnoverTimer = 8.0f + FastMath.nextRandomFloat() * 12.0f;
                }
            } else {
                turnoverTimer -= tpf;
                if (turnoverTimer <= 0.0f) {
                    isDestroyed = false;
                    currentAge = 0.0f;
                    float newX = 2.0f + FastMath.nextRandomFloat() * (gridW - 4.0f);
                    float newZ = 2.0f + FastMath.nextRandomFloat() * (gridH - 4.0f);
                    float newY = (terrarium != null) ? (terrarium.getSurfaceElevation(newX, newZ) + 0.6f + FastMath.nextRandomFloat() * 1.8f) : basePos.y;
                    basePos.set(newX, newY, newZ);
                    geom.setLocalTranslation(newX - size * 0.5f, newY - size * 0.5f, newZ);
                }
            }

            if (isDestroyed) {
                geom.setCullHint(Spatial.CullHint.Always);
                return;
            }

            geom.setCullHint(Spatial.CullHint.Dynamic);
            float degradation = 1.0f - (currentAge / maxLifespan) * 0.6f;
            float dewShimmer = (0.25f + 0.50f * rainWetness + 0.25f * FastMath.sqr(FastMath.sin(swayTime * 1.5f + phase))) * degradation;
            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Color") != null) {
                mat.setColor("Color", new ColorRGBA(0.92f, 0.96f, 1.0f, Math.min(0.85f, dewShimmer)));
            }
        }
    }

    public static class DandelionPappusParticle {
        public final Geometry geom;
        public final Vector3f spawnPos;
        public final float groundY;
        public final float driftFreq;
        public final float phase;
        public final float floatSpeed;

        public DandelionPappusParticle(Geometry geom, Vector3f spawnPos, float groundY, float driftFreq, float phase, float floatSpeed) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.groundY = groundY;
            this.driftFreq = driftFreq;
            this.phase = phase;
            this.floatSpeed = floatSpeed;
        }

        public void update(float tpf, float windDx, float windDz, float swayTime) {
            if (tpf <= 0.0001f) return;
            Vector3f pos = geom.getLocalTranslation();
            pos.x += (windDx * 0.35f + FastMath.sin(swayTime * driftFreq + phase) * 0.12f) * tpf;
            pos.z += (windDz * 0.35f + FastMath.cos(swayTime * driftFreq + phase) * 0.12f) * tpf;
            pos.y += (floatSpeed + FastMath.sin(swayTime * 1.2f + phase) * 0.08f) * tpf;
            if (pos.y > groundY + 6.0f) {
                pos.y = groundY + 0.3f;
            }
            geom.setLocalTranslation(pos);
        }
    }

    public static class MushroomClusterAgent {
        public final List<Spatial> mushroomInstances;
        public final float baseX, baseZ;
        public float flushMoisture;
        public float currentScale;
        public boolean visible;

        public MushroomClusterAgent(List<Spatial> instances, float x, float z) {
            this.mushroomInstances = instances;
            this.baseX = x;
            this.baseZ = z;
            this.flushMoisture = 0.8f;
            this.currentScale = 1.0f;
            this.visible = true;
        }

        public void update(float tpf, float rainWetness, Season season, float temp) {
            if (rainWetness > 0.25f) {
                flushMoisture = Math.min(1.0f, flushMoisture + 0.20f * tpf);
            } else {
                float desiccationRate = 0.010f + 0.025f * (Math.max(0.0f, temp) / 30.0f);
                if (season == Season.WINTER || temp < 2.0f) desiccationRate = 0.06f;
                flushMoisture = Math.max(0.0f, flushMoisture - desiccationRate * tpf);
            }

            float targetScale = (season == Season.WINTER || flushMoisture < 0.10f) ? 0.0f : (0.25f + 0.75f * flushMoisture);
            currentScale += (targetScale - currentScale) * Math.min(1.0f, 2.5f * tpf);

            if (currentScale < 0.05f) {
                if (visible) {
                    for (Spatial s : mushroomInstances) s.setCullHint(Spatial.CullHint.Always);
                    visible = false;
                }
            } else {
                if (!visible) {
                    for (Spatial s : mushroomInstances) s.setCullHint(Spatial.CullHint.Dynamic);
                    visible = true;
                }
                for (Spatial s : mushroomInstances) {
                    Float origScale = (Float) s.getUserData("OriginalScaleY");
                    if (origScale != null) {
                        s.setLocalScale(s.getLocalScale().x, origScale * currentScale, s.getLocalScale().z);
                    }
                }
            }
        }
    }

    public static class SoilSteamParticle {
        public final Geometry geom;
        public final Vector3f spawnPos;
        public final float maxAltitude;
        public final float riseSpeed;
        public final float phase;
        public float currentY;

        public SoilSteamParticle(Geometry geom, Vector3f spawnPos, float maxAltitude, float riseSpeed, float phase) {
            this.geom = geom;
            this.spawnPos = spawnPos;
            this.maxAltitude = maxAltitude;
            this.riseSpeed = riseSpeed;
            this.phase = phase;
            this.currentY = spawnPos.y;
        }

        public void update(float tpf, float windDx, float windDz) {
            currentY += riseSpeed * tpf;
            if (currentY >= maxAltitude) {
                currentY = spawnPos.y;
            }
            float progress = (currentY - spawnPos.y) / (maxAltitude - spawnPos.y);
            float alpha = (1.0f - progress) * 0.22f;

            Vector3f pos = geom.getLocalTranslation();
            pos.y = currentY;
            pos.x = spawnPos.x + windDx * progress * 0.8f;
            pos.z = spawnPos.z + windDz * progress * 0.8f;
            geom.setLocalTranslation(pos);

            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Color") != null) {
                mat.setColor("Color", new ColorRGBA(0.95f, 0.98f, 1.0f, alpha));
            }
        }
    }

    public static class StridulationAura {
        public final Geometry geom;
        public final Vector3f centerPos;
        public final float expansionSpeed;
        public final float maxRadius;
        public float currentRadius = 0.1f;

        public StridulationAura(Geometry geom, Vector3f centerPos, float expansionSpeed, float maxRadius) {
            this.geom = geom;
            this.centerPos = centerPos;
            this.expansionSpeed = expansionSpeed;
            this.maxRadius = maxRadius;
        }

        public void update(float tpf) {
            currentRadius += expansionSpeed * tpf;
            if (currentRadius >= maxRadius) {
                currentRadius = 0.1f;
            }
            float progress = currentRadius / maxRadius;
            float alpha = (1.0f - progress) * 0.35f;

            geom.setLocalScale(currentRadius, currentRadius, 1.0f);
            Material mat = geom.getMaterial();
            if (mat != null && mat.getMaterialDef().getMaterialParam("Color") != null) {
                mat.setColor("Color", new ColorRGBA(0.40f, 0.85f, 0.95f, alpha));
            }
        }
    }

    public static class DynamicPuddleAgent {
        public final Geometry geom;
        public final Vector3f center;
        public final float maxRadius;
        public float currentRadius = 0.05f;

        public DynamicPuddleAgent(Geometry geom, Vector3f center, float maxRadius) {
            this.geom = geom;
            this.center = center;
            this.maxRadius = maxRadius;
        }

        public void update(float tpf, float rain, float temp, float swayTime) {
            if (rain > 0.05f) {
                currentRadius = Math.min(maxRadius, currentRadius + 0.35f * rain * tpf);
            } else {
                float evapRate = 0.012f + 0.030f * (Math.max(0.0f, temp) / 30.0f);
                currentRadius = Math.max(0.0f, currentRadius - evapRate * tpf);
            }

            if (currentRadius <= 0.02f) {
                geom.setCullHint(Spatial.CullHint.Always);
            } else {
                geom.setCullHint(Spatial.CullHint.Dynamic);
                float ripple = (rain > 0.05f) ? (1.0f + 0.04f * FastMath.sin(swayTime * 12.0f + center.x)) : 1.0f;
                geom.setLocalScale(currentRadius * ripple, currentRadius * ripple, 1.0f);
                Material mat = geom.getMaterial();
                if (mat != null && mat.getMaterialDef().getMaterialParam("Color") != null) {
                    float alpha = Math.min(0.85f, 0.30f + (currentRadius / maxRadius) * 0.55f);
                    mat.setColor("Color", new ColorRGBA(0.20f, 0.45f, 0.70f, alpha));
                }
            }
        }
    }

    public static class CanopyDripParticle {
        public final Geometry geom;
        public final Vector3f canopyPos;
        public final float groundY;
        public final Vector3f currentPos;
        public float velY = 0.0f;
        public float resetTimer = 0.0f;

        public CanopyDripParticle(Geometry geom, Vector3f canopyPos, float groundY, float initialDelay) {
            this.geom = geom;
            this.canopyPos = canopyPos.clone();
            this.groundY = groundY;
            this.currentPos = canopyPos.clone();
            this.resetTimer = initialDelay;
        }

        public void update(float tpf, float rainWetness) {
            if (rainWetness < 0.05f) {
                geom.setCullHint(Spatial.CullHint.Always);
                return;
            }

            if (resetTimer > 0) {
                resetTimer -= tpf;
                geom.setCullHint(Spatial.CullHint.Always);
                return;
            }

            geom.setCullHint(Spatial.CullHint.Dynamic);
            velY -= 9.81f * tpf;
            currentPos.y += velY * tpf;
            geom.setLocalTranslation(currentPos);

            if (currentPos.y <= groundY) {
                currentPos.set(canopyPos);
                velY = 0.0f;
                resetTimer = 0.4f + (1.0f - rainWetness) * 2.0f;
            }
        }
    }

    public static class GnatSwarmAgent {
        public final Node swarmNode;
        public final Vector3f center;
        public final List<Geometry> gnats = new ArrayList<>();
        public final List<Float> gnatPhases = new ArrayList<>();
        public final float radius;

        public GnatSwarmAgent(Node swarmNode, Vector3f center, float radius, List<Geometry> gnats, List<Float> phases) {
            this.swarmNode = swarmNode;
            this.center = center;
            this.radius = radius;
            this.gnats.addAll(gnats);
            this.gnatPhases.addAll(phases);
        }

        public void update(float tpf, float time, boolean isTwilight, float rain, float temp) {
            boolean active = (isTwilight || rain > 0.05f) && rain < 0.35f && temp > 10.0f;
            if (!active) {
                swarmNode.setCullHint(Spatial.CullHint.Always);
                return;
            }
            swarmNode.setCullHint(Spatial.CullHint.Dynamic);

            for (int i = 0; i < gnats.size(); i++) {
                Geometry g = gnats.get(i);
                float phase = gnatPhases.get(i);
                float theta = time * 3.2f + phase;
                float r = radius * (0.35f + 0.65f * FastMath.sin(time * 1.8f + phase * 2.0f));
                float gx = center.x + r * FastMath.cos(theta);
                float gy = center.y + 0.25f * FastMath.sin(theta * 2.2f + phase) + 0.15f * FastMath.cos(time * 4.0f + phase);
                float gz = center.z + r * FastMath.sin(theta);
                g.setLocalTranslation(gx, gy, gz);
            }
        }
    }

    public static class AphidClusterAgent {
        public final Node node;
        public final Vector3f stemPos;
        public final List<Geometry> honeydewDroplets = new ArrayList<>();

        public AphidClusterAgent(Node node, Vector3f stemPos, List<Geometry> honeydewDroplets) {
            this.node = node;
            this.stemPos = stemPos;
            this.honeydewDroplets.addAll(honeydewDroplets);
        }

        public void update(float tpf, float swayTime) {
            float pulse = 0.85f + 0.15f * FastMath.sin(swayTime * 2.5f + stemPos.x);
            for (Geometry drop : honeydewDroplets) {
                drop.setLocalScale(pulse);
            }
        }
    }

    public static class ExtrafloralNectaryAgent {
        public final Geometry geom;
        public final Vector3f position;

        public ExtrafloralNectaryAgent(Geometry geom, Vector3f position) {
            this.geom = geom;
            this.position = position;
        }

        public void update(float tpf, float swayTime) {
            float glisten = 0.90f + 0.10f * FastMath.sin(swayTime * 3.0f + position.z);
            geom.setLocalScale(glisten);
        }
    }

    public static class CordycepsCadaverAgent {
        public final Node node;
        public final Vector3f anchorPos;
        public final Geometry sporeAura;
        public float auraPulse = 0.0f;

        public CordycepsCadaverAgent(Node node, Vector3f anchorPos, Geometry sporeAura) {
            this.node = node;
            this.anchorPos = anchorPos;
            this.sporeAura = sporeAura;
        }

        public void update(float tpf, float swayTime) {
            auraPulse += tpf * 1.5f;
            float scale = 0.9f + 0.2f * FastMath.sin(auraPulse);
            if (sporeAura != null) {
                sporeAura.setLocalScale(scale);
            }
        }
    }
}
