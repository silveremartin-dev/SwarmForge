package org.swarmforge.core.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * The physical structure of a colony with Morton 3D spatial indexing support.
 */
public class Nest implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Chamber> chambers = new CopyOnWriteArrayList<>();
    private final List<Tunnel> tunnels = new CopyOnWriteArrayList<>();

    /**
     * Calculates 3D Morton Code (Z-order curve) for spatial indexing.
     */
    public static long morton3D(int x, int y, int z) {
        long lx = Math.max(0, Math.min(1023, x));
        long ly = Math.max(0, Math.min(1023, y));
        long lz = Math.max(0, Math.min(1023, Math.abs(z)));
        return (splitBits(lx)) | (splitBits(ly) << 1) | (splitBits(lz) << 2);
    }

    public static long morton3D(float x, float y, float z) {
        return morton3D(Math.round(x), Math.round(y), Math.round(z));
    }

    private static long splitBits(long a) {
        a = (a | (a << 16)) & 0x00001f00000000ffL;
        a = (a | (a << 8))  & 0x0000100f0000000fL;
        a = (a | (a << 4))  & 0x000010c3000000c3L;
        a = (a | (a << 2))  & 0x0000124900001249L;
        return a;
    }

    public void clear() {
        chambers.clear();
        tunnels.clear();
    }

    public void addChamber(Chamber chamber) {
        if (chamber == null) return;
        chambers.add(chamber);
        sortChambersByMorton();
    }

    public void addTunnel(Tunnel tunnel) {
        if (tunnel == null) return;
        tunnels.add(tunnel);
    }

    public List<Chamber> getChambers() {
        return chambers;
    }

    public List<Tunnel> getTunnels() {
        return tunnels;
    }

    public Chamber findChamberById(String id) {
        if (id == null) return null;
        for (Chamber c : chambers) {
            if (id.equalsIgnoreCase(c.getId())) return c;
        }
        return null;
    }

    public List<Chamber> getChambersOfType(Chamber.Type type) {
        return chambers.stream()
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
    }

    public Chamber findNearestChamberOfType(Chamber.Type type, float x, float y, float z) {
        Chamber nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (Chamber c : chambers) {
            if (c.getType() == type) {
                float dx = c.getX() - x;
                float dy = c.getY() - y;
                float dz = c.getZ() - z;
                float distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = c;
                }
            }
        }
        return nearest;
    }

    public Chamber findNearestChamber(float x, float y, float z) {
        Chamber nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (Chamber c : chambers) {
            float dx = c.getX() - x;
            float dy = c.getY() - y;
            float dz = c.getZ() - z;
            float distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = c;
            }
        }
        return nearest;
    }

    public List<Chamber> findChambersInRadius(float x, float y, float z, float radius) {
        float rSq = radius * radius;
        List<Chamber> result = new ArrayList<>();
        for (Chamber c : chambers) {
            float dx = c.getX() - x;
            float dy = c.getY() - y;
            float dz = c.getZ() - z;
            if (dx * dx + dy * dy + dz * dz <= rSq) {
                result.add(c);
            }
        }
        return result;
    }

    private synchronized void sortChambersByMorton() {
        if (chambers.size() > 1) {
            chambers.sort(Comparator.comparingLong(c -> morton3D(c.getX(), c.getY(), c.getZ())));
        }
    }
}

