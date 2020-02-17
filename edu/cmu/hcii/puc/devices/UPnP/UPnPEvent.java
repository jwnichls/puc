package edu.cmu.hcii.puc.devices.UPnP;

/**
 * Simple abstraction of an event
 */

public abstract class UPnPEvent
{

  // Error code constants

  public static final int UNKNOWN_ERROR = 1;
  public static final int INVALID_ARGUMENT = 2;
  public static final int NO_DEVICE = 3;
  /***** Add more errors as necessary *****/

  private int errorCode;
  private String errorMsg;

  // Device identifier

  private String sourceUDN;

  // Constructors

  public UPnPEvent()
  {
    this(null);
  }

  public UPnPEvent(String sourceUDN)
  {
    this.sourceUDN = sourceUDN;
  }

  // Error adding methods

  public final void setError(int errorCode, String errorMsg)
  {
    this.errorMsg = errorMsg;
    setError(errorCode);
  }

  public final void setError(int errorCode)
  {
    this.errorCode = errorCode;
  }

  // Accessor

  public String getSourceUDN()
  {
    return sourceUDN;
  }

  // Implementation subclasses

  /**
   * Change of state, elicited either by a normal UPnP event, or by a response
   * to an accessor (getXXX) action call
   */
  public static class StateChange extends UPnPEvent
  {

    // Instance variables

    private String state;
    private String value;

    // Constructors

    public StateChange(String state, String value)
    {
      this(null, state, value);
    }

    public StateChange(String sourceUDN, String state, String value)
    {
      super(sourceUDN);
      this.state = state;
      this.value = value;
    }

    // Accessors

    public String getState()
    {
      return state;
    }

    public String getValue()
    {
      return value;
    }

  }

  /**
   * Response to any called method which returns a String
   */
  public static class StringResponse extends UPnPEvent
  {

    // Instance variables

    private int callingMethodID;
    private String response;

    // Constructor

    public StringResponse(int callingMethodID, String response)
    {
      this.callingMethodID = callingMethodID;
      this.response = response;
    }

    // Accessors

    public int getCallingMethodID()
    {
      return callingMethodID;
    }

    public String getResponse()
    {
      return response;
    }

  }

  /**
   * Event that a new device has arrived on the network
   */
  public static class FactoryEvent extends UPnPEvent
  {

    // Constants

    public static final int TYPE_DEVICE_FOUND = 1;
    public static final int TYPE_DEVICE_REMOVED = 2;

    // Instance variable

    private int eventType;
    private String friendlyName;

    // Constructor

    private FactoryEvent(int eventType, String sourceUDN, String friendlyName)
    {
      super(sourceUDN);
      this.eventType = eventType;
      this.friendlyName = friendlyName;
    }

    // Creation methods

    public static FactoryEvent createFoundEvent(String sourceUDN,
                                                String friendlyName)
    {
      return new FactoryEvent(TYPE_DEVICE_FOUND, sourceUDN, friendlyName);
    }

    public static FactoryEvent createRemovedEvent(String sourceUDN)
    {
      return new FactoryEvent(TYPE_DEVICE_REMOVED, sourceUDN, null);
    }

    // Accessors

    public int getEventType()
    {
      return eventType;
    }

    public String getFriendlyName()
    {
      return friendlyName;
    }

  }

}