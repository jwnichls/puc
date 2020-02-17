/**
 * LabelCIO.java
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
import java.awt.Panel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.LabelLibrary;
import edu.cmu.hcii.puc.Globals;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.Label;

import edu.cmu.hcii.puc.registry.WidgetRegistry;


// Class Definition

public class LabelCIO extends IndependentCIO {

    //**************************
    // Member Variables
    //**************************

    protected LabelLibrary m_pLabelLib;


    //**************************
    // Constructor
    //**************************

    public LabelCIO( LabelLibrary pLib ) {

	super( null );

	m_pLabelLib = pLib;

	m_Widget = new Label( this );

	m_Widget.addComponentListener( new ComponentAdapter() {
		public void componentResized( ComponentEvent e ) {
		    setLabelText();
		}
	    });
    }


    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() {

	((Label)m_Widget).setText( m_pLabelLib.getShortestLabel() );
    }

    public void addNotify() {
	setLabelText();
    }

    public void setAlignment( int nOrient ) {

	((Label)m_Widget).setAlignment( nOrient );
    }

    public void setLabelText() {
	
	String lbl;

	try {
	    FontMetrics fm = Globals.getFontMetricsObj( m_Widget.getFont() );
	    
	    lbl = m_pLabelLib.getLabelByPixelLength( fm, m_Widget.getSize().width ) + ":";
	}
	catch( Throwable t ) {
	    lbl = m_pLabelLib.getLabelByCharLength( 10 ) + ":";
	}

	((Label)m_Widget).setText( lbl );
    }    
}
