package edu.cmu.hcii.puc.devices.dollhouse;

import edu.cmu.hcii.puc.devices.DeviceException;

import java.io.*;
import java.net.*;

/**
 * Control to handle getting/sending of messages from/to the Lutron RadioRA
 * Dollhouse (through the Lantronix UDS-10).
 */
public class DollhouseControl
{

  // Constants

  public static final int PORT = 12321;
  public static final int CONFIG_PORT = 9999;

  public static final int ROOM_FOYER = 4;
  public static final int ROOM_KITCHEN = 5;
  public static final int ROOM_MEDIA_ROOM = 1;
  public static final int ROOM_MAIN_BEDROOM = 3;
  public static final int ROOM_FLOODS = 2;

  public static final int SCENE_WELCOME = 6;
  public static final int SCENE_AWAY = 7;
  public static final int SCENE_PATHWAY = 8;
  public static final int SCENE_MORNING = 9;
  public static final int SCENE_GOODNIGHT = 10;

  // Instance variables

  protected static String hostAddress = "192.168.0.90";
  private Socket socket;
  private PrintStream out;
  private BufferedReader in;

  private EventThread eventThread;

  private DollhouseDevice device;

  protected boolean[] rooms = {false, false, false, false, false, false};
  protected int[] cachedDims = {0, 0, 0, 0, 0, 0};
  private int scene = -1;

  // Constructor

  /**
   * Creates a control, with an associated DollhouseDevice object to send
   * incoming events to.
   *
   * @param device The device to receive events
   */
  public DollhouseControl(DollhouseDevice device)
  {
    this.device = device;
    eventThread = new EventThread();
    eventThread.start();
  }

  // Public methods

  /**
   * Event processing monitor
   *
   * @return True if and only if the event processing thread is running
   */
  public boolean isRunning()
  {
    return eventThread.isRunning;
  }

  /**
   * Starts the event monitoring thread
   *
   * @throws DeviceException If there is an error starting communication with
   * the dollhouse
   */
  public void start() throws DeviceException
  {
    eventThread.setRunning(true);
  }

  /**
   * Stops the event monitoring thread
   */
  public void stop()
  {
    try
    {
      eventThread.setRunning(false);
    }
    catch (DeviceException dEx) { }
  }

  /**
   * Turns the specified room off
   *
   * @param room One of the ROOM_XXX constants
   */
  public void roomOff(int room)
  {
    setDim(room, 0);
  }

  /**
   * Sets the specified room to the specified dim level
   *
   * @param room One of the ROOM_XXX constants
   * @param level An integer from 0 to 100.  If room is ROOM_FLOODS, then
   * this method should not be used, roomOn or roomOff should be used instead.
   * The behavior of roomDim on ROOM_FLOODS is not guaranteed by this
   * implementation.
   */
  public void roomDim(int room, int level)
  {
    setDim(room, level);
  }

  /**
   * Turns the specified room on
   *
   * @param room One of the ROOM_XXX constants
   */
  public void roomOn(int room)
  {
    setDim(room, 100);
  }

  /**
   * Turns the specified Scene on
   *
   * @param scene One of the SCENE_XXX constants
   */
  public void sceneOn(int scene)
  {
    setScene(scene);
  }

  /**
   * Turns all of the lights on to full power
   */
  public void allOn()
  {
    pressAllOn();
  }

  /**
   * Turns all of the lights off
   */
  public void allOff()
  {
    pressAllOff();
  }

  // Connect method

  /**
   * Attempts to establish a connection with the Lantronix UDS-10 running on
   * IP address hostAddress, using the port specified by PORT.
   *
   * If a connection is already running, that connection will be shut down, and
   * a new one will be made.  Thus this method can be used as a "restart" of
   * sorts, if the connection fails somehow, e.g gets out of sync.
   *
   * If the first attempt to connect fails, a connection is made to the UDS-10's
   * configuration port.  Making and terminating this connection causes the
   * UDS-10 to reboot, which sometimes allows a connection to be made when it
   * was previously blocked.  This process can take as much as 5 seconds, so
   * if there is a delay in starting, this is probably the cause.
   *
   * In our experiences, if the first connect fails it is probably because the
   * UDS-10 is getting incoming connections from other sources, which are
   * occupying its communication port.  Switching to a more obscure port or
   * ensuring the UDS-10 is on a private network can both solve this problem.
   *
   * @throws DeviceException If no connection can be made
   */
  private void connect() throws DeviceException
  {
    if (socket != null)
    {
      try
      {
        socket.close();
        socket = null;
      }
      catch (IOException ioEx)
      {}

      if (out != null)
      {
        out.close();
        out = null;
      }

      try
      {
        if (in != null)
        {
          in.close();
          in = null;
        }
      }
      catch (IOException ioEx)
      {}
    }

    IOException ex;

    try
    {
      socket = new Socket(hostAddress, PORT);
      out = new PrintStream(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out.println("ZMPI");
      return;
    }
    catch (IOException ioEx)
    {
      try
      {
        socket = new Socket(hostAddress, CONFIG_PORT);
        socket.close();

        socket = new Socket(hostAddress, PORT);
        out = new PrintStream(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println("ZMPI");
        return;
      }
      catch (IOException ioEx2)
      {
        ex = ioEx2;
      }
    }

    socket = null;
    out = null;
    in = null;
    throw new DeviceException("Could not connect: " + ex.getMessage());
  }


  /*
   * Private command methods - see RadioRA interface documentation for
   * details on the commands being sent
   */

  private synchronized void setDim(int room, int level)
  {
    if (room != ROOM_FLOODS)
      out.println("SDL," + room + "," + level);
    else
      out.println("SSL," + room + "," + (level == 0? "OFF" : "ON"));
    rooms[room] = (level != 0);
    cachedDims[room] = level;
  }

  private synchronized void setScene(int scene)
  {
    out.println("BP," + scene + ",ON");
  }

  private synchronized void pressAllOn()
  {
    out.println("BP,16,ON");
    for (int i = 1; i < 6; i++)
      cachedDims[i] = 100;
  }

  private synchronized void pressAllOff()
  {
    out.println("BP,17,OFF");
    for (int i = 1; i < 6; i++)
      cachedDims[i] = 0;
  }

  // Event receiving thread

  /**
   * This thread runs in the background and processes events.  If it has been
   * started (a setRunning(true) call has been made), then it continuously
   * checks for incoming messages on the socket's inputstream.  As it receives
   * these messages, it calls handleResponse.
   *
   * Initially, and again after any setRunning(false) calls, the thread
   * waits until it is notified to start again by a setRunning(true) call.
   */
  private class EventThread extends Thread
  {

    private boolean isRunning = false;

    public void run()
    {

      try
      {
        String resp;
        while (true)
        {
          synchronized (this)
          {
            if (!isRunning)
              this.wait();
            else if (in != null && in.ready())
            {
              resp = in.readLine();
              handleResponse(resp);
            }
            else
            {
              this.wait(100);
            }
          }
        }
      }
      catch (IOException ioEx)
      {
        ioEx.printStackTrace();
      }
      catch (InterruptedException iEx)
      {
        iEx.printStackTrace();
      }
    }

    /**
     * Handles incoming responses from the Lutron box.  Currently all this
     * method does is watch for ZMP (Zone Map) messages, which identify
     * which lights/scenes are currently on.
     *
     * @param resp The String message to parse
     */
    private void handleResponse(String resp)
    {
      resp = resp.toUpperCase();
      if (resp.startsWith("ZMP"))
      {
        resp = resp.substring(3);
        for (int i = 1; i < 6; i++)
        {
          boolean isOn = (resp.charAt(i) == '1');

          if (rooms[i] != isOn)
          {
            int newLevel = (isOn? 100 : 0);
            cachedDims[i] = newLevel;
            device.sendRoomChange(i, newLevel);
          }

          rooms[i] = isOn;
        }
      }
    }

    /**
     * Starts/stops the device, depending on the value of isRunning
     *
     * @param isRunning True to start the device, false to stop it
     *
     * @throws DeviceException If attempting to start the device fails,
     * due to a failed connection attempt
     */
    private synchronized void setRunning(boolean isRunning)
        throws DeviceException
    {
      if (isRunning)
      {
        connect();
      }
      this.isRunning = isRunning;
      this.notifyAll();
    }

  }

}