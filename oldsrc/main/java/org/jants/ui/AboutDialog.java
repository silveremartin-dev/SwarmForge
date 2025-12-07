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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/*
 * Slightly adapted after SwingSet2 demo by Sun Inc.
 *
 */
public class AboutDialog extends JDialog {

    public AboutDialog(JAnts jAntsApplication) {
        super(jAntsApplication.getFrame(), jAntsApplication.getString("AboutBox.title"), false);
        setLayout(new BorderLayout());
        setResizable(false);

        AboutPanel aboutPanel = new AboutPanel(jAntsApplication);
        getContentPane().add(aboutPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 3, 0));
        buttonPanel.setOpaque(false);
        JButton button = (JButton) buttonPanel.add(
                new JButton(jAntsApplication.getString("AboutBox.ok_button_text"))
        );
        add(buttonPanel, BorderLayout.SOUTH);
        button.addActionListener(new OkAction(this));
    }

    static class AboutPanel extends JPanel {
        ImageIcon aboutImage;
        JAnts jAntsApplication;

        public AboutPanel(JAnts jAntsApplication) {
            this.jAntsApplication = jAntsApplication;
            aboutImage = jAntsApplication.createImageIcon("About.jpg", "AboutBox.accessible_description");
            setOpaque(false);
        }

        public void paint(Graphics g) {
            aboutImage.paintIcon(this, g, 0, 0);
            super.paint(g);
        }

        public Dimension getPreferredSize() {
            return new Dimension(aboutImage.getIconWidth(),
                    aboutImage.getIconHeight());
        }
    }

    static class OkAction extends AbstractAction {
        JDialog aboutBox;

        protected OkAction(JDialog aboutBox) {
            super("OkAction");
            this.aboutBox = aboutBox;
        }

        public void actionPerformed(ActionEvent e) {
            aboutBox.setVisible(false);
        }
    }

}
