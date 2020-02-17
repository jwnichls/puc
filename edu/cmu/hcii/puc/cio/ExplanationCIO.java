/**
 * ExplanationCIO.java
 *
 * A sub-class of StateLinkedCIO.
 *
 * The concrete representation of a Label widget.
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.FontMetrics;

import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.Globals;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.Label;
import edu.cmu.hcii.puc.awt.Panel;

import edu.cmu.hcii.puc.registry.WidgetRegistry;


// Class Definition

public class ExplanationCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static 
    //**************************

    static class ExplanationCIOFactory implements CIOFactory {

	public ConcreteInteractionObject createCIO( Appliance a,
						    ApplianceObject ao ) {

	    return new ExplanationCIO( a, ao );
	}
    }

    static {
	// register the factory with the WidgetRegistry
	WidgetRegistry.addCIOFactory( "ExplanationCIO", new ExplanationCIOFactory() );
    }


    //**************************
    // Member Variables
    //**************************


    //**************************
    // Constructor
    //**************************

    public ExplanationCIO( Appliance appl, ApplianceObject applObj ) {

	super( appl, applObj, null );

	m_Widget = new Label( this, "", Label.CENTER );

	refreshDisplay();
    }


    //**************************
    // Protected Methods
    //**************************

    protected void refreshDisplay() {

	String lbl;

	try {
	    FontMetrics fm = Globals.getFontMetricsObj( m_Widget.getFont() );
	    
	    lbl = m_ApplObj.m_Labels.getLabelByPixelLength( fm, m_Widget.getSize().width );
	}
	catch( Throwable t ) {
	    lbl = m_ApplObj.m_Labels.getLabelByCharLength( 10 );
	}
	
	((Label)m_Widget).setText( lbl );
    }
    

    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() {

	((Label)m_Widget).setText( m_ApplObj.m_Labels.getShortestLabel() );
    }

    public boolean prefersFullWidth() { return true; }

    public void addNotify() {
	
	refreshDisplay();
    }    
}
