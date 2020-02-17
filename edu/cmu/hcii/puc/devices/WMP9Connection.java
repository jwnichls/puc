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

public class WMP9Connection
{

// Socket communication constants

  private static final int WMP_SOCKET = 5250;
  private static final String WMP_HOST = "localhost";

// Constants for requests sent to the device

  private static final byte END_PACKET = (byte)'\n';

  public static final byte SET_OUTPUT_TIME = (byte)'o';
  public static final byte SET_VOLUME = (byte)'v';
  public static final byte SET_MUTE = (byte)'m';
	public static final byte SET_SHUFFLE = (byte)'h';
	public static final byte SET_LOOP = (byte)'l';

  public static final byte COMMAND_PREV_TRACK = (byte)',';
  public static final byte COMMAND_PLAY = (byte)'p';
  public static final byte COMMAND_PAUSE = (byte)'d';
  public static final byte COMMAND_STOP = (byte)'s';
  public static final byte COMMAND_NEXT_TRACK = (byte)'.';

  private static final byte GET_FULL_STATE = (byte)'f';

// Constants for updates from the device

  private static final String STATE_WMP_VERSION = "version";
  private static final String STATE_STATUS_STRING = "status";
  private static final String STATE_PLAYBACK_STATUS = "playstatus";
  private static final String STATE_CURRENT_TITLE = "title";
  private static final String STATE_CURRENT_LENGTH = "len";
  private static final String STATE_OUTPUT_TIME = "pos";
  private static final String STATE_VOLUME = "vol";
  private static final String STATE_MUTE = "mute";
	private static final String STATE_LOOP = "loop";
  private static final String STATE_SHUFFLE = "shuffle";
  private static final String STATE_PLAY_AVAILABLE = "playAvail";
  private static final String STATE_STOP_AVAILABLE = "stopAvail";
  private static final String STATE_PAUSE_AVAILABLE = "pauseAvail";
  private static final String STATE_NEXT_AVAILABLE = "nextAvail";
  private static final String STATE_PREVIOUS_AVAILABLE = "prevAvail";
  private static final String STATE_POSITION_AVAILABLE = "posAvail";

// Constant values returned by Windows Media Player

  private static final int PLAYBACK_STOP = 1;
  private static final int PLAYBACK_PLAY = 2;
  private static final int PLAYBACK_PAUSE = 3;

// Instance variables

  private WMP9Device device;
  private Socket socket = null;
  private PrintStream ps = null;
  private BufferedReader in = null;

// Constructor

  public WMP9Connection(WMP9Device device)
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
      in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
    }
    catch (Exception e)
    {
      System.err.println("Error creating socket and i/o streams");
      e.printStackTrace(System.err);
    }

    new Thread(){
      public void run()
      {
	String inLine = null;
        do
        {
          try
          {
            inLine = in.readLine();
	    processLine(inLine);
          }
          catch (IOException ioEx)
          {
            System.err.println("Error reading data from socket");
            device.stop(WMP9Device.STATUS_ERROR);
          }
        } while(device.isRunning() && inLine != null);
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
    ps.write((int)GET_FULL_STATE);
    ps.write((int)END_PACKET);
  }

  public void requestCommandInvoke(byte command)
  {
      System.out.println( "Sending command: " + command );
      System.out.println( "Play Command: " + COMMAND_PLAY );
    ps.write((int)command);
    ps.write((int)END_PACKET);
  }

  /**
   * Takes in the new position as a percentage value, from 0 to 100
   */
  public void setPosition(int newPosition)
  {
    ps.write((int)SET_OUTPUT_TIME);
    ps.print(newPosition);
    ps.write((int)END_PACKET);
  }

  /**
   * Takes in the new volume as a percentage value, from 0 to 100
   */
  public void setVolume(int newVolume)
  {
    if (newVolume >= 1 && newVolume <= 100)
    {
      ps.write((int)SET_VOLUME);
      ps.write((int)newVolume);
      ps.write((int)END_PACKET);
    }
    else throw new IllegalArgumentException("Illegal volume: " + newVolume);
  }

  /**
   * Takes in the new mute settings as a boolean value
   */
  public void setMute(boolean newMute) 
  {    
    ps.write((int)SET_MUTE);
    ps.write(newMute? 1 : 0);
    ps.write((int)END_PACKET);
  } 

  /**
   * Takes in the new shuffle settings as a boolean value
   */
  public void setShuffle(boolean newShuffle) 
  {    
    ps.write((int)SET_SHUFFLE);
    ps.write(newShuffle? 1 : 0);
    ps.write((int)END_PACKET);
  } 

  /**
   * Takes in the new loop settings as a boolean value
   */
  public void setLoop(boolean newLoop) 
  {    
    ps.write((int)SET_LOOP);
    ps.write(newLoop? 1 : 0);
    ps.write((int)END_PACKET);
  } 

// Private helper methods

  private void processLine(String line)
  {
    System.out.println( "Received line=" + line );
    // separate into name:value pair
    int sepidx = line.indexOf(':');  

    if ( sepidx < 0 )
      return;

    String name = line.substring( 0, sepidx );
    String value = line.substring( sepidx+1 );

    if ( name.equals( STATE_WMP_VERSION ) ) { 
      device.dispatchStateEvent( WMP9Device.STATE_WMP_VERSION, value );
    }
    else if ( name.equals( STATE_STATUS_STRING ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_STATUS_STRING, value );
    }
    else if ( name.equals( STATE_PLAYBACK_STATUS ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_PLAYBACK_STATUS, value );
    }
    else if ( name.equals( STATE_CURRENT_TITLE ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_CURRENT_TITLE, value );
    }
    else if ( name.equals( STATE_CURRENT_LENGTH ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_CURRENT_DURATION, value );
    }
    else if ( name.equals( STATE_OUTPUT_TIME ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_CURRENT_POSITION, value );
    }
    else if ( name.equals( STATE_VOLUME ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_VOLUME, value );
    }
    else if ( name.equals( STATE_MUTE ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_MUTE, value );
    }
		else if ( name.equals( STATE_SHUFFLE ) ) {
			device.dispatchStateEvent( WMP9Device.STATE_SHUFFLE, value );
		}
		else if ( name.equals( STATE_LOOP ) ) {
			device.dispatchStateEvent( WMP9Device.STATE_LOOP, value );
		}
    /*
    else if ( name.equals( STATE_PLAY_AVAILABLE ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_PLAY_AVAILABLE, value );
    }
    else if ( name.equals( STATE_STOP_AVAILABLE ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_STOP_AVAILABLE, value );
    }
    else if ( name.equals( STATE_PAUSE_AVAILABLE ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_PAUSE_AVAILABLE, value );
    }
    */
    else if ( name.equals( STATE_NEXT_AVAILABLE ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_NEXT_AVAILABLE, value );
    }
    else if ( name.equals( STATE_PREVIOUS_AVAILABLE ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_PREVIOUS_AVAILABLE, value );
    }
    /*
    else if ( name.equals( STATE_POSITION_AVAILABLE ) ) {
      device.dispatchStateEvent( WMP9Device.STATE_POSITION_AVAILABLE, value );
    }
    */
  }
}

