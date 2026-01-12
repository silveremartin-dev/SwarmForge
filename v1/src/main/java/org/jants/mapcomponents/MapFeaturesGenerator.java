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

    public final static int eventLevelNoProbability = 0;
    public final static int eventLevelVeryLowProbability = 1;
    public final static int eventLevelLowProbability = 2;
    public final static int eventLevelMediumProbability = 3;
    public final static int eventLevelHighProbability = 4;
    public final static int eventLevelVeryHighProbability = 5;

    public final static int eventLevelNoIntensity = 0;
    public final static int eventLevelVeryLowIntensity = 1;
    public final static int eventLevelLowIntensity = 2;
    public final static int eventLevelMediumIntensity = 3;
    public final static int eventLevelHighIntensity = 4;
    public final static int eventLevelVeryHighIntensity = 5;

    public final static int eventDurationExtremelyShort = -3;
    public final static int eventDurationVeryShort = -2;
    public final static int occurrenceShort = -1;
    public final static int eventDurationNormal = 0;
    public final static int eventDurationLong = 1;
    public final static int eventDurationVeryLong = 2;
    public final static int eventDurationExtremelyLong = 3;

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

    //from XML file
    public void loadFeatures() {

    }

    //to XML file
    public void saveFeatures() {

    }

}
