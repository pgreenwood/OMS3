/*
 * JPanelButton.java
 *
 * Created on November 19, 2004, 9:48 AM
 */
package ngmfconsole;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Panel Button
 *
 * @author  Olaf David
 */
public class JPanelButton extends JToggleButton {

    private JPanel windowPanel;
    private Container window;

    public JPanelButton(Action a) {
        super(a);
    }

    public JPanelButton() {
        this("", null);
    }

    public JPanelButton(Icon icon) {
        this("", icon);
    }

    public JPanelButton(String text, Icon icon) {
        super(text, icon, false);
    }

    @Override
    public void setText(String text) {
        String t = text == null ? null : text + "  ";
        super.setText(t);
    }

    public void setWindowPanel(JPanel windowPanel) {
        this.windowPanel = windowPanel;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color c = g.getColor();
//        g.setColor(isEnabled() ? Color.GRAY : Color.LIGHT_GRAY);
        g.setColor(isEnabled() ? Color.BLACK : Color.GRAY);
        Insets i = getInsets();
//        int opSize = getFontMetrics(getFont()).stringWidth("M") / 2;
        int opSize = 2;
        int newx = getWidth() - i.right - 2 * opSize;
        int newy = (int) (getHeight() * 0.75);
        g.translate(newx, newy);
        g.drawLine(-opSize, 0, opSize, 0);
        if (!isSelected()) {
            g.drawLine(0, -opSize, 0, opSize);
        }
        g.translate(-newx, -newy);
        g.setColor(c);
    }

    @Override
    protected void fireActionPerformed(ActionEvent event) {
        Container w = getWindow();
        if (isSelected()) {
            adjustWindow(w);
            w.setVisible(true);
            w.requestFocus();
        } else {
            w.setVisible(false);
        }
    }

    private void adjustWindow(Container w) {
        Point p = new Point();
        SwingUtilities.convertPointToScreen(p, this);
        w.setLocation(p.x, p.y + getHeight());
    }

    private synchronized Container getWindow() {
        if (window == null) {
            window = createWindow();
        }
        return window;
    }

    private Container createWindow() {
        Component root = SwingUtilities.getRoot(this);
        final JDialog win = new JDialog((Window) root);
        win.setUndecorated(true);
        ComponentListener cl = new ComponentAdapter() {

            @Override
            public void componentMoved(ComponentEvent e) {
                if (isSelected()) {
                    adjustWindow(win);
                }
            }
        };
        root.addComponentListener(cl);
        win.getContentPane().add(windowPanel);
        win.setFocusable(true);
        win.pack();
        return win;
    }

    public static Component panelButton(PropertyChangeListener l) {
        JPanelButton t = new JPanelButton();
        t.setFocusPainted(false);
        ImageIcon runIcon = new ImageIcon(JPanelButton.class.getResource(
//                "/ngmfconsole/resources/settings-icon.png"));
                "/ngmfconsole/resources/oxygen/Categories-preferences-system-icon.png"));
        t.setIcon(runIcon);
        t.setWindowPanel(new Preferences(l));
        t.setBorderPainted(false);
        t.setRolloverEnabled(true);
        t.setToolTipText("Options and Settings.");
        return t;
    }


    public static void main(String args[]) throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JToolBar toolbar = new JToolBar();
        toolbar.add(panelButton(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(evt.getPropertyName() + " " + evt.getNewValue());
            }
        }));
        
        JFrame f = new JFrame();
//        toolbar.add(panelButton1(true, new BeanNode(new Bean())));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(toolbar, BorderLayout.NORTH);
        f.getContentPane().add(new JLabel("test"), BorderLayout.CENTER);
        f.setSize(500, 500);
        f.setLocation(200, 300);
        f.setVisible(true);
    }
}
