/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.view;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import org.swarmforge.client.ui.WorldEditorPane.RenderMode;
import org.swarmforge.client.view.atmosphere.AtmosphereEntities.*;
import org.swarmforge.core.world.Biome;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.WeatherSystem;

import java.util.List;

/**
 * Handles frame updates, motion trajectories, and seasonal phenology
 * for ambient atmosphere and micro-ecological agents in {@link AtmosphereMicroEcologyVisualizer}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class AtmosphereUpdateController {

    private AtmosphereUpdateController() {
    }

    static float update(AtmosphereMicroEcologyVisualizer viz,
                        WeatherSystem weather, Season effectiveSeason, Biome currentBiome,
                        RenderMode currentRenderMode, float swayTime, float tpf,
                        int gridWidth, int gridHeight, float rainWetness) {
        boolean isDay = (weather == null || weather.isDaytime());
        float temp = (weather != null) ? weather.getTemperature() : 20.0f;
        float rain = (weather != null) ? weather.getRainfall() : 0.0f;
        float windSpeedMs = (weather != null) ? weather.getWindSpeedMs() : 3.0f;
        float windAngle = (weather != null) ? (float) Math.toRadians(weather.getWindDirectionAngle()) : 0.785f;
        float windSin = FastMath.sin(windAngle);
        float windCos = FastMath.cos(windAngle);
        float windDx = windSin * (windSpeedMs * 0.45f);
        float windDz = -windCos * (windSpeedMs * 0.45f);

        // 1. Butterflies
        boolean flowersInBloom = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && !viz.activeFlowerLocations.isEmpty();
        boolean butterflyWeatherActive = isDay && temp >= 14.0f && rain < 0.20f;
        boolean butterfliesVisible = flowersInBloom && butterflyWeatherActive && currentRenderMode == RenderMode.REALISTIC;

        for (ButterflyAgent b : viz.activeButterflies) {
            if (!butterfliesVisible) {
                b.node.setCullHint(Spatial.CullHint.Always);
            } else {
                b.node.setCullHint(Spatial.CullHint.Dynamic);
                float t = swayTime * b.flightSpeed + b.phaseOffset;
                float ox = FastMath.cos(t) * b.flightRadius + FastMath.sin(t * 2.3f) * (b.flightRadius * 0.35f);
                float oz = FastMath.sin(t * 1.15f) * b.flightRadius + FastMath.cos(t * 1.7f) * (b.flightRadius * 0.35f);
                float oy = FastMath.sin(t * 2.0f) * 0.30f + 0.45f;

                b.node.setLocalTranslation(b.anchor.x + ox, b.anchor.y + oy, b.anchor.z + oz);

                float vx = -FastMath.sin(t) * b.flightRadius;
                float vz = FastMath.cos(t * 1.15f) * b.flightRadius * 1.15f;
                float heading = FastMath.atan2(vx, vz);
                b.node.setLocalRotation(new Quaternion().fromAngles(0, heading, 0));

                float flap = FastMath.sin(swayTime * b.flapSpeed + b.phaseOffset) * 0.85f;
                b.leftWing.setLocalRotation(new Quaternion().fromAngles(0, 0, flap));
                b.rightWing.setLocalRotation(new Quaternion().fromAngles(0, 0, -flap));
            }
        }

        // 2. Nocturnal Fireflies
        boolean fireflySeasonActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER);
        boolean fireflyWeatherActive = !isDay && temp >= 12.0f && rain < 0.30f;
        boolean firefliesVisible = fireflySeasonActive && fireflyWeatherActive && currentRenderMode == RenderMode.REALISTIC;

        for (FireflyParticle fp : viz.activeFireflies) {
            if (!firefliesVisible) {
                fp.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                fp.geom.setCullHint(Spatial.CullHint.Dynamic);
                float ft = swayTime * fp.speed + fp.phase;
                float dx = FastMath.sin(ft * 0.7f) * 0.45f;
                float dy = FastMath.cos(ft * 1.1f) * 0.25f;
                float dz = FastMath.sin(ft * 0.9f) * 0.45f;
                fp.geom.setLocalTranslation(fp.basePos.x + dx, fp.basePos.y + dy, fp.basePos.z + dz);

                float pulse = 0.35f + 0.65f * FastMath.sqr(FastMath.sin(ft * 2.5f));
                Material fMat = fp.geom.getMaterial();
                if (fMat != null && fMat.getMaterialDef().getMaterialParam("Color") != null) {
                    fMat.setColor("Color", new ColorRGBA(0.68f * pulse, 0.92f * pulse, 0.22f * pulse, pulse));
                }
            }
        }

        // 3. Autumn Falling Leaves
        boolean leavesActive = (effectiveSeason == Season.FALL) && currentRenderMode == RenderMode.REALISTIC;
        for (FallingLeafParticle leaf : viz.activeFallingLeaves) {
            if (!leavesActive) {
                leaf.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                leaf.geom.setCullHint(Spatial.CullHint.Dynamic);
                leaf.currentY -= leaf.fallSpeed * tpf;
                if (leaf.currentY <= leaf.groundY + 0.05f) {
                    leaf.currentY = leaf.spawnPos.y;
                }

                float lt = swayTime * leaf.swayFrequency + leaf.phase;
                float lx = leaf.spawnPos.x + FastMath.sin(lt) * leaf.swayRadius;
                float lz = leaf.spawnPos.z + FastMath.cos(lt * 1.3f) * leaf.swayRadius;

                leaf.geom.setLocalTranslation(lx, leaf.currentY, lz);
                leaf.geom.setLocalRotation(new Quaternion().fromAngles(FastMath.sin(lt * 2.0f) * 0.6f, lt * 0.5f, FastMath.cos(lt * 1.5f) * 0.6f));
            }
        }

        // 4. Distant Soaring Birds
        boolean birdsVisible = (effectiveSeason != Season.WINTER && isDay && rain < 0.25f && currentRenderMode == RenderMode.REALISTIC);
        for (BirdAgent bird : viz.activeBirds) {
            if (!birdsVisible) {
                bird.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                bird.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    bird.currentAngle += bird.speed * tpf;
                }
                float bx = bird.center.x + FastMath.cos(bird.currentAngle) * bird.radius;
                float bz = bird.center.z + FastMath.sin(bird.currentAngle) * bird.radius;
                bird.geom.setLocalTranslation(bx, bird.altitude, bz);
                bird.geom.setLocalRotation(new Quaternion().fromAngles(0, -bird.currentAngle + FastMath.HALF_PI, 0.12f));
            }
        }

        // 5. Spring Aerobiological Pollen Drift
        boolean pollenActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && isDay && rain < 0.10f && currentRenderMode == RenderMode.REALISTIC;
        for (PollenParticle p : viz.activePollen) {
            if (!pollenActive) {
                p.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                p.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    Vector3f pos = p.geom.getLocalTranslation();
                    pos.x += (windDx * 0.25f + FastMath.sin(swayTime * p.driftFreq + p.phase) * 0.15f) * tpf;
                    pos.z += (windDz * 0.25f + FastMath.cos(swayTime * p.driftFreq + p.phase) * 0.15f) * tpf;
                    pos.y += FastMath.sin(swayTime * 0.8f + p.phase) * 0.08f * tpf;
                    if (pos.x < 1.0f) pos.x = gridWidth - 2.0f;
                    if (pos.x > gridWidth - 1.0f) pos.x = 2.0f;
                    if (pos.z < 1.0f) pos.z = gridHeight - 2.0f;
                    if (pos.z > gridHeight - 1.0f) pos.z = 2.0f;
                    p.geom.setLocalTranslation(pos);
                }
            }
        }

        // 6. Desert Dust Drift Simulation
        boolean dustActive = (currentBiome == Biome.DESERT) && (windSpeedMs > 2.5f || temp > 30.0f) && currentRenderMode == RenderMode.REALISTIC;
        for (DesertDustParticle d : viz.activeDesertDust) {
            if (!dustActive) {
                d.geom.setCullHint(Spatial.CullHint.Always);
            } else {
                d.geom.setCullHint(Spatial.CullHint.Dynamic);
                if (tpf > 0.0001f) {
                    Vector3f pos = d.geom.getLocalTranslation();
                    pos.x += windDx * 0.65f * tpf;
                    pos.z += windDz * 0.65f * tpf;
                    pos.y += (FastMath.sin(swayTime * 2.0f + d.phase) * 0.20f) * tpf;
                    if (pos.x < 1.0f) pos.x = gridWidth - 2.0f;
                    if (pos.x > gridWidth - 1.0f) pos.x = 2.0f;
                    if (pos.z < 1.0f) pos.z = gridHeight - 2.0f;
                    if (pos.z > gridHeight - 1.0f) pos.z = 2.0f;
                    d.geom.setLocalTranslation(pos);
                }
            }
        }

        // Rain Wetness Calculation
        if (rain > 0.1f) {
            rainWetness = Math.min(1.0f, rainWetness + 0.35f * tpf);
        } else {
            float dryingRate = 0.02f + 0.05f * (Math.max(0.0f, temp) / 30.0f);
            rainWetness = Math.max(0.0f, rainWetness - dryingRate * tpf);
        }

        // 7-18. Micro-Ecology Agents
        boolean stridersActive = (effectiveSeason != Season.WINTER && rain < 0.40f && currentRenderMode == RenderMode.REALISTIC);
        for (WaterStriderAgent ws : viz.activeWaterStriders) {
            if (!stridersActive) ws.node.setCullHint(Spatial.CullHint.Always);
            else { ws.node.setCullHint(Spatial.CullHint.Dynamic); ws.update(tpf, swayTime); }
        }
        boolean websActive = currentRenderMode == RenderMode.REALISTIC;
        for (OrbWebAgent web : viz.activeOrbWebs) {
            if (!websActive) web.geom.setCullHint(Spatial.CullHint.Always);
            else web.update(tpf, swayTime, rainWetness, isDay, windSpeedMs);
        }
        boolean steamActive = (rainWetness > 0.15f && isDay && rain < 0.10f && temp > 10.0f && currentRenderMode == RenderMode.REALISTIC);
        for (SoilSteamParticle sp : viz.activeSoilSteam) {
            if (!steamActive) sp.geom.setCullHint(Spatial.CullHint.Always);
            else { sp.geom.setCullHint(Spatial.CullHint.Dynamic); sp.update(tpf, windDx, windDz); }
        }
        boolean stridActive = (temp > 22.0f && isDay && rain < 0.20f && (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && currentRenderMode == RenderMode.REALISTIC);
        for (StridulationAura sa : viz.activeStridulations) {
            if (!stridActive) sa.geom.setCullHint(Spatial.CullHint.Always);
            else { sa.geom.setCullHint(Spatial.CullHint.Dynamic); sa.update(tpf); }
        }
        boolean pappusActive = (effectiveSeason == Season.SPRING || effectiveSeason == Season.SUMMER) && isDay && rain < 0.10f && currentRenderMode == RenderMode.REALISTIC;
        for (DandelionPappusParticle dp : viz.activePappus) {
            if (!pappusActive) dp.geom.setCullHint(Spatial.CullHint.Always);
            else { dp.geom.setCullHint(Spatial.CullHint.Dynamic); dp.update(tpf, windDx, windDz, swayTime); }
        }
        float hour = (weather != null) ? weather.getTimeOfDay() : 19.0f;
        boolean isTwilight = (hour >= 5.5f && hour <= 7.5f) || (hour >= 18.5f && hour <= 21.0f);
        if (currentRenderMode == RenderMode.REALISTIC) {
            for (MushroomClusterAgent mc : viz.activeMushroomClusters) mc.update(tpf, rainWetness, effectiveSeason, temp);
            for (DynamicPuddleAgent puddle : viz.activePuddles) puddle.update(tpf, rain, temp, swayTime);
            for (CanopyDripParticle drip : viz.activeCanopyDrips) drip.update(tpf, rainWetness);
            for (GnatSwarmAgent swarm : viz.activeGnatSwarms) swarm.update(tpf, swayTime, isTwilight, rain, temp);
            for (AphidClusterAgent aphid : viz.activeAphidClusters) aphid.update(tpf, swayTime);
            for (ExtrafloralNectaryAgent nectary : viz.activeNectaries) nectary.update(tpf, swayTime);
            for (CordycepsCadaverAgent cordyceps : viz.activeCordycepsCadavers) cordyceps.update(tpf, swayTime);
        }
        boolean drought = (temp > 28.0f && rain < 0.05f && rainWetness < 0.05f && currentRenderMode == RenderMode.REALISTIC);
        for (Geometry mc : viz.activeMudcracks) mc.setCullHint(drought ? Spatial.CullHint.Dynamic : Spatial.CullHint.Always);

        return rainWetness;
    }
}
