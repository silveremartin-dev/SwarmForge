package org.swarmforge.core.domain;

/**
 * Listener for colony events.
 */
public interface ColonyListener {
    void onBirth(Colony colony, Individual individual);

    void onDeath(Colony colony, Individual individual);
}
