package edu.cmu.hcii.puc.devices;

import java.util.Hashtable;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class WMPDevice extends AbstractDevice
{

// Constants

  private static final int DEFAULT_PORT = 5157;
  private static final String SPEC_FILE = "WMPDeviceSpec.xml";
  private static final String NAME = "Windows Media Player";

  // Device constants
  public static final String COMMAND_PREV_TRACK = "PrevTrack";
  public static final String COMMAND_PLAY = "Play";
  public static final String COMMAND_PAUSE = "Pause";
  public static final String COMMAND_STOP = "StopPlay";
  public static final String COMMAND_NEXT_TRACK = "NextTrack";

  public static final String STATE_WMP_VERSION = "WMPVersion";
  public static final String STATE_PLAYBACK_STATUS = "PlaybackStatus";
  public static final String STATE_CURRENT_TITLE = "CurrentTitle";
  public static final String STATE_CURRENT_LENGTH = "CurrentLength";
  public static final String STATE_OUTPUT_TIME = "OutputTime";
  public static final String STATE_VOLUME = "Volume";

  public static final int PLAYBACK_PLAY = 1;
  public static final int PLAYBACK_PAUSE = 2;
  public static final int PLAYBACK_STOP = 3;

// Instance variables

  private WMPConnection connection;

// Constructor

  public WMPDevice()
  {
    connection = new WMPConnection(this);
  }

// Constant methods

  protected int getDefaultPort() { return DEFAULT_PORT; }
  protected String getSpecFileName() { return SPEC_FILE; }
  public String getName(){ return NAME; }
  public boolean hasGUI() { return false; }
  public boolean isGUIVisible() { return false; }
  public void setGUIVisibility(boolean isVisible) { }
  public void configure() { }

// Device methods

  /**
   * Override AbstractDevice.start() to perform socket initialization
   */
  public void start()
  {
    connection.start();
    super.start();
    requestFullState();
  }

  /**
   * Override AbstractDevice.stop() to close the socket connection
   */
  public void stop()
  {
    connection.stop();
    super.stop();
    /** @todo: finish implementation */
  }

  public void requestStateChange(String state, String value)
  {
    if (state != null && value != null)
    {
      if (state.equals(STATE_PLAYBACK_STATUS))
      {
        byte commandByte = 0;
        if (value.equals(String.valueOf(PLAYBACK_PLAY)))
          commandByte = WMPConnection.COMMAND_PLAY;
        else if (value.equals(String.valueOf(PLAYBACK_PAUSE)))
          commandByte = WMPConnection.COMMAND_PAUSE;
        else if (value.equals(String.valueOf(PLAYBACK_STOP)))
          commandByte = WMPConnection.COMMAND_STOP;
        if (commandByte != 0) connection.requestCommandInvoke(commandByte);
      }
      else if (state.equals(STATE_OUTPUT_TIME))
      {
        connection.setPosition(Integer.parseInt(value));
      }
      else if (state.equals(STATE_VOLUME))
      {
        connection.setVolume(Integer.parseInt(value));
      }
    }
  }

  public void requestCommandInvoke(String command)
  {
    if (command != null)
    {
      byte commandByte = 0;
      if (command.equals(COMMAND_PREV_TRACK))
        commandByte = WMPConnection.COMMAND_PREV_TRACK;
      else if (command.equals(COMMAND_PLAY))
        commandByte = WMPConnection.COMMAND_PLAY;
      else if (command.equals(COMMAND_STOP))
        commandByte = WMPConnection.COMMAND_STOP;
      else if (command.equals(COMMAND_PAUSE))
        commandByte = WMPConnection.COMMAND_PAUSE;
      else if (command.equals(COMMAND_NEXT_TRACK))
        commandByte = WMPConnection.COMMAND_NEXT_TRACK;

      if (commandByte != 0) connection.requestCommandInvoke(commandByte);
    }
  }

  public void requestFullState()
  {
    connection.requestFullState();
  }

  /**
   * This method is unused, since the requestFullState method has been
   * overridden to call WMPConnection.requestFullState.  If getAllStates is
   * called, it will throw an UnsupportedOperationException.
   */
  protected Hashtable getAllStates()
  {
    throw new UnsupportedOperationException();
  }

}