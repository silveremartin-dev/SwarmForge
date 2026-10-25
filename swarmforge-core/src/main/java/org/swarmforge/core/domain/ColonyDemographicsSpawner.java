/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.swarmforge.core.behavior.FSMArchitecture;
import org.swarmforge.core.behavior.ReasoningArchitecture;
import org.swarmforge.core.simulation.TunnelNetwork;
import org.swarmforge.core.structure.Chamber;
import org.swarmforge.core.structure.Nest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Biological Demographics and Spawning Engine for eusocial insect colonies.
 * Handles caste creation, subterranean placement inside chambers, surface activity
 * ratios governed by solar declination and circadian illumination, and Gaussian age/health distributions.
 */
public class ColonyDemographicsSpawner {

    private final Colony colony;

    public ColonyDemographicsSpawner(Colony colony) {
        this.colony = colony;
    }

    private Random getRandom() {
        return colony.getRandom();
    }

    public void applyGaussianAge(Individual ind, boolean isFoundingQueen) {
        if (ind == null) return;
        float maxLifespanDays = ind.getMaxLifespan();
        float maxLifespanSec = maxLifespanDays * 86400.0f;
        if (isFoundingQueen) {
            float youngAgeSec = (float) (getRandom().nextDouble(0.0, 0.05) * maxLifespanSec);
            ind.setAgeInSeconds(youngAgeSec);
        } else {
            double mean = 0.15 * maxLifespanSec;
            double stdDev = 0.06 * maxLifespanSec;
            double sample = mean + stdDev * getRandom().nextGaussian();
            float ageSec = (float) Math.max(0.01 * maxLifespanSec, Math.min(0.35 * maxLifespanSec, sample));
            ind.setAgeInSeconds(ageSec);
        }
    }

    public void applyGaussianHealth(Individual ind, boolean isFoundingQueen) {
        if (ind == null) return;
        float maxH = ind.getMaxHealth() > 0 ? ind.getMaxHealth() : 100f;
        if (isFoundingQueen) {
            ind.setHealth(maxH);
        } else {
            double mean = 0.94 * maxH;
            double stdDev = 0.05 * maxH;
            double sample = mean + stdDev * getRandom().nextGaussian();
            float h = (float) Math.max(0.65 * maxH, Math.min(maxH, sample));
            ind.setHealth(h);
        }
    }

    public List<Individual> createQueens(int count) {
        return createQueens(count, true);
    }

    public List<Individual> createQueens(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new ArrayList<>(count);
        boolean isFoundingQueen = (count == 1);

        for (int i = 0; i < count; i++) {
            float[] pos = getSpawningCoordinates(Individual.Caste.QUEEN, Individual.Job.NONE, i);
            Individual ind = new Individual(colony.getId(), Individual.Caste.QUEEN, pos[0], pos[1], pos[2]);
            ind.setSpecies(colony.getSpecies());
            ind.setBrain(new FSMArchitecture());
            if (getRandom() != null) {
                ind.setRandom(new Random(getRandom().nextLong()));
            }
            applyGaussianHealth(ind, isFoundingQueen);
            if (applyAgeDistribution) {
                applyGaussianAge(ind, isFoundingQueen);
            }
            batch.add(ind);
        }
        colony.addIndividualsBulk(batch);
        return batch;
    }

    public double calculateSurfaceActivityRatio(int dayOfYear, float hourOfDay, float ambientTemp, double latitude) {
        double declinationDeg = 23.45 * Math.sin(Math.toRadians((dayOfYear - 81) * 360.0 / 365.0));
        if (latitude < 0) {
            declinationDeg = -declinationDeg;
        }

        double hourAngleRad = Math.toRadians(15.0 * (hourOfDay - 12.0));
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declinationDeg);

        double sinElevation = Math.sin(latRad) * Math.sin(decRad) + Math.cos(latRad) * Math.cos(decRad) * Math.cos(hourAngleRad);
        double elevationDeg = Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, sinElevation))));

        double illumination = 0.0;
        if (elevationDeg > 0) {
            illumination = Math.min(1.0, sinElevation * 1.35);
        } else if (elevationDeg > -6.0) {
            illumination = (6.0 + elevationDeg) / 6.0 * 0.10;
        }

        if (ambientTemp < 8.0f || ambientTemp > 45.0f) {
            return 0.0;
        }

        double thermalFactor = 1.0;
        if (ambientTemp < 15.0f) {
            thermalFactor = Math.max(0.0, (ambientTemp - 8.0) / 7.0);
        } else if (ambientTemp > 35.0f) {
            thermalFactor = Math.max(0.1, 1.0 - (ambientTemp - 35.0) / 10.0);
        }

        return Math.max(0.0, Math.min(0.35, illumination * thermalFactor));
    }

    public double calculateSurfaceActivityRatio(int dayOfYear, float hourOfDay, float ambientTemp) {
        double lat = colony.getTerrarium() != null ? colony.getTerrarium().getLatitude() : 48.8;
        return calculateSurfaceActivityRatio(dayOfYear, hourOfDay, ambientTemp, lat);
    }

    public float[] getSpawningCoordinates(Individual.Caste caste, Individual.Job job, int index) {
        return getSpawningCoordinates(caste, job, index, 180, 14.0f, 22.0f);
    }

    public float[] getSpawningCoordinates(Individual.Caste caste, Individual.Job job, int index, int dayOfYear, float hourOfDay, float ambientTemp) {
        float nestX = colony.getNestX();
        float nestY = colony.getNestY();
        float nestZ = colony.getNestZ();
        float mapWidth = colony.getMapWidth();
        float mapHeight = colony.getMapHeight();
        Nest nest = colony.getNest();
        TunnelNetwork tunnelNetwork = colony.getTunnelNetwork();

        float sx = nestX;
        float sy = nestY;
        float sz = nestZ;

        List<Chamber> nestChambers = (nest != null && nest.getChambers() != null) ? nest.getChambers() : List.of();
        List<Chamber> entranceChambers = (nest != null) ? nest.getChambersOfType(Chamber.Type.ENTRANCE) : List.of();
        List<Chamber> nurseryChambers = (nest != null) ? nest.getChambersOfType(Chamber.Type.NURSERY) : List.of();
        List<TunnelNetwork.TunnelNode> simNodes = (tunnelNetwork != null && tunnelNetwork.getNodes() != null) ? tunnelNetwork.getNodes() : List.of();

        double surfaceActivityRatio = calculateSurfaceActivityRatio(dayOfYear, hourOfDay, ambientTemp);

        if (caste == Individual.Caste.QUEEN) {
            var qChamber = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.QUEEN_CHAMBER, nestX, nestY, nestZ) : null;
            if (qChamber != null) {
                sx = qChamber.x(); sy = qChamber.y(); sz = qChamber.z();
            } else if (nest != null) {
                List<Chamber> queenChambers = nest.getChambersOfType(Chamber.Type.QUEEN_QUARTERS);
                if (!queenChambers.isEmpty()) {
                    var ch = queenChambers.get(index % queenChambers.size());
                    sx = (float) ch.getX(); sy = (float) ch.getY(); sz = (float) ch.getZ();
                } else if (!nestChambers.isEmpty()) {
                    var ch = nestChambers.get(index % nestChambers.size());
                    sx = (float) ch.getX(); sy = (float) ch.getY(); sz = (float) ch.getZ();
                } else if (!simNodes.isEmpty()) {
                    var node = simNodes.stream()
                            .filter(n -> n.type() != TunnelNetwork.ChamberType.ENTRANCE)
                            .findFirst().orElse(simNodes.get(0));
                    sx = node.x(); sy = node.y(); sz = node.z();
                } else {
                    sz = colony.getDynamicQueenChamberDepth();
                }
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.stream()
                        .filter(n -> n.type() != TunnelNetwork.ChamberType.ENTRANCE)
                        .findFirst().orElse(simNodes.get(0));
                sx = node.x(); sy = node.y(); sz = node.z();
            } else {
                sz = colony.getDynamicQueenChamberDepth();
            }

            float rAngle = getRandom().nextFloat(0f, (float) (Math.PI * 2));
            float rDist = getRandom().nextFloat(0.05f, 0.2f);
            sx += (float) Math.cos(rAngle) * rDist;
            sy += (float) Math.sin(rAngle) * rDist;

        } else if (caste == Individual.Caste.NURSE) {
            if (!nurseryChambers.isEmpty()) {
                var ch = nurseryChambers.get(index % nurseryChambers.size());
                float maxR = Math.min(1.2f, Math.max(0.3f, (float) Math.cbrt(ch.getCapacity()) * 0.3f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR * 0.7);
                sx = (float) (ch.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.getY() + Math.sin(rAngle) * rDist);
                sz = (float) ch.getZ();
            } else if (tunnelNetwork != null && tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.BROOD_CHAMBER, nestX, nestY, nestZ) != null) {
                var ch = tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.BROOD_CHAMBER, nestX, nestY, nestZ);
                float maxR = Math.min(1.0f, Math.max(0.2f, ch.radiusX() * 0.7f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR);
                sx = (float) (ch.x() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.y() + Math.sin(rAngle) * rDist);
                sz = ch.z();
            } else if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                sx = (float) ch.getX(); sy = (float) ch.getY(); sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 4.0f;
            }
        } else if (caste == Individual.Caste.FORAGER) {
            boolean spawnOnSurface = (index % 100 < surfaceActivityRatio * 100);
            if (spawnOnSurface) {
                float refX = nestX;
                float refY = nestY;
                if (!entranceChambers.isEmpty()) {
                    var ent = entranceChambers.get(index % entranceChambers.size());
                    refX = (float) ent.getX();
                    refY = (float) ent.getY();
                } else if (tunnelNetwork != null && tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ) != null) {
                    var ent = tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ);
                    refX = ent.x();
                    refY = ent.y();
                }
                float rAngle = getRandom().nextFloat(0f, (float) (Math.PI * 2));
                float rDist = getRandom().nextFloat(0.5f, 3.5f);
                sx = refX + (float) Math.cos(rAngle) * rDist;
                sy = refY + (float) Math.sin(rAngle) * rDist;
                sz = colony.getSurfaceZAt(sx, sy);
            } else if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                sx = (float) ch.getX(); sy = (float) ch.getY(); sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 1.5f;
            }
        } else if (caste == Individual.Caste.WORKER) {
            boolean spawnOnSurface = (job == Individual.Job.FORAGER && (index % 100 < surfaceActivityRatio * 100));
            if (spawnOnSurface) {
                float refX = nestX;
                float refY = nestY;
                if (!entranceChambers.isEmpty()) {
                    var ent = entranceChambers.get(index % entranceChambers.size());
                    refX = (float) ent.getX();
                    refY = (float) ent.getY();
                } else if (tunnelNetwork != null && tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ) != null) {
                    var ent = tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ);
                    refX = ent.x();
                    refY = ent.y();
                }
                float rAngle = getRandom().nextFloat(0f, (float) (Math.PI * 2));
                float rDist = getRandom().nextFloat(0.5f, 3.5f);
                sx = refX + (float) Math.cos(rAngle) * rDist;
                sy = refY + (float) Math.sin(rAngle) * rDist;
                sz = colony.getSurfaceZAt(sx, sy);
            } else if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                float maxR = Math.min(1.2f, Math.max(0.3f, (float) Math.cbrt(ch.getCapacity()) * 0.3f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR * 0.7);
                sx = (float) (ch.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.getY() + Math.sin(rAngle) * rDist);
                sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 3.0f;
            }
        } else if (caste == Individual.Caste.SOLDIER) {
            boolean spawnOnSurface = (index % 100 < (surfaceActivityRatio * 0.75) * 100);
            if (spawnOnSurface) {
                float refX = nestX;
                float refY = nestY;
                if (!entranceChambers.isEmpty()) {
                    var ent = entranceChambers.get(index % entranceChambers.size());
                    refX = (float) ent.getX();
                    refY = (float) ent.getY();
                } else if (tunnelNetwork != null && tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ) != null) {
                    var ent = tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ);
                    refX = ent.x();
                    refY = ent.y();
                }
                float rAngle = getRandom().nextFloat(0f, (float) (Math.PI * 2));
                float rDist = getRandom().nextFloat(0.3f, 2.5f);
                sx = refX + (float) Math.cos(rAngle) * rDist;
                sy = refY + (float) Math.sin(rAngle) * rDist;
                sz = colony.getSurfaceZAt(sx, sy);
            } else if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                float maxR = Math.min(1.2f, Math.max(0.3f, (float) Math.cbrt(ch.getCapacity()) * 0.3f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR * 0.7);
                sx = (float) (ch.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.getY() + Math.sin(rAngle) * rDist);
                sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 2.0f;
            }
        } else if (caste == Individual.Caste.MALE) {
            if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                float maxR = Math.min(1.0f, Math.max(0.3f, (float) Math.cbrt(ch.getCapacity()) * 0.3f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR * 0.7);
                sx = (float) (ch.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.getY() + Math.sin(rAngle) * rDist);
                sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 4.0f;
            }
        }

        sx = Math.max(1.0f, Math.min(mapWidth - 1.0f, sx));
        sy = Math.max(1.0f, Math.min(mapHeight - 1.0f, sy));

        return ensureValidWalkablePos(sx, sy, sz);
    }

    private float[] ensureValidWalkablePos(float sx, float sy, float sz) {
        Terrarium terrarium = colony.getTerrarium();
        if (terrarium == null) return new float[]{sx, sy, sz};

        int cx = Math.round(sx);
        int cy = Math.round(sy);
        int cz = Math.round(sz);

        if (!terrarium.inBounds(cx, cy, cz)) {
            cx = Math.max(0, Math.min(terrarium.getWidth() - 1, cx));
            cy = Math.max(0, Math.min(terrarium.getHeight() - 1, cy));
            cz = Math.max(0, Math.min(terrarium.getDepth() - 1, cz));
        }

        var cell = terrarium.getCell(cx, cy, cz);
        if (cell == null || cell.isPassable()) {
            return new float[]{sx, sy, sz};
        }

        for (int r = 1; r <= 5; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) == r || Math.abs(dy) == r || Math.abs(dz) == r) {
                            int tx = cx + dx;
                            int ty = cy + dy;
                            int tz = cz + dz;
                            if (terrarium.inBounds(tx, ty, tz)) {
                                var tCell = terrarium.getCell(tx, ty, tz);
                                if (tCell != null && tCell.isPassable()) {
                                    return new float[]{(float) tx, (float) ty, (float) tz};
                                }
                            }
                        }
                    }
                }
            }
        }

        float surfZ = colony.getSurfaceZAt(sx, sy);
        return new float[]{sx, sy, surfZ};
    }

    public int calculateSubterraneanCapacity() {
        Nest nest = colony.getNest();
        TunnelNetwork tunnelNetwork = colony.getTunnelNetwork();
        if (nest != null && nest.getChambers() != null && !nest.getChambers().isEmpty()) {
            int cap = 0;
            for (var chamber : nest.getChambers()) {
                cap += Math.max(50, (int) chamber.getCapacity());
            }
            return Math.max(500, cap);
        }
        if (tunnelNetwork != null && tunnelNetwork.getNodes() != null && !tunnelNetwork.getNodes().isEmpty()) {
            return Math.max(500, tunnelNetwork.getNodes().size() * 50);
        }
        return 500;
    }

    public List<Individual> createWorkers(int count) {
        return createWorkers(count, true);
    }

    public List<Individual> createWorkers(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            Individual.Job job = (i % 2 == 0) ? Individual.Job.FORAGER : Individual.Job.NURSE;
            float[] pos = getSpawningCoordinates(Individual.Caste.WORKER, job, i);

            Individual ind = new Individual(colony.getId(), Individual.Caste.WORKER, pos[0], pos[1], pos[2]);
            ind.setHomePosition(colony.getNestX(), colony.getNestY(), colony.getNestZ());
            ind.setSpecies(colony.getSpecies());
            ind.setJob(job);
            ind.setBrain(new FSMArchitecture());
            if (getRandom() != null) {
                ind.setRandom(new Random(getRandom().nextLong()));
            }
            applyGaussianHealth(ind, false);
            if (applyAgeDistribution) {
                applyGaussianAge(ind, false);
            }
            batch.add(ind);
        }
        colony.addIndividualsBulk(batch);
        return batch;
    }

    public List<Individual> createSoldiers(int count) {
        return createSoldiers(count, true);
    }

    public List<Individual> createSoldiers(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            float[] pos = getSpawningCoordinates(Individual.Caste.SOLDIER, Individual.Job.GUARD, i);

            Individual ind = new Individual(colony.getId(), Individual.Caste.SOLDIER, pos[0], pos[1], pos[2]);
            ind.setHomePosition(colony.getNestX(), colony.getNestY(), colony.getNestZ());
            ind.setSpecies(colony.getSpecies());
            ind.setBrain(new FSMArchitecture());
            if (getRandom() != null) {
                ind.setRandom(new Random(getRandom().nextLong()));
            }
            applyGaussianHealth(ind, false);
            if (applyAgeDistribution) {
                applyGaussianAge(ind, false);
            }
            batch.add(ind);
        }
        colony.addIndividualsBulk(batch);
        return batch;
    }

    public List<Individual> createMales(int count) {
        return createMales(count, true);
    }

    public List<Individual> createMales(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            float[] pos = getSpawningCoordinates(Individual.Caste.MALE, Individual.Job.NONE, i);

            Individual ind = new Individual(colony.getId(), Individual.Caste.MALE, pos[0], pos[1], pos[2]);
            ind.setHomePosition(colony.getNestX(), colony.getNestY(), colony.getNestZ());
            ind.setSpecies(colony.getSpecies());
            ind.setBrain(new FSMArchitecture());
            if (getRandom() != null) {
                ind.setRandom(new Random(getRandom().nextLong()));
            }
            applyGaussianHealth(ind, false);
            if (applyAgeDistribution) {
                applyGaussianAge(ind, false);
            }
            batch.add(ind);
        }
        colony.addIndividualsBulk(batch);
        return batch;
    }

    public List<Individual> createBrood(int count) {
        return createBrood(count, true);
    }

    public List<Individual> createBrood(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new ArrayList<>(count);
        TunnelNetwork tunnelNetwork = colony.getTunnelNetwork();
        Terrarium terrarium = colony.getTerrarium();
        Nest nest = colony.getNest();
        float nestX = colony.getNestX();
        float nestY = colony.getNestY();
        float nestZ = colony.getNestZ();

        var broodChamber = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.BROOD_CHAMBER, nestX, nestY, nestZ) : null;
        float surfaceZ = terrarium != null ? terrarium.getSurfaceElevation(nestX, nestY) : 16.0f;
        float defaultUndergroundZ = Math.max(1.0f, surfaceZ - Math.max(1.0f, Math.abs(colony.getDynamicQueenChamberDepth() * 0.5f)));
        if (broodChamber == null && tunnelNetwork != null) {
            broodChamber = tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.QUEEN_CHAMBER, nestX, nestY, nestZ);
        }
        if (broodChamber == null && tunnelNetwork != null && !tunnelNetwork.getNodes().isEmpty()) {
            for (var node : tunnelNetwork.getNodes()) {
                if (node.z() < surfaceZ) {
                    broodChamber = node;
                    break;
                }
            }
        }
        float baseSx = broodChamber != null ? broodChamber.x() : nestX;
        float baseSy = broodChamber != null ? broodChamber.y() : nestY;
        float baseSz = broodChamber != null ? broodChamber.z() : defaultUndergroundZ;

        List<Chamber> nurseryChambers = nest != null ? nest.getChambersOfType(Chamber.Type.NURSERY) : List.of();
        List<Chamber> nestChambers = (nest != null && nest.getChambers() != null) ? nest.getChambers() : List.of();

        for (int i = 0; i < count; i++) {
            float sx = baseSx, sy = baseSy, sz = baseSz;
            if (!nurseryChambers.isEmpty()) {
                Chamber targetChamber = nurseryChambers.get(i % nurseryChambers.size());
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.1, 1.5);
                sx = (float) (targetChamber.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (targetChamber.getY() + Math.sin(rAngle) * rDist);
                sz = targetChamber.getZ();
            } else if (!nestChambers.isEmpty()) {
                Chamber targetChamber = nestChambers.get(i % nestChambers.size());
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.1, 1.5);
                sx = (float) (targetChamber.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (targetChamber.getY() + Math.sin(rAngle) * rDist);
                sz = targetChamber.getZ();
            } else if (broodChamber != null) {
                double rAngle = getRandom().nextDouble(0, Math.PI * 2);
                double rDist = getRandom().nextDouble(0.1, Math.min(1.2, broodChamber.radiusX() * 0.8));
                sx = (float) (broodChamber.x() + Math.cos(rAngle) * rDist);
                sy = (float) (broodChamber.y() + Math.sin(rAngle) * rDist);
                sz = broodChamber.z();
            }

            float antSurfaceZ = terrarium != null ? terrarium.getSurfaceElevation(sx, sy) : 16.0f;
            if (sz >= antSurfaceZ) {
                sz = Math.max(1.0f, antSurfaceZ - Math.max(1.0f, Math.abs(colony.getDynamicQueenChamberDepth() * 0.5f)));
            }

            sx = Math.max(1.0f, Math.min(colony.getMapWidth() - 1.0f, sx));
            sy = Math.max(1.0f, Math.min(colony.getMapHeight() - 1.0f, sy));
            sz = Math.max(1.0f, Math.min(terrarium != null ? terrarium.getDepth() - 1 : 63, sz));

            Individual ind = new Individual(colony.getId(), Individual.Caste.WORKER, sx, sy, sz);
            ind.setSpecies(colony.getSpecies());
            ind.setJob(Individual.Job.NONE);
            applyGaussianHealth(ind, false);

            double r = getRandom().nextDouble();
            var species = colony.getSpecies();
            float eggDur = species != null ? species.getEggStageDuration() : 300f;
            float larvaDur = species != null ? species.getLarvaStageDuration() : 600f;
            float pupaDur = species != null ? species.getPupaStageDuration() : 500f;
            if (eggDur <= 30f) eggDur *= 60.0f;
            if (larvaDur <= 30f) larvaDur *= 60.0f;
            if (pupaDur <= 30f) pupaDur *= 60.0f;

            if (r < 0.35) {
                ind.setLifeStage(Individual.LifeStage.EGG);
                ind.setMaturationThreshold(eggDur);
                if (applyAgeDistribution) {
                    ind.setAgeInSeconds((float) (getRandom().nextDouble(0, eggDur * 0.9)));
                }
            } else if (r < 0.75) {
                ind.setLifeStage(Individual.LifeStage.LARVA);
                ind.setMaturationThreshold(eggDur + larvaDur);
                if (applyAgeDistribution) {
                    ind.setAgeInSeconds((float) (eggDur + getRandom().nextDouble(0, larvaDur * 0.9)));
                }
            } else {
                ind.setLifeStage(Individual.LifeStage.PUPA);
                ind.setMaturationThreshold(eggDur + larvaDur + pupaDur);
                if (applyAgeDistribution) {
                    ind.setAgeInSeconds((float) (eggDur + larvaDur + getRandom().nextDouble(0, pupaDur * 0.9)));
                }
            }
            batch.add(ind);
        }
        colony.addIndividualsBulk(batch);
        return batch;
    }

    public Individual spawnUnit(CasteTemplate template) {
        if (colony.getProteinStored() >= template.getProteinCost() &&
                colony.getCarbohydrateStored() >= template.getCarbohydrateCost() &&
                colony.getWaterStored() >= template.getWaterCost()) {

            colony.setProteinStored(colony.getProteinStored() - template.getProteinCost());
            colony.setCarbohydrateStored(colony.getCarbohydrateStored() - template.getCarbohydrateCost());
            colony.setWaterStored(colony.getWaterStored() - template.getWaterCost());

            Individual ind = new Individual(colony.getId(), template, colony.getNestX(), colony.getNestY(), colony.getNestZ());
            ind.setSpecies(colony.getSpecies());
            if (template != null && template.getDecisionArchitectureType() != null) {
                ind.setBrain(ReasoningArchitecture.create(template.getDecisionArchitectureType()));
            } else {
                ind.setBrain(new FSMArchitecture());
            }
            applyGaussianHealth(ind, false);
            colony.addIndividual(ind);
            return ind;
        }
        return null;
    }
}
