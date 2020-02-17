package com.maya.puc.common;


/* $Id: Connection.java,v 1.13 2003/10/23 07:52:54 jeffreyn Exp $ */

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;


public class Connection {
    private static int next_id = 0;
    private int conn_id = -1;
    private Vector listeners = new Vector();
    private boolean connected = false;
    private ReceivingThread in = null;
    private MonitorThread monitor = null;
    private Socket sock = null;
    private OutputStream out = null;
    private boolean reconnect = false;

    private int connPort = -1;
    private InetAddress connIP = null;

    /**
     * Creates a new connection to the PUC device server at the specified
     * IP address and port.  After the connection, use the connect()
     * and disconnect() functions to make or break the actual connection,
     * and use isConnected() to determine whether the connection is actually
     * connected.  To send a message over the connection, use send(), and
     * to receive messages, register one or more classes that implement
     * ConnectionListener using addConnectionListener().
     *
     * @param _connIP the IP address of the PUC device server to connect to.
     * @param _connPort the port of the PUC device server.
     */

    public Connection(InetAddress _connIP, int _connPort) {
        connIP = _connIP;
        connPort = _connPort;

        conn_id = next_id++;

        try {
            monitor = new MonitorThread(this);
            monitor.start();
        } catch (IOException e) {
            System.out.println("IOException when starting MonitorThread: " + e);
        }
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
     * Determines whether a connection has been established.
     *
     * @return true if a connection is active, false otherwise
     */

    public boolean isConnected() {
        return connected;
    }

    /**
     * Attempts to establish a connection.  isConnected() should
     * be called immediately afterward to verify success.
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
            connected = true;
        } catch (UnknownHostException e) {
        } catch (SocketException e) {
        } catch (IOException e) {
        }
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

        try {
            synchronized (in) {
                in.stop();
            }
            sock.close();
        } catch (IOException e) {
            System.err.println("IOException in Connection.disconnect(): " + e);
        }

        connected = false;
    }

    /**
     * Stop all threads
     */
    
    public void stop() {
	if (connected) disconnect();
	try {
	    synchronized (monitor) {
		monitor.stop();
	    }
	}
	catch(Exception e) {
	    System.err.println("Exception in Connection.stop(): " + e);
	}
    }

    public static int lsbByteArrayToInt(byte[] array) {
        int output = -1;

        if (array == null)
            return output;

        if (array.length > 0)
            output = 0;
        output |= (array[0]) & (long) 0xFF;
        if (array.length > 1)
            output |= (array[1] << 8) & (long) 0xFF00;
        if (array.length > 2) {
            output |= (array[2] << 16) & (long) 0xFF0000;
            output |= (array[3] << 24) & (long) 0xFF000000;
        }
        return output;
    }

    public static byte[] intToLsbByteArray(int num, int len) {
        byte[] array = null;

        if ((len == 4) || (len == 2) || (len == 1)) {
            array = new byte[len];
            int idx = 0;

            array[0] = (byte) (num & (long) 0xFF);
            if (len == 1) return array;
            array[1] = (byte) ((num & (long) 0xFF00) >> 8);
            if (len == 2) return array;
            array[2] = (byte) ((num & (long) 0xFF0000) >> 16);
            array[3] = (byte) ((num & (long) 0xFF000000) >> 24);
            return array;
        } else {
            return array;
        }
    }

    /**
     * Returns the ID number of the connection.
     *
     * @ returns the ID number of the connection.
     */

    public int getConnectionID() {
        return conn_id;
    }

    /**
     * Sends a message to the stereo.
     *
     * @param _msg The message to send to the stereo.
     */

    public void send(Message _msg) throws IOException {
        synchronized (this) {
            try {
		int len, xmllen;
                XMLOutputter xmlgen = new XMLOutputter();
                byte[] msgbytes = xmlgen.outputString(_msg.getDocument()).getBytes();
		xmllen = len = msgbytes.length;

		// add binary data, if appropriate
		if ( _msg.hasBinaryData() ) {

		    len += _msg.getBinaryDataLength();
		    byte[] buf = new byte[ len ];
		    System.arraycopy( msgbytes, 0, buf, 0, xmllen );
		    
		    int bytesRead = 0;
		    while( true ) {

			bytesRead += _msg.getBinaryData().read( buf, xmllen + bytesRead, _msg.getBinaryDataLength()-bytesRead );
			if ( bytesRead == _msg.getBinaryDataLength() )
			    break;
		    }

		    msgbytes = buf;
		}

                out.write(intToLsbByteArray(len, 4));
		out.write(intToLsbByteArray(xmllen, 4));
                out.write(msgbytes);
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
//			out.write(_msg.serialization());
        }
    }

    /**
     * Registers a ConnectionListener to receive notifications whenever
     * the connection gets a new message.
     *
     * @param l - The listener to be registered.
     */

    public void addConnectionListener(ConnectionListener l) {
        listeners.addElement(l);
    }

    /**
     * Removes a ConnectionListener from the list of listeners to be
     * notified of connection events.
     *
     * @param l - The listener to be removed.
     */

    public void removeConnectionListener(ConnectionListener l) {
        listeners.removeElement(l);
    }

    /**
     * Sends a ConnectionEvent to all registered listeners.
     *
     * @param e - The ConnectionEvent to send out.
     */

    public void dispatchConnectionEvent(ConnectionEvent e) {
        ConnectionListener l;

        for (int i = 0; i < listeners.size(); i++) {
            l = (ConnectionListener) listeners.elementAt(i);

            if (e instanceof ConnectionEvent.MessageReceived)
                l.messageReceived((ConnectionEvent.MessageReceived) e);
	    else if (e instanceof ConnectionEvent.ConnectionLost)
		l.connectionLost((ConnectionEvent.ConnectionLost) e);
	    else if (e instanceof ConnectionEvent.ConnectionRegained)
		l.connectionRegained((ConnectionEvent.ConnectionRegained) e);
        }
    }

    class MonitorThread extends Thread {
        private Connection conn = null;

        /**
         * Creates a MonitorThread.
         */

        public MonitorThread(Connection _conn) throws IOException {
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
			conn.dispatchConnectionEvent(new ConnectionEvent.ConnectionRegained(conn));
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
        // private byte[] data = new byte[65535];
        protected Vector messages = new Vector();
        private Connection conn = null;
        SAXBuilder saxbuild = null;

        /**
         * Creates a ReceivingThread from a TCP socket.
         *
         * @param _sock The TCP socket from which to read the messages.
         */

        public ReceivingThread(Socket _sock, Connection _conn) throws IOException {
            in = _sock.getInputStream();
            conn = _conn;

            try {
                saxbuild = new SAXBuilder();
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }

        }

        public void run() {
            Document doc = null;
            com.maya.puc.common.Message msg = null;
            byte[] buf = new byte[65535];
            byte[] num = new byte[4];
            int bytesRead = 0;
            int len = 0;
	    int xmllen = 0;
            while (true) {
                if (conn.reconnectRequested()) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    try {
			int ret = 0;

			// STEP #1: Read length value
			
			len = 0;
			while( true ) {

			    len += ret = in.read( num, len, 4-len );
			    
			    if ( ret < 0 )
				throw new IOException( "Socket is dead." );

			    if ( len == 4 ) {
				
				len = lsbByteArrayToInt(num);
				break;
			    }
			}

			// STEP #2: Read XML length value

			xmllen = 0;
			while( true ) {

			    xmllen += ret = in.read( num, xmllen, 4-xmllen );

			    if ( ret < 0 )
				throw new IOException( "Socket is dead." );

			    if ( xmllen == 4 ) {
				
				xmllen = lsbByteArrayToInt(num);
				
				if ( xmllen > len )
				    throw new IOException( "XML length cannot exceed message length." );
				break;
			    }
			}

			// STEP #3: Read Message Data

			doc = null;
			bytesRead = 0;
			while( true ) {
			    bytesRead += ret = in.read(buf, bytesRead, len - bytesRead);

			    if ( ret < 0 )
				throw new IOException( "Socket is dead." );

			    if (bytesRead == len) {
				// get xml data into usable form
				String result = new String(buf, 0, xmllen);
				try {
				    doc = saxbuild.build(new StringReader(result));
				} catch (Exception e) {
				    System.out.println(e);
				}

				// get binary data into usable form
				ByteArrayInputStream binData = null;
				if ( len > xmllen )
				    binData = new ByteArrayInputStream( buf, xmllen, len-xmllen );

				msg = com.maya.puc.common.Message.decode(doc,binData,(binData == null?-1:len-xmllen));
				if (msg.isValid())
				    conn.dispatchConnectionEvent(new ConnectionEvent.MessageReceived(conn, msg));

				break;
			    }
			}
		    } catch (Exception e) {
                        System.out.println("This connection has experienced an error.");
                        e.printStackTrace();
                        conn.requestReconnect();
			conn.dispatchConnectionEvent(new ConnectionEvent.ConnectionLost(conn));
                    }
                }
            }
        }
    }
}
