package edu.cmu.hcii.puc.devices.UPnP.IntelLight;

import java.io.File;

import edu.cmu.hcii.puc.devices.DeviceException;
import edu.cmu.hcii.puc.devices.UPnP.*;

/**
 * <p>Title: LightControl</p>
 * <p>Description: Java interface to the native implementation of a Control
 * Point for Intel's sample UPnP Light device.</p>
 * @author Kevin Litwack
 * @version 1.0
 *
 * Design Notes:
 *
 * 1. DeviceException - Note that very few (if any) of the native methods
 * in this class actually ever throw DeviceException, as of the writing of
 * this version.  However, in order to allow for future versions to do more
 * robust error-checking and handling, any class using this control should
 * catch the exception anyway.
 *
 * 2. public vs. private methods - You may note that all the public methods
 * simply forward to a native method.  This, again, is to allow for future
 * versions to perform some actions on the Java side without changing the
 * external interface.
 *
 * 3. UDNs - All the methods which dispatch requests to specific lights have
 * a String UDN parameter.  This parameter should contain the UPnP UDN of the
 * device the command is directed to.  All LightDevice objects inherit this
 * field from UPnPFactoryDevice, and it should be filled in by the
 * LightFactory.createDevice method.
 */
public class LightControl extends UPnPControl
{

// Constants

  public static final String LIBRARY_LOCATION = "LightControl";

  // Power Constants
  public static final boolean POWER_ON = true;
  public static final boolean POWER_OFF = false;

// Static initialization to load the required .dll file

  static
  {
    try
    {
      System.loadLibrary(LIBRARY_LOCATION);
    }
    catch (UnsatisfiedLinkError ule)
    {
      System.err.println("ERROR: Failed to load dll from path!");
      ule.printStackTrace();
      System.exit(1);
      // Could attempt to load from within current jar file here
    }
  }

// Constructor

  public LightControl()
  {
    try
    {
      init();
    }
    catch (DeviceException dEx)
    {
      dEx.printStackTrace();
    }
  }

// Public methods

  /********************* Connection Methods *********************/

  /**
   * For information on these methods, see documentation in native code.
   */

  public void init() throws DeviceException
  {
    nativeInit();
  }

  public void startCP() throws DeviceException
  {
    nativeStartCP();
  }

  public void stopCP() throws DeviceException
  {
    nativeStopCP();
  }

  /********************* Informational Methods *********************/

  public void getPower(String UDN) throws DeviceException
  {
    nativeGetPower(UDN);
  }

  public void setPower(String UDN, boolean newPower) throws DeviceException
  {
    nativeSetPower(UDN, newPower);
  }

  public void getLoadLevel(String UDN) throws DeviceException
  {
    nativeGetLoadLevel(UDN);
  }

  public void setLoadLevel(String UDN, int newLoadLevel) throws DeviceException
  {
    nativeSetLoadLevel(UDN, newLoadLevel);
  }

  /************************ Commands *****************************/

  public void allOn() throws DeviceException
  {
    nativeAllOn();
  }

  public void allOff() throws DeviceException
  {
    nativeAllOff();
  }

// Private native methods

  /********************* Connection Methods *********************/

  private native void nativeInit() throws DeviceException;
  private native void nativeStartCP() throws DeviceException;
  private native void nativeStopCP() throws DeviceException;

  /********************* Control Methods *********************/

  private native void nativeGetPower(String UDN)
      throws DeviceException;

  private native void nativeSetPower(String UDN, boolean newPower)
      throws DeviceException;

  private native void nativeGetLoadLevel(String UDN)
      throws DeviceException;

  private native void nativeSetLoadLevel(String UDN, int newLoadLevel)
      throws DeviceException;

  /************************ Commands *****************************/

  private native void nativeAllOn() throws DeviceException;
  private native void nativeAllOff() throws DeviceException;

}
