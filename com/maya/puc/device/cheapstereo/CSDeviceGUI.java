package com.maya.puc.device.cheapstereo;

import com.maya.puc.common.PUCServer;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;

/**
 * A class that implements the functionality of PUCProxy.
 *
 * @author Joseph Hughes
 * @version $Id: CSDeviceGUI.java,v 1.2 2002/03/29 00:28:52 maya Exp $
 */

public class CSDeviceGUI extends Object implements WindowListener,
        ComponentListener, KeyListener, ActionListener, ItemListener {

    public static final String VERSION = "v0.00";

    private Dimension minsize = null;
    private Frame f = null;
    private CSScreenPanel screen = null;
    private CSButtonPanel bp = null;
    private boolean buttonsVisible = false;
    private int buttonsGridX = 0;
    private int buttonsGridY = 1;
    private Container content = null;
    private MenuItem presets = null;
    private CheckboxMenuItem remote = null;
    private MenuItem exit = null;
    private CSDevice dev = null;

    private boolean limitMaxSize = true;

    public CSDeviceGUI(CSDevice dev) {
        this.dev = dev;
        init();
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
        if (e.getActionCommand().equals(presets.getActionCommand())) {
            dev.requestCommandInvoke("LoadPresetValues");
        } else if (e.getActionCommand().equals(exit.getActionCommand())) {
            exit();
        }
    }

    private void setRemoteVisibility(boolean show) {
        if (buttonsVisible == show) return;

        if (!show) {
            System.out.println("Removing Remote");
            content.remove(bp);
        } else {
            GridBagLayout gbl = (GridBagLayout) content.getLayout();
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = buttonsGridX;
            gbc.gridy = buttonsGridY;
            gbl.setConstraints(bp, gbc);
            content.add(bp);
            System.out.println("Adding Remote");
        }
        f.pack();
        minsize = f.getSize();
        enforceFrameSize();
        buttonsVisible = show;
    }

    public void setState(CSMessage.ScreenDump msg) {
        if (screen != null) {
            screen.setState(msg);
        }
    }

    public void init() {
        screen = new CSScreenPanel();
        screen.addKeyListener(this);

        bp = new CSButtonPanel(dev);
        bp.addKeyListener(this);

        // set up us the GUI
        f = new Frame("Audiophase");
        URL url = getClass().getResource("pucproxy.jpg");
        f.addComponentListener(this);
        f.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
        content = new Panel();
        f.add(content);
        f.addKeyListener(this);
        content.addKeyListener(this);
        content.setBackground(Color.black);
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        content.setLayout(gbl);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(screen, gbc);
        content.add(screen);

        gbc.gridx = buttonsGridX;
        gbc.gridy = buttonsGridY;
        gbl.setConstraints(bp, gbc);
        if (buttonsVisible)
            content.add(bp);

        MenuBar mb = new MenuBar();
        Menu options = new Menu("Options");
        presets = new MenuItem("Load Preset Values");
        presets.addActionListener(this);
        options.add(presets);
        remote = new CheckboxMenuItem("Show Remote", buttonsVisible);
        remote.addItemListener(this);
        options.addSeparator();
        options.add(remote);
        options.addSeparator();
        exit = new MenuItem("Hide");
        exit.addActionListener(this);
        options.add(exit);
        mb.add(options);
        f.setMenuBar(mb);

        f.pack();
        minsize = f.getSize();

        // connect the interface event listeners
        f.addWindowListener(this);
        f.addKeyListener(this);
    }

    public void exit() {
        this.setVisibility(false);
        dev.updateStatus();
    }

    public void setVisibility(boolean visible) {
        f.setVisible(visible);
    }

    public boolean isVisible() {
        return f.isVisible();
    }

    public void windowClosing(WindowEvent e) {
        exit();
    };

    public void windowOpened(WindowEvent e) {
    };
    public void windowClosed(WindowEvent e) {
    };
    public void windowIconified(WindowEvent e) {
    };
    public void windowDeiconified(WindowEvent e) {
    };
    public void windowActivated(WindowEvent e) {
    };
    public void windowDeactivated(WindowEvent e) {
    };


    public void componentShown(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        if ((e.getComponent() == f) && (minsize != null)) {
            enforceFrameSize();
        }
    }

    private void enforceFrameSize() {
        Dimension dnew = f.getSize();
        boolean changed = false;

        if (dnew.width < minsize.width) {
            dnew.width = minsize.width;
            changed = true;
        }
        if (dnew.height < minsize.height) {
            dnew.height = minsize.height;
            changed = true;
        }

        if (limitMaxSize) {
            if (dnew.width > minsize.width) {
                dnew.width = minsize.width;
                changed = true;
            }
            if (dnew.height > minsize.height) {
                dnew.height = minsize.height;
                changed = true;
            }
        }


        if (changed) {
            f.setSize(dnew);
        }
    }


    public void keyReleased(KeyEvent e) {
        byte cmd = 0;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_1:
                cmd = 0x30;
                break;
            case KeyEvent.VK_2:
                cmd = 0x31;
                break;
            case KeyEvent.VK_ENTER:
                cmd = 0x3F;
                break;
            case KeyEvent.VK_UP:
                cmd = 0x3C;
                break;
            case KeyEvent.VK_DOWN:
                cmd = 0x3D;
                break;
            case KeyEvent.VK_ESCAPE:
                cmd = 0x40;
                break;
            case KeyEvent.VK_TAB:
                cmd = 0x38;
                break;
            default:
                break;
        }

        try {
            if (cmd != 0) {
                System.out.println("Sending Command " + cmd);
                dev.sendToStereo(new CSMessage.RemoteCommand(cmd));
            }
        } catch (Exception ex) {
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }


    public void itemStateChanged(ItemEvent e) {
        if (e.getItem().equals(remote.getLabel())) {
            setRemoteVisibility(remote.getState());
        }
    }
}
