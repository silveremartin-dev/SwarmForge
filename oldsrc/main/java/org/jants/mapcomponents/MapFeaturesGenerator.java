/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants.mapcomponents;

import org.jants.TerrariumCell;
import org.jants.Species;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class MapFeaturesGenerator {

    //more than one event available at the same time
    //https://stackoverflow.com/questions/14295469/what-does-mean-pipe-equal-operator
    public final static int eventNone = 0; // not an event
    public final static int eventRain = 1; // is the same than 1<<1 or 10 in binary
    public final static int eventHeat = 2; //should start with the lowest temperature (250 ?) and use intensity to change between low (: glacial) and high temparature (: hot)
    public final static int eventWind = 4; // is the same than 1<<3 or 1000 in binary
    public final static int eventWindDirection = 8;
    public final static int eventDaylight = 16;

    public final static int eventProbabilityNull = 0;
    public final static int eventProbabilityVeryLow = 1;
    public final static int eventProbabilityLow = 2;
    public final static int eventProbabilityMedium = 3;
    public final static int eventProbabilityHigh = 4;
    public final static int eventProbabilityVeryHigh = 5;

    public final static int eventLevelNoIntensity = 0;
    public final static int eventLevelVeryLowIntensity = 1;
    public final static int eventLevelLowIntensity = 2;
    public final static int eventLevelMediumIntensity = 3;
    public final static int eventLevelHighIntensity = 4;
    public final static int eventLevelVeryHighIntensity = 5;

    public final static int eventDurationExtremelyShort = -3; //may be one single step
    public final static int eventDurationVeryShort = -2; //may be 5 steps
    public final static int eventDurationShort = -1; //may be 25 steps
    public final static int eventDurationNormal = 0; //may be 125 steps
    public final static int eventDurationLong = 1; //may be 625 steps
    public final static int eventDurationVeryLong = 2; //may be 3125 steps
    public final static int eventDurationExtremelyLong = 3; //may be 15625 steps

    private MapFeaturesGenerator() {
        super();
    }

    //add or remove patches of preys, grass, rocks, trees, holes, water
    //change daylight, wind...
    public void init(int[][] cellTypesNumbers, int events, int[] eventProbabilities, Map<Species,Integer> speciesNumbers) {
        //trees, grass, enemies and preys go on heightmap only
        //rock, sand, water and holes can be on or under the heightmap
        Set<TerrariumCell> cells;

    }

    //add or remove patches of preys, grass, rocks, trees, holes, water
    //change daylight, wind...
    public void changeAtRuntime(int[][] cellTypesOccurrences, int events, int[] eventsLevel, int[] eventsDuration, Map<Species,Integer> speciesOccurrences) {
        temperature = 287;
        humidity = 0.2;
        winddirection = 0;
        windforce = 0.1;
        rainfall = 0;
        daylight = 30000;

        occurrences useful ? rather use probailities ?
    }

    public void generateNextEvent() {

    }

    //from XML file or maybe json
    public void loadFeatures() {

    }

    //to XML file or maybe json
    public void saveFeatures() {

    }

    // for a single event
    public static int getEventType (@NotNull String eventType) {
        return switch (eventType) {
            case "None" -> eventNone;
            case "Rain" -> eventRain;
            case "Heat" -> eventHeat;
            case "Wind" -> eventWind;
            case "WindDirection" -> eventWindDirection;
            case "Daylight" -> eventDaylight;
            default -> throw new IllegalArgumentException("Event type unknown.");
        };
    }

    public static int getEventProbability (@NotNull String eventProbability) {
        return switch (eventProbability) {
            case "Null" -> eventProbabilityNull;
            case "VeryLow" -> eventProbabilityVeryLow;
            case "Low" -> eventProbabilityLow;
            case "Medium" -> eventProbabilityMedium;
            case "High" -> eventProbabilityHigh;
            case "VeryHigh" -> eventProbabilityVeryHigh;
            default -> throw new IllegalArgumentException("Event probability unknown.");
        };
    }

    public static int getEventIntensity (@NotNull String eventIntensity) {
        return switch (eventIntensity) {
            case "No" -> eventLevelNoIntensity;
            case "VeryLow" -> eventLevelVeryLowIntensity;
            case "Low" -> eventLevelLowIntensity;
            case "Medium" -> eventLevelMediumIntensity;
            case "High" -> eventLevelHighIntensity;
            case "VeryHigh" -> eventLevelVeryHighIntensity;
            default -> throw new IllegalArgumentException("Event intensity unknown.");
        };
    }

    public static int getEventDuration (@NotNull String eventDuration) {
        return switch (eventDuration) {
            case "ExtremelyShort" -> eventDurationExtremelyShort;
            case "VeryShort" -> eventDurationVeryShort;
            case "Short" -> eventDurationShort;
            case "Normal" -> eventDurationNormal;
            case "Long" -> eventDurationLong;
            case "VeryLong" -> eventDurationVeryLong;
            case "ExtremelyLong" -> eventDurationExtremelyLong;
            default -> throw new IllegalArgumentException("Event duration unknown.");
        };
    }

}
