package org.swarmforge.core.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.simulation.SimulationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FuzzyLogicTest {
    private FuzzyLogicArchitecture architecture;
    private AgentView agent;

    @BeforeEach
    public void setUp() {
        architecture = new FuzzyLogicArchitecture();
        agent = mock(AgentView.class);
    }

    @Test
    public void testDecideRestOnLowEnergy() {
        when(agent.getEnergyLevel()).thenReturn(0.1f); // Low energy
        when(agent.isCarryingFood()).thenReturn(false);
        when(agent.getX()).thenReturn(50f);
        when(agent.getY()).thenReturn(0f);
        when(agent.getZ()).thenReturn(50f);
        when(agent.getHomeX()).thenReturn(50f);
        when(agent.getHomeY()).thenReturn(0f);

        ReasoningArchitecture.Action action = architecture.decide(agent, null);
        assertEquals(ReasoningArchitecture.Action.ActionType.REST, action.type());
    }

    @Test
    public void testDecideForageOnStrongTrail() {
        when(agent.getEnergyLevel()).thenReturn(1.0f); // High energy
        when(agent.isCarryingFood()).thenReturn(false); // Hungry for work
        
        SimulationContext context = mock(SimulationContext.class);
        when(context.getFoodPheromone(anyFloat(), anyFloat(), anyFloat())).thenReturn(0.8f); // Strong trail

        ReasoningArchitecture.Action action = architecture.decide(agent, context);
        assertEquals(ReasoningArchitecture.Action.ActionType.FORAGE, action.type());
    }
}
