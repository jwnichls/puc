/**
 * WinampDevice.java
 *
 * This is a server-side object for a device that controls the Winamp
 * media player.
 *
 * @author Kevin Litwack
 */

package edu.cmu.hcii.puc.devices;

import java.util.Hashtable;

// Class Definition

public class WinampDevice extends AbstractDevice
{

// Constants

  // Device constants
  private static final String SPEC_FILE = "WinampSpec.xml";
  private static final String LIB_NAME = "winamp";
  private static final String DEVICE_NAME = "Winamp";
  private static final int DEFAULT_PORT = 5156;

  // State/Command constants
  public static final String STATE_WINAMP_VERSION = "WinampVersion";
  public static final String STATE_PLAYLIST_LENGTH = "PlaylistLength";
  public static final String STATE_PLAYBACK_STATUS = "PlaybackStatus";
  public static final String STATE_CURRENT_TITLE = "CurrentTitle";
  public static final String STATE_OUTPUT_TIME = "OutputTime";
  public static final String STATE_SONG_LENGTH = "SongLength";
  public static final String STATE_POS_IN_PLAYLIST = "PosInPlaylist";
  public static final String STATE_IS_SHUFFLING = "IsShuffling";
  public static final String STATE_IS_LOOPING = "IsLooping";
  public static final String COMMAND_PREV_TRACK = "PrevTrack";
  public static final String COMMAND_PLAY = "Play";
  public static final String COMMAND_PAUSE = "Pause";
  public static final String COMMAND_STOP_PLAY = "StopPlay";
  public static final String COMMAND_NEXT_TRACK = "NextTrack";
  public static final String COMMAND_VOLUME_UP = "VolumeUp";
  public static final String COMMAND_VOLUME_DOWN = "VolumeDown";

  // Winamp info/control constants
  public static final int MODE_PLAY = 1;
  public static final int MODE_PAUSE = 2;
  public static final int MODE_STOP = 3;

// Static initialization to load the required .dll file

  static
  {
    try
    {
      System.loadLibrary(LIB_NAME);
    }
    catch (UnsatisfiedLinkError ule)
    {
      System.err.println("ERROR: Failed to load dll from path!");
      ule.printStackTrace();
      System.exit(1);
      // Could attempt to load from within current jar file here
    }
  }

// Main method - for testing

  public static void main(String[] args)
  {
    WinampDevice wd = new WinampDevice();
    wd.play();
  }

// Constructors

  public WinampDevice()
  {
    super();
  }

// Device methods

  /**
   * These are empty implementations; they can be ignored
   */
  public void configure() { }
  public boolean hasGUI() { return false; }
  public void setGUIVisibility(boolean isVisible) { }
  public boolean isGUIVisible() { return false; }

  /**
   * These return constants necessary for device initialization
   */
  public String getName() { return DEVICE_NAME; }
  protected int getDefaultPort() { return DEFAULT_PORT; }
  protected String getSpecFileName() { return SPEC_FILE; }

  /**
   * Overrides the start method to include starting an update thread
   */
  public void start()
  {
    super.start();
    new WinampUpdateThread(this).start();
  }

  /**
   * Changes the specified state to the specified value
   *
   * @param state Should be one of STATE_POS_IN_PLAYLIST, STATE_OUTPUT_TIME,
   * STATE_IS_SHUFFLING, or STATE_IS_LOOPING.
   */
  public void requestStateChange(String state, String value)
  {
    if (state != null)
    {
      if (state.equals(STATE_POS_IN_PLAYLIST))
        setPlaylistPos(Integer.parseInt(value));
      else if (state.equals(STATE_OUTPUT_TIME))
        seek(Integer.parseInt(value));
      else if (state.equals(STATE_IS_SHUFFLING))
      {
        if (isShuffling() != Boolean.valueOf(value).booleanValue())
          toggleShuffle();
      }
      else if (state.equals(STATE_IS_LOOPING))
      {
        if (isLooping() != Boolean.valueOf(value).booleanValue())
          toggleLoop();
      }
    }
  }

  /**
   * Invokes the specified command
   *
   * @param command Should be one of COMMAND_PREV_TRACK, COMMAND_PLAY,
   * COMMAND_PAUSE, COMMAND_STOP_PLAY, COMMAND_NEXT_TRACK, COMMAND_VOLUME_UP,
   * or COMMAND_VOLUME_DOWN.
   */
  public void requestCommandInvoke(String command)
  {
    if (command != null)
    {
      if (command.equals(COMMAND_PREV_TRACK))
        prevTrack();
      else if (command.equals(COMMAND_PLAY))
        play();
      else if (command.equals(COMMAND_PAUSE))
        pause();
      else if (command.equals(COMMAND_STOP_PLAY))
        stopPlay();
      else if (command.equals(COMMAND_NEXT_TRACK))
        nextTrack();
      else if (command.equals(COMMAND_VOLUME_UP))
        volumeUp();
      else if (command.equals(COMMAND_VOLUME_DOWN))
        volumeDown();
      else
        System.out.println("Unknown command: " + command);
    }
  }

// Private Helper Methods

  /**
   * Accessor for all states, to be used in a requestFullState call.  The
   * returned Hashtable contains the state string constants as keys, and
   * their values (converted to Strings if necessary) as values
   */
  protected Hashtable getAllStates()
  {
    Hashtable states = new Hashtable();
    states.put(STATE_WINAMP_VERSION, stringWinampVersion());
    states.put(STATE_PLAYLIST_LENGTH, String.valueOf(playlistLength()));
    states.put(STATE_PLAYBACK_STATUS, String.valueOf(playbackStatus()));
    states.put(STATE_CURRENT_TITLE, currentTitle());
    states.put(STATE_OUTPUT_TIME, String.valueOf(outputTime()));
    states.put(STATE_SONG_LENGTH, String.valueOf(songLength()));
    states.put(STATE_POS_IN_PLAYLIST, String.valueOf(posInPlaylist()));
    states.put(STATE_IS_SHUFFLING, String.valueOf(isShuffling()));
    states.put(STATE_IS_LOOPING, String.valueOf(isLooping()));
    return states;
  }

  /**
   * Calls the native winampVersion method, and converts the returned
   * integer into a pretty String
   */
  private String stringWinampVersion()
  {
    // @todo Finish this implementation
    return String.valueOf(winampVersion());
  }

// Native Methods

  /************************* Play Control *************************/

  protected native void prevTrack();
  protected native void play();
  protected native void pause();
  protected native void stopPlay();
  protected native void nextTrack();
  protected native void stopAfterCurrent();
  protected native void fadeStop();

  /************************* Other Control *************************/

  protected native void volumeUp();
  protected native void volumeDown();
  protected native void setVolume(int newVolume); // From 0 to 255
  protected native void toggleShuffle();
  protected native void toggleLoop();
  protected native void seek(int millis);
  protected native void setPlaylistPos(int pos);
  protected native void setPanning(int panning); // 0 (left) to 255 (right)

  /************************* Static information *************************/

  /**
   * Returns the version in a weird format.  According to winamp doc:
   *
   * "Version will be 0x20yx for winamp 2.yx. versions previous to Winamp 2.0
   * typically (but not always) use 0x1zyx for 1.zx versions. Weird, I know."
   */
  protected native int winampVersion();

  protected native int playlistLength();

  /************************* Non-Static information *************************/

  /**
   * Returns MODE_PLAY, MODE_PAUSE, or MODE_STOP
   */
  protected native int playbackStatus();

  protected native String currentTitle();

  /**
   * Returns the current output time in milliseconds
   */
  protected native int outputTime();

  /**
   * Returns the length of the song in milliseconds
   */
  protected native int songLength();

  protected native int posInPlaylist();

  protected native boolean isShuffling();
  protected native boolean isLooping();

}