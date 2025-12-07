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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * JComboBox Demo
 *
 * @author Jeff Dinkins
 */
public class ComboBoxModule extends JAntsModule implements ActionListener {


    JComboBox<?> presetCB;

    Hashtable<String, Object> parts = new Hashtable<>();

    public ComboBoxModule(JAnts jAntsApplication) {
        // Set the title for this demo, and an icon used to represent this
        // demo inside the jAntsApplication app.
        super(jAntsApplication, "ComboBoxDemo", "toolbar/JComboBox.gif");

        createComboBoxDemo();
    }

    public void createComboBoxDemo() {
        JPanel module = getModulePanel();

        JPanel demoPanel = getModulePanel();
        demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));

        demoPanel.add(Box.createRigidArea(VGAP20));
        demoPanel.add(innerPanel);
        demoPanel.add(Box.createRigidArea(VGAP20));

        innerPanel.add(Box.createRigidArea(HGAP20));

        // Create a panel to hold buttons
        JPanel comboBoxPanel = new JPanel() {
                public Dimension getMaximumSize() {
                    return new Dimension(getPreferredSize().width, super.getMaximumSize().height);
                }
        };
        comboBoxPanel.setLayout(new BoxLayout(comboBoxPanel, BoxLayout.Y_AXIS));

        comboBoxPanel.add(Box.createRigidArea(VGAP15));

        JLabel l = (JLabel) comboBoxPanel.add(new JLabel(getString("ComboBoxDemo.presets")));
        l.setAlignmentX(LEFT_ALIGNMENT);
        presetCB = createPresetComboBox();
        presetCB.setAlignmentX(LEFT_ALIGNMENT);
        l.setLabelFor(presetCB);
        comboBoxPanel.add(presetCB);
        comboBoxPanel.add(Box.createRigidArea(VGAP30));

        // Fill up the remaining space
        comboBoxPanel.add(new JPanel(new BorderLayout()));

        // set the default face
        presetCB.setSelectedIndex(0);
    }

    JComboBox<String> createPresetComboBox() {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem(getString("ComboBoxDemo.preset1"));
        cb.addItem(getString("ComboBoxDemo.preset2"));
        cb.addItem(getString("ComboBoxDemo.preset3"));
        cb.addItem(getString("ComboBoxDemo.preset4"));
        cb.addItem(getString("ComboBoxDemo.preset5"));
        cb.addItem(getString("ComboBoxDemo.preset6"));
        cb.addItem(getString("ComboBoxDemo.preset7"));
        cb.addItem(getString("ComboBoxDemo.preset8"));
        cb.addItem(getString("ComboBoxDemo.preset9"));
        cb.addItem(getString("ComboBoxDemo.preset10"));
        cb.addActionListener(this);
        return cb;
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == presetCB) {
            String hair = null;
            String eyes = null;
            String mouth = null;
            switch(presetCB.getSelectedIndex()) {
               case 0:
                   hair = (String) parts.get("philip");
                   eyes = (String) parts.get("howard");
                   mouth = (String) parts.get("jeff");
                   break;
               case 1:
                   hair = (String) parts.get("jeff");
                   eyes = (String) parts.get("larry");
                   mouth = (String) parts.get("philip");
                   break;
               case 2:
                   hair = (String) parts.get("howard");
                   eyes = (String) parts.get("scott");
                   mouth = (String) parts.get("hans");
                   break;
               case 3:
                   hair = (String) parts.get("philip");
                   eyes = (String) parts.get("jeff");
                   mouth = (String) parts.get("hans");
                   break;
               case 4:
                   hair = (String) parts.get("brent");
                   eyes = (String) parts.get("jon");
                   mouth = (String) parts.get("scott");
                   break;
               case 5:
                   hair = (String) parts.get("lara");
                   eyes = (String) parts.get("larry");
                   mouth = (String) parts.get("lisa");
                   break;
               case 6:
                   hair = (String) parts.get("james");
                   eyes = (String) parts.get("philip");
                   mouth = (String) parts.get("michael");
                   break;
               case 7:
                   hair = (String) parts.get("philip");
                   eyes = (String) parts.get("lisa");
                   mouth = (String) parts.get("brent");
                   break;
               case 8:
                   hair = (String) parts.get("james");
                   eyes = (String) parts.get("philip");
                   mouth = (String) parts.get("jon");
                   break;
               case 9:
                   hair = (String) parts.get("lara");
                   eyes = (String) parts.get("jon");
                   mouth = (String) parts.get("scott");
                   break;
            }
        }
    }

}
