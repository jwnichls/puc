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

public class WMP9Device extends AbstractDevice
{

// Constants

  private static final int DEFAULT_PORT = 5157;
  private static final String SPEC_FILE = "WMP9DeviceSpec.xml";
  private static final String NAME = "Windows Media Player";

  // Device constants
  public static final String COMMAND_PREV_TRACK = "WMP9.Root.PlayControls.TrackManipulation.PrevTrack";
  public static final String COMMAND_NEXT_TRACK = "WMP9.Root.PlayControls.TrackManipulation.NextTrack";

  public static final String STATE_WMP_VERSION = "WMP9.Root.Info.WMPVersion";
  public static final String STATE_STATUS_STRING = "WMP9.Root.Info.StatusString";
  public static final String STATE_PLAYBACK_STATUS = "WMP9.Root.PlayControls.Mode";
  public static final String STATE_CURRENT_TITLE = "WMP9.Root.SongInfo.CurrentTitle";
  public static final String STATE_CURRENT_DURATION = "WMP9.Root.SongInfo.CurrentDuration";
  public static final String STATE_CURRENT_POSITION = "WMP9.Root.SongInfo.CurrentPosition";
  public static final String STATE_SHUFFLE = "WMP9.Root.MoreControls.Shuffle";
  public static final String STATE_LOOP = "WMP9.Root.MoreControls.Loop";
  public static final String STATE_VOLUME = "WMP9.Root.AudioControls.Volume";
  public static final String STATE_MUTE = "WMP9.Root.AudioControls.Mute";
  public static final String STATE_PLAY_AVAILABLE = "PlayAvailable";
  public static final String STATE_STOP_AVAILABLE = "StopAvailable";
  public static final String STATE_PAUSE_AVAILABLE = "PauseAvailable";
  public static final String STATE_NEXT_AVAILABLE = "WMP9.Root.PlayControls.TrackManipulationAvailable.NextAvailable";
  public static final String STATE_PREVIOUS_AVAILABLE = "WMP9.Root.PlayControls.TrackManipulationAvailable.PreviousAvailable";
  public static final String STATE_POSITION_AVAILABLE = "PositionAvailable";

  public static final int PLAYBACK_STOP = 1;
  public static final int PLAYBACK_PLAY = 2;
  public static final int PLAYBACK_PAUSE = 3;

// Instance variables

  private WMP9Connection connection;

// Constructor

  public WMP9Device()
  {
    connection = new WMP9Connection(this);
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
          commandByte = WMP9Connection.COMMAND_PLAY;
        else if (value.equals(String.valueOf(PLAYBACK_PAUSE)))
          commandByte = WMP9Connection.COMMAND_PAUSE;
        else if (value.equals(String.valueOf(PLAYBACK_STOP)))
          commandByte = WMP9Connection.COMMAND_STOP;

        if (commandByte != 0) 
	    connection.requestCommandInvoke(commandByte);
      }
      else if (state.equals(STATE_CURRENT_POSITION))
      {
        connection.setPosition(Integer.parseInt(value));
      }
      else if (state.equals(STATE_VOLUME))
      {
        connection.setVolume(Integer.parseInt(value));
      }
      else if (state.equals(STATE_MUTE))
      {
	connection.setMute(Boolean.valueOf(value).booleanValue());
      }
      else if (state.equals(STATE_SHUFFLE))
      {
        connection.setShuffle(Boolean.valueOf(value).booleanValue());
      }
      else if (state.equals(STATE_LOOP))
      {
	connection.setLoop(Boolean.valueOf(value).booleanValue());
      }
    }
  }

  public void requestCommandInvoke(String command)
  {
    if (command != null)
    {
      byte commandByte = 0;
      if (command.equals(COMMAND_PREV_TRACK))
        commandByte = WMP9Connection.COMMAND_PREV_TRACK;
      else if (command.equals(COMMAND_NEXT_TRACK))
        commandByte = WMP9Connection.COMMAND_NEXT_TRACK;

      if (commandByte != 0) connection.requestCommandInvoke(commandByte);
    }
  }

  public void requestFullState()
  {
    connection.requestFullState();
  }

  /**
   * This method is unused, since the requestFullState method has been
   * overridden to call WMP9Connection.requestFullState.  If getAllStates is
   * called, it will throw an UnsupportedOperationException.
   */
  protected Hashtable getAllStates()
  {
    throw new UnsupportedOperationException();
  }

}
