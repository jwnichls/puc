/**
 * TextFieldLinkedCIO.java
 *
 * A sub-class of StateLinkedCIO.
 *
 * The concrete representation of a TextField widget.
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.Panel;

import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.PUC;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.TextField;

import edu.cmu.hcii.puc.registry.WidgetRegistry;

import com.maya.puc.common.Message;


// Class Definition

public class TextFieldLinkedCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static 
    //**************************

    static class TextFieldLinkedCIOFactory implements CIOFactory {

	public ConcreteInteractionObject createCIO( Appliance a,
						    ApplianceObject ao ) {

	    return new TextFieldLinkedCIO( a, ao );
	}
    }

    static {
	// register the factory with the WidgetRegistry
	WidgetRegistry.addCIOFactory( "TextFieldLinkedCIO", new TextFieldLinkedCIOFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public TextFieldLinkedCIO( Appliance appl, ApplianceObject applObj ) {

	super( appl, applObj, null );

	m_Widget = new TextField( this );

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

	((TextField)m_Widget).addTextListener( new TextListener() {
		
		public void textValueChanged( TextEvent e ) {

		    String stateText = (String)((ApplianceState)m_ApplObj).m_Type.getValueSpace().getValue().toString();
		    String fieldText = ((TextField)e.getSource()).getText();

		    if ( stateText.equals( fieldText ) ) return;

		    Message msg = new Message.StateChangeRequest( TextFieldLinkedCIO.this.m_ApplObj.m_sName, fieldText );

		    try { TextFieldLinkedCIO.this.m_Appliance.m_pConnection.send( msg ); }
		    catch( Throwable t ) { }
		}
	    });

	refreshDisplay();
    }


    //**************************
    // Protected Methods
    //**************************

    protected void refreshDisplay() {

	ApplianceState s = (ApplianceState)m_ApplObj;

	String sCurrentText = ((TextField)m_Widget).getText();
	String sValueText = s.m_Type.getValueSpace().getValue().toString();
	
	// BUG:JWN:Partially fixed problem where cursor moves
	// irritatingly in the TextField on state updates (because the
	// state updates continuously while you type).  This is only a
	// partial fix, because there is no guarantee that the state
	// change rebounds will arrive faster than the user types.
	// :-(  The curse of I/O interaction objects, I guess...

	if (! sCurrentText.equals( sValueText ) )
	    ((TextField)m_Widget).setText( s.m_Type.getValueSpace().getValue().toString() );
    }
    

    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() { }

    public boolean hasLabel() { return true; }

    public ConcreteInteractionObject getLabelCIO() { 
	
	if ( m_ApplObj.m_Labels != null )
	    return new LabelCIO( m_ApplObj.m_Labels ); 

	return null;
    }

    public void addNotify() {
	
	refreshDisplay();
    }    
}
