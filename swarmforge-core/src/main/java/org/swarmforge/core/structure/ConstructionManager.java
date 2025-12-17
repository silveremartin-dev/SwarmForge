package org.swarmforge.core.structure;

import org.swarmforge.core.simulation.Simulation;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Optional;

/**
 * Manages construction projects and terrain modification.
 */
public class ConstructionManager {
    private final Nest nest;
    private final List<ConstructionProject> projects = new CopyOnWriteArrayList<>();

    public ConstructionManager(Simulation simulation, Nest nest) {
        this.nest = nest;
    }

    public void tick() {
        // Complete projects check
        for (ConstructionProject project : projects) {
            project.checkCompletion();
        }
        projects.removeIf(ConstructionProject::isComplete);
    }

    public Optional<ConstructionTask> getAvailableTask(float antX, float antY, float antZ) {
        // Simple search for nearest unassigned task
        ConstructionTask nearest = null;
        float minDist = Float.MAX_VALUE;

        for (ConstructionProject project : projects) {
            for (ConstructionTask task : project.getTasks()) {
                if (!task.isCompleted() && !task.isAssigned()) {
                    float dist = (float) Math.sqrt(
                            Math.pow(task.getX() - antX, 2) +
                                    Math.pow(task.getY() - antY, 2) +
                                    Math.pow(task.getZ() - antZ, 2));
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = task;
                    }
                }
            }
        }
        return Optional.ofNullable(nearest);
    }

    public void planNewChamber(String id, Chamber.Type type, float x, float y, float z) {
        ConstructionProject project = new ConstructionProject(() -> {
            // On Complete: Create the actual chamber
            Chamber newChamber = new Chamber(id, type, x, y, z, 100);
            nest.addChamber(newChamber);
        });

        // Break down chamber into tasks (e.g., a 2x2x2 cube of voxels)
        // For simplicity, just one task for now
        project.addTask(new ConstructionTask(x, y, z));

        projects.add(project);
    }
}
