package com.maya.puc.device.cheapstereo;


/* $Id: CSConnection.java,v 1.3 2002/04/19 06:27:24 tkharris Exp $ */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;


public class CSConnection {

    // general constants

    /**
     * The port number for standard (file transfer) connections to the stereo.
     */

    public static final int CONN_PORT = 3000;

    private Vector listeners = new Vector();
    private boolean connected = false;
    private ReceivingThread in = null;
    private MonitorThread monitor = null;
    private Socket sock = null;
    private OutputStream out = null;
    private boolean reconnect = false;

    private int connPort = -1;
    private InetAddress connIP = null;


    public CSConnection(InetAddress _connIP, int _connPort) {
        connIP = _connIP;
        connPort = _connPort;
    }

    private void startMonitorThread() {
        try {
            monitor = new MonitorThread(this);
            monitor.start();
        } catch (IOException e) {
            System.out.println("IOException when starting MonitorThread: " + e);
        }
    }

    private void stopMonitorThread() {
        monitor.stop();
    }

    public void requestReconnect() {
        reconnect = true;
    }

    public boolean reconnectRequested() {
        return reconnect;
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

    /**
     * Determines whether an ARQ connection has been established.
     *
     * @return true if a connection is active, false otherwise
     */

    public boolean isConnected() {
        return connected;
    }

    /**
     * Attempts to establish a connection with an ARQ.  isConnected() should
     * be called immediately afterward to verify success.  This function takes
     * care of the initial CONN_REQ/CONN_GA exchange, and the establishment of
     * the TCP connection.
     */

    public void connect() {
        byte[] data = null;

        // sanity check
        if (connected) return;

        try {
            sock = new Socket(connIP, connPort);
            out = sock.getOutputStream();
            in = new ReceivingThread(sock, this);
            in.start();
            out.write((new String("leet")).getBytes());
            connected = true;
        } catch (UnknownHostException e) {
	    System.err.println("Error: Unknown host");
        } catch (SocketException e) {
	    System.err.println("Error: Socket exception");
        } catch (IOException e) {
	    System.err.println("Error: IOException");
        }
        startMonitorThread();
    }

    public Socket getNewSocket() {
        try {
            sock = new Socket(connIP, connPort);
            return sock;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Closes a connection.
     */

    public void disconnect() {
        if (!connected) return;
        connected = false;
        stopMonitorThread();
        try {
            synchronized (in) {
                in.stop();
            }
            sock.close();
        } catch (IOException e) {
            System.err.println("IOException in CSConnection.disconnect(): " + e);
        }

    }

    /**
     * Sends a message to the stereo.
     *
     * @param _msg The message to send to the stereo.
     */

    public void send(CSMessage _msg) throws IOException {
        synchronized (this) {
            out.write(_msg.serialization());
        }
    }

    /**
     * Determines the number of ArqMessages that have been received and
     * awaiting processing.  To retrieve them, call getMessage().
     *
     * @return The number of messages that can be retrieved with calls to
     *         getMessage().
     */

    public int messagesWaiting() {
        int messages = 0;
        synchronized (in.messages) {
            messages = in.messages.size();
        }
        return messages;
    }

    /**
     * Retrieves a message from the incoming message queue.
     *
     * @return The first unprocessed message in the queue, or null if there
     *         are no messages to retrieve.
     */

    public CSMessage getMessage() {
        if (messagesWaiting() <= 0)
            return null;

        CSMessage msg = null;
        synchronized (in.messages) {
            msg = (CSMessage) in.messages.firstElement();
            in.messages.removeElementAt(0);
        }
        return msg;
    }

    /**
     * Registers an ArqConnectionListener to receive notifications whenever
     * the connection gets a new message.
     *
     * @param l - The listener to be registered.
     */

    public void addCSConnectionListener(CSConnectionListener l) {
        listeners.addElement(l);
    }

    /**
     * Removes an ArqConnectionListener from the list of listeners to be
     * notified of connection events.
     *
     * @param l - The listener to be removed.
     */

    public void removeCSConnectionListener(CSConnectionListener l) {
        listeners.removeElement(l);
    }

    /**
     * Sends an ArqConnectionEvent to all registered listeners.
     *
     * @param e - The ArqConnectionEvent to send out.
     */

    public void dispatchCSConnectionEvent(CSConnectionEvent e) {
        CSConnectionListener l;

        for (int i = 0; i < listeners.size(); i++) {
            l = (CSConnectionListener) listeners.elementAt(i);

            if (e instanceof CSConnectionEvent.MessagesWaiting)
                l.messagesWaiting((CSConnectionEvent.MessagesWaiting) e);

            if (e instanceof CSConnectionEvent.Disconnected)
                l.disconnected((CSConnectionEvent.Disconnected) e);
        }
    }


    class MonitorThread extends Thread {
        private CSConnection conn = null;

        /**
         * Creates a MonitorThread.
         */

        public MonitorThread(CSConnection _conn) throws IOException {
            conn = _conn;
        }

        public void run() {
            while (true) {
                if (conn.reconnectRequested()) {
                    System.out.print("Attempting to reconnect...");
                    conn.disconnect();
                    conn.connect();
                    if (conn.isConnected()) {
                        System.out.print("reconnected.");
                        conn.reconnect = false;
                    }
                    System.out.println();
                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Receives, decodes, and enqueues incoming ARQ messages.
     */

    class ReceivingThread extends Thread {
        private InputStream in = null;
        private byte[] data = new byte[65535];
        protected Vector messages = new Vector();
        private CSConnection conn = null;

        /**
         * Creates a ReceivingThread from a TCP socket.
         *
         * @param _sock The TCP socket from which to read the messages.
         */

        public ReceivingThread(Socket _sock, CSConnection _conn) throws IOException {
            in = _sock.getInputStream();
            conn = _conn;
        }

        public void run() {
            int bytes = 0;
            int used = 0;
            int leftover = 0;
            boolean needData = true;
            CSMessage msg;
            while (conn.isConnected()) {
                if (conn.reconnectRequested()) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    try {
                        bytes = 0;
                        if (needData) {
                            synchronized (in) {
                                bytes = in.read(data, leftover, data.length - leftover);
                            }
                        }
                        needData = false;
                        bytes = bytes + leftover;
                        msg = CSMessage.decode(data, 0, bytes);
                        if (!msg.isValid()) {
                            leftover = bytes;
                            needData = true;
                        } else {
                            CSConnectionEvent.MessagesWaiting e;
                            synchronized (messages) {
                                messages.addElement(msg);
                                used = msg.getLength();
                                leftover = bytes - used;
                                if (leftover > 0) {
                                    byte[] data_old = data;
                                    data = new byte[65535];
                                    for (int i = 0; i < leftover; i++)
                                        data[i] = data_old[used + i];
                                } else {
                                    needData = true;
                                }
                                e = new CSConnectionEvent.MessagesWaiting(messages.size());
                            }
                            dispatchCSConnectionEvent(e);
                        }
                    } catch (Exception e) {
                        if (conn.isConnected()) {
						System.err.println("Exception in ReceivingThread.run(): " + e);
                        e.printStackTrace();
                        System.err.println("This connection has experienced an error.");
                        conn.requestReconnect();
                        }
                    }
                }
            }
        }
    }
}
