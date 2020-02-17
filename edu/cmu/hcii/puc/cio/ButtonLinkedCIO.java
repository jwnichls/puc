/**
 * ButtonLinkedCIO.java
 *
 * A sub-class of StateLinkedCIO.
 *
 * The concrete representation of a Button widget.
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.FontMetrics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Label;
import java.awt.Panel;

import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.ObjectListener;
import edu.cmu.hcii.puc.Globals;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.Button;

import edu.cmu.hcii.puc.registry.WidgetRegistry;

import edu.cmu.hcii.puc.types.ValueSpace;

import com.maya.puc.common.Message;
//import com.maya.puc.common.Message.StateChangeRequest;
//import com.maya.puc.common.Message.CommandInvokeRequest;


// Class Definition

public class ButtonLinkedCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static 
    //**************************

    static class ButtonLinkedCIOFactory implements CIOFactory {

	public ConcreteInteractionObject createCIO( Appliance a,
						    ApplianceObject ao ) {

	    return new ButtonLinkedCIO( a, ao );
	}
    }

    static {
	// register the factory with the WidgetRegistry
	WidgetRegistry.addCIOFactory( "ButtonLinkedCIO", new ButtonLinkedCIOFactory() );
    }


    //**************************
    // Member Variables
    //**************************


    //**************************
    // Constructor
    //**************************

    public ButtonLinkedCIO( Appliance appl, ApplianceObject applObj ) {

	super( appl, applObj, null );

	m_Widget = new Button( this );

	if ( !m_ApplObj.isState() ) {
	    m_ApplObj.addObjListener( new ObjectListener() {
		    
		    public void labelChanged( ApplianceObject obj ) {
			refreshDisplay();
		    }

		    public void enableChanged( ApplianceObject obj ) { 
			m_Widget.setEnabled( m_ApplObj.isEnabled() );
		    }
		});

	    ((Button)m_Widget).addActionListener( new ActionListener() {
		    public void actionPerformed( ActionEvent e ) {
			Message msg = new Message.CommandInvokeRequest( ButtonLinkedCIO.this.m_ApplObj.m_sName );

		    try { ButtonLinkedCIO.this.m_Appliance.m_pConnection.send( msg ); }
		    catch( Throwable t ) { }
		    }
		});
	}
	else {
	    ((ApplianceState)m_ApplObj).addStateListener( new StateListener() {

		    public void labelChanged( ApplianceObject obj ) {
			refreshDisplay();
		    }

		    public void enableChanged( ApplianceObject obj ) { 
			m_Widget.setEnabled( m_ApplObj.isEnabled() );
		    }

		    public void typeChanged( ApplianceState obj ) { 
			refreshDisplay();
		    }
		    
		    public void valueChanged( ApplianceState obj ) { 
			refreshDisplay();
		    }
		});

	    ApplianceState as = (ApplianceState)m_ApplObj;
	    if ( as.m_Type.getValueSpace().getSpace() == ValueSpace.BOOLEAN_SPACE ) {
		((Button)m_Widget).addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
			    Message msg = new Message.StateChangeRequest( m_ApplObj.m_sName, new Boolean(!((Boolean)((ApplianceState)m_ApplObj).m_Type.getValueSpace().getValue()).booleanValue()).toString() );
			    
			    try { ButtonLinkedCIO.this.m_Appliance.m_pConnection.send( msg ); }
			    catch( Throwable t ) { }
			}
		    });
	    }
	}

	refreshDisplay();
    }


    //**************************
    // Protected Methods
    //**************************

    protected void refreshDisplay() {

	String lbl;

	try {
	    FontMetrics fm = Globals.getFontMetricsObj( m_Widget.getFont() );

	    lbl = m_ApplObj.m_Labels.getLabelByPixelLength( fm, m_Widget.getSize().width - 10 );
	}
	catch( Throwable t ) {
	    lbl = m_ApplObj.m_Labels.getLabelByCharLength( 10 );
	}

	((Button)m_Widget).setLabel( lbl );
    }
    

    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() {

	((Button)m_Widget).setLabel( m_ApplObj.m_Labels.getShortestLabel() );
    }

    public void addNotify() {

	refreshDisplay();
    }
}
