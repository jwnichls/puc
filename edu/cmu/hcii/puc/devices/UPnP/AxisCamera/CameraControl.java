package edu.cmu.hcii.puc.devices.UPnP.AxisCamera;

import edu.cmu.hcii.puc.devices.DeviceException;
import edu.cmu.hcii.puc.devices.UPnP.*;

/**
 * Implementation of UPnPControl for the Axis UPnP-enabled camera.  For notes
 * on the design of this class, see the documentation for UPnPControl, and
 * or the comments within the native code for the Axis camera control dll.
 */
public class CameraControl extends UPnPControl
{

// Constants

  public static final String LIBRARY_LOCATION = "CameraControl";

  public static final int METHOD_GET_PRESETS = 1;

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

  public CameraControl()
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

  public void getPan() throws DeviceException
  {
    nativeGetPan();
  }

  public void setPan(int newPan) throws DeviceException
  {
    nativeSetPan(newPan);
  }

  public void getTilt() throws DeviceException
  {
    nativeGetTilt();
  }

  public void setTilt(int newTilt) throws DeviceException
  {
    nativeSetTilt(newTilt);
  }

  public void getZoom() throws DeviceException
  {
    nativeGetZoom();
  }

  public void setZoom(int newZoom) throws DeviceException
  {
    nativeSetZoom(newZoom);
  }

  /********************** Commands ********************************/

  public void home() throws DeviceException
  {
    nativeSetPan(0);
    nativeSetTilt(0);
    nativeSetZoom(1);
  }

  /********************** Preset Methods **************************/

  public void getPresets() throws DeviceException
  {
    nativeGetPresets();
  }

  public void goToPreset(int presetNum) throws DeviceException
  {
    nativeGoToPreset(presetNum);
  }

// Private native methods

  /********************* Connection Methods *********************/

  private native void nativeInit() throws DeviceException;
  private native void nativeStartCP() throws DeviceException;
  private native void nativeStopCP() throws DeviceException;

  /********************* Control Methods *********************/

  private native void nativeGetPan() throws DeviceException;
  private native void nativeSetPan(int newPan) throws DeviceException;
  private native void nativeGetTilt() throws DeviceException;
  private native void nativeSetTilt(int newTilt) throws DeviceException;
  private native void nativeGetZoom() throws DeviceException;
  private native void nativeSetZoom(int newZoom) throws DeviceException;

  /********************** Preset Methods **************************/

  private native void nativeGetPresets() throws DeviceException;
  private native void nativeGoToPreset(int presetNum) throws DeviceException;
  private native void nativeSetPreset(int presetNum, String presetName) throws DeviceException;
  private native void nativeRemovePreset(int presetNum) throws DeviceException;

}