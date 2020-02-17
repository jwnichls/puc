package edu.cmu.hcii.puc.devices;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

import java.io.File;

public class DVCamera
{

// Constants

  public static final String CAMDLL = "DVCam";

  // Power Constants
  public static final boolean POWER_ON = true;
  public static final boolean POWER_OFF = false;

  // Device Mode Constants
  public static final boolean DEVICE_MODE_CAMERA = false;
  public static final boolean DEVICE_MODE_VCR = true;

  // Media Constants
  public static final int MEDIA_VHS = 1;
  public static final int MEDIA_DV = 2;
  public static final int MEDIA_UNKNOWN = 3;
  public static final int MEDIA_NONE = 4;

  // Transport Mode Constants
  public static final int T_MODE_PLAY = 1;
  public static final int T_MODE_STOP = 2;
  public static final int T_MODE_PAUSE = 3;
  public static final int T_MODE_FF = 4;
  public static final int T_MODE_REW = 5;
  public static final int T_MODE_RECORD = 6;

// Static initialization to load the required .dll file

  static
  {
    try
    {
      System.loadLibrary(CAMDLL);
    }
    catch (UnsatisfiedLinkError ule)
    {
      System.err.println("ERROR: Failed to load dll from path!");
      ule.printStackTrace();
      System.exit(1);
      // Could attempt to load from within current jar file here
    }
  }

// Static Instance-getter

  /**
   * This is just a dummy method right now, but should eventually be
   * implemented to query for all the DV devices available, and return them
   * in an array
   */
  public static DVCamera[] getDVDevices()
  {
    return null;
  }

// Constructors - none at present

// Public methods

  /********************* Connection Methods *********************/

  public void connect() throws DeviceException
  {
    init();
  }

  /********************* Informational Methods *********************/

  public boolean isPowerOn() throws DeviceException
  {
    return nativeIsPowerOn();
  }

  public boolean getDeviceMode() throws DeviceException
  {
    return nativeGetDeviceMode();
  }

  public int getMedia() throws DeviceException
  {
    return nativeGetMedia();
  }

  public String getAVCVersion() throws DeviceException
  {
    return nativeGetAVCVersion();
  }

  public float getFrameRate() throws DeviceException
  {
    return nativeGetFrameRate();
  }

  public String getPort() throws DeviceException
  {
    return nativeGetPort();
  }

  /********************* Status Methods *********************/

  public String getTimecode() throws DeviceException
  {
    return nativeGetTimecode();
  }

  public int getTransportMode() throws DeviceException
  {
    return nativeGetTransportMode();
  }

  /********************* Transport Mode Set Methods *********************/

  public void setTransportMode(int mode) throws DeviceException
  {
    System.out.println("Attempting to set transport mode to " + mode);
    if (mode == T_MODE_PLAY) play();
    else if (mode == T_MODE_STOP) stop();
    else if (mode == T_MODE_REW) rewind();
    else if (mode == T_MODE_FF) fastForward();
    else if (mode == T_MODE_PAUSE) pause();
    else if (mode == T_MODE_RECORD) record();
  }

  public void play() throws DeviceException
    { nativePlay(); }
  public void stop() throws DeviceException
    { nativeStop(); }
  public void rewind() throws DeviceException
    { nativeRewind(); }
  public void fastForward() throws DeviceException
    { nativeFastForward(); }
  public void pause() throws DeviceException
    { nativePause(); }
  public void record() throws DeviceException
    { nativeRecord(); }
  public void stepForward() throws DeviceException
    { nativeStepForward(); }
  public void stepBack() throws DeviceException
    { nativeStepBack(); }

// Private native methods for sending calls to the DV dll

  /********************* Initialization Methods *********************/

  /**
   * Attempts to initialize the device object; note that in order for this
   * method to work, there must be a DV device connected to the computer.  Also,
   * as of the time of this writing (6/19/02), the dll will just pick the first
   * DV device it finds, and connect to that.
   *
   * @todo Implement error checking/graceful failure
   */
  private native void init() throws DeviceException;

  /********************* Informational Methods *********************/

  private native boolean nativeIsPowerOn() throws DeviceException;
  private native boolean nativeGetDeviceMode() throws DeviceException;
  private native int nativeGetMedia() throws DeviceException;
  private native String nativeGetAVCVersion() throws DeviceException;
  private native float nativeGetFrameRate() throws DeviceException;
  private native String nativeGetPort() throws DeviceException;

  /********************* Status Methods *********************/

  private native String nativeGetTimecode() throws DeviceException;
  private native int nativeGetTransportMode() throws DeviceException;

  /********************* Transport Mode Set Methods *********************/

  /**
   * Puts the device in play mode
   */
  private native void nativePlay() throws DeviceException;

  /**
   * Stops the device
   */
  private native void nativeStop() throws DeviceException;

  /**
   * Puts the device in rewind
   */
  private native void nativeRewind() throws DeviceException;

  /**
   * Puts the device in fast-forward
   */
  private native void nativeFastForward() throws DeviceException;

  /**
   * Pauses the device
   */
  private native void nativePause() throws DeviceException;

  /**
   * Puts the device in record mode
   */
  private native void nativeRecord() throws DeviceException;

  /**
   * Causes the device to step forward
   */
  private native void nativeStepForward() throws DeviceException;

  /**
   * Causes the device to step back
   */
  private native void nativeStepBack() throws DeviceException;

}