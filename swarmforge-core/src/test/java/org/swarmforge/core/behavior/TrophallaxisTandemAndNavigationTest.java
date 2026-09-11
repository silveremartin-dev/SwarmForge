/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.species.FormicaRufa;
import org.swarmforge.core.species.CataglyphisBombycina;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the biological and physical mechanisms for:
 * 1. Dynamic Trophallaxis & Social Crop Liquid Exchange
 * 2. Tandem Running Dynamics & Tactile Recruitment
 * 3. Excavation Spoil & External Mound Deposition
 * 4. Celestial Polarized Light Compass & Path Integration
 */
public class TrophallaxisTandemAndNavigationTest {

    @Test
    @DisplayName("1. Dynamic Trophallaxis & Social Crop: Liquid transfer and CHC profile blending")
    void testSocialCropAndTrophallaxis() {
        UUID colonyId = UUID.randomUUID();
        Individual donor = new Individual(colonyId, Individual.Caste.FORAGER, 10f, 10f, 0f);
        Individual recipient = new Individual(colonyId, Individual.Caste.NURSE, 10.2f, 10f, 0f);

        donor.setChcProfile(new float[]{0.9f, 0.1f, 0.8f, 0.2f});
        recipient.setChcProfile(new float[]{0.1f, 0.9f, 0.2f, 0.8f});

        float initialDissimilarity = donor.calculateChcDissimilarity(recipient);
        assertTrue(initialDissimilarity > 0.5f, "Initial CHC profiles should be distinct");

        // Fill donor social crop with nectar
        donor.fillSocialCrop(15.0f, ResourceType.NECTAR);
        assertEquals(15.0f, donor.getSocialCropAmount(), 0.001f);
        assertEquals(ResourceType.NECTAR, donor.getSocialCropResourceType());

        // Perform trophallaxis exchange
        boolean success = Interaction.trophallaxis(donor, recipient);
        assertTrue(success, "Trophallaxis exchange should succeed");

        // Donor should have regurgitated liquid, recipient should have received it
        assertTrue(donor.getSocialCropAmount() < 15.0f, "Donor crop amount should decrease");
        assertTrue(recipient.getSocialCropAmount() > 0.0f, "Recipient crop should now contain liquid");
        assertEquals(ResourceType.NECTAR, recipient.getSocialCropResourceType());

        // CHC profiles should have blended (decreased dissimilarity)
        float postDissimilarity = donor.calculateChcDissimilarity(recipient);
        assertTrue(postDissimilarity < initialDissimilarity, "CHC dissimilarity should decrease after trophallaxis");
    }

    @Test
    @DisplayName("2. Tandem Running Dynamics: Pairing, leader/follower roles and decoupling")
    void testTandemRunningPairingAndRoles() {
        UUID colonyId = UUID.randomUUID();
        Individual leader = new Individual(colonyId, Individual.Caste.WORKER, 20f, 20f, 0f);
        Individual follower = new Individual(colonyId, Individual.Caste.WORKER, 20.2f, 20f, 0f);

        leader.pairTandem(follower, true);

        assertTrue(leader.isTandemLeader(), "Leader should have LEADER role");
        assertTrue(follower.isTandemFollower(), "Follower should have FOLLOWER role");
        assertEquals(follower.getId(), leader.getTandemPartnerId());
        assertEquals(leader.getId(), follower.getTandemPartnerId());

        // Break tandem
        leader.breakTandem();
        assertFalse(leader.isTandemLeader());
    }

    @Test
    @DisplayName("3. Excavation Spoil & Mound Deposition: Earth pellet deposition raises local terrain")
    void testExcavationMoundDeposition() {
        Terrarium terrarium = new Terrarium(32, 32, 16);
        // Fill lower half with earth
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 5; z++) {
                    terrarium.setCell(TerrariumCell.earth(x, y, z));
                }
            }
        }

        FormicaRufa species = new FormicaRufa();
        Colony colony = new Colony(species, 16f, 16f, 5f);
        colony.setTerrarium(terrarium);
        terrarium.addColony(colony);

        float initialSurfaceZ = terrarium.getSurfaceElevation(16f, 16f);

        Individual builder = new Individual(colony.getId(), Individual.Caste.WORKER, 16f, 16f, initialSurfaceZ);
        builder.setCarriedItem(Individual.CarriedItem.EARTH);

        assertTrue(builder.isNearHomeSurface(colony), "Builder should be on surface near home");

        // Execute move or deposit
        builder.depositEarthMound(colony);

        assertEquals(Individual.CarriedItem.NONE, builder.getCarriedItem(), "Earth pellet should be deposited");
        float updatedSurfaceZ = terrarium.getSurfaceElevation(16f, 16f);
        assertTrue(updatedSurfaceZ >= initialSurfaceZ, "Surface elevation at nest mound should increase or stay solid");
    }

    @Test
    @DisplayName("4. Celestial Polarized Light & Path Integration: Displacement vector and return heading")
    void testPathIntegrationDeadReckoning() {
        UUID colonyId = UUID.randomUUID();
        Individual desertAnt = new Individual(colonyId, Individual.Caste.FORAGER, 50f, 50f, 0f);
        desertAnt.setSpecies(new CataglyphisBombycina());

        desertAnt.resetPathIntegration();
        assertEquals(0.0f, desertAnt.getPathIntegrationDistance(), 0.001f);

        // Forage outbound 10m East, 10m North
        desertAnt.integrateDisplacement(10.0f, 10.0f);

        assertEquals(Math.sqrt(200.0), desertAnt.getPathIntegrationDistance(), 0.01f);

        // Return heading should point directly South-West (-135 deg / -3*PI/4)
        float returnHeading = desertAnt.getPathIntegrationReturnHeading();
        double expectedAngle = Math.atan2(-10.0, -10.0);
        assertEquals(expectedAngle, returnHeading, 0.001f, "Return heading should point directly toward home");

        // Upon reaching nest
        desertAnt.resetPathIntegration();
        assertEquals(0.0f, desertAnt.getPathIntegrationDistance(), 0.001f);
    }
}
