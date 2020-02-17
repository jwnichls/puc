/*
 * SlideShowConnector.java
 * 
 * This class provides a connection using the Pebbles protocol to
 * Powerpoint through the SlideShowControl plug-in.  It is designed to
 * interoperate with PowerpointDevice, which talks the PUC protocol to
 * various hand-held devices.
 *
 * Revision History
 * ------------------------------
 * JWN: 04-19-2004: Adapted to Powerpoint tags
 *
 */

// Package Definition

package edu.cmu.hcii.puc.devices.powerpoint;


// Import Declarations

import java.net.*;
import java.util.StringTokenizer;
import java.util.Vector;

import pebbles.*;
import pebbles.net.*;
import pebbles.util.*;


// Class Definition

public class SlideShowConnector extends BasicUser {

    /*
     * SlideShowCommander Messages
     *
     * Copied from the SlideShowMessages.h
     */

    protected static final byte SLIDESHOW_BASE = Pebbles.MIN + 10;

    // events

    protected static final byte CMD_PRESENTATION_NAME     = SLIDESHOW_BASE + 1;
    protected static final byte CMD_SLIDE_NUMBER          = SLIDESHOW_BASE + 2;

    protected static final byte CMD_START_PRESENTATION    = SLIDESHOW_BASE + 3;
    protected static final byte CMD_RESUME_PRESENTATION   = SLIDESHOW_BASE + 19;
    protected static final byte CMD_STOP_PRESENTATION     = SLIDESHOW_BASE + 4;

    protected static final byte CMD_GOTO_SLIDE            = SLIDESHOW_BASE + 5;

    protected static final byte CMD_COLOR                 = SLIDESHOW_BASE + 6;

    // data requests

    protected static final byte CMD_AUTO_REFRESH          = SLIDESHOW_BASE + 11;
    protected static final byte CMD_GET_PRESENTATION_NAME = SLIDESHOW_BASE + 9;
    protected static final byte CMD_GET_SLIDE_NUMBER      = SLIDESHOW_BASE + 10;
    protected static final byte CMD_GET_TITLES            = SLIDESHOW_BASE + 7;
    protected static final byte CMD_GET_SLIDES            = SLIDESHOW_BASE + 8;

    // data

    protected static final byte CMD_TITLES                = SLIDESHOW_BASE + 12;
    protected static final byte CMD_SLIDE_TEXT            = SLIDESHOW_BASE + 13;
    protected static final byte CMD_SLIDE_IMAGE           = SLIDESHOW_BASE + 14;
    protected static final byte CMD_SLIDE_DISPLAY_MODE    = SLIDESHOW_BASE + 15;
    protected static final byte CMD_SLIDE_THUMBNAIL_SIZE  = SLIDESHOW_BASE + 17;
    protected static final byte CMD_SLIDE_ZOOM            = SLIDESHOW_BASE + 16;
    protected static final byte CMD_SLIDE_ZOOMED_IMAGE    = SLIDESHOW_BASE + 18;
    protected static final byte CMD_TAP_LEFT_DOWN         = SLIDESHOW_BASE + 20;
    protected static final byte CMD_TAP_LEFT_UP           = SLIDESHOW_BASE + 21;
    protected static final byte CMD_FORWARD_SLIDE         = SLIDESHOW_BASE + 22;
    protected static final byte CMD_BACKWARD_SLIDE        = SLIDESHOW_BASE + 23;
    protected static final byte CMD_POWERPOINT_FOREGROUND = SLIDESHOW_BASE + 24;
    
    public static final byte AUTO_REFRESH_TITLES = 0x01;
    public static final byte AUTO_REFRESH_SLIDES = 0x02;

    public static final byte INCLUDE_TEXT  = 0x10;
    public static final byte INCLUDE_NOTES = 0x20;
    public static final byte INCLUDE_IMAGE = 0x40;

    public static final byte BW_1BPP    = 0; // monochrome
    public static final byte BW_2BPP    = 1; // 4-level grayscale
    public static final byte COLOR_4BPP = 2; // 16 colors 
    public static final byte COLOR_8BPP = 3; // 256 colors
	
    public static final byte WINDOWS_BITMAP_FORMAT = 0x00; // OR these into the #pixel mode
    public static final byte PALM_BITMAP_FORMAT    = 0x10;


    /*
     * Member Variables
     */

    protected Vector m_vListeners;

    protected Plugin m_pPlugin;


    /*
     * Constructors
     */

    public SlideShowConnector() {

	m_vListeners = new Vector();
	m_pPlugin = null;
    }


    /*
     * Connection
     */

    public void connect() throws Exception {

	m_pPlugin = connectBySocket( "SlideShowControl" );
    }

    public void disconnect() {

	m_pPlugin = null;
    }

    public boolean isConnected() {

	return m_pPlugin != null;
    }


    /*
     * Listeners
     */

    public void addListener( SlideShowListener l ) {

	m_vListeners.add( l );
    }

    public void removeListener( SlideShowListener l ) {

	m_vListeners.remove( l );
    }

    public void removeAllListeners() {

	m_vListeners.removeAllElements();
    }


    /*
     * Functions that send data to Powerpoint
     */

    public void forwardSlide() {

	try {
	    new Message( CMD_FORWARD_SLIDE ).send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void backwardSlide() {

	try {
	    new Message( CMD_BACKWARD_SLIDE ).send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void setSlideNumber( int num ) {

	try {
	    Msg.pack( CMD_GOTO_SLIDE, "S", new Object[] { new Integer( num ) } )
		.send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void requestSlideData( int beginSlide, int endSlide ) {

	try {
	    Msg.pack( CMD_GET_SLIDES, "SSb", new Object[] { new Integer( beginSlide ), 
							    new Integer( endSlide ), 
							    new Integer( 0x30 ) } )
		.send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void setAutoRefresh( byte refresh, byte include ) {

	try {
	    Msg.pack( CMD_AUTO_REFRESH, "B", new Object[] { new Integer( refresh | include ) } )
		.send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void setSlideDisplayMode( byte color, byte type ) {

	try {
	    Msg.pack( CMD_SLIDE_DISPLAY_MODE, "B", new Object[] { new Integer( color | type ) } )
		.send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void requestTitles() {

	try {
	    new Message( CMD_GET_TITLES ).send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void requestSlideNumber() {

	try {
	    new Message( CMD_GET_SLIDE_NUMBER ).send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void requestPresentationName() {

	try {
	    new Message( CMD_GET_PRESENTATION_NAME )
		.send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void startPresentation() {

	try {
	    new Message( CMD_START_PRESENTATION )
		.send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void stopPresentation() {

	try {
	    new Message( CMD_STOP_PRESENTATION )
		.send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    public void resumePresentation() {

	try {
	    new Message( CMD_RESUME_PRESENTATION )
		.send( m_pPlugin, this );
	}
	catch( PebblesException e ) {

	    sendDisconnectEvent();
	}
    }

    
    /*
     * handle method for receiving messages from SlideShowControl
     */

    public void handle( Message m ) {
	
	switch (m.getCommand ()) {
	    
	case CMD_PRESENTATION_NAME: {
	    Object[] args = Msg.unpack( m, "a" );
	    String name = (String)args[ 0 ];
	    
	    for( int i = 0; i < m_vListeners.size(); i++ )
		((SlideShowListener)m_vListeners.elementAt(i))
		    .setPresentationName( name );
	    
	    break;
	}
	case CMD_TITLES: {
	    Object[] args = Msg.unpack( m, "a" );
	    String alltext = (String)args[ 0 ];
	    
	    Vector vTitles = new Vector();
	    StringTokenizer st = new StringTokenizer( alltext, "\0" );
	    
	    while( st.hasMoreTokens() )
		vTitles.add( st.nextToken() );
	    
	    String[] titles = new String[ vTitles.size() ];
	    for( int i = 0; i < vTitles.size(); i++ )
		titles[ i ] = (String)vTitles.elementAt( i );
	    
	    for( int i = 0; i < m_vListeners.size(); i++ )
		((SlideShowListener)m_vListeners.elementAt(i))
		    .setTitles( titles );
	    break;
	}
	case CMD_SLIDE_TEXT: {
	    // Extract the message data: in this case, a string 
	    Object[] args = Msg.unpack (m, "Sa");
	    int number = ((Integer)args[0]).intValue();
	    String alltext = (String)args[1];
	    
	    int index = alltext.indexOf( "\0" );
	    String text = alltext.substring( 0, index );
	    String notes = alltext.substring( index+1, alltext.length() ); 
	    
	    for( int i = 0; i < m_vListeners.size(); i++ )
		((SlideShowListener)m_vListeners.elementAt(i))
		    .setSlideText( number, text, notes );
	    break;
	}
	case CMD_SLIDE_NUMBER: {
	    Object[] args = Msg.unpack(m, "S");
	    int number = ((Integer)args[0]).intValue();
	    
	    for( int i = 0; i < m_vListeners.size(); i++ )
		((SlideShowListener)m_vListeners.elementAt(i))
		    .setSlideNumber( number );
	    break;
	}
	case CMD_SLIDE_IMAGE: {
	    for( int i = 0; i < m_vListeners.size(); i++ )
		((SlideShowListener)m_vListeners.elementAt(i))
		    .setSlideImage( m.getData() );
	    break;
	}
	default:
	    for( int i = 0; i < m_vListeners.size(); i++ )
		((SlideShowListener)m_vListeners.elementAt(i))
		    .unknownMessage( m );
	    break;
	}
    }

    protected void sendDisconnectEvent() {

	disconnect();
	
	for( int i = 0; i < m_vListeners.size(); i++ )
	    ((SlideShowListener)m_vListeners.elementAt(i))
		.disconnected();
    }
}
