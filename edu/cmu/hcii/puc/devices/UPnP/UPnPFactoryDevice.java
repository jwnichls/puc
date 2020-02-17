package edu.cmu.hcii.puc.devices.UPnP;

import java.util.Hashtable;

import com.maya.puc.common.*;
import edu.cmu.hcii.puc.devices.*;

/**
 * Abstraction of any device which expects to be generated and managed by a
 * UPnPFactory.
 */
public abstract class UPnPFactoryDevice extends AbstractDevice2
{

  // Instance variables

  protected UPnPControl control;
  protected Hashtable states;
  protected String UDN;
  protected String friendlyName;

  // Constructors

  public UPnPFactoryDevice(UPnPControl control, String UDN, String friendlyName)
  {
    super();
    this.control = control;
    this.UDN = UDN;
    this.friendlyName = friendlyName;
  }

  // Accessors

  public String getUDN() { return UDN; }

  public String getFriendlyName() { return friendlyName; }

  // Overridden AbstractDevice2 Methods

  public boolean isManuallyActivatable()
  {
    return false;
  }

  // Implemented event methods

  protected void processEvent(UPnPEvent evt)
  {
    if (evt instanceof UPnPEvent.StateChange)
    {
      UPnPEvent.StateChange sc = (UPnPEvent.StateChange) evt;
      String state = sc.getState();
      String value = sc.getValue();
      dispatchStateEvent(state, value);
      cache(state, value);
    }
    else
    {
      processNonStateEvent(evt);
    }
  }

  // Abstract methods

  protected abstract void processNonStateEvent(UPnPEvent evt);

  protected abstract void cache(String state, String value);

}