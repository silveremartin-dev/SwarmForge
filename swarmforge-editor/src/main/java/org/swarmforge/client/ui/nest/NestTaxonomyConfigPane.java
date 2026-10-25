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
package org.swarmforge.client.ui.nest;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;
import org.swarmforge.client.ui.GlossaryDialog;
import org.swarmforge.client.ui.NestRenderer;
import org.swarmforge.client.ui.SpeciesPresetManager;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.species.DefaultSpecies;

/**
 * Biological Taxonomy, Caste Morphology, and Architecture Paradigm Selector
 * for the Procedural Nest Generator.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestTaxonomyConfigPane extends VBox {

    private final SpeciesPresetManager speciesPresetMgr = new SpeciesPresetManager();

    private ComboBox<String> categorySelect;
    private ComboBox<String> genusSelect;
    private ComboBox<String> speciesModelCombo;
    private ComboBox<String> archSelect;
    private ComboBox<String> matSelect;
    private ComboBox<String> nestStageCombo;
    private Label speciesStatusLabel;

    private boolean isUpdatingSpeciesCombo = false;

    private final Runnable onManualParamChanged;
    private final Runnable onRegenAndRepaint;
    private final Consumer<DefaultSpecies> onSpeciesModelSelected;
    private final Consumer<Integer> onNestStageChanged;

    public NestTaxonomyConfigPane(
            Runnable onManualParamChanged,
            Runnable onRegenAndRepaint,
            Consumer<DefaultSpecies> onSpeciesModelSelected,
            Consumer<Integer> onNestStageChanged) {
        this.onManualParamChanged = onManualParamChanged;
        this.onRegenAndRepaint = onRegenAndRepaint;
        this.onSpeciesModelSelected = onSpeciesModelSelected;
        this.onNestStageChanged = onNestStageChanged;

        setSpacing(10);
        buildUI();
    }

    private void buildUI() {
        I18nManager i18n = I18nManager.getInstance();

        Label mainSpecsTitle = new Label();
        mainSpecsTitle.textProperty().bind(i18n.createStringBinding("nest.morphology.section_title"));
        mainSpecsTitle.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-padding:2 0 2 0;-fx-text-fill:#38bdf8;-fx-cursor:hand;");
        mainSpecsTitle.setOnMouseClicked(e -> GlossaryDialog.show("nest"));

        // 1. Insect Category / Family
        Label lblCat = new Label();
        lblCat.textProperty().bind(i18n.createStringBinding("nest.arch.category"));
        lblCat.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#a78bfa;-fx-underline:true;-fx-cursor:hand;");
        lblCat.setOnMouseClicked(e -> GlossaryDialog.show("nest"));
        categorySelect = new ComboBox<>();
        categorySelect.tooltipProperty().bind(i18n.createTooltipBinding("nest.category.select.tt"));
        populateCategorySelect();
        categorySelect.setPrefWidth(270);
        categorySelect.setOnAction(e -> {
            populateGenusCombo();
            filterSpeciesModelComboByCategory(categorySelect.getValue());
            onCategoryChanged();
            if (onManualParamChanged != null) onManualParamChanged.run();
        });

        // 2. Insect Genus (Taxonomy filter)
        Label lblGenus = new Label();
        lblGenus.textProperty().bind(i18n.createStringBinding("nest.genus.label"));
        lblGenus.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#c084fc;");
        genusSelect = new ComboBox<>();
        genusSelect.tooltipProperty().bind(i18n.createTooltipBinding("nest.genus.select.tt"));
        populateGenusCombo();
        genusSelect.setPrefWidth(270);
        genusSelect.setOnAction(e -> {
            filterSpeciesModelComboByCategory(categorySelect.getValue());
            if (onManualParamChanged != null) onManualParamChanged.run();
        });

        // 3. Species Reference Model
        Label lblSpecies = new Label();
        lblSpecies.textProperty().bind(i18n.createStringBinding("nest.arch.species_ref"));
        lblSpecies.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#38bdf8;");
        speciesModelCombo = new ComboBox<>();
        speciesModelCombo.tooltipProperty().bind(i18n.createTooltipBinding("nest.species.ref.tt"));
        populateSpeciesModelCombo();
        speciesModelCombo.setPrefWidth(270);
        speciesModelCombo.setOnAction(e -> {
            if (!isUpdatingSpeciesCombo) {
                handleSpeciesModelSelected();
            }
        });

        // 4. Nest Architecture Type
        Label lblArch = new Label();
        lblArch.textProperty().bind(i18n.createStringBinding("nest.arch.type"));
        lblArch.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#eab308;-fx-underline:true;-fx-cursor:hand;");
        lblArch.setOnMouseClicked(e -> GlossaryDialog.show("nest"));
        archSelect = new ComboBox<>();
        archSelect.tooltipProperty().bind(i18n.createTooltipBinding("nest.arch.type.tt"));
        populateArchSelect();
        archSelect.setPrefWidth(270);
        archSelect.setOnAction(e -> {
            if (onManualParamChanged != null) onManualParamChanged.run();
            if (onRegenAndRepaint != null) onRegenAndRepaint.run();
        });

        // 5. Construction Material
        Label lblMat = new Label();
        lblMat.textProperty().bind(i18n.createStringBinding("nest.arch.material"));
        lblMat.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#22c55e;-fx-underline:true;-fx-cursor:hand;");
        lblMat.setOnMouseClicked(e -> GlossaryDialog.show("materials"));
        matSelect = new ComboBox<>();
        matSelect.tooltipProperty().bind(i18n.createTooltipBinding("nest.arch.material.tt"));
        populateMatSelect();
        matSelect.setPrefWidth(270);
        matSelect.setOnAction(e -> {
            if (onManualParamChanged != null) onManualParamChanged.run();
            if (onRegenAndRepaint != null) onRegenAndRepaint.run();
        });

        // 6. Nest Development Stage / Maturity
        Label lblStage = new Label();
        lblStage.textProperty().bind(i18n.createStringBinding("nest.species.age"));
        lblStage.setStyle("-fx-font-weight:bold;-fx-font-size:11px;-fx-text-fill:#ec4899;");
        nestStageCombo = new ComboBox<>();
        nestStageCombo.tooltipProperty().bind(i18n.createTooltipBinding("nest.species.age.tt"));
        populateStageSelect();
        nestStageCombo.setPrefWidth(270);
        nestStageCombo.setOnAction(e -> {
            if (!isUpdatingSpeciesCombo && onNestStageChanged != null) {
                onNestStageChanged.accept(nestStageCombo.getSelectionModel().getSelectedIndex());
            }
        });

        speciesStatusLabel = new Label(i18n.get("nest.species.status_synced"));
        speciesStatusLabel.setStyle("-fx-font-size:10;-fx-text-fill:#94a3b8;-fx-wrap-text:true;");

        getChildren().addAll(
                mainSpecsTitle,
                lblCat, categorySelect,
                lblGenus, genusSelect,
                lblSpecies, speciesModelCombo,
                speciesStatusLabel,
                lblArch, archSelect,
                lblMat, matSelect,
                lblStage, nestStageCombo
        );
    }

    public void populateCategorySelect() {
        if (categorySelect == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String cur = categorySelect.getValue();
        categorySelect.getItems().clear();
        categorySelect.getItems().addAll(
                i18n.get("nest.category.ants"),
                i18n.get("nest.category.bees"),
                i18n.get("nest.category.wasps"),
                i18n.get("nest.category.termites")
        );
        FXCollections.sort(categorySelect.getItems());
        if (cur != null && categorySelect.getItems().contains(cur)) {
            categorySelect.setValue(cur);
        } else {
            categorySelect.getSelectionModel().selectFirst();
        }
    }

    public void populateArchSelect() {
        if (archSelect == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String cur = archSelect.getValue();
        archSelect.getItems().clear();
        archSelect.getItems().addAll(
                i18n.get("nest.arch.arboreal_carton"),
                i18n.get("nest.arch.arboreal_silk"),
                i18n.get("nest.arch.bamboo"),
                i18n.get("nest.arch.bivouac"),
                i18n.get("nest.arch.cathedral"),
                i18n.get("nest.arch.hanging_paper"),
                i18n.get("nest.arch.wax_comb"),
                i18n.get("nest.arch.hollow_trunk"),
                i18n.get("nest.arch.subterranean"),
                i18n.get("nest.arch.fungi_vault"),
                i18n.get("nest.arch.surface_dome"),
                i18n.get("nest.arch.wax_pots"),
                i18n.get("nest.arch.wooden_beehive")
        );
        FXCollections.sort(archSelect.getItems());
        if (cur != null && archSelect.getItems().contains(cur)) {
            archSelect.setValue(cur);
        } else {
            archSelect.getSelectionModel().selectFirst();
        }
    }

    public void populateMatSelect() {
        if (matSelect == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String cur = matSelect.getValue();
        matSelect.getItems().clear();
        matSelect.getItems().addAll(
                i18n.get("nest.mat.earth"),
                i18n.get("nest.mat.paper"),
                i18n.get("nest.mat.wax"),
                i18n.get("nest.mat.cement"),
                i18n.get("nest.mat.carton"),
                i18n.get("nest.mat.silk"),
                i18n.get("nest.mat.propolis"),
                i18n.get("nest.mat.wood"),
                i18n.get("nest.mat.living")
        );
        FXCollections.sort(matSelect.getItems());
        if (cur != null && matSelect.getItems().contains(cur)) {
            matSelect.setValue(cur);
        } else {
            matSelect.getSelectionModel().selectFirst();
        }
    }

    public void populateStageSelect() {
        if (nestStageCombo == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String cur = nestStageCombo.getValue();
        nestStageCombo.getItems().clear();
        nestStageCombo.getItems().addAll(
                i18n.get("nest.stage.foundation"),
                i18n.get("nest.stage.established"),
                i18n.get("nest.stage.mature"),
                i18n.get("nest.stage.giant")
        );
        if (cur != null && nestStageCombo.getItems().contains(cur)) {
            nestStageCombo.setValue(cur);
        } else {
            nestStageCombo.getSelectionModel().select(2); // MATURE
        }
    }

    public void populateGenusCombo() {
        if (genusSelect == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String cat = categorySelect != null ? categorySelect.getValue() : null;
        String curVal = genusSelect.getValue();
        genusSelect.getItems().clear();
        genusSelect.getItems().add(i18n.get("nest.genus.all"));

        String type = getCategoryType(cat);
        if ("ANT".equals(type)) {
            genusSelect.getItems().addAll(
                    "Atta (Leafcutter Ants)",
                    "Camponotus (Carpenter Ants)",
                    "Lasius (Black Garden Ants)",
                    "Pogonomyrmex (Harvester Ants)",
                    "Solenopsis (Fire Ants)",
                    "Crematogaster (Carton Ants)",
                    "Temnothorax (Twig Ants)",
                    "Eciton (Army Ants)"
            );
        } else if ("BEE".equals(type)) {
            genusSelect.getItems().addAll("Apis (Honeybees)", "Bombus (Bumblebees)");
        } else if ("WASP".equals(type)) {
            genusSelect.getItems().addAll("Vespula (Common Wasps)", "Vespa (Hornets)");
        } else if ("TERMITE".equals(type)) {
            genusSelect.getItems().addAll("Macrotermes (Cathedral Termites)", "Reticulitermes (Subterranean Termites)");
        }
        if (curVal != null && genusSelect.getItems().contains(curVal)) {
            genusSelect.setValue(curVal);
        } else {
            genusSelect.getSelectionModel().selectFirst();
        }
    }

    public void populateSpeciesModelCombo() {
        filterSpeciesModelComboByCategory(categorySelect != null ? categorySelect.getValue() : null);
    }

    public void filterSpeciesModelComboByCategory(String category) {
        if (speciesModelCombo == null) return;
        I18nManager i18n = I18nManager.getInstance();
        String targetType = getCategoryType(category);

        String targetGenus = null;
        if (genusSelect != null && genusSelect.getValue() != null && !genusSelect.getValue().equals(i18n.get("nest.genus.all")) && !genusSelect.getValue().contains("All Genera")) {
            targetGenus = genusSelect.getValue().split(" ")[0].toLowerCase();
        }

        String curSel = speciesModelCombo.getValue();
        boolean wasUpdating = isUpdatingSpeciesCombo;
        isUpdatingSpeciesCombo = true;
        try {
            speciesModelCombo.getItems().clear();
            for (String pName : speciesPresetMgr.getPresetNames()) {
                DefaultSpecies sp = speciesPresetMgr.getPreset(pName);
                String icon = "🐜";
                String spType = sp != null && sp.getInsectType() != null ? sp.getInsectType().toUpperCase() : "ANT";
                if (spType.contains("BEE") || spType.contains("WASP")) icon = "🐝";
                else if (spType.contains("TERMITE")) icon = "🐜";

                boolean categoryMatch = (targetType == null || spType.contains(targetType) ||
                        (targetType.equals("BEE") && (spType.contains("BEE") || spType.contains("WASP"))));

                boolean genusMatch = (targetGenus == null ||
                        (sp != null && sp.getScientificName() != null && sp.getScientificName().toLowerCase().contains(targetGenus)) ||
                        pName.toLowerCase().contains(targetGenus));

                if (categoryMatch && genusMatch) {
                    speciesModelCombo.getItems().add(icon + " " + pName);
                }
            }
            FXCollections.sort(speciesModelCombo.getItems());
            speciesModelCombo.getItems().add(i18n.get("nest.species.custom_config"));
            if (curSel != null && speciesModelCombo.getItems().contains(curSel)) {
                speciesModelCombo.setValue(curSel);
            } else if (!speciesModelCombo.getItems().isEmpty()) {
                speciesModelCombo.getSelectionModel().selectFirst();
            }
        } finally {
            isUpdatingSpeciesCombo = wasUpdating;
        }
    }

    private void handleSpeciesModelSelected() {
        String sel = speciesModelCombo.getValue();
        if (sel == null || sel.equals(I18nManager.getInstance().get("nest.species.custom_config"))) return;
        String cleanName = sel.replaceAll("^[🐜🐝✨🛠️]\\s*", "");
        DefaultSpecies sp = speciesPresetMgr.getPreset(cleanName);
        if (sp != null && onSpeciesModelSelected != null) {
            onSpeciesModelSelected.accept(sp);
        }
    }

    private void onCategoryChanged() {
        String cat = categorySelect.getValue();
        if (cat == null) return;
        String s = cat.toUpperCase();
        if (s.contains("HONEY") || s.contains("ABEILLE") || s.contains("APIS")) {
            setArchSelectValue("WAX_COMB_HEXAGONAL");
            setMatSelectValue("BEESWAX");
        } else if (s.contains("BUMBLE") || s.contains("BOURDON") || s.contains("BOMBUS")) {
            setArchSelectValue("WAX_POTS_CLUSTER");
            setMatSelectValue("PROPOLIS");
        } else if (s.contains("WASP") || s.contains("GUÊPE") || s.contains("GUEPE") || s.contains("VESPIDAE")) {
            setArchSelectValue("PAPER_PEDUNCULATE");
            setMatSelectValue("WOOD_PULP_PAPER");
        } else if (s.contains("TERMITE") || s.contains("ISOPTERA")) {
            setArchSelectValue("CATHEDRAL_MOUND");
            setMatSelectValue("STERCORAL_CEMENT");
        } else {
            String curArchNorm = NestRenderer.normalizeArchKey(archSelect.getValue());
            if (!curArchNorm.equals("ARBOREAL_SILK_LEAF") && !curArchNorm.equals("SURFACE_MOUND")) {
                setArchSelectValue("BURROW_UNDERGROUND");
            }
            setMatSelectValue("EARTH");
        }
        if (onRegenAndRepaint != null) onRegenAndRepaint.run();
    }

    public void setCategorySelectValue(String val) {
        if (categorySelect == null || val == null) return;
        String s = val.toUpperCase();
        I18nManager i18n = I18nManager.getInstance();
        String target;
        if (s.contains("TERMITE") || s.contains("ISOPTERA")) target = i18n.get("nest.category.termites");
        else if (s.contains("BEE") || s.contains("ABEILLE") || s.contains("BOURDON") || s.contains("APIS") || s.contains("BOMBUS")) target = i18n.get("nest.category.bees");
        else if (s.contains("WASP") || s.contains("GUÊPE") || s.contains("GUEPE") || s.contains("VESPIDAE")) target = i18n.get("nest.category.wasps");
        else target = i18n.get("nest.category.ants");

        if (categorySelect.getItems().contains(target)) {
            categorySelect.setValue(target);
        } else {
            for (String item : categorySelect.getItems()) {
                if (item.equalsIgnoreCase(val)) {
                    categorySelect.setValue(item);
                    return;
                }
            }
        }
    }

    public void setArchSelectValue(String val) {
        if (archSelect == null || val == null) return;
        String normKey = NestRenderer.normalizeArchKey(val);
        I18nManager i18n = I18nManager.getInstance();
        String i18nKey = switch (normKey) {
            case "WAX_COMB_HEXAGONAL" -> "nest.arch.wax_comb";
            case "WAX_POTS_CLUSTER" -> "nest.arch.wax_pots";
            case "PAPER_PEDUNCULATE" -> "nest.arch.hanging_paper";
            case "CATHEDRAL_MOUND" -> "nest.arch.cathedral";
            case "SUBTERRANEAN_FUNGI_VAULT" -> "nest.arch.fungi_vault";
            case "CARTON_NEST" -> "nest.arch.arboreal_carton";
            case "BAMBOO_STEM_NEST" -> "nest.arch.bamboo";
            case "BIVOUAC_LIVING_NEST" -> "nest.arch.bivouac";
            case "ARBOREAL_SILK_LEAF" -> "nest.arch.arboreal_silk";
            case "SURFACE_MOUND" -> "nest.arch.surface_dome";
            case "HOLLOW_TRUNK_NEST" -> "nest.arch.hollow_trunk";
            case "WOODEN_BEEHIVE" -> "nest.arch.wooden_beehive";
            default -> "nest.arch.subterranean";
        };
        String localized = i18n.get(i18nKey);
        if (archSelect.getItems().contains(localized)) {
            archSelect.setValue(localized);
        } else {
            for (String item : archSelect.getItems()) {
                if (NestRenderer.normalizeArchKey(item).equals(normKey)) {
                    archSelect.setValue(item);
                    return;
                }
            }
        }
    }

    public void setMatSelectValue(String val) {
        if (matSelect == null || val == null) return;
        String normKey = NestRenderer.normalizeMatKey(val);
        I18nManager i18n = I18nManager.getInstance();
        String i18nKey = switch (normKey) {
            case "WOOD_PULP_PAPER" -> "nest.mat.paper";
            case "BEESWAX" -> "nest.mat.wax";
            case "STERCORAL_CEMENT" -> "nest.mat.cement";
            case "CARTON" -> "nest.mat.carton";
            case "SILK_WEAVE" -> "nest.mat.silk";
            case "PROPOLIS" -> "nest.mat.propolis";
            case "HOLLOW_WOOD" -> "nest.mat.wood";
            case "LIVING_INSECTS" -> "nest.mat.living";
            default -> "nest.mat.earth";
        };
        String localized = i18n.get(i18nKey);
        if (matSelect.getItems().contains(localized)) {
            matSelect.setValue(localized);
        } else {
            for (String item : matSelect.getItems()) {
                if (NestRenderer.normalizeMatKey(item).equals(normKey)) {
                    matSelect.setValue(item);
                    return;
                }
            }
        }
    }

    public static String getCategoryType(String category) {
        if (category == null) return "ANT";
        String s = category.toUpperCase();
        if (s.contains("BEE") || s.contains("ABEILLE") || s.contains("BOURDON")) return "BEE";
        if (s.contains("WASP") || s.contains("GUÊPE") || s.contains("GUEPE")) return "WASP";
        if (s.contains("TERMITE")) return "TERMITE";
        return "ANT";
    }

    public String getArchitecture() { return NestRenderer.normalizeArchKey(archSelect != null ? archSelect.getValue() : ""); }
    public String getMaterial() { return NestRenderer.normalizeMatKey(matSelect != null ? matSelect.getValue() : ""); }

    public ComboBox<String> getCategorySelect() { return categorySelect; }
    public ComboBox<String> getGenusSelect() { return genusSelect; }
    public ComboBox<String> getSpeciesModelCombo() { return speciesModelCombo; }
    public ComboBox<String> getArchSelect() { return archSelect; }
    public ComboBox<String> getMatSelect() { return matSelect; }
    public ComboBox<String> getNestStageCombo() { return nestStageCombo; }
    public Label getSpeciesStatusLabel() { return speciesStatusLabel; }

    public boolean isUpdatingSpeciesCombo() { return isUpdatingSpeciesCombo; }
    public void setUpdatingSpeciesCombo(boolean updating) { this.isUpdatingSpeciesCombo = updating; }
}
