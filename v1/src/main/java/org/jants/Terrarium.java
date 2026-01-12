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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

// for the sake of the simulation, coordinates outside the map aare considered unreachable like if the ants were in a big terrarium
public class Terrarium implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static double defaultCellSize; // in meters

    //point of reference is the center of the map which is oriented top to the north
    private final double latitude; // "standard" google earth latitude: World Geodetic System WGS84 standard
    private final double longitude; // "standard" google earth longitude: World Geodetic System WGS84 standard
    private final double altitude;  // "standard" google earth altitude, in meters
    private final double xSize; // in meters from west to east
    private final double ySize; // in meters from north to south
    private final double zSize; // in meters from top to bottom, used by cells

    //changing parameters:
    public double temperature; // in kelvin degrees
    public double humidity; // from 0 to 100 per cent, https://en.wikipedia.org/wiki/Humidity
    public double winddirection; // clockwise from 0 degree north
    public double windforce; // in meters per second
    public double rainfall; // mm per hour
    public double daylight; // luminosity in lux https://en.wikipedia.org/wiki/Daylight, eventually mostly depends on latitude, longitude and date
    public LocalDateTime date; // local date time

    // we could also consider the following:
    // polarized light in the sky
    // the Earth’s magnetic field
    // pressure as some ants may live above 2000 m (80 percent sea level pressure), though this may be deduced from altitude

    public double cellSize;
    // resolution level is dependent of xSize, ySize and heightMap horizontal and vertical size
    //it is recommended that the same scale applies in all dimensions
    public byte[][] heightmap; // https://en.wikipedia.org/wiki/Heightmap
    public Set<TerrariumCell> cells; // cells, which contain the actual features of the environment

    // living elements
    public Set<AbstractEusocialColony> colonies; // ant individuals
    public Set<AbstractIndividual> wanderingLife; // other species individuals: neutral, preys, enemies dead bodies

    private static ResourceBundle jAntsResourceBundle;

    static {
        jAntsResourceBundle = ResourceBundle.getBundle("JAnts");
        defaultCellSize = Double.valueOf(jAntsResourceBundle.getString("LoggerFilePath"));
    }

    public Terrarium(double latitude, double longitude, double altitude, double xSize, double ySize, double zSize) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        temperature = 287;
        humidity = 0.2;
        winddirection = 0;
        windforce = 0.1;
        rainfall = 0;
        daylight = 30000;
        date = null;
        cellSize = defaultCellSize;
        heightmap = null;
        cells = new HashSet<>();
        colonies = new HashSet<>();
        wanderingLife = new HashSet<>();
    }

}
