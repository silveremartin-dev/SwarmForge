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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class AbstractSimulation extends Thread implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static Duration defaultCycleDuration = Duration.of(1, ChronoUnit.SECONDS);

    private LocalDateTime systemStartDateTime;

    private Duration pausedDuration; // the time spent in pause

    private LocalDateTime simulationStartDateTime;
    private LocalDateTime simulationDateTime;
    private Duration cycleDuration;

    private int cycleNumber;

    private final static int modeRealTime = 1;
    private final static int modeSimulationTime = 2;

    private int mode;

    private final static int simulationEnded = 0; // on not yet initialized
    private final static int simulationInitialized = 1;
    private final static int simulationRunning = 2;
    private final static int simulationPaused = 3;

    private int state;

    private AbstractSimulation() {
        super();
        state = simulationEnded;
        cycleDuration = defaultCycleDuration;
    }

    // the system start datetime
    public LocalDateTime getSystemStartDateTime() {
        return systemStartDateTime;
    }

    // the start datetime in the simulation
    public LocalDateTime getSimulationStartDateTime() {
        return simulationStartDateTime;
    }

    // the system datetime
    public LocalDateTime getSystemDateTime() {
        return LocalDateTime.now();
    }

    // the datetime in the simulation
    public LocalDateTime getSimulationDateTime() {
        return simulationDateTime;
    }

    // the computed time, not the time elapsed in the simulation
    public Duration getSystemRunningTime() {
        return Duration.between(systemStartDateTime, LocalDateTime.now()).minus(pausedDuration);
    }

    // the time elapsed in the simulation
    // as simulation runs in discrete turns, we have to choose the simulated duration between to cycles
    public Duration getSimulationRunningTime() {
        return Duration.between(simulationStartDateTime, simulationDateTime);
    }

    public Duration getCycleDuration() {
        return cycleDuration;
    }

    public void setCycleDuration(@NotNull Duration duration) {
        if (duration != null) {
            this.cycleDuration = cycleDuration;
        } else throw new IllegalArgumentException("Duration cannot be null.");
    }

    // the number of cycles is equal to the simulated duration / cycleDuration, though as CycleDuration may be set multiple times the result may vary accordingly
    public int getSimulatedCycles() {
        return cycleNumber;
    }

    // to be called first
    public void initialize(@NotNull Terrarium abstractMap, @NotNull LocalDateTime simulationDateTime) {
        if (state == simulationEnded) {
            if (abstractMap != null || simulationDateTime != null) {
                pausedDuration = Duration.ZERO;
                cycleNumber = 0;
                abstractMap.setDate(simulationDateTime);
                state = simulationInitialized;
            } else throw new IllegalArgumentException("AbstractMap and simulationDateTime cannot be null.");
        } else throw new RuntimeException("Cannot initialize a running simulation. Call end().");
    }

    //duration is the length of the run before auto stop, negative for no stop
    // computes simulation for some machine time
    public void runForTime(@NotNull Duration duration) {
        if (state == simulationInitialized) {
            if (duration != null) {
                systemStartDateTime = LocalDateTime.now();
                mode = modeRealTime;
                state = simulationRunning;
                while (true) {
                    generateNextCycle();
                }
            } else throw new IllegalArgumentException("Duration cannot be null.");
        } else throw new RuntimeException("Cannot run a simulation running or ended. Call pause().");
    }

    // computes simulation for some simulated time, may actually run faster or slower than machine time
    public void runForSimulationTime(@NotNull Duration duration) {
        if (state == simulationInitialized) {
            if (duration != null) {
                systemStartDateTime = LocalDateTime.now();
                mode = modeSimulationTime;
                state = simulationRunning;
                while (true) {
                    generateNextCycle();
                }
            } else throw new IllegalArgumentException("Duration cannot be null.");
        } else throw new RuntimeException("Cannot run a simulation running or ended. Call pause().");
    }

    //pauses the simulation
    public void pause() {
        if (state == simulationRunning) {
            state = simulationPaused;
        } else throw new RuntimeException("Cannot pause a simulation not running. Call runFor...().");
    }

    // to finalize and free resources
    public void end() {
        if (state == simulationRunning || state == simulationPaused) {
            state = simulationEnded;
        } else throw new RuntimeException("Cannot end a simulation not running or paused. Call initialize().");
    }

    //start all over, whether paused or not, like a call to end and initialize
    public void reset() {
        end();
        initialize( , simulationStartDateTime);
    }

    //go to next cycle
    //Simulation is done using probabilities.
    //For example, should an ant transport something, there is a probability is will take
    //all the contents of the cell or else just carry some part of it. The cell is still
    //considered full in the latter case but the more the action is carried over, the
    //more the likelihood that the cell will become empty.
    public void generateNextCycle() {
        if (mode = modeRealTime) {
            https://stackoverflow.com/questions/6602922/is-it-faster-to-access-final-local-variables-than-class-variables-in-java

        } else {

        }
        //generate disasters: sun heat, humidity, wind, snow, rain or major disaster: fire, water flow, earthquake.
        //update space: new plants, death, enemy raids, gallery collapses

        //update ants newborns, position, death
        //update ants life parameters : age, thirst, hunger, fatigue, diseases, injuries

        //compute behavior through delegation to each ant
        simulationDateTime += cycleDuration;
        cycleNumber++;
    }

}
