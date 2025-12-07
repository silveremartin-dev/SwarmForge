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
import java.awt.event.*;

/*
 * Slightly adapted after SwingSet2 demo by Sun Inc.
 *
 * The LayoutControlPanel contains controls for setting an
 * AbstractButton's horizontal and vertical text position and
 * horizontal and vertical alignment.
 */

public class LayoutControlPanel extends JPanel implements SwingConstants {

    private boolean  absolutePositions;
    private final DirectionPanel textPosition;
    private final DirectionPanel labelAlignment;
    private final ButtonModule module;

    LayoutControlPanel(ButtonModule module) {
        this.module = module;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
        setAlignmentY(TOP_ALIGNMENT);

        JLabel l;

        absolutePositions = true;

        textPosition = new DirectionPanel(true, "E", new TextPositionListener());
        labelAlignment = new DirectionPanel(true, "C", new LabelAlignmentListener());

        // Make sure the controls' text position and label alignment match
        // the initial value of the associated direction panel.
        for(int i = 0; i < module.getCurrentControls().size(); i++) {
            Component c = module.getCurrentControls().elementAt(i);
            setPosition(c, RIGHT, CENTER);
            setAlignment(c,CENTER,CENTER);
        }

        l = new JLabel(module.getString("LayoutControlPanel.textposition_label"));
        add(l);
        add(textPosition);

        add(Box.createRigidArea(module.VGAP20));

        l = new JLabel(module.getString("LayoutControlPanel.contentalignment_label"));
        add(l);
        add(labelAlignment);

        add(Box.createGlue());
    }


    class OrientationChangeListener implements ActionListener {

        public void actionPerformed( ActionEvent e ) {
            if( !e.getActionCommand().equals("OrientationChanged") ){
                return;
            }
            if( absolutePositions ){
                return;
            }

            String currentTextPosition = textPosition.getSelection();
            if( currentTextPosition.equals("NW") )
                textPosition.setSelection("NE");
            else if( currentTextPosition.equals("NE") )
                textPosition.setSelection("NW");
            else if( currentTextPosition.equals("E") )
                textPosition.setSelection("W");
            else if( currentTextPosition.equals("W") )
                textPosition.setSelection("E");
            else if( currentTextPosition.equals("SE") )
                textPosition.setSelection("SW");
            else if( currentTextPosition.equals("SW") )
                textPosition.setSelection("SE");

            String currentLabelAlignment = labelAlignment.getSelection();
            if( currentLabelAlignment.equals("NW") )
                labelAlignment.setSelection("NE");
            else if( currentLabelAlignment.equals("NE") )
                labelAlignment.setSelection("NW");
            else if( currentLabelAlignment.equals("E") )
                labelAlignment.setSelection("W");
            else if( currentLabelAlignment.equals("W") )
                labelAlignment.setSelection("E");
            else if( currentLabelAlignment.equals("SE") )
                labelAlignment.setSelection("SW");
            else if( currentLabelAlignment.equals("SW") )
                labelAlignment.setSelection("SE");
        }
    }

    class PositioningListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            JRadioButton rb = (JRadioButton) e.getSource();
            if(rb.getText().equals("Absolute") && rb.isSelected()) {
                absolutePositions = true;
            } else if(rb.getText().equals("Relative") && rb.isSelected()) {
                absolutePositions = false;
            }

            for(int i = 0; i < module.getCurrentControls().size(); i++) {
                Component c = module.getCurrentControls().elementAt(i);
                int hPos, vPos, hAlign, vAlign;
                if( c instanceof AbstractButton ) {
                   hPos = ((AbstractButton)c).getHorizontalTextPosition();
                   vPos = ((AbstractButton)c).getVerticalTextPosition();
                   hAlign = ((AbstractButton)c).getHorizontalAlignment();
                   vAlign = ((AbstractButton)c).getVerticalAlignment();
                } else if( c instanceof JLabel ) {
                   hPos = ((JLabel)c).getHorizontalTextPosition();
                   vPos = ((JLabel)c).getVerticalTextPosition();
                   hAlign = ((JLabel)c).getHorizontalAlignment();
                   vAlign = ((JLabel)c).getVerticalAlignment();
                } else {
                    continue;
                }
                setPosition(c, hPos, vPos);
                setAlignment(c, hAlign, vAlign);
            }

            module.invalidate();
            module.validate();
            module.repaint();
        }
    }

    class TextPositionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JRadioButton rb = (JRadioButton) e.getSource();
            if(!rb.isSelected()) {
                return;
            }
            String cmd = rb.getActionCommand();
            int hPos, vPos;
            if(cmd.equals("NW")) {
                    hPos = LEFT; vPos = TOP;
            } else if(cmd.equals("N")) {
                    hPos = CENTER; vPos = TOP;
            } else if(cmd.equals("NE")) {
                    hPos = RIGHT; vPos = TOP;
            } else if(cmd.equals("W")) {
                    hPos = LEFT; vPos = CENTER;
            } else if(cmd.equals("C")) {
                    hPos = CENTER; vPos = CENTER;
            } else if(cmd.equals("E")) {
                    hPos = RIGHT; vPos = CENTER;
            } else if(cmd.equals("SW")) {
                    hPos = LEFT; vPos = BOTTOM;
            } else if(cmd.equals("S")) {
                    hPos = CENTER; vPos = BOTTOM;
            } else /*if(cmd.equals("SE"))*/ {
                    hPos = RIGHT; vPos = BOTTOM;
            }
            for(int i = 0; i < module.getCurrentControls().size(); i++) {
                Component c = module.getCurrentControls().elementAt(i);
                setPosition(c, hPos, vPos);
            }
            module.invalidate();
            module.validate();
            module.repaint();
        }
    }

    class LabelAlignmentListener implements  ActionListener {

        public void actionPerformed(ActionEvent e) {
            JRadioButton rb = (JRadioButton) e.getSource();
            if(!rb.isSelected()) {
                return;
            }
            String cmd = rb.getActionCommand();
            int hPos, vPos;
            if(cmd.equals("NW")) {
                    hPos = LEFT; vPos = TOP;
            } else if(cmd.equals("N")) {
                    hPos = CENTER; vPos = TOP;
            } else if(cmd.equals("NE")) {
                    hPos = RIGHT; vPos = TOP;
            } else if(cmd.equals("W")) {
                    hPos = LEFT; vPos = CENTER;
            } else if(cmd.equals("C")) {
                    hPos = CENTER; vPos = CENTER;
            } else if(cmd.equals("E")) {
                    hPos = RIGHT; vPos = CENTER;
            } else if(cmd.equals("SW")) {
                    hPos = LEFT; vPos = BOTTOM;
            } else if(cmd.equals("S")) {
                    hPos = CENTER; vPos = BOTTOM;
            } else /*if(cmd.equals("SE"))*/ {
                    hPos = RIGHT; vPos = BOTTOM;
            }
            for(int i = 0; i < module.getCurrentControls().size(); i++) {
                Component c = module.getCurrentControls().elementAt(i);
                setAlignment(c,hPos,vPos);
                c.invalidate();
            }
            module.invalidate();
            module.validate();
            module.repaint();
        }
    }

    void setPosition(Component c, int hPos, int vPos) {
        boolean ltr = true;
        ltr = c.getComponentOrientation().isLeftToRight();
        if( absolutePositions ) {
            if( hPos == LEADING ) {
                hPos = ltr ? LEFT : RIGHT;
            } else if( hPos == TRAILING ) {
                hPos = ltr ? RIGHT : LEFT;
            }
        } else {
            if( hPos == LEFT ) {
                hPos = ltr ? LEADING : TRAILING;
            } else if( hPos == RIGHT ) {
                hPos = ltr ? TRAILING : LEADING;
            }
        }
        if(c instanceof AbstractButton) {
            AbstractButton x = (AbstractButton) c;
            x.setHorizontalTextPosition(hPos);
            x.setVerticalTextPosition(vPos);
        } else if(c instanceof JLabel) {
            JLabel x = (JLabel) c;
            x.setHorizontalTextPosition(hPos);
            x.setVerticalTextPosition(vPos);
        }
    }

    void setAlignment(Component c, int hPos, int vPos) {
        boolean ltr = true;
        ltr = c.getComponentOrientation().isLeftToRight();
        if( absolutePositions ) {
            if( hPos == LEADING ) {
                hPos = ltr ? LEFT : RIGHT;
            } else if( hPos == TRAILING ) {
                hPos = ltr ? RIGHT : LEFT;
            }
        } else {
            if( hPos == LEFT ) {
                hPos = ltr ? LEADING : TRAILING;
            } else if( hPos == RIGHT ) {
                hPos = ltr ? TRAILING : LEADING;
            }
        }
        if(c instanceof AbstractButton) {
            AbstractButton x = (AbstractButton) c;
            x.setHorizontalAlignment(hPos);
            x.setVerticalAlignment(vPos);
        } else if(c instanceof JLabel) {
            JLabel x = (JLabel) c;
            x.setHorizontalAlignment(hPos);
            x.setVerticalAlignment(vPos);
        }
    }
}
