package org.swarmforge.core.structure;

/**
 * A specific construction task (e.g. dig a voxel at x,y,z).
 */
public class ConstructionTask {

    private final float x, y, z;
    private float progress = 0;
    private final float requiredEffort = 10.0f; // Energy units to complete
    private boolean assigned = false;
    private boolean completed = false;

    public ConstructionTask(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void work(float effort) {
        this.progress += effort;
        if (this.progress >= requiredEffort) {
            this.completed = true;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
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
}
