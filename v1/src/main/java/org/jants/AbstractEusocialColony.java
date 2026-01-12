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
 *  WITHOUT WARRIndividualIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants;


import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// basically a set of Individuals sharing same scent from Individuals queen(s)
// parasites and commensals are not considered part of the colony but as wandering individuals
public abstract class AbstractEusocialColony implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final static int individualDead = 0;
    protected final static int individualLiving = 1;

    private int expectedMaxSize; // the maximum colony size after which the queen won't lay more eggs until some Individuals die, it is recommended that you put a value higher than the actual expected maximum as to prevent this artificial mechanism (used for performance reasons) from triggering as it may produce a bias in the simulation
    private AbstractEusocialndividual[] IndividualsArray;
    private int[] IndividualsLivingArray;

    public AbstractEusocialColony(int expectedMaxSize) {
        if (expectedMaxSize > 0) {
            this.expectedMaxSize = expectedMaxSize;
            IndividualsArray = new AbstractEusocialndividual[expectedMaxSize];
            IndividualsLivingArray = new int[expectedMaxSize];
            for (int i = 0; i< expectedMaxSize; i++) {
                IndividualsLivingArray[i] = individualDead;
            }
        } else throw new IllegalArgumentException("Colony size must be a strictly positive integer.");
    }

    public int getExpectedMaxSize() {
        return expectedMaxSize;
    }

    //very costly operation, avoid calling this method
    public Set<AbstractEusocialndividual> getIndividuals() {
        Set<AbstractEusocialndividual> resultIndividuals;
        AbstractEusocialndividual[] tempIndividualsArray;
        tempIndividualsArray = new AbstractEusocialndividual[expectedMaxSize];
        int currentIndividual = 0;
        for (int i = 0; i < expectedMaxSize; i++) {
            if (IndividualsLivingArray[i] == individualLiving) {
                tempIndividualsArray[currentIndividual] = IndividualsArray[i];
                currentIndividual++;
            }
        }
        resultIndividuals = new HashSet<>();
        resultIndividuals.addAll(List.of(tempIndividualsArray));
        return resultIndividuals;
    }

    public int getColonySize() {
        int result;
        result = 0;
        for (int i = 0; i < expectedMaxSize; i++) {
            if (IndividualsLivingArray[i] == individualLiving) {
                result++;
            }
        }
        return result;
    }

    //very costly operation, avoid calling this method
    public Set<AbstractEusocialndividual> getIndividualsBySex(int sex) {
        Set<AbstractEusocialndividual> resultIndividuals;
        AbstractEusocialndividual[] tempIndividualsArray;
        tempIndividualsArray = new AbstractEusocialndividual[expectedMaxSize];
        int currentIndividual = 0;
        for (int i = 0; i < expectedMaxSize; i++) {
            if (IndividualsLivingArray[i] == individualLiving && IndividualsArray[i].sex == sex ) {
                tempIndividualsArray[currentIndividual] = IndividualsArray[i];
                currentIndividual++;
            }
        }
        resultIndividuals = new HashSet<>();
        resultIndividuals.addAll(List.of(tempIndividualsArray));
        return resultIndividuals;
    }

}
