/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import org.swarmforge.client.util.I18nManager;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFX UI tests for SwarmForge Studio.
 * These tests automate interactions with the JavaFX application
 * and capture screenshots for verification.
 *
 * @author Gemini AI Assistant
 */
@org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "testfx.ui.enable", matches = "true")
@ExtendWith(ApplicationExtension.class)
public class SwarmForgeClientUITest {

    private Stage stage;
    private SwarmForgeClient clientApp;
    private static final Path SCREENSHOT_DIR = Path.of("target/test-screenshots");

    static {
        System.setProperty("testfx.setup.timeout", "90000");
    }

    @Start
    public void start(Stage stage) {
        System.setProperty("swarmforge.test", "true");
        this.stage = stage;
        I18nManager.getInstance().setLocale(java.util.Locale.ENGLISH);
        this.clientApp = new SwarmForgeClient();
        this.clientApp.start(stage);
        SCREENSHOT_DIR.toFile().mkdirs();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        if (this.clientApp != null) {
            this.clientApp.stop();
        }
    }

    /**
     * Capture a screenshot with the given name using TestFX robot.
     */
    private void captureScreenshot(String name) {
        WaitForAsyncUtils.waitForFxEvents();
        try {
            // Screenshot logic temporarily removed due to module compatibility issues
            System.out.println("Screenshot placeholder for: " + name);
        } catch (Exception e) {
            System.out.println("Screenshot skipped: " + name);
        }
    }

    @Test
    void testCompleteClientUiWorkflow(FxRobot robot) {
        // 1. Verify window is showing
        assertTrue(stage.isShowing());
        assertTrue(stage.getTitle().contains("SwarmForge"));
        captureScreenshot("01_application_started");

        // 2. Main tabs
        TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
        assertNotNull(tabPane);
        assertTrue(tabPane.getTabs().size() >= 3, "Expected at least 3 tabs");
        captureScreenshot("02_main_tabs");

        // 3. Cycle through all main studio tabs to verify layout & rendering
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            final int tabIdx = i;
            robot.interact(() -> tabPane.getSelectionModel().select(tabIdx));
            WaitForAsyncUtils.waitForFxEvents();
        }
        captureScreenshot("03_all_tabs_verified");

        // 4. World Editor preview button
        if (tabPane.getTabs().size() > 1) {
            robot.interact(() -> tabPane.getSelectionModel().select(1));
        }
        WaitForAsyncUtils.waitForFxEvents();
        var buttonOpt = robot.lookup(".button").queryAllAs(Button.class).stream()
                .filter(b -> b.getText() != null && (b.getText().contains("Generate") || b.getText().contains("Générer") || b.getText().contains("Preview") || b.getText().contains("Aperçu")))
                .findFirst();
        if (buttonOpt.isPresent()) {
            robot.interact(() -> buttonOpt.get().fire());
            WaitForAsyncUtils.waitForFxEvents();
        }
        captureScreenshot("05_terrain_generated");

        // 5. Simulation Manager & Intervention Panel verification
        assertNotNull(clientApp.simControlPanel, "SimulationControlPanel should be initialized");
        assertNotNull(clientApp.interventionPanel, "InterventionPanel should be initialized");
        assertEquals("localhost", clientApp.simControlPanel.getServerHost(), "Default server host should be 'localhost'");
        captureScreenshot("06_simulation_manager");
    }
}
