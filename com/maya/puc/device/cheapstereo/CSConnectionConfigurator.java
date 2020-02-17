package com.maya.puc.device.cheapstereo;

import com.maya.puc.common.FauxEform;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A dialog box that allows the selection of network settings.
 *
 * @author Joseph Hughes
 * @version $Id: CSConnectionConfigurator.java,v 1.2 2002/03/29 00:28:51 maya Exp $
 */

public class CSConnectionConfigurator extends Dialog implements ActionListener {
    private static final String TITLE = "Connect to stereo...";
    public static final double CONFIG_FILE_VERSION = 0.92;
    private String name = "";
    private InetAddress connIP = null;
    private int connPort = CSConnection.CONN_PORT;

    private TextField txtConnIP = null;
    private TextField txtConnPort = null;

    private Button btnOK = null;

    public CSConnectionConfigurator(Frame _parent) {
        super(_parent, TITLE, true);
        init();
    }

    public CSConnectionConfigurator(Frame _parent, String filename) {
        super(_parent, TITLE, true);
        loadSettings(filename);
        init();
    }

    public void init() {
        Panel p = new Panel();
        p.setBackground(Color.lightGray);
        int numrows = 5;

        p.setLayout(new GridLayout(numrows, 1));

        p.add(new Label("IP Address:"));
        txtConnIP = new TextField("");
        if (connIP != null)
            txtConnIP.setText(connIP.getHostAddress());
        p.add(txtConnIP);

        p.add(new Label("Port:"));
        txtConnPort = new TextField("" + connPort);
        p.add(txtConnPort);

        btnOK = new Button("OK");
        btnOK.addActionListener(this);
        p.add("Center", btnOK);

        this.add(p);
        this.pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOK) {
            try {
                if (txtConnIP.getText().length() > 0)
                    connIP = InetAddress.getByName(txtConnIP.getText());
                else
                    connIP = null;

                if (txtConnPort.getText().length() > 0)
                    connPort = Integer.parseInt(txtConnPort.getText());

                this.setVisible(false);
            } catch (Exception ex) {
            }
        }
    }

    private boolean loadSettings(String filename) {
        FauxEform settings = new FauxEform();
        settings.load(filename);
        try {
            connIP = InetAddress.getByName(settings.getStringAttr("ConnectionIP", ""));
        } catch (UnknownHostException e) {
            connIP = null;
        }
        connPort = settings.getIntAttr("ConnectionPort", connPort);

        return true;
    }

    public void saveSettings(String filename) {
        FauxEform settings = new FauxEform();
        settings.setAttr("Version", CONFIG_FILE_VERSION);
        if (connIP != null)
            settings.setAttr("ConnectionIP", connIP.getHostAddress());
        settings.setAttr("ConnectionPort", connPort);
        settings.save(filename);
    }

    public void printSettings() {
        System.out.println("connIP: " + this.getConnIP());
        System.out.println("connPort: " + this.getConnPort());
    }

    /**
     * Data access function.
     *
     * @return The IP address to which the connection attempt will be made.
     *         May be different than the results of getArqIP() if connecting
     *         to the ARQ through a NAT/IP Forwarding router/firewall.
     */

    public InetAddress getConnIP() {
        return connIP;
    }

    /**
     * Data access function.
     *
     * @return The port number to which the connection attempt is made.
     */

    public int getConnPort() {
        return connPort;
    }
}