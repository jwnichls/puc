/**
 * UnloadedAppliance.java
 * 
 * The UnloadedAppliance represents an appliance that is known about,
 * but whose specification has not been downloaded and whose interface
 * has not been generated.  The loadAppliance() method will download
 * the specification and return the Appliance object, which can be
 * used for generating a user interface.
 *
 * Revision History:
 * -----------------
 * 08/01/2002: (JWN) Created file and added variables for parsing.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;

import java.io.StringBufferInputStream;

import java.lang.*;

import com.maya.puc.common.Connection;
import com.maya.puc.common.ConnectionEvent;
import com.maya.puc.common.ConnectionListener;
import com.maya.puc.common.Message;

import edu.cmu.hcii.puc.parser.SpecParser;


// Class Definition

public class UnloadedAppliance extends ApplianceStub {

    //**************************
    // Member Variables
    //**************************



    //**************************
    // Constructor
    //**************************

    public UnloadedAppliance( ServerInfo pServer, int nPort, String sName ) {

	super( pServer, nPort, sName );
    }

    public UnloadedAppliance( Appliance pAppl ) {

	super( pAppl );

	if ( m_pConnection != null ) {
	    m_pConnection.stop();
	    m_pConnection = null;
	}
    }

    //**************************
    // Member Methods
    //**************************

    public boolean isLoaded() { return false; }

    public Appliance loadAppliance() {

	try {

	    m_pConnection = new Connection(m_pServer.getInetAddress(),
						    m_nPort);
	    
	    m_pConnection.connect();
	    
	    if (!m_pConnection.isConnected()) {
		Globals.printLog("Unable to open connection to device.");
		Globals.printLog("Bad things may happen.");
		System.err.println( "Unable to connect to device..." );
		m_pConnection.stop();
	    } else {
		m_pConnection.addConnectionListener( this );
		
		Message msg = new Message.SpecRequest();
		m_pConnection.send(msg);
		
		Globals.printLog( "Waiting for response to specification request." );
	    }
	} catch (Throwable t) {
	    t.printStackTrace();
	}

	return null;
    }

    public void connectionLost(ConnectionEvent.ConnectionLost c) {
	
    }
    
    public void connectionRegained(ConnectionEvent.ConnectionRegained c) {
	
	try {
	    Message newMsg = new Message.SpecRequest();
	    m_pConnection.send( newMsg );
	}
	catch(Exception e) { }
    }
    
    public void messageReceived(ConnectionEvent.MessageReceived c) {
	
	Message msg = c.getMessage();
	
	System.out.println(msg.toString());
	
	if (msg instanceof Message.DeviceSpec) {
	    
	    try {
		Message.DeviceSpec specMsg = (Message.DeviceSpec)msg;

		Appliance pAppl = new Appliance( this );
		
		Globals.printLog( "Opening specification from network." );
		Globals.printLog("Beginning parse of specification.");
		SpecParser.parse( new StringBufferInputStream( specMsg.getSpec() ),
				  pAppl );
		Globals.printLog("Parse complete. " + pAppl.m_Objects.size() + " objects.");
		
		CheckboxMenuItem mi = new CheckboxMenuItem( pAppl.getName() );
		pAppl.setMenuItem( mi );
		
		m_pServer.activateAppliance( this, pAppl );

		m_pConnection.addConnectionListener( pAppl );
		m_pConnection.removeConnectionListener( this );

		Message newMsg = new Message.FullStateRequest();
		m_pConnection.send( newMsg );		
	    }
	    catch( Throwable t ) {
		t.printStackTrace();
	    }
	}
    }
}

