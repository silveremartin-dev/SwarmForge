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
    // Weather System Snapshot State
    private final String weatherStateName;
    private final float timeOfDay;
    private final float temperature;
    private final float humidity;
    private final float rainfall;

    /**
     * Colony state snapshot.
     */
    public record ColonySnapshot(
            String id,
            String speciesName,
            float nestX, float nestY, float nestZ,
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
            Individual.LifeStage lifeStage,
            Individual.AiState state,
            Individual.CarriedItem carriedItem) implements Serializable {
    }

    private SimulationSnapshot(long tick, List<ColonySnapshot> colonies, byte[] pheromoneData, int dayOfYear,
                               String weatherStateName, float timeOfDay, float temperature, float humidity, float rainfall) {
        this.tick = tick;
        this.timestamp = System.currentTimeMillis();
        this.colonies = colonies;
        this.pheromoneData = pheromoneData;
        this.dayOfYear = dayOfYear;
        this.weatherStateName = weatherStateName;
        this.timeOfDay = timeOfDay;
        this.temperature = temperature;
        this.humidity = humidity;
        this.rainfall = rainfall;
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
                        ind.getLifeStage(),
                        ind.getState(),
                        ind.getCarriedItem()));
            }

            String spName = colony.getSpeciesName();
            if (spName == null && colony.getSpecies() != null) {
                spName = colony.getSpecies().getScientificName();
            }
            if (spName == null) spName = "Lasius niger";

            colonySnapshots.add(new ColonySnapshot(
                    colony.getId().toString(),
                    spName,
                    colony.getNestX(),
                    colony.getNestY(),
                    colony.getNestZ(),
                    colony.getFoodStored(),
                    colony.getWaterStored(),
                    colony.getPopulation(),
                    indSnapshots));
        }

        org.swarmforge.core.world.WeatherSystem weather = simulation.getWeather();
        String wStateName = (weather != null && weather.getWeatherState() != null) ? weather.getWeatherState().name() : "SUNNY";
        float tOfDay = weather != null ? weather.getTimeOfDay() : 12.0f;
        float temp = weather != null ? weather.getTemperature() : 20.0f;
        float hum = weather != null ? weather.getHumidity() : 50.0f;
        float rain = weather != null ? weather.getRainfall() : 0.0f;

        return new SimulationSnapshot(
                simulation.getTickCount(),
                colonySnapshots,
                simulation.getPheromoneGrid() != null ? simulation.getPheromoneGrid().serialize() : new byte[0],
                simulation.getSeasonManager() != null ? simulation.getSeasonManager().getDayOfYear() : 0,
                wStateName, tOfDay, temp, hum, rain);
    }

    /**
     * Restore simulation to this snapshot state.
     */
    public void restore(Simulation simulation) {
        simulation.reset(tick);

        for (ColonySnapshot cs : colonies) {
            String spName = cs.speciesName() != null ? cs.speciesName() : "Lasius niger";
            org.swarmforge.core.species.Species species = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies(spName);

            Colony colony = new Colony(species, cs.nestX(), cs.nestY(), cs.nestZ());
            if (cs.id() != null && !cs.id().isEmpty()) {
                try {
                    java.lang.reflect.Field idField = Colony.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(colony, java.util.UUID.fromString(cs.id()));
                } catch (Exception ignored) {}
            }

            colony.setFoodStored(cs.foodStored());
            colony.setWaterStored(cs.waterStored());
            if (simulation.getTerrarium() != null) {
                colony.setTerrarium(simulation.getTerrarium());
                colony.setMapBounds(simulation.getTerrarium().getWidth(), simulation.getTerrarium().getHeight());
            }

            // recreate individuals
            for (IndividualSnapshot is : cs.individuals()) {
                Individual ind = new Individual(colony.getId(), is.caste(), is.x(), is.y(), is.z());
                ind.setSpecies(species);
                ind.setPosition(is.x(), is.y(), is.z());
                ind.setHeading(is.heading());
                ind.setHealth(is.health());
                ind.setEnergy(is.energy());
                ind.setHunger(is.hunger());
                if (is.lifeStage() != null) {
                    ind.setLifeStage(is.lifeStage());
                }
                ind.setState(is.state());
                ind.setCarriedItem(is.carriedItem());

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

        // Restore Weather System State
        org.swarmforge.core.world.WeatherSystem weather = simulation.getWeather();
        if (weather != null) {
            if (weatherStateName != null) {
                try {
                    org.swarmforge.core.world.WeatherMarkovChain.WeatherState state =
                            org.swarmforge.core.world.WeatherMarkovChain.WeatherState.valueOf(weatherStateName);
                    weather.triggerClimateEvent(state);
                } catch (Exception ignored) {}
            }
            weather.setTimeOfDay(timeOfDay);
            weather.setTemperature(temperature);
            weather.setHumidity(humidity);
            weather.setRainfall(rainfall);
            weather.setDayOfYear(dayOfYear);
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
    * Serialize to compressed bytes (GZIP).
    */
    public byte[] toCompressedBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(baos);
             ObjectOutputStream oos = new ObjectOutputStream(gzos)) {
            oos.writeObject(this);
            oos.flush();
        }
        return baos.toByteArray();
    }

    /**
    * Deserialize from compressed bytes (GZIP).
    */
    public static SimulationSnapshot fromCompressedBytes(byte[] compressedData) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
        try (java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(bais);
             ObjectInputStream ois = new ObjectInputStream(gzis)) {
            return (SimulationSnapshot) ois.readObject();
        }
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
