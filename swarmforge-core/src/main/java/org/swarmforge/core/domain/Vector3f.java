package org.swarmforge.core.domain;

/**
 * High-performance immutable 3D Cartesian vector record for spatial simulation math.
 *
 * @param x Coordinate along X axis (meters).
 * @param y Coordinate along Y axis (meters).
 * @param z Coordinate along Z axis (meters).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public record Vector3f(float x, float y, float z) {

    public static final Vector3f ZERO = new Vector3f(0.0f, 0.0f, 0.0f);
    public static final Vector3f UP = new Vector3f(0.0f, 0.0f, 1.0f);

    public Vector3f add(Vector3f o) {
        return new Vector3f(this.x + o.x, this.y + o.y, this.z + o.z);
    }

    public Vector3f add(float dx, float dy, float dz) {
        return new Vector3f(this.x + dx, this.y + dy, this.z + dz);
    }

    public Vector3f subtract(Vector3f o) {
        return new Vector3f(this.x - o.x, this.y - o.y, this.z - o.z);
    }

    public Vector3f scale(float scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public float dot(Vector3f o) {
        return this.x * o.x + this.y * o.y + this.z * o.z;
    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public float distanceSquared(Vector3f o) {
        float dx = this.x - o.x;
        float dy = this.y - o.y;
        float dz = this.z - o.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float distance(Vector3f o) {
        return (float) Math.sqrt(distanceSquared(o));
    }

    public Vector3f normalize() {
        float len = length();
        if (len > 1e-6f) {
            return scale(1.0f / len);
        }
        return ZERO;
    }
}
