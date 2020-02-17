/**
 * ServerInfo.java
 * 
 * This object represents the clients knowledge of a PUCProxy server.
 * It is created when a user connects to a PUCProxy and contains
 * the most recent information retrieved via a ServerInformation
 * message, pointers to the UI elements on the PUC that represent the
 * server, and pointers to the Appliances that the PUC may be
 * connected to via the server.
 *
 * Revision History:
 * -----------------
 * 08/01/2002: (JWN) Created file and added variables for parsing.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.awt.Menu;
import java.awt.MenuItem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.*;

import java.net.InetAddress;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.maya.puc.common.Connection;
import com.maya.puc.common.ConnectionEvent;
import com.maya.puc.common.ConnectionListener;
import com.maya.puc.common.Message;


// Class Definition

public class ServerInfo implements ConnectionListener {

    //**************************
    // Member Variables
    //**************************

    protected String     m_sName;
    protected Hashtable  m_hAppliances;
    protected Connection m_pConnection;

    protected static PUC m_pPUC;

    protected Menu       m_pMenu;

    protected boolean    m_bOpenAllNext;


    //**************************
    // Constructor
    //**************************

    public ServerInfo( InetAddress inetServer, PUC pPUC ) throws java.io.IOException {

	this( inetServer, pPUC, false );
    }

    public ServerInfo( InetAddress inetServer, 
		       PUC pPUC, 
		       boolean bOpenAll ) 
	throws java.io.IOException {

	m_bOpenAllNext = bOpenAll;

	m_pPUC = pPUC;
	m_hAppliances = new Hashtable();

	// Connect to server and await ServerInformation message
	m_pConnection = new Connection(inetServer,
				       com.maya.puc.common.PUCServer.SERVER_INFO_PORT );
	m_pConnection.connect();
	
	if (!m_pConnection.isConnected()) {
	    Globals.printLog("Unable to connect to server info. port "
			     + inetServer.toString() );
	    System.err.println( "Unable to connect to server info port. " 
				+ inetServer.toString() );
	    m_pConnection.stop();
	}
	else {
	    m_pConnection.addConnectionListener( this );
	    Message msg = new Message.ServerInformationRequest();
	    m_pConnection.send(msg);
	    
	    Globals.printLog( "waiting for server information response." );
	}
    }
 
    //**************************
    // ActionListener (for MenuItems)
    //**************************
    
    protected class ServerMenuListener implements ActionListener {

	ApplianceStub m_pAppliance;

	public ServerMenuListener( ApplianceStub pAppl ) {

	    setAppliance( pAppl );
	}

	public void setAppliance( ApplianceStub pAppl ) {

	    m_pAppliance = pAppl;
	}

	public void actionPerformed( ActionEvent e ) {

	    if ( m_pAppliance instanceof UnloadedAppliance ) {

		((UnloadedAppliance)m_pAppliance).loadAppliance();
	    }
	    else if ( m_pAppliance instanceof Appliance ) {

		m_pPUC.setCurrentAppliance( (Appliance)m_pAppliance );
	    }
	}
    }


    //**************************
    // Member Methods
    //**************************

    public InetAddress getInetAddress() {

	return m_pConnection.getConnIP();
    }

    public void openAllNext() {

	m_bOpenAllNext = true;
    }

    public void activateAppliance( UnloadedAppliance pUAppl,
				   Appliance pAppl ) {

	m_pPUC.addActiveAppliance( pAppl );

	m_hAppliances.remove( pUAppl );
	m_hAppliances.put( pAppl.getServerName(), pAppl );

	MenuItem pMI = pAppl.getServerMenuItem();
	pMI.setLabel( pAppl.getServerName() );
	pMI.removeActionListener( pAppl.getServerMenuItemListener() );
	ActionListener l = new ServerMenuListener( pAppl );
	pMI.addActionListener( l );
	pAppl.setServerMenuItemListener( l );
    }

    protected void initMenu() {

	if ( m_pMenu == null ) {

	    m_pMenu = new Menu( m_sName );
	    m_pPUC.getServerMenu().add( m_pMenu );
	}
    }

    public void connectionLost(ConnectionEvent.ConnectionLost c) {
	
    }
    
    public void connectionRegained(ConnectionEvent.ConnectionRegained c) {
	
    }

    public void messageReceived(ConnectionEvent.MessageReceived c) {

	Message msg = c.getMessage();
	
	System.out.println(msg.toString());
	
	if (msg instanceof Message.ServerInformation) {
	    Message.ServerInformation smsg = (Message.ServerInformation)msg;
	    
	    if ( m_pMenu == null ) initMenu();

	    m_pMenu.removeAll();
	    clearMarks();

	    int nCount = 0;

	    m_sName = smsg.getServerName();
	    m_pMenu.setLabel( m_sName );

	    Enumeration e = smsg.getDeviceInfo();
	    while( e.hasMoreElements() ) {
		Message.ServerInformation.DeviceInfo pDI = (Message.ServerInformation.DeviceInfo)e.nextElement();

		ApplianceStub pA = (ApplianceStub)m_hAppliances.get( pDI.getDeviceName() );
		if ( pA == null ) {
		    pA = new UnloadedAppliance( this,
						pDI.getDevicePort(),
						pDI.getDeviceName() );
		    m_hAppliances.put( pDI.getDeviceName(), pA );

		    if ( m_bOpenAllNext )
			((UnloadedAppliance)pA).loadAppliance();
		}

		pA.setMark( true );
		nCount++;

		MenuItem pMI = new MenuItem( pDI.getDeviceName() );
		ActionListener l = new ServerMenuListener( pA );
		pMI.addActionListener( l );
		m_pMenu.add( pMI );
		pA.setServerMenuItem( pMI );
		pA.setServerMenuItemListener( l );
	    }

	    m_bOpenAllNext = false;

	    if ( nCount < m_hAppliances.size() ) {

		if ( nCount > 0 )
		    m_pMenu.addSeparator();

		e = m_hAppliances.elements();
		while( e.hasMoreElements() ) {
		    ApplianceStub pA = (ApplianceStub)e.nextElement();
		    if ( !pA.isMarked() ) {
			MenuItem pMI = new MenuItem( pA.getServerName() );
			pMI.setEnabled( false );
			m_pMenu.add( pMI );
		    }
		}
	    }

	    if ( m_hAppliances.size() == 0 &&
		 m_pMenu.getItemCount() == 0 ) {
		
		MenuItem pMI = new MenuItem( "No Appliances" );
		pMI.setEnabled( false );
		m_pMenu.add( pMI );
	    }
	}

    }

    public void unload( Appliance pAppl ) {

	UnloadedAppliance pUAppl = new UnloadedAppliance( pAppl );

	MenuItem pMI = pAppl.getServerMenuItem();
	pMI.removeActionListener( pAppl.getServerMenuItemListener() );
	ActionListener l = new ServerMenuListener( pUAppl );
	pMI.addActionListener( l );
	pAppl.setServerMenuItemListener( l );
    }

    protected void clearMarks() {
	
	Enumeration e = m_hAppliances.elements();
	while( e.hasMoreElements() )
	    ((ApplianceStub)e.nextElement()).setMark( false );
    }
}
