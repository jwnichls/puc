package edu.cmu.hcii.puc.devices.elevator;

import java.util.ArrayList;
import java.util.Arrays;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * Abstract model representing an Elevator
 */
public class ElevatorModel
{

// Constants

  /**
   * Setting this constant to true will cause an additional frame to be
   * displayed at load time, which will contain debugging output information
   * generated during the operation of the elevator simulator
   */
  public static final boolean DEBUG = false;

  /**
   * Constants specifying whether an event with a duration is starting or
   * ending
   */
  public static final boolean STARTED = true;
  public static final boolean ENDED = false;

  public static final int DEFAULT_NUM_FLOORS = 5;

  public static final int MAX_OCCUPANCY = 5;

  /**
   * The direction constants should be left with these values, because they
   * are used occasionally in calculations (e.g. if moving up, currentFloor +
   * DIR_UP = newFloor).
   */
  public static final int DIR_DOWN = -1;
  public static final int DIR_STATIONARY = 0;
  public static final int DIR_UP = 1;

  /**
   * Delays for a variety of events.  Change these as necessary to create
   * tweeked timing performance.
   */
  public static final long MOVE_TIME = 1000;
  public static final long ENTER_TIME = 3000;
  public static final long EXIT_TIME = 3000;
  public static final long DOOR_OPEN_TIME = 7000;
  public static final long DOOR_KEEP_OPEN_TIME = 3000;
  public static final long DOOR_OPENING_TIME = 1000;
  public static final long WAIT_FOR_REQUEST_TIME = 3000;

  /**
   * The constant specifying the "correct" access code, i.e. the one which
   * provides full access to all floors
   */
  private static final String ACCESS_CODE = "password";

  /**
   * debugFrame, output, and the static initialization code below all deal
   * with debugging output.
   */
  private static JFrame debugFrame = null;
  private static JTextArea output = null;

  static
  {
    if (DEBUG)
    {
      debugFrame = new JFrame("Elevator Simulator 1.0 Debug Output");
      debugFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

      try
      {
        URL url = Class.
            forName("edu.cmu.hcii.puc.devices.elevator.ElevatorModel")
            .getResource("pucproxy.jpg");
        debugFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
      }
      catch (Exception ex)
      {}

      output = new JTextArea(20, 50);
      JScrollPane sp = new JScrollPane(output);
      debugFrame.getContentPane().add(sp, BorderLayout.CENTER);
      debugFrame.setSize(output.getSize()); ;
      debugFrame.pack();
      debugFrame.show();
    }
  }

// Instance variables

  private int currentLoc;
  private int currentDir;
  private int destination;
  private int occupancy;
  private boolean doorsOpen;

  private int numUsersBlockingDoor;

  private boolean doorOpenButton;
  private boolean doorCloseButton;

  private boolean[] fullAccess;
  private boolean[] limitedAccess;

  private int numFloors;
  private Floor floors[];

  private ArrayList listeners;
  private ArrayList users;

// Locks

  private Object buttonLock;
  private Object doorLock;
  private Object doorButtonLock;
  private Object occupancyLock;

// Static methods

  public static void debug(String msg)
  {
    if (DEBUG) output.append(msg + "\n");
  }

// Constructor

  public ElevatorModel()
  {
    this(DEFAULT_NUM_FLOORS);
  }

  public ElevatorModel(int numFloors)
  {
    currentLoc = 0;
    currentDir = DIR_STATIONARY;
    destination = 0;
    occupancy = 0;
    doorsOpen = false;
    numUsersBlockingDoor = 0;

    doorOpenButton = doorCloseButton = false;

    this.numFloors = numFloors;
    floors = createFloors();
    setAccessibleFloors();

    listeners = new ArrayList();
    users = new ArrayList();

    buttonLock = new Object();
    doorLock = new Object();
    doorButtonLock = new Object();
    occupancyLock = new Object();

    MainThread mover = new MainThread();
    mover.start();
  }

// Public request methods

  public void call(int floor, int dir)
  {
    if (dir == DIR_STATIONARY) return;
    synchronized (buttonLock)
    {
      debug("Calling floor " + floors[floor] + ", dir " + getDirName(dir));
      if (dir == DIR_DOWN)
        floors[floor].downCalledFor = true;
      else if (dir == DIR_UP)
        floors[floor].upCalledFor = true;
      sendCallButtonEvent(floor, dir, true);
      buttonLock.notifyAll();
    }
  }

  public void requestStop(int floor)
  {
    synchronized (buttonLock)
    {
      debug("Requesting stop at floor " + floors[floor]);
      floors[floor].stopRequestedAt = true;
      sendFloorRequestButtonEvent(floor, true);
      buttonLock.notifyAll();
    }
  }

  /**
   * Gets an array of booleans representing the access granted to the given
   * access code
   *
   * @param code The access code to use
   *
   * @return fullAccess (access to all floors) if the password is correct,
   * otherwise limitedAccess (at the time this documention is being written,
   * this is access to every odd numbered floor)
   */
  public boolean[] getAccess(String code)
  {
    if (code != null && code.equals(ACCESS_CODE))
      return fullAccess;
    else
      return limitedAccess;
  }

// Entry/exit methods

  /**
   * Checks to make sure it's ok for this user to enter.  Assuming it is,
   * starts a thread which represents the process of them entering the
   * elevator
   *
   * @param user The User trying to enter
   *
   * @throws ElevatorException If the user could not enter.  More specifically:
   *
   * WrongFloorException: The user's floor and the elevator's floor do not
   * match.
   *
   * NoRoomException: The elevator is already at its maximum occupancy
   *
   * DoorClosedException: The doors are closing or closed
   */
  public void enter(AbstractUser user) throws ElevatorException
  {
    int floor = user.getLocation();
    synchronized (occupancyLock)
    {
      if (currentLoc != floor)
        throw new ElevatorException.WrongFloorException
            ("Cannot enter the elevator from a different floor!");
      else if (occupancy >= MAX_OCCUPANCY)
        throw new ElevatorException.NoRoomException
            ("The elevator is full, please wait");
      else
      {
        synchronized (doorLock)
        {
          if (!doorsOpen)
            throw new ElevatorException.DoorClosedException
                ("Cannot enter when doors are closed!");
          else
            numUsersBlockingDoor++;
        }

        incrementOccupancy();
        new EnterThread(floor, user).start();
      }
    }
  }

  /**
   * Thread representing the process of entering the elevator.  First sends
   * and event notifying listeners that a user has started to enter.  Then
   * sleeps for the constant ENTER_TIME, then sends another event notifying
   * listeners that the user has finished entering.
   */
  private class EnterThread extends Thread
  {

    private int floor;
    private AbstractUser user;

    public EnterThread(int floor, AbstractUser user)
    {
      super();
      this.floor = floor;
      this.user = user;
    }

    public void run()
    {
      sendEnteringEvent(user, STARTED);

      try
      {
        Thread.sleep(ENTER_TIME);
      }
      catch (InterruptedException iEx)
      {
        iEx.printStackTrace();
      }

      synchronized (doorLock)
      {
        numUsersBlockingDoor--;
      }
      sendEnteringEvent(user, ENDED);
    }
  }

  /**
   * Checks to make sure it's ok for this user to exit.  Assuming it is,
   * starts a thread which represents the process of them exiting the
   * elevator
   *
   * @param user The User trying to exit
   *
   * @throws ElevatorException If the user could not exit.  More specifically:
   *
   * ElevatorException (no subclass): The elevator is empty.  If this happens,
   * it means there is a bug in the model, because the user thinks they're
   * inside and the model does not.
   *
   * DoorClosedException: The doors are closing or closed
   */
  public int exit(AbstractUser user) throws ElevatorException
  {
    synchronized (occupancyLock)
    {
      if (occupancy <= 0)
        throw new ElevatorException("Cannot exit an empty elevator!");

      synchronized (doorLock)
      {
        if (!doorsOpen)
          throw new ElevatorException.DoorClosedException
              ("Cannot exit when doors are closed!");
        else
          numUsersBlockingDoor++;
      }

      decrementOccupancy();
      new ExitThread(user).start();
    }
    return currentLoc;
  }

  /**
   * Thread representing the process of exiting the elevator.  First sends
   * and event notifying listeners that a user has started to exit.  Then
   * sleeps for the constant EXIT_TIME, then sends another event notifying
   * listeners that the user has finished exiting.
   */
  private class ExitThread extends Thread
  {
    private AbstractUser user;

    public ExitThread(AbstractUser user)
    {
      super();
      this.user = user;
    }

    public void run()
    {
      sendExitingEvent(user, STARTED);

      try
      {
        Thread.sleep(EXIT_TIME);
      }
      catch (InterruptedException iEx)
      {
        iEx.printStackTrace();
      }

      synchronized (doorLock)
      {
        numUsersBlockingDoor--;
      }
      sendExitingEvent(user, ENDED);
    }
  }

  /**
   * Should be called by users to notify the elevator model that they have
   * teleported.  The model will then send an event notifying listeners of
   * the teleport.
   *
   * @param user The teleported User
   * @param oldFloor The floor the User was previously at
   * @param wasInside true if the User was inside the elevator, so the model
   * knows to decrement the occupancy
   */
  public void teleported(AbstractUser user, int oldFloor, boolean wasInside)
  {
    if (wasInside)
    {
      decrementOccupancy();
    }
    sendTeleportEvent(user, oldFloor, wasInside);
  }

// Occupancy methods

  private void incrementOccupancy()
  {
    synchronized (occupancyLock)
    {
      occupancy++;
    }
  }

  private void decrementOccupancy()
  {
    synchronized (occupancyLock)
    {
      occupancy--;
    }
  }

// Door button methods

  public boolean getDoorOpenButton()
  {
    return doorOpenButton;
  }

  public void setDoorOpenButton()
  {
    doorOpenButton = true;
    synchronized (buttonLock)
    {
      buttonLock.notifyAll();
    }
    sendDoorOpenButtonEvent(true);
  }

  public boolean getDoorCloseButton()
  {
    return doorOpenButton;
  }

  public void setDoorCloseButton()
  {
    doorCloseButton = true;
    sendDoorCloseButtonEvent(true);
    synchronized (doorButtonLock)
    {
      doorButtonLock.notifyAll();
    }
  }

// Status inquiry methods

  public int getNumFloors() { return numFloors; }

  public int getCurrentDir() { return currentDir; }

  public String getDirName(int dir)
  {
    switch (dir)
    {
      case DIR_DOWN:
        return "Down";
      case DIR_UP:
        return "Up";
      case DIR_STATIONARY:
        return "Stopped";
      default:
        return "Unknown direction";
    }
  }

  public String getFloorName(int floor)
  {
    return floors[floor].toString();
  }

  public String[] getFloorNames()
  {
    String[] names = new String[numFloors];
    for (int i = 0; i < numFloors; i++)
    {
      names[i] = floors[i].toString();
    }
    return names;
  }

  public int getCurrentLoc() { return currentLoc; }

  public boolean doorsAreOpen() { return doorsOpen; }

  public boolean stopRequestedAt(int floor)
  {
    return floors[floor].stopRequestedAt;
  }

  public boolean downRequestedAt(int floor)
  {
    return floors[floor].downCalledFor;
  }

  public boolean upRequestedAt(int floor)
  {
    return floors[floor].upCalledFor;
  }

// Event subscription and user addition methods

  public void subscribe(ElevatorListener el)
  {
    if (el != null) listeners.add(el);
  }

  public void addUser(AbstractUser user)
  {
    if (user != null)
    {
      subscribe(user);
      users.add(user);
      sendExistenceEvent(user, true);
    }
  }

  public void unsubscribe(ElevatorListener el)
  {
    listeners.remove(el);
  }

  public void removeUser(AbstractUser user)
  {
    if (user != null)
    {
      unsubscribe(user);
      users.remove(user);
      sendExistenceEvent(user, false);
    }
  }

  public ArrayList getUsers()
  {
    return users;
  }

// Accessibility method

  private void setAccessibleFloors()
  {
    fullAccess = new boolean[numFloors];
    Arrays.fill(fullAccess, true);

    limitedAccess = new boolean[numFloors];
    for (int i = 0; i < numFloors; i++)
    {
      limitedAccess[i] = ( (i % 2) == 0);
    }
  }

// Logic for choosing next floor to move to

  private int getTopRequest()
  {
    synchronized (buttonLock)
    {
      for (int i = numFloors - 1; i >= 0; i--)
      {
        if (floors[i].requestAt())
          return i;
      }
      return -1;
    }
  }

  private int getBottomRequest()
  {
    synchronized (buttonLock)
    {
      for (int i = 0; i < numFloors; i++)
      {
        if (floors[i].requestAt())
          return i;
      }
      return -1;
    }
  }

  /**
   * This is the main method used in determining where to go next.  The logic
   * used is fairly complicated; the general idea is as follows:
   *
   * First, look for the top-most and bottom-most floors that any request
   * is asking you to go to (up/down requests ask you to go to the floor they
   * were made at, in-elevator requests asked you to go to the specified floor).
   *
   * If there is a request above you and you are going up, or a request below
   * you and you are going down, the make that request's floor your new
   * destination.  Otherwise reset your direction to stationary and try finding
   * a new destination again.
   *
   * If your direction is stationary (possibly as a result of the first
   * attempt failing), but there is a request somewhere, make that your new
   * destination.
   *
   * If you are stationary and there are no requests except on the floor you
   * are already at, then clear any requests at your floor and wait for
   * notification that a button has been pushed somewhere.
   *
   * NOTE: As I said, this logic is very complicated.  Making small changes
   * can introduce very subtle synchronization bugs.  Do not modify this
   * method unless you are VERY SURE of what you are doing.
   */
  private void chooseNextDest()
  {
    boolean statePrinted = false;

    while (true)
    {
      int topRequest;
      int bottomRequest;

      synchronized (buttonLock)
      {
        topRequest = getTopRequest();
        bottomRequest = getBottomRequest();
      }

      if (currentDir == DIR_DOWN)
      {
        if (bottomRequest != -1)
        {
          if (bottomRequest < currentLoc)
          {
            destination = bottomRequest;
            return;
          }
          else if (bottomRequest > currentLoc)
          {
            currentDir = DIR_UP;
            sendDirEvent(DIR_UP);
          }
        }
        /* If there are no requests, or the only requests are on this floor,
         * switch to stationary mode */
        currentDir = DIR_STATIONARY;
        sendDirEvent(DIR_STATIONARY);
      }
      else if (currentDir == DIR_UP)
      {
        if (topRequest != -1)
        {
          if (topRequest > currentLoc)
          {
            destination = topRequest;
            return;
          }
          else if (topRequest < currentLoc)
          {
            currentDir = DIR_DOWN;
            sendDirEvent(DIR_DOWN);
          }
        }
        /* If there are no requests, or the only requests are on this floor,
         * switch to stationary mode */
        currentDir = DIR_STATIONARY;
        sendDirEvent(DIR_STATIONARY);
      }
      else // (currentDir == DIR_STATIONARY)
      {
        if (floors[currentLoc].requestAt() ||
            doorOpenButton ||
            doorCloseButton)
        {
          openDoors();
          continue;
        }

        if (topRequest == -1 ||
            (topRequest == currentLoc && bottomRequest == currentLoc))
        {
          if (!statePrinted)
          {
            printState();
            debug("chooseNextDest: waiting for request...");
            statePrinted = true;
          }
          /* If there are no requests, or the only requests are on this floor,
           * wait for a request to arrive */
          try
          {
            synchronized (buttonLock)
            {
              buttonLock.wait(WAIT_FOR_REQUEST_TIME);
            }
          }
          catch (InterruptedException iEx)
          {
            iEx.printStackTrace();
          }
        }
        else // There is a request, go to that floor
        {
          destination = (topRequest == currentLoc ? bottomRequest :
                         topRequest);
          if (destination < currentLoc)
          {
            currentDir = DIR_DOWN;
            sendDirEvent(DIR_DOWN);
          }
          else // (destination > currentLoc)
          {
            currentDir = DIR_UP;
            sendDirEvent(DIR_UP);
          }
          return;
        }
      }
    }
  }

// Motion control methods

  private void moveUp()
  {
    if (currentLoc + 1 < numFloors)
    {
      sendStartedMovingEvent(currentLoc, currentLoc + 1);
      try
      {
        Thread.sleep(MOVE_TIME);
      }
      catch (InterruptedException iEx)
      {
        iEx.printStackTrace();
      }
      currentLoc++;
      arriveAtFloor();
    }
  }

  private void moveDown()
  {
    if (currentLoc > 0)
    {
      sendStartedMovingEvent(currentLoc, currentLoc - 1);
      try
      {
        Thread.sleep(MOVE_TIME);
      }
      catch (InterruptedException iEx)
      {
        iEx.printStackTrace();
      }
      currentLoc--;
      arriveAtFloor();
    }
  }

// Event dispersion methods

  private void publish(ElevatorEvent event)
  {
    int size = listeners.size();
    for (int i = 0; i < size; i++)
    {
      ElevatorListener el = (ElevatorListener) listeners.get(i);
      if (el != null)
        el.receiveEvent(event);
    }
  }

  // ElevatorStatusEvents

  private void sendFloorEvent(int floor)
  {
    publish(new ElevatorEvent.FloorReachedEvent(floor));
  }

  private void sendDirEvent(int dir)
  {
    publish(new ElevatorEvent.DirChangedEvent(dir));
  }

  private void sendStartedMovingEvent(int oldFloor, int newFloor)
  {
    publish(new ElevatorEvent.StartedMovingEvent(oldFloor, newFloor));
  }

  private void sendDoorEvent(boolean started, boolean isOpen)
  {
    publish(new ElevatorEvent.DoorOpenEvent(started, isOpen));
  }

  // ButtonEvents

  private void sendCallButtonEvent(int floor, int dir, boolean newVal)
  {
    publish(new ElevatorEvent.CallButtonEvent(floor, dir, newVal));
  }

  private void sendFloorRequestButtonEvent(int floor, boolean newVal)
  {
    publish(new ElevatorEvent.FloorRequestButtonEvent(floor, newVal));
  }

  private void sendDoorOpenButtonEvent(boolean newVal)
  {
    publish(new ElevatorEvent.DoorOpenButtonEvent(newVal));
  }

  private void sendDoorCloseButtonEvent(boolean newVal)
  {
    publish(new ElevatorEvent.DoorCloseButtonEvent(newVal));
  }

  // UserEvents

  private void sendExistenceEvent(AbstractUser user, boolean exists)
  {
    publish(new ElevatorEvent.ExistenceEvent(user, exists));
  }

  private void sendTeleportEvent(AbstractUser user, int oldFloor,
                                 boolean wasInside)
  {
    publish(new ElevatorEvent.TeleportEvent(user, oldFloor, wasInside));
  }

  private void sendEnteringEvent(AbstractUser user, boolean started)
  {
    publish(new ElevatorEvent.EnteringEvent(user, started));
  }

  private void sendExitingEvent(AbstractUser user, boolean started)
  {
    publish(new ElevatorEvent.ExitingEvent(user, started));
  }

// Notification methods

  private void arriveAtFloor()
  {
    printState();
    sendFloorEvent(currentLoc);

    openDoors();

    if (currentLoc == destination)
    {
      // reachedDestination();
      chooseNextDest(); // Could change dir, if so pick up passengers in new dir
      pickupDown();
      pickupUp();
    }
  }

  /**
   * Called whenever the doors should be open.  Like the chooseNextDest
   * method, the synchronization of this method is precise, and small
   * modifications can introduce subtle bugs.
   *
   * The basic idea of this method's operation is as follows:
   *
   * First, make sure you have any reason to open the doors at all; if there
   * is no request directed at this floor, and the doorOpenButton is off, then
   * do nothing at all and continue moving.
   *
   * If there is a request or the doorOpenButton is on, then first turn off
   * all the requests for this floor.  Then send an event saying the doors
   * are opening, sleep for the DOOR_OPENING_TIME, and send an event saying
   * the doors have opened.
   *
   * Keep the doors open for DOOR_OPEN_TIME, unless the doorCloseButton is
   * pressed.
   *
   * Check to see if there are still any requests at this floor, if the
   * doorOpenButton is on, or if there are any users moving into or out of
   * the elevator.  If there are, sleep for DOOR_KEEP_OPEN_TIME and then check
   * again.  Otherwise, send an event closing the door, sleep for
   * DOOR_OPEN_TIME, and finally send an event saying the door has closed.
   */
  private void openDoors()
  {
    if (floors[currentLoc].stopAt() || doorOpenButton)
    {
      try
      {

        letOff();
        pickupDown();
        pickupUp();

        synchronized (doorLock)
        {
          sendDoorEvent(STARTED, true);
          debug("[ Opening doors...");
          Thread.sleep(DOOR_OPENING_TIME);
          doorsOpen = true;
          sendDoorEvent(ENDED, true);
        }

        synchronized (doorButtonLock)
        {
          if (!doorCloseButton)
            doorButtonLock.wait(DOOR_OPEN_TIME);
          if (doorCloseButton)
          {
            doorCloseButton = false;
            sendDoorCloseButtonEvent(false);
          }
        }

        while (doorOpenButton ||
               floors[currentLoc].stopAt() ||
               numUsersBlockingDoor > 0)
        {
          letOff();
          pickupDown();
          pickupUp();
          if (doorOpenButton)
          {
            doorOpenButton = false;
            sendDoorOpenButtonEvent(false);
          }
          Thread.sleep(DOOR_KEEP_OPEN_TIME);
        }


        synchronized (doorLock)
        {
          doorsOpen = false;
          sendDoorEvent(STARTED, false);
          Thread.sleep(DOOR_OPENING_TIME);
          debug("] Doors closed");
          sendDoorEvent(ENDED, false);
        }

      }
      catch (InterruptedException iEx)
      {
        iEx.printStackTrace();
      }
    }
  }

  private void printState()
  {
    debug("At floor " + floors[currentLoc] + ", " + getDirName(currentDir));
  }

  private void reachedDestination()
  {
    debug("Destination reached at floor " + floors[destination]);
  }

  private void pickupDown()
  {
    if (currentDir != DIR_UP && floors[currentLoc].downCalledFor)
    {
      synchronized (buttonLock)
      {
        debug("Picked up down-going passengers at floor " +
              floors[currentLoc]);
        floors[currentLoc].downCalledFor = false;
      }
      sendCallButtonEvent(currentLoc, DIR_DOWN, false);
    }
  }

  private synchronized void pickupUp()
  {
    if (currentDir != DIR_DOWN && floors[currentLoc].upCalledFor)
    {
      synchronized (buttonLock)
      {
        debug("Picked up up-going passengers at floor " +
              floors[currentLoc]);
        floors[currentLoc].upCalledFor = false;
      }
      sendCallButtonEvent(currentLoc, DIR_UP, false);
    }
  }

  private synchronized void letOff()
  {
    if (floors[currentLoc].stopRequestedAt)
    {
      synchronized (buttonLock)
      {
        debug("Let off passengers at floor " + floors[currentLoc]);
        floors[currentLoc].stopRequestedAt = false;
      }
      sendFloorRequestButtonEvent(currentLoc, false);
    }
  }

  /**
   * Each logical floor in the elevator is represented by an instance of this
   * Floor class.  This class maintains information about the requests at
   * this floor, the floor's name, and its logical (0-indexed) number.
   */
  private class Floor
  {

    protected boolean upCalledFor;
    protected boolean downCalledFor;
    protected boolean stopRequestedAt;
    protected boolean enabled;
    protected String name;
    protected int num;

    private Floor(String name, int num)
    {
      upCalledFor = downCalledFor = stopRequestedAt = false;
      enabled = true;
      this.name = name;
      this.num = num;
    }

    protected boolean requestAt()
    {
      return (upCalledFor || downCalledFor || stopRequestedAt);
    }

    protected boolean stopAt()
    {
      return ((upCalledFor && currentDir != DIR_DOWN) ||
              (downCalledFor && currentDir != DIR_UP) ||
              stopRequestedAt);
    }

    public String toString() { return name; }

  }

  /**
   * Utility method to create the floor array
   */
  protected Floor[] createFloors()
  {
    Floor[] floors = new Floor[numFloors];
    for (int i = 0; i < numFloors; i++)
    {
      floors[i] = new Floor(String.valueOf(i+1), i);
    }
    return floors;
  }


// Main execution thread

  /**
   * Controls the primary operation of the elevator.  Continuously loops,
   * moving up or down based on where the next destination is.  As destinations
   * are reached, the moveDown and moveUp methods will handle calling
   * chooseNextDest().
   */
  private class MainThread extends Thread
  {

    public void run()
    {
      chooseNextDest();
      while (true)
      {
        if (destination < currentLoc)
        {
          if (currentDir == DIR_DOWN)
            moveDown();
          else
          {
            debug("Dest is down, but elevator is not moving down!");
            return;
          }
        }
        else if ( destination > currentLoc)
        {
          if (currentDir == DIR_UP)
            moveUp();
          else
          {
            debug("Dest is up, but elevator is not moving up!");
            return;
          }
        }
      }
    }

  }

}