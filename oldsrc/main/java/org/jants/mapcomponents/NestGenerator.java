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

/*
L-system encodes these instructions:
L = rotate left by random angle underneath the surface
R = rotate right by random angle underneath the surface
F = move forward (from 0 to MAX_LENGTH) at present heading
[] =  save/load states
B = build a chamber based on probability

axiom : 'F'
rules : F->initial string

Misc:
1: probability of chamber building decreases as the depth increases
2: the interval of rotation reduces as probability of chamber building decreases
3: execution of L-system string ends when chamber building probability is below 10%

adapted after https://github.com/alecstem/ant-lsystem

See also http://algorithmicbotany.org/papers/#abop
 */

import org.jants.TerrariumCell;

import java.util.Set;
import java.util.Stack;

public class NestGenerator {

    // see examples at https://commons.wikimedia.org/wiki/Category:Ant_nests

    //external features

    //soil based
    public final static int nestTypeGroundCampsite = 1; //not a mound
    public final static int nestTypeGroundCylinder = 2;
    public final static int nestTypeGroundTurret = 3;
    public final static int nestTypeGroundCone = 4
    public final static int nestTypeGroundConeWithPit = 5;
    public final static int nestTypeGroundDome = 6;
    public final static int nestTypeGroundLabyrinth = 7;
    public final static int nestTypeGroundHole = 8;
    public final static int nestTypeGroundPit = 9;

    //wood based
    public final static int nestTypeTreeBall = 21;
    public final static int nestTypeTreeHole = 22;
    public final static int nestTypeTreeLeaf = 23;

    //other
    public final static int nestTypeShell = 40;

    //internal features

    //see directory samplenests for actual samples
    public final static int nestArchitectureDeep = 1;
    public final static int nestArchitectureLarge = 2;
    public final static int nestArchitectureSpaced = 4;
    public final static int nestArchitectureConnected = 8;
    public final static int nestArchitectureLevelled = 16;  // is the same than 1<<4 or 1000 in binary
    public final static int nestArchitectureOrganized = 32; // like if it had been planned with some core and peripheral structures

    //chambers may change status from time to time
    //not all nests contain all chambers sorts
    //chambers normally consists of many "connected" cells
    public final static int nestContentsEntry = 1;
    public final static int nestContentsVent = 2;
    public final static int nestContentsTunnel = 3;
    public final static int nestContentsChamberUnfinished = 11;
    public final static int nestContentsChamberEmpty = 12;
    public final static int nestContentsChamberAbandoned = 13;
    public final static int nestContentsChamberDump = 14; // trash/refuse or may be toilets https://journals.plos.org/plosone/article?id=10.1371/journal.pone.0118376 though this may need different content type after all
    public final static int nestContentsChamberLeafs = 15;
    public final static int nestContentsChamberFungus = 16;
    public final static int nestContentsChamberNursery = 17; //brood
    public final static int nestContentsChamberQueen = 18;

    private NestGenerator() {
        super();
    }

    public Set<TerrariumCell> getNest(int nestType, int nestArchitecture, int numChambers) {
        Set<TerrariumCell> cells = switch (nestType) {
            case nestTypeGroundCampsite -> generateNest("");
            case nestTypeGroundCylinder -> generateNest("");
            case nestTypeGroundTurret -> generateNest("");
            case nestTypeGroundCone -> generateNest("");
            case nestTypeGroundConeWithPit -> generateNest("");
            case nestTypeGroundDome -> generateNest("");
            case nestTypeGroundLabyrinth -> generateNest("");
            case nestTypeGroundHole -> generateNest("");
            case nestTypeGroundPit -> generateNest("");
            case nestTypeTreeBall -> generateNest("");
            case nestTypeTreeHole -> generateNest("");
            case nestTypeTreeLeaf -> generateNest("");
            case nestTypeShell -> generateNest("");
            default -> {
                throw new IllegalArgumentException("nestType is not supported.");
            }
        };

        return cells;
    }

    private final static int maximumLength = 30; // Defines the maximum length of shafts between chambers
    private final static int axiomIterations = 5;// How many times the rules of the L-system are applied.
    private final static int maximumChamberSize = 5;// Defines the maximum radius of each chamber.
    private float probability = 100;// Probability of a chamber being built
    private float probabilityDecay = 0.999;// The amount that the probability decreases after every iteration.

    private float a = (float)-Math.PI;
    private float b = (float)Math.PI;

    /*
    Try [LF[RF]BLF]RRFBLFB
    Some other examples of l-system strings that look decent:
    [LFRFB]RRFBLFB
    [LF[RF]BLF]RRFBLFB
    [[RRFB]LFBRFB]FBRFBLFB
    [RFB]LFBLFBRFB
    [RFB][LFBLFBRFB]LFBLFB[LLFB]RFB
    */
    private Set<TerrariumCell> generateNest(String lsystem) {

        int xSize = 100;
        int ySize = 100;
        StringBuilder tmp = new StringBuilder();

        for (int i = 0; i < axiomIterations; i++) {
            for (int j = 0; j < lsystem.length(); j++) {
                if (lsystem.charAt(j) != 'F') {
                    tmp.append(lsystem.charAt(j));
                } else {
                    tmp.append(lsystem);
                }
            }
        }

        readSystem(new Nest(xSize), tmp.toString());
    }

    /*
    Implementation of DDA (Digital differential analyzer line generating algorithm.) c/o GeeksForGeeks
    https://www.geeksforgeeks.org/dda-line-generation-algorithm-computer-graphics/
    */
    private void DDA(int x0, int x1, int y0, int y1) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int steps = Math.abs(dx) > Math.abs(dy) ? Math.abs(dx) : Math.abs(dy);
        float xInc = dx / steps;
        float yInc = dy / steps;
        float x = x0;
        float y = y0;
        for (int i = 0; i <= steps; i++) {
            FillConsoleOutputCharacter(hOutput, '#', 1, {x, y}, & dwWritten); // https://www.tenouk.com/ModuleS1.html
            x += xInc;
            y += yInc;
        }
    }

    private float changeHeading(Nest nest, char direction) {
        float newHeading;
        float helper = 100.0f - probability;
        if (direction == 'L') {
            newHeading = nest.heading + (Math.random() * (a / 2 - nest.heading));
        } else {
            newHeading = nest.heading + (Math.random() * (b / 2 - nest.heading));
        }
        return newHeading;
    }

    private void buildChamber(Nest nest) {
        int chamberLength = (int) (5 + Math.random() * maximumChamberSize);
        int t = (int) (2 + Math.random() * 3);
        while (chamberLength >= 3 && t > 0) {
            DDA(nest.x, nest.x + chamberLength * Math.sin(-Math.PI / 2), nest.y, nest.y + chamberLength * Math.cos(-Math.PI / 2));
            DDA(nest.x, nest.x + chamberLength * Math.sin(Math.PI / 2), nest.y, nest.y + chamberLength * Math.cos(Math.PI / 2));
            nest.y += 1;
            chamberLength -= 2;
            t--;
        }
    }

    private void readSystem(Nest nest, String lsystem) {
        Stack<Nest> s;
        for (int i = 0; i < lsystem.length(); i++) {
            int length = (int) (Math.random() * maximumLength);
            int prob_r = (int) (Math.random() * 100);
            switch (lsystem[i]) {
                case 'L':
                    nest.heading = changeHeading(nest, 'L');
                    break;
                case 'R':
                    nest.heading = changeHeading(nest, 'R');
                    break;
                case 'F':
                    DDA(nest.x, nest.x + length * Math.sin(nest.heading), nest.y, nest.y + length * Math.cos(nest.heading));
                    nest.x += length * Math.sin(nest.heading);
                    nest.y += length * Math.cos(nest.heading);
                    break;
                case '[':
                    s.push(nest);
                    break;
                case ']':
                    nest.heading = s.top().heading;
                    nest.x = s.peek().x;
                    nest.y = s.peek().y;
                    s.pop();
                    break;
                case 'B':
                    if (prob_r <= probability)
                        buildChamber(nest);
                    break;
                default:
                    break;
            }
            probability *= probabilityDecay;
            a += 0.001;
            b -= 0.001;
            if (probability < 10.00f) {
                break;
            }
        }
    }

    private class Nest {
        public float heading;
        public int x, y;

        public Nest(int xSize) {
            heading = 0;
            x = xSize / 2;
            y = 0;
        }
    }

}