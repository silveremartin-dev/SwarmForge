package org.swarmforge.core.structure;

/**
 * Represents a functional room within the nest.
 */
public class Chamber implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        QUEEN_QUARTERS,
        NURSERY,
        FOOD_STORAGE,
        WASTE_DUMP,
        ENTRANCE
    }

    private final String id;
    private final Type type;
    private float x, y, z;
    private float capacity;
    private float currentLoad;

    public Chamber(String id, Type type, float x, float y, float z, float capacity) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getCapacity() {
        return capacity;
    }

    public float getCurrentLoad() {
        return currentLoad;
    }

    public void addLoad(float amount) {
        this.currentLoad = Math.min(capacity, currentLoad + amount);
    }

    public void removeLoad(float amount) {
        this.currentLoad = Math.max(0, currentLoad - amount);
    }
}
