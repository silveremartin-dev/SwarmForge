/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import org.swarmforge.client.event.UiEvent;
import org.swarmforge.client.event.UiEventBus;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Concrete {@link TerrainCommand} that records before-and-after states for a block of voxels,
 * enabling reversible painting, excavation, deposition, and erosion in the 3D Terrarium.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class VoxelBlockModificationCommand implements TerrainCommand {

    public record VoxelChange(TerrariumCell previousCell, TerrariumCell newCell) {}

    private final Terrarium terrarium;
    private final String description;
    private final List<VoxelChange> changes;

    private int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

    public VoxelBlockModificationCommand(Terrarium terrarium, String description) {
        this.terrarium = Objects.requireNonNull(terrarium, "terrarium cannot be null");
        this.description = (description != null && !description.isBlank()) ? description : "Modification de terrain";
        this.changes = new ArrayList<>();
    }

    public void recordChange(TerrariumCell previousCell, TerrariumCell newCell) {
        if (previousCell == null || newCell == null) return;
        changes.add(new VoxelChange(previousCell, newCell));

        int x = newCell.x();
        int y = newCell.y();
        int z = newCell.z();

        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;
        if (z < minZ) minZ = z;
        if (z > maxZ) maxZ = z;
    }

    @Override
    public void execute() {
        for (VoxelChange change : changes) {
            terrarium.setCell(change.newCell());
        }
        notifyEvent("EXECUTE");
    }

    @Override
    public void undo() {
        for (VoxelChange change : changes) {
            terrarium.setCell(change.previousCell());
        }
        notifyEvent("UNDO");
    }

    private void notifyEvent(String op) {
        if (!changes.isEmpty() && minX <= maxX) {
            UiEventBus.getInstance().publishFx(new UiEvent.TerrainModifiedUiEvent(
                    minX, minY, minZ, maxX, maxY, maxZ, description + " (" + op + ")"
            ));
        }
    }

    @Override
    public String getDescription() {
        return description + " (" + changes.size() + " voxels)";
    }

    public int getChangeCount() {
        return changes.size();
    }
}
