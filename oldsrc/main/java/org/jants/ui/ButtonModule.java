/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants.ui;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SingleSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

/*
 * Slightly adapted after SwingSet2 demo by Sun Inc.
 *
 * JButton, JRadioButton, JToggleButton, JCheckBox Demos
 *
 * @author Jeff Dinkins
 */
public class ButtonModule extends JAntsModule implements ChangeListener {

    JTabbedPane tab;

    JPanel buttonPanel = new JPanel();
    JPanel checkboxPanel = new JPanel();
    JPanel radioButtonPanel = new JPanel();
    JPanel toggleButtonPanel = new JPanel();

    Vector<Component> buttons = new Vector<>();
    Vector<Component> checkboxes = new Vector<>();
    Vector<Component> radioButtons = new Vector<>();
    Vector<Component> toggleButtons = new Vector<>();

    Vector<Component> currentControls = buttons;

    JButton button;
    JCheckBox check;
    JRadioButton radio;

    EmptyBorder border5 = new EmptyBorder(5,5,5,5);
    EmptyBorder border10 = new EmptyBorder(10,10,10,10);

    ItemListener buttonDisplayListener = null;
    ItemListener buttonPadListener = null;

    Insets insets0 = new Insets(0,0,0,0);
    Insets insets10 = new Insets(10,10,10,10);

    public ButtonModule(JAnts jAntsApplication) {
        // Set the title for this demo, and an icon used to represent this
        // demo inside the jAntsApplication app.
        super(jAntsApplication, "ButtonDemo", "toolbar/JButton.gif");

        tab = new JTabbedPane();
        tab.getModel().addChangeListener(this);

        JPanel demo = getModulePanel();
        demo.setLayout(new BoxLayout(demo, BoxLayout.Y_AXIS));
        demo.add(tab);

        addButtons();
        addRadioButtons();
        addCheckBoxes();
        currentControls = buttons;
    }

    public void addButtons() {
        tab.addTab(getString("ButtonDemo.buttons"), buttonPanel);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(border5);

        JPanel p1 = createVerticalPanel(true);
        p1.setAlignmentY(TOP_ALIGNMENT);
        buttonPanel.add(p1);

        // Text Buttons
        JPanel p2 = createHorizontalPanel(false);
        p1.add(p2);
        p2.setBorder(new CompoundBorder(new TitledBorder(null, getString("ButtonDemo.textbuttons"),
                                                          TitledBorder.LEFT, TitledBorder.TOP), border5));

        buttons.add(p2.add(new JButton(getString("ButtonDemo.button1"))));
        p2.add(Box.createRigidArea(HGAP10));

        buttons.add(p2.add(new JButton(getString("ButtonDemo.button2"))));
        p2.add(Box.createRigidArea(HGAP10));

        buttons.add(p2.add(new JButton(getString("ButtonDemo.button3"))));

        p1.add(Box.createVerticalGlue());

        buttonPanel.add(Box.createHorizontalGlue());
        currentControls = buttons;
    }

    public void addRadioButtons() {
        ButtonGroup group = new ButtonGroup();

        tab.addTab(getString("ButtonDemo.radiobuttons"), radioButtonPanel);
        radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.X_AXIS));
        radioButtonPanel.setBorder(border5);

        JPanel p1 = createVerticalPanel(true);
        p1.setAlignmentY(TOP_ALIGNMENT);
        radioButtonPanel.add(p1);

        // Text Radio Buttons
        JPanel p2 = createHorizontalPanel(false);
        p1.add(p2);
        p2.setBorder(new CompoundBorder(
                      new TitledBorder(
                        null, getString("ButtonDemo.textradiobuttons"),
                        TitledBorder.LEFT, TitledBorder.TOP), border5)
        );

        radio = (JRadioButton)p2.add(
                new JRadioButton(getString("ButtonDemo.radio1")));
        group.add(radio);
        radioButtons.add(radio);
        p2.add(Box.createRigidArea(HGAP10));

        radio = (JRadioButton)p2.add(
                new JRadioButton(getString("ButtonDemo.radio2")));
        group.add(radio);
        radioButtons.add(radio);
        p2.add(Box.createRigidArea(HGAP10));

        radio = (JRadioButton)p2.add(
                new JRadioButton(getString("ButtonDemo.radio3")));
        group.add(radio);
        radioButtons.add(radio);

        // verticaly glue fills out the rest of the box
        p1.add(Box.createVerticalGlue());

        radioButtonPanel.add(Box.createHorizontalGlue());
        currentControls = radioButtons;
    }


    public void addCheckBoxes() {
        tab.addTab(getString("ButtonDemo.checkboxes"), checkboxPanel);
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.X_AXIS));
        checkboxPanel.setBorder(border5);

        JPanel p1 = createVerticalPanel(true);
        p1.setAlignmentY(TOP_ALIGNMENT);
        checkboxPanel.add(p1);

        // Text Radio Buttons
        JPanel p2 = createHorizontalPanel(false);
        p1.add(p2);
        p2.setBorder(new CompoundBorder(
                      new TitledBorder(
                        null, getString("ButtonDemo.textcheckboxes"),
                        TitledBorder.LEFT, TitledBorder.TOP), border5)
        );

        checkboxes.add(p2.add(new JCheckBox(getString("ButtonDemo.check1"))));
        p2.add(Box.createRigidArea(HGAP10));

        checkboxes.add(p2.add(new JCheckBox(getString("ButtonDemo.check2"))));
        p2.add(Box.createRigidArea(HGAP10));

        checkboxes.add(p2.add(new JCheckBox(getString("ButtonDemo.check3"))));

        // verticaly glue fills out the rest of the box
        p1.add(Box.createVerticalGlue());

        checkboxPanel.add(Box.createHorizontalGlue());
        currentControls = checkboxes;
    }

    public void createListeners() {
        buttonDisplayListener = new ItemListener() {
                Component c;
                AbstractButton b;

                public void itemStateChanged(ItemEvent e) {
                    JCheckBox cb = (JCheckBox) e.getSource();
                    String command = cb.getActionCommand();
                    if(command.equals("Enabled")) {
                        for(int i = 0; i < currentControls.size(); i++) {
                            c = currentControls.elementAt(i);
                            c.setEnabled(cb.isSelected());
                            c.invalidate();
                        }
                    }
                    invalidate();
                    validate();
                    repaint();
                }
        };

        buttonPadListener = new ItemListener() {
                AbstractButton b;

                public void itemStateChanged(ItemEvent e) {
                    // *** pad = 0
                    int pad = -1;
                    JRadioButton rb = (JRadioButton) e.getSource();
                    String command = rb.getActionCommand();
                    if(command.equals("ZeroPad") && rb.isSelected()) {
                        pad = 0;
                    } else if(command.equals("TenPad") && rb.isSelected()) {
                        pad = 10;
                    }

                    for(int i = 0; i < currentControls.size(); i++) {
                        b = (AbstractButton) currentControls.elementAt(i);
                        if(pad == -1) {
                            b.setMargin(null);
                        } else if(pad == 0) {
                            b.setMargin(insets0);
                        } else {
                            b.setMargin(insets10);
                        }
                    }
                    invalidate();
                    validate();
                    repaint();
                }
        };
    }

    public void stateChanged(ChangeEvent e) {
        SingleSelectionModel model = (SingleSelectionModel) e.getSource();
        if(model.getSelectedIndex() == 0) {
            currentControls = buttons;
        } else if(model.getSelectedIndex() == 1) {
            currentControls = radioButtons;
        } else if(model.getSelectedIndex() == 2) {
            currentControls = checkboxes;
        } else {
            currentControls = toggleButtons;
        }
    }

    public Vector<Component> getCurrentControls() {
        return currentControls;
    }
}
