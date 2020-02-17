/*
 * DenaliClimateDevice.java
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.*;
import java.awt.event.*;

import java.net.InetAddress;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

import com.maya.puc.common.*;


// Class Definition

public class DenaliClimateDevice extends AbstractDevice2 {

    //**************************
    // Constants
    //**************************

    protected final static String CONFIG_FILE = "denaliclimate.cfg";

    public final static String STATE_PERSON_LOCATION        = "Denali.External.PersonLocation";
    public final static String STATE_SYSTEM_POWER           = "Denali.Front.SystemPower";
    public final static String STATE_AUTOMATIC_MODE         = "Denali.Front.AutomaticMode";
    public final static String STATE_DRIVERS_TEMP           = "Denali.Front.DriversTemp";
    public final static String STATE_PASSENGERS_TEMP        = "Denali.Front.PassengersTemp";
    public final static String STATE_FAN_SETTING            = "Denali.Front.FanSetting";
    public final static String STATE_AIRFLOW_DIRECTION      = "Denali.Front.AirflowDirection";
    public final static String STATE_RECIRCULATION          = "Denali.Front.Recirculation";
    public final static String STATE_AIR_CONDITIONING       = "Denali.Front.AirConditioning";
    public final static String STATE_FRONT_DEFROST          = "Denali.Front.FrontDefrost";
    public final static String STATE_REAR_DEFOGGER          = "Denali.Front.RearDefogger";
    public final static String STATE_REAR_SYSTEM_POWER      = "Denali.Rear.Controls.RearSystemPower";
    public final static String STATE_REAR_SYSTEM_MODE       = "Denali.Rear.RearSystemMode";
    public final static String STATE_REAR_AUTOMATIC_MODE    = "Denali.Rear.Controls.Powered.RearAutomaticMode";
    public final static String STATE_REAR_TEMP              = "Denali.Rear.Controls.Powered.RearTemp";
    public final static String STATE_REAR_FAN_SETTING       = "Denali.Rear.Controls.Powered.RearFanSetting";
    public final static String STATE_REAR_AIRFLOW_DIRECTION = "Denali.Rear.Controls.Powered.RearAirflowDirection";

    protected final static String SPEC_NAME = "DenaliClimateSpec.xml";

    //**************************
    // Member Variables
    //**************************

    protected Hashtable        m_hStates;
    protected DenaliClimateGUI m_pGUI;
    protected JFrame           m_pFrame;


    //**************************
    // Constructor
    //**************************

    public DenaliClimateDevice() {

	m_hStates = new Hashtable();

	m_hStates.put( STATE_SYSTEM_POWER, new Boolean( true ) );
	m_hStates.put( STATE_AUTOMATIC_MODE, new Boolean( true ) );
	m_hStates.put( STATE_DRIVERS_TEMP, new Integer( 74 ) );
	m_hStates.put( STATE_PASSENGERS_TEMP, new Integer( 74 ) );
	m_hStates.put( STATE_FAN_SETTING, new Integer( 5 ) );
	m_hStates.put( STATE_AIRFLOW_DIRECTION, new Integer( 2 ) );
	m_hStates.put( STATE_RECIRCULATION, new Boolean( false ) );
	m_hStates.put( STATE_AIR_CONDITIONING, new Boolean( true ) );
	m_hStates.put( STATE_FRONT_DEFROST, new Boolean( false ) );
	m_hStates.put( STATE_REAR_DEFOGGER, new Boolean( false ) );
	m_hStates.put( STATE_REAR_SYSTEM_POWER, new Boolean( true ) );
	m_hStates.put( STATE_REAR_SYSTEM_MODE, new Boolean( true ) );
	m_hStates.put( STATE_REAR_AUTOMATIC_MODE, new Boolean( true ) );
	m_hStates.put( STATE_REAR_TEMP, new Integer( 74 ) );
	m_hStates.put( STATE_REAR_FAN_SETTING, new Integer( 5 ) );
	m_hStates.put( STATE_REAR_AIRFLOW_DIRECTION, new Integer( 1 ) );

	m_pFrame = new JFrame( "Denali Climate Control Simulation" );
	m_pGUI = new DenaliClimateGUI( this );
	m_pGUI.updateInteractiveVars();

	m_pFrame.getContentPane().add( m_pGUI );

	Insets i = m_pFrame.getInsets();
	m_pFrame.setSize( 506 + i.left + i.right, 730 + i.top + i.bottom );
    }


    //**************************
    // Device2 Methods
    //**************************

    public Object getStateValue( String stateName ) {

	return m_hStates.get( stateName );
    }

    public void setStateValue( String stateName, Object stateValue ) {

	m_hStates.put( stateName, stateValue );
	dispatchStateEvent( stateName, stateValue.toString() );
    }

    public void start() {

	super.start( AbstractDevice2.STATUS_ACTIVE );
    }

    public void stop() {

	super.stop();
    }

    public String getName() {

	return "GMC Denali Climate Controls";
    }

    public int getDefaultPort() {

	return 5161;
    }

    public String getSpecFileName() {

	return SPEC_NAME;
    }

    public void configure() {

	new PersonLocationDialog();
    }

    public void addConnection(PUCServer.Connection c)
    {
	super.addConnection( c );

	connectionData.put( c, "1" );
    }

    public void handleMessage( PUCServer.Connection conn, Message msg ) {

	if ( msg instanceof Message.FullStateRequest ) {

        conn.send(new Message.StateChangeNotification(STATE_PERSON_LOCATION,
            connectionData.get( conn ).toString()));

	    Enumeration en = m_hStates.keys();
	    while( en.hasMoreElements() ) {
		String stateName = (String)en.nextElement();
        conn.send(new Message.StateChangeNotification(stateName,
            m_hStates.get( stateName ).toString()));
	    }
	}
	else if ( msg instanceof Message.StateChangeRequest ) {

	    Message.StateChangeRequest scr = (Message.StateChangeRequest)msg;

	    if ( m_hStates.get( scr.getState() ) != null ) {
		// should probably do error checking here
		if ( scr.getState().equals( STATE_SYSTEM_POWER ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_AUTOMATIC_MODE ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_RECIRCULATION ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_AIR_CONDITIONING ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_FRONT_DEFROST ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_REAR_DEFOGGER ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_REAR_AUTOMATIC_MODE ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_DRIVERS_TEMP ) ) {
		    m_hStates.put( scr.getState(), Integer.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_PASSENGERS_TEMP ) ) {
		    m_hStates.put( scr.getState(), Integer.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_FAN_SETTING ) ) {
		    m_hStates.put( scr.getState(), Integer.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_AIRFLOW_DIRECTION ) ) {
		    m_hStates.put( scr.getState(), Integer.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_REAR_SYSTEM_POWER ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_REAR_SYSTEM_MODE ) ) {
		    m_hStates.put( scr.getState(), Boolean.valueOf( scr.getValue() ) );

		    int reartemp = m_pGUI.getRearTemp();
		    int reardir = m_pGUI.getRearDir();
		    m_hStates.put( STATE_REAR_TEMP, new Integer( reartemp ) );
		    m_hStates.put( STATE_REAR_AIRFLOW_DIRECTION, new Integer( reardir ) );
		    dispatchStateEvent( STATE_REAR_TEMP, reartemp + "" );
		    dispatchStateEvent( STATE_REAR_AIRFLOW_DIRECTION, reardir + "" );
		}
		else if ( scr.getState().equals( STATE_REAR_TEMP ) ) {
		    m_hStates.put( scr.getState(), Integer.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_REAR_FAN_SETTING ) ) {
		    m_hStates.put( scr.getState(), Integer.valueOf( scr.getValue() ) );
		}
		else if ( scr.getState().equals( STATE_REAR_AIRFLOW_DIRECTION ) ) {
		    m_hStates.put( scr.getState(), Integer.valueOf( scr.getValue() ) );
		}

		dispatchStateEvent( scr.getState(), scr.getValue() );

		m_pGUI.updateInteractiveVars();
		m_pGUI.repaint();
	    }
	}
	else if ( msg instanceof Message.SpecRequest ) {

	    try {
		conn.send( new Message.DeviceSpec( getSpec() ) );
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }
	}
    }

    public boolean hasGUI() { return true; }

    public void setGUIVisibility(boolean isVisible) {

	m_pFrame.setVisible( isVisible );
    }

    public boolean isGUIVisible() { return m_pFrame.isVisible(); }

    public class PersonLocationDialog extends Frame {

	/*
	 * Member Variables
	 */

	protected Hashtable m_hChoiceBoxes;
	protected Panel m_pPanel;


	/*
	 * Constructor
	 */

	public PersonLocationDialog() {

	    super( "Configure Locations" );

	    m_hChoiceBoxes = new Hashtable();

	    this.setLayout( new BorderLayout() );

	    ScrollPane pane = new ScrollPane();
	    this.add( pane );

	    m_pPanel = new Panel();
	    pane.add( m_pPanel );

	    m_pPanel.setLayout( null );

	    this.show();
	}


	/*
	 * AddNotify()
	 */

	public void addNotify() {

	    Label lbl;
	    Choice chc;
	    int top = 10;

	    super.addNotify();

	    // fill in the panel and choose sizes
	    for( int i = 0; i < connections.size(); i++ ) {
		try {
		    PUCServer.Connection c = (PUCServer.Connection)connections.get( i );
		    String data = (String)connectionData.get( c );
		    InetAddress addr = c.getInetAddress();

		    int idx = Integer.parseInt( data );

		    lbl = new Label( addr.toString() );
		    chc = new Choice();
		    chc.add("Driver");
		    chc.add("Passenger");
		    chc.add("Rear");
		    chc.select( idx-1 );

		    m_pPanel.add( lbl );
		    m_pPanel.add( chc );

		    lbl.setLocation( 10, top );
		    lbl.setSize( lbl.getPreferredSize() );
		    chc.setLocation( lbl.getSize().width + 15, top );
		    chc.setSize( chc.getPreferredSize() );

		    m_hChoiceBoxes.put( c, chc );

		    top += chc.getSize().height + 10;
		}
		catch( Exception e ) {
		    e.printStackTrace();
		}
	    }

	    Button cancel = new Button( "Cancel" );
	    Button okay = new Button( "Okay" );

	    m_pPanel.add( cancel );
	    m_pPanel.add( okay );

	    cancel.setLocation( 10, top );
	    cancel.setSize( cancel.getPreferredSize() );
	    okay.setLocation( cancel.getSize().width + 15, top );
	    okay.setSize( okay.getPreferredSize() );

	    System.out.println( cancel.getSize().toString() + " @ " + cancel.getLocation().toString() );
	    System.out.println( okay.getSize().toString() + " @ " + okay.getLocation().toString() );

	    m_pPanel.setSize( 300, top + okay.getPreferredSize().height + 20 );

	    cancel.addActionListener( new ActionListener() {

		    public void actionPerformed( ActionEvent evt ) {

			hide();
			dispose();
		    }
		});

	    okay.addActionListener( new ActionListener() {

		    public void actionPerformed( ActionEvent evt ) {

			hide();

			Enumeration en = m_hChoiceBoxes.keys();
			while( en.hasMoreElements() ) {

			    try {
				PUCServer.Connection c =
				    (PUCServer.Connection)en.nextElement();
				Choice chc = (Choice)m_hChoiceBoxes.get( c );
				String choiceStr =
				    ( chc.getSelectedIndex() + 1 ) + "";

				connectionData.put( c, choiceStr );

				c.send( new Message.StateChangeNotification( STATE_PERSON_LOCATION,
								     choiceStr ) );
			    }
			    catch( Exception e ) { }
			}

			dispose();
		    }
		});

	    this.setSize( 300, 400 );
	}
    }
}
