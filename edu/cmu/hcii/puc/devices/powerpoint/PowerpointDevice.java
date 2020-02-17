/*
 * PowerpointDevice.java
 * 
 * This device talks the Pebbles protocol and uses the
 * SlideShowControl plugin in order to control Powerpoint and receive
 * state information.
 *
 * Revision History
 * ------------------------------
 * JWN: 04-19-2004: Adapted to Powerpoint tags
 *
 */

// Package Definition

package edu.cmu.hcii.puc.devices.powerpoint;


// Import Declarations

import java.awt.*;
import java.awt.image.*;

import edu.cmu.hcii.puc.devices.AbstractDevice2;
import edu.cmu.hcii.puc.devices.DeviceException;

import com.maya.puc.common.*;

import pebbles.PebblesException;


// Class Definition

public class PowerpointDevice extends AbstractDevice2 implements SlideShowListener {

    //**************************
    // Constants
    //**************************

    protected final static String SPEC_NAME       = "powerpoint.xml";

    protected final static String SLIDE_IMAGE_STATE = "Powerpoint.CurrentSlide.SlideImage";
    protected final static String SLIDE_NOTES_STATE = "Powerpoint.CurrentSlide.SlideNotes";
    protected final static String START_COMMAND = "Powerpoint.SlideControls.StartPresentation";
    protected final static String RESUME_COMMAND = "Powerpoint.SlideControls.ResumePresentation";
    protected final static String END_COMMAND = "Powerpoint.SlideControls.EndPresentation";
    protected final static String PREV_SLIDE_COMMAND = "Powerpoint.SlideControls.Controls.PrevTrack";
    protected final static String NEXT_SLIDE_COMMAND = "Powerpoint.SlideControls.Controls.NextTrack";
    protected final static String SLIDE_NUMBER_STATE = "Powerpoint.SlideControls.SlideNumber";
    protected final static String FIRST_SLIDE_STATE = "Powerpoint.SlideControls.FirstSlide";
    protected final static String LAST_SLIDE_STATE = "Powerpoint.SlideControls.LastSlide";
    protected final static String SLIDES_LIST = "Powerpoint.Slides.SlideList";
    protected final static String SLIDE_NAME_SUFFIX = "Name";
    protected final static String DEMO_APPS_LIST = "Powerpoint.DemoApps.Apps";
    protected final static String ICON_SUFFIX = "Icon";
    protected final static String APP_NAME_SUFFIX = "Name";


    //**************************
    // Member Variables
    //**************************

    protected byte[]             m_pCurrentSlideImage;
    protected String             m_pCurrentSlideNotes;
    
    protected int                m_nFirstSlide;
    protected int                m_nLastSlide;
    protected int                m_nCurrentSlide;
    
    protected String[]           m_pSlideNames;
    
    protected byte[][]           m_pDemoAppIcons;
    protected String[]           m_pDemoAppNames;

    protected SlideShowConnector m_pSlideShow;


    //**************************
    // Constructor
    //**************************

    public PowerpointDevice() {

	SlideShowConnector m_pSlideShow = new SlideShowConnector();
	m_pSlideShow.addListener( this );
    }


    //**************************
    // AbstractDevice2 Methods
    //**************************
    
    public void start() {

	try {
	    m_pSlideShow.connect();

	    super.start( AbstractDevice2.STATUS_ACTIVE );
	}
	catch( Exception e ) {

	    super.stop( AbstractDevice2.STATUS_ERROR );
	}
    }

    public void stop() {

	m_pSlideShow.disconnect();

	super.stop();
    }

    public String getName() {

	return "Powerpoint";
    }

    public int getDefaultPort() {

	return 5182;
    }

    public String getSpecFileName() {

	return SPEC_NAME;
    }

    public void configure() {

	// TODO: configure this so that it can connect to an arbitrary
	// IP address
    }

    public boolean hasGUI() { 

	return false; 
    }

    public void setGUIVisibility(boolean isVisible) {
    }

    public boolean isGUIVisible() { 

	return false; 
    }

    public void handleMessage( PUCServer.Connection conn, Message msg ) {

	if ( msg instanceof Message.FullStateRequest ) {

	}
	else if ( msg instanceof Message.StateValueRequest ) {

	}
	else if ( msg instanceof Message.CommandInvokeRequest ) {

	    String cmd = ((Message.CommandInvokeRequest)msg).getCommand();
	    
	    if ( cmd.equals( START_COMMAND ) ) {
		
		m_pSlideShow.startPresentation();
	    }
	    else if ( cmd.equals( RESUME_COMMAND ) ) {
		
		m_pSlideShow.resumePresentation();
	    }
	    else if ( cmd.equals( END_COMMAND ) ) {
		
		m_pSlideShow.stopPresentation();
	    }
	    else if ( cmd.equals( PREV_SLIDE_COMMAND ) ) {
		
		m_pSlideShow.backwardSlide();
	    }
	    else if ( cmd.equals( NEXT_SLIDE_COMMAND ) ) {
		
		m_pSlideShow.forwardSlide();
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

    /*
     * SlideShowListener methods
     */

    public void setPresentationName( String name ) { }
    public void setTitles( String[] titles ) { }
    public void setSlideText( int slideNumber, String text, String notes ) { }
    public void setSlideNumber( int slideNumber ) { }
    public void setSlideImage( byte[] image ) { }
    public void unknownMessage( pebbles.Message msg ) { }

    public void disconnected() {

	stop( AbstractDevice2.STATUS_ERROR );
    }
}
