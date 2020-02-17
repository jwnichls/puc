package edu.cmu.hcii.puc.devices.UPnP;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.maya.puc.common.DeviceFactory;
import com.maya.puc.common.DeviceFactoryListener;

import edu.cmu.hcii.puc.devices.DeviceException;

/**
 * Abstract class encapsulating most of the functionaly of a UPnP-based
 * DeviceFactory.  Handles device lifetime as well as distribution of events
 * to their target devices.
 */
public abstract class UPnPFactory implements DeviceFactory
{

  // Instance variables

  private ArrayList listeners;
  private Hashtable devices;
  protected UPnPControl control = null;

  // Constructor

  public UPnPFactory()
  {
    listeners = new ArrayList();
    devices = new Hashtable();
    control = getControl();

    new BackgroundThread().start();
    new UpdateThread().start();
  }

  // Control accessor

  public abstract UPnPControl getControl();

  // DeviceFactory methods

  public void addListener(DeviceFactoryListener dfl)
  {
    listeners.add(dfl);
  }

  // Other methods

  /**
   * Called when a new device arrives on the UPnP network.
   *
   * @param device The new device
   */
  private void deviceFound(UPnPFactoryDevice device)
  {
    devices.put(device.getUDN(), device);

    Iterator it = listeners.iterator();
    while (it.hasNext())
    {
      DeviceFactoryListener dfl = (DeviceFactoryListener) it.next();
      dfl.loadNewDevice(device);
      device.start();
    }
  }

  /**
   * Called when a device is removed from the UPnP network
   *
   * @param deviceUDN The UDN (Unique Device Name?) of the device to be
   * removed
   */
  private void deviceRemoved(String deviceUDN)
  {
    UPnPFactoryDevice device = (UPnPFactoryDevice) devices.remove(deviceUDN);

    if (device != null)
    {
      Iterator it = listeners.iterator();
      while (it.hasNext())
      {
        DeviceFactoryListener dfl = (DeviceFactoryListener) it.next();
        dfl.removeDevice(device);
      }
    }
  }

  /**
   * Takes any event other than a UPnPEvent.FactoryEvent and processes it.
   * Does so by first attempting to find the source device specified by the
   * event's sourceUDN and forward the event to that device, and if that fails,
   * calling the abstract processNonTargetedEvent() method.
   *
   * @param e The event to process
   */
  private void processEvent(UPnPEvent e)
  {
    String sourceUDN = e.getSourceUDN();
    if (sourceUDN == null || sourceUDN.equals(""))
    {
      processNonTargetedEvent(e);
    }
    else
    {
      UPnPFactoryDevice dev = (UPnPFactoryDevice) devices.get(sourceUDN);
      if (dev != null) dev.processEvent(e);
    }
  }

  // Abstract methods

  /**
   * Methods should implement this if they expect to receive any events from
   * their native code which are not lifetime events (FactoryEvents), but do
   * not have a sourceUDN to associate them with a specific device.
   *
   * @param e The event to process
   */
  protected abstract void processNonTargetedEvent(UPnPEvent e);

  /**
   * Implementations of UPnPFactory must provide a way for this abstract
   * class to know about the devices it is manipulating.  This method should
   * be implemented by UPnPFactories to take the incoming FactoryEvent and
   * generate a new instance of their corresponding device from it.
   *
   * @param fe The event to generate the device from
   *
   * @return The newly created device
   */
  protected abstract UPnPFactoryDevice createDevice(UPnPEvent.FactoryEvent fe);

// Threads

  /**
   * This thread repeatedly calls UPnPControl.getNextEvent(), which waits
   * for an event to be received from the UPnP device and then returns it in a
   * UPnPEvent object.
   *
   * If an event is an instance of UPnPEvent.FactoryEvent, it is sent to one
   * of the discovery/removal methods.  Otherwise it is passed on to the
   * processEvent method.
   */
  private class UpdateThread extends Thread
  {

    public UpdateThread()
    {
      super("UpdateThread");
    }

    public void run()
    {
      UPnPEvent e;
      while (true)
      {
        try
        {
          e = control.getNextEvent();
          if (e instanceof UPnPEvent.FactoryEvent)
          {
            UPnPEvent.FactoryEvent fe = (UPnPEvent.FactoryEvent) e;
            int eventType = fe.getEventType();
            switch (eventType)
            {
              case UPnPEvent.FactoryEvent.TYPE_DEVICE_FOUND:
                UPnPFactoryDevice dev = createDevice(fe);
                deviceFound(dev);
                break;
              case UPnPEvent.FactoryEvent.TYPE_DEVICE_REMOVED:
                deviceRemoved(fe.getSourceUDN());
                break;
            }
          }
          else
          {
            processEvent(e);
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
  private class BackgroundThread extends Thread
  {

    public BackgroundThread()
    {
      super("BackgroundThread");
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