/**
 * ApplianceStub.java
 *
 * The abstract super-class of Appliance and UnloadedAppliance.
 * Contains only a single method for distinguishing the load state of
 * the appliance.
 *
 * Revision History:
 * -----------------
 * 08/01/2002: (JWN) Created file and added variables for parsing.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.awt.MenuItem;

import java.awt.event.ActionListener;

import java.lang.*;

import com.maya.puc.common.Connection;
import com.maya.puc.common.ConnectionEvent;
import com.maya.puc.common.ConnectionListener;


// Class Definition

public abstract class ApplianceStub extends    Object
				    implements ConnectionListener {

    //**************************
    // Member Variables
    //**************************

    protected String     m_sServerName;
    protected String     m_sDeviceName;
    protected ServerInfo m_pServer;
    protected int        m_nPort;

    // Used to search for deactivated appliances in
    // ServerInformation messages (see ServerInfo.java)
    protected boolean    m_bMark;

    public Connection m_pConnection;

    protected MenuItem       m_pServerMenuItem;
    protected ActionListener m_pServerMenuItemL;


    //**************************
    // Constructor
    //**************************

    public ApplianceStub( ServerInfo pServer, int nPort, String sServerName ) {

	m_pServer     = pServer;
	m_nPort       = nPort;
	m_sServerName = sServerName;
    }

    public ApplianceStub( ApplianceStub pStub ) {

	m_pServer     = pStub.m_pServer;
	m_nPort       = pStub.m_nPort;
	m_sServerName = pStub.m_sServerName;
	m_sDeviceName = pStub.m_sDeviceName;
	m_bMark       = pStub.m_bMark;

	m_pConnection = pStub.m_pConnection;

	m_pServerMenuItem  = pStub.m_pServerMenuItem;
	m_pServerMenuItemL = pStub.m_pServerMenuItemL;
    }


    //**************************
    // Member Methods
    //**************************

    public abstract boolean isLoaded();

    public void setMark( boolean bMark ) {

	m_bMark = bMark;
    }

    public boolean isMarked() { return m_bMark; }

    public String getName() { return m_sDeviceName; }

    public String getServerName() { return m_sServerName; }

    public ServerInfo getServer() { return m_pServer; }

    public Connection getConnection() { return m_pConnection; }

    public void setServerMenuItem( MenuItem pMI ) {

	m_pServerMenuItem = pMI;
    }

    public MenuItem getServerMenuItem() { return m_pServerMenuItem; }

    public void setServerMenuItemListener( ActionListener pL ) {

	m_pServerMenuItemL = pL;
    }

    public ActionListener getServerMenuItemListener() {

	return m_pServerMenuItemL;
    }
}
