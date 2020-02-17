package edu.cmu.hcii.puc.devices.elevator;

/**
 * All classes which want to receive Elevator events should do so in the
 * following manner, using this class.
 *
 * First, they must implement the ElevatorListener interface, so they may
 * receive and process events.  Then they should have an EventQueue for
 * storing the events they receive.  Finally, when they want to begin
 * listening, they should construct a new EventThread, with themself and their
 * event queue as arguments.  Starting this queue will begin a cycle in which
 * the model will call the listener's receiveEvent method to enqueue events,
 * and this thread will call their processEvent method on the events in the
 * order that they arrived.
 *
 * This method of event processing ensures synchronization of events, as long
 * as the model sends them in the order in which they were generated.
 */
public class EventThread extends Thread
{

  private ElevatorListener el;
  private EventQueue events;

  public EventThread(ElevatorListener el, EventQueue events)
  {
    super();
    this.el = el;
    this.events = events;
  }

  public void run()
  {
    ElevatorEvent e;
    while (true)
    {
      try
      {
        e = events.getNext();
        el.processEvent(e);
      }
      catch (ElevatorException dEx)
      {
        dEx.printStackTrace();
      }
    }
  }

}