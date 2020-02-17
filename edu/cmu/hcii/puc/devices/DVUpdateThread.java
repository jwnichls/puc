package edu.cmu.hcii.puc.devices;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class DVUpdateThread extends Thread
{

// Instance variables

  private DVDevice device;
  private DVDevice.ActiveDevice activeDevice;
  private Listeners listeners;
  private String lastTime;
  private int lastPlayMode;
  private boolean lastDeviceMode;

  private boolean errorEncountered;

// Constructors

  public DVUpdateThread(DVDevice device, Listeners listeners)
  {
    super();
    this.device = device;
    this.activeDevice = device.getActiveDevice();
    this.listeners = listeners;
    lastTime = "";
    lastPlayMode = -1;
    lastDeviceMode = false;

    errorEncountered = false;
  }

// Runnable method

  public void run()
  {
    System.out.println("Starting update thread run() method...");
    while (listeners.areActive())
    {
      try
      {
        if (errorEncountered)
        {
          // Attempt to reconnect to the device, and update all states
          activeDevice.updateAll();
          errorEncountered = false;
          device.requestFullState();
        }
        else
        {
          // Update just the transport mode and timecode
          activeDevice.updateNonStatic();

          boolean deviceMode = activeDevice.getDeviceMode();
          if (deviceMode != lastDeviceMode)
          {
            listeners.dispatchStateEvent(DVDevice.STATE_DEVICE_MODE,
              String.valueOf(deviceMode));
            lastDeviceMode = deviceMode;
          }

          int playMode = activeDevice.getTransportMode();
          if (playMode != lastPlayMode)
          {
            listeners.dispatchStateEvent(DVDevice.STATE_TRANSPORT_MODE,
              String.valueOf(playMode));
            lastPlayMode = playMode;
          }

          String time = activeDevice.getTimecode();
          if (!time.equals(lastTime))
          {
            lastTime = time;
            listeners.dispatchStateEvent(DVDevice.STATE_TIMECODE, time);
          }
        }
      }
      catch (DeviceException dEx)
      {
        System.err.println("ERROR: " + dEx.getMessage());
        listeners.dispatchStateEvent(DVDevice.STATE_POWER, String.valueOf(false));
        errorEncountered = true;
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