package edu.cmu.hcii.puc.devices.elevator;

import java.util.Vector;
import java.util.Collection;

/**
 * Simple synchronized FIFO queue for processing events
 */
public class EventQueue extends Vector
{

  public EventQueue()
  {
    super();
  }

  public synchronized void append(ElevatorEvent evt)
  {
    super.add(evt);
    notifyAll();
  }

  public synchronized ElevatorEvent getNext() throws ElevatorException
  {
    try
    {
      while(super.isEmpty())
        wait();
    }
    catch (InterruptedException iEx)
    {
      throw new ElevatorException("Interrupted while waiting for event");
    }

    return (ElevatorEvent)super.remove(0);
  }

  public synchronized void clear()
  {
    super.clear();
  }


}