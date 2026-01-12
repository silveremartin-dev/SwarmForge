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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public abstract class AbstractIndividual implements Serializable {

    private static final long serialVersionUID = 1L;

    //disease level
    public static final int diseaseNone = 0; // not a disease
    public static final int diseaseVeryLow = 1; //
    public static final int diseaseLow = 2; //
    public static final int diseaseModerate = 3; //or medium
    public static final int diseaseHigh = 4; //
    public static final int diseaseVeryHigh = 5; //extremely high

    //injury level
    public static final int injuryNone = 0; // healthy part
    public static final int injuryVeryLow = 1; //
    public static final int injuryLow = 2; //
    public static final int injuryModerate = 3; //or medium
    public static final int injuryHigh = 4; //
    public static final int injuryVeryHigh = 5; //extremely high

    public Species species;//should be set only once, at creation time

    //size
    public int xSize;
    public int ySize;
    public int zSize;

    public int mass;

    public Date birthDate;
    public Date deathDate;

    public int health; //from 0 (death) to 100 (perfect health) for worker, 200 for males, 1000 for queen
    public Map diseases; //diseases with progression for each, possibly along with reduced health or mobility
    public int[] injuries; //body parts with injuries, possibly along with reduced health or mobility

    public int behavior;
    public int task;

    public static final int taskRest = 1; //don't move

    public int fatigue; //from 0 tired to 100 fully rested
    public int thirst; //from 0 (death) to 100, values upper than 75 enable to give water to others
    public int hunger; //from 0 (death) to 100, values upper than 75 enable to feed others
    public int bodyTemperature; //from 270 (death) to 325 (death)

    //position
    public int x;
    public int y;
    public int z;

    public abstract int behave();

}
