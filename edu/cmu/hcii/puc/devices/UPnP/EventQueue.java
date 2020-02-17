package edu.cmu.hcii.puc.devices.UPnP;

import edu.cmu.hcii.puc.devices.DeviceException;
import java.util.Vector;

/**
 * Simple synchronized FIFO event queue implementation
 */

public class EventQueue extends Vector
{

  public EventQueue()
  {
    super();
  }

  public synchronized void append(UPnPEvent evt)
  {
    super.add(evt);
    notifyAll();
  }

  public synchronized UPnPEvent getNext() throws DeviceException
  {
    try
    {
      while(super.isEmpty())
        wait();
    }
    catch (InterruptedException iEx)
    {
      throw new DeviceException("Interrupted while waiting for event");
    }

    return (UPnPEvent)super.remove(0);
  }

  public synchronized void clear()
  {
    super.clear();
  }

}