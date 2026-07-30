/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.function.Function;

/**
 * Utility helper to attach rich tooltips and localized descriptive titles
 * to JavaFX ComboBox drop-down items and button headers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ComboBoxTooltipHelper {

    /**
     * Configures a ComboBox with custom string mapping, cell rendering, and hover tooltips.
     *
     * @param combo The ComboBox to configure
     * @param titleMapper Function returning a clean localized title for each item
     * @param descriptionMapper Function returning a detailed technical description for each item
     * @param <T> Item type
     */
    public static <T> void setupDescriptiveComboBox(
            ComboBox<T> combo,
            Function<T, String> titleMapper,
            Function<T, String> descriptionMapper
    ) {
        if (combo == null) return;

        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(T object) {
                if (object == null) return "";
                try {
                    return titleMapper.apply(object);
                } catch (Exception ex) {
                    return object.toString();
                }
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });

        // Popup List Cell Factory
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    String title = titleMapper.apply(item);
                    String desc = descriptionMapper.apply(item);
                    setText(title);

                    Tooltip tt = createFormattedTooltip(title, desc);
                    setTooltip(tt);
                }
            }
        });

        // Selected Item Button Cell Factory (Header)
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    String title = titleMapper.apply(item);
                    String desc = descriptionMapper.apply(item);
                    setText(title);

                    Tooltip tt = createFormattedTooltip(title, desc);
                    setTooltip(tt);
                }
            }
        });
    }

    private static Tooltip createFormattedTooltip(String title, String desc) {
        Tooltip tt = new Tooltip(title + "\n────────────────────────────\n" + desc);
        tt.setMaxWidth(380);
        tt.setWrapText(true);
        tt.setShowDelay(Duration.millis(100));
        tt.setShowDuration(Duration.seconds(12));
        tt.setStyle("-fx-font-size: 11px; -fx-font-family: 'Segoe UI', sans-serif; -fx-background-color: #0f172a; -fx-text-fill: #38bdf8; -fx-border-color: #0284c7; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 8, 0, 0, 4);");
        return tt;
    }
}
