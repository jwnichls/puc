/**
 * LabelLinkedCIO.java
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

import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.Globals;
import edu.cmu.hcii.puc.LabelLibrary;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.Label;

import edu.cmu.hcii.puc.registry.WidgetRegistry;


// Class Definition

public class LabelLinkedCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static 
    //**************************

    static class LabelLinkedCIOFactory implements CIOFactory {

	public ConcreteInteractionObject createCIO( Appliance a,
						    ApplianceObject ao ) {

	    return new LabelLinkedCIO( a, ao );
	}
    }

    static {
	// register the factory with the WidgetRegistry
	WidgetRegistry.addCIOFactory( "LabelLinkedCIO", new LabelLinkedCIOFactory() );
    }


    //**************************
    // Member Variables
    //**************************


    //**************************
    // Constructor
    //**************************

    public LabelLinkedCIO( Appliance appl, ApplianceObject applObj ) {

	super( appl, applObj, null );

	m_Widget = new Label( this );

	ApplianceState s = (ApplianceState)applObj;

	s.addStateListener( new StateListener() {

		public void enableChanged( ApplianceObject obj ) { 
		    m_Widget.setEnabled( m_ApplObj.isEnabled() );
		}

		public void labelChanged( ApplianceObject obj ) {
		    refreshDisplay();
		}

		public void typeChanged( ApplianceState obj ) { 
		    refreshDisplay();
		}
		
		public void valueChanged( ApplianceState obj ) { 
		    refreshDisplay();
		}
	    });

	refreshDisplay();
    }


    //**************************
    // Protected Methods
    //**************************

    protected void refreshDisplay() {

	ApplianceState s = (ApplianceState)m_ApplObj;

	String sLabel;

	if ( s.m_Type.getValueLabels() == null )
	    sLabel = s.m_Type.getValueSpace().getValue().toString();
	else {
	    // find the right LabelLibrary
	    int nIdx = 0;
	    Object pVal = s.m_Type.getValueSpace().getValue();

	    if ( pVal instanceof java.lang.Boolean )
		nIdx = ((Boolean)pVal).booleanValue() ? 1 : 0;
	    else if ( pVal instanceof java.lang.Number ) 
		nIdx = ((Number)pVal).intValue();
		
	    LabelLibrary pLabels = (LabelLibrary)s.m_Type.getValueLabels().elementAt( nIdx );

	    if ( pLabels == null ) 
		sLabel = s.m_Type.getValueSpace().getValue().toString();
	    else {
		try {
		    FontMetrics fm = Globals.getFontMetricsObj( m_Widget.getFont() );
		    sLabel = pLabels.getLabelByPixelLength( fm, m_Widget.getSize().width );
		}
		catch( Throwable t ) {
		    sLabel = pLabels.getLabelByCharLength( 10 );
		}
	    }
	}

	((Label)m_Widget).setText( sLabel );
    }
    

    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() { }

    public boolean hasLabel() { return m_ApplObj.m_Labels != null; }

    public ConcreteInteractionObject getLabelCIO() { 
	
	if ( m_ApplObj.m_Labels != null )
	    return new LabelCIO( m_ApplObj.m_Labels ); 
	else
	    return null;
    }

    public void addNotify() {
	
	refreshDisplay();
    }    
}
