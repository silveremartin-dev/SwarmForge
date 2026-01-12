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
import javax.swing.event.*;
import javax.swing.border.*;

import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
// TODO
// https://github.com/LGoodDatePicker/ ou https://github.com/JDatePicker/JDatePicker ou https://www.javacodex.com/Swing/Swing-Calendar
// https://www.jfree.org/jfreechart/samples.html
// https://koor.fr/Java/TutorialSwing/swing_AbstractAction.wp
public class JAnts extends JPanel {

    String[] modules = {
      "ButtonModule",
      "ComboBoxModule",
      "FileChooserModule",
      "HTMLManualModule",
      "OptionPaneModule",
      "ProgressBarModule",
      "ScrollPaneModule",
      "SliderModule",
    };

    void loadModules() {
        for(int i = 0; i < modules.length;) {
            loadModules(modules[i]);
            i++;
        }
    }

    // The current Look & Feel
    private static LookAndFeelData currentLookAndFeel;
    private static LookAndFeelData[] lookAndFeelData;
    // List of modules
    private final ArrayList<JAntsModule> modulesList = new ArrayList<>();

    // The preferred size of the module
    private static final int PREFERRED_WIDTH = 720;
    private static final int PREFERRED_HEIGHT = 640;

    // A place to hold on to the visible module
    private static JAntsModule currentModule = null;
    private JPanel modulePanel = null;

    // About Box
    private JDialog aboutBox = null;

    // Status Bar
    private JTextField statusField = null;

    // Tool Bar
    private ToggleButtonToolBar toolbar = null;
    private final ButtonGroup toolbarGroup = new ButtonGroup();

    // Menus
    private JMenuBar menuBar = null;
    private JMenu lafMenu = null;
    private JMenu themesMenu = null;
    private JMenu optionsMenu = null;
    private final ButtonGroup lafMenuGroup = new ButtonGroup();
    private final ButtonGroup themesMenuGroup = new ButtonGroup();

    // Popup menu
    private JPopupMenu popupMenu = null;
    private final ButtonGroup popupMenuGroup = new ButtonGroup();

    // Used only if jAntsApplication is an application
    private final JFrame frame;

    // The tab pane that holds the demo
    private JTabbedPane tabbedPane;

    private JEditorPane demoSrcPane;


    // contentPane cache, saved from the applet or application frame
    Container contentPane;


    // number of jAntsApplications - for multiscreen
    // keep track of the number of jAntsApplications created - we only want to exit
    // the program when the last one has been closed.
    private static int numSSs = 0;
    private static final Vector<JAnts> jAntsApplications = new Vector<>();

    public JAnts() {
        this(null);
    }

    /**
     * jAntsApplication Constructor
     */
    public JAnts(GraphicsConfiguration gc) {
        String lafClassName = UIManager.getLookAndFeel().getClass().getName();
        lookAndFeelData = getInstalledLookAndFeelData();
        currentLookAndFeel = Arrays.stream(lookAndFeelData)
                .filter(laf -> lafClassName.equals(laf.className))
                .findFirst().get();

        frame = createFrame(gc);

        //https://stackoverflow.com/questions/2011601/jframe-without-frame-border-maximum-button-minimum-button-and-frame-icon
        //frame.setUndecorated(true);

        // set the layout
        setLayout(new BorderLayout());

        // set the preferred size of the demo
        setPreferredSize(new Dimension(PREFERRED_WIDTH,PREFERRED_HEIGHT));

        initializeModule();
        preloadFirstModule();

        showJAntsApplication();

        // Start loading the rest of the demo in the background
        ModuleLoadThread moduleLoader = new ModuleLoadThread(this);
        moduleLoader.start();
    }


    /**
     * jAntsApplication Main. Called only if we're an application, not an applet.
     */
    public static void main(final String[] args) {

        // must run in EDT when constructing the GUI components
        SwingUtilities.invokeLater(() -> {
            // Create jAntsApplication on the default monitor
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            JAnts jAntsApplication = new JAnts(GraphicsEnvironment.
                                         getLocalGraphicsEnvironment().
                                         getDefaultScreenDevice().
                                         getDefaultConfiguration());
        });
    }

    // *******************************************************
    // *************** Demo Loading Methods ******************
    // *******************************************************



    public void initializeModule() {
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);

        menuBar = createMenus();

        frame.setJMenuBar(menuBar);

        // creates popup menu accessible via keyboard
        popupMenu = createPopupMenu();

        ToolBarPanel toolbarPanel = new ToolBarPanel();
        toolbarPanel.setLayout(new BorderLayout());
        toolbar = new ToggleButtonToolBar();
        toolbarPanel.add(toolbar, BorderLayout.CENTER);
        top.add(toolbarPanel, BorderLayout.SOUTH);
        toolbarPanel.addContainerListener(toolbarPanel);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.getModel().addChangeListener(new TabListener());

        statusField = new JTextField("");
        statusField.setEditable(false);
        add(statusField, BorderLayout.SOUTH);

        modulePanel = new JPanel();
        modulePanel.setLayout(new BorderLayout());
        modulePanel.setBorder(new EtchedBorder());
        tabbedPane.addTab("Hi There!", modulePanel);
    }

    JAntsModule currentTabDemo = null;
    class TabListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            SingleSelectionModel model = (SingleSelectionModel) e.getSource();
            boolean srcSelected = model.getSelectedIndex() == 1;
            if(currentTabDemo != currentModule && demoSrcPane != null && srcSelected) {
                demoSrcPane.setText(getString("SourceCode.loading"));
                repaint();
            }
        }
    }


    /**
     * Create menus
     */
    public JMenuBar createMenus() {
        JMenuItem mi;
        // ***** create the menuBar ****
        JMenuBar menuBar = new JMenuBar();
        menuBar.getAccessibleContext().setAccessibleName(
            getString("MenuBar.accessible_description"));

        // ***** create File menu
        JMenu fileMenu = menuBar.add(new JMenu(getString("FileMenu.file_label")));
        fileMenu.setMnemonic(getMnemonic("FileMenu.file_mnemonic"));
        fileMenu.getAccessibleContext().setAccessibleDescription(getString("FileMenu.accessible_description"));

        createMenuItem(fileMenu, "FileMenu.about_label", "FileMenu.about_mnemonic",
                       "FileMenu.about_accessible_description", new AboutAction(this));

        fileMenu.addSeparator();

        createMenuItem(fileMenu, "FileMenu.open_label", "FileMenu.open_mnemonic",
                       "FileMenu.open_accessible_description", null);

        createMenuItem(fileMenu, "FileMenu.save_label", "FileMenu.save_mnemonic",
                       "FileMenu.save_accessible_description", null);

        createMenuItem(fileMenu, "FileMenu.save_as_label", "FileMenu.save_as_mnemonic",
                       "FileMenu.save_as_accessible_description", null);


        fileMenu.addSeparator();

        createMenuItem(fileMenu, "FileMenu.exit_label", "FileMenu.exit_mnemonic",
                       "FileMenu.exit_accessible_description", new ExitAction(this)
        );

        // Create these menu items for the first jAntsApplication only.
        if (numSSs == 0) {
        // ***** create laf switcher menu
        lafMenu = menuBar.add(new JMenu(getString("LafMenu.laf_label")));
        lafMenu.setMnemonic(getMnemonic("LafMenu.laf_mnemonic"));
        lafMenu.getAccessibleContext().setAccessibleDescription(
            getString("LafMenu.laf_accessible_description"));

        for (LookAndFeelData lafData : lookAndFeelData) {
            mi = createLafMenuItem(lafMenu, lafData);
            mi.setSelected(lafData.equals(currentLookAndFeel));
        }

        // ***** create themes menu
        themesMenu = menuBar.add(new JMenu(getString("ThemesMenu.themes_label")));
        themesMenu.setMnemonic(getMnemonic("ThemesMenu.themes_mnemonic"));
        themesMenu.getAccessibleContext().setAccessibleDescription(
            getString("ThemesMenu.themes_accessible_description"));

        // ***** create the options menu
        optionsMenu = menuBar.add(
            new JMenu(getString("OptionsMenu.options_label")));
        optionsMenu.setMnemonic(getMnemonic("OptionsMenu.options_mnemonic"));
        optionsMenu.getAccessibleContext().setAccessibleDescription(
            getString("OptionsMenu.options_accessible_description"));

        // ***** create tool tip submenu item.
        mi = createCheckBoxMenuItem(optionsMenu, "OptionsMenu.tooltip_label",
                "OptionsMenu.tooltip_mnemonic",
                "OptionsMenu.tooltip_accessible_description",
                new ToolTipAction());
        mi.setSelected(true);

        // ***** create drag support submenu item.
        createCheckBoxMenuItem(optionsMenu, "OptionsMenu.dragEnabled_label",
                "OptionsMenu.dragEnabled_mnemonic",
                "OptionsMenu.dragEnabled_accessible_description",
                new DragSupportAction());

        }


        // ***** create the multiscreen menu, if we have multiple screens
        GraphicsDevice[] screens = GraphicsEnvironment.
                                    getLocalGraphicsEnvironment().
                                    getScreenDevices();
        if (screens.length > 1) {

            JMenu multiScreenMenu = menuBar.add(new JMenu(
                                     getString("MultiMenu.multi_label")));

            multiScreenMenu.setMnemonic(getMnemonic("MultiMenu.multi_mnemonic"));
            multiScreenMenu.getAccessibleContext().setAccessibleDescription(
             getString("MultiMenu.multi_accessible_description"));

            createMultiscreenMenuItem(multiScreenMenu, MultiScreenAction.ALL_SCREENS);
            for (int i = 0; i < screens.length; i++) {
                createMultiscreenMenuItem(multiScreenMenu, i);
            }
        }

        return menuBar;
    }

    /**
     * Create a checkbox menu menu item
     */
    private JMenuItem createCheckBoxMenuItem(JMenu menu, String label,
                                             String mnemonic,
                                             String accessibleDescription,
                                             Action action) {
        JCheckBoxMenuItem mi = (JCheckBoxMenuItem)menu.add(
                new JCheckBoxMenuItem(getString(label)));
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(
                accessibleDescription));
        mi.addActionListener(action);
        return mi;
    }

    /**
     * Create a radio button menu item for items that are part of a
     * button group.
     */
    private JMenuItem createButtonGroupMenuItem(JMenu menu, String label,
                                                String mnemonic,
                                                String accessibleDescription,
                                                Action action,
                                                ButtonGroup buttonGroup) {
        JRadioButtonMenuItem mi = (JRadioButtonMenuItem)menu.add(
                new JRadioButtonMenuItem(getString(label)));
        buttonGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(
                accessibleDescription));
        mi.addActionListener(action);
        return mi;
    }

    /**
     * Creates a generic menu item
     */
    public JMenuItem createMenuItem(JMenu menu, String label, String mnemonic,
                               String accessibleDescription, Action action) {
        JMenuItem mi = menu.add(new JMenuItem(getString(label)));
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(action);
        if(action == null) {
            mi.setEnabled(false);
        }
        return mi;
    }

    /**
     * Creates a JRadioButtonMenuItem for the Themes menu
     */
    public JMenuItem createThemesMenuItem(JMenu menu, String label, String mnemonic,
                               String accessibleDescription, MetalTheme theme) {
        JRadioButtonMenuItem mi = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(getString(label)));
        themesMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(new ChangeThemeAction(this, theme));

        return mi;
    }

    /**
     * Creates a JRadioButtonMenuItem for the Look and Feel menu
     */
    public JMenuItem createLafMenuItem(JMenu menu, LookAndFeelData lafData) {
        JMenuItem mi = menu.add(new JRadioButtonMenuItem(lafData.label));
        lafMenuGroup.add(mi);
        mi.setMnemonic(lafData.mnemonic);
        mi.getAccessibleContext().setAccessibleDescription(lafData.accDescription);
        mi.addActionListener(new ChangeLookAndFeelAction(this, lafData));
        return mi;
    }

    /**
     * Creates a multi-screen menu item
     */
    public JMenuItem createMultiscreenMenuItem(JMenu menu, int screen) {
        JMenuItem mi;
        if (screen == MultiScreenAction.ALL_SCREENS) {
            mi = menu.add(new JMenuItem(getString("MultiMenu.all_label")));
            mi.setMnemonic(getMnemonic("MultiMenu.all_mnemonic"));
            mi.getAccessibleContext().setAccessibleDescription(getString(
                                                                 "MultiMenu.all_accessible_description"));
        }
        else {
            mi = menu.add(new JMenuItem(getString("MultiMenu.single_label") + " " +
                                                                                                 screen));
            mi.setMnemonic(KeyEvent.VK_0 + screen);
            mi.getAccessibleContext().setAccessibleDescription(getString(
                                               "MultiMenu.single_accessible_description") + " " + screen);

        }
        mi.addActionListener(new MultiScreenAction(this, screen));
        return mi;
    }

    public JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu("JPopupMenu demo");

        for (LookAndFeelData lafData : lookAndFeelData) {
            createPopupMenuItem(popup, lafData);
        }

        // register key binding to activate popup menu
        InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.SHIFT_DOWN_MASK),
                "postMenuAction");
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), "postMenuAction");
        getActionMap().put("postMenuAction", new ActivatePopupMenuAction(this, popup));

        return popup;
    }

    /**
     * Creates a JMenuItem for the Look and Feel popup menu
     */
    public JMenuItem createPopupMenuItem(JPopupMenu menu, LookAndFeelData lafData) {
        JMenuItem mi = menu.add(new JMenuItem(lafData.label));
        popupMenuGroup.add(mi);
        mi.setMnemonic(lafData.mnemonic);
        mi.getAccessibleContext().setAccessibleDescription(lafData.accDescription);
        mi.addActionListener(new ChangeLookAndFeelAction(this, lafData));
        return mi;
    }


    /**
     * Load the first demo. This is done separately from the remaining demos
     * so that we can get jAntsApplication up and available to the user quickly.
     */
    public void preloadFirstModule() {
        JAntsModule module = addModule(new SimulationModule(this));
        setModule(module);
    }


    /**
     * Add a demo to the toolbar
     */
    public JAntsModule addModule(JAntsModule module) {
        modulesList.add(module);
        // do the following on the gui thread
        SwingUtilities.invokeLater(new jAntsApplicationRunnable(this, module) {
            public void run() {
                SwitchToModuleAction action = new SwitchToModuleAction(jAntsApplication, (JAntsModule) obj);
                JToggleButton tb = jAntsApplication.getToolBar().addToggleButton(action);
                jAntsApplication.getToolBarGroup().add(tb);
                if(jAntsApplication.getToolBarGroup().getSelection() == null) {
                    tb.setSelected(true);
                }
                tb.setText(null);
                tb.setToolTipText(((JAntsModule)obj).getToolTip());

                if(modules[modules.length-1].equals(obj.getClass().getName())) {
                    setStatus(getString("Status.popupMenuAccessible"));
                }

            }
        });
        return module;
    }


    /**
     * Sets the current demo
     */
    public void setModule(JAntsModule module) {
        currentModule = module;

        // Ensure panel's UI is current before making visible
        JComponent currentDemoPanel = module.getModulePanel();
        SwingUtilities.updateComponentTreeUI(currentDemoPanel);

        modulePanel.removeAll();
        modulePanel.add(currentDemoPanel, BorderLayout.CENTER);

        tabbedPane.setSelectedIndex(0);
        tabbedPane.setTitleAt(0, module.getName());
        tabbedPane.setToolTipTextAt(0, module.getToolTip());
    }


    /**
     * Bring up the jAntsApplication demo by showing the frame
     */
    public void showJAntsApplication() {
        // put jAntsApplication in a frame and show it
        JFrame f = getFrame();
        f.setTitle(getString("Frame.title"));
        f.getContentPane().add(this, BorderLayout.CENTER);
        f.pack();

        Rectangle screenRect = f.getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                f.getGraphicsConfiguration());

        // Make sure we don't place the demo off the screen.
        int centerWidth = screenRect.width < f.getSize().width ?
                screenRect.x :
                screenRect.x + screenRect.width/2 - f.getSize().width/2;
        int centerHeight = screenRect.height < f.getSize().height ?
                screenRect.y :
                screenRect.y + screenRect.height/2 - f.getSize().height/2;

        centerHeight = Math.max(centerHeight, screenInsets.top);

        f.setLocation(centerWidth, centerHeight);
        f.setVisible(true);
        numSSs++;
        jAntsApplications.add(this);
    }

    // *******************************************************
    // ****************** Utility Methods ********************
    // *******************************************************

    /**
     * Loads a module from a classname
     */
    void loadModule(String classname) {
        setStatus(getString("Status.loading") + getString(classname + ".name"));
        JAntsModule module;
        try {
            Class<?> moduleClass = Class.forName(classname);
            Constructor<?> moduleConstructor = moduleClass.getConstructor(new Class[]{JAnts.class});
            module = (JAntsModule) moduleConstructor.newInstance(new Object[]{this});
            addDemo(module);
        } catch (Exception e) {
            System.out.println("Error occurred loading module: " + classname);
        }
    }

    /**
     * Returns the frame instance
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Returns the menuBar
     */
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Returns the toolbar
     */
    public ToggleButtonToolBar getToolBar() {
        return toolbar;
    }

    /**
     * Returns the toolbar button group
     */
    public ButtonGroup getToolBarGroup() {
        return toolbarGroup;
    }

    /**
     * Returns the content pane whether we're in an applet
     * or application
     */
    public Container getContentPane() {
        if(contentPane == null) {
            if(getFrame() != null) {
                contentPane = getFrame().getContentPane();
            }
        }
        return contentPane;
    }

    /**
     * Create a frame for jAntsApplication to reside in if brought up
     * as an application.
     */
    public static JFrame createFrame(GraphicsConfiguration gc) {
        JFrame frame = new JFrame(gc);
        if (numSSs == 0) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            WindowListener l = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    numSSs--;
                    jAntsApplications.remove(this);
                }
            };
            frame.addWindowListener(l);
        }
        return frame;
    }

    /**
     * Set the status
     */
    public void setStatus(String s) {
        // do the following on the gui thread
        SwingUtilities.invokeLater(new jAntsApplicationRunnable(this, s) {
            public void run() {
                jAntsApplication.statusField.setText((String) obj);
            }
        });
    }

    /**
     * This method returns a string from the demo's resource bundle.
     */
    public static String getString(String key) {
        String value = null;
        try {
            value = TextAndMnemonicUtils.getTextAndMnemonicString(key);
        } catch (MissingResourceException e) {
            System.out.println("java.util.MissingResourceException: Couldn't find value for: " + key);
        }
        if(value == null) {
            value = "Could not find resource: " + key + "  ";
        }
        return value;
    }

    /**
     * Returns a mnemonic from the resource bundle. Typically used as
     * keyboard shortcuts in menu items.
     */
    public char getMnemonic(String key) {
        return (getString(key)).charAt(0);
    }

    /**
     * Creates an icon from an image contained in the "images" directory.
     */
    public ImageIcon createImageIcon(String filename, String description) {
        String path = "/resources/images/" + filename;
        return new ImageIcon(getClass().getResource(path));
    }

    /**
     * Stores the current L&F, and calls updateLookAndFeel, below
     */
    public void setLookAndFeel(LookAndFeelData laf) {
        if(!currentLookAndFeel.equals(laf)) {
            currentLookAndFeel = laf;
            /* The recommended way of synchronizing state between multiple
             * controls that represent the same command is to use Actions.
             * The code below is a workaround and will be replaced in future
             * version of jAntsApplication demo.
             */
            String lafName = laf.label;
            themesMenu.setEnabled(laf.name.equals("Metal"));
            updateLookAndFeel();
            for(int i=0;i<lafMenu.getItemCount();i++) {
                JMenuItem item = lafMenu.getItem(i);
                item.setSelected(item.getText().equals(lafName));
            }
        }
    }

    private void updateThisJAntsApplication() {
        JFrame frame = getFrame();
        if (frame == null) {
            SwingUtilities.updateComponentTreeUI(this);
        } else {
            SwingUtilities.updateComponentTreeUI(frame);
        }

        SwingUtilities.updateComponentTreeUI(popupMenu);
        if (aboutBox != null) {
            SwingUtilities.updateComponentTreeUI(aboutBox);
        }
    }

    /**
     * Sets the current L&F on each demo module
     */
    public void updateLookAndFeel() {
        try {
            UIManager.setLookAndFeel(currentLookAndFeel.className);
            for (JAnts ss : jAntsApplications) {
                ss.updateThisJAntsApplication();
            }
        } catch (Exception ex) {
            System.out.println("Failed loading L&F: " + currentLookAndFeel);
            System.out.println(ex);
        }
    }

    // *******************************************************
    // **************   ToggleButtonToolbar  *****************
    // *******************************************************
    static Insets zeroInsets = new Insets(1,1,1,1);
    protected static class ToggleButtonToolBar extends JToolBar {
        public ToggleButtonToolBar() {
            super();
            setFloatable(true);
            setRollover(true);
            //https://stackoverflow.com/questions/19070324/how-to-remove-replace-the-close-button-from-a-floating-jtoolbar
        }

        JToggleButton addToggleButton(Action a) {
            JToggleButton tb = new JToggleButton(
                (String)a.getValue(Action.NAME),
                (Icon)a.getValue(Action.SMALL_ICON)
            );
            tb.setMargin(zeroInsets);
            tb.setText(null);
            tb.setEnabled(a.isEnabled());
            tb.setToolTipText((String)a.getValue(Action.SHORT_DESCRIPTION));
            tb.setAction(a);
            add(tb);
            return tb;
        }
    }

    // *******************************************************
    // *********  ToolBar Panel / Docking Listener ***********
    // *******************************************************
    static class ToolBarPanel extends JPanel implements ContainerListener {

        public boolean contains(int x, int y) {
            Component c = getParent();
            if (c != null) {
                Rectangle r = c.getBounds();
                return (x >= 0) && (x < r.width) && (y >= 0) && (y < r.height);
            }
            else {
                return super.contains(x,y);
            }
        }

        public void componentAdded(ContainerEvent e) {
            Container c = e.getContainer().getParent();
            if (c != null) {
                c.getParent().validate();
                c.getParent().repaint();
            }
        }

        public void componentRemoved(ContainerEvent e) {
            Container c = e.getContainer().getParent();
            if (c != null) {
                c.getParent().validate();
                c.getParent().repaint();
            }
        }
    }

    // *******************************************************
    // ******************   Runnables  ***********************
    // *******************************************************

    /**
     * Generic jAntsApplication runnable. This is intended to run on the
     * AWT gui event thread so as not to muck things up by doing
     * gui work off the gui thread. Accepts a jAntsApplication and an Object
     * as arguments, which gives subtypes of this class the two
     * "must haves" needed in most runnables for this demo.
     */
    static class jAntsApplicationRunnable implements Runnable {
        protected JAnts jAntsApplication;
        protected Object obj;

        public jAntsApplicationRunnable(JAnts jAntsApplication, Object obj) {
            this.jAntsApplication = jAntsApplication;
            this.obj = obj;
        }

        public void run() {
        }
    }


    // *******************************************************
    // ********************   Actions  ***********************
    // *******************************************************

    public static class SwitchToModuleAction extends AbstractAction {
        JAnts jAntsApplication;
        JAntsModule module;

        public SwitchToModuleAction(JAnts jAntsApplication, JAntsModule module) {
            super(module.getName(), module.getIcon());
            this.jAntsApplication = jAntsApplication;
            this.module = module;
        }

        public void actionPerformed(ActionEvent e) {
            jAntsApplication.setModule(module);
        }
    }

    static class ChangeLookAndFeelAction extends AbstractAction {
        JAnts jAntsApplication;
        LookAndFeelData lafData;
        protected ChangeLookAndFeelAction(JAnts jAntsApplication, LookAndFeelData lafData) {
            super("ChangeTheme");
            this.jAntsApplication = jAntsApplication;
            this.lafData = lafData;
        }

        public void actionPerformed(ActionEvent e) {
            jAntsApplication.setLookAndFeel(lafData);
        }
    }

     class ActivatePopupMenuAction extends AbstractAction {
        JAnts jAntsApplication;
        JPopupMenu popup;
        protected ActivatePopupMenuAction(JAnts jAntsApplication, JPopupMenu popup) {
            super("ActivatePopupMenu");
            this.jAntsApplication = jAntsApplication;
            this.popup = popup;
        }

        public void actionPerformed(ActionEvent e) {
            Dimension invokerSize = getSize();
            Dimension popupSize = popup.getPreferredSize();
            popup.show(jAntsApplication, (invokerSize.width - popupSize.width) / 2,
                       (invokerSize.height - popupSize.height) / 2);
        }
    }

    static class ChangeThemeAction extends AbstractAction {
        JAnts jAntsApplication;
        MetalTheme theme;
        protected ChangeThemeAction(JAnts jAntsApplication, MetalTheme theme) {
            super("ChangeTheme");
            this.jAntsApplication = jAntsApplication;
            this.theme = theme;
        }

        public void actionPerformed(ActionEvent e) {
            MetalLookAndFeel.setCurrentTheme(theme);
            jAntsApplication.updateLookAndFeel();
        }
    }

    static class ExitAction extends AbstractAction {
        JAnts jAntsApplication;
        protected ExitAction(JAnts jAntsApplication) {
            super("ExitAction");
            this.jAntsApplication = jAntsApplication;
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class AboutAction extends AbstractAction {
        JAnts jAntsApplication;
        protected AboutAction(JAnts jAntsApplication) {
            super("AboutAction");
            this.jAntsApplication = jAntsApplication;
        }

        public void actionPerformed(ActionEvent e) {
            if(aboutBox == null) {
                aboutBox = new AboutDialog(jAntsApplication);
             }
            aboutBox.pack();
            aboutBox.setLocationRelativeTo(getFrame());
            aboutBox.setVisible(true);
        }
    }

    static class MultiScreenAction extends AbstractAction {
        static final int ALL_SCREENS = -1;
        int screen;
        protected MultiScreenAction(JAnts jAntsApplication, int screen) {
            super("MultiScreenAction");
            this.screen = screen;
        }

        public void actionPerformed(ActionEvent e) {
            GraphicsDevice[] gds = GraphicsEnvironment.
                                   getLocalGraphicsEnvironment().
                                   getScreenDevices();
            if (screen == ALL_SCREENS) {
                for (int i = 0; i < gds.length; i++) {
                    JAnts jAntsApplication = new JAnts(
                                  gds[i].getDefaultConfiguration());
                }
            }
            else {
                JAnts jAntsApplication = new JAnts(
                             gds[screen].getDefaultConfiguration());
            }
        }
    }

    // *******************************************************
    // **********************  Misc  *************************
    // *******************************************************

    static class ModuleLoadThread extends Thread {
        JAnts jAntsApplication;

        ModuleLoadThread(JAnts jAntsApplication) {
            this.jAntsApplication = jAntsApplication;
        }

        public void run() {
            SwingUtilities.invokeLater(jAntsApplication::loadModules);
        }
    }

    private static LookAndFeelData[] getInstalledLookAndFeelData() {
        return Arrays.stream(UIManager.getInstalledLookAndFeels())
                .map(laf -> getLookAndFeelData(laf))
                .toArray(LookAndFeelData[]::new);
    }

    private static LookAndFeelData getLookAndFeelData(
            UIManager.LookAndFeelInfo info) {
        switch (info.getName()) {
            case "Metal":
                return new LookAndFeelData(info, "java");
            case "Nimbus":
                return new LookAndFeelData(info, "nimbus");
            case "Windows":
                return new LookAndFeelData(info, "windows");
            case "GTK+":
                return new LookAndFeelData(info, "gtk");
            case "CDE/Motif":
                return new LookAndFeelData(info, "motif");
            case "Mac OS X":
                return new LookAndFeelData(info, "mac");
            default:
                return new LookAndFeelData(info);
        }
    }

    private static class LookAndFeelData {
        String name;
        String className;
        String label;
        char mnemonic;
        String accDescription;

        public LookAndFeelData(UIManager.LookAndFeelInfo info) {
            this(info.getName(), info.getClassName(), info.getName(),
                 info.getName(), info.getName());
        }

        public LookAndFeelData(UIManager.LookAndFeelInfo info, String property) {
            this(info.getName(), info.getClassName(),
                    getString(String.format("LafMenu.%s_label", property)),
                    getString(String.format("LafMenu.%s_mnemonic", property)),
                    getString(String.format("LafMenu.%s_accessible_description",
                                    property)));
        }

        public LookAndFeelData(String name, String className, String label,
                               String mnemonic, String accDescription) {
            this.name = name;
            this.className = className;
            this.label = label;
            this.mnemonic = mnemonic.charAt(0);
            this.accDescription = accDescription;
        }

        @Override
        public String toString() {
            return className;
        }
    }
}
