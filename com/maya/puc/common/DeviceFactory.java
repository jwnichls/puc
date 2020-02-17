package com.maya.puc.common;

/**
 * A Device Factory is a single jar file that is capable of detecting the
 * arrival and removal of devices on a network.  The PUCProxy searches for
 * Device Factories in the same manner that it searches for regular Devices.
 * The difference is that when a Device Factory is found, it is not loaded as
 * a Device directly, but instead gets pointers to loadNewDevice and
 * removeDevice methods (in the form of a DeviceFactoryListener).
 *
 * For a good example of a Device Factory, see the Intel Light factory
 * in the edu.cmu.hcii.puc.devices.UPnP.IntelLight package (and the abstract
 * supporting classes in the UPnP package).
 */
public interface DeviceFactory
{

  /**
   * Add this DeviceFactoryListener to the list of listeners
   *
   * @param dfl The listener to be added
   */
  public void addListener(DeviceFactoryListener dfl);

  /**
   * Returns a friendly name, intended solely for display purposes
   *
   * @return The friendly name to display
   */
  public String getName();

}