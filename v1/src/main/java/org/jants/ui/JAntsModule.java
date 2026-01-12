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

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;

/*
 * Slightly adapted after SwingSet2 demo by Sun Inc.
 *
 */
public class JAntsModule extends JFrame {

    // The preferred size of the demo
    private final int PREFERRED_WIDTH = 680;
    private final int PREFERRED_HEIGHT = 600;

    Border loweredBorder = new CompoundBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED),
                                              new EmptyBorder(5,5,5,5));

    // Premade convenience dimensions, for use wherever you need 'em.
    public static Dimension HGAP2 = new Dimension(2,1);
    public static Dimension VGAP2 = new Dimension(1,2);

    public static Dimension HGAP5 = new Dimension(5,1);
    public static Dimension VGAP5 = new Dimension(1,5);

    public static Dimension HGAP10 = new Dimension(10,1);
    public static Dimension VGAP10 = new Dimension(1,10);

    public static Dimension HGAP15 = new Dimension(15,1);
    public static Dimension VGAP15 = new Dimension(1,15);

    public static Dimension HGAP20 = new Dimension(20,1);
    public static Dimension VGAP20 = new Dimension(1,20);

    public static Dimension HGAP25 = new Dimension(25,1);
    public static Dimension VGAP25 = new Dimension(1,25);

    public static Dimension HGAP30 = new Dimension(30,1);
    public static Dimension VGAP30 = new Dimension(1,30);

    private final JAnts jAntsApplication;
    private final JPanel panel;
    private final String resourceName;
    private final String iconPath;

    public JAntsModule(JAnts jAntsApplication) {
        this(jAntsApplication, null, null);
    }

    public JAntsModule(JAnts jAntsApplication, String resourceName, String iconPath) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        this.resourceName = resourceName;
        this.iconPath = iconPath;
        this.jAntsApplication = jAntsApplication;
    }

    public String getResourceName() {
        return resourceName;
    }

    public JPanel getModulePanel() {
        return panel;
    }

    public JAnts getJAntsApplication() {
        return jAntsApplication;
    }


    public String getString(String key) {
        if (getJAntsApplication() != null) {
            return getJAntsApplication().getString(key);
        }else{
            return "";
        }
    }

    public char getMnemonic(String key) {
        return (getString(key)).charAt(0);
    }

    public ImageIcon createImageIcon(String filename, String description) {
        if(getJAntsApplication() != null) {
            return getJAntsApplication().createImageIcon(filename, description);
        } else {
            String path = "/resources/images/" + filename;
            return new ImageIcon(getClass().getResource(path), description);
        }
    }

    public String getName() {
        return getString(getResourceName() + ".name");
    }

    public Icon getIcon() {
        return createImageIcon(iconPath, getResourceName() + ".name");
    }

    public String getToolTip() {
        return getString(getResourceName() + ".tooltip");
    }

    public void mainImpl() {
        JFrame frame = new JFrame(getName());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(getModulePanel(), BorderLayout.CENTER);
        getModulePanel().setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));
        frame.pack();
        frame.setVisible(true);
    }

    public JPanel createHorizontalPanel(boolean threeD) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setAlignmentX(LEFT_ALIGNMENT);
        if(threeD) {
            p.setBorder(loweredBorder);
        }
        return p;
    }

    public JPanel createVerticalPanel(boolean threeD) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentY(TOP_ALIGNMENT);
        p.setAlignmentX(LEFT_ALIGNMENT);
        if(threeD) {
            p.setBorder(loweredBorder);
        }
        return p;
    }

    public void init() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(getModulePanel(), BorderLayout.CENTER);
    }

    void updateDragEnabled(boolean dragEnabled) {}
}
