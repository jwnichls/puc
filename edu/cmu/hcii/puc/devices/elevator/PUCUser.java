package edu.cmu.hcii.puc.devices.elevator;

import com.maya.puc.common.*;

/**
 * Represents a PUC user's interaction with an ElevatorModel.
 */
public class PUCUser extends AbstractUser
{

  // Static user count

  private static int userNum = 0;

  // Instance variables

  protected PUCServer.Connection c;
  private String name;

  // Constructors

  public PUCUser(ElevatorModel model, PUCServer.Connection c)
  {
    this(model, 0, c);
  }

  public PUCUser(ElevatorModel model, int startingLoc, PUCServer.Connection c)
  {
    super(model, startingLoc);
    this.c = c;
    name = "PUC User #" + userNum++;
  }

  // AbstractUser abstract methods and utilities

  protected void updateStatus()
  {
    String statusStr;
    if (status == INSIDE)
      statusStr = "2";
    else if (status == OUTSIDE)
      statusStr = "1";
    else
      return;

    c.send(new Message.StateChangeNotification
           (ElevatorDevice.STATE_USER_INSIDE, statusStr));
  }

  protected void updateFloor()
  {
    c.send(new Message.
           StateChangeNotification(ElevatorDevice.STATE_USER_LOCATION,
                                   model.getFloorName(currentLoc)
                                   ));
  }

  protected void updateCallButtons()
  {
    c.send(new Message.StateChangeNotification
           (ElevatorDevice.STATE_CALL_DOWN,
            String.valueOf(model.downRequestedAt(currentLoc))
            ));
    c.send(new Message.StateChangeNotification
           (ElevatorDevice.STATE_CALL_UP,
            String.valueOf(model.upRequestedAt(currentLoc))
            ));
  }

  protected void updateAccess()
  {
    int numFloors = model.getNumFloors();
    for (int i = 0; i < numFloors; i++)
    {
      c.send(new Message.StateChangeNotification
             (ElevatorDevice.STATE_ENABLE_PREFIX + model.getFloorName(i) +
              ElevatorDevice.STATE_ENABLE_POSTFIX,
              String.valueOf(haveAccess[i]))
             );
    }
  }

  protected String getName() { return name; }

}