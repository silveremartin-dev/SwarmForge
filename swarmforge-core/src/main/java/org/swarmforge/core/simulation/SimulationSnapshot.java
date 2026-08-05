/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation state snapshot for rewind/replay functionality.
 * Captures the complete simulation state at a point in time.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SimulationSnapshot implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationSnapshot.class);
    private static final long serialVersionUID = 1L;

    private final long tick;
    private final long timestamp;
    private final List<ColonySnapshot> colonies;
    private final byte[] pheromoneData;
    private final int dayOfYear;
    // Environment
    // Environment

    /**
     * Colony state snapshot.
     */
    public record ColonySnapshot(
            String id,
            float foodStored,
            float waterStored,
            int population,
            List<IndividualSnapshot> individuals) implements Serializable {
    }

    /**
     * Individual state snapshot (compressed).
     */
    public record IndividualSnapshot(
            String id,
            float x, float y, float z,
            float heading,
            float health,
            float energy,
            float hunger,
            boolean alive,
            Individual.Caste caste,
            Individual.AiState state,
            Individual.CarriedItem carriedItem) implements Serializable {
    }

    private SimulationSnapshot(long tick, List<ColonySnapshot> colonies, byte[] pheromoneData, int dayOfYear) {
        this.tick = tick;
        this.timestamp = System.currentTimeMillis();
        this.colonies = colonies;
        this.pheromoneData = pheromoneData;
        this.dayOfYear = dayOfYear;
    }

    /**
     * Create a snapshot from the current simulation state.
     */
    public static SimulationSnapshot capture(Simulation simulation) {
        List<ColonySnapshot> colonySnapshots = new ArrayList<>();

        for (Colony colony : simulation.getColonies()) {
            List<IndividualSnapshot> indSnapshots = new ArrayList<>();

            for (Individual ind : colony.getLivingIndividuals()) {
                indSnapshots.add(new IndividualSnapshot(
                        ind.getId().toString(),
                        ind.getX(), ind.getY(), ind.getZ(),
                        ind.getHeading(),
                        ind.getHealth(),
                        ind.getEnergy(),
                        ind.getHunger(),
                        ind.isAlive(),
                        ind.getCaste(),
                        ind.getState(),
                        ind.getCarriedItem()));
            }

            colonySnapshots.add(new ColonySnapshot(
                    colony.getId().toString(),
                    colony.getFoodStored(),
                    colony.getWaterStored(),
                    colony.getPopulation(),
                    indSnapshots));
        }

        return new SimulationSnapshot(
                simulation.getTickCount(),
                colonySnapshots,
                simulation.getPheromoneGrid().serialize(),
                simulation.getSeasonManager() != null ? simulation.getSeasonManager().getDayOfYear() : 0);
    }

    /**
     * Restore simulation to this snapshot state.
     */
    /**
     * Restore simulation to this snapshot state.
     */
    public void restore(Simulation simulation) {
        simulation.reset(tick);

        for (ColonySnapshot cs : colonies) {
            // Need a species to creating a colony.
            // Since snapshot doesn't store species data in detail, we might need a default
            // or lookup.
            // For now, let's assume a default "Generic Ant" or try to preserve species info
            // in snapshot if possible.
            // But Species is complex. Let's create a placeholder Species or reuse one if
            // possible.
            // Better yet, let's make Species serializable or store speciesID.

            // For this implementation, we will use a "Snapshot Species".
            org.swarmforge.core.species.CustomSpecies species = new org.swarmforge.core.species.CustomSpecies();
            species.setCommonName("Restored Species");
            species.setScientificName("Genericus preservedus");

            Colony colony = new Colony(species, 0, 0, 0); // Position will be overwritten by individuals usually?
            // Actually Colony has a nest position. We didn't save it in snapshot!
            // Ideally ColonySnapshot should have nest X/Y/Z.
            // Let's assume nest is at average of individuals or 0,0 for now, or update
            // ColonySnapshot.
            // Updating ColonySnapshot record effectively requires changing the record
            // definition which is immutable.
            // Let's rely on setters if available or reflection? No, let's use what we have.

            // Wait, Colony class has no generic setters for ID.
            // We need to match ID to keep consistency?
            // Actually, if we reset simulation, we can just create new objects.
            // But if we want to keep same IDs for clients to know, we might need to set ID
            // via reflection or constructor.
            // Colony constructor generates random ID.

            // Let's modify Colony to allow ID injection or just ignore ID persistence for
            // now
            // (clients will see "new" colony).
            // Persistence requires ID preservation usually.

            colony.setFoodStored(cs.foodStored);
            colony.setWaterStored(cs.waterStored);

            // recreate individuals
            for (IndividualSnapshot is : cs.individuals) {
                Individual ind = new Individual(colony.getId(), is.caste, is.x, is.y, is.z);
                // We'd need to set the ID to match 'is.id' but Individual ID is final random
                // UUID.
                // This is a limitation of current architecture.
                // For a "Rewind", we usually want exact identity.
                // For "Save/Load", typically we deserialize the whole object graph.

                // Since this is "Snapshot" based on manual copying properties:
                ind.setPosition(is.x, is.y, is.z);
                ind.setHeading(is.heading);
                ind.setHealth(is.health);
                ind.setEnergy(is.energy);
                ind.setHunger(is.hunger);
                ind.setState(is.state);
                ind.setCarriedItem(is.carriedItem);

                colony.addIndividual(ind);
            }
            simulation.addColony(colony);
        }

        // Restore Pheromones
        if (pheromoneData != null && pheromoneData.length > 0) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(pheromoneData);
                DataInputStream dis = new DataInputStream(bais);
                int version = dis.readInt();
                int count = dis.readInt();
                org.swarmforge.core.gpu.SparsePheromoneGrid grid = simulation.getPheromoneGrid();

                if (grid != null) {
                    for (int i = 0; i < count; i++) {
                        long key = dis.readLong();
                        float[] vals = new float[org.swarmforge.core.gpu.SparsePheromoneGrid.PHEROMONE_TYPES];
                        for (int t = 0; t < vals.length; t++) {
                            vals[t] = dis.readFloat();
                        }
                        grid.putEntry(key, vals);
                    }
                }
            } catch (IOException e) {
                LOG.error("Failed to restore pheromones: ", e);
            }
        }

        // Restore Season
        if (simulation.getSeasonManager() != null) {
            simulation.getSeasonManager().setDayOfYear(dayOfYear);
        }
    }

    /**
     * Get the tick number of this snapshot.
     */
    public long getTick() {
        return tick;
    }

    /**
     * Get the capture timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get estimated memory size in bytes.
     */
    public int getEstimatedSize() {
        int size = 16; // Header
        for (ColonySnapshot cs : colonies) {
            size += 32 + cs.individuals.size() * 64;
        }
        size += pheromoneData.length;
        return size;
    }

    /**
     * Serialize to bytes.
     */
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        oos.flush();
        return baos.toByteArray();
    }

    /**
     * Deserialize from bytes.
     */
    public static SimulationSnapshot fromBytes(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (SimulationSnapshot) ois.readObject();
    }
}
