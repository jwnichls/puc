package edu.cmu.hcii.puc.devices;

import java.io.*;
import java.net.*;
import java.util.Hashtable;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class WMPConnection
{

// Socket communication constants

  private static final int WMP_SOCKET = 5665;
  private static final String WMP_HOST = "localhost";

// Constants for requests sent to the device

  private static final byte START_PACKET = 0x70;
  private static final byte END_PACKET = 0x71;

  public static final byte GET_WMP_VERSION = 0x01;
  public static final byte GET_PLAYBACK_STATUS = 0x02;
  public static final byte GET_CURRENT_TITLE = 0x03;
  public static final byte GET_CURRENT_LENGTH = 0x04;
  public static final byte GET_OUTPUT_TIME = 0x05;
  public static final byte GET_VOLUME = 0x06;

  public static final byte SET_OUTPUT_TIME = 0x15;
  public static final byte SET_VOLUME = 0x16;

  public static final byte COMMAND_PREV_TRACK = 0x21;
  public static final byte COMMAND_PLAY = 0x22;
  public static final byte COMMAND_PAUSE = 0x23;
  public static final byte COMMAND_STOP = 0X24;
  public static final byte COMMAND_NEXT_TRACK = 0x25;

  private static final byte GET_FULL_STATE = 0x31;

// Constants for updates from the device

  private static final byte STATE_WMP_VERSION = 0x01;
  private static final byte STATE_PLAYBACK_STATUS = 0x02;
  private static final byte STATE_CURRENT_TITLE = 0x03;
  private static final byte STATE_CURRENT_LENGTH = 0x04;
  private static final byte STATE_OUTPUT_TIME = 0x05;
  private static final byte STATE_VOLUME = 0x06;

// Constant values returned by Windows Media Player

  private static final int PLAYBACK_STOP = 0;
  private static final int PLAYBACK_PAUSE = 1;
  private static final int PLAYBACK_PLAY = 2;

// Instance variables

  private WMPDevice device;
  private Socket socket = null;
  private PrintStream ps = null;
  private InputStream in = null;

// Constructor

  public WMPConnection(WMPDevice device)
  {
    this.device = device;
  }

// Public methods

  public void start()
  {
    try
    {
      socket = new Socket(WMP_HOST, WMP_SOCKET);
      ps = new PrintStream(socket.getOutputStream());
      in = socket.getInputStream();
    }
    catch (Exception e)
    {
      System.err.println("Error creating socket and i/o streams");
      e.printStackTrace(System.err);
    }

    new Thread(){
      public void run()
      {
        byte inByte = 0;
        do
        {
          try
          {
            inByte = (byte)in.read();
            if (inByte == START_PACKET)
            {
              inByte = (byte)in.read();
              processBytes(inByte);
              inByte = (byte)in.read();
              if (inByte != END_PACKET) resync();
            }
          }
          catch (IOException ioEx)
          {
            System.err.println("Error reading data from socket");
            device.stop(WMPDevice.STATUS_ERROR);
          }
        } while(device.isRunning() && inByte != 0);
      }
    }.start();
  }

  public void stop()
  {
    try
    {
      socket.shutdownInput();
      socket.shutdownOutput();
      socket.close();
      System.err.println("Socket closed");
    }
    catch (IOException ioEx)
    {
      System.err.println("Error closing socket");
      ioEx.printStackTrace(System.err);
    }
    catch (NullPointerException npEx)
    {
      System.err.println("Cannot close socket; socket not initialized");
    }
  }

  public void requestFullState()
  {
    ps.write((int)START_PACKET);
    ps.write((int)GET_FULL_STATE);
    ps.write((int)END_PACKET);
  }

  public void requestCommandInvoke(byte command)
  {
    ps.write((int)START_PACKET);
    ps.write((int)command);
    ps.write((int)END_PACKET);
  }

  /**
   * Takes in the new position as a percentage value, from 0 to 100
   */
  public void setPosition(int newPosition)
  {
    ps.write((int)START_PACKET);
    ps.write((int)SET_OUTPUT_TIME);
    ps.write(newPosition);
    ps.write((int)END_PACKET);
  }

  /**
   * Takes in the new volume as a percentage value, from 0 to 100
   */
  public void setVolume(int newVolume)
  {
    if (newVolume >= 1 && newVolume <= 100)
    {
      ps.write((int)START_PACKET);
      ps.write((int)SET_VOLUME);
      ps.write((int)newVolume);
      ps.write((int)END_PACKET);
    }
    else throw new IllegalArgumentException("Illegal volume: " + newVolume);
  }

// Private helper methods

  private void processBytes(byte inByte)
  {
    switch(inByte)
    {
      case STATE_WMP_VERSION:
      {
        try
        {
          // Read in the number of characters in the version
          int numChars = in.read();

          // Read the given number of characters from the socket
          byte[] versionBytes = new byte[numChars];
          in.read(versionBytes);

          // Dispatch the returned characters as the version
          device.dispatchStateEvent(WMPDevice.STATE_WMP_VERSION,
            new String(versionBytes));
        }
        catch (IOException ioEx)
        {
          System.err.println("Error getting WMP Version");
          ioEx.printStackTrace(System.err);
          device.stop(WMPDevice.STATUS_ERROR);
        }
        break;
      }
      case STATE_PLAYBACK_STATUS:
      {
        try
        {
          // Read one byte to get the playback status
          int status = in.read();
          if (status == PLAYBACK_PLAY)
          {
            device.dispatchStateEvent(WMPDevice.STATE_PLAYBACK_STATUS,
              String.valueOf(WMPDevice.PLAYBACK_PLAY));
          }
          else if (status == PLAYBACK_PAUSE)
          {
            device.dispatchStateEvent(WMPDevice.STATE_PLAYBACK_STATUS,
              String.valueOf(WMPDevice.PLAYBACK_PAUSE));
          }
          else
          {
            device.dispatchStateEvent(WMPDevice.STATE_PLAYBACK_STATUS,
              String.valueOf(WMPDevice.PLAYBACK_STOP));
          }
        }
        catch (IOException ioEx)
        {
          System.err.println("Error getting playback status");
          ioEx.printStackTrace(System.err);
          device.stop(WMPDevice.STATUS_ERROR);
        }
        break;
      }
      case STATE_CURRENT_TITLE:
      {
        try
        {
          // Read in the number of characters in the title
          int numChars = in.read();

          // Read the given number of characters from the socket
          byte[] titleBytes = new byte[numChars];
          in.read(titleBytes);

          String title = new String(titleBytes);
          int lastSlashLoc = title.lastIndexOf('\\');
          if (lastSlashLoc >= 0)
          {
            title = title.substring(lastSlashLoc + 1);
          }

          // Dispatch the returned characters as the title
          device.dispatchStateEvent(WMPDevice.STATE_CURRENT_TITLE, title);
        }
        catch (IOException ioEx)
        {
          System.err.println("Error getting current title");
          ioEx.printStackTrace(System.err);
          device.stop(WMPDevice.STATUS_ERROR);
        }
        break;
      }
      case STATE_CURRENT_LENGTH:
      {
        try
        {
          // Read in the number of characters in the length of the current
          // song
          int numChars = in.read();

          // Read the given number of characters from the socket
          byte[] lengthBytes = new byte[numChars];
          in.read(lengthBytes);

          // Dispatch the returned characters as the song length
          device.dispatchStateEvent(WMPDevice.STATE_CURRENT_LENGTH,
            bytesToTime(lengthBytes));
        }
        catch (IOException ioEx)
        {
          System.err.println("Error getting current song length");
          ioEx.printStackTrace(System.err);
          device.stop(WMPDevice.STATUS_ERROR);
        }
        break;
      }
      case STATE_OUTPUT_TIME:
      {
        try
        {
          // Read in the current output time as a percentage
          int position = in.read();

          // Dispatch this position
          device.dispatchStateEvent(WMPDevice.STATE_OUTPUT_TIME,
            String.valueOf(position));
        }
        catch (IOException ioEx)
        {
          System.err.println("Error getting current output time");
          ioEx.printStackTrace(System.err);
          device.stop(WMPDevice.STATUS_ERROR);
        }
        break;
      }
      case STATE_VOLUME:
      {
        try
        {
          // Read in the volume
          int volume = in.read();

          // Dispatch this volume
          device.dispatchStateEvent(WMPDevice.STATE_VOLUME,
            String.valueOf(volume));
        }
        catch (IOException ioEx)
        {
          System.err.println("Error getting current volume");
          ioEx.printStackTrace(System.err);
          device.stop(WMPDevice.STATUS_ERROR);
        }
        break;
      }
      default:
      {
        System.err.println("Unrecognized state returned from WMP socket");
        // Do nothing
      }
    }
  }

  private void resync()
  {
    System.err.println("Synchronization error, reading bytes until next " +
      "end packet: ");

    // Clean up - read until END_PACKET is reached
    byte lastByte = 0;
    do
    {
      try
      {
        lastByte = (byte)in.read();
        System.err.print(Integer.toHexString((int)lastByte) + " ");
      }
      catch (IOException ioEx)
      {
        ioEx.printStackTrace(System.err);
      }
    } while (lastByte != END_PACKET);
    System.err.println("");
  }

  private String bytesToIntString(byte[] bytes)
  {
    String bytesAsString = new String(bytes);
    int value = (int)Double.parseDouble(bytesAsString);
    return String.valueOf(value);
  }

  private String bytesToTime(byte[] bytes)
  {
    String bytesAsString = new String(bytes);
    int value = (int)Double.parseDouble(bytesAsString);
    if (value < 0) return "No file";
    else
    {
      String time = "";
      if (value >= 60*60)
      {
        time += (value / (60*60)) + ":";
        value -= (value / (60*60)) * (60*60);
      }
      if (value >= 60)
      {
        time += (value / 60);
        value -= (value / 60) * 60;
      }
      time += ":" + (value < 10? "0" + value : "" + value);
      return time;
    }
  }

}