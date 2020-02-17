package com.maya.puc.common;

import java.awt.*;

public interface Device {

    /**
     * Get the human-readable name of the device supported by this class.
     */
    public String getName();

    /**
     * Get the XML spec used to generate the interface for this device.
     */
    public String getSpec();

    /**
     * Request that the full device state be sent out.
     */
    public void requestFullState();

    /**
     * Request that certain state be changed.
     *
     * @param state A state name specified in the spec.
     * @param value The string value to change the named state to.
     */
    public void requestStateChange(String state, String value);

    /**
     * Request that a certain command be sent to the device.
     *
     * @param command A command name, as specified in the spec.
     */
    public void requestCommandInvoke(String command);

    /**
     * Register a StateListener to receive state change notifications.
     */
    public void addStateListener(StateListener sl);

    /**
     * Remove a StateListener from the notification list.
     */
    public void removeStateListener(StateListener sl);

    /**
     * Take steps to configure the device (i.e. pop up a configuration dialog).
     */
    public void configure();

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
