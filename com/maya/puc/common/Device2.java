package com.maya.puc.common;

import java.awt.*;

public interface Device2 {

    /**
     * Get the human-readable name of the device supported by this class.
     */
    public String getName();

    /**
     * Get the XML spec used to generate the interface for this device.
     */
    public String getSpec();

    /**
     * Handle an incoming message.
     */
    public void handleMessage( PUCServer.Connection c, Message m );

    /**
     * Add a connection.
     */
    public void addConnection( PUCServer.Connection c );

    /**
     * Remove a connection.
     */
    public void removeConnection( PUCServer.Connection c );

    /**
     * Remove all connections.
     */
    public void removeAllConnections();

    /**
     * Take steps to configure the device (i.e. pop up a configuration dialog).
     */
    public void configure();

    /**
     * Should return true if and only if this device can be manually started
     * and stopped by the user of the PUCProxy
     */
    public boolean isManuallyActivatable();

    /**
     * Determine whether the device has a (non-configuration) GUI.
     */
    public boolean hasGUI();

    /**
     * Specify whether the device GUI should be visible on the screen.
     */
    public void setGUIVisibility(boolean isVisible);

    /**
     * Determine whether the device's GUI is visible on the screen.
     */
    public boolean isGUIVisible();

    /**
     * Establish a connection with the device, and begin reporting state.
     */
    public void start();

    /**
     * Stop generating state updates.
     */
    public void stop();

    /**
     * Determine whether the device is currently active.
     */
    public boolean isRunning();

    /**
     * Get the current status of this device.  Useful for reporting
     * connection errors.  Incidentally, to notify the PUCProxy program
     * that the status string has changed, send a state update
     * with state and value set to null to the StateListeners.
     */
    public String getStatus();

    /**
     * Add a listener for status.
     */
    public void addStatusListener( StatusListener l );

    /**
     * Remove a listener for status.
     */
    public void removeStatusListener( StatusListener l );

    /**
     * Remove all status listeners.
     */
    public void removeAllStatusListeners();

    /**
     * Set the port that clients will connect to in order to
     * access this device.
     */
    public int getPort();

    /**
     * Retrieve the number of the port that PUC clients will
     * connect to in order to access this device.
     */
    public void setPort(int port);
}
