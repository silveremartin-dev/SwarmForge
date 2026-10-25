/*
 * SwarmForge - Ant Colony & Insect Simulation Platform
 * Copyright (c) 2024-2026 Silvère Martin-Michiellot & SwarmForge Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.swarmforge.client.ui.weather;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.swarmforge.client.util.I18nManager;

/**
 * Natural Disasters Configuration Card for the Weather and Climate Editor.
 * Configures occurrence probabilities for fires, flash floods, sandstorms,
 * lightning, tornadoes, droughts, and hard freezes.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherDisastersEditorPane extends VBox {

    private final Map<String, Slider> disasterProbabilities = new LinkedHashMap<>();
    private final Runnable onFieldEdited;

    public WeatherDisastersEditorPane(Runnable onFieldEdited) {
        this.onFieldEdited = onFieldEdited;
        buildUI();
    }

    private void buildUI() {
        I18nManager i18n = I18nManager.getInstance();
        setSpacing(10);
        setPadding(new Insets(10));
        getStyleClass().add("card-pane");

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("weather.disasters.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label warning = new Label();
        warning.textProperty().bind(i18n.createStringBinding("weather.disasters.warning"));
        warning.setStyle("-fx-font-style: italic;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        String[][] disasters = {
                { "weather.disasters.fire", "1.0", "🔥 Fire & Lightning" },
                { "weather.disasters.flood", "2.0", "🌊 Flash Flood" },
                { "weather.disasters.sandstorm", "1.5", "🏜️ Sandstorm / Dust Storm" },
                { "weather.disasters.lightning", "3.0", "⚡ Lightning Strikes" },
                { "weather.disasters.tornado", "0.5", "🌪 Tornado" },
                { "weather.disasters.drought", "3.0", "🏜 Drought" },
                { "weather.disasters.freeze", "2.0", "❄️ Hard Freeze" }
        };

        int row = 0;
        for (String[] disaster : disasters) {
            Label label = new Label();
            label.textProperty().bind(i18n.createStringBinding(disaster[0]));
            label.setStyle("-fx-min-width: 140;");
            label.tooltipProperty().bind(i18n.createTooltipBinding("weather.disasters.slider.tt"));

            Slider slider = new Slider(0, 20, Double.parseDouble(disaster[1]));
            slider.setPrefWidth(200);
            slider.setShowTickLabels(true);
            slider.setMajorTickUnit(5);
            slider.tooltipProperty().bind(i18n.createTooltipBinding("weather.disasters.slider.tt"));
            slider.valueProperty().addListener((o, a, b) -> {
                if (onFieldEdited != null) onFieldEdited.run();
            });

            Label valueLabel = new Label();
            valueLabel.setStyle("-fx-min-width: 90;");
            valueLabel.tooltipProperty().bind(i18n.createTooltipBinding("weather.disasters.val.tt"));
            valueLabel.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
                    () -> String.format(Locale.US, "%.1f %s", slider.getValue(), i18n.get("weather.disasters.unit")),
                    slider.valueProperty(),
                    i18n.localeProperty()
            ));

            disasterProbabilities.put(disaster[2], slider);

            grid.add(label, 0, row);
            grid.add(slider, 1, row);
            grid.add(valueLabel, 2, row);
            row++;
        }

        getChildren().addAll(title, warning, grid);
    }

    public Map<String, Slider> getDisasterProbabilities() {
        return disasterProbabilities;
    }

    public Map<String, Double> getDisasterValues() {
        Map<String, Double> values = new LinkedHashMap<>();
        disasterProbabilities.forEach((k, v) -> values.put(k, v.getValue()));
        return values;
    }

    public void setDisasterValue(String key, double val) {
        Slider slider = disasterProbabilities.get(key);
        if (slider != null) {
            slider.setValue(val);
        }
    }
}
