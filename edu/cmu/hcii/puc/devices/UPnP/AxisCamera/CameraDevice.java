package edu.cmu.hcii.puc.devices.UPnP.AxisCamera;

import com.maya.puc.common.*;
import edu.cmu.hcii.puc.devices.*;
import edu.cmu.hcii.puc.devices.UPnP.*;

import javax.swing.JOptionPane;

/**
 * Implementation of UPnPDevice for an Axis UPnP-enabled camera
 */
public class CameraDevice extends UPnPDevice
{

// Device constants

  public static final String DEVICE_NAME = "Axis UPnP Camera";
  public static final String SPEC_FILE_NAME = "AxisCameraSpec.xml";
  public static final int DEFAULT_PORT = 5172;

  public static final String STATE_PAN = "AxisCamera.Controls.Sliders.Pan";
  public static final String STATE_TILT = "AxisCamera.Controls.Sliders.Tilt";
  public static final String STATE_ZOOM = "AxisCamera.Controls.Sliders.Zoom";
  public static final String STATE_PRESET = "AxisCamera.Presets.Presets";

  public static final String COMMAND_HOME = "AxisCamera.Controls.Home";

// Instance variables

  private CameraControl cc;

  /* Cached state variables */
  private String lastPan = "0";
  private String lastTilt = "0";
  private String lastZoom = "1";
  private PresetList presets;

  public CameraDevice()
  {
    presets = new PresetList();
  }

// UPnPDevice abstract methods

  protected UPnPControl getControl()
  {
    cc = new CameraControl();
    return cc;
  }

  protected void cache(String state, String value)
  {
    if (state.equals(STATE_PAN)) lastPan = value;
    else if (state.equals(STATE_TILT)) lastTilt = value;
    else if (state.equals(STATE_ZOOM)) lastZoom = value;
  }

  protected void processNonStateEvent(UPnPEvent evt)
  {
    if (evt instanceof UPnPEvent.StringResponse)
    {
      UPnPEvent.StringResponse sr = (UPnPEvent.StringResponse) evt;
      int callingMethodID = sr.getCallingMethodID();
      String response = sr.getResponse();

      switch (callingMethodID)
      {
        case CameraControl.METHOD_GET_PRESETS:
          presets.parseUPnPPresetList(response);
          dispatchPresetList();
          break;
        default:
          System.out.println("Unknown callingMethodID " + callingMethodID);
      }
    }
    /** @todo Implement non-state events */
  }

// AbstractDevice2 abstract methods

  public void handleMessage(PUCServer.Connection c, Message m)
  {
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
        if (scr.getState().equals(STATE_PAN))
        {
          cc.setPan(Integer.parseInt(newValStr));
        }
        else if (scr.getState().equals(STATE_TILT))
        {
          cc.setTilt(Integer.parseInt(newValStr));
        }
        else if (scr.getState().equals(STATE_ZOOM))
        {
          cc.setZoom(Integer.parseInt(newValStr));
        }
        else if (scr.getState().equals(STATE_PRESET))
        {
          cc.goToPreset(Integer.parseInt(newValStr));
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
        if (command.equals(COMMAND_HOME))
        {
          cc.home();
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
    return DEVICE_NAME;
  }

  protected int getDefaultPort()
  {
    return DEFAULT_PORT;
  }

  public void configure()
  {
  }

// Private device methods

  private void sendFullState(PUCServer.Connection c)
  {
    c.send(new Message.StateChangeNotification(STATE_PAN, lastPan));
    c.send(new Message.StateChangeNotification(STATE_TILT, lastTilt));
    c.send(new Message.StateChangeNotification(STATE_ZOOM, lastZoom));
    try
    {
      cc.getPresets();
    }
    catch (DeviceException dEx)
    {
      dEx.printStackTrace();
    }
  }

  private void dispatchPresetList()
  {
    int size = presets.size();
    PresetList.Preset p;
    for (int i = 0; i < size; i++)
    {
      p = presets.getPreset(i);
      dispatchStateEvent("Preset" + p.getPresetNum() + "Avail", "true");
      dispatchStateEvent("PresetName" + p.getPresetNum(), p.getPresetName());
    }
  }

}
