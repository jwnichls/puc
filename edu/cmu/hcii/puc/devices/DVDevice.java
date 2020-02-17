package edu.cmu.hcii.puc.devices;

/**
 * <p>Title: DV (Digital Video) Device class for PUC interface</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * @author klitwack
 * @version 1.0
 */

import com.maya.puc.common.Device;
import com.maya.puc.common.PUCServer;
import com.maya.puc.common.StateListener;
import com.maya.puc.common.TextResource;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class DVDevice implements Device
{

// Constants

  /* Device Constants */
  public static final String SPEC_FILE = "DVDeviceSpec.xml";
  public static final String DEVICE_NAME = "DV Device";
  public static final int DEFAULT_PORT = 5155;
  public static final String STATUS_INACTIVE = "Idle";
  public static final String STATUS_ACTIVE = "Active";
  public static final String STATUS_ERROR = "Error";

  /* State Name Constants */
  public static final String STATE_POWER = "DVDevice.Root.PowerState";
  public static final String STATE_DEVICE_MODE = "DVDevice.Root.PoweredItems.Info.DeviceModeState";
  public static final String STATE_MEDIA = "DVDevice.Root.PoweredItems.Info.MediaState";
  public static final String STATE_AVC_VERSION = "DVDevice.Root.PoweredItems.Info.AVCVersionState";
  public static final String STATE_CAPTURE_FRAME_RATE =
    "DVDevice.Root.PoweredItems.Info.CaptureFrameRateState";
  public static final String STATE_PORT = "DVDevice.Root.PoweredItems.Info.PortState";
  public static final String STATE_TIMECODE = "DVDevice.Root.PoweredItems.Control.TimecodeState";
  public static final String STATE_TRANSPORT_MODE = "DVDevice.Root.PoweredItems.Control.TransportModeState";
  public static final String STATE_ACTIVE_PAGE = "DVDevice.Root.PoweredItems.ActivePageState";

  /* Command Name Constants */
  public static final String COMMAND_STEP_FORWARD = "DVDevice.Root.PoweredItems.Control.StepControls.StepForward";
  public static final String COMMAND_STEP_BACK = "DVDevice.Root.PoweredItems.Control.StepControls.StepBack";
  public static final String COMMAND_REFRESH = "Refresh";

// Instance variables

  private Listeners listeners;
  private boolean isRunning;
  private int port;
  private String status;
  private ActiveDevice device;

// Constructors

  public DVDevice()
  {
    // Initialization
    listeners = new Listeners(getName());
    isRunning = false;
    port = DEFAULT_PORT;
    status = STATUS_INACTIVE;
    device = new ActiveDevice(new DVCamera());
  }

// Device methods

  /**
   * Get the human-readable name of the device supported by this class.
   */
  public String getName() { return DEVICE_NAME; }

  /**
   * Get the XML spec used to generate the interface for this device.
   */
  public String getSpec()
  {
    return TextResource.readToString(this.getClass(), SPEC_FILE);
  }

  /**
   * Request that the full device state be sent out.
   */
  public void requestFullState()
  {
    System.out.println("Full state update requested...");
    if (device == null) return;

    Hashtable states = device.getAllStates();
    Enumeration keys = states.keys();
    while (keys.hasMoreElements())
    {
      String state = (String) keys.nextElement();
      listeners.dispatchStateEvent(state, (String) states.get(state));
    }
  }

  /**
   * Request that certain state be changed.
   *
   * @param state A state name specified in the spec.
   * @param value The string value to change the named state to.
   */
  public void requestStateChange(String state, String value)
  {
    if (device != null && state != null)
    {
      if (state.equals(STATE_TRANSPORT_MODE))
      {
        try
        {
          device.setTransportMode(Integer.parseInt(value));
          listeners.dispatchStateEvent(state, value);
        }
        catch (DeviceException dEx)
        {
          System.err.println("ERROR: " + dEx.getMessage());
        }
      }
      else if (state.equals(STATE_ACTIVE_PAGE))
      {
        device.setActivePage(Integer.parseInt(value));
        listeners.dispatchStateEvent(state, value);
      }
    }
  }

  /**
   * Request that a certain command be sent to the device.
   *
   * @param command A command name, as specified in the spec.
   */
  public void requestCommandInvoke(String command)
  {
    try
    {
      if (command.equals(COMMAND_STEP_FORWARD))
      {
        device.stepForward();
      }
      else if (command.equals(COMMAND_STEP_BACK))
      {
        device.stepBack();
      }
      /**
       * The refresh command has been removed from the spec; to re-add it,
       * uncomment this code
       *
      else if (command.equals(COMMAND_REFRESH))
      {
        device.updateStatic();
        requestFullState();
      } */
      else
        System.out.println("Unknown command: " + command);
    }
    catch (DeviceException dEx)
    {
      System.err.println("ERROR: " + dEx.getMessage());
    }
  }

  /**
   * Register a StateListener to receive state change notifications.
   */
  public void addStateListener(StateListener sl)
  {
    listeners.addElement(sl);
  }

  /**
   * Remove a StateListener from the notification list.
   */
  public void removeStateListener(StateListener sl)
  {
    listeners.removeElement(sl);
  }

  /**
   * Take steps to configure the device (i.e. pop up a configuration dialog).
   */
  public void configure()
  {
    System.out.println("DVDevice.configure() called; this method does nothing");
  }

  /**
   * Determine whether the device has a (non-configuration) GUI.
   */
  public boolean hasGUI() { return false; }

  /**
   * Specify whether the device GUI should be visible on the screen.
   */
  public void setGUIVisibility(boolean isVisible)
  {
    System.out.println("DVDevice.setGUIVisibility() called; this method " +
      "does nothing");
  }

  /**
   * Determine whether the device's GUI is visible on the screen.
   */
  public boolean isGUIVisible() { return false; }

  /**
   * Establish a connection with the device, and begin reporting state.
   */
  public void start()
  {
    try
    {
      device.updateAll();
      status = STATUS_ACTIVE;
      isRunning = true;
      listeners.updateStatus();
      listeners.setActive(true);
      new DVUpdateThread(this, listeners).start();
    }
    catch (DeviceException dEx)
    {
      System.err.println("ERROR: " + dEx.getMessage());
      isRunning = false;
      status = STATUS_ERROR;
    }
  }

  /**
   * Stop generating state updates; set status to a default inactive string
   */
  public void stop()
  {
    stop(STATUS_INACTIVE);
  }

  /**
   * Stop generating state updates; set status to the given string
   */
  public void stop(String status)
  {
    this.status = status;
    isRunning = false;
    listeners.updateStatus();
    listeners.setActive(false);
  }

  /**
   * Determine whether the device is currently active.
   */
  public boolean isRunning() { return isRunning; }

  /**
   * Get the current status of this device.  Useful for reporting
   * connection errors.  Incidentally, to notify the PUCProxy program
   * that the status string has changed, send a state update
   * with state and value set to null to the StateListeners.
   */
  public String getStatus() { return status; }

  /**
   * Set the port that clients will connect to in order to
   * access this device.
   */
  public int getPort() { return port; }

  /**
   * Retrieve the number of the port that PUC clients will
   * connect to in order to access this device.
   */
  public void setPort(int port) { this.port = port; }

  /**
   * Get a reference to the active device object
   */
  public ActiveDevice getActiveDevice() { return device; }

// Private Inner class containing state information

  public static class ActiveDevice
  {

  // Instance variables

    // State holders
    private DVCamera dvc;
    private boolean power = false;
    private boolean deviceMode = false;
    private int media = DVCamera.MEDIA_NONE;
    private String avcVersion = "";
    private float frameRate = (float)0.0;
    private String port = "";
    private String timecode = "";
    private int transportMode = DVCamera.T_MODE_STOP;
    private int activePage = 1; // Control page

  // Constructor

    /**
     * Constructs an ActiveDevice for this DVDevice given a DVCamera object
     * to query for state information
     */
    public ActiveDevice(DVCamera dvc)
    {
      this.dvc = dvc;
    }

  // Public State Accessor Methods

    /**
     * Accessor for all states, to be used in a requestFullState call.  The
     * returned Hashtable contains the state string constants as keys, and
     * their values (converted to Strings if necessary) as values
     */
    public Hashtable getAllStates()
    {
      Hashtable states = new Hashtable();
      states.put(DVDevice.STATE_POWER, String.valueOf(power));
      states.put(DVDevice.STATE_DEVICE_MODE, String.valueOf(deviceMode));
      states.put(DVDevice.STATE_MEDIA, String.valueOf(media));
      states.put(DVDevice.STATE_AVC_VERSION, avcVersion);
      states.put(DVDevice.STATE_CAPTURE_FRAME_RATE, String.valueOf(frameRate));
      states.put(DVDevice.STATE_PORT, port);
      states.put(DVDevice.STATE_TIMECODE, timecode);
      states.put(DVDevice.STATE_TRANSPORT_MODE, String.valueOf(transportMode));
      states.put(DVDevice.STATE_ACTIVE_PAGE, String.valueOf(activePage));
      return states;
    }

    // Individual accessors

    public boolean getPower() { return power; }
    public boolean getDeviceMode() { return deviceMode; }
    public int getMedia() { return media; }
    public String getAvcVersion() { return avcVersion; }
    public float getFrameRate() { return frameRate; }
    public String getPort() { return port; }
    public String getTimecode() { return timecode; }
    public int getTransportMode() { return transportMode; }

  // Commands

    public void setTransportMode(int state) throws DeviceException
      { dvc.setTransportMode(state); }

    public void setActivePage(int activePage)
      { this.activePage = activePage; }

    public void stepForward() throws DeviceException
      { dvc.stepForward(); }

    public void stepBack() throws DeviceException
      { dvc.stepBack(); }

  // Update Methods

    public void updateAll() throws DeviceException
    {
      dvc.connect();
      updateStatic();
      updateNonStatic();
    }

    public void updateStatic() throws DeviceException
    {
      power = dvc.isPowerOn();
      media = dvc.getMedia();
      avcVersion = dvc.getAVCVersion();
      frameRate = dvc.getFrameRate();
      port = dvc.getPort();
    }

    public void updateNonStatic() throws DeviceException
    {
      timecode = dvc.getTimecode();
      transportMode = dvc.getTransportMode();
      deviceMode = dvc.getDeviceMode();
    }

  } // End inner class ActiveDevice

}
