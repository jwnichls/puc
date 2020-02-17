package com.maya.puc.common;

import java.io.InputStream;

import java.util.Enumeration;
import java.util.Vector;

import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ListIterator;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class encodes and decodes the PUC communications spec.
 *
 * @author Joseph Hughes
 * @version $Id: Message.java,v 1.18 2003/11/11 22:01:50 jeffreyn Exp $
 */

public abstract class Message {
    public static String ROOT_STRING = "message";

    public static String UNDEFINED_STRING = "undefined";

    /**
     * This function decodes a Message from its XML Document representation.
     *
     * @param _doc The XML Document object to decode
     *
     * @returns Message A decoded PUC message.  After getting a message back
     * 			from this function, be sure to check that the message is not null.
     */

    public static Message decode(Document _doc, 
				 InputStream _binData,
				 int _binLength ) {
        Message result = null;

        Element root = _doc.getRootElement();

        if (!root.getName().equals(ROOT_STRING))
            return null;

        Element type = (Element) root.getChildren().get(0);
        if (type.getName().equals(StateChangeRequest.TYPE_STRING))
            result = new StateChangeRequest(_doc);
        if (type.getName().equals(StateChangeNotification.TYPE_STRING))
            result = new StateChangeNotification(_doc);
	if (type.getName().equals(StateValueRequest.TYPE_STRING))
	    result = new StateValueRequest(_doc);
        if (type.getName().equals(CommandInvokeRequest.TYPE_STRING))
            result = new CommandInvokeRequest(_doc);
        if (type.getName().equals(SpecRequest.TYPE_STRING))
            result = new SpecRequest(_doc);
        if (type.getName().equals(DeviceSpec.TYPE_STRING))
            result = new DeviceSpec(_doc);
	if (type.getName().equals(FullStateRequest.TYPE_STRING))
            result = new FullStateRequest(_doc);
	if (type.getName().equals(ServerInformationRequest.TYPE_STRING))
	    result = new ServerInformationRequest(_doc);
	if (type.getName().equals(ServerInformation.TYPE_STRING))
	    result = new ServerInformation(_doc);
	if (type.getName().equals(AlertInformation.TYPE_STRING))
	    result = new AlertInformation(_doc);
	if (type.getName().equals(RegisterDevice.TYPE_STRING))
	    result = new RegisterDevice(_doc);
	if (type.getName().equals(UnregisterDevice.TYPE_STRING))
	    result = new UnregisterDevice(_doc);
	if (type.getName().equals(BinaryStateChangeNotification.TYPE_STRING))
	    result = new BinaryStateChangeNotification( _doc,
							_binData,
							_binLength );

	if (result == null) {
	    System.out.println("result is not a known type");
	} else if (!result.isValid()) {
	    System.out.println("result is not valid");
	}
        if (result == null || !result.isValid())
            return null;
        else
            return result;
    }

    /**
     * Converts the message to human-readable String form for debugging.
     *
     * @return A String that represents the contents of this message;
     *         may contain newline characters.
     */

    public abstract String toString();

    /**
     * Prepare the message for sending.  This incorporates connection 
     * specific information into the message that is only known by the
     * internals of the server.  This method should only be called
     * from within PUCServer. 
     */
    public void prepareMessage( PUCServer.Connection c ) { }
    
    /**
     * Gets the XML Document representation of the message.
     *
     * @returns Document the XML Document representation of the message.
     */

    public abstract Document getDocument();

    /**
     * Determines whether a message is valid.  Useful because an attempt
     * to decode a message must be performed in order to decide its
     * validity.
     *
     * @returns boolean - true if the message is valid
     */

    public abstract boolean isValid();

    /**
     * Determines whether the message contains binary data.
     */

    public boolean hasBinaryData() {

	return false;
    }

    /**
     * Returns the InputStream that allows access to this messages
     * binary data.
     */

    public InputStream getBinaryData() {

	return null;
    } 

    /**
     * Returns the length of the binary data attached to this
     * message.  Returns -1 if no data is attached.
     */
    public int getBinaryDataLength() {

	return -1;
    }

    public void setInputStream( InputStream binData, int length ) {
	
    }

    /**
     * Convenience function for debug printing.
     *
     * @returns String - "valid" if the message is valid, "invalid" otherwise
     */

    public String validString() {
        if (this.isValid())
            return "valid";
        else
            return "invalid";
    }

    /**
     * This message is a request for the server to send the value
     * of the named state to the client.  This is only used for 
     * variables that have binary data.
     */
    
    public static class StateValueRequest extends Message {
        public static String TYPE_STRING = "state-value-request";

	public final static String DESIRED_HEIGHT_OPT = "desired-height";
	public final static String DESIRED_WIDTH_OPT  = "desired-width";

	private String state = null;
	private Hashtable params = new Hashtable();
        private boolean valid = false;
	
	public StateValueRequest(String _state) {
	    state = _state;
	    valid = true;
	}

	public StateValueRequest(Document _doc) {
	    Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

	    List l = type.getChildren();
	    for( int i = 0; i < l.size(); i++ ) {
		Element elem = (Element)l.get( i );
		if ( elem.getName().equals( "state" ) )
		    state = elem.getText();
		else {
		    if ( elem.getText().length() > 0 )
			params.put( elem.getName(), elem.getText() );
		    else
			params.put( elem.getName(), new Boolean( true ) );
		}
	    }

            Element elState = type.getChild("state");
            if (elState == null) {
                return;
            }
            state = elState.getText();

            valid = true;    
	}

	public String get( String key ) {

	    return (String)params.get( key );
	}

	public void put( String key, String value ) {

	    params.put( key, value );
	}

	public boolean isValid() {

	    return valid;
	}

	public String getState() {

	    return state;
	}

	public void setState( String _state ) {

	    state = _state;
	}

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elVCN = new Element(TYPE_STRING);
	    Element elState = new Element("state");
	    elState.setText( state );
	    elVCN.addContent( elState );

	    Enumeration e = params.keys();
	    while( e.hasMoreElements() ) {
		String key = (String)e.nextElement();
		String val = (String)params.get( key );

		Element elParam = new Element( key );
		elParam.setText( val );
		elVCN.addContent( elParam );
	    }

	    elMessage.addContent( elVCN );

            return new Document(elMessage);
        }

	public String toString() {

	    return TYPE_STRING + ": [ " + state + " ]";
	}
    }


    /**
     * This message is a notification of a change in a binary state
     * variable.  It may contain a binary data attachment if it is the
     * result of a state-value-request message.
     */
    public static class BinaryStateChangeNotification extends Message {
        public static String TYPE_STRING = "binary-state-change-notification";
	public static String STATE_TAG = "state";
	public static String CONTENT_TYPE_ATTRIBUTE = "content-type";
	private String state = null;
	private InputStream binData = null;
	private int binLength = -1;
	private String contentType = null;
        private boolean valid = false;
	
	public BinaryStateChangeNotification(String _state, 
					     String _contentType ) {

	    state = _state;
	    contentType = _contentType;
	    valid = true;
	}

	public BinaryStateChangeNotification(String _state,
					     InputStream _binData, 
					     String _contentType,
					     int _length ) { 
	 
	    this( _state, _contentType );

	    setBinaryData( _binData, _contentType, _length );
	}

	public BinaryStateChangeNotification(Document _doc,
					     InputStream _binData, 
					     int _length ) {
	    Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

            Element elState = type.getChild(STATE_TAG);
            if (elState == null) {
                return;
            }
            state = elState.getText();

	    Attribute attr = elState.getAttribute( CONTENT_TYPE_ATTRIBUTE );
	    if ( attr == null ) {
		return;
	    }
	    setBinaryData( _binData, attr.getValue(), _length );

            valid = true;    
	}

	public boolean hasBinaryData() {
	    
	    return (binData != null);
	}

	public InputStream getBinaryData() {
	    
	    return binData;
	} 
	
	public int getBinaryDataLength() {
	
	    return binLength;
	}

	public boolean isValid() {

	    return valid;
	}

	public String getContentType() {

	    return contentType;
	}

	public String getState() {

	    return state;
	}

	public void setState( String _state ) {

	    state = _state;
	}

	public void setBinaryData( InputStream _binData, 
				   String _contentType,
				   int _length ) {

	    binData = _binData;
	    contentType = _contentType;
	    binLength = _length;
	}

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elVCN = new Element(TYPE_STRING);
	    Element elState = new Element("state");
	    elState.setAttribute( CONTENT_TYPE_ATTRIBUTE, contentType );
	    elState.setText( state );
	    elVCN.addContent( elState );
	    elMessage.addContent( elVCN );

            return new Document(elMessage);
        }

	public String toString() {

	    String extraInfo = "no binary attachment";
	    if ( hasBinaryData() )
		extraInfo = "binary data attached";

	    return TYPE_STRING + ": [ " + state + " (" + contentType +
		") = " + extraInfo + " ]";
	}
    }


    /**
     * This message is a request for the device to change the named
     * state to the named value.
     */

    public static class StateChangeRequest extends Message {
        public static String TYPE_STRING = "state-change-request";
	private PUCData data = null;
        private boolean valid = false;

        public StateChangeRequest(String _state, String _value) {
            valid = true;
	    data = new PUCData.Value( _state, _value );
        }

	public StateChangeRequest(String _state) {
	    valid = true;
	    data = new PUCData.Value( _state );
	}

        public StateChangeRequest(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

	    try {

		data = PUCData.Parse( (Element)type.getChildren().get(0), true );
		valid = true;
	    }
	    catch( Exception e ) {

		valid = false;
	    }
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elVCN = new Element(TYPE_STRING);
	    
	    if ( data.isValue() )
		((PUCData.Value)data).writeOldXML( elVCN );
	    else
		data.writeXML( elVCN );

            elMessage.addContent(elVCN);

            return new Document(elMessage);
        }

	public boolean isListData() {
	    return !data.isValue();
	}

	public PUCData getData() {
	    return data;
	}

        public String getState() {
            return ((PUCData.Value)data).getState();
        }

        public String getValue() {
            return ((PUCData.Value)data).getValue();
        }

        public boolean isValid() {
            return valid;
        }

	public boolean isDefined() {
	    return ((PUCData.Value)data).getDefined();
	}

        public String toString() {
            String msg = "";
            msg += "StateChangeRequest (" + this.validString() + "):";
	    if ( isListData() )
		msg += "\n\tcomplex list data";
	    else {
		msg += "\n\tstate = " + getState();
		msg += "\n\tvalue = " + getValue();
	    }
            return msg;
        }
    }

    /**
     * This message is a notification that the named state has changed
     * to the value specified.  Note that there is not a one to one
     * mapping between StateChangeRequests and StateChangeNotifications.
     */

    public static class StateChangeNotification extends Message {
        public static String TYPE_STRING = "state-change-notification";
        private boolean valid = false;
	private PUCData data = null;

        public StateChangeNotification(String _state, String _value) {
            valid = true;
	    data = new PUCData.Value( _state, _value );
        }

	public StateChangeNotification(PUCData _data) {
	    valid = true;
	    data = _data;
	}

	public StateChangeNotification(String _state) {
	    valid = true;
	    data = new PUCData.Value( _state );
	}

        public StateChangeNotification(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

	    try {

		data = PUCData.Parse( (Element)type.getChildren().get(0), true );
		valid = true;
	    }
	    catch( Exception e ) {

		valid = false;
	    }
        }

	public void prepareMessage( PUCServer.Connection c ) {

	}

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
	    Element elVCN = new Element(TYPE_STRING);

	    if ( data.isValue() )
		((PUCData.Value)data).writeOldXML( elVCN );
	    else
		data.writeXML( elVCN );	    

            elMessage.addContent(elVCN);

            return new Document(elMessage);
        }

	public boolean isListData() {
	    return !data.isValue();
	}

	public PUCData getData() {
	    return data;
	}

        public String getState() {
            return ((PUCData.Value)data).getState();
        }

        public String getValue() {
            return ((PUCData.Value)data).getValue();
        }

        public boolean isValid() {
            return valid;
        }

	public boolean isDefined() {
	    return ((PUCData.Value)data).getDefined();
	}

        public String toString() {
            String msg = "";
            msg += "StateChangeNotification (" + this.validString() + "):";
	    if ( isListData() )
		msg += "\n\tcomplex list data";
	    else {
		msg += "\n\tstate = " + getState();
		msg += "\n\tvalue = " + getValue();
	    }
            return msg;
        }
    }

    /**
     * This message requests a device specification from the device.
     */

    public static class SpecRequest extends Message {
        public static String TYPE_STRING = "spec-request";
        private boolean valid = false;

        public SpecRequest() {
            valid = true;
        }

        public SpecRequest(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

            valid = true;
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elType = new Element(TYPE_STRING);
            elMessage.addContent(elType);

            return new Document(elMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String toString() {
            String msg = "";
            msg += "SpecRequest (" + this.validString() + ")";
            return msg;
        }
    }

    /**
     * This message is a request for the device to invoke the named
     * command.
     */

    public static class CommandInvokeRequest extends Message {
        public static String TYPE_STRING = "command-invoke-request";
        private String command = "";
        private boolean valid = false;

        public CommandInvokeRequest(String _command) {
            this.valid = true;
            command = _command;
        }

        public CommandInvokeRequest(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

            Element elCommand = type.getChild("command");
            if (elCommand == null) {
                return;
            }
            command = elCommand.getText();

            valid = true;
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elCommand = new Element("command");
            elCommand.setText(command);
            Element elVCN = new Element(TYPE_STRING);
            elVCN.addContent(elCommand);
            elMessage.addContent(elVCN);

            return new Document(elMessage);
        }

        public String getCommand() {
            return command;
        }

        public boolean isValid() {
            return valid;
        }

        public String toString() {
            String msg = "";
            msg += "CommandInvokeRequest (" + this.validString() + "):";
            msg += "\n\tcommand = " + getCommand();
            return msg;
        }
    }

    /**
     * This message provides a device specification for the current
     * device.
     */

    public static class DeviceSpec extends Message {
        public static String TYPE_STRING = "device-spec";
        private String spec = "";
        private boolean valid = false;

        public DeviceSpec(String _spec) {
            valid = true;
            spec = _spec;
        }

        public DeviceSpec(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

            Element elSpec = type.getChild("spec");
            if (elSpec == null) {
                return;
            }
            spec = elSpec.getText();

            valid = true;
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elSpec = new Element("spec");
            elSpec.setText(spec);
            Element elType = new Element(TYPE_STRING);
            elType.addContent(elSpec);
            elMessage.addContent(elType);

            return new Document(elMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getSpec() {
            return spec;
        }

        public String toString() {
            String msg = "";
            msg += "DeviceSpec (" + this.validString() + "):";
            msg += "\n\tspec = " + spec.substring(0, Math.min(30, spec.length())) + "...";
            return msg;
        }

    }

    /**
     * This message requests a dump of the full set of states in
     * the device.  It should be answered by several
     * StateChangeNotifications, one for each state in the device.
     */

    public static class FullStateRequest extends Message {
        public static String TYPE_STRING = "full-state-request";
        private boolean valid = false;

        public FullStateRequest() {
            valid = true;
        }

        public FullStateRequest(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

            valid = true;
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elType = new Element(TYPE_STRING);
            elMessage.addContent(elType);

            return new Document(elMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String toString() {
            String msg = "";
            msg += "FullStateRequest (" + this.validString() + "):";
            return msg;
        }
    }


    /**
     * This message is a request for the device server to send
     * information about its active devices. 
     */

    public static class ServerInformationRequest extends Message {
        public static String TYPE_STRING = "server-information-request";
        private boolean valid = false;

        public ServerInformationRequest() {
            valid = true;
        }

        public ServerInformationRequest(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

            valid = true;
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elVCN = new Element(TYPE_STRING);
            elMessage.addContent(elVCN);

            return new Document(elMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String toString() {
            String msg = "";
            msg += "ServerInformationRequest (" + this.validString() + "):";
            return msg;
        }
    }


    /**
     * This message contains information about a server and its active
     * devices.
     */

    public static class ServerInformation extends Message {
        public static String TYPE_STRING = "server-information";
        public static String SERVER_NAME_STRING = "server-name";
        public static String DEVICE_STRING = "device";
        public static String NAME_STRING = "name";
        public static String PORT_STRING = "port";
	private String m_sServerName;
	private Vector m_vDeviceInfo;
        private boolean valid = false;

	public static class DeviceInfo {
	    
	    public DeviceInfo( String sName, int nPort ) {
		m_sDeviceName = sName;
		m_nDevicePort = nPort;
	    }

	    protected String m_sDeviceName;
	    protected int    m_nDevicePort;

	    public String getDeviceName() { return m_sDeviceName; }
	    public int    getDevicePort() { return m_nDevicePort; }
	}

        public ServerInformation( String sServerName, Enumeration eDevices ) {

	    m_sServerName = sServerName;
	    m_vDeviceInfo = new Vector();

	    while( eDevices.hasMoreElements() ) {
		Device2 pD = (Device2)eDevices.nextElement();

		if (! pD.isRunning() ) continue;

		DeviceInfo pDI = new DeviceInfo( pD.getName(),
						 pD.getPort() );
		m_vDeviceInfo.addElement( pDI );
	    }

            valid = true;
        }

        public ServerInformation(Document _doc) {
	    m_sServerName = null;
	    m_vDeviceInfo = new Vector();

            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

	    List pList = type.getChildren();
	    ListIterator pI = pList.listIterator();

	    while( pI.hasNext() ) {
		Element elChild = (Element) pI.next();
		if ( elChild.getName().equals(SERVER_NAME_STRING) ) {
		    m_sServerName = elChild.getText();
		}
		else if ( elChild.getName().equals(DEVICE_STRING) ) {
		    try {
			Element elDName = elChild.getChild(NAME_STRING);
			Element elDPort = elChild.getChild(PORT_STRING);
			String sDeviceName = elDName.getText();
			Integer nDevicePort = new Integer( elDPort.getText() );
			m_vDeviceInfo.addElement( 
					   new DeviceInfo(
						 sDeviceName,
						 nDevicePort.intValue() 
						          ) 
					         );
		    }
		    catch( Exception e ) {
			// ignore this device
		    }
		}
	    }

            valid = m_sServerName != null;
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elVCN = new Element(TYPE_STRING);
	    Element elServerName = new Element(SERVER_NAME_STRING);
	    elServerName.setText(m_sServerName);
	    elVCN.addContent(elServerName);
	    for( int i = 0; i < m_vDeviceInfo.size(); i++ ) {
		DeviceInfo pDI = (DeviceInfo)m_vDeviceInfo.elementAt( i );
		Element elDevice = new Element(DEVICE_STRING);
		Element elDeviceName = new Element(NAME_STRING);
		elDeviceName.setText(pDI.getDeviceName());
		Element elDevicePort = new Element(PORT_STRING);
		elDevicePort.setText(pDI.getDevicePort() + "");
		elDevice.addContent(elDeviceName);
		elDevice.addContent(elDevicePort);
		elVCN.addContent(elDevice);
	    }
            elMessage.addContent(elVCN);

            return new Document(elMessage);
        }

	public String getServerName() {

	    return m_sServerName;
	}

	public Enumeration getDeviceInfo() {
	    
	    return m_vDeviceInfo.elements();
	}

        public boolean isValid() {
            return valid;
        }

        public String toString() {
            String msg = "";
            msg += "ServerInformation (" + this.validString() + "):";
	    msg += "\n\tServer Name: " + getServerName();

	    Enumeration e = getDeviceInfo();
	    while( e.hasMoreElements() ) {
		DeviceInfo pDI = (DeviceInfo)e.nextElement();
		msg += "\n\tDevice: " + pDI.getDeviceName() + 
		    " on port " + pDI.getDevicePort();
	    }

            return msg;
        }
    }

    /**
     * This message is sent from the server when a alert dialog needs
     * to be displayed.
     */

    public static class AlertInformation extends Message {
        public static String TYPE_STRING = "alert-information";
        private String alertMessage = "";
        private boolean valid = false;

        public AlertInformation(String _alertmsg) {
            this.valid = true;
	    alertMessage = _alertmsg;
        }

        public AlertInformation(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

	    alertMessage = type.getText();

            valid = alertMessage != null;
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elVCN = new Element(TYPE_STRING);
	    elVCN.setText(alertMessage);
            elMessage.addContent(elVCN);

            return new Document(elMessage);
        }

        public String getAlertMessage() {
            return alertMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String toString() {
            String msg = "";
            msg += "AlertInformation (" + this.validString() + "):";
            msg += "\n\tmessage = " + getAlertMessage();
            return msg;
        }
    }


    /**
     * This message is sent from a PUCServer to another in order register
     * a device with the discovery service.
     */

    public static class RegisterDevice extends Message {
        public static String TYPE_STRING = "register-device";
        public static String NAME_STRING = "name";
        public static String PORT_STRING = "port";
        private String deviceName = "";
	private int devicePort = 0;
        private boolean valid = false;

        public RegisterDevice(String _deviceName, int _devicePort) {
            this.valid = true;
	    deviceName = _deviceName;
	    devicePort = _devicePort;
        }

        public RegisterDevice(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

	    try {
		Element elName = type.getChild(NAME_STRING);
		Element elPort = type.getChild(PORT_STRING);
		deviceName = elName.getText();
		devicePort = Integer.parseInt( elPort.getText() );

		valid = deviceName != null && devicePort > 0;
	    }
	    catch( Exception e ) {
		// ignore this registration attempt
		valid = false;
	    }
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elVCN = new Element(TYPE_STRING);
	    Element elName = new Element(NAME_STRING);
	    Element elPort = new Element(PORT_STRING);

	    elName.setText(deviceName);
	    elPort.setText(devicePort + "");
	    
	    elVCN.addContent( elName );
	    elVCN.addContent( elPort );

            elMessage.addContent(elVCN);

            return new Document(elMessage);
        }

        public String getDeviceName() {
            return deviceName;
        }

	public int getDevicePort() {
	    return devicePort;
	}

        public boolean isValid() {
            return valid;
        }

        public String toString() {
            String msg = "";
            msg += "RegisterDevice (" + this.validString() + "):";
            msg += "\n\tname = " + getDeviceName();
            msg += "\n\tport = " + getDevicePort();
            return msg;
        }
    }

    /**
     * This message is sent from a PUCServer to another in order unregister
     * a device with the discovery service.
     */

    public static class UnregisterDevice extends Message {
        public static String TYPE_STRING = "unregister-device";
        public static String PORT_STRING = "port";
	private int devicePort = 0;
        private boolean valid = false;

        public UnregisterDevice(int _devicePort) {
            this.valid = true;
	    devicePort = _devicePort;
        }

        public UnregisterDevice(Document _doc) {
            Element root = _doc.getRootElement();
            if ((root == null) || (!root.getName().equals(ROOT_STRING))) {
                return;
            }

            Element type = (Element) root.getChildren().get(0);
            if ((type == null) || (!type.getName().equals(TYPE_STRING))) {
                return;
            }

	    try {
		Element elPort = type.getChild(PORT_STRING);
		devicePort = Integer.parseInt( elPort.getText() );

		valid = devicePort > 0;
	    }
	    catch( Exception e ) {
		// ignore this registration attempt
		valid = false;
	    }
        }

        public Document getDocument() {
            Element elMessage = new Element(ROOT_STRING);
            Element elVCN = new Element(TYPE_STRING);
	    Element elPort = new Element(PORT_STRING);

	    elPort.setText(devicePort + "");	    
	    elVCN.addContent( elPort );

            elMessage.addContent(elVCN);

            return new Document(elMessage);
        }

	public int getDevicePort() {
	    return devicePort;
	}

        public boolean isValid() {
            return valid;
        }

        public String toString() {
            String msg = "";
            msg += "UnregisterDevice (" + this.validString() + "):";
            msg += "\n\tport = " + getDevicePort();
            return msg;
        }
    }


    public static void main( String[] args ) {

	XMLOutputter xmlout = new XMLOutputter();

	PUCServer s = new PUCServer();
	PUCServer.DeviceConnection c = null; // s.stupid();

	final String[] values = { "Orange", "Apple", "Banana" };
	boolean flag;

	StateChangeNotification test, test2;

	StateChangeRequest test3, test4;

	/*
	 * Test #1: Undefined (SCN)
	 */

	test = new StateChangeNotification( "TestState" );
	test.prepareMessage( c );
	test2 = new StateChangeNotification( test.getDocument() );

	if ( !test2.isDefined() ) {
	    System.out.println( "Test #1 Passed" );
	}
	else {
	    System.out.println( "Test #1 Failed" );
	    System.exit(-1);
	}

	/*
	 * Test #2: Undefined (SCR)
	 */

	test3 = new StateChangeRequest( "TestState" );
	test3.prepareMessage( c );
	test4 = new StateChangeRequest( test3.getDocument() );

	if ( !test4.isDefined() ) {
	    System.out.println( "Test #2 Passed" );
	}
	else {
	    System.out.println( "Test #2 Failed" );
	    System.exit(-1);
	}

	/*
	 * Test #3: Normal Value (SCN)
	 */

	test = new StateChangeNotification( "TestState", "success" );
	test.prepareMessage( c );
	test2 = new StateChangeNotification( test.getDocument() );

	if ( test2.isDefined() && test2.getValue().equals( "success" ) )
	    System.out.println( "Test #3 Passed" );
	else {
	    System.out.println( "Test #3 Failed" );
	    System.exit(-1);
	}

	/*
	 * Test #4: Normal Value (SCR)
	 */

	test3 = new StateChangeRequest( "TestState", "success" );
	test3.prepareMessage( c );
	test4 = new StateChangeRequest( test3.getDocument() );

	if ( test4.isDefined() && test4.getValue().equals( "success" ) )
	    System.out.println( "Test #4 Passed" );
	else {
	    System.out.println( "Test #4 Failed" );
	    System.exit(-1);
	}

	/*
	 * Test #5: List Data
	 *

	test = new StateChangeNotification( "TestState", values );
	test.prepareMessage( c );
	test2 = new StateChangeNotification( test.getDocument() );

	System.out.print( "\n" + xmlout.outputString(test.getDocument()) );

	flag = true;
	for( int i = 0; flag && i < test2.getValues().length; i++ ) {
	    flag = values[ i ].equals( test2.getValues()[ i ] );
	}

	if ( flag && test.getOperation() == test2.getOperation() && 
	     test.getVersion().equals( test2.getVersion() ) )
	    System.out.println( "Test #5 Passed" );
	else {
	    System.out.println( "Test #5 Failed" );
	} 

	/*
	 * Test #6: List Delete
	 *

	test = new StateChangeNotification( "TestState", 5, 10 );
	test.prepareMessage( c );
	test2 = new StateChangeNotification( test.getDocument() );

	System.out.print( "\n" + xmlout.outputString(test.getDocument()) );

	if ( test.getOperation() == test2.getOperation() && 
	     test.getVersion().equals( test2.getVersion() ) &&
	     test.getBegin() == test2.getBegin() &&
	     test.getEnd() == test2.getEnd() )
	    System.out.println( "Test #6 Passed" );
	else {
	    System.out.println( "Test #6 Failed" );
	} 
	
	/*
	 * Test #7: List Insert
	 *

	test = new StateChangeNotification( "TestState", 5, values );
	test.prepareMessage( c );
	test2 = new StateChangeNotification( test.getDocument() );

	System.out.print( "\n" + xmlout.outputString(test.getDocument()) );

	flag = true;
	for( int i = 0; flag && i < test2.getValues().length; i++ ) {
	    flag = values[ i ].equals( test2.getValues()[ i ] );
	}

	if ( flag && test.getOperation() == test2.getOperation() && 
	     test.getVersion().equals( test2.getVersion() ) &&
	     test.getBegin() == test2.getBegin() )
	    System.out.println( "Test #7 Passed" );
	else {
	    System.out.println( "Test #7 Failed" );
	} 

	/*
	 * Test #8: List Replace
	 *

	test = new StateChangeNotification( "TestState", 5, 10, values );
	test.prepareMessage( c );
	test2 = new StateChangeNotification( test.getDocument() );

	System.out.print( "\n" + xmlout.outputString(test.getDocument()) );

	flag = true;
	for( int i = 0; flag && i < test2.getValues().length; i++ ) {
	    flag = values[ i ].equals( test2.getValues()[ i ] );
	}

	if ( flag && test.getOperation() == test2.getOperation() && 
	     test.getVersion().equals( test2.getVersion() ) &&
	     test.getBegin() == test2.getBegin() &&
	     test.getBegin() == test2.getBegin() )
	    System.out.println( "Test #8 Passed" );
	else {
	    System.out.println( "Test #8 Failed" );
	} 

	/*
	 * Test #9: Data (SCR)
	 *

	test3 = new StateChangeRequest( "TestState", values );
	test3.prepareMessage( c );
	test4 = new StateChangeRequest( test3.getDocument() );

	System.out.print( "\n" + xmlout.outputString(test3.getDocument()) );

	flag = true;
	for( int i = 0; flag && i < test4.getValues().length; i++ ) {
	    flag = values[ i ].equals( test4.getValues()[ i ] );
	}

	if ( flag && test3.getOperation() == test4.getOperation() )
	    System.out.println( "Test #9 Passed" );
	else {
	    System.out.println( "Test #9 Failed" );
	} 
	*/
	
	/*
	 * Test #10: Alert Message
	 */

	AlertInformation test5 = new AlertInformation( "A simple message" );
	test5.prepareMessage( c );
	AlertInformation test6 = new AlertInformation( test5.getDocument() );

	System.out.println( "\n" + xmlout.outputString(test5.getDocument()) );

	if ( test5.getAlertMessage().equals( test6.getAlertMessage() ) )
	    System.out.println( "Test #10 Passed" );
	else
	    System.out.println( "Test #10 Failed" );
    }
}









