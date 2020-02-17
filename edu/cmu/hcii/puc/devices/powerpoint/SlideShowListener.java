/*
 * SlideShowListener.java
 *
 * This interface defines the methods that the SlideShowConnector will
 * call when events are fired from Powerpoint.
 */

// Package Definition

package edu.cmu.hcii.puc.devices.powerpoint;


// Interface Definition

public interface SlideShowListener {

    public void setPresentationName( String name );
    public void setTitles( String[] titles );
    public void setSlideText( int slideNumber, String text, String notes );
    public void setSlideNumber( int slideNumber );
    public void setSlideImage( byte[] image );
    public void disconnected();
    public void unknownMessage( pebbles.Message msg );
}
