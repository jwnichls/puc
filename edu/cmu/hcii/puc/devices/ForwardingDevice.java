/*
 * ForwardingDevice.java
 *
 * This device forwards message packets to another device. May useful
 * for aggregating multiple appliances into one server (even if the
 * appliances themselves are attached to different machines).
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;

import com.maya.puc.common.*;


// Class Definition

public class ForwardingDevice extends AbstractDevice2 implements ConnectionListener {

    //**************************
    // Constants
    //**************************

    protected final static String CONFIG_FILE = "forward.cfg";
    protected final static String SERVER_ADDR = "appliance-addr";
    protected final static String SERVER_PORT = "appliance-port";

    //**************************
    // Member Variables
    //**************************

    Connection m_pForwardingConn;

    
    //**************************
    // Constructor
    //**************************
    
    public ForwardingDevice() {

	FauxEform pSettings = new FauxEform();
	pSettings.load( CONFIG_FILE );

	String sAddress = pSettings.getStringAttr( SERVER_ADDR, "localhost" );
	int    nPort    = pSettings.getIntAttr(    SERVER_PORT, 5150 );

	if ( sAddress.equals( "" ) ) {
	    // present a dialog box to get the server addr and port
	    openConfigDlg( sAddress, nPort );
	}
	else {
	    try {
		// connect to server
		connectToServer( sAddress, nPort );
	    }
	    catch( Exception e ) {
		
		openConfigDlg( sAddress, nPort );
	    }
	}
    }


    //**************************
    // Protected Methods
    //**************************

    protected void connectToServer( String sAddr, int nPort ) throws Exception {
	InetAddress pAddr = InetAddress.getByName( sAddr );
	
	m_pForwardingConn = new Connection( pAddr, nPort );
	m_pForwardingConn.addConnectionListener( this );
    }

    public class ConfigDialog extends java.awt.Dialog {

	// Member Variables
	
	String  m_sServerName = "localhost";
	int     m_nPortNum    = 5150;
	
	protected ForwardingDevice m_pForwardDevice;
	
	TextField m_pServerName;
	TextField m_pPortNum;
	
	Button m_pOkay;
	Button m_pCancel;
	
	
	// Constructor
	
	public ConfigDialog( Frame frame, ForwardingDevice fd, 
			     String sAddr, int nPort ) {
	    super( frame, "Server Address", true );

	    m_sServerName = sAddr;
	    m_nPortNum = nPort;
	    
	    m_pForwardDevice = fd;
	    
	    GridBagLayout gbl = new GridBagLayout();
	    GridBagConstraints pGC = new GridBagConstraints();
	    setLayout( gbl );
	    
	    pGC.gridx = 0;
	    pGC.gridy = 0;
	    pGC.gridwidth = 1;
	    pGC.gridheight = 1;
	    pGC.weightx = 0.0;
	    pGC.weighty = 0.0;
	    pGC.anchor = GridBagConstraints.EAST;
	    pGC.fill = GridBagConstraints.NONE;
	    pGC.insets = new Insets( 0, 0, 0, 0 );
	    pGC.ipadx = 0;
	    pGC.ipady = 0;
	    
	    Label l2 = new Label( "Address:" );
	    l2.setAlignment( Label.RIGHT );
	    pGC.gridy = 1;
	    gbl.setConstraints( l2, pGC );
	    
	    pGC.gridx = 1;
	    pGC.gridwidth = 2;
	    
	    m_pServerName = new TextField( m_sServerName, 25 );
	    pGC.anchor = GridBagConstraints.WEST;
	    pGC.gridy = 1;
	    gbl.setConstraints( m_pServerName, pGC );

	    Label l3 = new Label( "Port:" );
	    l3.setAlignment( Label.RIGHT );
	    pGC.gridx = 0;
	    pGC.gridy = 2;
	    pGC.anchor = GridBagConstraints.EAST;
	    gbl.setConstraints( l3, pGC );
	    

	    pGC.gridx = 1;
	    pGC.gridwidth = 2;
	    
	    m_pPortNum = new TextField( m_nPortNum + "", 25 );
	    pGC.anchor = GridBagConstraints.WEST;
	    pGC.gridy = 2;
	    gbl.setConstraints( m_pPortNum, pGC );
	    
	    m_pCancel = new Button( "Cancel" );
	    pGC.gridy = 3;
	    pGC.gridwidth = 1;
	    pGC.weightx = 1.0;
	    pGC.weighty = 1.0;
	    pGC.anchor = GridBagConstraints.CENTER;
	    gbl.setConstraints( m_pCancel, pGC );
	    
	    m_pOkay = new Button( "OK" );
	    pGC.gridx = 2;
	    gbl.setConstraints( m_pOkay, pGC );
	    
	    m_pCancel.addActionListener( new ActionListener() {
		    public void actionPerformed( ActionEvent e ) {
			ConfigDialog.this.dispose();
		    }
		});
	    
	    m_pOkay.addActionListener( new ActionListener() {
		    public void actionPerformed( ActionEvent e ) {
			
			m_sServerName = m_pServerName.getText();

			try {
			    m_nPortNum = Integer.parseInt( m_pPortNum.getText() );
			
			    m_pForwardDevice.connectToServer( m_sServerName, m_nPortNum );
			
			    ConfigDialog.this.dispose();			
			} catch( Exception ex ) {
			 
			    System.out.println( "address or port incorrectly specified" );
			}
		    }
		});
	    
	    add( l2 );
	    add( m_pServerName );
	    add( m_pPortNum );
	    add( m_pCancel );
	    add( m_pOkay );
	    
	    this.setSize( 240, 125 );
	    
	    this.show();
	}
    }    

    protected void openConfigDlg( String addr, int port ) {

	ConfigDialog cd = new ConfigDialog( new Frame(), this, addr, port );
	cd.setVisible( true );
    }


    //**************************
    // Device2 Methods
    //**************************
    
    public void start() {

	try {
	    m_pForwardingConn.connect();

	    super.start( AbstractDevice2.STATUS_ACTIVE );
	}
	catch( Exception e ) {
	    
	    super.stop( AbstractDevice2.STATUS_ERROR );
	}
    }

    public void stop() {

	m_pForwardingConn.stop();
	super.stop();
    }

    public String getName() {

	return "forwarder";
    }

    public int getDefaultPort() {

	return 5160;
    }

    public String getSpec() {

	return "";
    }

    public String getSpecFileName() {
	
	return "";
    }

    public void configure() {

	openConfigDlg( m_pForwardingConn.getConnIP().toString(), m_pForwardingConn.getConnPort() );
    }

    public void handleMessage( PUCServer.Connection conn, Message msg ) {

	try {
	    m_pForwardingConn.send( msg );
	}
	catch( Exception e ) { }
    }

    public void messageReceived( ConnectionEvent.MessageReceived e ) {

	System.out.println( "Message received for forwarding: " + e.getMessage().toString() );

	PUCServer.Connection c;
	
	for (int i = 0; i < connections.size(); i++) {
	    c = (PUCServer.Connection) connections.get(i);
	    
	    try {
		c.send( e.getMessage() );
	    }
	    catch( Exception ex ) { }
	}
    }

    public void connectionLost( ConnectionEvent.ConnectionLost e ) { }
    public void connectionRegained( ConnectionEvent.ConnectionRegained e ) { }
}
