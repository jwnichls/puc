/*
 * DenaliClimateGUI.java
 *
 * Knob Positions:
 * Front Top Mode: 70, 80, 80, 80
 * Front Top Temp: 216, 80, 80, 80
 * Front Top Dir:  362, 80, 80, 80
 *
 * Front Driver:   24, 285, 70, 70
 * Front Passenger:401,285, 70, 70 
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.*;
import java.awt.event.*;

import java.net.URL;

import javax.swing.*;


// Class Definition

public class DenaliClimateGUI extends JPanel {

    //**************************
    // Constants
    //**************************

    //**************************
    // Member Variables
    //**************************

    protected DenaliClimateDevice m_pDevice;

    protected Font m_pAutoFont;
    protected Font m_pTempFont;

    protected Image m_pBackgroundImage;
    protected Image m_pDirBothImage;
    protected Image m_pDirPanelImage;
    protected Image m_pDirFloorImage;
    protected Image m_pDirDefogImage;
    protected Image m_pNoAirImage;
    protected Image m_pFrontDefrostImage;
    protected Image m_pRearDefogImage;
    protected Image m_pRecircImage;

    protected KnobControl m_pTopModeKnob;
    protected KnobControl m_pTopTempKnob;
    protected KnobControl m_pTopDirKnob;
    protected KnobControl m_pDriverKnob;
    protected KnobControl m_pPassengerKnob;

    protected InvisibleButton m_pFrontFanUp;
    protected InvisibleButton m_pFrontFanDown;
    protected InvisibleButton m_pFrontAuto;
    protected InvisibleButton m_pFrontDir;
    protected InvisibleButton m_pFrontPower;
    protected InvisibleButton m_pFrontRecirc;
    protected InvisibleButton m_pFrontAC;
    protected InvisibleButton m_pFrontDefrost;
    protected InvisibleButton m_pRearDefog;

    protected InvisibleButton m_pRearPower;
    protected InvisibleButton m_pRearDir;
    protected InvisibleButton m_pRearTempDown;
    protected InvisibleButton m_pRearTempUp;
    protected InvisibleButton m_pRearFanDown;
    protected InvisibleButton m_pRearFanUp;


    //**************************
    // Constructor
    //**************************

    public DenaliClimateGUI( DenaliClimateDevice device ) {

	m_pDevice = device;

	m_pAutoFont = new Font( "SansSerif", Font.BOLD, 12 );
	m_pTempFont = new Font( "SansSerif", Font.BOLD, 36 );

	try {
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    URL url;
	    
	    MediaTracker mt = new MediaTracker( this );

            url = getClass().getResource("climate.jpg");
	    m_pBackgroundImage = tk.getImage( url );
	    mt.addImage( m_pBackgroundImage, 0 );

            url = getClass().getResource("dirboth.jpg");
	    m_pDirBothImage = tk.getImage( url );
	    mt.addImage( m_pDirBothImage, 1 );

            url = getClass().getResource("dirpanel.jpg");
	    m_pDirPanelImage = tk.getImage( url );
	    mt.addImage( m_pDirPanelImage, 2 );

            url = getClass().getResource("dirfloor.jpg");
	    m_pDirFloorImage = tk.getImage( url );
	    mt.addImage( m_pDirFloorImage, 3 );

            url = getClass().getResource("dirdefog.jpg");
	    m_pDirDefogImage = tk.getImage( url );
	    mt.addImage( m_pDirDefogImage, 4 );

            url = getClass().getResource("noaircond.jpg");
	    m_pNoAirImage = tk.getImage( url );
	    mt.addImage( m_pNoAirImage, 5 );

            url = getClass().getResource("frontdefrost.jpg");
	    m_pFrontDefrostImage = tk.getImage( url );
	    mt.addImage( m_pFrontDefrostImage, 6 );

            url = getClass().getResource("reardefog.jpg");
	    m_pRearDefogImage = tk.getImage( url );
	    mt.addImage( m_pRearDefogImage, 7 );

            url = getClass().getResource("recirc.jpg");
	    m_pRecircImage = tk.getImage( url );
	    mt.addImage( m_pRecircImage, 8 );

	    mt.waitForAll();
	}
	catch( Exception e ) { }

	this.setLayout( null );

	m_pTopModeKnob = new KnobControl( 0, Math.PI/16, 19 );
	m_pTopModeKnob.setSize( 80, 80 );
	m_pTopModeKnob.setLocation( 72, 78 );
	m_pTopModeKnob.setToolTipText( "Adjust fan/mode of rear climate controls" );
	this.add( m_pTopModeKnob );

	m_pTopModeKnob.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    boolean rearauto = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE )).booleanValue();


		    if ( m_pTopModeKnob.getIndex() > 13 && 
			 m_pTopModeKnob.getIndex() < 16 ) {

			if ( rearmode )
			    m_pTopModeKnob.setIndex( 13 );
			else
			    m_pTopModeKnob.setIndex( 16 );
		    }
		    else if ( m_pTopModeKnob.getIndex() > 16 &&
			      m_pTopModeKnob.getIndex() < 19 ) {

			if ( rearmode )
			    m_pTopModeKnob.setIndex( 19 );
			else
			    m_pTopModeKnob.setIndex( 16 );
		    }

		    switch ( m_pTopModeKnob.getIndex() ) {

		    case 19:
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE, new Boolean( true ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE, new Boolean( false ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AIRFLOW_DIRECTION, new Integer( 3 - m_pTopDirKnob.getIndex() ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_TEMP, new Integer( 82 - m_pTopTempKnob.getIndex() ) );
			break;

		    case 16:
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE, new Boolean( true ) );
			break;

		    case 13:
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE, new Boolean( false ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER, new Boolean( false ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AIRFLOW_DIRECTION, new Integer( 3 - m_pTopDirKnob.getIndex() ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_TEMP, new Integer( 82 - m_pTopTempKnob.getIndex() ) );
			break;

		    default:
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE, new Boolean( false ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER, new Boolean( true ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_FAN_SETTING, new Integer( 13 - m_pTopModeKnob.getIndex() ) );
			break;
		    }

		    repaint();
		}
	    });

	m_pTopTempKnob = new KnobControl( 0, Math.PI / 16, 16 );
	m_pTopTempKnob.setSize( 80, 80 );
	m_pTopTempKnob.setLocation( 218, 78 );
	m_pTopTempKnob.setToolTipText( "Adjust temperature in rear" );
	this.add( m_pTopTempKnob );

	m_pTopTempKnob.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    
		    if ( !rearmode )
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_TEMP, new Integer( 82 - m_pTopTempKnob.getIndex() ) );

		    repaint();
		}
	    });

	m_pTopDirKnob = new KnobControl( 0, Math.PI / 2, 2 );
	m_pTopDirKnob.setSize( 80, 80 );
	m_pTopDirKnob.setLocation( 363, 78 );
	m_pTopDirKnob.setToolTipText( "Adjust airflow direction in rear" );
	this.add( m_pTopDirKnob );

	m_pTopDirKnob.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    
		    if ( !rearmode )
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AIRFLOW_DIRECTION, new Integer( 3 - m_pTopDirKnob.getIndex() ) );

		    repaint();
		}
	    });

	m_pDriverKnob = new KnobControl( -1f/8f * Math.PI, Math.PI / 24, 30 );
	m_pDriverKnob.setSize( 70, 70 );
	m_pDriverKnob.setLocation( 24, 285 );
	m_pDriverKnob.setToolTipText( "Set driver's side temperature" );
	this.add( m_pDriverKnob );

	m_pDriverKnob.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean power = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER )).booleanValue();
		    if ( power ) {
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_DRIVERS_TEMP, new Integer( 90 - m_pDriverKnob.getIndex() ) );
			repaint();
		    }
		}
	    });

	m_pPassengerKnob = new KnobControl( -1f/8f * Math.PI, Math.PI / 24, 30 );
	m_pPassengerKnob.setSize( 70, 70 );
	m_pPassengerKnob.setLocation( 401, 283 );
	m_pPassengerKnob.setToolTipText( "Set passenger's side temperature" );
	this.add( m_pPassengerKnob );

	m_pPassengerKnob.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {
		    
		    boolean power = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER )).booleanValue();
		    if ( power ) {
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_PASSENGERS_TEMP, new Integer( 90 - m_pPassengerKnob.getIndex() ) );
			repaint();
		    }
		}
	    });

	m_pFrontFanUp = new InvisibleButton();
	m_pFrontFanUp.setSize( 40, 40 );
	m_pFrontFanUp.setLocation( 127, 263 );
	m_pFrontFanUp.setToolTipText( "Increase front fan speed" );
	this.add( m_pFrontFanUp );

	m_pFrontFanUp.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    int oldVal = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_FAN_SETTING )).intValue();
		    boolean auto = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_AUTOMATIC_MODE )).booleanValue();

		    if ( !auto && oldVal != 13 )
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_FAN_SETTING, new Integer( oldVal + 1 ) );
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER, new Boolean( true ) );
		    repaint();
		}
	    });

	m_pFrontFanDown = new InvisibleButton();
	m_pFrontFanDown.setSize( 40, 40 );
	m_pFrontFanDown.setLocation( 127, 333 );
	m_pFrontFanDown.setToolTipText( "Decrease front fan speed" );
	this.add( m_pFrontFanDown );

	m_pFrontFanDown.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    int oldVal = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_FAN_SETTING )).intValue();
		    boolean auto = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_AUTOMATIC_MODE )).booleanValue();

		    if ( !auto && oldVal != 0 )
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_FAN_SETTING, new Integer( oldVal - 1 ) );
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER, new Boolean( true ) );
		    repaint();
		}
	    });

	m_pFrontAuto = new InvisibleButton();
	m_pFrontAuto.setSize( 45, 50 );
	m_pFrontAuto.setLocation( 329, 261 );
	m_pFrontAuto.setToolTipText( "Toggle front automatic mode" );
	this.add( m_pFrontAuto );

	m_pFrontAuto.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    boolean power = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER )).booleanValue();
		    boolean oldVal;
		    if ( power ) 
			oldVal = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_AUTOMATIC_MODE )).booleanValue();
		    else
			oldVal = false;

		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_AUTOMATIC_MODE, new Boolean( !oldVal ) );
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER, new Boolean( true ) );
		    repaint();
		}
	    });

	m_pFrontDir = new InvisibleButton();
	m_pFrontDir.setSize( 45, 50 );
	m_pFrontDir.setLocation( 329, 325 );
	m_pFrontDir.setToolTipText( "Cycle through front air direction options" );
	this.add( m_pFrontDir );

	m_pFrontDir.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    boolean power = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER ) ).booleanValue();
		    int oldVal = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_AIRFLOW_DIRECTION )).intValue();

		    if ( power ) {
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_AIRFLOW_DIRECTION, new Integer( ( ( oldVal % 4 ) + 1 ) ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_AUTOMATIC_MODE, new Boolean( false ) );
			repaint();
		    }
		}
	    });

	m_pFrontPower = new InvisibleButton();
	m_pFrontPower.setSize( 75, 29 );
	m_pFrontPower.setLocation( 29, 398 );
	m_pFrontPower.setToolTipText( "Toggle power of front climate system" );
	this.add( m_pFrontPower );

	m_pFrontPower.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    boolean oldVal = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER )).booleanValue();
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER, new Boolean( !oldVal ) );
		    repaint();
		}
	    });

	m_pFrontRecirc = new InvisibleButton();
	m_pFrontRecirc.setSize( 75, 29 );
	m_pFrontRecirc.setLocation( 120, 398 );
	m_pFrontRecirc.setToolTipText( "Toggle recirculation mode" );
	this.add( m_pFrontRecirc );

	m_pFrontRecirc.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    boolean oldVal = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_RECIRCULATION )).booleanValue();
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_RECIRCULATION, new Boolean( !oldVal ) );
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_AUTOMATIC_MODE, new Boolean( false ) );
		    repaint();
		}
	    });

	m_pFrontAC = new InvisibleButton();
	m_pFrontAC.setSize( 75, 29 );
	m_pFrontAC.setLocation( 211, 398 );
	m_pFrontAC.setToolTipText( "Toggle A/C on and off" );
	this.add( m_pFrontAC );

	m_pFrontAC.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    boolean oldVal = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_AIR_CONDITIONING )).booleanValue();
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_AIR_CONDITIONING, new Boolean( !oldVal ) );
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER, new Boolean( true ) );
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_AUTOMATIC_MODE, new Boolean( false ) );
		    repaint();
		}
	    });

	m_pFrontDefrost = new InvisibleButton();
	m_pFrontDefrost.setSize( 75, 29 );
	m_pFrontDefrost.setLocation( 303, 398 );
	m_pFrontDefrost.setToolTipText( "Toggle front defrost on/off" );
	this.add( m_pFrontDefrost );

	m_pFrontDefrost.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    boolean oldVal = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_FRONT_DEFROST )).booleanValue();
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_FRONT_DEFROST, new Boolean( !oldVal ) );
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER, new Boolean( true ) );
		    repaint();
		}
	    });

	m_pRearDefog = new InvisibleButton();
	m_pRearDefog.setSize( 75, 29 );
	m_pRearDefog.setLocation( 394, 398 );
	m_pRearDefog.setToolTipText( "Toggle rear defog on/off" );
	this.add( m_pRearDefog );

	m_pRearDefog.addActionListener( new ActionListener() {
		
		public void actionPerformed( ActionEvent e ) {

		    boolean oldVal = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_DEFOGGER )).booleanValue();
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_DEFOGGER, new Boolean( !oldVal ) );
		    m_pDevice.setStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER, new Boolean( true ) );
		    repaint();
		}
	    });

	m_pRearPower = new InvisibleButton();
	m_pRearPower.setSize( 35, 30 );
	m_pRearPower.setLocation( 170, 584 );
	m_pRearPower.setToolTipText( "Toggle power of rear climate system" );
	this.add( m_pRearPower );

	m_pRearPower.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    boolean rearpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER )).booleanValue();
		    
		    if ( rearmode )
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER, new Boolean( !rearpower ) );

		    repaint();
		}
	    });

	m_pRearDir = new InvisibleButton();
	m_pRearDir.setSize( 35, 30 );
	m_pRearDir.setLocation( 204, 584 );
	m_pRearDir.setToolTipText( "Toggle rear air flow direction" );
	this.add( m_pRearDir );

	m_pRearDir.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    boolean rearpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER )).booleanValue();
		    boolean rearauto = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE )).booleanValue();
		    int reardir = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_AIRFLOW_DIRECTION )).intValue();
		    
		    if ( rearmode && rearpower ) {
			if ( reardir != 3 || rearauto ) {
			    m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AIRFLOW_DIRECTION, new Integer( ( reardir % 3 ) + 1 ) );
			    m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE, new Boolean( false ) );
			}
			else
			    m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE, new Boolean( true ) );
		    }

		    repaint();
		}
	    });

	m_pRearTempDown = new InvisibleButton();
	m_pRearTempDown.setSize( 35, 30 );
	m_pRearTempDown.setLocation( 262, 584 );
	m_pRearTempDown.setToolTipText( "Decrease rear temperature" );
	this.add( m_pRearTempDown );

	m_pRearTempDown.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    boolean rearpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER )).booleanValue();
		    int reartemp = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_TEMP )).intValue();
		    
		    if ( rearmode ) {
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER, new Boolean( true ) );
			if ( reartemp != 66 )
			    m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_TEMP, new Integer( reartemp - 1 ) );
		    }

		    repaint();
		}
	    });

	m_pRearTempUp = new InvisibleButton();
	m_pRearTempUp.setSize( 35, 30 );
	m_pRearTempUp.setLocation( 296, 584 );
	m_pRearTempUp.setToolTipText( "Increase rear temperature" );
	this.add( m_pRearTempUp );

	m_pRearTempUp.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    boolean rearpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER )).booleanValue();
		    int reartemp = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_TEMP )).intValue();
		    
		    if ( rearmode ) {
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER, new Boolean( true ) );
			if ( reartemp != 82 )
			    m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_TEMP, new Integer( reartemp + 1 ) );
		    }

		    repaint();
		}
	    });

	m_pRearFanDown = new InvisibleButton();
	m_pRearFanDown.setSize( 35, 30 );
	m_pRearFanDown.setLocation( 352, 584 );
	m_pRearFanDown.setToolTipText( "Increase rear fan speed" );
	this.add( m_pRearFanDown );

	m_pRearFanDown.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    boolean rearpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER )).booleanValue();
		    int rearfan = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_FAN_SETTING )).intValue();
		    
		    if ( rearmode ) {
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER, new Boolean( true ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE, new Boolean( false ) );
			if ( rearfan != 0 )
			    m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_FAN_SETTING, new Integer( rearfan - 1 ) );
		    }

		    repaint();
		}
	    });

	m_pRearFanUp = new InvisibleButton();
	m_pRearFanUp.setSize( 35, 30 );
	m_pRearFanUp.setLocation( 386, 584 );
	m_pRearFanUp.setToolTipText( "Decrease rear fan speed" );
	this.add( m_pRearFanUp );

	m_pRearFanUp.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
		    boolean rearpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER )).booleanValue();
		    int rearfan = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_FAN_SETTING )).intValue();
		    
		    if ( rearmode ) {
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER, new Boolean( true ) );
			m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE, new Boolean( false ) );
			if ( rearfan != 13 )
			    m_pDevice.setStateValue( DenaliClimateDevice.STATE_REAR_FAN_SETTING, new Integer( rearfan + 1 ) );
		    }

		    repaint();
		}
	    });
    }

    //**************************
    // Member Methods
    //**************************

    public boolean isOpaque() {
	
	return true;
    }

    public int getRearTemp() {

	return 82 - m_pTopTempKnob.getIndex();
    }

    public int getRearDir() {

	return 3 - m_pTopDirKnob.getIndex();
    }

    public void updateInteractiveVars() {

	int drivertemp = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_DRIVERS_TEMP ) ).intValue();
	int passengertemp = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_PASSENGERS_TEMP ) ).intValue();
	int reartemp = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_TEMP )).intValue();
	boolean rearpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER )).booleanValue();
	int rearfan = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_FAN_SETTING )).intValue();
	boolean rearauto = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE )).booleanValue();
	boolean rearmode = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_MODE )).booleanValue();
	int rearairdir = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_AIRFLOW_DIRECTION )).intValue();

	m_pDriverKnob.setIndex( 90 - drivertemp );
	m_pPassengerKnob.setIndex( 90 - passengertemp );	

	if ( !rearmode ) {
	    // control from front
	    m_pTopTempKnob.setIndex( 82 - reartemp );
	    m_pTopDirKnob.setIndex( 3 - rearairdir );

	    if ( !rearpower )
		m_pTopModeKnob.setIndex( 13 );
	    else if ( rearauto )
		m_pTopModeKnob.setIndex( 19 );
	    else
		m_pTopModeKnob.setIndex( 13 - rearfan );
	}
	else {
	    m_pTopModeKnob.setIndex( 16 );
	}
    }

    public void paintComponent( Graphics g ) {

	boolean air = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_AIR_CONDITIONING )).booleanValue();
	boolean frontdefrost = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_FRONT_DEFROST )).booleanValue();
	boolean reardefog = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_DEFOGGER )).booleanValue();
	boolean recirc = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_RECIRCULATION )).booleanValue();
	boolean frontpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_SYSTEM_POWER )).booleanValue();
	boolean frontauto = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_AUTOMATIC_MODE )).booleanValue();
	boolean rearauto = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_AUTOMATIC_MODE )).booleanValue();
	boolean rearpower = ((Boolean)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_SYSTEM_POWER )).booleanValue();

	int drivertemp = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_DRIVERS_TEMP ) ).intValue();
	int passengertemp = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_PASSENGERS_TEMP ) ).intValue();
	int frontairdir = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_AIRFLOW_DIRECTION )).intValue();
	int reartemp = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_TEMP )).intValue();
	int rearairdir = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_AIRFLOW_DIRECTION )).intValue();

	int frontfan = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_FAN_SETTING )).intValue();
	int rearfan = ((Integer)m_pDevice.getStateValue( DenaliClimateDevice.STATE_REAR_FAN_SETTING )).intValue();

	g.drawImage( m_pBackgroundImage, 0, 0, this );
	g.setColor( Color.white );

	if ( frontpower ) {

	    g.setFont( m_pTempFont );
	    g.drawString( drivertemp + "", 215, 330 );
	    g.drawString( passengertemp + "", 275, 330 );
	    g.setFont( m_pAutoFont );

	    if ( frontauto )
		g.drawString( "AUTO", 280, 285 );

	    if ( !frontauto ) {
		if ( !air )
		    g.drawImage( m_pNoAirImage, 237, 353, this );
	    
		if ( recirc )
		    g.drawImage( m_pRecircImage, 120, 399, this );

		switch( frontairdir ) {
		case 1: // panel
		    g.drawImage( m_pDirPanelImage, 304, 349, this );
		    break;

		case 2: // both
		    g.drawImage( m_pDirBothImage, 300, 350, this );
		    break;
		    
		case 3: // floor
		    g.drawImage( m_pDirFloorImage, 301, 350, this );
		    break;

		case 4: // defog
		    g.drawImage( m_pDirDefogImage, 297, 344, this );
		    break;
		}

		int top = 360, width = 20, height = 4;
		
		for( int i = 0; i < frontfan; i++ ) {
		    
		    g.fillRect( 185, top, width, height );
		    top -= height * 2;
		}
	    }

	    if ( frontdefrost )
		g.drawImage( m_pFrontDefrostImage, 302, 398, this );
	    
	    if ( reardefog )
		g.drawImage( m_pRearDefogImage, 394, 398, this );

	}

	if ( rearpower ) {

	    g.setFont( m_pTempFont );
	    g.drawString( reartemp + "", 260, 530 );
	    g.setFont( m_pAutoFont );

	    if ( rearauto )
		g.drawString( "AUTO", 340, 505 );
	    else {
		switch( rearairdir ) {

		case 1: // panel
		    g.drawImage( m_pDirPanelImage, 349, 519, this );
		    break;

		case 2: // both
		    g.drawImage( m_pDirBothImage, 344, 520, this );
		    break;
		    
		case 3: // floor
		    g.drawImage( m_pDirFloorImage, 345, 520, this );
		    break;
		}

		int top = 541, width = 20, height = 2;
		
		for( int i = 0; i < rearfan; i++ ) {
		    
		    g.fillRect( 220, top, width, height );
		    top -= height * 2;
		}
	    }
	}
    }

    
    //**************************
    // Static Main Method
    //**************************

    /*
    public static void main( String[] args ) {

	JFrame frame = new JFrame( "Denali Climate Control Simulation" );
	DenaliClimateGUI gui = new DenaliClimateGUI();
	
	// frame.getContentPane().setLayout( new BorderLayout() );
	frame.getContentPane().add( gui );

	Insets i = frame.getInsets();
	frame.setSize( 506 + i.left + i.right, 730 + i.top + i.bottom );
	frame.setVisible( true );
    }
    */
}
