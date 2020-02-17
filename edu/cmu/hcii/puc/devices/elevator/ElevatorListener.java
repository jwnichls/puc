package edu.cmu.hcii.puc.devices.elevator;

/**
 * Any class which wants to be notified of events from an ElevatorModel
 * should implement this interface.
 */
public interface ElevatorListener
{

  /**
   * When an event is first generated, all subscribed listeners will have their
   * receiveEvent method called.  At this time they should merely enqueue the
   * event, and leave processing to a separate EventThread.  This ensures
   * rapid event dispatching, and prevents one listener's processing from
   * slowing down the entire model.
   *
   * @param event The event to receive and enqueue
   */
  public void receiveEvent(ElevatorEvent event);

  /**
   * processEvent will be called by a running EventThread as each event is
   * popped off of the event queue.  Implementors of this event should check
   * the event's class, and handle it if desired.
   *
   * @param event The event to handle
   *
   * @return true if the event was in fact processed by this method.  Otherwise
   * false should be returned, so children of this implementor can reserve
   * the possibility of handling any events not handled by their parent.
   */
  public boolean processEvent(ElevatorEvent event);

}