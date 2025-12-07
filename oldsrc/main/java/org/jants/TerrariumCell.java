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

package org.jants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TerrariumCell implements Serializable {

    private static final long serialVersionUID = 1L;

    public int x; // x index of the cell in the map, should never be set manually
    public int y; // y index of the cell in the map, should never be set manually
    public int z; // z index of the cell in the map, should never be set manually

    // TODO use or delete
    //x coordinate +1
    public TerrariumCell frontTerrariumCell;
    //x coordinate -1
    public TerrariumCell backTerrariumCell;
    //y coordinate +1
    public TerrariumCell leftTerrariumCell;
    //y coordinate -1
    public TerrariumCell rightTerrariumCell;
    //z coordinate +1
    public TerrariumCell topTerrariumCell;
    //z coordinate -1
    public TerrariumCell bottomTerrariumCell;

    public double temperature; // in kelvin degrees
    public double humidity; // from 0 to 100 per cent, https://en.wikipedia.org/wiki/Humidity
    public double windDirection; // clockwise from 0 degree north
    public double windForce; // in meters per second
    public double daylight; // luminosity in lux https://en.wikipedia.org/wiki/Daylight, eventually mostly depends on latitude, longitude and date

    public double co2Level; // in percent of air composition
    public double nitrousoxideLevel; // in percent of air composition
    public double methaneLevel; // in percent of air composition

    // co2 level
    // https://pubmed.ncbi.nlm.nih.gov/10887903/
    // https://insectessociaux.com/2018/03/28/digging-in-the-deep-how-does-carbon-dioxide-affect-communal-nest-building-in-ants/#:~:text=Subterranean%20ants%20are%20confronted%20with,Roces%202000%3B%20Bollazzi%20et%20al.
    // Subterranean ants are confronted with CO2 concentrations vastly exceeding atmospheric levels (currently ~0.04%), even very close to the soil surface. These levels increase even more with depth so that 5-6 meters underground ants encounter an environment with 6-7% CO2 (Kleineidam and Roces 2000; Bollazzi et al. 2012).

    // nitrous oxide
    // The fact that cows produce a ton of greenhouse gas in the form of methane is science factoid that people tend to pull out in casual climate discussions (you have those, don’t you?). As it turns out, one of the other major natural contributors of greenhouse gas is a creature that is a whole lot smaller but a lot more numerous.
    // Leafcutter ants, which are prevalent in many parts of the continent, do exactly what their name suggests. After trimming chunks of leaves from foliage the ants tend to form piles of the organic matter in order to promote the growth of fungus that they then eat. One of the byproducts of this farming method is huge quantities of nitrous oxide.
    // “You can’t walk through a tropical forest without seeing them stripping leaves, marching in long lines and excavating nests,” researcher Fiona Soper said in a statement. “Our research in Costa Rica shows a previously unsuspected role for these ants in tropical forests: creating emission hotspots for the greenhouse gas nitrous oxide.”
    // The science team surveyed almost two dozen leafcutter ant colonies during a trip to Costa Rica and discovered that these piles of decaying plant matter are capable of producing twice as much nitrous oxide as the surrounding rainforest.

    //methane
    // Levels of methane seeping from the mounds were about 20 times higher than those emanating from the surrounding forest floor. But the big surprise was nitrous oxide, which left the mounds in concentrations 1000 times or more above background levels, the researchers report online today in the Proceedings of the Royal Society B.
    // https://pubmed.ncbi.nlm.nih.gov/10887903/ The maximum concentration within a typical one-chamber ant nest with approximately 200 ants can reach 12.5 times atmospheric concentration, reaching 95% of equilibrium concentrations within 15 min.

    //contents
    public final static int contentsAir = 0;

    public final static int contentsIce = 1;
    public final static int contentsSnow = 2;
    public final static int contentsWater = 3; //water source or rain

    public final static int contentsRock = 11; //hard unmovable rock
    public final static int contentsSand = 12; // pebbles, can but dug out
    public final static int contentsClay = 13; // soft earth, can be dug out and molten

    public final static int contentsWood = 21; //tree or decayed plant, can be easily fired, also used to build up the ant nest : thatch, twig
    public static final int contentsLeaves = 22;
    public static final int contentsFungus = 23;
    public static final int contentsSugar = 24; // nectar
    public static final int contentsFat = 25; // grease, oil, fat, seed
    public static final int contentsMeat = 26; // edible food, dead enemy
    public final static int contentsPoison = 27; // whatever it is: glue...

    public static final int contentsBiologicalWaste = 31; // full cell of it, dead bodies, etc

    public int contents;

    // this is a basic system with grade values; user can prefer to use actual concentration levels in ppm or whatever
    // value fillNone would be meaningless in that context since that would mean the content of the cell is actually switched to air
    // when taking, cell value should decrease ; when dropping, cell filling value should increase
    public final static int fillVeryLow = 1;
    public final static int fillLow = 2;
    public final static int fillMedium = 3;
    public final static int fillHigh = 4;
    public final static int fillVeryHigh = 5;
    public final static int fillFull = 6;

    public int fillLevel;

    //scents
    //food scents
    public static final int scentSugar = 1;
    public static final int scentMeat = 2;
    public static final int scentFat = 3;
    public static final int scentDecay = 4; // possible fungus source
    public static final int scentHumidity = 5; // possible water source or rain

    //danger scents
    public static final int scentPoison = 11; //whatever to avoid
    public static final int scentBattle = 12; //formic acid
    public static final int scentBurnt = 13; //burned or burning
    public static final int scentPoop = 14;
    public static final int scentDump = 15; // fool smell

    //nest scents
    public static final int scentBrood = 21;
    public static final int scentFungus = 22;
    public static final int scentLeaves = 23;
    public static final int scentNest = 24; //actual scent may change depending on who the queen is (or are)

    //gas scents
    public static final int scentCo2 = 31; // high gas level
    public static final int scentMethane = 32; // high gas level
    public static final int scentNitrousOxide = 33; // high gas level

    //additional scents are species (preys and enemies specific species, including other ants species, commensal specific species...) and nest specific (same species, different nest or colony)
    public static final int scentOtherEnemy = 41;
    public static final int scentOtherFriendly = 42;

    // this is a basic system with grade values; usr can prefer to use actual concentration levels in ppm or whatever
    private final static int scentNone = 0;
    private final static int scentVeryLow = 1;
    private final static int scentLow = 2;
    private final static int scentMedium = 3;
    private final static int scentHigh = 4;
    private final static int scentVeryHigh = 5;

    public int[] scents; // kind and concentration/intensity

    public Map<AbstractIndividual, Integer> scentSpecificIndividual;

    public TerrariumCell(int x, int y, int z) {
        temperature = 287;
        humidity = 0.2;
        windDirection = 0;
        windForce = 0.1;
        daylight = 30000;
        co2Level = 0.04;
        nitrousoxideLevel= 0;
        methaneLevel = 0;
        contents = contentsAir;
        fillLevel = fillFull;
        scents = new int[19]; // we should avoid having a fixed size as the array may grow with further scents added
        IntStream.range(0, scents.length).forEach(i -> scents[i] = scentNone);
        scentSpecificIndividual = new HashMap<>();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private int getCellTypeIndex(int cellType) {
        return switch (cellType) {
            case contentsAir -> 0;
            case contentsIce -> 1;
            case contentsSnow -> 2;
            case contentsWater -> 3;
            case contentsRock -> 4;
            case contentsSand -> 5;
            case contentsClay -> 6;
            case contentsWood -> 7;
            case contentsLeaves -> 8;
            case contentsFungus -> 9;
            case contentsSugar -> 10;
            case contentsFat -> 11;
            case contentsMeat -> 12;
            case contentsPoison -> 13;
            case contentsBiologicalWaste -> 14;
            default -> throw new IllegalArgumentException("Cell type unknown.");
        };
    }

    // scentOtherEnemy and scentOtherFriendly need additional characterization using scentSpecificIndividual
    private int getScentTypeIndex(int scentType) {
        return switch (scentType) {
            case scentSugar -> 0;
            case scentMeat -> 1;
            case scentFat -> 2;
            case scentDecay -> 3;
            case scentHumidity -> 4;
            case scentPoison -> 5;
            case scentBattle -> 6;
            case scentBurnt -> 7;
            case scentPoop -> 8;
            case scentDump -> 9;
            case scentBrood -> 10;
            case scentFungus -> 11;
            case scentLeaves -> 12;
            case scentNest -> 13;
            case scentCo2 -> 14;
            case scentMethane -> 15;
            case scentNitrousOxide -> 16;
            case scentOtherEnemy -> 17;
            case scentOtherFriendly -> 18;
            default -> throw new IllegalArgumentException("Scent type unknown.");
        };
    }

}
