package com.maya.puc.common;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class PUCServer implements ConnectionListener {

    public final static int SERVER_INFO_PORT = 5149;

    boolean discoveryServiceRunning = false;
    com.maya.puc.common.Connection discoveryConnection = null;

    Vector listeners = new Vector();
    Vector connections = new Vector();
    Vector devices = new Vector();
    Hashtable registeredDevices = new Hashtable();
    private MonitorThread monitor = null;

    public PUCServer() {

	try {
	    startDiscoveryService();
	}
	catch( BindException e ) {

	    // otherwise this PUCProxy will register its active devices
	    // with the existing discovery service

	    try {
		discoveryServiceRunning = false;

		discoveryConnection = new com.maya.puc.common.Connection( InetAddress.getLocalHost(), SERVER_INFO_PORT );
		discoveryConnection.connect();

		discoveryConnection.addConnectionListener( this );

		if ( discoveryConnection.isConnected() )
		    System.out.println( "Connected to discovery service." );
		else
		    System.err.println( "No discovery service available!" );
	    }
	    catch( Exception e2 ) {
		System.err.println( "No discovery service available!" );
		e2.printStackTrace();
	    }
	}
    }

    protected void startDiscoveryService() throws BindException {

	// If this block succeeds, then this PUCProxy will run the
	// discovery service

	// NOTE: It is assumed that a PUC discovery service will be
	// running on the info port.  If not, this app and whatever
	// is running on 5149 will likely become confused

	InfoListener pIL = new InfoListener( this );
	pIL.start();
	listeners.addElement( pIL );

	discoveryServiceRunning = true;
	if ( discoveryConnection != null )
	    discoveryConnection.stop();
	discoveryConnection = null;
    }

    public void stop() {
	int size = listeners.size();
	for (int i = 0; i < size; i++) {
            Listener curr = (Listener) listeners.elementAt(i);
	    curr.stopListener();
	}
    }

    // ConnectionListener Methods
    // - used with discovery service connection

    // we don't currently listen for messages received from the
    // discovery service
    public void messageReceived(ConnectionEvent.MessageReceived e) { }

    // try to establish a new discovery service
    public void connectionLost(ConnectionEvent.ConnectionLost e) {

	// if the discovery service ever goes away, another PUCProxy
	// can start one

	try {
	    startDiscoveryService();
	    serverStateChanged( false, null );
	}
	catch( BindException ex ) {
	    ex.printStackTrace();
	    discoveryServiceRunning = false;
	}
    }

    // re-send registration messages
    public void connectionRegained(ConnectionEvent.ConnectionRegained e) {

	if (! discoveryServiceRunning ) {
	    try {
		Enumeration en = devices.elements();
		while( en.hasMoreElements() ) {
		    Device2 dev = (Device2)en.nextElement();
		    Message msg = new Message.RegisterDevice( dev.getName(),
							      dev.getPort() );
		    discoveryConnection.send( msg );
		}
	    }
	    catch( Exception ex ) { }
	}
    }


    public void serverStateChanged( boolean added, Device2 dev ) {

	if ( discoveryServiceRunning ) {
	    Enumeration e = connections.elements();
	    while( e.hasMoreElements() ) {
		Connection c = (Connection)e.nextElement();
		if ( c instanceof InfoConnection )
		    ((InfoConnection)c).update();
	    }
	}
	else {
	    if ( dev == null )
		return;

	    Message msg;
	    if ( added )
		msg = new Message.RegisterDevice( dev.getName(), dev.getPort() );
	    else
		msg = new Message.UnregisterDevice( dev.getPort() );

	    try {
		discoveryConnection.send( msg );
	    }
	    catch( Exception e ) {
		System.err.println( "Problem sending message to discovery service." );
	    }
	}
    }

    public String getName() {
	try {
	    return InetAddress.getLocalHost().getHostName();
	} catch( Exception e ) {
	    return "PUCProxy";
	}
    }

    public void startListener(Device2 device) {
        if (findListener(device.getPort()) != null)
            return;

	try {
	    Listener l = new DeviceListener(this, device);
	    l.start();
	    listeners.addElement(l);

	    serverStateChanged( true, device );
	}
	catch( Exception e ) {
	    System.err.println( "Another network application is already running on this port!" );
	    System.err.println( "Perhaps another appliance is running on it?" );
	    e.printStackTrace();
	}
    }


    public void stopListener( Device2 device ) {
        Listener l = findListener(device.getPort());

        if (l == null)
            return;

        listeners.removeElement(l);
        l.stopListener();

	serverStateChanged( false, device );
    }

    private Listener findListener(int port) {
        int i = 0;
        Listener l = null;

        while (i < listeners.size()) {
            l = (Listener) listeners.elementAt(i);
            if (l.getPort() == port)
                return l;
            i++;
        }
        return null;
    }

    private void addConnection(Socket _sock, Device2 _device) {
        Connection conn = new DeviceConnection(_sock, _device, this);
        connections.addElement(conn);

	System.out.println( "PUCServer: Added connection to " + _device.getName() );
	_device.addConnection(conn);

        conn.start();
    }

    private void addInfoConnection(Socket _sock) {
        Connection conn = new InfoConnection(_sock, this);
        connections.addElement(conn);
        conn.start();
    }


    public void addDevice(Device2 _dev) {
        devices.addElement(_dev);
    }

    public void removeDevice(Device2 _dev) {
        devices.removeElement(_dev);

	// close and remove connections to this device
	Vector vTemp = new Vector();

	Enumeration e = connections.elements();
	while( e.hasMoreElements() ) {
	    Connection conn = (Connection)e.nextElement();

	    if (conn instanceof DeviceConnection &&
		((DeviceConnection)conn).getDevice() == _dev) {
		vTemp.addElement(conn);
		conn.close();
	    }
	}

	e = vTemp.elements();
	while( e.hasMoreElements() ) {
	    Connection c = (Connection)e.nextElement();
	    _dev.removeConnection( c );
	    connections.removeElement( c );
	}
    }

    private void receiveMessage(Message _msg, String _device) {
        System.out.println("Received message (" + _device + "): " + _msg);
    }

    private void sendMessage(Message _msg, String _device) {
        int size = connections.size();
        Connection curr = null;
        for (int i = 0; i < size; i++) {
            curr = (Connection) connections.elementAt(i);
            if (curr instanceof DeviceConnection &&
		((DeviceConnection)curr).getDevice().equals(_device)) {
                curr.send(_msg);
            }
        }
    }


    public abstract class Listener extends Thread {
        public int port = -1;
        public PUCServer server = null;
        public ServerSocket incoming = null;
        private boolean stopping = false;


        public Listener(PUCServer _server, int _port) throws BindException {
            server = _server;
            port = _port;

            try {
                System.out.println("Attempting to listen on port " + port);
                incoming = new ServerSocket(port);
	    } catch (BindException e) {

		throw e;
            } catch (Exception e2) {
                System.out.println(e2);
                e2.printStackTrace();
            }
        }

	public abstract void handleIncoming( Socket sock );

        public int getPort() {
            return port;
        }

        public void stopListener() {
            System.out.println("Done listening on port " + port);
            this.stopping = true;
            try {
                incoming.close();
            } catch (IOException e) {
                System.err.println("Error closing incoming socket in PUCServer.Listener.stopListener:");
                e.printStackTrace();
            }
            this.stop();
        }

        public void run() {
            Socket sock = null;

            while (incoming != null && !stopping) {
                System.out.println("Listening...");
                try {
                    sock = incoming.accept();
                    if (sock != null) {
                        System.out.println("Got new connection! " + port );
			handleIncoming( sock );
                    }
                } catch (SocketException se) {
                    if (!stopping) {
                        System.err.println("SocketException in PUCServer.Listener.run():");
                        se.printStackTrace();
                    }
                } catch (Exception e) {
                    System.err.println("Exception in PUCServer.Listener.run():");
                    e.printStackTrace();
                }
            }
        }
    }

    public class DeviceListener extends Listener {

	public Device2 device = null;

	public DeviceListener(PUCServer _server, Device2 _device)
	    throws BindException {
	    super(_server, _device.getPort());
	    device = _device;
        if (device == null)
          throw new NullPointerException("Device is null!");
	}


	public void handleIncoming( Socket sock ) {
	    System.out.println( "Handle Incoming Device" );
		server.addConnection(sock, device);
	}
    }

    public class InfoListener extends Listener {

	public InfoListener(PUCServer _server) throws BindException {
	    super(_server, SERVER_INFO_PORT);
	}

	public void handleIncoming( Socket sock ) {

	    server.addInfoConnection(sock);
	}
    }

    public abstract class Connection extends Thread {
        protected PUCServer server = null;
        protected Socket sock = null;
        protected DataInputStream in = null;
        protected DataOutputStream out = null;
        protected XMLOutputter xmlgen = null;
        protected SAXBuilder saxbuild = null;
        protected boolean shutdown = false;
        protected Document doc = null;
	protected Hashtable properties = null;

	public Connection(Socket _sock, PUCServer _server) {
            sock = _sock;
            server = _server;
	    properties = new Hashtable();
            try {
                in = new DataInputStream( sock.getInputStream() );
                out = new DataOutputStream( sock.getOutputStream() );
                xmlgen = new XMLOutputter();
                saxbuild = new SAXBuilder();
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
	}

	public abstract void handleMessage(Message m);
	
	public void setProperty( Object key, Object val ) {

	    properties.put( key, val );
	}

	public Object getProperty( Object key ) {

	    return properties.get( key );
	}

	public void handleShutdown() {

	    server.connections.remove(this);
	}

        public void close() {
            try {
                sock.close();
            } catch (Exception e) {
                System.err.println("Error closing socket: " + e);
                e.printStackTrace();
            }
        }

        public boolean isFinished() {
            return shutdown;
        }

	public InetAddress getInetAddress() {

	    if ( sock != null )
		return sock.getInetAddress();
	    else
		return null;
	}

	/*
        public int lsbByteArrayToInt(byte[] array) {
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

        public byte[] intToLsbByteArray(int num, int len) {
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
	*/

        public void send(Message _msg) {

            if (shutdown)
                return;

	    synchronized( out ) {

		try {
		    int len, xmllen;
		    _msg.prepareMessage( this );
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
		    
		    System.out.println( "Sending message size(" + xmllen + "," + len + ") : " + _msg.toString() );
		    
		    out.writeInt( len );
		    out.writeInt( xmllen );
		    out.write(msgbytes);

		} catch (Exception e) {
		    shutdown = true;
		    System.out.println(e);
		    e.printStackTrace();
		}
	    }
        }


        public void run() {
	    int bytesRead = 0;
	    int len = 0;
	    int xmllen = 0;
	    int ret = 0;
	    byte[] buf = new byte[65535];
	    byte[] num = new byte[4];
	    Document doc;
	    Message msg;

            while (!shutdown) {
                try {
		    ret = 0;

		    // STEP #1: Read length value
		    
		    len = in.readInt();
		    

		    // STEP #2: Read XML length value
		    
		    xmllen = in.readInt();
			    
		    if ( xmllen > len )
			throw new IOException( "XML length cannot exceed message length." );

		    
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
				shutdown = true;
			    }

			    // get binary data into usable form
			    ByteArrayInputStream binData = null;
			    if ( len > xmllen )
				binData = new ByteArrayInputStream( buf, xmllen, len-xmllen );
			    
			    if ( !shutdown ) {
			      
				msg = com.maya.puc.common.Message.decode(doc,binData,(binData == null?-1:len-xmllen));
				if (msg.isValid())
				    handleMessage( msg );
			    }

			    break;
			}
		    }
                } catch (IOException e) {
                    e.printStackTrace();
                    shutdown = true;
                }
            }

            handleShutdown();
        }
    }

    public class InfoConnection extends Connection {

	public InfoConnection(Socket _sock, PUCServer _server) {
	    super(_sock,_server);
	}

	public void update() {

	    send(new Message.ServerInformation( PUCServer.this.getName(), devices.elements() ));
	}

	public void handleMessage( Message msg ) {

	    if ( msg == null )
		return;

	    if (msg instanceof Message.ServerInformationRequest) {
		System.out.println("Received Information Request...Sending response.");
		send(new Message.ServerInformation( PUCServer.this.getName(), devices.elements() ));
	    }
	    else if (msg instanceof Message.RegisterDevice) {

		RegisteredDevice dev = new RegisteredDevice( (Message.RegisterDevice)msg );

		if ( registeredDevices.get( new Integer( dev.getPort() ) ) == null ) {

		    registeredDevices.put( new Integer( dev.getPort() ), dev );
		    devices.addElement( dev );
		    PUCServer.this.serverStateChanged( true, dev );
		}
	    }
	    else if (msg instanceof Message.UnregisterDevice) {

		Integer port = new Integer( ((Message.UnregisterDevice)msg).getDevicePort() );
		RegisteredDevice dev = (RegisteredDevice)registeredDevices.get( port );
		if ( dev != null ) {
		    registeredDevices.remove( port );
		    devices.removeElement( dev );
		    PUCServer.this.serverStateChanged( false, dev );
		}
	    }
	}
    }

    /* Debugging Method
         public DeviceConnection stupid() {
      return new DeviceConnection( (Socket)null, "stupid", this );
         }
     */

   public class DeviceConnection
       extends Connection
   {
     private Device2 device = null;

     public DeviceConnection(Socket _sock, Device2 _device, PUCServer _server)
     {
       super(_sock, _server);
       device = _device;
     }

     public Device2 getDevice()
     {
       return device;
     }

     public void handleMessage(Message msg)
     {

       Device2 dev;
       if (msg != null)
       {
         for (int i = 0; i < devices.size(); i++)
         {
             if (msg instanceof Message.ServerInformationRequest)
             {
               send(new Message.ServerInformation(PUCServer.this.getName(),
                                                  devices.elements()));
             }
             else
               device.handleMessage(this, msg);
         }
       }
     }

     public void handleShutdown()
     {
       super.handleShutdown();
       device.removeConnection(this);
     }
   }

    public void stateChanged(String device, String state, String value)
    {
      Connection c;
      if ( (state == null) || (value == null))
        return;
      Message.StateChangeNotification mscn = new Message.StateChangeNotification(
          state, value);

      for (int i = 0; i < connections.size(); i++)
      {
        c = (Connection) connections.elementAt(i);
        if (c instanceof DeviceConnection &&
            ( (DeviceConnection) c).getDevice().equals(device))
        {
          c.send(mscn);
        }
      }
    }


    class MonitorThread extends Thread
    {
      private PUCServer server = null;

      /**
       * Creates a MonitorThread.
       */

      public MonitorThread(PUCServer _server)
      {
        server = _server;
      }

      public void run()
      {
        Connection conn = null;
        while (true)
        {
          for (int i = 0; i < connections.size(); i++)
          {
            conn = (Connection) connections.elementAt(i);
            if (conn.isFinished())
            {
              System.out.println("Closing a client connection.");
              conn.close();
              conn.stop();
              connections.removeElement(conn);

              if (conn instanceof DeviceConnection)
              {
                DeviceConnection dc = (DeviceConnection) conn;
                dc.getDevice().removeConnection(conn);
              }
            }
          }

          try
          {
            sleep(10000);
          }
          catch (InterruptedException e)
          {
          }
        }
      }
    }

  }
