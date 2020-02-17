package com.maya.puc.common;

/**
 * Interface to be implemented by anything which wants to be notified when
 * a Device Factory discovers or removes a device.  In practice, this is
 * probably only the PUCProxy, though others could be added in the future.
 */
public interface DeviceFactoryListener
{

  /**
   * Called when a new device is discovered.  The DeviceFactoryListener should
   * perform the necessary steps to add this device to its list, etc.
   *
   * @param device The device being added
   */
  public void loadNewDevice(Device2 device);

  /**
   * Called when a device is removed.
   *
   * @param device The device being removed
   */
  public void removeDevice(Device2 device);

}