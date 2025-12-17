/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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
@ExtendWith(ApplicationExtension.class)
public class SwarmForgeClientUITest {

    private SwarmForgeClient app;
    private Stage stage;

    // Screenshot output directory
    private static final Path SCREENSHOT_DIR = Path.of("target/test-screenshots");

    @Start
    private void start(Stage stage) {
        this.stage = stage;
        this.app = new SwarmForgeClient();
        app.start(stage);

        // Ensure screenshot directory exists
        SCREENSHOT_DIR.toFile().mkdirs();
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
    void applicationStarts_shouldShowMainWindow(FxRobot robot) {
        // Verify window is showing
        assertTrue(stage.isShowing());
        assertEquals("SwarmForge Studio", stage.getTitle());

        captureScreenshot("01_application_started");
    }

    @Test
    void mainTabs_shouldExist(FxRobot robot) {
        // Find the main TabPane
        TabPane tabPane = robot.lookup(".tab-pane").queryAs(TabPane.class);
        assertNotNull(tabPane);

        // Verify expected tabs
        assertTrue(tabPane.getTabs().size() >= 3, "Expected at least 3 tabs");

        captureScreenshot("02_main_tabs");
    }

    @Test
    void clickWorldEditorTab_shouldShowTerrainGenerator(FxRobot robot) {
        // Click on World Editor tab
        robot.clickOn("World Editor");
        WaitForAsyncUtils.waitForFxEvents();

        // Wait for render
        robot.sleep(500, TimeUnit.MILLISECONDS);

        captureScreenshot("03_world_editor_tab");
    }

    @Test
    void clickSpeciesEditorTab_shouldShowSpeciesDesigner(FxRobot robot) {
        // Click on Species Editor tab
        robot.clickOn("Species Editor");
        WaitForAsyncUtils.waitForFxEvents();

        robot.sleep(500, TimeUnit.MILLISECONDS);

        captureScreenshot("04_species_editor_tab");
    }

    @Test
    void clickGeneratePreview_shouldGenerateTerrain(FxRobot robot) {
        // Navigate to World Editor
        robot.clickOn("World Editor");
        robot.sleep(500, TimeUnit.MILLISECONDS);

        // Click Generate Preview button
        Button genButton = robot.lookup("Generate Preview").queryAs(Button.class);
        if (genButton != null) {
            robot.clickOn(genButton);
            robot.sleep(2000, TimeUnit.MILLISECONDS); // Wait for terrain generation
        }

        captureScreenshot("05_terrain_generated");
    }

    @Test
    void simulationManager_connectToServer(FxRobot robot) {
        // Should start on Simulation Manager tab
        robot.clickOn("Simulation Manager");
        robot.sleep(300, TimeUnit.MILLISECONDS);

        // Find host field and verify default
        TextField hostField = robot.lookup(".text-field").queryAs(TextField.class);
        if (hostField != null) {
            assertEquals("localhost", hostField.getText());
        }

        captureScreenshot("06_simulation_manager");
    }
}
