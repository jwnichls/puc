/**
 * X10Device.java
 *
 * This is a server-side object for a device that controls X10
 * light devices.
 *
 * @author Jeff Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.io.*;

import java.lang.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.maya.puc.common.Device;
import com.maya.puc.common.FauxEform;
import com.maya.puc.common.Message;
import com.maya.puc.common.StateListener;

import edu.cmu.hcii.puc.SpecGenerator;

import javax.xml.parsers.*;
import org.w3c.dom.*;



// Class X10Device Definition

public class X10Device implements Device
{

    private static String m_sName = "x10";
    private Vector m_vListeners = new Vector();

    private boolean m_bActive = false;

    private int m_nPort;

    private X10Lamp x10lamp;


    // Constants

    protected static final String X10PROXY_TAG = "x10spec";
    protected static final String COMPORT_TAG  = "comport";
    protected static final String OBJECT_TAG   = "object";
    protected static final String HOUSE_TAG    = "house";
    protected static final String DEVICE_TAG   = "device";
    protected static final String NAME_TAG     = "name";
    protected static final String COMMANDS_TAG = "commands";
    protected static final String COMMAND_TAG  = "command";

    protected static final String NAME_ATTRIB  = "name";

    protected static final String ON_CMD       = "ON";
    protected static final String OFF_CMD      = "OFF";
    protected static final String DIM_CMD      = "DIM";
    protected static final String BRIGHT_CMD   = "BRIGHT";

    protected static final String[] ON_LABELS  = { "On" };
    protected static final String[] OFF_LABELS = { "Off" };


    protected static final String NAME_PREFIX  = "device";

    protected static final String CONFIG_FILE      = "x10.cfg";
    protected static final String CONFIG_FILE_ATTR = "ConfigFile";


    // Inner Classes

    static public class X10Object extends Object {

	public X10Object() { m_vCommands = new Vector(); }

	public String m_sName;

	public String m_sHouse;
	public String m_sDevice;

	public Vector m_vCommands;

	public String m_sInternalName;
	
	public String statename;
    }


    // State Variables

    private String m_sComPort;

    protected String m_sStatus;

    private Vector m_vObjects;
    protected Hashtable m_hObjects;

    private String m_sPUCSpec;

    private int m_nActiveObject;

    protected Hashtable m_hCmdLabels;

    private Vector twowayx10;

    // End State Variables


    public X10Device() {
	m_vListeners = new Vector();

	m_hCmdLabels = new Hashtable();
	m_hCmdLabels.put( ON_CMD, "On" );
	m_hCmdLabels.put( OFF_CMD, "Off" );
	m_hCmdLabels.put( DIM_CMD, "Dim" );
	m_hCmdLabels.put( BRIGHT_CMD, "Brighten" );

	m_bActive = false;
	m_nPort = 5152;

	x10lamp = new X10Lamp (this);

	FauxEform pSettings = new FauxEform();
	pSettings.load( CONFIG_FILE );

	String sFile = pSettings.getStringAttr( CONFIG_FILE_ATTR, "" );

	if ( sFile.equals( "" ) ) {
	    // present a dialog box to get the name of the x10 spec file
	    openConfigDlg( null );
	}
	else {
	    // parse the chosen file
	    fileChosen( false, new File( sFile ) );
	}
    }

    public void configure() {
	
	FauxEform pSettings = new FauxEform();
	
	try {
	    pSettings.load( CONFIG_FILE );
	}
	catch( Throwable t ) { }

	String sFile = pSettings.getStringAttr( CONFIG_FILE_ATTR, "..\\..\\specs\\x10proxy_spec.xml" );

	openConfigDlg( sFile );
    }

    private void openConfigDlg( String sLastFile ) {

	JFileChooser chooser = new JFileChooser();

	if ( sLastFile != null ) {
	    chooser.setSelectedFile( new File( sLastFile ) );
	}

	FileFilter pFilter = new FileFilter() {

		public boolean accept( File f ) {

		    return f.isDirectory() ||
			   ( f.isFile() && f.canRead() && f.getName().endsWith( ".xml" ) );
		}

		public String getDescription() {

		    return "X10 Proxy Specifications (*.xml)";
		}
	    };

	chooser.setFileFilter(pFilter);
	int returnVal = chooser.showOpenDialog( null );
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    fileChosen( returnVal != JFileChooser.APPROVE_OPTION,
			chooser.getSelectedFile() );
	}
    }

    public boolean hasGUI() { return false; }
    public void setGUIVisibility(boolean isVisible) { }
    public boolean isGUIVisible() { return false; }

    public void start()  { 
	m_bActive = true; 
	m_sStatus = "Listening"; 
	dispatchStateEvent( null, null );
	try {
		x10lamp.open_serial_port (m_sComPort);
	}
	catch (DeviceException dEx) {
	       System.err.println("ERROR: " + dEx.getMessage());
	}
    }

    public void stop() { 
	m_bActive = false; 
	m_sStatus = "Stopped"; 
	dispatchStateEvent( null, null );
	
	x10lamp.close_serial_port ();
   }

    public boolean isRunning() { return m_bActive; }

    public String getStatus() { return m_sStatus; }

    public void setPort( int nPort ) {

	m_nPort = nPort;
    }

    public int getPort() { return m_nPort; }

    public void fileChosen( boolean bCancelled, File pFile ) {

	if ( bCancelled ) return;

	try {
	    m_vObjects = new Vector();
	    m_hObjects = new Hashtable();

	    // open and parse the x10 spec

	    FileInputStream pIn = new FileInputStream( pFile );
	    Document pDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( pIn );

	    // find the X10PROXY_TAG

	    Node pX10Proxy = findFirstTag( pDoc, X10PROXY_TAG );

	    m_sName = pX10Proxy.getAttributes().getNamedItem( NAME_ATTRIB ).getNodeValue();

	    // parse each of the objects

	    NodeList pList = pX10Proxy.getChildNodes();
	    for( int i = 0; i < pList.getLength(); i++ ) {
		Node pN = pList.item(i);
		if ( OBJECT_TAG.equals( pN.getLocalName() ) ) {
		    m_vObjects.addElement( objectHelper( pN ) );
		}
		else if ( COMPORT_TAG.equals( pN.getLocalName() ) ) {
		    NodeList pComList = pN.getChildNodes();
		    for( int j = 0; j < pComList.getLength(); j++ ) {
			if ( pComList.item(j).getNodeType() == Node.TEXT_NODE ) {
			    m_sComPort = pComList.item(j).getNodeValue();
			}
		    }
		}
	    }


	    // generate a PUC specification
	    
	    m_sPUCSpec = SpecGenerator.spec( "X10: " + m_sName, SpecGenerator.groupings( SpecGenerator.group( generateSpec() ) ) );

	    FauxEform pSettings = new FauxEform();
	    pSettings.setAttr( CONFIG_FILE_ATTR, pFile.toString() );
	    pSettings.save( CONFIG_FILE );

	    m_sStatus = "Ready.";
	    dispatchStateEvent( null, null );
	}
	catch( Exception e ) {
	    e.printStackTrace();
	    System.err.println( "Could not parse X10 specification."
				);
	    m_sStatus = "Bad X10 Spec";
	    dispatchStateEvent( null, null );
	}
    }

    public String generateSpec() {

	String valLabels = "";
	String[] groups = new String[ m_vObjects.size() ];

	String[] labels = new String[ 1 ];

	int cnt = 0;

	Enumeration en = m_vObjects.elements();
	
	twowayx10 = new Vector();

	for( int i = 0; i < m_vObjects.size(); i++ ) {

	    X10Object pObj = (X10Object)m_vObjects.elementAt( i );

	    pObj.m_sInternalName = NAME_PREFIX + cnt;

	    m_hObjects.put( pObj.m_sInternalName, pObj );

	    String cmds = "";
	    for( int j = 0; j < pObj.m_vCommands.size(); j++ ) {
		
		String cmd = (String)pObj.m_vCommands.elementAt( j );

		labels[ 0 ] = (String)m_hCmdLabels.get( cmd );

		if (cmd.regionMatches (true, 2, "STATE", 0, 5) || cmd.regionMatches (true, 3, "STATE", 0, 5)) {
		    labels[ 0 ] = pObj.m_sName;
			
		    cmds += SpecGenerator.state ( pObj.m_sInternalName + "_" + cmd, 10, SpecGenerator.type( SpecGenerator.valueSpace( SpecGenerator.bool() ) + SpecGenerator.valueLabels( SpecGenerator.map( "false", OFF_LABELS ) +
SpecGenerator.map( "true", ON_LABELS ) ) ) + SpecGenerator.labels (labels));
			
		    pObj.statename = pObj.m_sInternalName + "_" + cmd;
		    twowayx10.addElement (pObj);
		}
		else {
		 if (pObj.statename != null) 
			cmds += SpecGenerator.command (pObj.m_sInternalName + "_" + cmd, 10, SpecGenerator.labels (labels) + SpecGenerator.activeif ( SpecGenerator.equals (pObj.statename, "TRUE")));
		else 
		        cmds += SpecGenerator.command (pObj.m_sInternalName + "_" + cmd, 10, SpecGenerator.labels (labels));
						
		}
	    }

	    labels[ 0 ] = pObj.m_sName;
	    groups[ i ] = SpecGenerator.group( cmds +
					       SpecGenerator.labels( labels ) +
					       SpecGenerator.activeif( SpecGenerator.equals( "ActiveDevice", i+1+"" ) ) );

	    labels[ 0 ] = ((X10Object)m_vObjects.elementAt( i )).m_sName;
	    valLabels += SpecGenerator.map( i+1, labels );

	    cnt++;
	}

	labels[ 0 ] = "All Units Off";
	String spec =
	    SpecGenerator.command( "AllOff",
				   10, SpecGenerator.labels( labels ) );

	labels[ 0 ] = "All Lights On";
	spec +=
	    SpecGenerator.command( "AllLightsOn",
				   10, SpecGenerator.labels( labels ) );

	labels[ 0 ] = "All Lights Off";
	spec +=
	    SpecGenerator.command( "AllLightsOff",
				   10, SpecGenerator.labels( labels ) );

	labels[ 0 ] = "Device";
	spec +=
	    SpecGenerator.state( "ActiveDevice", 10, 
				 SpecGenerator.type( SpecGenerator.valueSpace( SpecGenerator.enumerated( m_vObjects.size() ) ) +
						     SpecGenerator.valueLabels( valLabels ) ) +
				 SpecGenerator.labels( labels ) );

	for( int i = 0; i < groups.length; i++ ) {
	    spec += groups[i];
	}
	
	x10lamp.vector_x10_object (twowayx10);

	return spec;
    }

    public X10Object objectHelper( Node pN ) {

	X10Object pObj = new X10Object();

	pObj.m_sHouse = getAttribValue( pN, HOUSE_TAG );
	pObj.m_sDevice = getAttribValue( pN, DEVICE_TAG );
	pObj.m_sName = getAttribValue( pN, NAME_TAG );

	Node pCmd = findFirstTag( pN, COMMANDS_TAG );
	NodeList pCList = pCmd.getChildNodes();
	for( int i = 0; i < pCList.getLength(); i++ ) {
	    if ( COMMAND_TAG.equals( pCList.item(i).getLocalName() ) ) {
		NodeList pL = pCList.item(i).getChildNodes();
		for( int j = 0; j < pL.getLength(); j++ ) {
		    if ( pL.item(j).getNodeType() == Node.TEXT_NODE ) {
			pObj.m_vCommands.addElement( pL.item(j).getNodeValue() );
		    }
		}
	    }
	}

	return pObj;
    }

    public String getAttribValue( Node pN, String sTag ) {

	Node pAttrib = findFirstTag( pN, sTag );
	NodeList pAList = pAttrib.getChildNodes();

	for( int j = 0; j < pAList.getLength(); j++ ) {
	    if ( pAList.item(j).getNodeType() == Node.TEXT_NODE ) {
		return pAList.item(j).getNodeValue();
	    }
	}

	System.out.println( "Found no value for " + sTag );
	return "";
    }

    public String getName() {
	return "x10";
    }

    public String getSpec() {
	return m_sPUCSpec;
    }

    public void requestFullState() {
	dispatchStateEvent( "ActiveDevice", m_nActiveObject + 1 + "" );
    }

    public void requestStateChange(String state, String value) {

	try {
	    if ( state.equals( "ActiveDevice" ) ) {
		int val = new Integer( value ).intValue();

		m_nActiveObject = val;

		dispatchStateEvent( state, value );
	    }
	    else {
		for( int i = 0; i < twowayx10.size(); i++ ) {
		    X10Object pObj = (X10Object)twowayx10.elementAt(i);
		    System.out.println( state + " ?= " + pObj.statename );

		    if ( state.equals( pObj.statename ) ) {

			System.out.println( "found a state to change." );

			if ( new Boolean( value ).booleanValue() ) 
			    sendX10Command( pObj, ON_CMD );
			else
			    sendX10Command( pObj, OFF_CMD );

			return;
		    }
		}
	    }
	}
	catch( Exception e ) { }
    }

    public void requestCommandInvoke(String command) {
	System.out.println( "Received invoke request for " + command );
	try {
	    if ( command.equals( "AllOff" ) ) {

	      sendX10Command( "ALLUNITSOFF" );
	    }
	    else if ( command.equals( "AllLightsOn" ) ) {

	      sendX10Command( "ALLLIGHTSON" );
	    }
	    else if ( command.equals( "AllLightsOff" ) ) {

	      sendX10Command( "ALLLIGHTSOFF" );
	    }
	    else {

		StringTokenizer st = new StringTokenizer( command, "_" );

		// first token should be the X10Object name
		// second token should be the X10 command name
		String name = st.nextToken();
		String cmd = st.nextToken();

		sendX10Command( (X10Object)m_hObjects.get( name ), cmd );
	    }
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
    }

    public void sendX10Command( String sCmd )  {

	System.out.println( "Executing X10 command " + sCmd );
	x10lamp.addqueue ("B", "1", sCmd);

    }

    public void sendX10Command( X10Object pObj, String sCmd ) {

	System.out.println( "Executing X10 command " + sCmd + " " + pObj.m_sHouse + pObj.m_sDevice );
	x10lamp.addqueue (pObj.m_sHouse, pObj.m_sDevice, sCmd);

    }

    public void addStateListener(StateListener sl) {
	m_vListeners.addElement( sl );
    }

    public void removeStateListener(StateListener sl) {
	m_vListeners.removeElement( sl );
    }


    public void dispatchStateEvent(String state, String value)  {
	StateListener l;
	Enumeration en;

	en = m_vListeners.elements();
	while( en.hasMoreElements() ) {
	    l = (StateListener)en.nextElement();
	    l.stateChanged(getName(), state, value);
	}
    }

    public static Node findFirstTag( Node pNode, String sTagName ) {

	NodeList pList = pNode.getChildNodes();

	for( int i = 0; i < pList.getLength(); i++ ) {

	    Node n = pList.item(i);
	    if ( sTagName.equals( n.getLocalName() ) ) return n;

	    n = findFirstTag( n, sTagName );
	    if ( n != null ) return n;
	}

	return null;
    }
}
