package edu.cmu.hcii.puc.devices.UPnP;

import com.maya.puc.common.*;
import edu.cmu.hcii.puc.devices.*;

/**
 * Abstraction of any UPnPDevice, and its use of a UPnPControl object to
 * interact with a native UPnP Control Point implementation.
 */

public abstract class UPnPDevice extends AbstractDevice2
{

  protected UPnPControl control;

// Constructor

  public UPnPDevice()
  {
    control = getControl();
  }

// Overridden methods

  public void start(String status)
  {
    control.clearAllEvents();
    new BackgroundThread(control).start();
    new UpdateThread(this).start();

    super.start(status);
  }

  public void stop(String status)
  {
    super.stop(status);

    try
    {
      control.stopCP();
    }
    catch (DeviceException dEx)
    {
      dEx.printStackTrace();
    }
  }

// Abstract methods

  protected abstract UPnPControl getControl();

  protected abstract void cache(String state, String value);

  protected abstract void processNonStateEvent(UPnPEvent evt);

// Threads

  /**
   * This thread repeatedly calls UPnPControl.getNextEvent(), which waits
   * for an event to be received from the UPnP device and then returns it in a
   * UPnPEvent object.  As each event is received, it is forwarded to all
   * listening connections, and the corresponding cached state is updated.
   *
   * The purpose of caching is to avoid a request for every state when a new
   * PUC subscribes.
   */
  private static class UpdateThread extends Thread
  {

    private UPnPDevice device;

    public UpdateThread(UPnPDevice device)
    {
      super("UpdateThread");
      this.device = device;
    }

    public void run()
    {
      UPnPEvent e;
      while (device.isRunning())
      {
        try
        {
          e = device.control.getNextEvent();
          if (e instanceof UPnPEvent.StateChange)
          {
            UPnPEvent.StateChange sc = (UPnPEvent.StateChange) e;
            String state = sc.getState();
            String value = sc.getValue();
            System.out.println("UPnP Device (" + getName() +
                               "): Sending state change event: (" +
                               state + ", " + value + ")");
            device.dispatchStateEvent(state, value);
            // Update cached state
            device.cache(state, value);
          }
          else
          {
            device.processNonStateEvent(e);
          }
        }
        catch (DeviceException dEx)
        {
          dEx.printStackTrace();
        }
      }
    }

  }

  /**
   * This thread simply calls UPnPControl.startCP, which executes the
   * UPnP Control Point.  For reasons not entirely clear to me at the time of
   * development, the Control Point needs to be run in its own thread, in this
   * manner.
   */
  private static class BackgroundThread extends Thread
  {

    private UPnPControl control;

    public BackgroundThread(UPnPControl control)
    {
      super("BackgroundThread");
      this.control = control;
    }

    public void run()
    {
      try
      {
        control.startCP();
      }
      catch (DeviceException dEx)
      {
        dEx.printStackTrace();
      }
    }

  }


}