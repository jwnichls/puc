package edu.cmu.hcii.puc.devices;

import com.maya.puc.common.StateListener;

import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Listeners extends Vector
{

// Instance variables

  private String deviceName;
  private boolean active;

// Constructors

  public Listeners(String deviceName)
  {
    super();
    this.deviceName = deviceName;
    active = false;
  }

// Dispatch methods

  /**
   * Updates the specified state/value pair on all registered StateListeners
   *
   * Note: Calling dispatchStateEvent(null, null) causes the status string
   * to update.  Yes, this is a random hack.  You should call updateStatus(),
   * if you want status to update, since it's obviously much clearer.
   *
   * @param state The name of the state variable to update
   * @param value The new value of state
   */
  public void dispatchStateEvent(String state, String value)
  {
    StateListener sl;

    for (int i = 0; i < size(); i++)
    {
      sl = (StateListener) elementAt(i);
      sl.stateChanged(deviceName, state, value);
    }
  }

  /**
   * Updates the status on all registered StateListeners
   *
   * Note: The implementation of this method uses a hack in dispatchStateEvent.
   * For more information, see the javadoc for that method.
   */
  public void updateStatus() { dispatchStateEvent(null, null); }

// Informational methods

  public boolean areActive() { return active; }

  public void setActive(boolean active) { this.active = active; }

}