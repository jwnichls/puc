package edu.cmu.hcii.puc.devices;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class WinampUpdateThread extends Thread
{

// Instance variables

  private WinampDevice device;

  // State holders
  private String currentTitle = "";
  private boolean isLooping = false;
  private boolean isShuffling = false;
  private int outputTime = 0;
  private int playbackStatus = WinampDevice.MODE_STOP;
  private int posInPlaylist = 0;
  private int songLength = 0;

// Constructors

  public WinampUpdateThread(WinampDevice device)
  {
    super();
    this.device = device;
  }

// Runnable method

  public void run()
  {
    System.out.println("Starting update thread run() method...");
    while (device.isRunning())
    {
      String newTitle = device.currentTitle();
      boolean newLooping = device.isLooping();
      boolean newShuffling = device.isShuffling();
      int newOutputTime = device.outputTime();
      int newPlaybackStatus = device.playbackStatus();
      int newPosInPlaylist = device.posInPlaylist();
      int newSongLength = device.songLength();

      if (newTitle != null && !newTitle.equals(currentTitle))
      {
        currentTitle = newTitle;
        device.dispatchStateEvent(WinampDevice.STATE_CURRENT_TITLE,
                                  currentTitle);
      }

      if (newLooping != isLooping)
      {
        isLooping = newLooping;
        device.dispatchStateEvent(WinampDevice.STATE_IS_LOOPING,
                                  String.valueOf(isLooping));
      }

      if (newShuffling != isShuffling)
      {
        isShuffling = newShuffling;
        device.dispatchStateEvent(WinampDevice.STATE_IS_SHUFFLING,
                                  String.valueOf(isShuffling));
      }

      if (newOutputTime != outputTime)
      {
        outputTime = newOutputTime;
        device.dispatchStateEvent(WinampDevice.STATE_OUTPUT_TIME,
                                  String.valueOf(outputTime));
      }

      if (newPlaybackStatus != playbackStatus)
      {
        playbackStatus = newPlaybackStatus;
        device.dispatchStateEvent(WinampDevice.STATE_PLAYBACK_STATUS,
                                  String.valueOf(playbackStatus));
      }

      if (newPosInPlaylist != posInPlaylist)
      {
        posInPlaylist = newPosInPlaylist;
        device.dispatchStateEvent(WinampDevice.STATE_POS_IN_PLAYLIST,
                                  String.valueOf(posInPlaylist));
      }

      if (newSongLength != songLength)
      {
        songLength = newSongLength;
        device.dispatchStateEvent(WinampDevice.STATE_SONG_LENGTH,
                                  String.valueOf(songLength));
      }

      try
      {
        sleep(1000);
      }
      catch (InterruptedException iEx)
      {
        iEx.printStackTrace();
      }
    }
  }

}