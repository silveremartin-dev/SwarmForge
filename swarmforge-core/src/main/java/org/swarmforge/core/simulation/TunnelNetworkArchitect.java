/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 SilvÃ¨re Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.simulation.TunnelNetwork.ChamberType;
import org.swarmforge.core.simulation.TunnelNetwork.OntogenyStage;

import java.util.UUID;

/**
 * Procedural Architecture Generator & Ontogenetic Growth Engine for {@link TunnelNetwork}.
 * Handles topology construction across castes, life stages, and biological species archetypes.
 *
 * @author SilvÃ¨re Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class TunnelNetworkArchitect {

    private TunnelNetworkArchitect() {}

    public static void buildNetworkForArchitecture(TunnelNetwork net, float nx, float ny, float nz, String nestType, int population) {
        String arch = nestType.toUpperCase();
        float scale = Math.max(0.5f, net.getScaleFactorSetting());

        if (population <= 3) {
            net.setOntogenyStage(OntogenyStage.FOUNDING_CLAUSTRAL);
            buildFoundingClaustralNest(net, nx, ny, nz, scale, arch);
        } else if (population <= 25) {
            net.setOntogenyStage(OntogenyStage.INCIPIENT);
            buildIncipientNest(net, nx, ny, nz, scale, arch);
        } else if (population <= 100) {
            net.setOntogenyStage(OntogenyStage.DEVELOPING);
            buildDevelopingNest(net, nx, ny, nz, scale, arch);
        } else {
            net.setOntogenyStage(OntogenyStage.MATURE_CLIMAX);
            buildMatureNestForArchitecture(net, nx, ny, nz, arch, scale);
        }
    }

    // ── 0. Claustral Founding Nest (Solitary Queen Stage) ──────────────────────
    private static void buildFoundingClaustralNest(TunnelNetwork net, float nx, float ny, float nz, float s, String arch) {
        String a = arch != null ? arch.toUpperCase() : "BURROW";
        if (a.contains("PAPER") || a.contains("PEDUNCULATE") || a.contains("VESPA")) {
            // Solitary foundress on hanging branch: single pedicel + 1 embryonic comb cell
            UUID branch = net.createNode(nx - 2.0f * s, ny, nz + 6.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID pedicel = net.createNode(nx, ny, nz + 5.5f * s, ChamberType.ENTRANCE, 1.2f * s, 1.2f * s, 1.4f * s);
            UUID claustrum = net.createNode(nx, ny, nz + 4.5f * s, ChamberType.QUEEN_CHAMBER, 2.0f * s, 2.0f * s, 1.4f * s);
            net.createEdge(branch, pedicel);
            net.createEdge(pedicel, claustrum);
        } else if (a.contains("WAX_COMB") || a.contains("BEEHIVE") || a.contains("HEXAGONAL")) {
            // Solitary foundress in tree cavity with single starter wax comb
            UUID ent = net.createNode(nx, ny, nz + 2.0f * s, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID comb = net.createNode(nx, ny, nz + 3.5f * s, ChamberType.QUEEN_CHAMBER, 2.2f * s, 2.2f * s, 1.5f * s);
            net.createEdge(ent, comb);
        } else if (a.contains("WAX_POTS") || a.contains("POTS_CLUSTER") || a.contains("BOMBUS")) {
            // Solitary bumblebee queen in shallow moss hollow with 1 honeypot
            UUID ent = net.createNode(nx, ny, nz + 0.1f, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID pot = net.createNode(nx, ny, nz - 0.8f * s, ChamberType.QUEEN_CHAMBER, 2.4f * s, 2.4f * s, 1.6f * s);
            net.createEdge(ent, pot);
        } else if (a.contains("CATHEDRAL") || a.contains("TERMITE") || a.contains("STERCORAL")) {
            // Founding royal pair sealed in subterranean copularium cavity
            UUID plug = net.createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.2f * s, 1.2f * s, 1.0f * s);
            UUID copularium = net.createNode(nx, ny, nz - 2.5f * s, ChamberType.QUEEN_CHAMBER, 2.5f * s, 2.5f * s, 1.6f * s);
            net.createEdge(plug, copularium);
        } else if (a.contains("FUNGI") || a.contains("VAULT") || a.contains("ATTA")) {
            // Solitary foundress leafcutter queen in vertical claustral tube with founding fungal pellet
            UUID plug = net.createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.2f * s, 1.2f * s, 1.0f * s);
            UUID fungusCell = net.createNode(nx, ny, nz - 4.5f * s, ChamberType.QUEEN_CHAMBER, 2.6f * s, 2.6f * s, 1.8f * s);
            net.createEdge(plug, fungusCell);
        } else if (a.contains("SILK") || a.contains("LEAF") || a.contains("OECOPHYLLA") || a.contains("ARBOREAL_SILK")) {
            // Solitary weaver queen in single curled green leaf
            UUID branch = net.createNode(nx, ny, nz + 5.0f * s, ChamberType.ENTRANCE, 1.5f * s, 1.5f * s, 1.2f * s);
            UUID leafCell = net.createNode(nx, ny, nz + 4.2f * s, ChamberType.QUEEN_CHAMBER, 2.2f * s, 2.2f * s, 1.4f * s);
            net.createEdge(branch, leafCell);
        } else if (a.contains("CARTON")) {
            // Solitary foundress on branch knot with starter carton paste cell
            UUID ent = net.createNode(nx, ny, nz + 4.0f * s, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID cartonCell = net.createNode(nx, ny, nz + 4.5f * s, ChamberType.QUEEN_CHAMBER, 2.2f * s, 2.2f * s, 1.4f * s);
            net.createEdge(ent, cartonCell);
        } else if (a.contains("BAMBOO") || a.contains("STEM") || a.contains("GALL")) {
            // Solitary foundress in hollow plant internode
            UUID pore = net.createNode(nx - 2.0f * s, ny, nz + 2.0f * s, ChamberType.ENTRANCE, 1.2f * s, 1.0f * s, 1.0f * s);
            UUID stemCell = net.createNode(nx + 1.0f * s, ny, nz + 2.0f * s, ChamberType.QUEEN_CHAMBER, 2.4f * s, 1.4f * s, 1.2f * s);
            net.createEdge(pore, stemCell);
        } else if (a.contains("BIVOUAC") || a.contains("ARMY") || a.contains("ECITON")) {
            // Solitary queen / nomad cluster under leaf shelter
            UUID ent = net.createNode(nx, ny, nz + 1.0f * s, ChamberType.ENTRANCE, 1.5f * s, 1.5f * s, 1.2f * s);
            UUID cluster = net.createNode(nx, ny, nz + 0.3f * s, ChamberType.QUEEN_CHAMBER, 2.4f * s, 2.4f * s, 1.6f * s);
            net.createEdge(ent, cluster);
        } else if (a.contains("WOOD") || a.contains("TREE") || a.contains("TRUNK") || a.contains("HOLLOW_TRUNK")) {
            // Solitary foundress in soft bark slit
            UUID slit = net.createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID xylemCell = net.createNode(nx, ny, nz + 2.5f * s, ChamberType.QUEEN_CHAMBER, 2.2f * s, 2.2f * s, 1.4f * s);
            net.createEdge(slit, xylemCell);
        } else {
            // Solitary claustral copula underground (Formica rufa, Pogonomyrmex, Lasius, Supercolony)
            UUID ent = net.createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID claustrum = net.createNode(nx, ny, nz - 3.5f * s, ChamberType.QUEEN_CHAMBER, 2.4f * s, 2.4f * s, 1.5f * s);
            net.createEdge(ent, claustrum);
        }
    }

    // ── 1. Incipient Colony Nest (First Nanitic Workers) ─────────────────────────
    private static void buildIncipientNest(TunnelNetwork net, float nx, float ny, float nz, float s, String arch) {
        String a = arch != null ? arch.toUpperCase() : "BURROW";
        if (a.contains("PAPER") || a.contains("PEDUNCULATE") || a.contains("VESPA")) {
            UUID branch = net.createNode(nx - 2.5f * s, ny, nz + 6.5f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.2f * s);
            UUID pedicel = net.createNode(nx, ny, nz + 6.0f * s, ChamberType.TUNNEL, 1.2f * s, 1.2f * s, 1.4f * s);
            UUID combDisc = net.createNode(nx, ny, nz + 4.8f * s, ChamberType.HONEYCOMB, 2.6f * s, 2.6f * s, 1.6f * s);
            UUID queen = net.createNode(nx + 1.2f * s, ny, nz + 4.5f * s, ChamberType.QUEEN_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID brood = net.createNode(nx - 1.2f * s, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 2.5f * s, 2.5f * s, 1.4f * s);
            UUID entry = net.createNode(nx, ny, nz + 3.6f * s, ChamberType.ENTRANCE, 1.6f * s, 1.6f * s, 1.2f * s);
            net.createEdge(branch, pedicel);
            net.createEdge(pedicel, combDisc);
            net.createEdge(combDisc, queen);
            net.createEdge(combDisc, brood);
            net.createEdge(combDisc, entry);
        } else if (a.contains("WAX_COMB") || a.contains("BEEHIVE") || a.contains("HEXAGONAL")) {
            UUID ent = net.createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID hub = net.createNode(nx, ny, nz + 3.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.2f * s);
            UUID comb1 = net.createNode(nx - 1.5f * s, ny, nz + 4.5f * s, ChamberType.HONEYCOMB, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID comb2 = net.createNode(nx + 1.5f * s, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID queen = net.createNode(nx, ny, nz + 3.8f * s, ChamberType.QUEEN_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            net.createEdge(ent, hub);
            net.createEdge(hub, comb1);
            net.createEdge(hub, comb2);
            net.createEdge(hub, queen);
        } else if (a.contains("WAX_POTS") || a.contains("POTS_CLUSTER") || a.contains("BOMBUS")) {
            UUID ent = net.createNode(nx, ny, nz + 0.1f, ChamberType.ENTRANCE, 1.6f * s, 1.6f * s, 1.2f * s);
            UUID hub = net.createNode(nx, ny, nz - 0.6f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID honeyPot = net.createNode(nx + 1.4f * s, ny, nz - 0.9f * s, ChamberType.POLLEN_POT, 2.2f * s, 2.2f * s, 1.5f * s);
            UUID broodPot = net.createNode(nx - 1.4f * s, ny, nz - 0.9f * s, ChamberType.BROOD_CHAMBER, 2.4f * s, 2.4f * s, 1.5f * s);
            UUID queen = net.createNode(nx, ny, nz - 1.4f * s, ChamberType.QUEEN_CHAMBER, 3.0f * s, 3.0f * s, 1.8f * s);
            net.createEdge(ent, hub);
            net.createEdge(hub, honeyPot);
            net.createEdge(hub, broodPot);
            net.createEdge(hub, queen);
        } else if (a.contains("CATHEDRAL") || a.contains("TERMITE") || a.contains("STERCORAL")) {
            UUID turret = net.createNode(nx, ny, nz + 1.5f * s, ChamberType.VENTILATION_CHIMNEY, 1.6f * s, 1.6f * s, 1.8f * s);
            UUID subHub = net.createNode(nx, ny, nz - 1.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
            UUID fungus = net.createNode(nx + 2.0f * s, ny, nz - 3.0f * s, ChamberType.FUNGUS_GARDEN, 3.0f * s, 3.0f * s, 1.8f * s);
            UUID royal = net.createNode(nx - 1.5f * s, ny, nz - 5.0f * s, ChamberType.QUEEN_CHAMBER, 3.6f * s, 3.6f * s, 2.0f * s);
            net.createEdge(turret, subHub);
            net.createEdge(subHub, fungus);
            net.createEdge(subHub, royal);
        } else if (a.contains("FUNGI") || a.contains("VAULT") || a.contains("ATTA")) {
            UUID crater = net.createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID shaft = net.createNode(nx, ny, nz - 3.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.4f * s);
            UUID fungus = net.createNode(nx + 2.5f * s, ny, nz - 5.0f * s, ChamberType.FUNGUS_GARDEN, 3.4f * s, 3.4f * s, 2.0f * s);
            UUID queen = net.createNode(nx - 2.0f * s, ny, nz - 8.0f * s, ChamberType.QUEEN_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
            net.createEdge(crater, shaft);
            net.createEdge(shaft, fungus);
            net.createEdge(shaft, queen);
        } else if (a.contains("MOUND") || a.contains("SURFACE_MOUND") || a.contains("SOLAR") || a.contains("RUFA")) {
            UUID ent = net.createNode(nx, ny, nz + 0.4f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID solarium = net.createNode(nx + 1.5f * s, ny, nz + 1.8f * s, ChamberType.SOLARIUM, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID hub = net.createNode(nx, ny, nz - 2.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.3f * s);
            UUID brood = net.createNode(nx - 2.0f * s, ny, nz - 4.0f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID queen = net.createNode(nx, ny, nz - 7.0f * s, ChamberType.QUEEN_CHAMBER, 3.6f * s, 3.6f * s, 2.0f * s);
            net.createEdge(ent, solarium);
            net.createEdge(ent, hub);
            net.createEdge(hub, brood);
            net.createEdge(hub, queen);
        } else if (a.contains("SILK") || a.contains("LEAF") || a.contains("OECOPHYLLA") || a.contains("ARBOREAL_SILK")) {
            UUID branch = net.createNode(nx, ny, nz + 5.0f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID pav1 = net.createNode(nx - 2.0f * s, ny, nz + 5.5f * s, ChamberType.BROOD_CHAMBER, 3.0f * s, 3.0f * s, 1.8f * s);
            UUID pav2 = net.createNode(nx + 2.0f * s, ny, nz + 5.5f * s, ChamberType.QUEEN_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
            net.createEdge(branch, pav1);
            net.createEdge(branch, pav2);
        } else if (a.contains("CARTON")) {
            UUID ent = net.createNode(nx, ny, nz + 3.5f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID core = net.createNode(nx, ny, nz + 4.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
            UUID brood = net.createNode(nx - 1.8f * s, ny, nz + 5.0f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID queen = net.createNode(nx + 1.8f * s, ny, nz + 5.0f * s, ChamberType.QUEEN_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            net.createEdge(ent, core);
            net.createEdge(core, brood);
            net.createEdge(core, queen);
        } else if (a.contains("BAMBOO") || a.contains("STEM") || a.contains("GALL")) {
            UUID pore = net.createNode(nx - 3.0f * s, ny, nz + 2.0f * s, ChamberType.ENTRANCE, 1.4f * s, 1.2f * s, 1.2f * s);
            UUID node1 = net.createNode(nx, ny, nz + 2.0f * s, ChamberType.BROOD_CHAMBER, 2.6f * s, 1.5f * s, 1.4f * s);
            UUID node2 = net.createNode(nx + 3.0f * s, ny, nz + 2.0f * s, ChamberType.QUEEN_CHAMBER, 3.0f * s, 1.6f * s, 1.4f * s);
            net.createEdge(pore, node1);
            net.createEdge(node1, node2);
        } else if (a.contains("BIVOUAC") || a.contains("ARMY") || a.contains("ECITON")) {
            UUID anchor = net.createNode(nx, ny, nz + 1.8f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID core = net.createNode(nx, ny, nz + 0.6f * s, ChamberType.QUEEN_CHAMBER, 3.4f * s, 3.4f * s, 2.0f * s);
            UUID brood = net.createNode(nx, ny, nz - 0.2f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            net.createEdge(anchor, core);
            net.createEdge(core, brood);
        } else if (a.contains("WOOD") || a.contains("TREE") || a.contains("TRUNK") || a.contains("HOLLOW_TRUNK")) {
            UUID slit = net.createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID shaft = net.createNode(nx, ny, nz + 3.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.5f * s);
            UUID brood = net.createNode(nx + 2.0f * s, ny, nz + 3.5f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID queen = net.createNode(nx - 2.0f * s, ny, nz + 4.5f * s, ChamberType.QUEEN_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
            net.createEdge(slit, shaft);
            net.createEdge(shaft, brood);
            net.createEdge(shaft, queen);
        } else {
            // Standard subterranean burrow (Lasius, Messor, Pogonomyrmex)
            UUID ent = net.createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID hub = net.createNode(nx, ny, nz - 2.5f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID brood = net.createNode(nx - 2.5f * s, ny + 1.0f * s, nz - 4.5f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID food = net.createNode(nx + 2.5f * s, ny - 1.0f * s, nz - 4.0f * s, ChamberType.FOOD_STORAGE, 2.6f * s, 2.6f * s, 1.5f * s);
            UUID queen = net.createNode(nx, ny, nz - 7.5f * s, ChamberType.QUEEN_CHAMBER, 3.6f * s, 3.6f * s, 2.0f * s);
            net.createEdge(ent, hub);
            net.createEdge(hub, brood);
            net.createEdge(hub, food);
            net.createEdge(hub, queen);
        }
    }

    // ── 2. Developing Colony Nest (Expanding Population) ────────────────────────
    private static void buildDevelopingNest(TunnelNetwork net, float nx, float ny, float nz, float s, String arch) {
        String a = arch != null ? arch.toUpperCase() : "BURROW";
        if (a.contains("PAPER") || a.contains("PEDUNCULATE") || a.contains("VESPA")) {
            UUID branch = net.createNode(nx - 3.5f * s, ny, nz + 7.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID pedicel = net.createNode(nx, ny, nz + 7.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.6f * s);
            UUID topDisc = net.createNode(nx, ny, nz + 5.5f * s, ChamberType.HONEYCOMB, 3.4f * s, 3.4f * s, 1.8f * s);
            UUID lowDisc = net.createNode(nx, ny, nz + 3.8f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID queen = net.createNode(nx + 2.0f * s, ny, nz + 5.0f * s, ChamberType.QUEEN_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
            UUID entry = net.createNode(nx, ny, nz + 2.2f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            net.createEdge(branch, pedicel);
            net.createEdge(pedicel, topDisc);
            net.createEdge(topDisc, lowDisc);
            net.createEdge(topDisc, queen);
            net.createEdge(lowDisc, entry);
        } else if (a.contains("WAX_COMB") || a.contains("BEEHIVE") || a.contains("HEXAGONAL")) {
            UUID ent = net.createNode(nx, ny, nz + 1.2f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID shaft = net.createNode(nx, ny, nz + 3.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.5f * s);
            UUID honey1 = net.createNode(nx - 2.5f * s, ny, nz + 6.0f * s, ChamberType.FOOD_STORAGE, 3.5f * s, 3.5f * s, 1.8f * s);
            UUID honey2 = net.createNode(nx + 2.5f * s, ny, nz + 6.0f * s, ChamberType.FOOD_STORAGE, 3.5f * s, 3.5f * s, 1.8f * s);
            UUID brood = net.createNode(nx, ny, nz + 4.8f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID queen = net.createNode(nx, ny, nz + 2.8f * s, ChamberType.QUEEN_CHAMBER, 4.0f * s, 4.0f * s, 2.0f * s);
            net.createEdge(ent, shaft);
            net.createEdge(shaft, honey1);
            net.createEdge(shaft, honey2);
            net.createEdge(shaft, brood);
            net.createEdge(shaft, queen);
        } else if (a.contains("CATHEDRAL") || a.contains("TERMITE") || a.contains("STERCORAL")) {
            UUID spireApex = net.createNode(nx, ny, nz + 5.5f * s, ChamberType.VENTILATION_CHIMNEY, 1.8f * s, 1.8f * s, 2.5f * s);
            UUID subHub = net.createNode(nx, ny, nz - 2.0f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.6f * s);
            UUID nursery = net.createNode(nx + 3.0f * s, ny, nz - 4.5f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
            UUID fungus = net.createNode(nx - 3.0f * s, ny, nz - 6.5f * s, ChamberType.FUNGUS_GARDEN, 4.2f * s, 4.2f * s, 2.2f * s);
            UUID royal = net.createNode(nx, ny, nz - 9.0f * s, ChamberType.QUEEN_CHAMBER, 4.5f * s, 4.5f * s, 2.2f * s);
            net.createEdge(spireApex, subHub);
            net.createEdge(subHub, nursery);
            net.createEdge(subHub, fungus);
            net.createEdge(subHub, royal);
        } else if (a.contains("FUNGI") || a.contains("VAULT") || a.contains("ATTA")) {
            UUID cr1 = net.createNode(nx - 2.5f * s, ny, nz - 0.1f, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID cr2 = net.createNode(nx + 2.5f * s, ny, nz - 0.1f, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID hub = net.createNode(nx, ny, nz - 3.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.6f * s);
            UUID leafCache = net.createNode(nx + 3.5f * s, ny, nz - 4.5f * s, ChamberType.LEAF_CACHE, 3.4f * s, 3.4f * s, 1.8f * s);
            UUID fungus1 = net.createNode(nx - 4.0f * s, ny, nz - 7.5f * s, ChamberType.FUNGUS_GARDEN, 4.2f * s, 4.2f * s, 2.2f * s);
            UUID fungus2 = net.createNode(nx + 4.0f * s, ny, nz - 8.5f * s, ChamberType.FUNGUS_GARDEN, 4.4f * s, 4.4f * s, 2.4f * s);
            UUID royal = net.createNode(nx, ny, nz - 12.0f * s, ChamberType.QUEEN_CHAMBER, 4.8f * s, 4.8f * s, 2.4f * s);
            net.createEdge(cr1, hub);
            net.createEdge(cr2, hub);
            net.createEdge(hub, leafCache);
            net.createEdge(hub, fungus1);
            net.createEdge(hub, fungus2);
            net.createEdge(hub, royal);
        } else if (a.contains("MOUND") || a.contains("SURFACE_MOUND") || a.contains("SOLAR") || a.contains("RUFA")) {
            UUID ent1 = net.createNode(nx - 2.0f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.4f * s);
            UUID ent2 = net.createNode(nx + 2.0f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.4f * s);
            UUID solarium = net.createNode(nx, ny, nz + 3.0f * s, ChamberType.SOLARIUM, 3.8f * s, 3.8f * s, 2.0f * s);
            UUID hub = net.createNode(nx, ny, nz - 2.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
            UUID wintering = net.createNode(nx - 3.5f * s, ny, nz - 7.0f * s, ChamberType.HIBERNATION, 3.8f * s, 3.8f * s, 2.0f * s);
            UUID queen = net.createNode(nx + 2.5f * s, ny, nz - 10.0f * s, ChamberType.QUEEN_CHAMBER, 4.4f * s, 4.4f * s, 2.2f * s);
            net.createEdge(ent1, solarium);
            net.createEdge(ent2, solarium);
            net.createEdge(solarium, hub);
            net.createEdge(hub, wintering);
            net.createEdge(hub, queen);
        } else if (a.contains("SILK") || a.contains("LEAF") || a.contains("OECOPHYLLA") || a.contains("ARBOREAL_SILK")) {
            UUID branch = net.createNode(nx, ny, nz + 5.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID hub = net.createNode(nx, ny, nz + 7.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
            UUID pav1 = net.createNode(nx - 3.0f * s, ny, nz + 7.0f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID pav2 = net.createNode(nx + 3.0f * s, ny, nz + 7.0f * s, ChamberType.FOOD_STORAGE, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID royalPav = net.createNode(nx, ny, nz + 8.5f * s, ChamberType.QUEEN_CHAMBER, 4.2f * s, 4.2f * s, 2.0f * s);
            net.createEdge(branch, hub);
            net.createEdge(hub, pav1);
            net.createEdge(hub, pav2);
            net.createEdge(hub, royalPav);
        } else if (a.contains("CARTON")) {
            UUID ent = net.createNode(nx, ny, nz + 3.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID core = net.createNode(nx, ny, nz + 5.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
            UUID brood1 = net.createNode(nx - 2.5f * s, ny, nz + 6.0f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID food = net.createNode(nx + 2.5f * s, ny, nz + 6.0f * s, ChamberType.FOOD_STORAGE, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID queen = net.createNode(nx, ny, nz + 6.8f * s, ChamberType.QUEEN_CHAMBER, 4.0f * s, 4.0f * s, 2.0f * s);
            net.createEdge(ent, core);
            net.createEdge(core, brood1);
            net.createEdge(core, food);
            net.createEdge(core, queen);
        } else if (a.contains("BIVOUAC") || a.contains("ARMY") || a.contains("ECITON")) {
            UUID anchor = net.createNode(nx, ny, nz + 2.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID core = net.createNode(nx, ny, nz + 0.8f * s, ChamberType.QUEEN_CHAMBER, 4.4f * s, 4.4f * s, 2.2f * s);
            UUID brood = net.createNode(nx - 2.5f * s, ny, nz - 0.4f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
            UUID food = net.createNode(nx + 2.5f * s, ny, nz - 0.4f * s, ChamberType.FOOD_STORAGE, 3.4f * s, 3.4f * s, 1.8f * s);
            net.createEdge(anchor, core);
            net.createEdge(core, brood);
            net.createEdge(core, food);
        } else {
            buildYoungBurrowNest(net, nx, ny, nz, s);
        }
    }

    // ── 3. Mature Climax Architecture Router ───────────────────────────────────
    private static void buildMatureNestForArchitecture(TunnelNetwork net, float nx, float ny, float nz, String arch, float scale) {
        if (arch.contains("WOOD") || arch.contains("TREE") || arch.contains("TRUNK") || arch.contains("HOLLOW_TRUNK")) {
            buildHollowTrunkNest(net, nx, ny, nz, scale);
        } else if (arch.contains("WAX_COMB") || arch.contains("BEEHIVE") || arch.contains("HEXAGONAL")) {
            buildHexagonalCombNest(net, nx, ny, nz, scale);
        } else if (arch.contains("WAX_POTS") || arch.contains("POTS_CLUSTER") || arch.contains("BOMBUS")) {
            buildWaxPotsClusterNest(net, nx, ny, nz, scale);
        } else if (arch.contains("PAPER") || arch.contains("PEDUNCULATE") || arch.contains("VESPA")) {
            buildPaperPedunculateNest(net, nx, ny, nz, scale);
        } else if (arch.contains("CATHEDRAL") || arch.contains("TERMITE") || arch.contains("STERCORAL")) {
            buildCathedralMoundNest(net, nx, ny, nz, scale);
        } else if (arch.contains("FUNGI") || arch.contains("VAULT") || arch.contains("ATTA")) {
            buildSubterraneanFungiVaultNest(net, nx, ny, nz, scale);
        } else if (arch.contains("MOUND") || arch.contains("SURFACE_MOUND") || arch.contains("SOLAR") || arch.contains("RUFA")) {
            buildSurfaceMoundNest(net, nx, ny, nz, scale);
        } else if (arch.contains("SILK") || arch.contains("LEAF") || arch.contains("OECOPHYLLA") || arch.contains("ARBOREAL_SILK")) {
            buildArborealSilkLeafNest(net, nx, ny, nz, scale);
        } else if (arch.contains("CARTON")) {
            buildCartonNest(net, nx, ny, nz, scale);
        } else if (arch.contains("BAMBOO") || arch.contains("STEM") || arch.contains("GALL")) {
            buildBambooStemNest(net, nx, ny, nz, scale);
        } else if (arch.contains("BIVOUAC") || arch.contains("ARMY") || arch.contains("ECITON")) {
            buildBivouacLivingNest(net, nx, ny, nz, scale);
        } else if (arch.contains("SUPERCOLONY") || arch.contains("SUPERCOLONIE")) {
            buildSupercolonyNetworkNest(net, nx, ny, nz, scale);
        } else {
            buildMatureUndergroundBurrow(net, nx, ny, nz, scale);
        }
    }

    /**
     * Checks colony growth and seamlessly triggers ontogenetic nest expansion
     * as population and building materials increase over time.
     */

    public static void buildNetworkForArchitecture(TunnelNetwork net, float nx, float ny, float nz, String nestType) {
        String arch = nestType.toUpperCase();
        float scale = Math.max(0.5f, net.getScaleFactorSetting());

        if (arch.contains("WOOD") || arch.contains("TREE") || arch.contains("TRUNK") || arch.contains("HOLLOW_TRUNK")) {
            buildHollowTrunkNest(net, nx, ny, nz, scale);
        } else if (arch.contains("WAX_COMB") || arch.contains("BEEHIVE") || arch.contains("HEXAGONAL")) {
            buildHexagonalCombNest(net, nx, ny, nz, scale);
        } else if (arch.contains("WAX_POTS") || arch.contains("POTS_CLUSTER") || arch.contains("BOMBUS")) {
            buildWaxPotsClusterNest(net, nx, ny, nz, scale);
        } else if (arch.contains("PAPER") || arch.contains("PEDUNCULATE") || arch.contains("VESPA")) {
            buildPaperPedunculateNest(net, nx, ny, nz, scale);
        } else if (arch.contains("CATHEDRAL") || arch.contains("TERMITE") || arch.contains("STERCORAL")) {
            buildCathedralMoundNest(net, nx, ny, nz, scale);
        } else if (arch.contains("FUNGI") || arch.contains("VAULT") || arch.contains("ATTA")) {
            buildSubterraneanFungiVaultNest(net, nx, ny, nz, scale);
        } else if (arch.contains("MOUND") || arch.contains("SURFACE_MOUND") || arch.contains("SOLAR") || arch.contains("RUFA")) {
            buildSurfaceMoundNest(net, nx, ny, nz, scale);
        } else if (arch.contains("SILK") || arch.contains("LEAF") || arch.contains("OECOPHYLLA") || arch.contains("ARBOREAL_SILK")) {
            buildArborealSilkLeafNest(net, nx, ny, nz, scale);
        } else if (arch.contains("CARTON")) {
            buildCartonNest(net, nx, ny, nz, scale);
        } else if (arch.contains("BAMBOO") || arch.contains("STEM") || arch.contains("GALL")) {
            buildBambooStemNest(net, nx, ny, nz, scale);
        } else if (arch.contains("BIVOUAC") || arch.contains("ARMY") || arch.contains("ECITON")) {
            buildBivouacLivingNest(net, nx, ny, nz, scale);
        } else if (arch.contains("SUPERCOLONY") || arch.contains("SUPERCOLONIE")) {
            buildSupercolonyNetworkNest(net, nx, ny, nz, scale);
        } else if (arch.contains("SIMPLE") || arch.contains("YOUNG") || arch.contains("JEUNE")) {
            buildYoungBurrowNest(net, nx, ny, nz, scale);
        } else {
            buildMatureUndergroundBurrow(net, nx, ny, nz, scale);
        }
    }

    // ── 1. Mature Subterranean Burrow (Lasius / Formica) ───────────────────────
    private static void buildMatureUndergroundBurrow(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID ent1 = net.createNode(nx - 2.5f * s, ny + 1.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID ent2 = net.createNode(nx + 2.5f * s, ny - 1.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID hub = net.createNode(nx, ny, nz - 3.0f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
        net.createEdge(ent1, hub);
        net.createEdge(ent2, hub);

        UUID shaft1 = net.createNode(nx + 0.8f * s, ny - 0.5f * s, nz - 6.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.4f * s);
        UUID shaft2 = net.createNode(nx - 0.8f * s, ny + 0.5f * s, nz - 10.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.4f * s);
        UUID shaft3 = net.createNode(nx + 0.5f * s, ny + 0.8f * s, nz - 14.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.4f * s);
        net.createEdge(hub, shaft1);
        net.createEdge(shaft1, shaft2);
        net.createEdge(shaft2, shaft3);

        // Food granaries (shallow)
        UUID food1 = net.createNode(nx + 4.5f * s, ny + 2.0f * s, nz - 4.5f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID food2 = net.createNode(nx - 4.0f * s, ny - 3.0f * s, nz - 5.5f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.9f * s);
        net.createEdge(shaft1, food1);
        net.createEdge(shaft1, food2);

        // Brood nurseries (mid depth)
        UUID brood1 = net.createNode(nx - 4.5f * s, ny + 3.5f * s, nz - 8.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood2 = net.createNode(nx + 4.0f * s, ny - 3.5f * s, nz - 9.5f * s, ChamberType.BROOD_CHAMBER, 3.5f * s, 3.5f * s, 1.8f * s);
        UUID brood3 = net.createNode(nx, ny + 5.0f * s, nz - 11.5f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        net.createEdge(shaft2, brood1);
        net.createEdge(shaft2, brood2);
        net.createEdge(shaft2, brood3);

        // Queen Royal Chamber (deep, protected, wide lenticular)
        UUID queen = net.createNode(nx + 3.0f * s, ny + 1.0f * s, nz - 15.5f * s, ChamberType.QUEEN_CHAMBER, 5.2f * s, 5.2f * s, 2.4f * s);
        net.createEdge(shaft3, queen);

        // Waste dump & Hibernation
        UUID waste = net.createNode(nx - 5.0f * s, ny - 4.0f * s, nz - 17.0f * s, ChamberType.WASTE_DUMP, 3.4f * s, 3.4f * s, 1.8f * s);
        UUID hiber = net.createNode(nx + 2.0f * s, ny - 4.5f * s, nz - 18.0f * s, ChamberType.HIBERNATION, 4.2f * s, 4.2f * s, 2.0f * s);
        net.createEdge(queen, waste);
        net.createEdge(shaft3, hiber);
    }

    // ── 2. Young Simple Burrow ────────────────────────────────────────────────
    private static void buildYoungBurrowNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        UUID ent = net.createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
        UUID shaft = net.createNode(nx + 0.5f * s, ny - 0.5f * s, nz - 4.0f * s, ChamberType.TUNNEL, 1.3f * s, 1.3f * s, 1.3f * s);
        net.createEdge(ent, shaft);

        UUID brood = net.createNode(nx - 3.0f * s, ny + 1.5f * s, nz - 5.0f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.6f * s);
        UUID food = net.createNode(nx + 3.0f * s, ny - 1.5f * s, nz - 4.5f * s, ChamberType.FOOD_STORAGE, 3.0f * s, 3.0f * s, 1.5f * s);
        UUID queen = net.createNode(nx, ny, nz - 8.5f * s, ChamberType.QUEEN_CHAMBER, 4.4f * s, 4.4f * s, 2.2f * s);
        UUID waste = net.createNode(nx - 2.5f * s, ny - 2.0f * s, nz - 9.5f * s, ChamberType.WASTE_DUMP, 2.8f * s, 2.8f * s, 1.4f * s);

        net.createEdge(shaft, brood);
        net.createEdge(shaft, food);
        net.createEdge(shaft, queen);
        net.createEdge(queen, waste);
    }

    // ── 3. Surface Thatch Mound (Formica rufa) ────────────────────────────────
    private static void buildSurfaceMoundNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
        // Mound surface entrances & Solarium (elevated above ground z > 0)
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

    // ── 4. Subterranean Fungi Vault (Atta leafcutters) ─────────────────────────
    private static void buildSubterraneanFungiVaultNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

        // Huge fungal chambers (domed/lenticular)
        UUID fungus1 = net.createNode(nx - 6.0f * s, ny + 3.0f * s, nz - 6.5f * s, ChamberType.FUNGUS_GARDEN, 4.8f * s, 4.8f * s, 2.5f * s);
        UUID fungus2 = net.createNode(nx + 6.0f * s, ny - 3.0f * s, nz - 7.5f * s, ChamberType.FUNGUS_GARDEN, 5.0f * s, 5.0f * s, 2.6f * s);
        UUID fungus3 = net.createNode(nx - 5.5f * s, ny - 4.0f * s, nz - 11.5f * s, ChamberType.FUNGUS_GARDEN, 5.2f * s, 5.2f * s, 2.8f * s);
        UUID fungus4 = net.createNode(nx + 5.5f * s, ny + 4.0f * s, nz - 12.5f * s, ChamberType.FUNGUS_GARDEN, 5.0f * s, 5.0f * s, 2.6f * s);
        net.createEdge(midShaft, fungus1);
        net.createEdge(midShaft, fungus2);
        net.createEdge(lowShaft, fungus3);
        net.createEdge(lowShaft, fungus4);

        // Brood nestled near fungi
        UUID brood1 = net.createNode(nx, ny - 5.5f * s, nz - 9.5f * s, ChamberType.BROOD_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID brood2 = net.createNode(nx, ny + 5.5f * s, nz - 10.5f * s, ChamberType.BROOD_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
        net.createEdge(midShaft, brood1);
        net.createEdge(midShaft, brood2);

        // Deep Queen Royal Crypt
        UUID queen = net.createNode(nx, ny, nz - 16.0f * s, ChamberType.QUEEN_CHAMBER, 5.5f * s, 5.5f * s, 2.6f * s);
        net.createEdge(lowShaft, queen);

        // Isolated deep waste pits
        UUID waste1 = net.createNode(nx - 7.0f * s, ny, nz - 18.5f * s, ChamberType.WASTE_DUMP, 4.0f * s, 4.0f * s, 2.0f * s);
        UUID waste2 = net.createNode(nx + 7.0f * s, ny, nz - 18.5f * s, ChamberType.WASTE_DUMP, 4.0f * s, 4.0f * s, 2.0f * s);
        net.createEdge(queen, waste1);
        net.createEdge(queen, waste2);
    }

    // ── 5. Cathedral Termite Mound (Isoptera) ───────────────────────────────────
    private static void buildCathedralMoundNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

    // ── 6. Complex Supercolony Network (Linepithema) ───────────────────────────
    private static void buildSupercolonyNetworkNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

        // Polygyne queens
        UUID queen1 = net.createNode(nx, ny, nz - 13.0f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.4f * s);
        UUID queen2 = net.createNode(nx - 10.0f * s, ny + 5.0f * s, nz - 10.5f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.2f * s);
        UUID queen3 = net.createNode(nx + 10.0f * s, ny - 5.0f * s, nz - 10.5f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.2f * s);
        net.createEdge(centerShaft, queen1);
        net.createEdge(leftShaft, queen2);
        net.createEdge(rightShaft, queen3);

        // Brood clusters
        UUID brood1 = net.createNode(nx - 4.0f * s, ny + 2.5f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood2 = net.createNode(nx + 4.0f * s, ny - 2.5f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood3 = net.createNode(nx - 12.0f * s, ny + 2.0f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood4 = net.createNode(nx + 12.0f * s, ny - 2.0f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        net.createEdge(centerShaft, brood1);
        net.createEdge(centerShaft, brood2);
        net.createEdge(leftShaft, brood3);
        net.createEdge(rightShaft, brood4);

        // Granaries & Storage
        UUID food1 = net.createNode(nx + 4.5f * s, ny + 4.5f * s, nz - 7.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID food2 = net.createNode(nx - 4.5f * s, ny - 4.5f * s, nz - 7.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID food3 = net.createNode(nx, ny, nz - 16.5f * s, ChamberType.FOOD_STORAGE, 4.2f * s, 4.2f * s, 2.2f * s);
        net.createEdge(centerShaft, food1);
        net.createEdge(centerShaft, food2);
        net.createEdge(queen1, food3);

        UUID waste = net.createNode(nx, ny + 8.0f * s, nz - 19.0f * s, ChamberType.WASTE_DUMP, 3.8f * s, 3.8f * s, 1.8f * s);
        net.createEdge(food3, waste);
    }

    // ── 7. Hexagonal Wax Comb (Apis mellifera) ──────────────────────────────────
    private static void buildHexagonalCombNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

    // ── 8. Wax Pots Cluster (Bombus terrestris) ─────────────────────────────────
    private static void buildWaxPotsClusterNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

    // ── 9. Hanging Paper Nest (Vespula) ─────────────────────────────────────────
    private static void buildPaperPedunculateNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

    // ── 10. Arboreal Silk Leaf Nest (Oecophylla) ────────────────────────────────
    private static void buildArborealSilkLeafNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

    // ── 11. Carton Nest (Crematogaster) ─────────────────────────────────────────
    private static void buildCartonNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

    // ── 12. Bamboo Stem Nest (Colobopsis / Temnothorax) ─────────────────────────
    private static void buildBambooStemNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

    // ── 13. Living Bivouac Nest (Eciton Army Ants) ──────────────────────────────
    private static void buildBivouacLivingNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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

    // ── 14. Hollow Trunk Nest (Camponotus Carpenter Ants) ──────────────────────
    private static void buildHollowTrunkNest(TunnelNetwork net, float nx, float ny, float nz, float s) {
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
