package edu.cmu.hcii.puc.devices.elevator;

import com.maya.puc.common.*;
import edu.cmu.hcii.puc.devices.*;

import java.util.Hashtable;

/**
 * PUC Device for interaction with the Elevator simulator.  Has the capability
 * of spawning new PUCUser objects as new connections are received.
 */
public class ElevatorDevice extends AbstractDevice2 implements ElevatorListener
{

  // Constants

  public static final String DEVICE_NAME = "Simulated Elevator";
  public static final String SPEC_FILE_NAME = "elevator.xml";
  public static final int DEFAULT_PORT = 5173;

  public static final String STATE_COLOR = "Elevator.Car.Color";
  public static final String STATE_USER_INSIDE = "Elevator.Car.UserState";
  public static final String STATE_ELEVATOR_LOCATION = "Elevator.Car.ElevatorLocation";

  public static final String STATE_ELEVATOR_DIRECTION = "Elevator.Car.ElevatorDirection";
  public static final String SPEC_DIR_DOWN = "3";
  public static final String SPEC_DIR_STATIONARY = "2";
  public static final String SPEC_DIR_UP = "1";

  public static final String STATE_DOORS_OPEN = "Elevator.Car.DoorState";
  public static final String STATE_USER_LOCATION = "Elevator.Car.Outside.UserLocation";
  public static final String STATE_CALL_UP = "Elevator.Car.Outside.Call.Up";
  public static final String STATE_CALL_DOWN = "Elevator.Car.Outside.Call.Down";
  public static final String STATE_ACCESS_CODE = "Elevator.Car.Inside.AccessCode";

  public static final String STATE_ENABLE_PREFIX = "Elevator.Car.Inside.Floor";
  public static final String STATE_ENABLE_POSTFIX = ".Enable";
  public static final String STATE_REQUEST_PREFIX = "Elevator.Car.Inside.Floor";
  public static final String STATE_REQUEST_POSTFIX = ".Request";

  public static final String STATE_DOOR_OPEN = "Elevator.Car.Inside.DoorControl.Open";
  public static final String STATE_DOOR_CLOSE = "Elevator.Car.Inside.DoorControl.Close";

  public static final String COMMAND_ENTER = "Elevator.Car.Outside.Enter";
  public static final String COMMAND_EXIT = "Elevator.Car.Inside.Exit";

  // Instance variables

  private Hashtable users; // Keys are PUCServer.Connections, values are PUCUsers
  private ElevatorModel model;
  private Elevator view;
  private ElevatorConfig config;
  private EventQueue events;

  // Constructor

  public ElevatorDevice()
  {
    users = new Hashtable();
    events = new EventQueue();
    model = new ElevatorModel(5);
    model.subscribe(this);
    view = new Elevator(model);
    initConfig();

    new EventThread(this, events).start();
  }

  // Abstract method implementations

  public void addConnection(PUCServer.Connection c)
  {
    super.addConnection(c);
    PUCUser newUser = new PUCUser(model, c);
    System.out.println("Adding user: " + newUser);
    users.put(c, newUser);
  }

  public void removeConnection(PUCServer.Connection c)
  {
    super.removeConnection(c);
    PUCUser oldUser = (PUCUser) users.remove(c);
    System.out.println("Removing user: " + oldUser);
    if (oldUser != null)
      oldUser.destroy();
  }

  public void handleMessage(PUCServer.Connection c, Message m)
  {
    PUCUser user = (PUCUser) users.get(c);

    if (m instanceof Message.FullStateRequest)
    {
      sendFullState(user);
    }
    else if (m instanceof Message.StateChangeRequest)
    {
      Message.StateChangeRequest scr = (Message.StateChangeRequest)m;
      String newValStr = scr.getValue();

      if (scr.getState().equals(STATE_CALL_UP))
      {
        if (new Boolean(newValStr).booleanValue())
          user.requestUp();
      }
      else if (scr.getState().equals(STATE_CALL_DOWN))
      {
        if (new Boolean(newValStr).booleanValue())
          user.requestDown();
      }
      else if (scr.getState().startsWith(STATE_REQUEST_PREFIX))
      {
        String specFloor = "" + scr.getState().charAt(STATE_REQUEST_PREFIX.length());
        if (new Boolean(newValStr).booleanValue())
        {
          user.requestFloor(specFloorToElevatorFloor(specFloor));
        }
      }
      else if (scr.getState().equals(STATE_ACCESS_CODE))
      {
        user.getAccess(newValStr);
      }
      else if (scr.getState().equals(STATE_DOOR_OPEN))
      {
        if (new Boolean(newValStr).booleanValue())
          model.setDoorOpenButton();
      }
      else if (scr.getState().equals(STATE_DOOR_CLOSE))
      {
        if (new Boolean(newValStr).booleanValue())
          model.setDoorCloseButton();
      }
    }
    else if (m instanceof Message.CommandInvokeRequest)
    {
      Message.CommandInvokeRequest cir = (Message.CommandInvokeRequest)m;
      String command = cir.getCommand();

      if (command.equals(COMMAND_ENTER))
      {
        try
        {
          user.enter();
        }
        catch (ElevatorException eEx)
        {
          c.send(new Message.AlertInformation(eEx.getMessage()));
        }
      }
      else if (command.equals(COMMAND_EXIT))
      {
        try
        {
          user.exit();
        }
        catch (ElevatorException eEx)
        {
          c.send(new Message.AlertInformation(eEx.getMessage()));
        }
      }
    }
    else if (m instanceof Message.SpecRequest)
    {
      try
      {
        c.send(new Message.DeviceSpec(getSpec()));
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

  }

  protected String getSpecFileName() { return SPEC_FILE_NAME; }

  public String getName() { return DEVICE_NAME; }

  protected int getDefaultPort() { return DEFAULT_PORT; }

  private void initConfig()
  {
    config = new ElevatorConfig(model);
  }

  public void configure()
  {
    config.show();
  }

  // Overridden GUI methods

  public boolean hasGUI() { return true; }

  public void setGUIVisibility(boolean isVisible)
  {
    if (isVisible)
      view.show();
    else
      view.hide();
  }

  public boolean isGUIVisible()
  {
    return view.isVisible();
  }

  // ElevatorListener methods

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
      handleDoorOpen(e.started, e.isOpen);
      return true;
    }
    else if (event instanceof ElevatorEvent.DoorCloseButtonEvent)
    {
      ElevatorEvent.DoorCloseButtonEvent e =
          (ElevatorEvent.DoorCloseButtonEvent) event;
      handleDoorCloseButtonSet(e.newVal);
      return true;
    }
    else if (event instanceof ElevatorEvent.DoorOpenButtonEvent)
    {
      ElevatorEvent.DoorOpenButtonEvent e =
          (ElevatorEvent.DoorOpenButtonEvent) event;
      handleDoorOpenButtonSet(e.newVal);
      return true;
    }
    else if (event instanceof ElevatorEvent.FloorRequestButtonEvent)
    {
      ElevatorEvent.FloorRequestButtonEvent e =
          (ElevatorEvent.FloorRequestButtonEvent) event;
      handleFloorRequestButtonSet(e.floor, e.newVal);
      return true;
    }
    else
      return false;

  }

  public void handleFloorReached(int floor)
  {
    dispatchStateEvent(STATE_ELEVATOR_LOCATION, model.getFloorName(floor));
  }

  public void handleDirChanged(int dir)
  {
    dispatchStateEvent(STATE_ELEVATOR_DIRECTION, elevatorDirToSpecDir(dir));
  }

  public void handleDoorOpen(boolean started, boolean isOpen)
  {
    if (((started == ElevatorModel.ENDED) && isOpen) ||
        ((started == ElevatorModel.STARTED) && !isOpen))
      dispatchStateEvent(STATE_DOORS_OPEN, (isOpen? "1" : "2"));
  }

  public void handleFloorRequestButtonSet(int floor, boolean newVal)
  {
    dispatchStateEvent(STATE_REQUEST_PREFIX + model.getFloorName(floor) +
                       STATE_REQUEST_POSTFIX, String.valueOf(newVal));
  }

  public void handleDoorOpenButtonSet(boolean newVal)
  {
    dispatchStateEvent(STATE_DOOR_OPEN, String.valueOf(newVal));
  }

  public void handleDoorCloseButtonSet(boolean newVal)
  {
    dispatchStateEvent(STATE_DOOR_CLOSE, String.valueOf(newVal));
  }

  // Protected utility methods

  protected static String elevatorDirToSpecDir(int elevatorDir)
  {
    switch (elevatorDir)
    {
      case ElevatorModel.DIR_DOWN:
        return SPEC_DIR_DOWN;
      case ElevatorModel.DIR_STATIONARY:
        return SPEC_DIR_STATIONARY;
      case ElevatorModel.DIR_UP:
        return SPEC_DIR_UP;
      default:
        return SPEC_DIR_STATIONARY;
    }
  }

  protected static int specFloorToElevatorFloor(String specFloor)
  {
    return (Integer.parseInt(specFloor) - 1);
  }

  // Private utility methods

  private void sendFullState(PUCUser user)
  {
    PUCServer.Connection c = user.c;
    c.send(new Message.
           StateChangeNotification(STATE_ACCESS_CODE, "Enter code"));
    c.send(new Message.
           StateChangeNotification(STATE_USER_INSIDE,
                                   (user.status == AbstractUser.INSIDE? "2" : "1")
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_COLOR,
                                   user.getColorName()
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_ELEVATOR_LOCATION,
                                   model.getFloorName(model.getCurrentLoc())
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_ELEVATOR_DIRECTION,
                                   elevatorDirToSpecDir(model.getCurrentLoc())
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_DOORS_OPEN,
                                   (model.doorsAreOpen() ? "1" : "2")
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_USER_LOCATION,
                                   model.getFloorName(user.currentLoc)
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_CALL_UP,
                                   String.valueOf(user.isUpRequested())
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_CALL_DOWN,
                                   String.valueOf(user.isDownRequested())
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_DOOR_OPEN,
                                   String.valueOf(model.getDoorOpenButton())
                                   ));
    c.send(new Message.
           StateChangeNotification(STATE_DOOR_CLOSE,
                                   String.valueOf(model.getDoorCloseButton())
                                   ));

    int numFloors = model.getNumFloors();
    for (int i = 0; i < numFloors; i++)
    {
      c.send(new Message.StateChangeNotification
             (STATE_ENABLE_PREFIX + model.getFloorName(i) + STATE_ENABLE_POSTFIX,
              String.valueOf(user.haveAccess[i]))
             );
      c.send(new Message.StateChangeNotification
             (STATE_REQUEST_PREFIX + model.getFloorName(i) + STATE_REQUEST_POSTFIX,
              String.valueOf(model.stopRequestedAt(i)))
             );
    }

  }

}
