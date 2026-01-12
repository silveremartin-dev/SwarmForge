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

package org.jants.ui;import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Dictionary;

/*
 * Slightly adapted after SwingSet2 demo by Sun Inc.
 *
 * @author Dave Kloba
 * @author Jeff Dinkins
 */
public class SliderModule extends JAntsModule {

    /**
     * SliderDemo Constructor
     */
    public SliderModule(JAnts jAntsApplication) {
        // Set the title for this demo, and an icon used to represent this
        // demo inside the jAntsApplication app.
        super(jAntsApplication, "SliderDemo", "toolbar/JSlider.gif");

        createSliderDemo();
    }

    public void createSliderDemo() {
        JSlider s;
        JPanel hp;
        JPanel vp;
        GridLayout g;
        JPanel tp;
        JLabel tf;
        ChangeListener listener;

        getModulePanel().setLayout(new BorderLayout());

        tf = new JLabel(getString("SliderDemo.slidervalue"));
        getModulePanel().add(tf, BorderLayout.SOUTH);

        tp = new JPanel();
        g = new GridLayout(1, 2);
        g.setHgap(5);
        g.setVgap(5);
        tp.setLayout(g);
        getModulePanel().add(tp, BorderLayout.CENTER);

        listener = new SliderListener(tf);

        BevelBorder border = new BevelBorder(BevelBorder.LOWERED);

        hp = new JPanel();
        hp.setLayout(new BoxLayout(hp, BoxLayout.Y_AXIS));
        hp.setBorder(new TitledBorder(
                        border,
                        getString("SliderDemo.horizontal"),
                        TitledBorder.LEFT,
                        TitledBorder.ABOVE_TOP));
        tp.add(hp);

        vp = new JPanel();
        vp.setLayout(new BoxLayout(vp, BoxLayout.X_AXIS));
        vp.setBorder(new TitledBorder(
                        border,
                        getString("SliderDemo.vertical"),
                        TitledBorder.LEFT,
                        TitledBorder.ABOVE_TOP));
        tp.add(vp);

        // Horizontal Slider 1
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder(getString("SliderDemo.plain")));
        s = new JSlider(-10, 100, 20);
        s.getAccessibleContext().setAccessibleName(getString("SliderDemo.plain"));
        s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.a_plain_slider"));
        s.addChangeListener(listener);

        p.add(Box.createRigidArea(VGAP5));
        p.add(s);
        p.add(Box.createRigidArea(VGAP5));
        hp.add(p);
        hp.add(Box.createRigidArea(VGAP10));

        // Horizontal Slider 2
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder(getString("SliderDemo.majorticks")));
        s = new JSlider(100, 1000, 400);
        s.setPaintTicks(true);
        s.setMajorTickSpacing(100);
        s.getAccessibleContext().setAccessibleName(getString("SliderDemo.majorticks"));
        s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.majorticksdescription"));
        s.addChangeListener(listener);

        p.add(Box.createRigidArea(VGAP5));
        p.add(s);
        p.add(Box.createRigidArea(VGAP5));
        hp.add(p);
        hp.add(Box.createRigidArea(VGAP10));

        // Horizontal Slider 3
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder(getString("SliderDemo.ticks")));
        s = new JSlider(0, 11, 6);

        s.putClientProperty("JSlider.isFilled", Boolean.TRUE );

        s.setPaintTicks(true);
        s.setMajorTickSpacing(5);
        s.setMinorTickSpacing(1);

        s.setPaintLabels( true );
        s.setSnapToTicks( true );

        @SuppressWarnings("unchecked")
        Dictionary<Object, Object> labelTable = s.getLabelTable();
        labelTable.put(Integer.valueOf(11), new JLabel(Integer.valueOf(11).toString(), JLabel.CENTER));
        s.setLabelTable( s.getLabelTable() );

        s.getAccessibleContext().setAccessibleName(getString("SliderDemo.minorticks"));
        s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.minorticksdescription"));

        s.addChangeListener(listener);

        p.add(Box.createRigidArea(VGAP5));
        p.add(s);
        p.add(Box.createRigidArea(VGAP5));
        hp.add(p);
        hp.add(Box.createRigidArea(VGAP10));

        // Horizontal Slider 4
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new TitledBorder(getString("SliderDemo.disabled")));
        BoundedRangeModel brm = new DefaultBoundedRangeModel(80, 0, 0, 100);
        s = new JSlider(brm);
        s.setPaintTicks(true);
        s.setMajorTickSpacing(20);
        s.setMinorTickSpacing(5);
        s.setEnabled(false);
        s.getAccessibleContext().setAccessibleName(getString("SliderDemo.disabled"));
        s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.disableddescription"));
        s.addChangeListener(listener);

        p.add(Box.createRigidArea(VGAP5));
        p.add(s);
        p.add(Box.createRigidArea(VGAP5));
        hp.add(p);

        //////////////////////////////////////////////////////////////////////////////

        // Vertical Slider 1
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new TitledBorder(getString("SliderDemo.plain")));
        s = new JSlider(JSlider.VERTICAL, -10, 100, 20);
        s.getAccessibleContext().setAccessibleName(getString("SliderDemo.plain"));
        s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.a_plain_slider"));
        s.addChangeListener(listener);
        p.add(Box.createRigidArea(HGAP10));
        p.add(s);
        p.add(Box.createRigidArea(HGAP10));
        vp.add(p);
        vp.add(Box.createRigidArea(HGAP10));

        // Vertical Slider 2
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new TitledBorder(getString("SliderDemo.majorticks")));
        s = new JSlider(JSlider.VERTICAL, 100, 1000, 400);

        s.putClientProperty( "JSlider.isFilled", Boolean.TRUE );

        s.setPaintTicks(true);
        s.setMajorTickSpacing(100);
        s.getAccessibleContext().setAccessibleName(getString("SliderDemo.majorticks"));
        s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.majorticksdescription"));
        s.addChangeListener(listener);
        p.add(Box.createRigidArea(HGAP25));
        p.add(s);
        p.add(Box.createRigidArea(HGAP25));
        vp.add(p);
        vp.add(Box.createRigidArea(HGAP5));

        // Vertical Slider 3
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new TitledBorder(getString("SliderDemo.minorticks")));
        s = new JSlider(JSlider.VERTICAL, 0, 100, 60);
        s.setPaintTicks(true);
        s.setMajorTickSpacing(20);
        s.setMinorTickSpacing(5);

        s.setPaintLabels( true );

        s.getAccessibleContext().setAccessibleName(getString("SliderDemo.minorticks"));
        s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.minorticksdescription"));

        s.addChangeListener(listener);
        p.add(Box.createRigidArea(HGAP10));
        p.add(s);
        p.add(Box.createRigidArea(HGAP10));
        vp.add(p);
        vp.add(Box.createRigidArea(HGAP5));

        // Vertical Slider 4
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new TitledBorder(getString("SliderDemo.disabled")));
        s = new JSlider(JSlider.VERTICAL, 0, 100, 80);
        s.setPaintTicks(true);
        s.setMajorTickSpacing(20);
        s.setMinorTickSpacing(5);
        s.setEnabled(false);
        s.getAccessibleContext().setAccessibleName(getString("SliderDemo.disabled"));
        s.getAccessibleContext().setAccessibleDescription(getString("SliderDemo.disableddescription"));
        s.addChangeListener(listener);
        p.add(Box.createRigidArea(HGAP20));
        p.add(s);
        p.add(Box.createRigidArea(HGAP20));
        vp.add(p);
    }

    class SliderListener implements ChangeListener {
        JLabel tf;
        public SliderListener(JLabel f) {
            tf = f;
        }
        public void stateChanged(ChangeEvent e) {
            JSlider s1 = (JSlider)e.getSource();
            tf.setText(getString("SliderDemo.slidervalue") + s1.getValue());
        }
    }
}
