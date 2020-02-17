package edu.cmu.hcii.puc.devices.elevator;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * The main GUI display for an ElevatorModel object
 */
public class ElevatorPanel extends JPanel implements ElevatorListener
{

// Constants

  // Sizes

  private static final int SHAFT_WIDTH = 120;
  private static final int DOOR_WIDTH = 30;
  private static final int WAITING_AREA_WIDTH = 150;
  private static final int NAME_WIDTH = 80;
  private static final int FLOOR_WIDTH = NAME_WIDTH + WAITING_AREA_WIDTH
      + DOOR_WIDTH;
  private static final int FLOOR_HEIGHT = 80;
  private static final Dimension ELEV_SIZE =
      new Dimension(SHAFT_WIDTH - 1, FLOOR_HEIGHT - 1);

  // Colors

  private static final Color FLOOR_COLOR = new Color(200, 200, 200);
  private static final Color DOOR_COLOR = new Color(128, 128, 128);
  private static final Color WALL_COLOR = new Color(255, 235, 205);
  private static final Color ABOVE_ELEV_COLOR = new Color(255, 248, 220);
  private static final Color REQUESTED_COLOR = new Color(220, 20, 60);

  // Other

  private final int numFloors;
  private final Point[] elevLocs;
  private final int[] outsideSpots;
  private final int[] insideSpots;
  private final Dimension size;

  // Logical instance variables

  private ElevatorModel model;
  private EventQueue events;

  private int currentFloor;
  private int currentDir;
  private boolean doorsOpen;

  // Graphical instance variables

  private Floor[] floors;
  private ElevatorShaft shaft;
  private Image person;

  // User tracking

  private ArrayList movingPeople;

  // Constructors

  public ElevatorPanel()
  {
    this(new ElevatorModel());
  }

  public ElevatorPanel(ElevatorModel model)
  {
    setOpaque(true);

    this.model = model;
    numFloors = model.getNumFloors();

    elevLocs = new Point[numFloors];
    int elevLocY = (numFloors - 1) * FLOOR_HEIGHT;
    for (int i = 0; i < numFloors; i++)
    {
      elevLocs[i] = new Point(FLOOR_WIDTH, elevLocY);
      elevLocY -= FLOOR_HEIGHT;
    }

    outsideSpots = new int[4];
    int rightMostSpot = FLOOR_WIDTH - 75;
    for (int i = 0; i < 4; i++)
    {
      outsideSpots[i] = rightMostSpot;
      rightMostSpot -= 30;
    }

    insideSpots = new int[3];
    int leftMostSpot = FLOOR_WIDTH + 5;
    for (int i = 0; i < 3; i++)
    {
      insideSpots[i] = leftMostSpot;
      leftMostSpot += 30;
    }

    person = loadImage("person.gif");

    size = new Dimension(SHAFT_WIDTH + FLOOR_WIDTH + 1,
                         (FLOOR_HEIGHT * numFloors) + 1);

    events = new EventQueue();
    movingPeople = new ArrayList();

    // Set up floors and elevator shaft
    floors = new Floor[numFloors];
    for (int i = 0; i < numFloors; i++)
    {
      floors[i] = new Floor(model.getFloorName(i), i);
    }
    shaft = new ElevatorShaft();

    handleFloorReached(model.getCurrentLoc());
    handleDirChanged(model.getCurrentDir());
    model.subscribe(this);
    new EventThread(this, events).start();
  }

  // GUI methods

  public Dimension getMinimumSize() { return size; }
  public Dimension getPreferredSize() { return size; }

  public void paint(Graphics g)
  {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setColor(Color.white);
    g2d.fill(new Rectangle(size));
    g2d.setFont(g2d.getFont().deriveFont(Font.BOLD));

    for (int i = 0; i < numFloors; i++)
    {
      floors[i].paint(g2d);
    }
    shaft.paint(g2d);

    int size = movingPeople.size();
    for (int i = 0; i < size; i++)
    {
      MovingPerson mp = (MovingPerson) movingPeople.get(i);
      drawPerson(g2d, mp.user.getColor(),
                 mp.location, floors[mp.floor].boundary.y + 20);
    }
  }

  // ElevatorListener methods dealing with actual status of elevator

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
    else if (event instanceof ElevatorEvent.StartedMovingEvent)
    {
      ElevatorEvent.StartedMovingEvent e =
          (ElevatorEvent.StartedMovingEvent) event;
      long finishByTime = e.getTimecode() + ElevatorModel.MOVE_TIME;
      handleStartedMoving(e.oldFloor, e.newFloor, finishByTime);
      return true;
    }
    else if (event instanceof ElevatorEvent.DirChangedEvent)
    {
      ElevatorEvent.DirChangedEvent e =
          (ElevatorEvent.DirChangedEvent) event;
      handleDirChanged(e.dir);
      return true;
    }
    else if (event instanceof ElevatorEvent.DoorOpenEvent)
    {
      ElevatorEvent.DoorOpenEvent e =
          (ElevatorEvent.DoorOpenEvent) event;
      long finishByTime = e.getTimecode() + ElevatorModel.DOOR_OPENING_TIME;
      handleDoorOpen(e.started, e.isOpen, finishByTime);
      return true;
    }
    else if (event instanceof ElevatorEvent.CallButtonEvent)
    {
      ElevatorEvent.CallButtonEvent e =
          (ElevatorEvent.CallButtonEvent) event;
      handleCallButtonSet(e.floor, e.dir, e.newVal);
      return true;
    }
    else if (event instanceof ElevatorEvent.FloorRequestButtonEvent)
    {
      ElevatorEvent.FloorRequestButtonEvent e =
          (ElevatorEvent.FloorRequestButtonEvent) event;
      handleFloorRequestButtonSet(e.floor, e.newVal);
      return true;
    }
    else if (event instanceof ElevatorEvent.ExistenceEvent)
    {
      ElevatorEvent.ExistenceEvent e =
          (ElevatorEvent.ExistenceEvent) event;
      handleExistenceEvent(e.user, e.exists);
      return true;
    }
    else if (event instanceof ElevatorEvent.TeleportEvent)
    {
      ElevatorEvent.TeleportEvent e =
          (ElevatorEvent.TeleportEvent) event;
      handleTeleportEvent(e.user, e.oldFloor, e.wasInside);
      return true;
    }
    else if (event instanceof ElevatorEvent.EnteringEvent)
    {
      ElevatorEvent.EnteringEvent e =
          (ElevatorEvent.EnteringEvent) event;
      if (e.starting == ElevatorModel.STARTED)
        handleEnteringEvent(e.user);
      return true;
    }
    else if (event instanceof ElevatorEvent.ExitingEvent)
    {
      ElevatorEvent.ExitingEvent e =
          (ElevatorEvent.ExitingEvent) event;
      if (e.starting == ElevatorModel.STARTED)
        handleExitingEvent(e.user);
      return true;
    }
    else
      return false;
  }

  // ElevatorStatusEvents

  public void handleFloorReached(int floor)
  {
    floors[currentFloor].setAtThisFloor(false);
    currentFloor = floor;
    floors[currentFloor].setAtThisFloor(true);
  }

  public void handleStartedMoving(int oldFloor, int newFloor, long finishByTime)
  {
    shaft.moveElevator(oldFloor, newFloor, finishByTime);
  }

  public void handleDirChanged(int dir)
  {
    currentDir = dir;
  }

  public void handleDoorOpen(boolean started, boolean isOpen, long finishByTime)
  {
    if (started == ElevatorModel.STARTED)
    {
      floors[currentFloor].setDoorsOpen(isOpen, finishByTime);
      doorsOpen = isOpen;
    }
  }

  // ButtonEvents

  public void handleCallButtonSet(int floor, int dir, boolean newVal)
  {
    floors[floor].setCallButton(dir, newVal);
  }

  public void handleFloorRequestButtonSet(int floor, boolean newVal)
  {
    shaft.setRequested(floor, newVal);
  }

  // UserEvents

  public void handleExistenceEvent(AbstractUser user, boolean exists)
  {
    if (exists)
    {
      floors[user.currentLoc].claimLoc(user);
    }
    else
    {
      if ((user.status == AbstractUser.INSIDE) ||
          (user.status == AbstractUser.ENTERING))
      {
        shaft.leaveLoc(user);
      }
      else
      {
        floors[user.currentLoc].leaveLoc(user);
      }
    }
    repaint();
  }

  public void handleTeleportEvent(AbstractUser user, int oldFloor,
                                  boolean wasInside)
  {
    if (wasInside)
    {
      shaft.leaveLoc(user);
    }
    else
    {
      floors[oldFloor].leaveLoc(user);
    }
    floors[user.currentLoc].claimLoc(user);
    repaint();
  }

  public void handleEnteringEvent(AbstractUser user)
  {
    int oldLoc = outsideSpots[user.posInLine];
    floors[user.currentLoc].leaveLoc(user);
    shaft.claimLoc(user);
    int newLoc = insideSpots[user.posInLine];

    MovingPerson mp = new MovingPerson(user, oldLoc, 1);
    movingPeople.add(mp);

    int interval = (int) ( (double) ElevatorModel.ENTER_TIME * .62 /
                      ( (newLoc - oldLoc) /
                       MovingPerson.DIST_TO_MOVE));
    Timer t = new Timer(interval, mp);
    t.start();
  }

  public void handleExitingEvent(AbstractUser user)
  {
    int oldLoc = insideSpots[user.posInLine];
    shaft.leaveLoc(user);
    floors[user.currentLoc].claimLoc(user);
    int newLoc = outsideSpots[user.posInLine];

    MovingPerson mp = new MovingPerson(user, oldLoc, -1);
    movingPeople.add(mp);

    int interval = (int) ( (double) ElevatorModel.EXIT_TIME * .62 /
                      ( (oldLoc - newLoc) /
                       MovingPerson.DIST_TO_MOVE));
    Timer t = new Timer(interval, mp);
    t.start();
  }

  /**
   * The ElevatorPanel object keeps a list of all people who are currently
   * in motion (moving into or out of the elevator, not moving with the
   * elevator as it goes up or down).  This class represents these people,
   * by running a timer which shifts their location incrementally and
   * terminates when either the destination is reached or the user's status
   * changes from a transition state (entering or exiting) to inside or outside.
   */
  private class MovingPerson implements ActionListener
  {
    public static final int DIST_TO_MOVE = 2;

    private AbstractUser user;
    private int floor;
    private int dir; // 1 is to the right (entering), 0 is to the left (exiting)
    private int location;

    public MovingPerson(AbstractUser user, int oldLoc, int dir)
    {
      this.user = user;
      floor = user.currentLoc;
      this.dir = dir;
      location = oldLoc;
    }

    public void actionPerformed(ActionEvent ae)
    {
      if (!user.isTransitioning())
      {
        stopMoving((Timer) ae.getSource());
      }
      else if ((dir == 1 && location >= insideSpots[user.posInLine]) ||
               (dir == -1 && location <= outsideSpots[user.posInLine]))
      {
        dir = 0;
      }
      else if (dir != 0)
      {
        location += dir * DIST_TO_MOVE;
        ElevatorPanel.this.repaint();
      }
    }

    private void stopMoving(Timer t)
    {
      t.stop();
      movingPeople.remove(this);
      ElevatorPanel.this.repaint();
    }

  }

  /**
   * Utility method for drawing a person
   *
   * @param g The Graphics2D object to paint with
   * @param c The color to draw the shirt in
   * @param x The x coordinate of the top-left corner to draw the image in
   * @param y The y coordinate of the top-left corner to draw the image in
   */
  private void drawPerson(Graphics2D g, Color c, int x, int y)
  {
    g.drawImage(person, x, y, this);
    g.setColor(Color.black);
    g.drawRect(x+6, y+24, 7, 14);
    g.setColor(c);
    g.fillRect(x+7, y+25, 6, 13);
  }

  /**
   * Each floor in the model is represented graphically by a Floor object.
   *
   * Each Floor object controls painting of its own name, any users which are
   * waiting at it, its up and down call buttons, and the doors to the
   * elevator.
   */
  private class Floor
  {

    private final int DOOR_INTERVAL = (int)
        ((double) ElevatorModel.DOOR_OPENING_TIME * .8 / DoorMover.NUM_INCRS);

    // Instance variables

    // Rectangles
    private Rectangle boundary;
    private Rectangle upButton;
    private Rectangle downButton;

    // Floor info
    private String name;
    private int num;

    // Door stuff
    private int doorState; // 0 is totally open, 5 is totally closed
    private boolean doorsMoving;
    private int targetDoorState;
    private long doorFinishByTime;

    // At floor
    private boolean atThisFloor;

    // Call buttons
    private boolean upCalled;
    private boolean downCalled;

    // People
    private AbstractUser[] usersHere;
    private ArrayList excessUsers;

    // Constructor

    public Floor(String name, int num)
    {
      this.name = name;
      this.num = num;
      doorState = 5;
      upCalled = downCalled = false;

      usersHere = new AbstractUser[] {null, null, null, null};
      excessUsers = new ArrayList();

      boundary = new Rectangle(0, (numFloors - num - 1) * FLOOR_HEIGHT,
                               FLOOR_WIDTH, FLOOR_HEIGHT);
      upButton = new Rectangle(FLOOR_WIDTH - DOOR_WIDTH - 18, boundary.y + 15,
                               10, 10);
      downButton = new Rectangle(upButton.x, upButton.y + 15, upButton.width,
                                 upButton.height);
    }

    protected void paint(Graphics2D g)
    {
      g.setColor(Color.black);
      g.draw(boundary);

      Rectangle wall = new Rectangle(boundary.x + 1, boundary.y + 1,
                                     boundary.width - 1, 60);
      g.setColor(WALL_COLOR);
      g.fill(wall);

      Rectangle floor = new Rectangle(boundary.x + 1, boundary.y + 60,
                                      boundary.width - 1, 20);
      g.setColor(FLOOR_COLOR);
      g.fill(floor);

      g.setColor(Color.black);
      g.drawLine(boundary.x, boundary.y + 60, boundary.x + boundary.width,
                 boundary.y + 60);

      g.drawString("Floor " + name, boundary.x + 15, boundary.y + 35);

      int rightEdge = boundary.x + boundary.width;
      int leftEdge = rightEdge - 30;

      Polygon aboveElev = new Polygon();
      aboveElev.addPoint(leftEdge, boundary.y + 1);
      aboveElev.addPoint(rightEdge, boundary.y + 1);
      aboveElev.addPoint(rightEdge, boundary.y + 20);
      if (atThisFloor)
        g.setColor(Color.yellow);
      else
        g.setColor(ABOVE_ELEV_COLOR);
      g.fill(aboveElev);

      int doorShowingWidth = 3 * doorState;
      int doorVerticalOffset = 2 * doorState;

      Polygon leftDoor = new Polygon();
      leftDoor.addPoint(leftEdge, boundary.y);
      leftDoor.addPoint(leftEdge + doorShowingWidth,
                        boundary.y + doorVerticalOffset);
      leftDoor.addPoint(leftEdge + doorShowingWidth,
                        boundary.y + 60 + doorVerticalOffset);
      leftDoor.addPoint(leftEdge, boundary.y + 60);

      Polygon rightDoor = new Polygon();
      rightDoor.addPoint(rightEdge, boundary.y + 20);
      rightDoor.addPoint(rightEdge - doorShowingWidth,
                         boundary.y + 20 - doorVerticalOffset);
      rightDoor.addPoint(rightEdge - doorShowingWidth,
                         boundary.y + 80 - doorVerticalOffset);
      rightDoor.addPoint(rightEdge, boundary.y + 80);

      g.setColor(DOOR_COLOR);
      g.fill(leftDoor);
      g.fill(rightDoor);

      g.setColor(Color.black);
      g.draw(leftDoor);
      g.draw(rightDoor);
      g.drawLine(leftEdge, boundary.y + 60, rightEdge, boundary.y + 80);
      g.drawLine(rightEdge, boundary.y + 20, leftEdge, boundary.y);

      g.setColor(Color.black);
      if (num != numFloors - 1)
        g.draw(upButton);
      if (num != 0)
        g.draw(downButton);
      g.setColor(Color.gray);
      if (!upCalled && (num != numFloors -1))
        g.fill(upButton);
      if (!downCalled && (num != 0))
        g.fill(downButton);
      g.setColor(Color.yellow);
      if (upCalled && (num != numFloors -1))
        g.fill(upButton);
      if (downCalled && (num != 0))
        g.fill(downButton);

      for (int i = 0; i < 4; i++)
      {
        AbstractUser user = usersHere[i];
        if ((user != null) && !user.isTransitioning())
          drawPerson(g, user.getColor(), outsideSpots[i], boundary.y + 20);
      }

    }

    // Methods for updating state of floor

    protected void setAtThisFloor(boolean atThisFloor)
    {
      this.atThisFloor = atThisFloor;
      ElevatorPanel.this.repaint();
    }

    protected void setCallButton(int dir, boolean newVal)
    {
      if (dir == ElevatorModel.DIR_DOWN)
        downCalled = newVal;
      else
        upCalled = newVal;
      repaint();
    }

    /**
     * Should be called by any user attempting to arrive at this floor, by
     * means of exiting the elevator, teleporting, or being spawned at
     * this floor.  This synchronized method will look for a free spot, and
     * if there is one, will put the user at that spot.
     *
     * If no free spot is available, the user will be placed in the front spot,
     * and the user previously occupying this spot will be pushed into an
     * excessUsers queue.
     *
     * @param user The user moving to this floor
     */
    protected synchronized void claimLoc(AbstractUser user)
    {
      for (int i = 0; i < 4; i++)
      {
        if (usersHere[i] == null)
        {
          usersHere[i] = user;
          user.posInLine = i;
          return;
        }
      }
      user.posInLine = 0;
      excessUsers.add(usersHere[0]);
      usersHere[0] = user;
    }

    /**
     * This method should be called by any user attempting to exit this floor,
     * by entering the elevator, teleporting, or being removed from this
     * model's system.
     *
     * If there are excess users waiting, take the first one of them and
     * put him in the newly created vacancy.
     *
     * @param user The user leaving this floor
     */
    protected synchronized void leaveLoc(AbstractUser user)
    {
      if (user.posInLine == 0)
      {
        if (!excessUsers.remove(user))
        {
          fillSpotFromExcess(0);
        }
      }
      else
      {
       fillSpotFromExcess(user.posInLine);
      }
      user.posInLine = -1;
    }

    /**
     * Utility method to try and fill a vacated spot from the list of
     * excess users.
     *
     * @param spot The number of the spot that was vacated
     */
    protected synchronized void fillSpotFromExcess(int spot)
    {
      AbstractUser replUser;
      if (excessUsers.size() > 0)
      {
        replUser = (AbstractUser) excessUsers.remove(0);
        replUser.posInLine = spot;
      }
      else
        replUser = null;

      usersHere[spot] = replUser;

    }

    protected void setDoorsOpen(boolean doorsOpen, long finishByTime)
    {
      targetDoorState = (doorsOpen? 0 : 5);
      doorFinishByTime = finishByTime;
      if (!doorsMoving)
      {
        Timer t = new Timer(DOOR_INTERVAL, new DoorMover());
        doorsMoving = true;
        t.start();
      }
    }

    /**
     * Utility class to open/close the doors in response to door opening and
     * closing events from the model.
     */
    private class DoorMover implements ActionListener
    {
      public static final int NUM_INCRS = 5;

      private int increment;

      public DoorMover()
      {
        getIncrement();
      }

      private void getIncrement()
      {
        increment = (doorState < targetDoorState? 1 : -1);
      }

      public void actionPerformed(ActionEvent ae)
      {
        if (System.currentTimeMillis() >= doorFinishByTime)
        {
          doorState = targetDoorState;
          ElevatorPanel.this.repaint();
        }

        if (doorState == targetDoorState)
        {
          Timer t = (Timer) ae.getSource();
          t.stop();
          doorsMoving = false;
        }
        else
        {
          getIncrement();
          synchronized (ElevatorPanel.Floor.this)
          {
            doorState += increment;
          }
          ElevatorPanel.this.repaint();
        }
      }
    }

  }

  /**
   * This class represents the Elevator itself.  It controls painting of the
   * elevator, any users inside of the elevator, and the floor request buttons.
   */
  private class ElevatorShaft
  {

    private final int MOVE_INTERVAL =
        (int) ((double) ElevatorModel.MOVE_TIME * .62 / ElevatorMover.NUM_INCRS);

    // Instance variables
    private Rectangle boundary;
    private int currentFloor;

    // Elevator Motion
    private boolean isMoving;
    private long finishByTime;
    private Point location;
    private Point destLoc;

    // People
    private AbstractUser[] usersHere;
    private ArrayList excessUsers;

    // Floor request buttons
    private boolean[] requests;

    // Constructor

    public ElevatorShaft()
    {
      boundary = new Rectangle(FLOOR_WIDTH, 0, SHAFT_WIDTH,
                               FLOOR_HEIGHT * numFloors);
      currentFloor = model.getCurrentLoc();
      isMoving = false;

      location = new Point(elevLocs[currentFloor].x, elevLocs[currentFloor].y);

      usersHere = new AbstractUser[] {null, null, null};
      excessUsers = new ArrayList();

      requests = new boolean[numFloors];
      for (int i = 0; i < numFloors; i++)
        requests[i] = false;
    }

    protected void paint(Graphics2D g)
    {
      g.setColor(Color.black);
      g.draw(boundary);
      g.fill(boundary);

      // Paint the elevator

      Polygon walls = new Polygon();
      walls.addPoint(location.x + 1, location.y + 1);
      walls.addPoint(location.x + 1, location.y + 60);
      walls.addPoint(location.x + 90, location.y + 60);
      walls.addPoint(location.x + 120, location.y + 80);
      walls.addPoint(location.x + 120, location.y + 20);
      walls.addPoint(location.x + 90, location.y + 1);

      g.setColor(WALL_COLOR);
      g.fill(walls);

      Polygon floor = new Polygon();
      floor.addPoint(location.x + 1, location.y + 60);
      floor.addPoint(location.x + 1, location.y + 80);
      floor.addPoint(location.x + 120, location.y + 80);
      floor.addPoint(location.x + 90, location.y + 60);

      g.setColor(FLOOR_COLOR);
      g.fill(floor);

      g.setColor(Color.black);
      g.drawLine(location.x + 1, location.y + 60,
                 location.x + 90, location.y + 60);
      g.drawLine(location.x + 90, location.y + 60,
                 location.x + 120, location.y + 80);
      g.drawLine(location.x + 90, location.y + 60,
                 location.x + 90, location.y + 1);

      for (int i = 0; i < 3; i++)
      {
        AbstractUser user = usersHere[i];
        if ((user != null) && !user.isTransitioning())
          drawPerson(g, usersHere[i].getColor(), insideSpots[i],
                     location.y + 20);
      }

      int x = location.x - 3;
      int y = location.y + 15;
      g.setFont(g.getFont().deriveFont(Font.BOLD));
      for (int i = 0; i < numFloors; i++)
      {
        x += 15;
        if (x >= location.x + 85)
        {
          y += 15;
          x = location.x + 10;
        }
        if (requests[i])
          g.setColor(Color.red);
        else
          g.setColor(Color.black);
        g.drawString(floors[i].name, x, y);
      }

    }

    protected void setRequested(int floor, boolean newVal)
    {
      requests[floor] = newVal;
      ElevatorPanel.this.repaint();
    }

    /**
     * Same as Floor.claimLoc, see documention for that method for details
     *
     * @param user
     */
    protected synchronized void claimLoc(AbstractUser user)
    {
      for (int i = 0; i < 3; i++)
      {
        if (usersHere[i] == null)
        {
          usersHere[i] = user;
          user.posInLine = i;
          return;
        }
      }
      user.posInLine = 0;
      excessUsers.add(usersHere[0]);
      usersHere[0] = user;
    }

    /**
     * Same as Floor.leaveLoc, see documention for that method for details
     *
     * @param user
     */
    protected synchronized void leaveLoc(AbstractUser user)
    {
      if (user.posInLine == 0)
      {
        if (!excessUsers.remove(user))
        {
          fillSpotFromExcess(0);
        }
      }
      else
      {
       fillSpotFromExcess(user.posInLine);
      }
      user.posInLine = -1;
    }

    /**
     * Same as Floor.fillSpotFromExcess, see documentation for that method for
     * details
     *
     * @param spot
     */
    protected synchronized void fillSpotFromExcess(int spot)
    {
      AbstractUser replUser;
      if (excessUsers.size() > 0)
      {
        replUser = (AbstractUser) excessUsers.remove(0);
        replUser.posInLine = spot;
      }
      else
        replUser = null;

      usersHere[spot] = replUser;
    }

    private void moveElevator(int oldFloor, int newFloor, long finishByTime)
    {
      destLoc = elevLocs[newFloor];
      this.finishByTime = finishByTime;

      if (!isMoving)
      {
        int dir = newFloor - oldFloor;
        Timer t = new Timer(MOVE_INTERVAL, new ElevatorMover(dir));
        isMoving = true;
        t.start();
      }
    }

    /**
     * Controls animation of elevator movement, in response to events from
     * the model
     */
    private class ElevatorMover implements ActionListener
    {
      public static final int NUM_INCRS = 20;

      private int dir;
      private int increment;

      public ElevatorMover(int dir)
      {
        this.dir = dir;
        increment = (-dir) * (FLOOR_HEIGHT / NUM_INCRS);
      }

      public void actionPerformed(ActionEvent ae)
      {
        if (System.currentTimeMillis() >= finishByTime)
        {
          location.setLocation(destLoc.x, destLoc.y);
          ElevatorPanel.this.repaint();
        }


        if ((dir == 1 && location.y <= destLoc.y) ||
            (dir == -1 && location.y >= destLoc.y))
        {
          Timer t = (Timer) ae.getSource();
          t.stop();
          isMoving = false;
        }
        else
        {
          synchronized (location)
          {
            location.move(location.x, location.y + increment);
          }
          ElevatorPanel.this.repaint();
        }
      }
    }

  }

  /**
   * Utility method to load an image from a file
   *
   * @param filename The name of the file to load
   * @return The loaded image
   */
  public Image loadImage(String filename)
  {
    MediaTracker tracker = new MediaTracker(new Label(""));
    int nextID = 0;

    Image image;
    try
    {
      URL url = getClass().getResource(filename);
      image = Toolkit.getDefaultToolkit().getImage(url);
      int id = nextID++;
      tracker.addImage(image, id);
      try
      {
        tracker.waitForID(id);
      }
      finally
      {
        tracker.removeImage(image);
      }
    }
    catch (Exception e)
    {
      image = null;
    }

    return image;
  }


}