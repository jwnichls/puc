package edu.cmu.hcii.puc.devices;

/**
 * <p>Title: Abstract Device class for PUC interface</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author klitwack
 * @version 1.0
 */

// Import Declarations

import com.maya.puc.common.Device;
import com.maya.puc.common.Message;
import com.maya.puc.common.StateListener;
import com.maya.puc.common.TextResource;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Abstract class providing implementations of common functionality for the
 * Device interface
 */
public abstract class AbstractDevice implements Device
{

// Device constants

  /**
   * These constants are just utilities to provide common status values
   */
  public static final String STATUS_INACTIVE = "Idle";
  public static final String STATUS_ACTIVE = "Active";
  public static final String STATUS_ERROR = "Error";

// Instance variables

  private ArrayList listeners;
  private boolean isRunning;
  private String status;
  private int port;

// Constructor

  /**
   * Performs necessary initialization for device instance variables
   */
  public AbstractDevice()
  {
    listeners = new ArrayList();
    isRunning = false;
    status = STATUS_INACTIVE;
    port = getDefaultPort();
  }

// Implemented device methods

  /**
   * Starts the device; sets status to a default active string
   */
  public void start()
  {
    start(STATUS_ACTIVE);
  }

  /**
   * Starts the device; sets status to the specified string
   */
  public void start(String status)
  {
    isRunning = true;
    this.status = status;
    updateStatus();
  }

  /**
   * Stops the device; sets status to a default inactive string
   */
  public void stop()
  {
    stop(STATUS_INACTIVE);
  }

  /**
   * Stops the device; sets status to the specified string
   */
  public void stop(String status)
  {
    isRunning = false;
    this.status = status;
    updateStatus();
  }

  /**
   * Returns true if the device is currently running, otherwise false
   */
  public boolean isRunning() { return isRunning; }

  /**
   * Returns a string representing the current status of the device
   */
  public String getStatus() { return status; }

  /**
   * Sets the port being used by this device
   */
  public void setPort( int port ) { this.port = port; }

  /**
   * Returns the port being used by this device
   */
  public int getPort() { return port; }

  /**
   * Returns the text of the XML specification for this device
   */
  public String getSpec()
  {
    return TextResource.readToString(this.getClass(), getSpecFileName());
  }

  /**
   * Sends all the registered state listeners a complete list of states and
   * their current values.  This implementation relies on a Hashtable returned
   * by the abstract method getAllStates(), which should be implemented by
   * subclasses to contain key/value pairs representing states and their values.
   */
  public void requestFullState()
  {
    System.out.println("Full state update requested...");

    Hashtable states = getAllStates();
    Enumeration keys = states.keys();
    while (keys.hasMoreElements())
    {
      String state = (String) keys.nextElement();
      dispatchStateEvent(state, (String) states.get(state));
    }
  }

  /**
   * Adds the given StateListener to the list of listeners for this device
   */
  public void addStateListener(StateListener sl)
  {
    listeners.add(sl);
  }

  /**
   * Removes the given StateListener from the list of listeners for this device
   */
  public void removeStateListener(StateListener sl)
  {
    listeners.remove(sl);
  }

// Astract device methods

  public abstract void requestStateChange(String state, String value);

  public abstract void requestCommandInvoke(String command);

  public abstract String getName();

  public abstract void configure();

  public abstract boolean hasGUI();

  public abstract void setGUIVisibility(boolean isVisible);

  public abstract boolean isGUIVisible();

// Implemented utility methods

  /**
   * Updates the specified state/value pair on all registered StateListeners
   *
   * Note: Calling dispatchStateEvent(null, null) causes the status string
   * to update.  Yes, this is a random hack.  You should call updateStatus(),
   * if you want status to update, since it's obviously much clearer.
   *
   * @param state The name of the state variable to update
   * @param value The new value of state
   */
  protected void dispatchStateEvent(String state, String value)
  {
    StateListener sl;

    for (int i = 0; i < listeners.size(); i++)
    {
      sl = (StateListener) listeners.get(i);
      sl.stateChanged(getName(), state, value);
    }
  }

  /**
   * Updates the status on all registered StateListeners
   *
   * Note: The implementation of this method uses a hack in dispatchStateEvent.
   * For more information, see the javadoc for that method.
   */
  protected void updateStatus() { dispatchStateEvent(null, null); }

// Abstract utility methods

  /**
   * Accessor for all states, to be used in a requestFullState call.  The
   * returned Hashtable should contain the state string constants as keys, and
   * their values (converted to Strings if necessary) as values
   */
  protected abstract Hashtable getAllStates();

  /**
   * Should return a constant representing the default port to use
   */
  protected abstract int getDefaultPort();

  /**
   * Should return a constant String that contains the name of the specification
   * file for the device
   */
  protected abstract String getSpecFileName();

}