/**
 * Appliance.java
 * 
 * This object represents an appliance that the PUC can control.  This
 * includes not only information from the specification, but links to
 * the generated user interface panels.
 *
 * Revision History:
 * -----------------
 * 07/07/2001: (JWN) Created file and added variables for parsing.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.awt.CheckboxMenuItem;

import java.lang.*;

import java.util.Hashtable;
import java.util.Vector;

import com.maya.puc.common.Connection;
import com.maya.puc.common.ConnectionEvent;
import com.maya.puc.common.ConnectionListener;
import com.maya.puc.common.Message;

import edu.cmu.hcii.puc.uigen.UIGenerator;


// Class Definition

public class Appliance extends ApplianceStub {

    //**************************
    // Member Variables
    //**************************

    public GroupNode m_pRoot;

    public Hashtable m_Objects;

    public Vector    m_vDependedObjects;

    protected UIGenerator m_pUIGenerator;
    protected CheckboxMenuItem m_pMenuItem;


    //**************************
    // Constructor
    //**************************

    public Appliance( UnloadedAppliance pAppliance ) {

	super( pAppliance );
    }

    
    //**************************
    // Member Methods
    //**************************
    
    public void connectionLost(ConnectionEvent.ConnectionLost c) {
	
    }
    
    public void connectionRegained(ConnectionEvent.ConnectionRegained c) {
	
	try {
	    System.out.println( "Received ConnectionRegained Event!" );
	    
	    System.out.println( "Sending FullStateRequest" );
	    Message newMsg = new Message.FullStateRequest();
	    m_pConnection.send( newMsg );
	}
	catch(Exception e) { }
    }
    
    public void messageReceived(ConnectionEvent.MessageReceived c) {
	
	Message msg = c.getMessage();
	
	System.out.println(msg.toString());
	
	if (msg instanceof Message.StateChangeNotification) {
	    Message.StateChangeNotification scnMsg = (Message.StateChangeNotification) msg;
	    
	    ApplianceState as = (ApplianceState) m_Objects.get(scnMsg.getState());
		
	    if (as != null)
		as.setValue(scnMsg.getValue());
	}
    }

    public void setParseVariables( String sName, GroupNode roots, Hashtable objects, Vector dobjects ) {

	m_sDeviceName = sName;
	m_pRoot = roots;
	m_Objects = objects;
	m_vDependedObjects = dobjects;
    }

    public boolean isLoaded() { return true; }

    public void setMenuItem( CheckboxMenuItem pMI ) {

	m_pMenuItem = pMI;
    }

    public CheckboxMenuItem getMenuItem() {

	return m_pMenuItem;
    }

    public UIGenerator getUIGenerator() {

	return m_pUIGenerator;
    }

    public void setUIGenerator( UIGenerator pUI ) {

	m_pUIGenerator = pUI;
    }

    public void setName( String sName ) {

	m_sDeviceName = sName;
    }
}

