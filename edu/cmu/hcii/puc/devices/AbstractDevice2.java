package edu.cmu.hcii.puc.devices;

/**
 * <p>Title: Abstract Device class for PUC interface</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author klitwack
 * @version 1.0
 */

// Import Declarations

import com.maya.puc.common.Device2;
import com.maya.puc.common.Message;
import com.maya.puc.common.PUCServer;
import com.maya.puc.common.StatusListener;
import com.maya.puc.common.TextResource;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Abstract class providing implementations of common functionality for the
 * Device interface
 */
public abstract class AbstractDevice2 implements Device2
{

// Device constants

  /**
   * These constants are just utilities to provide common status values
   */
  public static final String STATUS_INACTIVE = "Idle";
  public static final String STATUS_ACTIVE = "Active";
  public static final String STATUS_ERROR = "Error";

// Instance variables

  protected ArrayList connections;
  protected Hashtable connectionData;
  private ArrayList statusListeners;
  private boolean isRunning;
  private String status;
  private int port;

// Constructor

  /**
   * Performs necessary initialization for device instance variables
   */
  public AbstractDevice2()
  {
    connections = new ArrayList();
    connectionData = new Hashtable();
    statusListeners = new ArrayList();
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
    this.status = status;
    if (! isRunning)
    {
      isRunning = true;
      updateStatus(true);
    }
    else
      updateStatus(false);
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
    this.status = status;
    if (isRunning)
    {
      isRunning = false;
      updateStatus(true);
    }
    else
      updateStatus(false);
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
   * Adds the given Connection to the list of connections for this device
   */
  public void addConnection(PUCServer.Connection c)
  {
    connections.add(c);
  }

  /**
   * Removes the given Connection from the list of connections for this device
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
   * Adds the given StatusListener to the list of status listeners for this device
   */
  public void addStatusListener(StatusListener sl)
  {
    statusListeners.add(sl);
  }

  /**
   * Removes the given StatusListener from the list of status listeners for this device
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

// Astract device methods

  public abstract void handleMessage( PUCServer.Connection c, Message m );

  public abstract String getName();

  public abstract void configure();

  /**
   * Default is to assume devices are manually activatable
   */
  public boolean isManuallyActivatable() { return true; }

  /**
   * Default is to assume devices do not have a GUI
   */
  public boolean hasGUI() { return false; }

  public void setGUIVisibility(boolean isVisible) { }

  public boolean isGUIVisible() { return false; }

// Implemented utility methods

  /**
   * Updates the specified state/value pair on all connections
   *
   * @param state The name of the state variable to update
   * @param value The new value of state
   */
  protected void dispatchStateEvent(String state, String value)
  {
    sendAll( new Message.StateChangeNotification( state, value ) );
  }

  /**
   * Sends the specified message to all connections.
   */
  protected void sendAll( Message msg ) {
    PUCServer.Connection c;

    for (int i = 0; i < connections.size(); i++)
    {
      c = (PUCServer.Connection) connections.get(i);

      try {
        c.send( msg );
      }
      catch( Exception e ) { }
    }
  }


  /**
   * Updates the status on all registered StateListeners
   *
   * Note: The implementation of this method uses a hack in dispatchStateEvent.
   * For more information, see the javadoc for that method.
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

// Abstract utility methods

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
