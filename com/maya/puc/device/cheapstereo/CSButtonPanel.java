package com.maya.puc.device.cheapstereo;

/*
 * $Id: CSButtonPanel.java,v 1.2 2002/03/29 00:28:51 maya Exp $
 */

import com.maya.puc.common.FauxEform;

import java.awt.*;
import java.awt.event.MouseListener;
import java.net.URL;

/**
 * An AWT panel representing the remote control buttons
 *
 * @author Joseph Hughes
 * @version $Id: CSButtonPanel.java,v 1.2 2002/03/29 00:28:51 maya Exp $
 */

public class CSButtonPanel extends Panel implements MouseListener {
    Dimension prefSize = new Dimension(50, 50);
    Image buffer = null;
    Image buttonImage = null;

    private String buttonsImageName = "remote.jpg";

    private int colorBackgroundR = 0;
    private int colorBackgroundG = 0;
    private int colorBackgroundB = 0;
    private Color colorBackground = null;

    private boolean connected = false;
    byte buttonCode[] = {
        CSMessage.RemoteCommand.CMD_REPEAT,
        CSMessage.RemoteCommand.CMD_SLEEP,
        CSMessage.RemoteCommand.CMD_MEMORY,
        CSMessage.RemoteCommand.CMD_TIMER,
        CSMessage.RemoteCommand.CMD_BAND,
        CSMessage.RemoteCommand.CMD_TUNE_DOWN,
        CSMessage.RemoteCommand.CMD_TUNE_UP,
        CSMessage.RemoteCommand.CMD_X_BASS,
        CSMessage.RemoteCommand.CMD_FUNCTION,
        CSMessage.RemoteCommand.CMD_WIDE,
        CSMessage.RemoteCommand.CMD_EJECT,
        CSMessage.RemoteCommand.CMD_DISC,
        CSMessage.RemoteCommand.CMD_PREV,
        CSMessage.RemoteCommand.CMD_NEXT,
        CSMessage.RemoteCommand.CMD_VOL_UP,
        CSMessage.RemoteCommand.CMD_VOL_DOWN,
        CSMessage.RemoteCommand.CMD_STOP,
        CSMessage.RemoteCommand.CMD_PLAY,
        CSMessage.RemoteCommand.CMD_POWER
    };

    Rectangle buttonArea[] = {
        new Rectangle(34, 46, 26, 20), // repeat
        new Rectangle(34, 66, 26, 17), // sleep
        new Rectangle(34, 82, 31, 21), // memory
        new Rectangle(73, 63, 26, 21), // timer
        new Rectangle(73, 85, 26, 20), // band
        new Rectangle(35, 105, 32, 39), // random/tuning down
        new Rectangle(67, 105, 32, 39), // tuning up
        new Rectangle(22, 154, 27, 32), // x-bass
        new Rectangle(49, 152, 35, 22), // function
        new Rectangle(83, 154, 30, 30), // wide
        new Rectangle(49, 182, 36, 19), // eject
        new Rectangle(49, 203, 36, 18), // disc skip
        new Rectangle(16, 187, 30, 28), // previous
        new Rectangle(16, 215, 31, 24), // next
        new Rectangle(88, 187, 26, 27), // vol up
        new Rectangle(85, 213, 28, 25), // vol down
        new Rectangle(47, 235, 19, 28), // stop
        new Rectangle(68, 234, 19, 31), // play
        new Rectangle(58, 266, 53, 34) // power
    };

    CSButtonListener buttonListener = null;

    public CSButtonPanel(CSButtonListener _buttonListener) {
        buttonListener = _buttonListener;
        this.addMouseListener(this);

        this.setSkin(new FauxEform());
    }

    public Dimension getPreferredSize() {
        return prefSize;
    }

    public boolean isFocusTraversable() {
        return true;
    }

    public void setConnected(boolean _connected) {
        connected = _connected;
        render();
        repaint();
    }

    public void paint(Graphics g) {
        if (buffer == null) {
            buffer = createImage(prefSize.width, prefSize.height);
            render();
        }
        g.drawImage(buffer, 0, 0, this);

    }

    public void update(Graphics g) {
        paint(g);
    }

    public void render() {
        if (buffer == null)
            return;

        Graphics g = buffer.getGraphics();
        g.drawImage(buttonImage, 0, 0, this);
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
        Rectangle rect;
        Point pt = e.getPoint();
        for (int i = 0; i < buttonArea.length; i++) {
            if (buttonArea[i].contains(pt)) {
                buttonListener.buttonPressed(buttonCode[i]);
                return;
            }
        }
    }

    public void mousePressed(java.awt.event.MouseEvent arg1) {
    }

    public void mouseExited(java.awt.event.MouseEvent arg1) {
    }

    public void mouseEntered(java.awt.event.MouseEvent arg1) {
    }

    public void mouseClicked(java.awt.event.MouseEvent arg1) {
    }

    public void setSkin(FauxEform skin) {
        buttonsImageName = skin.getStringAttr("ButtonPanelButtonsImageName", buttonsImageName);

        colorBackgroundR = skin.getIntAttr("ButtonPanelColorBackgroundR", colorBackgroundR);
        colorBackgroundG = skin.getIntAttr("ButtonPanelColorBackgroundG", colorBackgroundG);
        colorBackgroundB = skin.getIntAttr("ButtonPanelColorBackgroundB", colorBackgroundB);
        colorBackground = new Color(colorBackgroundR, colorBackgroundG, colorBackgroundB);

        this.setBackground(colorBackground);

        URL url;
        Toolkit tk = Toolkit.getDefaultToolkit();
        try {
            MediaTracker mt = new MediaTracker(this);
            int id = 0;
            url = getClass().getResource(buttonsImageName);
            buttonImage = tk.getImage(url);
            mt.addImage(buttonImage, 0);
            mt.waitForAll();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        prefSize = new Dimension(buttonImage.getWidth(this), buttonImage.getHeight(this));

        requestFocus();
        render();
        repaint();
    }
}