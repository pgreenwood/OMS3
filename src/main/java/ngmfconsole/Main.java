/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ngmfconsole;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 *
 * @author od
 */
public class Main {

    public static final Logger logger = Logger.getLogger("oms3.console");

    static String java_home() {
        String jh = System.getProperty("jh");
        if (jh != null) {
            return jh;
        }
        jh = System.getenv("JAVA_HOME");
        if (jh != null) {
            return jh;
        }
        return System.getProperty("java.home");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        Handler h = new FileHandler(System.getProperty("user.dir") + File.separatorChar + "log.txt", true);
        h.setFormatter(new SimpleFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(h);

        try {
            logger.info("Start Session");
            logger.setLevel(Level.parse(System.getProperty("loglevel")));
        } catch (Exception E) {
            logger.warning("Not a valid log level in '-Dloglevel=???': '" + System.getProperty("loglevel") + "'");
            logger.setLevel(Level.INFO);
        } finally {
            logger.info("Log level: " + logger.getLevel().toString());
        }

        logger.info("OMS version " + Utils.getOMSAppVersion());
        logger.info("User dir: " + System.getProperty("user.dir"));
        logger.info("OMS home: " + Console.oms3Home);
        logger.info("java.home: " + java_home());

        // adjust LnF
        String osName = System.getProperty("os.name");
        if ((osName != null) && osName.toLowerCase().startsWith("lin")) {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } else {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        try {
            String.class.getMethod("isEmpty", (Class<?>[]) null);
        } catch (Exception E) {
            JOptionPane.showMessageDialog(null,
                    "You are using an older Java version, however JDK 1.6 is needed!\nPlease install the right JDK, start again ...",
                    "Problem...", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        String jh = java_home();
        if (jh == null) {
            JOptionPane.showMessageDialog(null,
                    "You need to install the latest JDK and set 'JAVA_HOME' to your JDK install directory.  "
                    + "\nPlease start again ...",
                    "Problem...", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        if (!new File(jh).exists()) {
            JOptionPane.showMessageDialog(null,
                    "'JAVA_HOME' (" + jh + ") does not exists. Please fix this."
                    + "\nPlease start again ...",
                    "Problem...", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // open window
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                final Console c = new Console();
                c.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                c.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (JOptionPane.showConfirmDialog(c, "Exit ?", "Console", JOptionPane.YES_NO_OPTION)
                                == JOptionPane.YES_OPTION) {
                            c.savePrefs();
                            logger.info("Exit");
                            System.exit(0);
                        }
                    }
                });
                c.setSize(800, 600);
                c.setLocationRelativeTo(null);
                c.setVisible(true);
            }
        });
    }
}
