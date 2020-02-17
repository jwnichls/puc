/*
 * DeviceWrapper.java
 *
 * This class wraps a Device object and translates to/from a Device2
 * interface.
 */

// Package Definitions

package com.maya.puc.common;


// Import Declarations

import java.util.ArrayList;


// Class Definition

public class DeviceWrapper implements Device2, StateListener {

    //**************************
    // Member Variables
    //**************************

    protected Device m_pDevice;

    private ArrayList connections;
    private ArrayList statusListeners;



    //**************************
    // Constructor
    //**************************

    public DeviceWrapper( Device device ) {

	m_pDevice = device;

	connections = new ArrayList();
	statusListeners = new ArrayList();

	m_pDevice.addStateListener( this );
    }


    //**************************
    // StateListener Method
    //**************************

    public void stateChanged(String device, String state, String value) {

	if ( ( state == null ) || ( value == null ) ) {

	    updateStatus(false);
	    return;
	}

	dispatchStateEvent( state, value );
    }


    //**************************
    // Device2 Methods
    //**************************

    public String getName() {

	return m_pDevice.getName();
    }

    public String getSpec() {

	return m_pDevice.getSpec();
    }

    public void handleMessage( PUCServer.Connection c, Message msg ) {

	try {
	    if (msg instanceof Message.StateChangeRequest) {
		Message.StateChangeRequest mscr = (Message.StateChangeRequest) msg;
		m_pDevice.requestStateChange(mscr.getState(), mscr.getValue());
	    } else if (msg instanceof Message.FullStateRequest) {
		m_pDevice.requestFullState();
	    } else if (msg instanceof Message.CommandInvokeRequest) {
		m_pDevice.requestCommandInvoke(((Message.CommandInvokeRequest) msg).getCommand());
	    } else if (msg instanceof Message.SpecRequest) {
		c.send(new Message.DeviceSpec(getSpec()));
	    }
	}
	catch( Exception e ) { }
    }

    public void configure() {

	m_pDevice.configure();
    }

    /**
     * Default is to have devices be manually activatable
     */
    public boolean isManuallyActivatable()
    {
      return true;
    }

    public boolean hasGUI() {

	return m_pDevice.hasGUI();
    }

    public void setGUIVisibility(boolean isVisible) {

	m_pDevice.setGUIVisibility( isVisible );
    }

    public boolean isGUIVisible() {

	return m_pDevice.isGUIVisible();
    }

    /**
     * Starts the device; sets status to the specified string
     */
    public void start()
    {
      if (! m_pDevice.isRunning())
      {
        m_pDevice.start();
        updateStatus(true);
      }
      else
        updateStatus(false);
    }

    /**
     * Stops the device; sets status to the specified string
     */
    public void stop()
    {
      if (m_pDevice.isRunning())
      {
        m_pDevice.stop();
        updateStatus(true);
      }
      else
        updateStatus(false);
    }

    public boolean isRunning() {

	return m_pDevice.isRunning();
    }

    public String getStatus() {

	return m_pDevice.getStatus();
    }

    public int getPort() {

	return m_pDevice.getPort();
    }

    public void setPort( int nPort ) {

	m_pDevice.setPort( nPort );
    }

    /**
     * Adds the given Connection to the list of connections for this device
     */
    public void addConnection(PUCServer.Connection c)
    {
	connections.add(c);
    }

    /**
     * Removes the given Connection from the list of connections for
     * this device
     */
    public void removeConnection(PUCServer.Connection c)
    {
	connections.remove(c);
    }

    /**
     * Removes all the connections for this device.
     */
    public void removeAllConnections()
    {
	connections.clear();
    }

    /**
     * Adds the given StatusListener to the list of status listeners
     * for this device
     */
    public void addStatusListener(StatusListener sl)
    {
	statusListeners.add(sl);
    }

    /**
     * Removes the given StatusListener from the list of status
     * listeners for this device
     */
    public void removeStatusListener(StatusListener sl)
    {
	statusListeners.remove(sl);
    }

    /**
     * Removes all the status listeners for this device.
     */
    public void removeAllStatusListeners()
    {
	statusListeners.clear();
    }

    /**
     * Updates the specified state/value pair on all connections
     *
     * @param state The name of the state variable to update
     * @param value The new value of state
     */
    protected void dispatchStateEvent(String state, String value)
    {
	PUCServer.Connection c;

	for (int i = 0; i < connections.size(); i++) {

	    try {
		c = (PUCServer.Connection) connections.get(i);

		Message m = new Message.StateChangeNotification( state, value );
		c.send( m );
		if (c.isFinished()) removeConnection(c);
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Updates the status on all registered StateListeners
     */
    protected void updateStatus(boolean activeChanged)
    {
      StatusListener sl;

      for(int i = 0; i < statusListeners.size(); i++)
      {
        sl = (StatusListener) statusListeners.get(i);
        sl.statusChanged( this, getStatus() );
        if (activeChanged)
        {
          sl.activeChanged( this );
        }
      }
    }

}
