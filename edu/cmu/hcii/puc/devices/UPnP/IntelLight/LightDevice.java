package edu.cmu.hcii.puc.devices.UPnP.IntelLight;

import com.maya.puc.common.*;
import edu.cmu.hcii.puc.devices.*;
import edu.cmu.hcii.puc.devices.UPnP.*;

import javax.swing.JOptionPane;

/**
 * Implementation of a UPnPFactoryDevice which represents control of a single
 * Intel UPnP Light simulator.
 */
public class LightDevice extends UPnPFactoryDevice
{

// Device constants

  public static final String DEVICE_NAME = "Intel UPnP Light";
  public static final String SPEC_FILE_NAME = "IntelLightSpec.xml";
  public static int NEXT_PORT = 5190;

  public static final String STATE_POWER = "UPnP.Light.Power";
  public static final String STATE_LOAD_LEVEL = "UPnP.Light.LoadLevel";
  public static final String COMMAND_ALL_ON = "UPnP.All.On";
  public static final String COMMAND_ALL_OFF = "UPnP.All.Off";

// Instance variables

  private LightControl lc;

  /* Cached state variables */
  private String lastPower = "true";
  private String lastLoadLevel = "100";

// Constructor

  public LightDevice(UPnPControl control, String UDN, String friendlyName)
  {
    super(control, UDN, friendlyName);
    lc = (LightControl) control;
  }

// UPnPFactoryDevice abstract methods

  protected void processNonStateEvent(UPnPEvent evt)
  {
    // Should not be any non-state events for lights
  }

  protected void cache(String state, String value)
  {
    if (state != null && value != null)
    {
      if (state != null && state.equals(STATE_POWER))
        lastPower = value;
      else if (state != null && state.equals(STATE_LOAD_LEVEL))
        lastLoadLevel = value;
    }
  }

// AbstractDevice2 abstract methods

  public void handleMessage(PUCServer.Connection c, Message m)
  {
    System.out.println("Light " + getUDN() + " handling message");
    if (m instanceof Message.FullStateRequest)
    {
      sendFullState(c);
    }
    else if (m instanceof Message.StateChangeRequest)
    {
      Message.StateChangeRequest scr = (Message.StateChangeRequest)m;
      String newValStr = scr.getValue();

      try
      {
        if (scr.getState().equals(STATE_POWER))
        {
          lc.setPower(getUDN(), Boolean.valueOf(newValStr).booleanValue());
          sendAll(new Message.StateChangeNotification(STATE_POWER, newValStr));
          lastPower = newValStr;
        }
        else if (scr.getState().equals(STATE_LOAD_LEVEL))
        {
          lc.setLoadLevel(getUDN(), Integer.parseInt(newValStr));
          sendAll(new Message.StateChangeNotification(STATE_LOAD_LEVEL, newValStr));
          lastLoadLevel = newValStr;
        }
      }
      catch (DeviceException dEx)
      {
        dEx.printStackTrace();
      }
    }
    else if (m instanceof Message.CommandInvokeRequest)
    {
      Message.CommandInvokeRequest cir = (Message.CommandInvokeRequest)m;
      String command = cir.getCommand();

      try
      {
        if (cir.getCommand().equals(COMMAND_ALL_ON))
        {
          lc.allOn();
        }
        else if (cir.getCommand().equals(COMMAND_ALL_OFF))
        {
          lc.allOff();
        }
      }
      catch (DeviceException dEx)
      {
        dEx.printStackTrace();
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

  protected String getSpecFileName()
  {
    return SPEC_FILE_NAME;
  }

  public String getName()
  {
    return friendlyName;
  }

  protected int getDefaultPort()
  {
    return NEXT_PORT++;
  }

  public void configure()
  {
    JOptionPane.showMessageDialog(null, "UDN: " + getUDN(), "Info",
                                  JOptionPane.INFORMATION_MESSAGE);
  }

// Private device methods

  private void sendFullState(PUCServer.Connection c)
  {
    c.send(new Message.StateChangeNotification(STATE_POWER, lastPower));
    c.send(new Message.StateChangeNotification(STATE_LOAD_LEVEL, lastLoadLevel));
  }

}
