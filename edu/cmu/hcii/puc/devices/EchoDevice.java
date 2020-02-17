/**
 * EchoDevice.java
 *
 * This is a server-side object for a device that echoes request
 * messages as actual changes.
 *
 * @author Jeff Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.Frame;
import java.awt.Insets;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.ScrollPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.io.*;

import java.lang.*;

import java.util.Enumeration;
import java.util.Vector;

import com.maya.puc.common.Connection;
import com.maya.puc.common.ConnectionEvent;
import com.maya.puc.common.ConnectionListener;
import com.maya.puc.common.Device;
import com.maya.puc.common.FauxEform;
import com.maya.puc.common.Message;
import com.maya.puc.common.PUCServer;
import com.maya.puc.common.StateListener;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.Globals;

import edu.cmu.hcii.puc.parser.SpecParser;

import edu.cmu.hcii.puc.uigen.EchoUIGenerator;

// import Winamp;


// Class Definition

public class EchoDevice implements Device,
				   FileDialogListener {

    /*
     * EchoConnection class
     *
     * A stub class that extends com.maya.puc.common.Connection.  This
     * forwards changes from the EchoDevice UI to the clients connected to
     * the EchoDevice.
     */

    protected class EchoConnection extends Connection {

	EchoDevice m_pDevice;

	public EchoConnection( EchoDevice pDevice ) {
	    super( null, 8000 );

	    m_pDevice = pDevice;
	}

	public void requestReconnect() { }
	public boolean reconnectRequested() { return false; }
	public boolean isConnected() { return true; }
	public void connect() { }
	public void disconnect() { }

	public void send( Message pMsg ) throws java.io.IOException {
	    Message.StateChangeRequest pSCRMsg = (Message.StateChangeRequest)pMsg;

	    m_pDevice.requestStateChange( pSCRMsg.getState(),
					  pSCRMsg.getValue() );
	}

	public void received( String state, String value ) {
	    dispatchConnectionEvent( new ConnectionEvent.MessageReceived( this, new Message.StateChangeNotification( state, value ) ) );
	}
    }


    /*
     * Member Variables
     */

    private static String m_sName = "echo";
    private Vector m_vListeners = new Vector();

    protected boolean m_bActive;
    protected int m_nPort;

    protected String m_sSpec;

    protected Frame m_pUIFrame;
    protected MenuBar m_pMenuBar;
    protected ScrollPane m_pScrollPane;
    protected Appliance m_pAppliance;

    protected String m_sStatus;

    protected String m_sFilename;

    protected EchoConnection m_pConnection;

    protected static final String CONFIG_FILE = "echo.cfg";
    protected static final String CONFIG_FILE_ATTR = "ConfigFile";

    public EchoDevice() {
	m_vListeners = new Vector();

	m_pUIFrame = new Frame("Echo Proxy UI");
	m_pMenuBar = new MenuBar();
	m_pUIFrame.setMenuBar( m_pMenuBar );

	m_pScrollPane = new ScrollPane();
	m_pUIFrame.add( m_pScrollPane );

	initMenus( m_pMenuBar );

	Globals.init( "1.0", 240, "echodevice.xml", ".", m_pScrollPane );

	m_pUIFrame.setSize(Globals.getScreenWidth(), 290);
	m_pUIFrame.setVisible( true );

	m_sStatus = "Stopped";
	dispatchStateEvent( null, null );

	m_pUIFrame.addWindowListener( new WindowAdapter() {
		public void windowClosing( WindowEvent e ) {
		    m_pUIFrame.setVisible( false );
		    dispatchStateEvent( null, null );
		}
	    });

	FauxEform pSettings = new FauxEform();
	pSettings.load( CONFIG_FILE );

	String sFile = pSettings.getStringAttr( CONFIG_FILE_ATTR, "" );

	m_nPort = 5153;

	if ( sFile.equals( "" ) ) {
	    // present a dialog box to get the name of the x10 spec file
	    new FileDialog( "Echo Proxy Config Info...", "..\\..\\specs\\pdgdemo.xml", this );
	}
	else {
	    // parse the chosen file
	    fileChosen( false, sFile );

	    // load variables that exist
	    Enumeration e = m_pAppliance.m_Objects.elements();
	    while( e.hasMoreElements() ) {
		ApplianceObject pObj = (ApplianceObject)e.nextElement();

		if ( pObj.isState() ) {
		    ApplianceState pState = (ApplianceState)pObj;
		    Object o = pSettings.getAttr( "ST-" + pState.m_sName );

		    if ( o != null ) {
			String sValue = o.toString();

			if (! sValue.equals( "" ) )
			    pState.setValue( sValue );
		    }
		}
	    }

	    saveConfigInfo();
	}
    }

    protected void initMenus( MenuBar pMB ) {

	MenuItem m;

	// FILE MENU

	Menu file = new Menu( "File" );
	pMB.add( file );

        m = new MenuItem("Save Variables");
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		saveConfigInfo();
            }
        });
        file.add(m);

	file.addSeparator();

        m = new MenuItem("Hide");
        m.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		m_pUIFrame.setVisible( false );
		dispatchStateEvent( null, null );
            }
        });
        file.add(m);
    }

    public void fileChosen( boolean bCancelled, String sFilename ) {

	try {
	    BufferedReader pIn = new BufferedReader( new FileReader( sFilename ) );
	    m_sSpec = "";

	    String line;
	    while( ( line = pIn.readLine() ) != null )
		m_sSpec += line;

	    pIn.close();

	    m_pAppliance = SpecParser.parse( new StringBufferInputStream( m_sSpec ) );

	    m_sFilename = sFilename;

	    m_pAppliance.m_pConnection = new EchoConnection( this );

	    m_pAppliance.m_pConnection.addConnectionListener( new ConnectionListener() {
		    public void messageReceived(ConnectionEvent.MessageReceived e) {
			Message msg = e.getMessage();

			if (msg instanceof Message.StateChangeNotification) {
			    Message.StateChangeNotification scnMsg = (Message.StateChangeNotification) msg;

			    ApplianceState as = (ApplianceState) m_pAppliance.m_Objects.get(scnMsg.getState());

			    if (as != null)
				as.setValue(scnMsg.getValue());
			}

		    }
		});

	    m_pAppliance.setUIGenerator( new EchoUIGenerator( m_pMenuBar ) );

	    Insets i = m_pScrollPane.getInsets();

	    m_pScrollPane.removeAll();
	    m_pScrollPane.add( m_pAppliance.getUIGenerator() );

	    m_pAppliance.getUIGenerator().setSize( m_pUIFrame.getSize().width -
						   i.left - i.right,
						   m_pUIFrame.getSize().height -
						   i.top - i.bottom );
	    m_pAppliance.getUIGenerator().setLocation( i.left,
						       i.top );

	    m_pAppliance.getUIGenerator().generateUI( m_pAppliance );

	    saveConfigInfo();

	    m_sStatus = "Ready";
	    dispatchStateEvent( null, null );
	}
	catch( Throwable t ) {
	    t.printStackTrace();
	}
    }

    protected void saveConfigInfo() {

	FauxEform pSettings = new FauxEform();
	pSettings.setAttr( CONFIG_FILE_ATTR, m_sFilename );

	// save state variables
	Enumeration e = m_pAppliance.m_Objects.elements();
	while( e.hasMoreElements() ) {
	    ApplianceObject pObj = (ApplianceObject)e.nextElement();

	    if ( pObj.isState() ) {
		ApplianceState pState = (ApplianceState)pObj;
		pSettings.setAttr( "ST-" + pState.m_sName, pState.m_Type.getValueSpace().getValue() );
	    }
	}

	pSettings.save( CONFIG_FILE );
    }

    public void configure() {
	FauxEform pSettings = new FauxEform();

	try {
	    pSettings.load( CONFIG_FILE );
	}
	catch( Throwable t ) {}

	String sFile = pSettings.getStringAttr( CONFIG_FILE_ATTR, "..\\..\\specs\\pdgdemo.xml" );

	new FileDialog( "Echo Proxy Config Info...", sFile, this );
    }

    public boolean hasGUI() { return true; }

    public void setGUIVisibility(boolean isVisible) {
	m_pUIFrame.setVisible( isVisible );
    }

    public boolean isGUIVisible() {
	return m_pUIFrame.isVisible();
    }

    public void start() { m_bActive = true; }
    public void stop() { m_bActive = false; }

    public boolean isRunning() { return m_bActive; }

    public String getStatus() { return null; }

    public void setPort( int nPort ) {

	m_nPort = nPort;
    }

    public int getPort() { return m_nPort; }

    public String getName() {
	return m_sName;
    }

    public String getSpec() {

	return m_sSpec;
    }

    public void requestFullState() {
	System.out.println( "Full state requested" );

	// submit variable states for those that exist
	Enumeration e = m_pAppliance.m_Objects.elements();
	while( e.hasMoreElements() ) {
	    ApplianceObject pObj = (ApplianceObject)e.nextElement();

	    if ( pObj.isState() ) {
		ApplianceState pState = (ApplianceState)pObj;
		String sValue = pState.m_Type.getValueSpace().getValue().toString();

		dispatchStateEvent( pState.m_sName, sValue );
	    }
	}
    }

    public void requestStateChange(String state, String value) {
	System.out.println( "State changed: " + state + " = " + value );

	dispatchStateEvent( state, value );
	((EchoConnection)m_pAppliance.m_pConnection).received( state, value );
    }

    private void dispatchStateEvent(String state, String value)  {
	StateListener l;
	Enumeration en;

	en = m_vListeners.elements();
	while( en.hasMoreElements() ) {
	    l = (StateListener)en.nextElement();
	    l.stateChanged(getName(), state, value);
	}
    }

    public void requestCommandInvoke(String command) {
	System.out.println( "Command invoked: " + command );
    }

    public void addStateListener(StateListener sl) {
	m_vListeners.addElement( sl );
    }

    public void removeStateListener(StateListener sl) {
	m_vListeners.removeElement( sl );
    }
}
