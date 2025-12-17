package org.swarmforge.core.structure;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The physical structure of a colony.
 */
public class Nest implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Chamber> chambers = new CopyOnWriteArrayList<>();
    private final List<Tunnel> tunnels = new CopyOnWriteArrayList<>();

    public void addChamber(Chamber chamber) {
        chambers.add(chamber);
    }

    public void addTunnel(Tunnel tunnel) {
        tunnels.add(tunnel);
    }

    public List<Chamber> getChambers() {
        return chambers;
    }

    public List<Tunnel> getTunnels() {
        return tunnels;
    }

    public Chamber findNearestChamber(float x, float y, float z) {
        Chamber nearest = null;
        float minDist = Float.MAX_VALUE;

        for (Chamber c : chambers) {
            float dist = (float) Math
                    .sqrt(Math.pow(c.getX() - x, 2) + Math.pow(c.getY() - y, 2) + Math.pow(c.getZ() - z, 2));
            if (dist < minDist) {
                minDist = dist;
                nearest = c;
            }
        }
        return nearest;
    }
}
