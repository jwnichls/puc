package edu.cmu.hcii.puc.devices.dollhouse;

import com.maya.puc.common.*;
import edu.cmu.hcii.puc.devices.*;

import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * PUC Device class for control of a Lutron RadioRA dollhouse using a
 * Lantronix UDS-10.  The actual communication with the device is handled
 * by an associated DollhouseControl object.
 */
public class DollhouseDevice extends AbstractDevice2
{

  // Constants

  public static final String SPEC_FILE_NAME = "LutronDollhouseSpec.xml";
  public static final String DEVICE_NAME = "Lutron Dollhouse";
  public static final int DEVICE_PORT = 5175;

  public static final String STATE_FLOODS = "Lutron.Zones.Floods.Floods";
  public static final String STATE_SCENE = "Lutron.Scenes.Scene";
  public static final String SUFFIX_DIM = ".Dim";

  public static final String COMMAND_ALL_ON = "Lutron.Scenes.All.On";
  public static final String COMMAND_ALL_OFF = "Lutron.Scenes.All.Off";
  public static final String SUFFIX_ON = ".On";
  public static final String SUFFIX_OFF = ".Off";

  public static final String PREFIX_FOYER = "Lutron.Zones.Foyer";
  public static final String PREFIX_KITCHEN = "Lutron.Zones.Kitchen";
  public static final String PREFIX_MEDIA_ROOM = "Lutron.Zones.MediaRoom";
  public static final String PREFIX_MAIN_BEDROOM = "Lutron.Zones.MainBedroom";

  // Instance variables

  private DollhouseControl control;

  // Constructor

  public DollhouseDevice()
  {
    control = new DollhouseControl(this);
  }

  // AbstractDevice2 methods

  /**
   * Attempts to start the control's event processing thread.  Assuming this
   * works, then calls AbstractDevice2.start().
   */
  public void start()
  {
    if (!control.isRunning())
    {
      try
      {
        control.start();
      }
      catch (DeviceException dEx)
      {
        System.out.println("Could not start device, connect failed:");
        System.out.println(dEx.getMessage());
        return;
      }
    }
    super.start();
  }

  /**
   * Overrides AbstractDevice2.stop() to also stop the control's event
   * processing thread.
   */
  public void stop()
  {
    if ((control != null) && control.isRunning())
    {
      super.stop();
      control.stop();
    }
  }

  /**
   * Handle an incoming PUC message m, from Connection c.
   *
   * @param c The source Connection of this message
   * @param m The message
   */
  public void handleMessage(PUCServer.Connection c, Message m)
  {
    if (m instanceof Message.FullStateRequest)
    {
      sendFullState();
    }
    else if (m instanceof Message.StateChangeRequest)
    {
      Message.StateChangeRequest scr = (Message.StateChangeRequest)m;
      String state = scr.getState();
      String newValStr = scr.getValue();

      if (state.equals(STATE_SCENE))
      {
        control.sceneOn(Integer.parseInt(newValStr) + 5);
      }
      else if (state.equals(STATE_FLOODS))
      {
        if (new Boolean(newValStr).booleanValue())
          control.roomOn(control.ROOM_FLOODS);
        else
          control.roomOff(control.ROOM_FLOODS);
        dispatchStateEvent(STATE_FLOODS, newValStr);
      }
      else if (state.endsWith(SUFFIX_DIM))
      {
        int roomNum = getRoomNum(state);
        if (roomNum != 0)
        {
          control.roomDim(roomNum, Integer.parseInt(newValStr));
          dispatchStateEvent(state, newValStr);
        }
      }
    }
    else if (m instanceof Message.CommandInvokeRequest)
    {
      Message.CommandInvokeRequest cir = (Message.CommandInvokeRequest)m;
      String command = cir.getCommand();

      if (command.equals(COMMAND_ALL_ON))
      {
        control.allOn();
        sendFullState();
      }
      else if (command.equals(COMMAND_ALL_OFF))
      {
        control.allOff();
        sendFullState();
      }
      else
      {
        int roomNum = getRoomNum(command);
        if (roomNum != 0)
        {
          if (command.endsWith(SUFFIX_ON))
          {
            control.roomOn(roomNum);
            dispatchStateEvent(getRoomDimStateName(roomNum), "100");
          }
          else if (command.endsWith(SUFFIX_OFF))
          {
            control.roomOff(roomNum);
            dispatchStateEvent(getRoomDimStateName(roomNum), "0");
          }
        }
      }
    }
    else if (m instanceof Message.SpecRequest)
    {
      try
      {
        c.send(new Message.DeviceSpec(getSpec()));
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  protected String getSpecFileName() { return SPEC_FILE_NAME; }

  public String getName() { return DEVICE_NAME; }

  protected int getDefaultPort() { return DEVICE_PORT; }

  /**
   * Configure pops up a dialog to enter the IP Address of the UDS-10, using
   * the IPDialog class
   */
  public void configure()
  {
    String newIP = new IPDialog().getNewIP();
    if (newIP != null)
    {
      control.hostAddress = newIP;
      System.out.println("Changed IP of Dollhouse to " + newIP);

      if (control.isRunning())
      {
        try
        {
          control.start();
        }
        catch (DeviceException dEx)
        {
          dEx.printStackTrace();
        }
      }
    }
  }

  // Protected methods for sending light change events

  protected void sendRoomChange(int room, int dim)
  {
    String roomState;

    switch (room)
    {
      case DollhouseControl.ROOM_FLOODS:
        dispatchStateEvent(STATE_FLOODS, (dim == 0? "false" : "true"));
        return;
      case DollhouseControl.ROOM_FOYER:
        roomState = PREFIX_FOYER;
        break;
      case DollhouseControl.ROOM_KITCHEN:
        roomState = PREFIX_KITCHEN;
        break;
      case DollhouseControl.ROOM_MAIN_BEDROOM:
        roomState = PREFIX_MAIN_BEDROOM;
        break;
      case DollhouseControl.ROOM_MEDIA_ROOM:
        roomState = PREFIX_MEDIA_ROOM;
        break;
      default:
        return;
    }

    roomState += SUFFIX_DIM;
    dispatchStateEvent(roomState, String.valueOf(dim));
  }

  protected void sendSceneChange(int sceneNum)
  {
    dispatchStateEvent(STATE_SCENE, String.valueOf(sceneNum));
  }

  // Utilities

  private void sendFullState()
  {
    dispatchStateEvent(STATE_FLOODS, "" + control.rooms[control.ROOM_FLOODS]);
    dispatchStateEvent(PREFIX_FOYER + SUFFIX_DIM,
                       "" + (control.cachedDims[control.ROOM_FOYER]));
    dispatchStateEvent(PREFIX_KITCHEN + SUFFIX_DIM,
                       "" + (control.cachedDims[control.ROOM_KITCHEN]));
    dispatchStateEvent(PREFIX_MAIN_BEDROOM + SUFFIX_DIM,
                       "" + (control.cachedDims[control.ROOM_MAIN_BEDROOM]));
    dispatchStateEvent(PREFIX_MEDIA_ROOM + SUFFIX_DIM,
                       "" + (control.cachedDims[control.ROOM_MEDIA_ROOM]));
  }

  private int getRoomNum(String str)
  {
    int roomNum = 0;
    if (str.startsWith(PREFIX_FOYER))
      roomNum = control.ROOM_FOYER;
    else if (str.startsWith(PREFIX_KITCHEN))
      roomNum = control.ROOM_KITCHEN;
    else if (str.startsWith(PREFIX_MAIN_BEDROOM))
      roomNum = control.ROOM_MAIN_BEDROOM;
    else if (str.startsWith(PREFIX_MEDIA_ROOM))
      roomNum = control.ROOM_MEDIA_ROOM;
    return roomNum;
  }

  private String getRoomDimStateName(int roomNum)
  {
    switch (roomNum)
    {
      case DollhouseControl.ROOM_FLOODS:
        return STATE_FLOODS;
      case DollhouseControl.ROOM_FOYER:
        return PREFIX_FOYER + SUFFIX_DIM;
      case DollhouseControl.ROOM_KITCHEN:
        return PREFIX_KITCHEN + SUFFIX_DIM;
      case DollhouseControl.ROOM_MAIN_BEDROOM:
        return PREFIX_MAIN_BEDROOM + SUFFIX_DIM;
      case DollhouseControl.ROOM_MEDIA_ROOM:
        return PREFIX_MEDIA_ROOM + SUFFIX_DIM;
      default:
        return "";
    }
  }

}
