package org.swarmforge.client.ui;

import java.util.function.Consumer;

/**
 * Universal Glossary & Pedagogical Navigation Router for SwarmForge.
 * Directs all contextual help requests to the primary embedded Glossary Tab view
 * in the editor interface, eliminating modal popups for a seamless user experience.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GlossaryDialog {

    private static Consumer<String> navigationHandler;

    public static void setNavigationHandler(Consumer<String> handler) {
        navigationHandler = handler;
    }

    public static void show() {
        show(null);
    }

    public static void show(String searchTerm) {
        if (navigationHandler != null) {
            navigationHandler.accept(searchTerm);
        } else {
            System.err.println("[GlossaryDialog] Navigation handler not registered for term: " + searchTerm);
        }
    }
}
