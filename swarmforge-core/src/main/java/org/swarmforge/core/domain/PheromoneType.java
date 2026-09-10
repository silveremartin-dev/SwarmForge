package org.swarmforge.core.domain;

/**
 * Defines the different types of pheromones used by ants.
 * Maps meaningful names to indices in the PheromoneGrid.
 */
public enum PheromoneType {
    FOOD_TRAIL(0),
    HOME_TRAIL(1),
    ALARM(2),
    RECRUITMENT(3),
    QUEEN_SCENT(4),
    BROOD_SCENT(5),
    DEATH_SCENT(6),
    TERRITORY(7);

    private final int index;

    PheromoneType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
