package edu.cmu.hcii.puc.devices;

import java.util.Hashtable;
import java.awt.*;

import org.havi.types.*;
import org.havi.system.*;
import org.havi.constants.*;
import org.havi.fcm.vcr.*;
import org.havi.fcm.constants.*;
import org.havi.fcm.types.TimeCode;

public class HaviVcrDevice extends AbstractDevice implements
  ConstVcrCounterType, ConstVcrTransportMode, ConstVcrRecordingMode,
  ConstForwardSpeed, ConstReverseSpeed
{

// Constants

  // Device constants
  private static final int DEFAULT_PORT = 5161;
  private static final String SPEC_FILE = "HaviVcrDeviceSpec.xml";
  private static String name = "Havi VCR";

  // State constants
  public static final String STATE_PLAYBACK_STATUS = "PlaybackStatus";
  public static final String STATE_POSITION = "Position";
  public static final String STATE_RECORDING_MODE = "RecordingMode";
  public static final String STATE_FORWARD_SPEED = "ForwardSpeed";
  public static final String STATE_REVERSE_SPEED = "ReverseSpeed";

  // Command constants
  public static final String COMMAND_RESET_TIME = "ResetTime";
  public static final String COMMAND_EJECT = "Eject";

  // Havi constants
  private final short mApiCode = (short)0x8001;
  private int timeout = 3000;

// Instance variables

  // Initialization flag
  private boolean deviceLoaded;

  // State variables
  private int playMode;
  private int recordMode;
  private int forwardSpeed;
  private int reverseSpeed;
  private String position;

  // HAVi variables
  private SoftwareElement softwareElement;
  private SEID seid;
  private SEID[] vcrSeids;
  private RegistryLocalClient registryClient;
  private EventManagerLocalClient eventMgrClient;
  private VcrClient[] vcrs;
  private int currentClient;
  private OperationCode opCode;

// Constructors

  public HaviVcrDevice()
  {
    super();
    try
    {
      init();
      deviceLoaded = true;
    }
    catch (DeviceException dEx)
    {
      System.err.println("Error creating HaviVcrDevice: " + dEx.getMessage());
      dEx.printStackTrace(System.err);
      deviceLoaded = false;
    }
  }

// Initialization methods

  public void init() throws DeviceException
  {
		try
		{
			// create SE
			softwareElement = new SoftwareElement(new HaviVcrListener() );
			seid = softwareElement.getSeid();

			// OpCode
			opCode = new OperationCode(mApiCode, (byte)0x01);

			// create HAVi clients
			registryClient = new RegistryLocalClient(softwareElement);
			eventMgrClient = new EventManagerLocalClient(softwareElement);

			// now to find Vcr Fcm
			while (true)
			{
				if (findVcrFcms())
				{
					// create VcrClients
          for (int i = 0; i < vcrSeids.length; i++)
          {
					  vcrs[i] = new VcrClient (softwareElement, vcrSeids[i]);
          }
          currentClient = 0;
					break;
				}
			}
		}
		catch (Exception e )
		{
			throw new DeviceException("Havi initialization error: " + e.getMessage());
		}
  }

	private boolean findVcrFcms()
	{
		boolean retVal = false;
		HaviByteArrayOutputStream hbaos = new HaviByteArrayOutputStream();
		Attribute[] regAttrList = new Attribute[1];
		SimpleQuery sq1 = null;
		SEIDSeqHolder seidSeq = null;

		// Create simple queries SQ1, SQ2 and SQ3
		// SQ1   SE_TYPE = DCM
		hbaos.reset();
		hbaos.writeInt(ConstSoftwareElementType.VCR_FCM);
		try
		{
			sq1 = new SimpleQuery(ConstAttributeName.ATT_SE_TYPE, hbaos.toByteArray(), (short)ConstComparisonOperator.EQU);
			seidSeq = new  SEIDSeqHolder();
			registryClient.getElementSync(timeout, sq1, seidSeq);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}


		if (seidSeq == null)
		{
			System.out.println("VcrTest :: findVcrFcm :query resulted NOTHING ");
			return false;
		}
		else
		{
			System.out.println("VcrTest :: findVcrFcm :query success ");
			SEID[] sa = seidSeq.getValue();

			if (sa.length > 0)
			{
				System.out.println("VcrTest :: findVcrFcm : query result  has " + sa.length + " elements");

				vcrSeids = sa;
				if(sa[0]==null)
				{
					System.out.println("VcrTest :: findVcrFcm :  sa null ");
				}
				else
				{
					//System.out.println("VcrTest :: findVcrFcm : Fcm SEID = " + VlHaviUtility.toHexString(fcmSeid.getValue()) );
					System.out.println("VcrTest :: findVcrFcm : Fcm SEID = " + vcrSeid);
					retVal = true;
				}
			}
			else
			{
				System.out.println("VcrTest :: findVcrFcm : query result  has 0 elements ");
				retVal = false;
			}
		}
		return retVal;
	} //  findVcrFcm

// Un-implemented Device methods

  public boolean hasGUI() { return false; }
  public boolean isGUIVisible() { return false; }
  public void setGUIVisibility(boolean isVisible) { }

// Constant Device methods

  protected int getDefaultPort() { return DEFAULT_PORT; }
  protected String getSpecFileName() { return SPEC_FILE; }
  public String getName() { return name; }

// Overriden AbstractDevice methods

  public void start(String status)
  {
    if (deviceLoaded) super.start(status);
  }

// Implemented Device methods

  public void configure()
  {
    if (vcrSeids.length > 1)
    {
      System.out.println("Current client #: " + currentClient);
      System.out.println("Enter new client # (from 0 to "
        + (vcrSeids.length - 1));
      char input = (char)System.in.read();
      try
      {
        int newClient = Integer.parseInt("" + input);
        if (newClient < vcrSeids.length)
        {
          currentClient = newClient;
          update();
        }
      }
      catch(NumberFormatException nfEx)
      {
        nfEx.printStackTrace(System.err);
      }
    }
  }

  public void requestStateChange(String state, String value)
  {
    if (state != null && value != null)
    {
      try
      {
        if (state.equals(STATE_PLAYBACK_STATUS))
          setPlayMode(Integer.parseInt(value));
        else if (state.equals(STATE_RECORDING_MODE))
          vcr.setRecordingModeSync(timeout, Integer.parseInt(value));
        else if (state.equals(STATE_FORWARD_SPEED))
        {
          forwardSpeed = Integer.parseInt(value);
          if (playMode == VARIABLE_FORWARD)
            vcr.variableForwardSync(timeout, forwardSpeed);
        }
        else if (state.equals(STATE_REVERSE_SPEED))
        {
          reverseSpeed = Integer.parseInt(value);
          if (playMode == VARIABLE_REVERSE)
            vcr.variableReverseSync(timeout, forwardSpeed);
        }
        else
          System.err.println("Unrecognized state: " + state);
      }
      catch (NumberFormatException nfEx)
      {
        System.err.println("Number format error in state " + state + "; " +
          "value was " + value);
        nfEx.printStackTrace(System.err);
      }
      catch (HaviException hEx)
      {
        System.err.println("Error in HAVi device: " + hEx.getMessage());
        hEx.printStackTrace(System.err);
      }
    }
  }

  protected Hashtable getAllStates()
  {
    try
    {
      update();
    }
    catch (HaviException hEx)
    {
      System.err.println("Error updating states from device: "
        + hEx.getMessage());
      hEx.printStackTrace(System.err);
    }
    catch (HaviUnionException huEx)
    {
      System.err.println("Error updating states from device: "
        + huEx.getMessage());
      huEx.printStackTrace(System.err);
    }

    Hashtable states = new Hashtable();
    states.put(STATE_PLAYBACK_STATUS, String.valueOf(playMode));
    states.put(STATE_POSITION, position);
    states.put(STATE_RECORDING_MODE, String.valueOf(recordMode));
    return states;
  }

  public void requestCommandInvoke(String command)
  {
    if (command != null)
    {
      try
      {
        if (command.equals(COMMAND_EJECT)) eject();
        else if (command.equals(COMMAND_RESET_TIME)) resetTime();
        else
          System.err.println("Unrecognized command: " + command);
      }
      catch (HaviException hEx)
      {
        System.err.println("Error sending command " + command + " to device: " +
          hEx.getMessage());
        hEx.printStackTrace(System.err);
      }
    }
  }

// Private utility methods

  private void setPlayMode(int mode) throws HaviException
  {
    switch (mode)
    {
      case PLAY:
        vcr.playSync(timeout);
        break;
      case RECORD:
        vcr.recordSync(timeout);
        break;
      case FAST_FORWARD:
        vcr.fastForwardSync(timeout);
        break;
      case FAST_REVERSE:
        vcr.fastReverseSync(timeout);
        break;
      case VARIABLE_FORWARD:
        vcr.variableForwardSync(timeout, forwardSpeed);
        break;
      case VARIABLE_REVERSE:
        vcr.variableReverseSync(timeout, reverseSpeed);
        break;
      case STOP:
        vcr.stopSync(timeout);
        break;
      case RECPAUSE:
        vcr.recPauseSync(timeout);
        break;
      case SKIP:
        // vcr.skipSync(timeout);
        System.err.println("Skip not yet implemented");
        break;
      case NO_MEDIA:
        // vcr.playSync(timeout);
        System.err.println("Cannot change state to No Media");
        break;
      default:
        System.err.println("Unknown playmode constant: " + mode);
    }
  }

  private void eject() throws HaviException
  {
    vcr.ejectMediaSync(timeout);
  }

  private void resetTime() throws HaviException
  {
    vcr.clearRTCSync(timeout);
  }

  private void update() throws HaviException, HaviUnionException
  {
    VcrCounterValue counterValue = new VcrCounterValue();
    vcr.getPositionSync(timeout, ABSOLUTE_TIME, counterValue);
    position = formatPosition(counterValue);

    VcrTransportState state = new VcrTransportState();
    vcr.getStateSync(timeout, state);
    playMode = state.getDiscriminator();

    switch (playMode)
    {
      case RECORD:
        recordMode = state.getRmode();
        break;
      case VARIABLE_FORWARD:
        forwardSpeed = state.getFspeed();
        break;
      case VARIABLE_REVERSE:
        reverseSpeed = state.getRspeed();
        break;
      default:
        // Do nothing
    }
  }

  private String formatPosition(VcrCounterValue counterValue)
    throws HaviUnionException
  {
    TimeCode tc = counterValue.getAbsTime();
    return tc.getHour() + ":" + tc.getMinute() + ":" + tc.getSec();
  }

}