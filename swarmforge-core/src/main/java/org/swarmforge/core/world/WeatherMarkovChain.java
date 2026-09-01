/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import java.util.Random;

/**
 * Markov Chain State Machine for Discrete Weather Condition Transitions.
 * Computes dynamic transition matrices derived from barometric pressure tendencies,
 * temperature, humidity, and monthly climatological precipitations.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherMarkovChain {

    public enum WeatherState {
        SUNNY("☀️ Sunny", 1.0f),
        PARTLY_CLOUDY("⛅ Partly Cloudy", 0.95f),
        OVERCAST("☁️ Overcast", 0.85f),
        FOG("🌫 Fog", 0.60f),
        LIGHT_RAIN("🌧 Light Rain", 0.40f),
        HEAVY_RAIN("⛈ Heavy Rain", 0.10f),
        THUNDERSTORM("⚡ Convective Thunderstorm", 0.05f),
        SANDSTORM("🏜️ Sandstorm", 0.0f),
        HAIL("🧊 Hailstorm", 0.02f),
        BLIZZARD("❄️ Blizzard / Snowstorm", 0.0f),
        TEMPEST("🌪️ Tempest / Gale", 0.0f),
        HEATWAVE("🔥 Heatwave", 0.20f),
        DROUGHT("🌵 Drought", 0.70f),
        SNOW("❄️ Snowfall", 0.30f);

        public final String label;
        public final float flightSuitability; // 0.0 to 1.0 multiplier for insect flight

        WeatherState(String label, float flightSuitability) {
            this.label = label;
            this.flightSuitability = flightSuitability;
        }
    }

    private WeatherState currentState = WeatherState.SUNNY;
    private final Random random = new Random(12345);
    private float stateDurationHours = 0f;

    public WeatherState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(WeatherState state) {
        this.currentState = state;
    }

    public float getStateDurationHours() {
        return stateDurationHours;
    }

    /**
     * Advance Markov state based on hours elapsed and atmospheric conditions.
     */
    public WeatherState update(float deltaHours, float temp, float humidity, float windSpeed,
                               float monthlyRainAvg, double pressureTrend, double lat) {
        stateDurationHours += deltaHours;

        // Dynamic transition rate scaled by step duration (deltaHours)
        // Atmospheric stability factor: high pressure trend stabilizes current state,
        // while rapid pressure drops accelerate weather transitions.
        float stabilityFactor = (float) Math.exp(-Math.abs(pressureTrend) * 0.15);
        
        // Base transition probability per hour (varies by weather state volatility)
        float hourlyTransitionChance = switch (currentState) {
            case THUNDERSTORM, HAIL -> 0.85f;    // Highly volatile short-lived events (~0.5-1h)
            case FOG, LIGHT_RAIN -> 0.45f;        // Medium volatility (~0.5-2h, e.g. passing showers)
            case PARTLY_CLOUDY, HEAVY_RAIN -> 0.30f;
            case SUNNY, OVERCAST, DROUGHT -> 0.12f; // Stable conditions that can persist for hours/days
            default -> 0.25f;
        };

        // Modulate transition chance with step duration & stability factor
        float transitionProb = (1.0f - (float) Math.exp(-hourlyTransitionChance * deltaHours)) * (1.0f - 0.5f * stabilityFactor);

        if (random.nextFloat() > transitionProb) {
            return currentState;
        }

        // Calculate state transition probability matrix
        float rainProb = Math.min(0.8f, monthlyRainAvg / 250.0f);
        boolean isFreezing = temp <= 0;
        boolean isAridDesert = (monthlyRainAvg < 15 && humidity < 30);

        // Barometric pressure tendency effect: falling pressure pushes towards storm/rain
        float pressureDropFactor = (float) Math.max(0.1, 1.0 - pressureTrend * 0.5);

        WeatherState nextState = currentState;

        switch (currentState) {
            case SUNNY -> {
                if (isAridDesert && windSpeed > 35) {
                    if (random.nextFloat() < 0.3f) nextState = WeatherState.SANDSTORM;
                } else if (random.nextFloat() < 0.25f * pressureDropFactor) {
                    nextState = WeatherState.PARTLY_CLOUDY;
                }
            }
            case PARTLY_CLOUDY -> {
                float roll = random.nextFloat();
                if (roll < 0.3f) {
                    nextState = WeatherState.SUNNY;
                } else if (roll < 0.7f * pressureDropFactor) {
                    nextState = WeatherState.OVERCAST;
                } else if (humidity > 85 && Math.abs(lat) > 40 && random.nextFloat() < 0.2f) {
                    nextState = WeatherState.FOG;
                }
            }
            case OVERCAST -> {
                float roll = random.nextFloat();
                if (roll < 0.3f) {
                    nextState = WeatherState.PARTLY_CLOUDY;
                } else if (roll < 0.3f + rainProb * pressureDropFactor) {
                    if (isFreezing) {
                        nextState = WeatherState.SNOW;
                    } else if (temp > 25 && humidity > 75 && random.nextFloat() < 0.4f) {
                        nextState = WeatherState.THUNDERSTORM;
                    } else {
                        nextState = WeatherState.LIGHT_RAIN;
                    }
                }
            }
            case FOG -> {
                if (temp > 15 || windSpeed > 15 || random.nextFloat() < 0.4f) {
                    nextState = WeatherState.PARTLY_CLOUDY;
                }
            }
            case LIGHT_RAIN -> {
                float roll = random.nextFloat();
                if (roll < 0.4f) {
                    nextState = WeatherState.OVERCAST;
                } else if (roll < 0.7f * pressureDropFactor) {
                    nextState = WeatherState.HEAVY_RAIN;
                }
            }
            case HEAVY_RAIN -> {
                float roll = random.nextFloat();
                if (roll < 0.5f) {
                    nextState = WeatherState.LIGHT_RAIN;
                } else if (temp > 22 && random.nextFloat() < 0.3f) {
                    nextState = WeatherState.THUNDERSTORM;
                }
            }
            case THUNDERSTORM -> {
                if (random.nextFloat() < 0.6f) {
                    nextState = WeatherState.HEAVY_RAIN;
                }
            }
            case SANDSTORM -> {
                if (windSpeed < 20 || random.nextFloat() < 0.5f) {
                    nextState = WeatherState.SUNNY;
                }
            }
            case SNOW -> {
                if (temp > 2 || random.nextFloat() < 0.4f) {
                    nextState = WeatherState.OVERCAST;
                }
            }
        }

        if (nextState != currentState) {
            currentState = nextState;
            stateDurationHours = 0f;
        }

        return currentState;
    }
}
