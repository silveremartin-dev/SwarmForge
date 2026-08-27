/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import java.util.*;

/**
 * Realistic Weather & Atmospheric Physics Engine for SwarmForge.
 * Features 12-month climate profiles, Perlin continuous micro-fluctuations,
 * Markov Chain weather state transitions, Soil Thermal Inertia phase lag,
 * Barometric pressure tendency, Hydric coupling, and In-Silico Eusocial Insect biological impact indicators.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherSystem {

    private final Random random = new Random(42);
    private final WeatherMarkovChain markovChain = new WeatherMarkovChain();
    private final SoilHydricCoupling hydricCoupling = new SoilHydricCoupling(32);

    // Geographic Coordinates & Atmospheric Attributes
    private double latitude = 48.8;  // °N
    private double longitude = 2.35; // °E
    private double altitude = 100.0; // m
    private double basePressure = 1013.25; // hPa
    private String windDirection = "SW";
    private double soilInertiaDays = 3.0;
    private double depthAttenuation = 0.85;

    // Monthly Climate Curves (12 Months)
    private final double[] tempMin = new double[]{ -1, 0, 4, 9, 14, 17, 19, 18, 14, 9, 4, 0 };
    private final double[] tempAvg = new double[]{ 3, 5, 9, 14, 19, 22, 25, 24, 20, 14, 8, 4 };
    private final double[] tempMax = new double[]{ 7, 9, 14, 19, 24, 27, 30, 29, 25, 19, 12, 8 };

    private final double[] windMin = new double[]{ 10, 12, 14, 12, 10, 8, 7, 7, 8, 10, 12, 12 };
    private final double[] windAvg = new double[]{ 20, 22, 25, 22, 18, 16, 15, 15, 17, 20, 22, 22 };
    private final double[] windMax = new double[]{ 45, 50, 55, 45, 40, 35, 35, 35, 40, 45, 50, 50 };

    private final double[] rainAvg = new double[]{ 55, 45, 50, 55, 65, 55, 50, 55, 65, 75, 65, 60 };
    private final double[] humidityAvg = new double[]{ 78, 74, 68, 62, 63, 64, 65, 68, 73, 79, 82, 81 };

    // Dynamic Atmospheric Real-Time State (SI Units)
    private float currentTemp = 15.0f; // °C (SI metric)
    private float currentHumidity = 65.0f; // % RH
    private float currentWindSpeed = 15.0f; // km/h (SI: convert to m/s = windSpeed / 3.6)
    private float currentRainfall = 0.0f; // mm/h
    private float currentSnowfall = 0.0f; // mm/h
    private float currentPressure = 1013.25f; // hPa (SI: 101325 Pa)
    private float magneticField = 48.0f; // µT (Microtesla SI geomagnetic field)
    private float pressureTrend = 0.0f; // dP/dt
    private boolean isDaytime = true;

    // Simulation Time
    private int dayOfYear = 180; // 1 to 365
    private float timeOfDayHours = 12.0f; // 0.0 to 24.0

    // Modifiers
    private float tempOffset = 0f;
    private float rainMultiplier = 1f;

    public WeatherSystem(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        updateAtmosphere(0f);
    }

    /**
     * Apply a complete climate profile configuration map from WeatherEditorPane or JSON preset.
     */
    public void applyClimateProfile(Map<String, Object> config) {
        if (config == null) return;

        if (config.containsKey("latitude")) this.latitude = ((Number) config.get("latitude")).doubleValue();
        if (config.containsKey("longitude")) this.longitude = ((Number) config.get("longitude")).doubleValue();
        if (config.containsKey("altitude")) this.altitude = ((Number) config.get("altitude")).doubleValue();
        if (config.containsKey("basePressure")) this.basePressure = ((Number) config.get("basePressure")).doubleValue();
        if (config.containsKey("windDirection")) this.windDirection = (String) config.get("windDirection");
        if (config.containsKey("soilInertiaDays")) this.soilInertiaDays = ((Number) config.get("soilInertiaDays")).doubleValue();
        if (config.containsKey("depthAttenuation")) this.depthAttenuation = ((Number) config.get("depthAttenuation")).doubleValue();

        copyArray(config, "tempMin", tempMin);
        copyArray(config, "tempAvg", tempAvg);
        copyArray(config, "tempMax", tempMax);

        copyArray(config, "windMin", windMin);
        copyArray(config, "windAvg", windAvg);
        copyArray(config, "windMax", windMax);

        copyArray(config, "rainAvg", rainAvg);
        copyArray(config, "humidityAvg", humidityAvg);

        updateAtmosphere(0f);
    }

    @SuppressWarnings("unchecked")
    private void copyArray(Map<String, Object> cfg, String key, double[] target) {
        if (cfg.containsKey(key) && cfg.get(key) instanceof List) {
            List<Number> list = (List<Number>) cfg.get(key);
            for (int i = 0; i < Math.min(12, list.size()); i++) {
                target[i] = list.get(i).doubleValue();
            }
        }
    }

    /**
     * Advance simulation time and recalculate physics.
     *
     * @param hours Hours elapsed
     */
    public void advanceTime(float hours) {
        timeOfDayHours += hours;
        while (timeOfDayHours >= 24.0f) {
            timeOfDayHours -= 24.0f;
            dayOfYear = (dayOfYear % 365) + 1;
        }
        updateAtmosphere(hours);
    }

    private void updateAtmosphere(float deltaHours) {
        // 1. Determine active month index & day interpolation factor (0.0 to 1.0)
        double monthFloat = ((dayOfYear - 1) / 365.0) * 12.0;
        int m1 = (int) Math.floor(monthFloat) % 12;
        int m2 = (m1 + 1) % 12;
        double frac = monthFloat - Math.floor(monthFloat);

        // Interpolated baseline values from 12-month curves
        double baseTempAvg = lerp(tempAvg[m1], tempAvg[m2], frac);
        double baseTempMin = lerp(tempMin[m1], tempMin[m2], frac);
        double baseTempMax = lerp(tempMax[m1], tempMax[m2], frac);

        double baseWindAvg = lerp(windAvg[m1], windAvg[m2], frac);
        double baseRainAvg = lerp(rainAvg[m1], rainAvg[m2], frac);
        double baseHumAvg = lerp(humidityAvg[m1], humidityAvg[m2], frac);

        // 2. Diurnal Solar Cycle (Diurnal Amplitude variation)
        double sunAngle = Math.sin((timeOfDayHours - 6.0) * Math.PI / 12.0); // Peak at 14:00
        double diurnalTemp = (baseTempMax - baseTempMin) * 0.5 * sunAngle;

        // 3. Perlin Micro-Fluctuations (Continuous smooth turbulence)
        double timeSeed = (dayOfYear * 24.0 + timeOfDayHours) * 0.05;
        double perlinTempNoise = PerlinNoise.noise(timeSeed, 1.2) * 2.5; // +/- 2.5°C smooth fluctuation
        double perlinWindNoise = PerlinNoise.noise(timeSeed, 5.8) * 10.0; // +/- 10 km/h wind gusts
        double perlinPressNoise = PerlinNoise.noise(timeSeed, 9.4) * 8.0;  // Barometric drift

        // Dynamic Wind Direction evolution based on continuous turbulence & atmospheric drift
        double windAngle = PerlinNoise.noise(timeSeed * 0.1, 15.3) * 360.0;
        if (windAngle < 0) windAngle += 360.0;
        String[] directions = new String[] { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };
        int dirIdx = (int) Math.floor(((windAngle + 22.5) % 360.0) / 45.0);
        this.windDirection = directions[Math.abs(dirIdx) % 8];

        currentTemp = (float) (baseTempAvg + diurnalTemp + perlinTempNoise + tempOffset);

        // Barometric Pressure & Pressure Tendency (dP/dt)
        float targetPressure = (float) (basePressure + perlinPressNoise - (altitude * 0.12));
        pressureTrend = (targetPressure - currentPressure);
        currentPressure = targetPressure;

        // Earth's Geomagnetic Field (µT - Microtesla SI units derived from latitude & altitude)
        double latRad = Math.toRadians(latitude);
        magneticField = (float) (31.0 * Math.sqrt(1.0 + 3.0 * Math.sin(latRad) * Math.sin(latRad)) * Math.exp(-altitude / 100000.0));

        // Markov Chain Discrete Weather State Transition
        WeatherMarkovChain.WeatherState state = markovChain.update(
                deltaHours, currentTemp, currentHumidity, currentWindSpeed, (float) baseRainAvg, pressureTrend, latitude
        );

        // 4. Rainfall, Snowfall, and Humidity derived from Markov State & Curves
        currentSnowfall = 0.0f;
        switch (state) {
            case LIGHT_RAIN -> currentRainfall = (float) Math.max(1.0, baseRainAvg / 40.0) * rainMultiplier;
            case HEAVY_RAIN -> currentRainfall = (float) Math.max(8.0, baseRainAvg / 15.0) * rainMultiplier;
            case THUNDERSTORM -> currentRainfall = (float) Math.max(25.0, baseRainAvg / 5.0) * rainMultiplier;
            case HAIL -> {
                currentRainfall = (float) Math.max(15.0, baseRainAvg / 10.0) * rainMultiplier;
                currentSnowfall = (float) Math.max(5.0, baseRainAvg / 20.0) * rainMultiplier;
            }
            case SNOW -> {
                currentRainfall = 0.0f;
                currentSnowfall = (float) Math.max(2.0, baseRainAvg / 30.0) * rainMultiplier;
            }
            case BLIZZARD -> {
                currentRainfall = 0.0f;
                currentSnowfall = (float) Math.max(15.0, baseRainAvg / 10.0) * rainMultiplier;
            }
            case TEMPEST -> {
                currentRainfall = (float) Math.max(30.0, baseRainAvg / 4.0) * rainMultiplier;
                currentSnowfall = currentTemp <= 0 ? 10.0f : 0.0f;
            }
            default -> {
                currentRainfall = 0.0f;
                currentSnowfall = 0.0f;
            }
        }

        currentWindSpeed = (float) Math.max(0, baseWindAvg + perlinWindNoise +
                (state == WeatherMarkovChain.WeatherState.THUNDERSTORM ? 25 :
                 state == WeatherMarkovChain.WeatherState.TEMPEST || state == WeatherMarkovChain.WeatherState.BLIZZARD ? 45 : 0));
        currentHumidity = (float) Math.max(10, Math.min(100, baseHumAvg + ((currentRainfall > 0 || currentSnowfall > 0) ? 20 : -diurnalTemp * 0.8)));

        // 5. Hydric Coupling & Subterranean Moisture updates
        hydricCoupling.updateHydricAndThermalState(currentRainfall, currentSnowfall, currentTemp, currentWindSpeed, deltaHours);

        // 6. Daylight hours check
        float dayLength = calculateDayLength();
        float sunrise = 12.0f - dayLength * 0.5f;
        float sunset = 12.0f + dayLength * 0.5f;
        isDaytime = (timeOfDayHours >= sunrise && timeOfDayHours <= sunset);
    }

    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private float calculateDayLength() {
        double declination = 23.45 * Math.sin(Math.toRadians((dayOfYear - 81) * 360.0 / 365.0));
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);
        double tanProduct = -Math.tan(latRad) * Math.tan(decRad);
        if (tanProduct >= 1.0) return 0.0f;
        if (tanProduct <= -1.0) return 24.0f;
        double hourAngle = Math.acos(tanProduct);
        return (float) (2.0 * Math.toDegrees(hourAngle) / 15.0);
    }

    // ── Eusocial Insect In-Silico Impact Queries ──────────────────────────────

    /**
     * Can winged insects (foragers, alates, queens) fly in current weather?
     */
    public boolean canInsectsFly() {
        if (currentWindSpeed > 32.0f) return false; // Wind flight barrier
        if (markovChain.getCurrentState() == WeatherMarkovChain.WeatherState.SANDSTORM) return false;
        if (markovChain.getCurrentState() == WeatherMarkovChain.WeatherState.THUNDERSTORM) return false;
        if (currentRainfall > 10.0f) return false;
        return markovChain.getCurrentState().flightSuitability > 0.2f;
    }

    /**
     * Returns foraging activity multiplier (0.0 to 1.0) for colony workers.
     */
    public float getForagingMultiplier() {
        if (!canInsectsFly() && currentRainfall > 5.0f) return 0.05f;
        if (currentTemp < 5.0f || currentTemp > 42.0f) return 0.1f; // Thermal inhibition

        float tempFactor = (currentTemp >= 15.0f && currentTemp <= 32.0f) ? 1.0f : 0.6f;
        float dayFactor = isDaytime ? 1.0f : 0.3f;
        return tempFactor * dayFactor * markovChain.getCurrentState().flightSuitability;
    }

    /**
     * Subterranean gallery temperature taking into account depth attenuation and soil thermal inertia.
     */
    public float getTemperatureAtDepth(int depthCell) {
        float annualAvg = (float) getAvg(tempAvg);
        return hydricCoupling.calculateTemperatureAtDepth(depthCell, currentTemp, annualAvg, soilInertiaDays, depthAttenuation);
    }

    /**
     * Get subterranean soil moisture percentage at specific gallery depth.
     */
    public float getSoilHumidityAtDepth(int depthCell) {
        return hydricCoupling.getMoistureAtDepth(depthCell);
    }

    // ── Getters & Setters (SI Units & Climate Event Controls) ─────────────────

    public float getTemperature() { return currentTemp; }
    /** SI Kelvin temperature */
    public float getTemperatureKelvin() { return currentTemp + 273.15f; }
    public float getHumidity() { return currentHumidity; }
    public float getWindSpeed() { return currentWindSpeed; }
    /** SI Meters per second wind speed */
    public float getWindSpeedMs() { return currentWindSpeed / 3.6f; }
    public String getWindDirection() { return windDirection; }
    public float getWindDirectionAngle() {
        return switch (windDirection) {
            case "N" -> 0.0f;
            case "NE" -> 45.0f;
            case "E" -> 90.0f;
            case "SE" -> 135.0f;
            case "S" -> 180.0f;
            case "SW" -> 225.0f;
            case "W" -> 270.0f;
            case "NW" -> 315.0f;
            default -> 225.0f;
        };
    }
    public float getRainfall() { return currentRainfall; }
    public float getSnowfall() { return currentSnowfall; }
    public float getPressure() { return currentPressure; }
    /** SI Pascal pressure */
    public float getPressurePa() { return currentPressure * 100.0f; }
    /** SI Microtesla geomagnetic field */
    public float getMagneticField() { return magneticField; }

    public WeatherMarkovChain.WeatherState getWeatherState() { return markovChain.getCurrentState(); }
    public String getCurrentWeatherType() { return markovChain != null && markovChain.getCurrentState() != null ? markovChain.getCurrentState().name() : "CLEAR"; }
    public float getRainfallIntensity() { return currentRainfall; }

    /**
     * Manually trigger a climate event (tempest, hail, cloudy, blizzard, thunderstorm, etc.).
     */
    public void triggerClimateEvent(WeatherMarkovChain.WeatherState state) {
        if (state != null) {
            markovChain.setCurrentState(state);
            updateAtmosphere(0f);
        }
    }

    public boolean isDaytime() { return isDaytime; }
    public float getSunAngle() { return timeOfDayHours / 24.0f; }
    public float getLightLevel() { return isDaytime ? 1.0f : 0.15f; }
    public boolean isRaining() { return currentRainfall > 0 || currentSnowfall > 0; }
    public int getDayOfYear() { return dayOfYear; }
    public float getTimeOfDay() { return timeOfDayHours; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitude() { return altitude; }

    public void setTemperatureOffset(float offset) { this.tempOffset = offset; }
    public void setRainMultiplier(float mult) { this.rainMultiplier = mult; }
    public void setTemperature(float t) { this.currentTemp = t; }
    public void setHumidity(float h) { this.currentHumidity = h; }
    public void setWindSpeed(float s) { this.currentWindSpeed = s; }
    public void setRainfall(float r) { this.currentRainfall = r; }

    private double getAvg(double[] arr) {
        double s = 0;
        for (double d : arr) s += d;
        return s / arr.length;
    }
}
