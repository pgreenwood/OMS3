/*
 * Console.java
 *
 * Created on Aug 9, 2010, 9:13:39 AM
 */
package ngmfconsole;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/** Console
 *
 * @author od
 */
public class Console extends JFrame {

    static final String PREF_OPEN_FILES = "console.file.open";
    static final String PREF_ACTIVE_FILE = "console.file.active";
    //
    static final String PREF_JAVA_OPTIONS = "oms.java.options";
    static final String PREF_WORK_DIR = "oms.java.workingdir";
    static final String PREF_JAVA_HOME = "oms.java.home";
    static final String PREF_OMS_VERSION = "oms.version";
    //
    static final String SIMULATION_DIR = "simulation";
    //
    static final String OMS_DIR = ".oms";
    static final String PROJECT_PROPERTIES = "project.properties";
    //
    static final File dotoms3 = new File(System.getProperty("user.home")
            + File.separatorChar + OMS_DIR);
    //
    static final File oms3Home = new File(dotoms3, Utils.getOMSAppVersion());
    static final File prefsFile = new File(dotoms3, PROJECT_PROPERTIES);
    //
    static final String[] jars = {
        "oms-all.jar",
        "groovy-all-1.7.10.jar",
        "jfreechart-1.0.12.jar",
        "jcommon-1.0.15.jar",
        "cpptasks-1.0b6-od.jar"
    };

    static File getOMSHome(String version) {
        return new File(dotoms3, version);
    }
    //
    static final FileFilter oms3FileFilter = new FileFilter() {

        @Override
        public boolean accept(File f) {
            String n = f.getName();
            return n.endsWith(".sim")
                    || n.endsWith(".esp")
                    || n.endsWith(".fast")
                    || n.endsWith(".luca")
                    || n.endsWith(".test")
                    || n.endsWith(".groovy")
                    || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "OMS Files (*.sim, *.esp, *.luca, *.fast)";
        }
    };

    class MyComboBoxRenderer extends BasicComboBoxRenderer {

        ListCellRenderer delegate;

        public MyComboBoxRenderer(ListCellRenderer delegate) {
            this.delegate = delegate;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected && (-1 < index)) {
                list.setToolTipText(((File) list.getModel().getElementAt(index)).getPath());
            }
            return delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
    //
    JFileChooser workChooser = new JFileChooser();
    JFileChooser openChooser = new JFileChooser();
    //
    TabMenu menu;
    boolean installing = false;
    boolean clearOutput = true;

    static class FileDisplayer extends File {

        public FileDisplayer(File f) {
            super(f.toString());
        }

        @Override
        public String toString() {
            return super.getName();
        }
    }

    Console() {
        initComponents();
        setupComponents();
    }

    private void setupComponents() {
        setTitle(title());
        setIconImage(new ImageIcon(getClass().getResource("/ngmfconsole/resources/objects-32.png")).getImage());

        workCombo.setRenderer(new MyComboBoxRenderer(workCombo.getRenderer()));

        jToolBar1.addSeparator();
        optionsToolbar.add(JPanelButton.panelButton(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String n = evt.getPropertyName();
                if (n.equals(Preferences.SHOW_HIDDEN)) {
                    openChooser.setFileHidingEnabled(!(Boolean) evt.getNewValue());
                    workChooser.setFileHidingEnabled(!(Boolean) evt.getNewValue());
                } else if (n.equals(Preferences.CLEAR_OUT)) {
                    clearOutput = (Boolean) evt.getNewValue();
                }
            }
        }));


        exitMI.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Console.this.dispatchEvent(
                        new WindowEvent(Console.this, WindowEvent.WINDOW_CLOSING));
            }
        });

        openChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        openChooser.setMultiSelectionEnabled(true);
        openChooser.addChoosableFileFilter(oms3FileFilter);

        workChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        workChooser.setMultiSelectionEnabled(false);
        workChooser.setApproveButtonText("Select");
        workChooser.setDialogTitle("Project Directory");

        // last session restore

        Properties prefs = new Properties();
        if (prefsFile.exists()) {
            try {
                FileReader r = new FileReader(prefsFile);
                prefs.load(r);
                r.close();
            } catch (IOException ex) {
                Main.logger.log(Level.SEVERE, "Error", ex);
            }
        }

        enableProjectControls(false);


        workCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                try {
                    String wd = ((File) e.getItem()).getPath();
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        saveProjectConf(wd);
                        closeAll();
                    } else {
                        loadProjectConf(wd);
                        File simDir = new File(wd, SIMULATION_DIR);
                        openChooser.setCurrentDirectory(simDir.exists() ? simDir : new File(wd));
                        enableProjectControls(true);
                    }
                } catch (IOException ex) {
                    Main.logger.log(Level.SEVERE, "Error", ex);
                    ex.printStackTrace(System.err);
                }
            }
        });
        workCombo.requestFocus();

        tabs.addMouseListener(new PopupListener(tabs));
        tabs.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabs.getSelectedIndex() > -1) {
                    setTitle(tabs.getTitleAt(tabs.getSelectedIndex()) + " - " + title());
                } else {
                    setTitle(title());
                }
            }
        });
        menu = new TabMenu(tabs);

        // some checks
        adjustForNB();
        checkButtons();
        clearIndicator();

        String wd = prefs.getProperty(PREF_OPEN_FILES);
        if (wd != null && !wd.isEmpty()) {
            for (String w1 : wd.split("\\s*;\\s*")) {
                loadProject(new File(w1));
            }
            String active = prefs.getProperty(PREF_ACTIVE_FILE);
            if (active != null && !active.isEmpty()) {
                activateProject(active);
            }
        }

        new Thread() {

            @Override
            public void run() {
                File installDir = oms3Home;
                try {
                    if (!needToInstall(installDir)) {
                        statusMessageLabel.setText("Runtime found in '" + installDir + "'");
                        return;
                    }
                    setInstalling(true);
                    setDownloadingIndicator();
                    installDir.mkdirs();
                    for (int i = 0; i < jars.length; i++) {
                        statusMessageLabel.setText("Installing (" + (i + 1) + "/" + jars.length + ")  " + new File(installDir, jars[i]) + " ... ");
                        Utils.download(new File(Utils.downloadDir(), jars[i]), new File(installDir, jars[i]));
                    }
                    statusMessageLabel.setText("Runtime (re)installed in '" + installDir + "'");
                } catch (IOException ex) {
                    Main.logger.log(Level.SEVERE, "Error", ex);
                    ex.printStackTrace(System.err);
                } finally {
                    setInstalling(false);
                    clearIndicator();
                }
            }
        }.start();
    }

    boolean getClearOutput() {
        return clearOutput;
    }

    boolean needToInstall(File installDir) throws MalformedURLException {
        File downloadDir = Utils.downloadDir();
        for (int i = 0; i < jars.length; i++) {
            File d = new File(downloadDir,jars[i]);
            File f = new File(installDir, jars[i]);
            if (!f.exists() || f.length() == 0 || !f.canRead() || 
                    d.length() != f.length() ||
                    d.lastModified() > f.lastModified()) {  // newer file same version, need to install
                return true; 
            }
        }
        return false;
    }

    private void enableProjectControls(boolean b) {
        workCombo.setEnabled(b);
        newButton.setEnabled(b);
        openButton.setEnabled(b);
        saveAllButton.setEnabled(b);
        closeProject.setEnabled(b);
    }

    void savePrefs() {
        Properties prefs = new Properties();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < workCombo.getItemCount(); i++) {
            String dir = ((File) workCombo.getItemAt(i)).getPath();
            b.append(dir.replace('\\', '/'));
            if (i < workCombo.getItemCount() - 1) {
                b.append(";");
            }
        }
        prefs.setProperty(PREF_OPEN_FILES, b.toString());
        prefs.setProperty(PREF_ACTIVE_FILE, Integer.toString(workCombo.getSelectedIndex()));
        try {
            FileWriter w = new FileWriter(prefsFile);
            prefs.store(w, "OMS Preferences.");
            w.close();
        } catch (IOException ex) {
            Main.logger.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace(System.err);
        }
    }

    private void adjustForNB() {
        File nbDir = new File(System.getProperty("user.home") + File.separatorChar + ".netbeans");
        if (nbDir.exists()) {
            File[] files = nbDir.listFiles();
            if (files != null && files.length > 0) {
                Arrays.sort(files, new Comparator<File>() {

                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                File lastNB = files[files.length - 1];
                if (lastNB.exists()) {
                    File bp = new File(lastNB, "build.properties");
                    try {
                        Properties p = new Properties();
                        p.load(new FileReader(bp));
                        if (p.getProperty("oms.ignore") == null) {
                            p.setProperty("oms.version", Utils.getOMSAppVersion());
                            FileWriter w = new FileWriter(bp);
                            p.store(w, "");
                            w.close();
                        }
                    } catch (IOException E) {
                        return;
                    }
                }
                //System.out.println("File " + files[files.length - 1].getAbsolutePath());
            }
        }
    }

    void setDownloadingIndicator() {
        statusIndicator.setIcon(
                new ImageIcon(getClass().getResource("/ngmfconsole/resources/ajax-loader.gif")));
    }

    void clearIndicator() {
        statusIndicator.setIcon(null);
    }

    synchronized boolean isInstalling() {
        return installing;
    }

    synchronized void setInstalling(boolean installing) {
        this.installing = installing;
    }

    void loadFile(File file) {
        SimPanel p = new SimPanel(this);
        p.loadFile(file);
        tabs.addTab(file.getName(), p);
        tabs.setToolTipTextAt(tabs.getTabCount() - 1, file.toString());
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
        statusMessageLabel.setText("Loaded: " + file);
        checkButtons();
    }

    void checkButtons() {
        saveAllButton.setEnabled(tabs.getTabCount() > 0);
    }

    File getOpenChooser() {
        return openChooser.getCurrentDirectory();
    }

    String getWork() {
        return ((File) workCombo.getSelectedItem()).getPath();
    }

    String title() {
        return "Console (OMS " + Utils.getOMSAppVersion() + ")";
    }

    void toggleTitle(SimPanel p) {
        int index = tabs.indexOfComponent(p);
        String s = SimPanel.newFile.getName();
        if (p.getFile() != null) {
            s = p.getFile().getName();
        }
        tabs.setTitleAt(index, s + (p.isModified() ? "*" : " "));
    }

    void saveProjectConf(String dir) throws IOException {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            SimPanel t = (SimPanel) tabs.getComponentAt(i);
            if (t.getFile() != null) {
                if (b.length() != 0) {
                    b.append(";");
                }
                b.append(t.getFile().toString().substring(dir.length() + 1).replace('\\', '/'));
            }
        }

        int activeFile = tabs.getSelectedIndex() < 0 ? 0 : tabs.getSelectedIndex();

        File conf = new File(dir + File.separatorChar + OMS_DIR, PROJECT_PROPERTIES);
        
        String f = "";
        if (conf.exists()) {
            f = readFile(conf.toString());
        }

        if (f.indexOf(PREF_OPEN_FILES) > -1) {
            f = f.replaceFirst(PREF_OPEN_FILES + "\\s*=(.)*\n", PREF_OPEN_FILES + "=" + b.toString() + "\n");
        } else {
            f = f + "\n\n# OMS Console configuration:\n";
            f = f + PREF_OPEN_FILES + "=" + b.toString() + "\n";
        }
        if (f.indexOf(PREF_ACTIVE_FILE) > -1) {
            f = f.replaceFirst(PREF_ACTIVE_FILE + "\\s*=(.)*\n", PREF_ACTIVE_FILE + "=" + activeFile + "\n");
        } else {
            f = f + PREF_ACTIVE_FILE + "=" + activeFile + "\n";
        }
        
        conf.getParentFile().mkdirs();
        PrintWriter fo = new PrintWriter(conf);
        fo.print(f);
        fo.close();
    }

    public static String readFile(String name) {
        StringBuilder b = new StringBuilder();
        try {
            BufferedReader r = new BufferedReader(new FileReader(name));
            String line;
            while ((line = r.readLine()) != null) {
                b.append(line).append('\n');
            }
            r.close();
        } catch (IOException E) {
            throw new RuntimeException(E.getMessage());
        }
        return b.toString();
    }

    void loadProjectConf(String dir) throws IOException {
        Properties p = new Properties();
        File conf = new File(dir + File.separatorChar + OMS_DIR, PROJECT_PROPERTIES);
        if (conf.exists()) {
            FileInputStream fi = new FileInputStream(conf);
            p.load(fi);
            fi.close();
            String s = p.getProperty(PREF_OPEN_FILES);
            if (s == null || s.isEmpty() || s.trim().isEmpty()) {
                return;
            }
            for (String s1 : s.split("\\s*;\\s*")) {
                File f = new File(dir, s1);
                if (f.exists()) {
                    loadFile(f);
                }
            }
            String a = p.getProperty(PREF_ACTIVE_FILE, "0");
            try {
                int idx = Integer.parseInt(a);
                if (idx > -1 && (idx < tabs.getTabCount())) {
                    tabs.setSelectedIndex(idx);
                }
            } catch (NumberFormatException E) {
            }
        }
        if (tabs.getTabCount() == 0) {
            loadFile(SimPanel.newFile);
        }
    }

    void closeAll() {
        tabs.removeAll();
        checkButtons();
    }

    /**
     * 
     */
    class TabMenu extends JPopupMenu implements ActionListener {

        private static final long serialVersionUID = 1L;
        private String saveTabLabel = "Save";
        private String saveAsTabLabel = "Save As...";
        private String closeTabLabel = "Close";
        private String closeAllLabel = "Close All";
        private String closeOtherLabel = "Close Other";
        private JTabbedPane pane;
        private int curTab = -1;

        TabMenu(JTabbedPane pane) {
            this.pane = pane;
            JMenuItem saveTab = new JMenuItem(saveTabLabel);
            saveTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
            JMenuItem saveAsTab = new JMenuItem(saveAsTabLabel);
            JMenuItem closeTab = new JMenuItem(closeTabLabel);
            JMenuItem closeAll = new JMenuItem(closeAllLabel);
            JMenuItem closeOther = new JMenuItem(closeOtherLabel);
            add(saveAsTab);
            addSeparator();
            add(closeTab);
            add(closeAll);
            add(closeOther);
            add(saveTab);
            saveTab.addActionListener(this);
            saveAsTab.addActionListener(this);
            closeTab.addActionListener(this);
            closeAll.addActionListener(this);
            closeOther.addActionListener(this);
        }

        void setCurrentTab(int index) {
            curTab = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(saveTabLabel)) {
                if (curTab > -1) {
                    ((SimPanel) tabs.getComponentAt(curTab)).save(false);
                }
            } else if (e.getActionCommand().equals(saveAsTabLabel)) {
                if (curTab > -1) {
                    ((SimPanel) tabs.getComponentAt(curTab)).save(true);
                }
            } else if (e.getActionCommand().equals(closeTabLabel)) {
                if (curTab > -1) {
                    if (((SimPanel) tabs.getComponentAt(curTab)).checkClose()) {
                        pane.remove(curTab);
                    }
                }
            } else if (e.getActionCommand().equals(closeAllLabel)) {
                pane.removeAll();
            } else if (e.getActionCommand().equals(closeOtherLabel)) {
                if (curTab > -1) {
                    while (tabs.getTabCount() > curTab + 1) {//close tabs after
                        tabs.remove(curTab + 1);
                    }
                    while (tabs.getTabCount() > 1) {//close tabs before
                        tabs.remove(0);
                    }
                }
            }
            checkButtons();
            try {
                saveProjectConf(getWork());
            } catch (IOException ex) {
                Main.logger.log(Level.SEVERE, "Error", ex);
                ex.printStackTrace(System.err);
            }
        }
    }

    /**
     * triggers popup menu with right click on tab
     */
    class PopupListener extends MouseAdapter {

        JTabbedPane pane;

        PopupListener(JTabbedPane pane) {
            this.pane = pane;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            checkForPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            checkForPopup(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            checkForPopup(e);
        }

        private void checkForPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int tabNumber = pane.getUI().tabForCoordinate(pane, e.getX(), e.getY());
                if (tabNumber >= 0) {//must be a tab
                    Component c = e.getComponent();
                    menu.setCurrentTab(tabNumber);//set the selected tab
                    menu.show(c, e.getX(), e.getY());
                }
            }
        }
    }

//    public static void main(String[] args) {
//        String f =
//                PREF_ACTIVE_FILE + "=2\n"
//                + "\n";
//        System.out.println(f);
//        f = f.replaceFirst(PREF_ACTIVE_FILE + "\\s*=(.)+\n", PREF_ACTIVE_FILE + "=" + 5);
//        System.out.println(f);
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabs = new javax.swing.JTabbedPane();
        tools = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        newProject = new javax.swing.JButton();
        closeProject = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        workCombo = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        workDirButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        optionsToolbar = new javax.swing.JToolBar();
        jToolBar1 = new javax.swing.JToolBar();
        newButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveAllButton = new javax.swing.JButton();
        status = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        space = new javax.swing.JLabel();
        statusIndicator = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMI = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        getContentPane().add(tabs, java.awt.BorderLayout.CENTER);

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.add(jSeparator1);

        newProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ngmfconsole/resources/oxygen/Actions-project-development-new-template-icon.png"))); // NOI18N
        newProject.setToolTipText("New project.");
        newProject.setEnabled(false);
        newProject.setFocusable(false);
        newProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(newProject);

        closeProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ngmfconsole/resources/oxygen/Actions-project-development-close-icon.png"))); // NOI18N
        closeProject.setToolTipText("Close project.");
        closeProject.setFocusable(false);
        closeProject.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeProject.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        closeProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeProjectActionPerformed(evt);
            }
        });
        jToolBar2.add(closeProject);

        workCombo.setToolTipText("Switch projects.");
        workCombo.setMaximumSize(new java.awt.Dimension(263, 2000));
        workCombo.setPreferredSize(new java.awt.Dimension(263, 24));

        jLabel1.setText("Project:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addGap(4, 4, 4)
                .addComponent(workCombo, 0, 201, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(workCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jToolBar2.add(jPanel2);

        workDirButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ngmfconsole/resources/oxygen/Actions-document-open-folder-icon.png"))); // NOI18N
        workDirButton.setToolTipText("Open project.");
        workDirButton.setBorderPainted(false);
        workDirButton.setFocusable(false);
        workDirButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        workDirButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        workDirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                workDirButtonActionPerformed(evt);
            }
        });
        jToolBar2.add(workDirButton);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        optionsToolbar.setFloatable(false);
        optionsToolbar.setRollover(true);
        optionsToolbar.setBorderPainted(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 155, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap(39, Short.MAX_VALUE)
                    .addComponent(optionsToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 38, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(optionsToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE))
        );

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ngmfconsole/resources/oxygen/Actions-document-new-icon.png"))); // NOI18N
        newButton.setToolTipText("New simulation");
        newButton.setEnabled(false);
        newButton.setFocusable(false);
        newButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(newButton);

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ngmfconsole/resources/oxygen/Actions-document-open-icon.png"))); // NOI18N
        openButton.setToolTipText("Open simulation.");
        openButton.setEnabled(false);
        openButton.setFocusable(false);
        openButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(openButton);

        saveAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ngmfconsole/resources/saveall-24.png"))); // NOI18N
        saveAllButton.setToolTipText("Save all simulations.");
        saveAllButton.setEnabled(false);
        saveAllButton.setFocusable(false);
        saveAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(saveAllButton);

        javax.swing.GroupLayout toolsLayout = new javax.swing.GroupLayout(tools);
        tools.setLayout(toolsLayout);
        toolsLayout.setHorizontalGroup(
            toolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toolsLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        toolsLayout.setVerticalGroup(
            toolsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getContentPane().add(tools, java.awt.BorderLayout.NORTH);

        status.setLayout(new java.awt.BorderLayout());

        statusMessageLabel.setText("status");
        status.add(statusMessageLabel, java.awt.BorderLayout.CENTER);

        space.setText(" ");
        status.add(space, java.awt.BorderLayout.WEST);

        statusIndicator.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ngmfconsole/resources/ajax-loader.gif"))); // NOI18N
        statusIndicator.setText(" ");
        status.add(statusIndicator, java.awt.BorderLayout.LINE_END);

        getContentPane().add(status, java.awt.BorderLayout.SOUTH);

        fileMenu.setText("File");

        exitMI.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        exitMI.setMnemonic('x');
        exitMI.setText("Exit");
        fileMenu.add(exitMI);

        jMenuBar1.add(fileMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        loadFile(SimPanel.newFile);
}//GEN-LAST:event_newButtonActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        int result = openChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selected = openChooser.getSelectedFiles();
            if (selected.length > 0) {
                for (File file : selected) {
                    loadFile(file);
                }
            } else {
                loadFile(openChooser.getSelectedFile());
            }
            try {
                saveProjectConf(getWork());
            } catch (IOException ex) {
                Main.logger.log(Level.SEVERE, "Error", ex);
                ex.printStackTrace(System.err);
            }
        }
}//GEN-LAST:event_openButtonActionPerformed

    private void saveAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllButtonActionPerformed
        for (int i = 0; i < tabs.getTabCount(); i++) {
            SimPanel t = (SimPanel) tabs.getComponentAt(i);
            if (t.isModified()) {
                t.save(false);
            }
        }
        try {
            saveProjectConf(getWork());
        } catch (IOException ex) {
            Main.logger.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace(System.err);
        }
}//GEN-LAST:event_saveAllButtonActionPerformed
    File currWork = new File(System.getProperty("user.home"));

    void loadProject(File folder) {
        if (!folder.exists()) {
            return;
        }
        workCombo.addItem(new FileDisplayer(folder));
        File simDir = new File(folder, SIMULATION_DIR);
        openChooser.setCurrentDirectory(simDir.exists() ? simDir : folder);
        currWork = folder;
        enableProjectControls(true);
        workCombo.setSelectedIndex(workCombo.getItemCount() - 1);
    }

    void activateProject(String idx) {
        try {
            int i = Integer.parseInt(idx);
            if (i > -1 && i < workCombo.getItemCount()) {
                workCombo.setSelectedIndex(i);
            }
        } catch (NumberFormatException E) {
        }
    }

    private void workDirButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_workDirButtonActionPerformed
        workChooser.setCurrentDirectory(currWork);
        int result = workChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = workChooser.getSelectedFile();
            loadProject(selected);
        }
}//GEN-LAST:event_workDirButtonActionPerformed

    private void closeProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeProjectActionPerformed
        if (workCombo.getItemCount() > 0) {
            String wd = ((File) workCombo.getSelectedItem()).getPath();
            try {
                saveProjectConf(wd);
                closeAll();
                workCombo.removeItemAt(workCombo.getSelectedIndex());
            } catch (IOException ex) {
                Main.logger.log(Level.SEVERE, "Error", ex);
                ex.printStackTrace(System.err);
            }
        }
    }//GEN-LAST:event_closeProjectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeProject;
    private javax.swing.JMenuItem exitMI;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JButton newButton;
    private javax.swing.JButton newProject;
    private javax.swing.JButton openButton;
    private javax.swing.JToolBar optionsToolbar;
    private javax.swing.JButton saveAllButton;
    private javax.swing.JLabel space;
    private javax.swing.JPanel status;
    private javax.swing.JLabel statusIndicator;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JTabbedPane tabs;
    private javax.swing.JPanel tools;
    private javax.swing.JComboBox workCombo;
    private javax.swing.JButton workDirButton;
    // End of variables declaration//GEN-END:variables
}
