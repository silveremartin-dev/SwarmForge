package org.swarmforge.core.ecs.components;

import com.artemis.Component;
import java.util.UUID;

/**
 * Associates an entity with a specific colony.
 */
public class ColonyComponent extends Component {
    public UUID colonyId;
}
