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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;

/*
 * Slightly adapted after SwingSet2 demo by Sun Inc.
 *
 * JFileChooserDemo
 *
 * @author Jeff Dinkins
 */
public class FileChooserModule extends JAntsModule {
    private JLabel theImage;
    private Icon jpgIcon;
    private Icon gifIcon;

    public FileChooserModule(JAnts jAntsApplication) {
        // Set the title for this demo, and an icon used to represent this
        // demo inside the jAntsApplication app.
        super(jAntsApplication, "FileChooserDemo", "toolbar/JFileChooser.gif");
        createFileChooserDemo();
    }

    public void createFileChooserDemo() {
        theImage = new JLabel("");
        jpgIcon = createImageIcon("filechooser/jpgIcon.jpg", "jpg image");
        gifIcon = createImageIcon("filechooser/gifIcon.gif", "gif image");

        JPanel demoPanel = getModulePanel();
        demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.X_AXIS));

        demoPanel.add(Box.createRigidArea(VGAP20));
        demoPanel.add(innerPanel);
        demoPanel.add(Box.createRigidArea(VGAP20));

        innerPanel.add(Box.createRigidArea(HGAP20));

        // Create a panel to hold buttons
        JPanel buttonPanel = new JPanel() {
            public Dimension getMaximumSize() {
                return new Dimension(getPreferredSize().width, super.getMaximumSize().height);
            }
        };
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        buttonPanel.add(Box.createRigidArea(VGAP15));
        buttonPanel.add(createPlainFileChooserButton());
        buttonPanel.add(Box.createRigidArea(VGAP15));

        // Create a panel to hold the image
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        JScrollPane scroller = new JScrollPane(theImage);
        scroller.getVerticalScrollBar().setUnitIncrement(10);
        scroller.getHorizontalScrollBar().setUnitIncrement(10);
        imagePanel.add(scroller, BorderLayout.CENTER);

        // add buttons and image panels to inner panel
        innerPanel.add(buttonPanel);
        innerPanel.add(Box.createRigidArea(HGAP30));
        innerPanel.add(imagePanel);
        innerPanel.add(Box.createRigidArea(HGAP20));
    }

    public JFileChooser createFileChooser() {
        // create a filechooser
        JFileChooser fc = new JFileChooser();
        // Add fileFilter & fileView
        javax.swing.filechooser.FileFilter filter = createFileFilter(
                getString("FileChooserDemo.filterdescription"),
                "jpg", "gif");
        BasicFileView fileView = new BasicFileView();
        fileView.putIcon("jpg", jpgIcon);
        fileView.putIcon("gif", gifIcon);
        fc.setFileView(fileView);
        fc.addChoosableFileFilter(filter);
        fc.setFileFilter(filter);

        // set the current directory to be the images directory
        File swingFile = new File("resources/images/About.jpg");
        if(swingFile.exists()) {
            fc.setCurrentDirectory(swingFile);
            fc.setSelectedFile(swingFile);
        }

        return fc;
    }


    public JButton createPlainFileChooserButton() {
        Action a = new AbstractAction(getString("FileChooserDemo.plainbutton")) {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = createFileChooser();

                // show the fileChooser
                int result = fc.showOpenDialog(getModulePanel());

                // if we selected an image, load the image
                if(result == JFileChooser.APPROVE_OPTION) {
                    loadImage(fc.getSelectedFile().getPath());
                }
            }
        };
        return createButton(a);
    }


    private javax.swing.filechooser.FileFilter createFileFilter(
            String description, String...extensions) {
        description = createFileNameFilterDescriptionFromExtensions(
                    description, extensions);
        return new FileNameExtensionFilter(description, extensions);
    }

    private String createFileNameFilterDescriptionFromExtensions(
            String description, String[] extensions) {
        String fullDescription = (description == null) ?
                "(" : description + " (";
        // build the description from the extension list
        fullDescription += "." + extensions[0];
        for (int i = 1; i < extensions.length; i++) {
            fullDescription += ", .";
            fullDescription += extensions[i];
        }
        fullDescription += ")";
        return fullDescription;
    }

    static class FileChooserImageIcon extends ImageIcon {

        public FileChooserImageIcon(String filename) {
            super(filename);
        }

        public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.white);
            g.fillRect(0,0, c.getWidth(), c.getHeight());
            if(getImageObserver() == null) {
                g.drawImage(
                    getImage(),
                    c.getWidth()/2 - getIconWidth()/2,
                    c.getHeight()/2 - getIconHeight()/2,
                    c
                );
            } else {
                g.drawImage(
                    getImage(),
                    c.getWidth()/2 - getIconWidth()/2,
                    c.getHeight()/2 - getIconHeight()/2,
                    getImageObserver()
                );
            }
        }
    }

    public void loadImage(String filename) {
        theImage.setIcon(new FileChooserImageIcon(filename));
    }

    public JButton createButton(Action a) {
        JButton b = new JButton(a) {
            public Dimension getMaximumSize() {
                int width = Short.MAX_VALUE;
                int height = super.getMaximumSize().height;
                return new Dimension(width, height);
            }
        };
        return b;
    }

    public JButton createImageButton(Action a) {
        JButton b = new JButton(a);
        b.setMargin(new Insets(0,0,0,0));
        return b;
    }

}
