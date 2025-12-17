package org.swarmforge.core.structure;

/**
 * Represents a connection between two points in the nest.
 */
public class Tunnel implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final Chamber start;
    private final Chamber end;
    private final float length;

    public Tunnel(Chamber start, Chamber end) {
        this.start = start;
        this.end = end;
        this.length = (float) Math.sqrt(
                Math.pow(end.getX() - start.getX(), 2) +
                        Math.pow(end.getY() - start.getY(), 2) +
                        Math.pow(end.getZ() - start.getZ(), 2));
    }

    public Chamber getStart() {
        return start;
    }

    public Chamber getEnd() {
        return end;
    }

    public float getLength() {
        return length;
    }
}
