package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Component for what the entity is carrying.
 */
public class InventoryComponent extends Component {
    public enum ItemType { NONE, FOOD, WATER, BROOD }
    
    public ItemType carriedItem = ItemType.NONE;
    public float amount = 0f;
}
