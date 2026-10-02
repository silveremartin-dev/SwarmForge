package org.swarmforge.client.util;

import org.junit.jupiter.api.Test;
import org.swarmforge.client.ui.NestPresetManager;
import org.swarmforge.client.ui.SpeciesPresetManager;
import org.swarmforge.client.ui.WeatherPresetManager;
import org.swarmforge.client.ui.WorldPresetManager;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.core.species.DefaultSpecies;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonDataIntegrityTest {

    @Test
    public void testJsonAcademicScenarios() {
        List<Scenario> academic = AcademicScenarios.getAllAcademicScenarios(42L);
        assertNotNull(academic);
        assertEquals(16, academic.size());
        for (Scenario s : academic) {
            assertNotNull(s.getId());
            assertNotNull(s.getTitle());
            assertFalse(s.getColonies().isEmpty());
        }
    }

    @Test
    public void testJsonMultiplayerScenarios() {
        List<Scenario> mp = AcademicScenarios.getAllMultiplayerScenarios(42L);
        assertNotNull(mp);
        assertEquals(3, mp.size());
        for (Scenario s : mp) {
            assertNotNull(s.getId());
            assertTrue(s.isMultiplayerOnly());
        }
    }

    @Test
    public void testJsonSpeciesPresets() {
        SpeciesPresetManager mgr = new SpeciesPresetManager();
        assertFalse(mgr.getPresetNames().isEmpty());
        assertTrue(mgr.getPresetNames().size() >= 15);

        DefaultSpecies lasius = mgr.getPresetOrFallback("Lasius niger");
        assertNotNull(lasius);
        assertEquals("ANT", lasius.getInsectType());

        DefaultSpecies apis = mgr.getPresetOrFallback("Apis mellifera");
        assertNotNull(apis);
    }

    @Test
    public void testJsonNestPresets() {
        NestPresetManager mgr = new NestPresetManager();
        assertFalse(mgr.names().isEmpty());
        assertTrue(mgr.names().size() >= 10);
    }

    @Test
    public void testJsonWorldPresets() {
        WorldPresetManager mgr = new WorldPresetManager();
        assertFalse(mgr.names().isEmpty());
        assertTrue(mgr.names().size() >= 8);
    }

    @Test
    public void testJsonWeatherPresets() {
        WeatherPresetManager mgr = new WeatherPresetManager();
        assertFalse(mgr.names().isEmpty());
        assertTrue(mgr.names().size() >= 8);
    }

    @Test
    public void testJsonAccessoryPresets() {
        org.swarmforge.client.ui.AccessoryPresetManager mgr = new org.swarmforge.client.ui.AccessoryPresetManager();
        assertFalse(mgr.getPresetNames().isEmpty());
        assertTrue(mgr.getPresetNames().size() >= 20, "Expected at least 20 accessory/prey presets, found: " + mgr.getPresetNames().size());

        var spider = mgr.get("predator-spider-salticidae");
        assertTrue(spider.isPresent());
        assertEquals(org.swarmforge.core.domain.AccessoryPreset.Category.PREDATOR, spider.get().getCategory());
        assertEquals(org.swarmforge.core.domain.AccessoryPreset.HuntMode.AMBUSH, spider.get().getHuntMode());

        var aphid = mgr.get("mutualist-aphid-cinara");
        assertTrue(aphid.isPresent());
        assertEquals(org.swarmforge.core.domain.AccessoryPreset.Category.APHID_MUTUALIST, aphid.get().getCategory());
    }
}
