package edu.cmu.hcii.puc.devices.elevator;

import java.awt.Color;

/**
 * Base class for any abstraction of a "User" in the elevator system.
 */
public abstract class AbstractUser implements ElevatorListener
{

  // Constants


  /**
   * The user's state is tracked as one of 4 possible places: outside, entering,
   * inside, or exiting.
   */
  public static final int OUTSIDE = 1;
  public static final int ENTERING = 2;
  public static final int INSIDE = 3;
  public static final int EXITING = 4;

  /**
   * Each user has an associated Color; colors are assigned to users by
   * cycling through a list of 10 possible colors, stored in this static list
   */
  private static final Color[] shirtColors = new Color[]
      {
      Color.red,
      Color.blue,
      Color.green,
      Color.yellow,
      Color.cyan,
      Color.gray,
      Color.magenta,
      Color.orange,
      Color.pink,
      Color.black
  };

  /**
   * The names of the possible colors are also stored in a static array, so
   * users can have a friendly description of their color for display purposes
   */
  private static final String[] colorNames = new String[]
      {
      "Red",
      "Blue",
      "Green",
      "Yellow",
      "Cyan",
      "Gray",
      "Magenta",
      "Orange",
      "Pink",
      "Black"
  };

  private static int nextColorNum = 0;

  // Instance fields

  protected ElevatorModel model;
  private EventQueue events;

  protected int status;
  protected int currentLoc;
  protected int posInLine;
  protected boolean[] haveAccess;

  private Color shirtColor;
  private String colorName;

  // Constructors

  /**
   * Constructs a user, and by default starts the user at location 0 (the
   * bottom floor)
   *
   * @param model The model for the user to interact with
   */
  public AbstractUser(ElevatorModel model)
  {
    this(model, 0);
  }

  /**
   * Constructs a user, and puts them on the specified starting floor
   *
   * @param model The model for the user to interact with
   * @param startingLoc The floor to start on.  Must be between 0 and
   * model.getNumFloors() - 1, or undetermined (but probably bad) behavior
   * is likely.
   */
  public AbstractUser(ElevatorModel model, int startingLoc)
  {
    int colorNum = getNextColorNum();
    shirtColor = shirtColors[colorNum];
    colorName = colorNames[colorNum];

    this.model = model;
    currentLoc = startingLoc;
    posInLine = 0;
    status = OUTSIDE;
    events = new EventQueue();

    haveAccess = model.getAccess(null);

    model.addUser(this);

    new EventThread(this, events).start();
  }

  // Destructor

  /**
   * Should be called whenever a User thinks it is permanently done interacting
   * with the system, so the model can remove this User gracefully.
   */
  public synchronized void destroy()
  {
    model.removeUser(this);
  }

  // Shirt methods

  private static int getNextColorNum()
  {
    int num = nextColorNum++;
    if (nextColorNum > 9)
      nextColorNum = 0;
    return num;
  }

  public Color getColor()
  {
    return shirtColor;
  }

  public String getColorName()
  {
    return colorName;
  }

  // Other implemented methods

  /**
   * This user will attempt to enter the elevator, assuming their current
   * position is outside (i.e. not entering, inside, or exiting).  If they
   * fail, they will do so gracefully by throwing an ElevatorException.
   *
   * @throws ElevatorException Signifies the elevator model would not allow
   * this user to enter, for one of a variety of possible reasons.
   */
  public synchronized final void enter() throws ElevatorException
  {
    if (status == OUTSIDE)
    {
      model.enter(this);
      status = ENTERING;
      updateStatus();
    }
  }

  /**
   * This user will attempt to exit the elevator, assuming their current
   * position is inside (i.e. not exiting, outside, or entering).  If they fail,
   * they will do so gracefully by throwing an ElevatorException.
   *
   * @throws ElevatorException Signifies the elevator model would not allow
   * this user to exit, for one of a variety of possible reasons.
   */
  public synchronized final void exit() throws ElevatorException
  {
    if (status == INSIDE)
    {
      model.exit(this);
      status = EXITING;
      updateStatus();
      updateCallButtons();
    }
  }

  /**
   * The user's location (i.e. the floor they are on).
   *
   * @return The 0-indexed floor the user is currently at.
   */
  public final int getLocation()
  {
    return currentLoc;
  }

  /**
   * Tests if the user is moving into/out of the elevator
   *
   * @return true if the user in entering or exiting, otherwise false
   */
  public final boolean isTransitioning()
  {
    return ((status == ENTERING) || (status == EXITING));
  }

  /**
   * Reset this user's access permissions, by checking the validity of the
   * given access code.
   *
   * @param accessCode The access code to use to get permissions.  A value of
   * null is allowed, but will usually generate the minimal set of permissions.
   */
  public final void getAccess(String accessCode)
  {
    this.haveAccess = model.getAccess(accessCode);
    updateAccess();
  }

  /**
   * If the user is inside the elevator and has permissions to access the
   * given floor, this will send the model a request to go to that floor
   *
   * @param floor The floor to go to
   * @return true if the request was processed, otherwise false
   */
  public final boolean requestFloor(int floor)
  {
    if ((status == INSIDE) && haveAccess != null && haveAccess[floor])
    {
      model.requestStop(floor);
      return true;
    }
    else
      return false;
  }

  /**
   * If the user is outside, press the "Up" button
   *
   * @return The floor the request occurred at (this user's current location)
   */
  public final int requestUp()
  {
    if (status == OUTSIDE)
    {
      model.call(currentLoc, ElevatorModel.DIR_UP);
      return currentLoc;
    }
    else
      return -1;
  }

  /**
   * Tests whether up is requested at this user's current location
   *
   * @return true if up is requested at this floor
   */
  public final boolean isUpRequested()
  {
    return model.upRequestedAt(currentLoc);
  }

  /**
   * If the user is outside, press the "Down" button
   *
   * @return The floor the request occurred at (this user's current location)
   */
  public final int requestDown()
  {
    if (status == OUTSIDE)
    {
      model.call(currentLoc, ElevatorModel.DIR_DOWN);
      return currentLoc;
    }
    else
      return -1;
  }

  /**
   * Tests whether down is requested at this user's current location
   *
   * @return true if down is requested at this floor
   */
  public final boolean isDownRequested()
  {
    return model.downRequestedAt(currentLoc);
  }

  /**
   * Generates a user-friendly name for this AbstractUser
   *
   * @return The name to display
   */
  public String toString()
  {
    return getName() + " @ " + model.getFloorName(currentLoc);
  }

  // ElevatorListener method & event processing machinery

  public void receiveEvent(ElevatorEvent event)
  {
    events.append(event);
  }

  public boolean processEvent(ElevatorEvent event)
  {
    if (event instanceof ElevatorEvent.FloorReachedEvent)
    {
      ElevatorEvent.FloorReachedEvent e =
          (ElevatorEvent.FloorReachedEvent) event;
      handleFloorReached(e.floor);
      return true;
    }
    else if (event instanceof ElevatorEvent.CallButtonEvent)
    {
      ElevatorEvent.CallButtonEvent e =
          (ElevatorEvent.CallButtonEvent) event;
      handleCallButtonSet(e.floor, e.dir, e.newVal);
      return true;
    }
    else if (event instanceof ElevatorEvent.EnteringEvent)
    {
      ElevatorEvent.EnteringEvent e =
          (ElevatorEvent.EnteringEvent) event;
      if ((e.user == this) && (e.starting == ElevatorModel.ENDED))
        handleFinishedEntering();
      return true;
    }
    else if (event instanceof ElevatorEvent.ExitingEvent)
    {
      ElevatorEvent.ExitingEvent e =
          (ElevatorEvent.ExitingEvent) event;
      if ((e.user == this) && (e.starting == ElevatorModel.ENDED))
        handleFinishedExiting();
      return true;
    }
    else
      return false;
  }

  public void handleFloorReached(int floor)
  {
    if (status == INSIDE)
    {
      currentLoc = floor;
      updateFloor();
    }
  }

  public void handleCallButtonSet(int floor, int dir, boolean newVal)
  {
    if ((status == OUTSIDE) && (floor == currentLoc))
      updateCallButtons();
  }

  public void handleFinishedEntering()
  {
    status = INSIDE;
    updateStatus();
  }

  public void handleFinishedExiting()
  {
    status = OUTSIDE;
    updateStatus();
  }

  // Fun extra method

  public synchronized boolean teleport(int newFloor)
  {
    if ((status == INSIDE) || status == OUTSIDE)
    {
      // Let the model know that the teleport is occurring!
      int oldLoc = currentLoc;
      int oldStatus = status;

      currentLoc = newFloor;
      status = OUTSIDE;

      updateStatus();
      updateFloor();
      updateCallButtons();
      model.teleported(this, oldLoc, (oldStatus == INSIDE));
      return true;
    }
    else
      return false;
  }

  // Abstract methods

  /**
   * Called whenever the user moves into or out of the elevator
   */
  protected abstract void updateStatus();

  /**
   * Called whenever the user's floor changes
   */
  protected abstract void updateFloor();

  /**
   * Called whenever either of the call buttons on the user's current floor
   * are changed, if and only if the user is not in the elevator (if they are
   * in the elevator, they cannot see the call buttons so presumably they
   * don't care)
   */
  protected abstract void updateCallButtons();

  /**
   * Called whenever the list of accessible floors for this user changes in
   * any way
   */
  protected abstract void updateAccess();

  /**
   * Get a printable name for this user
   * @return The name
   */
  protected abstract String getName();

}