/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;

import java.awt.Desktop;
import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive, searchable scientific glossary pane for SwarmForge Studio.
 * Organizes biological, biomechanical, geological and computational terminology into dedicated categories
 * with live filtering and links to authoritative scientific literature and encyclopedias.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GlossaryPaneView extends VBox {

    private final TextField searchField;
    private final TabPane categoryTabPane;

    public GlossaryPaneView() {
        super(10);
        setPadding(new Insets(0));

        I18nManager i18n = I18nManager.getInstance();

        VBox headerVBox = new VBox(6);
        headerVBox.setPadding(new Insets(8, 10, 5, 10));

        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("glossary.title"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        headerRow.getChildren().addAll(title, sp);
        headerVBox.getChildren().addAll(headerRow, new Separator());

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(10, 15, 15, 15));

        Label subtitle = new Label();
        subtitle.textProperty().bind(i18n.createStringBinding("glossary.subtitle"));
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-wrap-text: true;");

        this.searchField = new TextField();
        this.searchField.promptTextProperty().bind(i18n.createStringBinding("glossary.search_prompt"));
        this.searchField.setStyle("-fx-font-size: 13px;");

        this.categoryTabPane = new TabPane();
        this.categoryTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Section 1: Architectures de Nids
        VBox vNest = new VBox(10);
        vNest.setPadding(new Insets(15));
        addGlossaryRowKey(vNest, "glossary.entry.nest.wax_comb.title", "glossary.entry.nest.wax_comb.desc", "https://fr.wikipedia.org/wiki/Rayon_de_cire");
        addGlossaryRowKey(vNest, "glossary.entry.nest.wax_pots.title", "glossary.entry.nest.wax_pots.desc", "https://fr.wikipedia.org/wiki/Propolis");
        addGlossaryRowKey(vNest, "glossary.entry.nest.paper.title", "glossary.entry.nest.paper.desc", "https://fr.wikipedia.org/wiki/Gu%C3%AApe");
        addGlossaryRowKey(vNest, "glossary.entry.nest.cathedral.title", "glossary.entry.nest.cathedral.desc", "https://fr.wikipedia.org/wiki/Termiti%C3%A8re");
        addGlossaryRowKey(vNest, "glossary.entry.nest.silk.title", "glossary.entry.nest.silk.desc", "https://fr.wikipedia.org/wiki/Oecophylla");
        addGlossaryRowKey(vNest, "glossary.entry.nest.fungi.title", "glossary.entry.nest.fungi.desc", "https://fr.wikipedia.org/wiki/Atta_(genre)");
        addGlossaryRowKey(vNest, "glossary.entry.nest.carton.title", "glossary.entry.nest.carton.desc", "https://fr.wikipedia.org/wiki/Crematogaster");
        addGlossaryRowKey(vNest, "glossary.entry.nest.bivouac.title", "glossary.entry.nest.bivouac.desc", "https://fr.wikipedia.org/wiki/Fourmi_l%C3%A9gionnaire");
        addGlossaryRowKey(vNest, "glossary.entry.nest.mound.title", "glossary.entry.nest.mound.desc", "https://fr.wikipedia.org/wiki/Formica_rufa");
        addGlossaryRowKey(vNest, "glossary.entry.nest.wood.title", "glossary.entry.nest.wood.desc", "https://fr.wikipedia.org/wiki/Camponotus");
        addGlossaryRowKey(vNest, "glossary.nest.bamboo.title", "glossary.nest.bamboo.desc");
        addGlossaryRowKey(vNest, "glossary.nest.subterranean.title", "glossary.nest.subterranean.desc");
        addGlossaryRowKey(vNest, "glossary.nest.subterranean_lime.title", "glossary.nest.subterranean_lime.desc");
        addGlossaryRowKey(vNest, "glossary.nest.arboreal_carton.title", "glossary.nest.arboreal_carton.desc");
        addGlossaryRowKey(vNest, "glossary.nest.materials.title", "glossary.nest.materials.desc");
        addGlossaryRowKey(vNest, "glossary.nest.stages.title", "glossary.nest.stages.desc");

        // Section 2: Structure Sociale & Reines
        VBox vSocial = new VBox(10);
        vSocial.setPadding(new Insets(15));
        addGlossaryRowKey(vSocial, "glossary.entry.social.gynic.title", "glossary.entry.social.gynic.desc", "https://fr.wikipedia.org/wiki/Eusocialit%C3%A9");
        addGlossaryRowKey(vSocial, "glossary.entry.social.king.title", "glossary.entry.social.king.desc", "https://fr.wikipedia.org/wiki/Termite");
        addGlossaryRowKey(vSocial, "glossary.entry.social.flight.title", "glossary.entry.social.flight.desc", "https://fr.wikipedia.org/wiki/Vol_nuptial");
        addGlossaryRowKey(vSocial, "glossary.entry.social.inhibition.title", "glossary.entry.social.inhibition.desc", "https://fr.wikipedia.org/wiki/Ph%C3%A9romone");
        addGlossaryRowKey(vSocial, "glossary.entry.social.polycalism.title", "glossary.entry.social.polycalism.desc", "https://fr.wikipedia.org/wiki/Supercolonie");
        addGlossaryRowKey(vSocial, "glossary.social.queen_mode.title", "glossary.social.queen_mode.desc");
        addGlossaryRowKey(vSocial, "glossary.social.trophallaxis.title", "glossary.social.trophallaxis.desc");
        addGlossaryRowKey(vSocial, "glossary.social.polyethism.title", "glossary.social.polyethism.desc");
        addGlossaryRowKey(vSocial, "glossary.social.stigmergy.title", "glossary.social.stigmergy.desc");

        // Section 3: Sol & Géologie SIG
        VBox vEnv = new VBox(10);
        vEnv.setPadding(new Insets(15));
        addGlossaryRowKey(vEnv, "glossary.entry.env.dim.title", "glossary.entry.env.dim.desc", "https://fr.wikipedia.org/wiki/Mod%C3%A8le_num%C3%A9rique_de_terrain");
        addGlossaryRowKey(vEnv, "glossary.entry.env.depth.title", "glossary.entry.env.depth.desc", "https://fr.wikipedia.org/wiki/Stratigraphie");
        addGlossaryRowKey(vEnv, "glossary.entry.env.res.title", "glossary.entry.env.res.desc", "https://fr.wikipedia.org/wiki/Voxel");
        addGlossaryRowKey(vEnv, "glossary.entry.env.sig.title", "glossary.entry.env.sig.desc", "https://fr.wikipedia.org/wiki/Shuttle_Radar_Topography_Mission");
        addGlossaryRowKey(vEnv, "glossary.entry.env.water.title", "glossary.entry.env.water.desc", "https://fr.wikipedia.org/wiki/Nappe_phr%C3%A9atique");
        addGlossaryRowKey(vEnv, "glossary.entry.env.microclimate.title", "glossary.entry.env.microclimate.desc", "https://fr.wikipedia.org/wiki/Microclimat");
        addGlossaryRowKey(vEnv, "glossary.env.moisture.title", "glossary.env.moisture.desc");
        addGlossaryRowKey(vEnv, "glossary.env.temperature.title", "glossary.env.temperature.desc");
        addGlossaryRowKey(vEnv, "glossary.env.co2.title", "glossary.env.co2.desc");
        addGlossaryRowKey(vEnv, "glossary.env.solar.title", "glossary.env.solar.desc");
        addGlossaryRowKey(vEnv, "glossary.env.magnetic.title", "glossary.env.magnetic.desc");
        addGlossaryRowKey(vEnv, "glossary.env.soil_layers.title", "glossary.env.soil_layers.desc");
        addGlossaryRowKey(vEnv, "glossary.env.pressure.title", "glossary.env.pressure.desc");
        addGlossaryRowKey(vEnv, "glossary.env.trail_pheromones.title", "glossary.env.trail_pheromones.desc");

        // Section 4: Moteurs de Raisonnement & Cognition
        VBox vReasoning = new VBox(10);
        vReasoning.setPadding(new Insets(15));
        addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.bt.title", "glossary.entry.reasoning.bt.desc", "https://fr.wikipedia.org/wiki/Arbre_de_comportement");
        addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.blackboard.title", "glossary.entry.reasoning.blackboard.desc", "https://en.wikipedia.org/wiki/Blackboard_system");
        addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.fsm.title", "glossary.entry.reasoning.fsm.desc", "https://fr.wikipedia.org/wiki/Automate_%C3%A0_%C3%A9tats_finis");
        addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.bdi.title", "glossary.entry.reasoning.bdi.desc", "https://fr.wikipedia.org/wiki/Mod%C3%A8le_BDI");
        addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.fuzzy.title", "glossary.entry.reasoning.fuzzy.desc", "https://fr.wikipedia.org/wiki/Logique_floue");
        addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.hybrid.title", "glossary.entry.reasoning.hybrid.desc", "https://fr.wikipedia.org/wiki/Syst%C3%A8me_hybride");
        addGlossaryRowKey(vReasoning, "glossary.entry.reasoning.snn.title", "glossary.entry.reasoning.snn.desc", "https://fr.wikipedia.org/wiki/R%C3%A9seau_de_neurones_artificiels");
        addGlossaryRowKey(vReasoning, "glossary.reasoning.nn.title", "glossary.reasoning.nn.desc");
        addGlossaryRowKey(vReasoning, "glossary.reasoning.bulk.title", "glossary.reasoning.bulk.desc");
        addGlossaryRowKey(vReasoning, "glossary.reasoning.quorum.title", "glossary.reasoning.quorum.desc");

        // Section 5: Capteurs & Biomécanique
        VBox vSensors = new VBox(10);
        vSensors.setPadding(new Insets(15));
        addGlossaryRowKey(vSensors, "glossary.entry.sensors.subgenual.title", "glossary.entry.sensors.subgenual.desc", "https://fr.wikipedia.org/wiki/Organe_subg%C3%A9nual");
        addGlossaryRowKey(vSensors, "glossary.entry.sensors.uv.title", "glossary.entry.sensors.uv.desc", "https://fr.wikipedia.org/wiki/Cataglyphis");
        addGlossaryRowKey(vSensors, "glossary.entry.sensors.mandible.title", "glossary.entry.sensors.mandible.desc", "https://fr.wikipedia.org/wiki/Mandibule_(arthropode)");
        addGlossaryRowKey(vSensors, "glossary.entry.sensors.autothysis.title", "glossary.entry.sensors.autothysis.desc", "https://fr.wikipedia.org/wiki/Colobopsis_explodens");
        addGlossaryRowKey(vSensors, "glossary.entry.sensors.arolia.title", "glossary.entry.sensors.arolia.desc", "https://fr.wikipedia.org/wiki/Tarse_(anatomie)");
        addGlossaryRowKey(vSensors, "glossary.biomech.antennal_olfaction.title", "glossary.biomech.antennal_olfaction.desc");
        addGlossaryRowKey(vSensors, "glossary.species.hydrocarbons.title", "glossary.species.hydrocarbons.desc");
        addGlossaryRowKey(vSensors, "glossary.species.formic_acid.title", "glossary.species.formic_acid.desc");
        addGlossaryRowKey(vSensors, "glossary.species.metabolism.title", "glossary.species.metabolism.desc");
        addGlossaryRowKey(vSensors, "glossary.species.polymorphism.title", "glossary.species.polymorphism.desc");
        addGlossaryRowKey(vSensors, "glossary.species.desiccation.title", "glossary.species.desiccation.desc");

        // Section 6: Espèces Associées, Proies & Pathogènes
        VBox vAccessory = new VBox(10);
        vAccessory.setPadding(new Insets(15));
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.flora.title", "glossary.entry.accessory.flora.desc", "https://fr.wikipedia.org/wiki/Nectaire");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.aphids.title", "glossary.entry.accessory.aphids.desc", "https://fr.wikipedia.org/wiki/Miellat");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.prey.title", "glossary.entry.accessory.prey.desc", "https://fr.wikipedia.org/wiki/Insecte");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.predator.title", "glossary.entry.accessory.predator.desc", "https://fr.wikipedia.org/wiki/Fourmilion");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.pathogen.title", "glossary.entry.accessory.pathogen.desc", "https://fr.wikipedia.org/wiki/Ophiocordyceps_unilateralis");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.trophic_web.title", "glossary.entry.accessory.trophic_web.desc", "https://fr.wikipedia.org/wiki/R%C3%A9seau_trophique");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.commensalism.title", "glossary.entry.accessory.commensalism.desc", "https://fr.wikipedia.org/wiki/Myrm%C3%A9cophilie");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.myrmecochory.title", "glossary.entry.accessory.myrmecochory.desc", "https://fr.wikipedia.org/wiki/Myrm%C3%A9cochorie");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.parasitoidism.title", "glossary.entry.accessory.parasitoidism.desc", "https://fr.wikipedia.org/wiki/Parasito%C3%AFde");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.diapause.title", "glossary.entry.accessory.diapause.desc", "https://fr.wikipedia.org/wiki/Diapause");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.r0.title", "glossary.entry.accessory.r0.desc", "https://fr.wikipedia.org/wiki/Nombre_reproductif_de_base");
        addGlossaryRowKey(vAccessory, "glossary.entry.accessory.lat.title", "glossary.entry.accessory.lat.desc", "https://fr.wikipedia.org/wiki/Dur%C3%A9e_du_jour");

        // Section 7: Moteur & Contrôles Temps Réel
        VBox vEngine = new VBox(10);
        vEngine.setPadding(new Insets(15));
        addGlossaryRowKey(vEngine, "glossary.engine.god_mode.title", "glossary.engine.god_mode.desc");
        addGlossaryRowKey(vEngine, "glossary.engine.dt.title", "glossary.engine.dt.desc");
        addGlossaryRowKey(vEngine, "glossary.engine.multi_colony.title", "glossary.engine.multi_colony.desc");
        addGlossaryRowKey(vEngine, "glossary.engine.seed.title", "glossary.engine.seed.desc");
        addGlossaryRowKey(vEngine, "glossary.engine.audio_synth.title", "glossary.engine.audio_synth.desc");
        addGlossaryRowKey(vEngine, "glossary.engine.time_scale.title", "glossary.engine.time_scale.desc");
        addGlossaryRowKey(vEngine, "glossary.engine.view_focus.title", "glossary.engine.view_focus.desc");
        addGlossaryRowKey(vEngine, "glossary.engine.checkpoint.title", "glossary.engine.checkpoint.desc");

        // Sort each section alphabetically by localized entry title
        sortGlossaryVBox(vNest);
        sortGlossaryVBox(vSocial);
        sortGlossaryVBox(vEnv);
        sortGlossaryVBox(vReasoning);
        sortGlossaryVBox(vSensors);
        sortGlossaryVBox(vAccessory);
        sortGlossaryVBox(vEngine);

        Tab tNest = new Tab();
        tNest.textProperty().bind(i18n.createStringBinding("glossary.cat.nest"));
        tNest.setGraphic(new FontIcon(Feather.HOME));
        tNest.setContent(new ScrollPane(vNest));

        Tab tSocial = new Tab();
        tSocial.textProperty().bind(i18n.createStringBinding("glossary.cat.social"));
        tSocial.setGraphic(new FontIcon(Feather.USERS));
        tSocial.setContent(new ScrollPane(vSocial));

        Tab tEnv = new Tab();
        tEnv.textProperty().bind(i18n.createStringBinding("glossary.cat.env"));
        tEnv.setGraphic(new FontIcon(Feather.GLOBE));
        tEnv.setContent(new ScrollPane(vEnv));

        Tab tReasoning = new Tab();
        tReasoning.textProperty().bind(i18n.createStringBinding("glossary.cat.reasoning"));
        tReasoning.setGraphic(new FontIcon(Feather.CPU));
        tReasoning.setContent(new ScrollPane(vReasoning));

        Tab tSensors = new Tab();
        tSensors.textProperty().bind(i18n.createStringBinding("glossary.cat.sensors"));
        tSensors.setGraphic(new FontIcon(Feather.EYE));
        tSensors.setContent(new ScrollPane(vSensors));

        Tab tAccessory = new Tab();
        tAccessory.textProperty().bind(i18n.createStringBinding("glossary.cat.accessory"));
        tAccessory.setGraphic(new FontIcon(Feather.FEATHER));
        tAccessory.setContent(new ScrollPane(vAccessory));

        Tab tEngine = new Tab();
        tEngine.textProperty().bind(i18n.createStringBinding("glossary.tab.engine", "Engine & Controls"));
        tEngine.setGraphic(new FontIcon(Feather.ACTIVITY));
        tEngine.setContent(new ScrollPane(vEngine));

        this.categoryTabPane.getTabs().addAll(tNest, tSocial, tEnv, tReasoning, tSensors, tAccessory, tEngine);
        VBox.setVgrow(this.categoryTabPane, Priority.ALWAYS);

        // Add live search filter listener
        this.searchField.textProperty().addListener((obs, oldV, newV) -> {
            String query = newV == null ? "" : newV.toLowerCase().trim();
            filterVBoxRows(vNest, query);
            filterVBoxRows(vSocial, query);
            filterVBoxRows(vEnv, query);
            filterVBoxRows(vReasoning, query);
            filterVBoxRows(vSensors, query);
            filterVBoxRows(vAccessory, query);
            filterVBoxRows(vEngine, query);
        });

        contentBox.getChildren().addAll(subtitle, this.searchField, this.categoryTabPane);
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        getChildren().addAll(headerVBox, contentBox);
    }

    /**
     * Programmatically filter the glossary and switch to the category tab matching the search term.
     *
     * @param searchTerm The search keyword or phrase
     */
    public void searchAndSelect(String searchTerm) {
        if (searchTerm != null && !searchTerm.isBlank()) {
            if (this.searchField != null) {
                this.searchField.setText(searchTerm);
            }
            if (this.categoryTabPane != null) {
                String lowerTerm = searchTerm.toLowerCase();
                if (lowerTerm.contains("nest") || lowerTerm.contains("nid") || lowerTerm.contains("wax") || lowerTerm.contains("cire") || lowerTerm.contains("mound") || lowerTerm.contains("arch")) {
                    this.categoryTabPane.getSelectionModel().select(0);
                } else if (lowerTerm.contains("queen") || lowerTerm.contains("reine") || lowerTerm.contains("social") || lowerTerm.contains("king") || lowerTerm.contains("roi") || lowerTerm.contains("nuptial") || lowerTerm.contains("troph") || lowerTerm.contains("stig")) {
                    this.categoryTabPane.getSelectionModel().select(1);
                } else if (lowerTerm.contains("env") || lowerTerm.contains("temp") || lowerTerm.contains("press") || lowerTerm.contains("sol") || lowerTerm.contains("micro") || lowerTerm.contains("phero") || lowerTerm.contains("moist") || lowerTerm.contains("humid")) {
                    this.categoryTabPane.getSelectionModel().select(2);
                } else if (lowerTerm.contains("fsm") || lowerTerm.contains("bdi") || lowerTerm.contains("décision") || lowerTerm.contains("reason") || lowerTerm.contains("quorum") || lowerTerm.contains("bt") || lowerTerm.contains("fuzzy")) {
                    this.categoryTabPane.getSelectionModel().select(3);
                } else if (lowerTerm.contains("subgenual") || lowerTerm.contains("vibration") || lowerTerm.contains("uv") || lowerTerm.contains("autothys") || lowerTerm.contains("arolia") || lowerTerm.contains("mandib") || lowerTerm.contains("olfac") || lowerTerm.contains("hydrocarbon") || lowerTerm.contains("formic") || lowerTerm.contains("metabol") || lowerTerm.contains("polymorph") || lowerTerm.contains("desiccat")) {
                    this.categoryTabPane.getSelectionModel().select(4);
                } else if (lowerTerm.contains("flora") || lowerTerm.contains("aphid") || lowerTerm.contains("prey") || lowerTerm.contains("predat") || lowerTerm.contains("pathogen") || lowerTerm.contains("diapause")) {
                    this.categoryTabPane.getSelectionModel().select(5);
                } else if (lowerTerm.contains("seed") || lowerTerm.contains("moteur") || lowerTerm.contains("divin") || lowerTerm.contains("audio") || lowerTerm.contains("coloni") || lowerTerm.contains("god") || lowerTerm.contains("dt")) {
                    this.categoryTabPane.getSelectionModel().select(6);
                }
            }
        }
    }

    private void filterVBoxRows(VBox box, String query) {
        for (Node child : box.getChildren()) {
            if (child instanceof HBox row) {
                boolean visible = query.isEmpty();
                if (!visible) {
                    for (Node n : row.getChildren()) {
                        if (n instanceof Label l && l.getText().toLowerCase().contains(query)) {
                            visible = true;
                            break;
                        }
                    }
                }
                row.setVisible(visible);
                row.setManaged(visible);
            }
        }
    }

    private void sortGlossaryVBox(VBox box) {
        I18nManager i18n = I18nManager.getInstance();
        Collator collator = Collator.getInstance(i18n.getLocale());
        collator.setStrength(Collator.PRIMARY);
        List<Node> list = new ArrayList<>(box.getChildren());
        list.sort((n1, n2) -> {
            if (n1 instanceof HBox h1 && n2 instanceof HBox h2) {
                String t1 = extractRowTitle(h1);
                String t2 = extractRowTitle(h2);
                return collator.compare(t1, t2);
            }
            return 0;
        });
        box.getChildren().setAll(list);
    }

    private String extractRowTitle(HBox row) {
        for (Node n : row.getChildren()) {
            if (n instanceof Label l && l.getStyleClass().contains("help-entry-title")) {
                return l.getText().replaceAll("^•\\s*", "").replaceAll("\\s*:\\s*$", "").trim();
            }
        }
        return "";
    }

    private void addGlossaryRowKey(VBox box, String titleKey, String descKey) {
        addGlossaryRowKey(box, titleKey, descKey, null);
    }

    private void addGlossaryRowKey(VBox box, String titleKey, String descKey, String wikiUrl) {
        I18nManager i18n = I18nManager.getInstance();
        Label t = new Label();
        t.textProperty().bind(javafx.beans.binding.Bindings.concat("• ", i18n.createStringBinding(titleKey), " : "));
        t.getStyleClass().add("help-entry-title");
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7; -fx-min-width: 240px;");

        Label d = new Label();
        d.textProperty().bind(i18n.createStringBinding(descKey));
        d.getStyleClass().add("help-entry-desc");
        d.setWrapText(true);

        HBox row = new HBox(8, t, d);
        if (wikiUrl != null && !wikiUrl.isBlank()) {
            Hyperlink wikiLink = new Hyperlink();
            wikiLink.textProperty().bind(i18n.createStringBinding("glossary.link.wikipedia"));
            wikiLink.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");
            wikiLink.setOnAction(e -> {
                try {
                    Desktop.getDesktop().browse(new URI(wikiUrl));
                } catch (Exception ex) {
                    ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Wikipédia : " + wikiUrl).show();
                }
            });
            row.getChildren().add(wikiLink);
        }
        row.getStyleClass().add("card-pane");
        row.setPadding(new Insets(8));
        HBox.setHgrow(d, Priority.ALWAYS);
        box.getChildren().add(row);
    }
}
