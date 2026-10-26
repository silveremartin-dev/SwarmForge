package org.swarmforge.core.behavior.jit;

/**
 * Functional contract for a dynamically compiled, unboxed behavior evaluation routine.
 *
 * Guaranteed 100% deterministic, zero GC, executing on primitive IEEE 754 registers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
@FunctionalInterface
public interface JitCompiledBehavior {

    /**
     * Evaluates agent behavior transition logic on unboxed primitives without object allocation.
     *
     * @param currentState Current FSM state ordinal (0 to 14)
     * @param energy Current energy level [0.0f, 1.0f]
     * @param hunger Current hunger level [0.0f, 100.0f]
     * @param health Current health level [0.0f, 100.0f]
     * @param isAtNest 1 if within nest chamber boundary, 0 if in outside foraging domain
     * @param hasFood 1 if carrying food item, 0 otherwise
     * @param alarmPhero Current local alarm pheromone concentration [0.0f, 1.0f]
     * @param deterministicSeed Deterministic 64-bit seed derived from (tick, entityId)
     * @return Packed 64-bit long: (nextStateOrdinal << 32) | (actionTypeOrdinal)
     */
    long evaluate(int currentState, float energy, float hunger, float health,
                  int isAtNest, int hasFood, float alarmPhero, long deterministicSeed);
}
