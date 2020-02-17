package edu.cmu.hcii.puc.devices.UPnP.IntelLight;

import java.util.ArrayList;

import edu.cmu.hcii.puc.devices.UPnP.*;

/**
 * Implementation of UPnPFactory for Intel UPnP Light simulators.
 */
public class LightFactory extends UPnPFactory
{

  // Constants

  public static final String NAME = "Intel UPnP Light Factory";

  // Instance variables

  // Constructor

  public LightFactory()
  {
    super();
  }

  // Abstract UPnPFactory methods

  public String getName()
  {
    return NAME;
  }

  public UPnPControl getControl()
  {
    if (control == null)
    {
      control = new LightControl();
    }
    return control;
  }

  protected void processNonTargetedEvent(UPnPEvent e)
  {
    /**
     * @todo For non-targeted events, may eventually want to use a default
     * light.  For now, ignore them.
     */
  }

  protected UPnPFactoryDevice createDevice(UPnPEvent.FactoryEvent fe)
  {
    return new LightDevice(control, fe.getSourceUDN(), fe.getFriendlyName());
  }

}