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

package org.jants.ui;import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.URL;

/*
 * Slightly adapted after SwingSet2 demo by Sun Inc.
 *
 *
 * @author Jeff Dinkins
 */
public class OptionPaneModule extends JAntsModule {

    /**
     * OptionPaneDemo Constructor
     */
    public OptionPaneModule(JAnts jAntsApplication) {
        // Set the title for this demo, and an icon used to represent this
        // demo inside the jAntsApplication app.
        super(jAntsApplication, "OptionPaneDemo", "toolbar/JOptionPane.gif");

        JPanel demo = getModulePanel();

        demo.setLayout(new BoxLayout(demo, BoxLayout.X_AXIS));

        JPanel bp = new JPanel() {
            public Dimension getMaximumSize() {
                return new Dimension(getPreferredSize().width, super.getMaximumSize().height);
            }
        };
        bp.setLayout(new BoxLayout(bp, BoxLayout.Y_AXIS));

        bp.add(Box.createRigidArea(VGAP30));
        bp.add(Box.createRigidArea(VGAP30));

        bp.add(createInputDialogButton());      bp.add(Box.createRigidArea(VGAP15));
        bp.add(createWarningDialogButton());    bp.add(Box.createRigidArea(VGAP15));
        bp.add(createMessageDialogButton());    bp.add(Box.createRigidArea(VGAP15));
        bp.add(createComponentDialogButton());  bp.add(Box.createRigidArea(VGAP15));
        bp.add(createConfirmDialogButton());    bp.add(Box.createVerticalGlue());

        demo.add(Box.createHorizontalGlue());
        demo.add(bp);
        demo.add(Box.createHorizontalGlue());
    }

    public JButton createWarningDialogButton() {
        Action a = new AbstractAction(getString("OptionPaneDemo.warningbutton")) {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                    getModulePanel(),
                    getString("OptionPaneDemo.warningtext"),
                    getString("OptionPaneDemo.warningtitle"),
                    JOptionPane.WARNING_MESSAGE
                );
            }
        };
        return createButton(a);
    }

    public JButton createMessageDialogButton() {
        Action a = new AbstractAction(getString("OptionPaneDemo.messagebutton")) {
            URL img = getClass().getResource("/resources/images/optionpane/bottle.gif");
            String imagesrc = "<img src=\"" + img + "\" width=\"284\" height=\"100\">";
            String message = getString("OptionPaneDemo.messagetext");
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                    getModulePanel(),
                    "<html>" + imagesrc + "<br><center>" + message + "</center><br></html>"
                );
            }
        };
        return createButton(a);
    }

    public JButton createConfirmDialogButton() {
        Action a = new AbstractAction(getString("OptionPaneDemo.confirmbutton")) {
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(getModulePanel(), getString("OptionPaneDemo.confirmquestion"));
                if(result == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(getModulePanel(), getString("OptionPaneDemo.confirmyes"));
                } else if(result == JOptionPane.NO_OPTION) {
                    JOptionPane.showMessageDialog(getModulePanel(), getString("OptionPaneDemo.confirmno"));
                }
            }
        };
        return createButton(a);
    }

    public JButton createInputDialogButton() {
        Action a = new AbstractAction(getString("OptionPaneDemo.inputbutton")) {
            public void actionPerformed(ActionEvent e) {
                String result = JOptionPane.showInputDialog(getModulePanel(), getString("OptionPaneDemo.inputquestion"));
                if ((result != null) && (result.length() > 0)) {
                    JOptionPane.showMessageDialog(getModulePanel(),
                                    result + ": " +
                                    getString("OptionPaneDemo.inputresponse"));
                }
            }
        };
        return createButton(a);
    }

    public JButton createComponentDialogButton() {
        Action a = new AbstractAction(getString("OptionPaneDemo.componentbutton")) {
            public void actionPerformed(ActionEvent e) {
                // In a ComponentDialog, you can show as many message components and
                // as many options as you want:

                // Messages
                Object[]      message = new Object[4];
                message[0] = getString("OptionPaneDemo.componentmessage");
                message[1] = new JTextField(getString("OptionPaneDemo.componenttextfield"));

                JComboBox<String> cb = new JComboBox<>();
                cb.addItem(getString("OptionPaneDemo.component_cb1"));
                cb.addItem(getString("OptionPaneDemo.component_cb2"));
                cb.addItem(getString("OptionPaneDemo.component_cb3"));
                message[2] = cb;

                message[3] = getString("OptionPaneDemo.componentmessage2");

                // Options
                String[] options = {
                    getString("OptionPaneDemo.component_op1"),
                    getString("OptionPaneDemo.component_op2"),
                    getString("OptionPaneDemo.component_op3"),
                    getString("OptionPaneDemo.component_op4"),
                    getString("OptionPaneDemo.component_op5")
                };
                int result = JOptionPane.showOptionDialog(
                    getModulePanel(),                             // the parent that the dialog blocks
                    message,                                    // the dialog message array
                    getString("OptionPaneDemo.componenttitle"), // the title of the dialog window
                    JOptionPane.DEFAULT_OPTION,                 // option type
                    JOptionPane.INFORMATION_MESSAGE,            // message type
                    null,                                       // optional icon, use null to use the default icon
                    options,                                    // options string array, will be made into buttons
                    options[3]                                  // option that should be made into a default button
                );
                switch(result) {
                   case 0: // yes
                     JOptionPane.showMessageDialog(getModulePanel(), getString("OptionPaneDemo.component_r1"));
                     break;
                   case 1: // no
                     JOptionPane.showMessageDialog(getModulePanel(), getString("OptionPaneDemo.component_r2"));
                     break;
                   case 2: // maybe
                     JOptionPane.showMessageDialog(getModulePanel(), getString("OptionPaneDemo.component_r3"));
                     break;
                   case 3: // probably
                     JOptionPane.showMessageDialog(getModulePanel(), getString("OptionPaneDemo.component_r4"));
                     break;
                   default:
                     break;
                }

            }
        };
        return createButton(a);
    }

    public JButton createButton(Action a) {
        JButton b = new JButton() {
            public Dimension getMaximumSize() {
                int width = Short.MAX_VALUE;
                int height = super.getMaximumSize().height;
                return new Dimension(width, height);
            }
        };
        // setting the following client property informs the button to show
        // the action text as it's name. The default is to not show the
        // action text.
        b.putClientProperty("displayActionText", Boolean.TRUE);
        b.setAction(a);
        // b.setAlignmentX(JButton.CENTER_ALIGNMENT);
        return b;
    }

}
