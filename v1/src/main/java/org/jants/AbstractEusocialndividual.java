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

import java.util.Date;
import java.util.HashMap;

public abstract class AbstractEusocialndividual extends AbstractIndividual {

    private static final long serialVersionUID = 1L;

    //sex / caste
    public static final int sexWorker = 1; //female neutral ant, worker
    public static final int sexSoldier = 2;  //female neutral ant, soldier
    public static final int sexRepletes = 3; //female neutral ant, repletes, plerergates or rotund: In Camponotus inflatus in Australia, repletes formed 49% (516 ants) of a colony of 1063 ants, and 46% (1835 ants) of a colony of 4019 ants. The smaller colony contained six wingless queens. The larger colony had 66 chambers containing repletes, with a maximum of 191 repletes in a chamber. The largest replete was 15 millimetres long and had a mass of 1.4 grams. The nest had a maximum depth of 1.7 metres, and tunnels stretched 2.4 metres from the nest entrance. The workers went out foraging during daylight to collect nectar from Mulga nectaries, and meat from the carcass of a Tiliqua blue-tongued lizard.[8]
    public static final int sexErgatoidQueen = 4; //female ant, fed to become a queen, https://en.wikipedia.org/wiki/Ergatoid
    public static final int sexQueen = 5; //female ant, fed to become a queen
    public static final int sexErgatoidMale = 6; //male ant, unfertilized egg, https://en.wikipedia.org/wiki/Ergatoid
    public static final int sexMale = 7; //male ant, unfertilized egg, drones

    public static final int sexKing = 8; //male termite, fed to become a king

    //stage
    public static final int stageEgg = 1; //egg
    public static final int stageNymph = 2; //nymph (for termites)
    public static final int stageLarva = 2; //larva
    public static final int stagePupae = 3; //pupae
    public static final int stageAdult = 4; //adult
    public static final int stageDead= 5; //dead

    private static int[] stageTransitions;

    //body parts
    //values are powers of two and can be used in combination
    public static final int bodyHead = 1; // is the same than 1<<0 or 1 in binary
    public static final int bodyAntennas = 2;  // is the same than 1<<1 or 10 in binary
    public static final int bodyWings = 4;  // is the same than 1<<2 or 100 in binary
    public static final int bodyThorax = 8; // is the same than 1<<3 or 1000 in binary
    public static final int bodyAbdomen = 16; //
    public static final int bodyFrontLeftLeg = 32; //
    public static final int bodyFrontRightLeg = 64; //
    public static final int bodyMiddleLeftLeg = 128; //
    public static final int bodyMiddleRightLeg = 256; //
    public static final int getBodyBackRightLeg = 512; //
    public static final int bodyBackRightLeg = 1024; //

    //ants immediate behaviors to achieve a drive, a global task
    public static final int behaviorStand = 1; //don't move, guard
    public static final int behaviorIngest = 11;//feed of drink
    public static final int behaviorDefecate = 12; //poop
    public static final int behaviorTake = 21; //to transport mineral, vegetal, animal, waste, including cutting leaves
    public static final int behaviorDrop = 22; //eventually to create a dump, pile leaves, dig nest
    public static final int behaviorWalk = 31; //for example following a scent
    public static final int behaviorRun = 32; // fleeing danger, some ants can run up to 85,5 cm/s (3,1 km/h), may be on walls or ceiling
    public static final int behaviorFly = 33; //for winged males and queens only
    public static final int behaviorDry = 41; //heat the area so that humidity drops
    public static final int behaviorCool = 42; //refresh area so that temperature drops, rather in bees but may be ants have a way
    public static final int behaviorAttack = 51; //throw acid, bite
    public static final int behaviorMate = 61; //for winged males and queens only
    public static final int behaviorLay = 62; //queen only (though some females workers may also lay eggs under some special circumstances)
    public static final int behaviorChain = 63; //connect to other ants as forming a swarm or a bridge

    // active action of antenna, body language, sound, scent
    // reception of the message is automatic assuming proximity (which may be different depending on the stimulus)
    // https://www.antkeepers.com/facts/ants/communication/
    // TODO revise this list
    public static final int behaviorCommunicationAlert = 101; // warn
    public static final int behaviorCommunicationEntice = 102;
    public static final int behaviorCommunicationRecruit = 103; // to food sources or new nest locations
    public static final int behaviorCommunicationGrooming = 104; // the cleaning and tending to other ants
    public static final int behaviorCommunicationTrophallaxis = 105; // the exchange of liquids, orally/anally
    public static final int behaviorCommunicationExchangeOfSolidFood = 106;
    public static final int behaviorCommunicationPeerPressure = 107;
    public static final int behaviorCommunicationRecognition = 108;  // members of the colony, determine caste, telling apart dead or living ants
    public static final int behaviorCommunicationInfluencingCastes = 109; // stimulating or preventing the development of different castes
    public static final int behaviorCommunicationControllingRivals = 110;  // other fertile females of the same nest
    public static final int behaviorCommunicationMarkingTerritories = 111;  // distance to the colony, marking of territorial borders
    public static final int behaviorCommunicationSexualCommunication = 112;  // determining species and genders as well as synchronising the nuptial flight

    public static final int behaviorEmitScent = 121; // especially for queen
    public static final int behaviorMatchScent = 122; // try to match its scent to the one from ants around

    // global tasks, drives
    public static final int taskCleanSelf = 2; //
    public static final int taskFeedSelf = 3; //

    public static final int taskProtect = 11; //stand but alert: guard or fight, real or ritualized (encounters between workers last for 15 seconds)
    public static final int taskPatrol = 12; //move in or around the nest, maintain the frontier
    public static final int taskGatherFood = 13; //
    public static final int taskGatherLeaves = 14; // to feed commensals or fungus to be grown on
    public static final int taskGatherFungus = 15; // to be grown inside on leaves
    public static final int taskGatherCommensals = 16; // to be grown inside on leaves

    public static final int taskMaintainNest = 21; //dig, close or consolidate tunnels, plug nest ; stitch and weave leaves with glue silk (https://en.wikipedia.org/wiki/Weaver_ant)
    public static final int taskCultivate = 22; // needs leaves, fungus to rais
    public static final int taskRaiseEggs = 23; // move, feed them, needs food
    public static final int taskFeedOthers = 24; // feed adults, needs food

    public static final int taskClean = 31; // move things to dump

    public static final int taskGroomOthers = 41; // espaeially the queen
    public static final int taskCommunicate = 42; //relay messages for collective intelligence, emit and match scent

    public static final int taskMate = 51; // for winged males and queens only
    public static final int taskProcreate = 52; // queen only (though some females workers may also lay eggs under some special circumstances)

    public static final int scentUndefined = -1;

    public int sex;
    public int age; //from 0 to somewhere about 3600 * 24 * 365 * 3 for workers, 3600 * 24 * 365 * 5 for males and 3600 * 24 * 365 * 25 for queen
    public int stage;
    public int scentId; // personal scent id, normally the one from the queen, which is the pass to entrance in the nest

    //as ants carry in their mouth only one thing can be carried at a time, either in carriedCellContents or carriedIndividual

    public int carriedCellContents; // what is actually carried if any : sand, water, mud, ant (living), food, trash (may be a dead ant)
    public AbstractIndividual carriedIndividual; //only valid if carriedCellContents = AbstractCell.contentsAir, most of the time an egg or a commensal

    public AbstractEusocialndividual(int sex, int stage, int scentId, int x, int y, int z) {

        this.sex = sex;
        age = 0;
        this.stage = stage;
        this.scentId = scentId;

        int[] massAndDimensions = getMassAndDimensionsFromSexAndStage(sex, stage);
        mass = massAndDimensions[0];
        xSize = massAndDimensions[1];
        ySize = massAndDimensions[2];
        zSize = massAndDimensions[3];
        stageTransitions = getStageTransitionsFromSex(sex);

        birthDate = new Date();
        deathDate = null;

        health = mass;
        diseases = new HashMap();
        injuries = new int[10]; //corresponding to the 10 body parts
        for (int i=0; i< injuries.length; i++) {
            injuries[i] = injuryNone;
        }
        behavior = behaviorStand;
        task = taskRest;

        fatigue = 100;
        thirst = 100;
        hunger = 100;

        bodyTemperature = 293;

        this.x = x;
        this.y = y;
        this.z = z;

        carriedCellContents = TerrariumCell.contentsAir;
        carriedIndividual = null;
    }

    // actual ants dimension may still vary, for example first ants produced by a fresh queen are smaller than regular individuals
    // The average weight of an ant differs dependent on the types from 1 to 150 milligrams. Our normal black and red forest ants weigh about 5-7 milligrams.
    // The lighter ants are the known domestic parasites of the Pharaoh and some species of tropical ants, such as the small ones. The mass of the worker caste ant of these species is 1-2 milligrams.
    // And one of the largest ant species is the South American ant and the African wandering ants. In the first case, a functioning ant weighs up to 90 milligrams, while in the latter the uterus during the period of sedentary life is robust and can weigh up to 10 grams!
    //returns a int[4] with mass, x, y, z
    public static int[] getMassAndDimensionsFromSexAndStage(int sex, int stage) {
        int[] result;
        result = new int[4];
        switch (sex) {
            case sexWorker : switch (stage) {
                case stageEgg : return new int[4] { 0.002, , ,};
                case stageLarva : return new int[4] { 0.004, , ,};
                case stagePupae : return new int[4] { 0.006, , ,};
                case stageAdult : return new int[4] { 0.008, 0.75, ,};
            }
            case sexSoldier : switch (stage) {
                case stageEgg : return new int[4] { , , ,};
                case stageLarva : return new int[4] { , , ,};
                case stagePupae : return new int[4] { , , ,};
                case stageAdult : return new int[4] { 0.01, , ,};
            }
            case sexRepletes : switch (stage) {
                case stageEgg : return new int[4] { , , ,};
                case stageLarva : return new int[4] { , , ,};
                case stagePupae : return new int[4] { , , ,};
                case stageAdult : return new int[4] { , , ,};
            }
            case sexErgatoidQueen : switch (stage) {
                case stageEgg : return new int[4] { , , ,};
                case stageLarva : return new int[4] { , , ,};
                case stagePupae : return new int[4] { , , ,};
                case stageAdult : return new int[4] { , , ,};
            }
            case sexQueen : switch (stage) {
                case stageEgg : return new int[4] { , , ,};
                case stageLarva : return new int[4] { , , ,};
                case stagePupae : return new int[4] { , , ,};
                case stageAdult : return new int[4] { 1, 52, ,};
            }
            case sexErgatoidMale : switch (stage) {
                case stageEgg : return new int[4] { , , ,};
                case stageLarva : return new int[4] { , , ,};
                case stagePupae : return new int[4] { , , ,};
                case stageAdult : return new int[4] { , , ,};
            }
            case sexMale : switch (stage) {
                case stageEgg : return new int[4] { , , ,};
                case stageLarva : return new int[4] { , , ,};
                case stagePupae : return new int[4] { , , ,};
                case stageAdult : return new int[4] { , , ,};
            }
        }
        return result;
    }

    //actual values may vary
    //returns a int[4] with transition from stage 1 to 2, 2 to 3, 3 to 4, 4 to 5
    //values in days
    public static int[] getStageTransitionsFromSex(int sex) {
        int[] result;
        result = new int[4];
        switch (sex) {
            case sexWorker : return new int[4] {14, 28, 56, 365};
            case sexSoldier : return new int[4] {14, 28, 56, 365};
            case sexRepletes : return new int[4] {14, 28, 56, 365};
            case sexErgatoidQueen : return new int[4] {14, 28, 56, 365 * 5};
            case sexQueen : return new int[4] {14, 28, 56, 365 * 20};
            case sexErgatoidMale :  return new int[4] {14, 28, 56, 365};
            case sexMale : return new int[4] {14, 28, 56, 66};
        }
        return result;
    }

    // returns a int[] corresponding to the day between stage change from day 0
    public int[] getStageTransitionValues() {
        return stageTransitions;
    }

    public void setStageTransitionValues(int[] stageTransitions) {
        if (stageTransitions.length == 4) {
            this.stageTransitions = stageTransitions;
        } else throw new IllegalArgumentException("StageTransition array must be of length 4.");
    }

    // probabilistic behavior ? pure deterministic ? base on energy maximization ?
    // example : As a short-term response, Acromyrmex lundii workers are known to relocate brood and fungus to nest chambers with CO2 levels ranging from 1 to 3% [5,6]. Workers also use CO2 as an orientation cue during nest excavation, avoiding levels of 4% and preferring places with 1% CO2 for digging
    // https://en.wikipedia.org/wiki/Bees_algorithm
    // https://en.wikipedia.org/wiki/Task_allocation_and_partitioning_in_social_insects
    // https://en.wikipedia.org/wiki/Patterns_of_self-organization_in_ants
    // The brain has a volume of about one one-thousandth of a cubic millimeter, and may contain around 250,000 neurons https://en.wikipedia.org/wiki/List_of_animals_by_number_of_neurons
    public int behave() {
        //change age and eventually die
        //dead bodies are kept for a while (possibliy being moved to a dump area), after which their id is recycled
        //change size according to stage
        //move to free cells perhaps using polarized light in the sky, the Earth’s magnetic field, wind direction, a step counter, panoramic “snapshots” of landmarks
        //avoid poison
        //update disease levels and injuries
        //all other internal parameters
        //sense, plan, act : chose appropriate behavior from current drives, tasks, environment, etc.
        //cannot move to a full cell, cannot drop there either
        //cannot dig air nor rock
        //again if fighting or positioned
        //change hunger and thirst level and fatigue
        //laying eggs actually produces new individuals
        //sample complex tasks : glue leaves with commensals, enslave other ants, carry in commensals to raise and milk them, grow fungus on leaves, maintain nest...
        //an enemy cannot be carried
        //only one thing carried at a time
        return taskRest;
    }

}