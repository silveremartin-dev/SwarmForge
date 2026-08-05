/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.epidemiology;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.structure.physics.NestVoxelGrid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Social Immunity & Advanced Epidemiology Orchestrator.
 * Implements the 3 Pillars of Eusocial Defense against Epizootics:
 * 1. Allogrooming (Nettoyage mutuel): Mutual spore removal and low-dose social immunization.
 * 2. Propolis Deposition (Résines antiseptiques): Coating nest walls with antiseptic resin to inhibit germination.
 * 3. Necrophorism (Transport des cadavres): Sanitation workers removing sporulating cadavers to external middens.
 */
public class SocialImmunityManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public record Midden(float x, float y, float z, List<IndividualInfection> cadavers) implements Serializable {}

    public record EpizooticReport(
            int susceptibleCount,
            int exposedCount,
            int infectedCount,
            int sporulatingDeadCount,
            int immuneCount,
            int allogroomingEventsCount,
            int propolisCoatedVoxelsCount,
            int cadaversQuarantinedCount
    ) implements Serializable {}

    private final Midden externalMidden;
    private int totalAllogroomingEvents;
    private int totalCadaversQuarantined;

    public SocialImmunityManager(float middenX, float middenY, float middenZ) {
        this.externalMidden = new Midden(middenX, middenY, middenZ, new ArrayList<>());
        this.totalAllogroomingEvents = 0;
        this.totalCadaversQuarantined = 0;
    }

    /**
     * Executes Allogrooming (Mutual Cleaning) between groomer ant and target ant.
     * Removes up to 85% of cuticular spores before germination.
     */
    public boolean performAllogrooming(IndividualInfection groomerInfection,
                                      IndividualInfection targetInfection,
                                      Random rng) {
        if (targetInfection.getState() != InfectionState.EXPOSED) {
            return false;
        }

        // Groomer scrapes off spores
        targetInfection.groomSporeLoad(0.85f);
        totalAllogroomingEvents++;

        // Groomer gains low-level spore exposure building social immunity
        if (targetInfection.getActivePathogen() != null) {
            groomerInfection.exposeToSpores(targetInfection.getActivePathogen(), 0.02f);
        }
        return true;
    }

    /**
     * Applies foraged Propolis Resin (ResourceType.PROPOLIS_RESIN) to a nest voxel cell.
     */
    public boolean applyPropolisCoating(Colony colony, NestVoxelGrid grid, int vx, int vy, int vz) {
        if (colony.getResourceAmount(ResourceType.PROPOLIS_RESIN) < 1.0f) {
            return false;
        }
        NestVoxelGrid.VoxelCell cell = grid.getVoxel(vx, vy, vz);
        if (cell == null) {
            return false;
        }

        colony.consumeResource(ResourceType.PROPOLIS_RESIN, 1.0f);
        cell.setPropolisCoating(Math.min(1.0f, cell.getPropolisCoating() + 0.50f));
        // Antiseptic effect reduces fungal spore load in voxel immediately by 80%
        cell.setFungalSporeLoad(cell.getFungalSporeLoad() * 0.20f);
        return true;
    }

    /**
     * Executes Necrophorism (Corpse Removal): carries a dead sporulating ant to external midden outside the nest.
     */
    public boolean performNecrophorism(Individual sanitationWorker, Individual deadAnt,
                                        IndividualInfection deadAntInfection) {
        if (deadAnt.isAlive() || deadAntInfection.getState() != InfectionState.SPORULATING_DEAD) {
            return false;
        }

        // Pick up dead ant and move to external midden
        sanitationWorker.setCarriedItem(Individual.CarriedItem.DEAD_ANT);
        deadAnt.setPosition(externalMidden.x(), externalMidden.y(), externalMidden.z());
        externalMidden.cadavers().add(deadAntInfection);
        totalCadaversQuarantined++;
        return true;
    }

    /**
     * Main epidemiology simulation step for a colony.
     */
    public EpizooticReport simulateEpidemiologyStep(
            Map<UUID, IndividualInfection> infectionRegistry,
            List<Individual> livingIndividuals,
            NestVoxelGrid grid,
            Random rng) {

        int susceptible = 0, exposed = 0, infected = 0, sporulatingDead = 0, immune = 0;

        for (Individual ind : livingIndividuals) {
            IndividualInfection inf = infectionRegistry.computeIfAbsent(ind.getId(), k -> new IndividualInfection(k));

            // Tick infection lifecycle
            float pathogenResistance = (ind.getHaplodiploidGenome() != null) ? ind.getHaplodiploidGenome().getPathogenResistance() : 1.0f;
            inf.tick(pathogenResistance);

            // Apply pathogen damage to ant health if infected
            if (inf.getState() == InfectionState.INFECTED) {
                boolean died = ind.takeDamage(inf.getActivePathogen().getBaseLethality() * 100.0f);
                if (died) {
                    inf.setState(InfectionState.SPORULATING_DEAD);
                }
            }

            // Environmental voxel infection transmission
            int vx = Math.max(0, Math.min(grid.getWidth() - 1, (int) Math.floor(ind.getX())));
            int vy = Math.max(0, Math.min(grid.getHeight() - 1, (int) Math.floor(ind.getY())));
            int vz = Math.max(0, Math.min(grid.getDepth() - 1, (int) Math.floor(ind.getZ())));

            NestVoxelGrid.VoxelCell cell = grid.getVoxel(vx, vy, vz);
            if (cell != null && cell.getFungalSporeLoad() > 0.1f) {
                // Propolis coating reduces environmental spore germination rate by up to 90%!
                float effectiveSporeLoad = cell.getFungalSporeLoad() * (1.0f - cell.getPropolisCoating() * 0.90f);
                inf.exposeToSpores(PathogenType.BEAUVERIA_BASSIANA, effectiveSporeLoad * 0.1f);
            }

            switch (inf.getState()) {
                case SUSCEPTIBLE -> susceptible++;
                case EXPOSED -> exposed++;
                case INFECTED -> infected++;
                case SPORULATING_DEAD -> sporulatingDead++;
                case IMMUNE -> immune++;
            }
        }

        // Count propolis coated voxels
        int propolisCount = 0;
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                for (int z = 0; z < grid.getDepth(); z++) {
                    NestVoxelGrid.VoxelCell cell = grid.getVoxel(x, y, z);
                    if (cell != null && cell.getPropolisCoating() > 0.05f) {
                        propolisCount++;
                    }
                }
            }
        }

        return new EpizooticReport(
                susceptible, exposed, infected, sporulatingDead, immune,
                totalAllogroomingEvents, propolisCount, totalCadaversQuarantined
        );
    }

    public Midden getExternalMidden() {
        return externalMidden;
    }
}
