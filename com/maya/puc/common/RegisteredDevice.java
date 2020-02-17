/*
 * RegisteredDevice.java
 *
 * A class that describes a device that has registered with the discovery
 * service and is not actually available through this server.  
 *
 */

package com.maya.puc.common;

import java.awt.*;

public class RegisteredDevice implements Device2 {

    /*
     * Member Variables
     */

    protected String m_sDeviceName;
    protected int    m_nDevicePort;
    

    /*
     * Constructor
     */

    public RegisteredDevice( String name, int port ) {

	m_sDeviceName = name;
	m_nDevicePort = port;
    }

    public RegisteredDevice( Message.RegisterDevice msg ) {

	m_sDeviceName = msg.getDeviceName();
	m_nDevicePort = msg.getDevicePort();
    }

    /**
     * Get the human-readable name of the device supported by this class.
     */
    public String getName() {

	return m_sDeviceName;
    }

    /**
     * Get the XML spec used to generate the interface for this device.
     */
    public String getSpec() { 

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Handle an incoming message.
     */
    public void handleMessage( PUCServer.Connection c, Message m ) {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );	
    }

    /**
     * Add a connection.
     */
    public void addConnection( PUCServer.Connection c ) {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Remove a connection.
     */
    public void removeConnection( PUCServer.Connection c ) {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }
    
    /**
     * Remove all connections.
     */
    public void removeAllConnections() {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Take steps to configure the device (i.e. pop up a configuration dialog).
     */
    public void configure() {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Determine whether the device has a (non-configuration) GUI.
     */
    public boolean hasGUI() {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Specify whether the device GUI should be visible on the screen.
     */
    public void setGUIVisibility(boolean isVisible) {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Determine whether the device's GUI is visible on the screen.
     */
    public boolean isGUIVisible() {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Establish a connection with the device, and begin reporting state.
     */
    public void start() {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Stop generating state updates.
     */
    public void stop() {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Determine whether the device is currently active.
     */
    public boolean isRunning() {

	return true;
    }

    /**
     * These devices are not manually activatable.
     */
    public boolean isManuallyActivatable() {

	return false;
    }

    /**
     * Get the current status of this device.  Useful for reporting
     * connection errors.  Incidentally, to notify the PUCProxy program
     * that the status string has changed, send a state update
     * with state and value set to null to the StateListeners.
     */
    public String getStatus() {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Add a listener for status.
     */
    public void addStatusListener( StatusListener l ) {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Remove a listener for status.
     */
    public void removeStatusListener( StatusListener l ) {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }
    
    /**
     * Remove all status listeners.
     */
    public void removeAllStatusListeners() {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }

    /**
     * Set the port that clients will connect to in order to
     * access this device.
     */
    public int getPort() {

	return m_nDevicePort;
    }

    /**
     * Retrieve the number of the port that PUC clients will
     * connect to in order to access this device.
     */
    public void setPort(int port) {

	throw new UnsupportedOperationException( "RegisteredDevice objects are not implemented through this server" );
    }
}
