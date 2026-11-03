/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import java.util.UUID;
import org.swarmforge.core.simulation.TunnelNetwork.ChamberType;

/**
 * Specialized mature climax architecture builder for {@link TunnelNetwork}.
 * Handles advanced morphological architectures such as arboreal nests, wax combs, cathedral mounds, etc.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class TunnelNetworkClimaxArchitect {

    private TunnelNetworkClimaxArchitect() {}

    public static boolean tryBuildClimaxNest(TunnelNetwork net, float nx, float ny, float nz, String arch, float scale) {
        if (arch.contains("WOOD") || arch.contains("TREE") || arch.contains("TRUNK") || arch.contains("HOLLOW_TRUNK")) {
            buildHollowTrunkNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("WAX_COMB") || arch.contains("BEEHIVE") || arch.contains("HEXAGONAL")) {
            buildHexagonalCombNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("WAX_POTS") || arch.contains("POTS_CLUSTER") || arch.contains("BOMBUS")) {
            buildWaxPotsClusterNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("PAPER") || arch.contains("PEDUNCULATE") || arch.contains("VESPA")) {
            buildPaperPedunculateNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("CATHEDRAL") || arch.contains("TERMITE") || arch.contains("STERCORAL")) {
            buildCathedralMoundNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("FUNGI") || arch.contains("VAULT") || arch.contains("ATTA")) {
            buildSubterraneanFungiVaultNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("MOUND") || arch.contains("SURFACE_MOUND") || arch.contains("SOLAR") || arch.contains("RUFA")) {
            buildSurfaceMoundNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("SILK") || arch.contains("LEAF") || arch.contains("OECOPHYLLA") || arch.contains("ARBOREAL_SILK")) {
            buildArborealSilkLeafNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("CARTON")) {
            buildCartonNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("BAMBOO") || arch.contains("STEM") || arch.contains("GALL")) {
            buildBambooStemNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("BIVOUAC") || arch.contains("ARMY") || arch.contains("ECITON")) {
            buildBivouacLivingNest(net, nx, ny, nz, scale);
            return true;
        } else if (arch.contains("SUPERCOLONY") || arch.contains("SUPERCOLONIE")) {
            buildSupercolonyNetworkNest(net, nx, ny, nz, scale);
            return true;
        }
        return false;
    }

    public static void buildSurfaceMoundNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID ent1 = net.createNode(nx - 3.0f * s, ny - 1.0f * s, nz + 0.6f * s, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        UUID ent2 = net.createNode(nx + 3.0f * s, ny + 1.0f * s, nz + 0.6f * s, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        UUID solarium = net.createNode(nx, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 4.5f * s, 4.5f * s, 2.4f * s);
        UUID moundCenter = net.createNode(nx, ny, nz + 1.5f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.8f * s);

        net.createEdge(ent1, moundCenter);
        net.createEdge(ent2, moundCenter);
        net.createEdge(moundCenter, solarium);

        UUID deepShaft1 = net.createNode(nx - 0.5f * s, ny + 0.5f * s, nz - 4.0f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.5f * s);
        UUID deepShaft2 = net.createNode(nx + 0.5f * s, ny - 0.5f * s, nz - 9.0f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.5f * s);
        net.createEdge(moundCenter, deepShaft1);
        net.createEdge(deepShaft1, deepShaft2);

        UUID broodWarm = net.createNode(nx + 4.0f * s, ny + 2.0f * s, nz - 3.0f * s, ChamberType.BROOD_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID pantry = net.createNode(nx - 4.0f * s, ny - 2.5f * s, nz - 5.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        net.createEdge(deepShaft1, broodWarm);
        net.createEdge(deepShaft1, pantry);

        UUID winterCrypt = net.createNode(nx, ny, nz - 14.0f * s, ChamberType.HIBERNATION, 4.8f * s, 4.8f * s, 2.4f * s);
        UUID royalVault = net.createNode(nx + 3.5f * s, ny - 2.0f * s, nz - 13.0f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.5f * s);
        UUID waste = net.createNode(nx - 4.5f * s, ny + 3.0f * s, nz - 15.0f * s, ChamberType.WASTE_DUMP, 3.4f * s, 3.4f * s, 1.8f * s);

        net.createEdge(deepShaft2, winterCrypt);
        net.createEdge(deepShaft2, royalVault);
        net.createEdge(winterCrypt, waste);
    }

    public static void buildSubterraneanFungiVaultNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID ent1 = net.createNode(nx - 4.5f * s, ny - 2.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        UUID ent2 = net.createNode(nx + 4.5f * s, ny + 2.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        UUID ent3 = net.createNode(nx, ny + 5.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);

        UUID upperHub = net.createNode(nx, ny, nz - 3.5f * s, ChamberType.TUNNEL, 2.2f * s, 2.2f * s, 1.8f * s);
        net.createEdge(ent1, upperHub);
        net.createEdge(ent2, upperHub);
        net.createEdge(ent3, upperHub);

        UUID midShaft = net.createNode(nx + 0.5f * s, ny - 0.5f * s, nz - 8.0f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.8f * s);
        UUID lowShaft = net.createNode(nx - 0.5f * s, ny + 0.5f * s, nz - 13.0f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.8f * s);
        net.createEdge(upperHub, midShaft);
        net.createEdge(midShaft, lowShaft);

        UUID fungus1 = net.createNode(nx - 6.0f * s, ny + 3.0f * s, nz - 6.5f * s, ChamberType.FUNGUS_GARDEN, 4.8f * s, 4.8f * s, 2.5f * s);
        UUID fungus2 = net.createNode(nx + 6.0f * s, ny - 3.0f * s, nz - 7.5f * s, ChamberType.FUNGUS_GARDEN, 5.0f * s, 5.0f * s, 2.6f * s);
        UUID fungus3 = net.createNode(nx - 5.5f * s, ny - 4.0f * s, nz - 11.5f * s, ChamberType.FUNGUS_GARDEN, 5.2f * s, 5.2f * s, 2.8f * s);
        UUID fungus4 = net.createNode(nx + 5.5f * s, ny + 4.0f * s, nz - 12.5f * s, ChamberType.FUNGUS_GARDEN, 5.0f * s, 5.0f * s, 2.6f * s);
        net.createEdge(midShaft, fungus1);
        net.createEdge(midShaft, fungus2);
        net.createEdge(lowShaft, fungus3);
        net.createEdge(lowShaft, fungus4);

        UUID brood1 = net.createNode(nx, ny - 5.5f * s, nz - 9.5f * s, ChamberType.BROOD_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID brood2 = net.createNode(nx, ny + 5.5f * s, nz - 10.5f * s, ChamberType.BROOD_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
        net.createEdge(midShaft, brood1);
        net.createEdge(midShaft, brood2);

        UUID queen = net.createNode(nx, ny, nz - 16.0f * s, ChamberType.QUEEN_CHAMBER, 5.5f * s, 5.5f * s, 2.6f * s);
        net.createEdge(lowShaft, queen);

        UUID waste1 = net.createNode(nx - 7.0f * s, ny, nz - 18.5f * s, ChamberType.WASTE_DUMP, 4.0f * s, 4.0f * s, 2.0f * s);
        UUID waste2 = net.createNode(nx + 7.0f * s, ny, nz - 18.5f * s, ChamberType.WASTE_DUMP, 4.0f * s, 4.0f * s, 2.0f * s);
        net.createEdge(queen, waste1);
        net.createEdge(queen, waste2);
    }

    public static void buildCathedralMoundNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID ent1 = net.createNode(nx - 2.5f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID ent2 = net.createNode(nx + 2.5f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID spire = net.createNode(nx, ny, nz + 10.0f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 3.0f * s);
        UUID subHub = net.createNode(nx, ny, nz - 2.5f * s, ChamberType.TUNNEL, 2.2f * s, 2.2f * s, 1.8f * s);

        net.createEdge(ent1, spire);
        net.createEdge(ent2, spire);
        net.createEdge(ent1, subHub);
        net.createEdge(ent2, subHub);

        UUID royalCell = net.createNode(nx, ny, nz - 9.0f * s, ChamberType.QUEEN_CHAMBER, 5.4f * s, 5.4f * s, 2.5f * s);
        net.createEdge(subHub, royalCell);

        UUID nursery1 = net.createNode(nx + 4.5f * s, ny + 2.0f * s, nz - 5.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID nursery2 = net.createNode(nx - 4.5f * s, ny - 2.0f * s, nz - 5.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        net.createEdge(subHub, nursery1);
        net.createEdge(subHub, nursery2);

        UUID fungusComb1 = net.createNode(nx - 4.0f * s, ny + 3.5f * s, nz - 8.0f * s, ChamberType.FOOD_STORAGE, 4.0f * s, 4.0f * s, 2.2f * s);
        UUID fungusComb2 = net.createNode(nx + 4.0f * s, ny - 3.5f * s, nz - 8.0f * s, ChamberType.FOOD_STORAGE, 4.0f * s, 4.0f * s, 2.2f * s);
        net.createEdge(royalCell, fungusComb1);
        net.createEdge(royalCell, fungusComb2);

        UUID waste = net.createNode(nx, ny + 4.0f * s, nz - 14.0f * s, ChamberType.WASTE_DUMP, 3.5f * s, 3.5f * s, 1.8f * s);
        net.createEdge(royalCell, waste);
    }

    public static void buildSupercolonyNetworkNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID mainEnt = net.createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 2.5f * s, 2.5f * s, 1.8f * s);
        UUID leftEnt = net.createNode(nx - 9.0f * s, ny + 4.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID rightEnt = net.createNode(nx + 9.0f * s, ny - 4.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);

        UUID centerShaft = net.createNode(nx, ny, nz - 6.0f * s, ChamberType.TUNNEL, 2.2f * s, 2.2f * s, 1.8f * s);
        UUID leftShaft = net.createNode(nx - 8.0f * s, ny + 3.5f * s, nz - 5.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.6f * s);
        UUID rightShaft = net.createNode(nx + 8.0f * s, ny - 3.5f * s, nz - 5.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.6f * s);

        net.createEdge(mainEnt, centerShaft);
        net.createEdge(leftEnt, leftShaft);
        net.createEdge(rightEnt, rightShaft);
        net.createEdge(centerShaft, leftShaft);
        net.createEdge(centerShaft, rightShaft);

        UUID queen1 = net.createNode(nx, ny, nz - 13.0f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.4f * s);
        UUID queen2 = net.createNode(nx - 10.0f * s, ny + 5.0f * s, nz - 10.5f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.2f * s);
        UUID queen3 = net.createNode(nx + 10.0f * s, ny - 5.0f * s, nz - 10.5f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.2f * s);
        net.createEdge(centerShaft, queen1);
        net.createEdge(leftShaft, queen2);
        net.createEdge(rightShaft, queen3);

        UUID brood1 = net.createNode(nx - 4.0f * s, ny + 2.5f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood2 = net.createNode(nx + 4.0f * s, ny - 2.5f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood3 = net.createNode(nx - 12.0f * s, ny + 2.0f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood4 = net.createNode(nx + 12.0f * s, ny - 2.0f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        net.createEdge(centerShaft, brood1);
        net.createEdge(centerShaft, brood2);
        net.createEdge(leftShaft, brood3);
        net.createEdge(rightShaft, brood4);

        UUID food1 = net.createNode(nx + 4.5f * s, ny + 4.5f * s, nz - 7.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID food2 = net.createNode(nx - 4.5f * s, ny - 4.5f * s, nz - 7.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID food3 = net.createNode(nx, ny, nz - 16.5f * s, ChamberType.FOOD_STORAGE, 4.2f * s, 4.2f * s, 2.2f * s);
        net.createEdge(centerShaft, food1);
        net.createEdge(centerShaft, food2);
        net.createEdge(queen1, food3);

        UUID waste = net.createNode(nx, ny + 8.0f * s, nz - 19.0f * s, ChamberType.WASTE_DUMP, 3.8f * s, 3.8f * s, 1.8f * s);
        net.createEdge(food3, waste);
    }

    public static void buildHexagonalCombNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID ent1 = net.createNode(nx - 2.0f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.5f * s);
        UUID ent2 = net.createNode(nx + 2.0f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.5f * s);
        UUID frameTop = net.createNode(nx, ny, nz + 3.0f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
        net.createEdge(ent1, frameTop);
        net.createEdge(ent2, frameTop);

        UUID queenCell = net.createNode(nx, ny, nz + 1.8f * s, ChamberType.QUEEN_CHAMBER, 4.5f * s, 4.5f * s, 2.2f * s);
        net.createEdge(frameTop, queenCell);

        UUID broodL = net.createNode(nx - 3.5f * s, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        UUID broodR = net.createNode(nx + 3.5f * s, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        net.createEdge(frameTop, broodL);
        net.createEdge(frameTop, broodR);

        UUID honeyVault1 = net.createNode(nx - 2.5f * s, ny, nz + 8.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID honeyVault2 = net.createNode(nx + 2.5f * s, ny, nz + 8.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        net.createEdge(broodL, honeyVault1);
        net.createEdge(broodR, honeyVault2);
    }

    public static void buildWaxPotsClusterNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID ent = net.createNode(nx, ny, nz + 0.2f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.5f * s);
        UUID hub = net.createNode(nx, ny, nz - 0.8f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
        net.createEdge(ent, hub);

        UUID queen = net.createNode(nx, ny, nz - 1.8f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.3f * s);
        net.createEdge(hub, queen);

        UUID brood1 = net.createNode(nx + 2.2f * s, ny + 1.5f * s, nz - 1.2f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        UUID brood2 = net.createNode(nx - 2.2f * s, ny - 1.5f * s, nz - 1.2f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        net.createEdge(hub, brood1);
        net.createEdge(hub, brood2);

        UUID honeyPot = net.createNode(nx + 1.8f * s, ny - 2.2f * s, nz - 1.0f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.9f * s);
        UUID pollenPot = net.createNode(nx - 1.8f * s, ny + 2.2f * s, nz - 1.0f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.9f * s);
        net.createEdge(hub, honeyPot);
        net.createEdge(hub, pollenPot);

        UUID waste = net.createNode(nx, ny + 3.0f * s, nz - 2.2f * s, ChamberType.WASTE_DUMP, 3.0f * s, 3.0f * s, 1.5f * s);
        net.createEdge(queen, waste);
    }

    public static void buildPaperPedunculateNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID branchAnchor = net.createNode(nx - 4.0f * s, ny, nz + 8.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
        UUID peduncle = net.createNode(nx, ny, nz + 8.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.8f * s);
        UUID entrance = net.createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
        UUID spire = net.createNode(nx, ny, nz + 4.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.5f * s);

        net.createEdge(branchAnchor, peduncle);
        net.createEdge(peduncle, spire);
        net.createEdge(spire, entrance);

        UUID queenCell = net.createNode(nx, ny, nz + 6.5f * s, ChamberType.QUEEN_CHAMBER, 4.4f * s, 4.4f * s, 2.2f * s);
        net.createEdge(spire, queenCell);

        UUID brood1 = net.createNode(nx - 3.0f * s, ny, nz + 4.2f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        UUID brood2 = net.createNode(nx + 3.0f * s, ny, nz + 4.2f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        net.createEdge(spire, brood1);
        net.createEdge(spire, brood2);

        UUID foodStorage = net.createNode(nx, ny + 3.0f * s, nz + 5.0f * s, ChamberType.FOOD_STORAGE, 3.5f * s, 3.5f * s, 1.8f * s);
        net.createEdge(spire, foodStorage);
    }

    public static void buildArborealSilkLeafNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID branch = net.createNode(nx, ny, nz + 6.0f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID canopyHub = net.createNode(nx, ny, nz + 10.0f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
        net.createEdge(branch, canopyHub);

        UUID queenLeaf = net.createNode(nx, ny, nz + 9.0f * s, ChamberType.QUEEN_CHAMBER, 4.8f * s, 4.8f * s, 2.4f * s);
        net.createEdge(canopyHub, queenLeaf);

        UUID broodLeaf1 = net.createNode(nx - 3.5f * s, ny, nz + 8.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID broodLeaf2 = net.createNode(nx + 3.5f * s, ny, nz + 8.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        net.createEdge(canopyHub, broodLeaf1);
        net.createEdge(canopyHub, broodLeaf2);

        UUID foodLeaf = net.createNode(nx, ny + 3.5f * s, nz + 8.0f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.8f * s);
        net.createEdge(canopyHub, foodLeaf);
    }

    public static void buildCartonNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID entrance = net.createNode(nx, ny, nz + 4.0f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.5f * s);
        UUID core = net.createNode(nx, ny, nz + 6.5f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.6f * s);
        net.createEdge(entrance, core);

        UUID queenCell = net.createNode(nx, ny, nz + 6.0f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.3f * s);
        net.createEdge(core, queenCell);

        UUID brood = net.createNode(nx - 3.0f * s, ny, nz + 7.2f * s, ChamberType.BROOD_CHAMBER, 3.5f * s, 3.5f * s, 1.8f * s);
        UUID food = net.createNode(nx + 3.0f * s, ny, nz + 7.2f * s, ChamberType.FOOD_STORAGE, 3.5f * s, 3.5f * s, 1.8f * s);
        net.createEdge(core, brood);
        net.createEdge(core, food);
    }

    public static void buildBambooStemNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID entrance = net.createNode(nx - 8.0f * s, ny, nz + 2.0f * s, ChamberType.ENTRANCE, 1.8f * s, 1.4f * s, 1.4f * s);
        UUID stemHub = net.createNode(nx, ny, nz + 2.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.3f * s, 1.3f * s);
        net.createEdge(entrance, stemHub);

        UUID queen = net.createNode(nx + 3.0f * s, ny, nz + 2.0f * s, ChamberType.QUEEN_CHAMBER, 4.2f * s, 1.8f * s, 1.6f * s);
        net.createEdge(stemHub, queen);

        UUID brood = net.createNode(nx + 7.5f * s, ny, nz + 2.0f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 1.6f * s, 1.4f * s);
        net.createEdge(queen, brood);

        UUID food = net.createNode(nx - 3.5f * s, ny, nz + 2.0f * s, ChamberType.FOOD_STORAGE, 3.2f * s, 1.5f * s, 1.4f * s);
        net.createEdge(stemHub, food);
    }

    public static void buildBivouacLivingNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID logAnchor = net.createNode(nx, ny, nz + 3.5f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.5f * s);
        UUID entrance = net.createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        net.createEdge(logAnchor, entrance);

        UUID core = net.createNode(nx, ny, nz + 0.2f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.5f * s);
        net.createEdge(entrance, core);

        UUID brood = net.createNode(nx - 3.0f * s, ny, nz - 0.8f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID food = net.createNode(nx + 3.0f * s, ny, nz - 0.8f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.8f * s);
        net.createEdge(core, brood);
        net.createEdge(core, food);
    }

    public static void buildHollowTrunkNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID entrance = net.createNode(nx, ny - 2.0f * s, nz + 1.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID shaft1 = net.createNode(nx, ny, nz + 4.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 2.0f * s);
        UUID shaft2 = net.createNode(nx, ny, nz + 9.0f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 2.0f * s);
        net.createEdge(entrance, shaft1);
        net.createEdge(shaft1, shaft2);

        UUID queen = net.createNode(nx, ny, nz + 7.0f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.4f * s);
        net.createEdge(shaft1, queen);

        UUID brood1 = net.createNode(nx + 3.5f * s, ny, nz + 4.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood2 = net.createNode(nx - 3.5f * s, ny, nz + 6.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        net.createEdge(shaft1, brood1);
        net.createEdge(queen, brood2);

        UUID foodStorage = net.createNode(nx + 3.0f * s, ny, nz + 10.5f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        net.createEdge(shaft2, foodStorage);

        UUID wasteDump = net.createNode(nx, ny, nz + 0.5f * s, ChamberType.WASTE_DUMP, 3.2f * s, 3.2f * s, 1.6f * s);
        net.createEdge(entrance, wasteDump);
    }
}
