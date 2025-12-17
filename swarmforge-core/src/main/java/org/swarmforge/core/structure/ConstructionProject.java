package org.swarmforge.core.structure;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A larger construction goal consisting of multiple tasks.
 */
public class ConstructionProject {

    private final List<ConstructionTask> tasks = new CopyOnWriteArrayList<>();
    private final Runnable onCompletion; // Callback when finished

    public ConstructionProject(Runnable onCompletion) {
        this.onCompletion = onCompletion;
    }

    public void addTask(ConstructionTask task) {
        tasks.add(task);
    }

    public List<ConstructionTask> getTasks() {
        return tasks;
    }

    public boolean isComplete() {
        for (ConstructionTask task : tasks) {
            if (!task.isCompleted())
                return false;
        }
        return true;
    }

    public void checkCompletion() {
        if (isComplete() && onCompletion != null) {
            onCompletion.run();
        }
    }
}
