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

import org.jants.util.Morton3D;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

// for the sake of the simulation, coordinates outside the map are considered unreachable like if the ants were in a big terrarium
// coordinates inside the terrarium are stored by an int in each direction from the center of the terrarium
// in each dimension, the index corresponds to (xsizex2/cellsize)
public class Terrarium implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static double defaultCellSize; // in meters

    //point of reference is the center of the map which is oriented top to the north
    public final double latitude; // "standard" google earth latitude: World Geodetic System WGS84 standard
    public final double longitude; // "standard" google earth longitude: World Geodetic System WGS84 standard
    public final double altitude;  // "standard" google earth altitude, in meters
    public final double xSize; // in meters from west to east
    public final double ySize; // in meters from north to south
    public final double zSize; // in meters from top to bottom, used by cells

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

    public final double cellSize;

    private final Morton3D mortonEncoder;
    public final Map<Long, TerrariumCell> cells; // cells, which contain the actual features of the environment

    // living elements
    public Set<AbstractEusocialColony> colonies; // ant individuals
    public Set<AbstractIndividual> wanderingLife; // other species individuals: neutral, preys, enemies dead bodies

    private static ResourceBundle jAntsResourceBundle;

    static {
        jAntsResourceBundle = ResourceBundle.getBundle("JAnts");
        defaultCellSize = Double.valueOf(jAntsResourceBundle.getString("CellSize"));
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
        mortonEncoder = new Morton3D();
        cells = new HashMap<>();
        colonies = new HashSet<>();
        wanderingLife = new HashSet<>();
    }

    // there is no check on the x, y, z values which can be out of terrarium xsize, ysize, zsize
    // x, y, z of the cell are changed accordingly
    public void putCell(int x, int y, int z, @NotNull TerrariumCell cell) {
        long mortonCode = mortonEncoder.encode(x, y, z);
        cell.x = x;
        cell.y = y;
        cell.z = z;
        cells.put(mortonCode, cell);
    }

    // there is no check on the x, y, z values which can be out of terrarium xsize, ysize, zsize
    // if null is returned that means that the cell is unassigned
    // TODO if null, return a TerrariumCell.contentsAir or TerrariumCell.contentsRock depending on the location
    public TerrariumCell getCell(int x, int y, int z) {
        long mortonCode = mortonEncoder.encode(x, y, z);
        return cells.get(mortonCode);
    }

    // there is no check on the x, y, z values which can be out of terrarium xsize, ysize, zsize
    public void removeCell(int x, int y, int z) {
        long mortonCode = mortonEncoder.encode(x, y, z);
        cells.remove(mortonCode);
    }

    // with check that the value is between bounds
    public double getXIndex(double x) {
        if (Math.abs(x) <= xSize)
            return Math.floor((x+xSize)/cellSize);
        else
            throw new IllegalArgumentException("x value outside terrarium bounds");
    }

    // checks if the value is between bounds
    public boolean isInsideXBounds(double x) {
        return (Math.abs(x) <= xSize);
    }

    // with check that the value is between bounds
    public double getYIndex(double y) {
        if (Math.abs(y) <= ySize)
            return Math.floor((y+ySize)/cellSize);
        else
            throw new IllegalArgumentException("y value outside terrarium bounds");
    }

    // checks if the value is between bounds
    public boolean isInsideYBounds(double y) {
        return (Math.abs(y) <= ySize);
    }

    // with check that the value is between bounds
    public double getZIndex(double z) {
        if (Math.abs(z) <= zSize)
            return Math.floor((z+zSize)/cellSize);
        else
            throw new IllegalArgumentException("z value outside terrarium bounds");
    }

    // checks if the value is between bounds
    public boolean isInsideZBounds(double z) {
        return (Math.abs(z) <= zSize);
    }

}
