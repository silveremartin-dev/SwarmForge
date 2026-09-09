package org.swarmforge.client.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class I18nManagerTest {

    private I18nManager i18n;

    @BeforeEach
    void setUp() {
        i18n = I18nManager.getInstance();
    }

    @Test
    void testApostrophePreservedInFrench() {
        i18n.setLocale(Locale.FRENCH);
        String msg = i18n.get("accessory.delete.confirm_msg");
        assertTrue(msg.contains("d'espèce"), "Apostrophe should not be stripped from 'd\\'espèce', got: " + msg);
        assertFalse(msg.contains("despèce"), "Should not contain 'despèce'");

        String msg2 = i18n.get("accessory.field.category.tt");
        assertTrue(msg2.contains("l'espèce"), "Apostrophe should not be stripped from 'l\\'espèce', got: " + msg2);
        assertTrue(msg2.contains("l'écosystème"), "Apostrophe should not be stripped from 'l\\'écosystème', got: " + msg2);
        assertFalse(msg2.contains("lespèce"), "Should not contain 'lespèce'");
    }

    @Test
    void testWorldEditorKeysInFrench() {
        i18n.setLocale(Locale.FRENCH);
        assertEquals("Rendu 3D & Options de Couches :", i18n.get("world.render_layers.title"));
        assertEquals("Visibilité des Couches :", i18n.get("world.layer_visibility.title"));
        assertEquals("Matière Organique", i18n.get("world.layer.organic"));
        assertEquals("Terre / Humus", i18n.get("world.layer.earth"));
        assertEquals("Sable", i18n.get("world.layer.sand"));
        assertEquals("Argile", i18n.get("world.layer.clay"));
        assertEquals("Limon", i18n.get("world.layer.silt"));
        assertEquals("Tourbe", i18n.get("world.layer.peat"));
        assertEquals("Gravier / Cailloux", i18n.get("world.layer.gravel"));
        assertEquals("Pierre / Roche", i18n.get("world.layer.stone"));
        assertEquals("Adapter le Monde", i18n.get("world.geo.adapt"));
        assertEquals("Synchroniser SIG", i18n.get("world.geo.sync"));
    }

    @Test
    void testVoxelHoverSubstrateKeys() {
        i18n.setLocale(Locale.FRENCH);
        assertEquals("Humus (Terre Végétale)", i18n.get("world.mat.humus"));
        assertEquals("Sable Xérique", i18n.get("world.mat.sand"));
        assertEquals("Argile Limoneuse", i18n.get("world.mat.clay"));
        assertEquals("Cavité / Galerie Excavée", i18n.get("world.mat.cavity"));
        assertEquals("Terre / Sol", i18n.get("world.mat.earth"));

        i18n.setLocale(Locale.ENGLISH);
        assertEquals("Humus (Topsoil)", i18n.get("world.mat.humus"));
        assertEquals("Xeric Sand", i18n.get("world.mat.sand"));
        assertEquals("Excavated Gallery / Cavity", i18n.get("world.mat.cavity"));
    }

    @Test
    void testPlaybackAndSpeedKeys() {
        i18n.setLocale(Locale.FRENCH);
        assertEquals("Horloge, Vitesse & Contrôles de Lecture", i18n.get("sim.playback.header"));
        assertEquals("Vitesse :", i18n.get("sim.speed.label"));
        assertTrue(i18n.get("sim.btn.play.tt").contains("Démarrer"));
        assertTrue(i18n.get("sim.btn.rewind.tt").contains("Rembobiner"));

        i18n.setLocale(Locale.ENGLISH);
        assertEquals("Time, Speed & Playback Controls", i18n.get("sim.playback.header"));
        assertEquals("Speed :", i18n.get("sim.speed.label"));
        assertTrue(i18n.get("sim.btn.play.tt").contains("Start"));
    }

    @Test
    void testGodModeKeys() {
        i18n.setLocale(Locale.FRENCH);
        assertEquals("Entités & Couvain", i18n.get("god.cat.entities"));
        assertEquals("Catastrophes", i18n.get("god.cat.disaster"));
        assertEquals("Exécuté", i18n.get("god.status.executed"));
        assertEquals("Suspendu", i18n.get("god.status.paused"));
        assertEquals("En attente", i18n.get("god.status.pending"));

        i18n.setLocale(Locale.ENGLISH);
        assertEquals("Entities & Brood", i18n.get("god.cat.entities"));
        assertEquals("Disaster", i18n.get("god.cat.disaster"));
        assertEquals("Executed", i18n.get("god.status.executed"));
    }
}
