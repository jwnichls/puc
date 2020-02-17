/**
 * RadioStationCIO.java
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

import java.awt.Button;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.ObjectListener;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.TextField;

import edu.cmu.hcii.puc.registry.WidgetRegistry;

import com.maya.puc.common.Message;


// Class Definition

public class RadioStationCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static 
    //**************************

    static class RadioStationCIOFactory implements CIOFactory {

	public ConcreteInteractionObject createCIO( Appliance a,
						    ApplianceObject ao ) {

	    return new RadioStationCIO( a, ao );
	}
    }

    static {
	// register the factory with the WidgetRegistry
	WidgetRegistry.addCIOFactory( "RadioStationCIO", new RadioStationCIOFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    protected Label m_pLabel;
    protected TextField m_pTextField;
    protected Button m_pRButton;
    protected Button m_pLButton;

    protected boolean m_bBand; // false = AM, true = FM


    //**************************
    // Constructor
    //**************************

    public RadioStationCIO( Appliance appl, ApplianceObject applObj ) {

	super( appl, applObj, null );

	m_bBand = ((ApplianceState)m_ApplObj).m_Type.getValueSpace().getName().endsWith( "fm" );

	m_Widget = new Panel();

	m_pLabel = new Label( "" );
	m_pTextField = new TextField( this, "107.7" );
	m_pRButton = new Button( ">" );
	m_pLButton = new Button( "<" );

	GridBagLayout pLayout = new GridBagLayout();
	GridBagConstraints pC = new GridBagConstraints();
	((Panel)m_Widget).setLayout( pLayout );

	pC.fill = GridBagConstraints.HORIZONTAL;
	pC.weightx = 1.0;
	pC.ipadx = 3;
	pLayout.setConstraints( m_pLButton, pC );
	pLayout.setConstraints( m_pRButton, pC );
	((Panel)m_Widget).add( m_pLButton );

	pC.gridwidth = 2;
	pC.weightx = 2.0;
	pLayout.setConstraints( m_pTextField, pC );
	((Panel)m_Widget).add( m_pTextField );

	((Panel)m_Widget).add( m_pRButton );

	if ( ((ApplianceState)m_ApplObj).m_bReadOnly ) {
	    m_pLButton.setEnabled( false );
	    m_pRButton.setEnabled( false );
	}

	((ApplianceState)m_ApplObj).addStateListener( new StateListener() {
		
		public void enableChanged( ApplianceObject obj ) { 
		    m_pLButton.setEnabled( m_ApplObj.isEnabled() );
		    m_pRButton.setEnabled( m_ApplObj.isEnabled() );
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

	m_pLButton.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
		    goPrevStation();
		}
	    });

	m_pRButton.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
		    goNextStation();
		}
	    });

	m_pTextField.addTextListener( new TextListener() {

		public void textValueChanged( TextEvent e ) {

		    String stateText = (String)((ApplianceState)m_ApplObj).m_Type.getValueSpace().getValue().toString();
		    String fieldText = ((TextField)e.getSource()).getText();

		    if ( stateText.equals( fieldText ) ) return;

		    goToStation( fieldText );
		}
	    });


	refreshDisplay();
    }


    //**************************
    // Protected Methods
    //**************************

    protected float getStation() {

	try {
	    return new Float((String)((ApplianceState)m_ApplObj).m_Type.getValueSpace().getValue()).floatValue();
	}
	catch( Exception e ) {
	    if ( m_bBand ) return 87.5f;
	    else return 530f;
	}
    }

    protected void goNextStation() {

	float f = getStation();
	String s;
	
	if ( m_bBand ) {
	    f += 0.1f;
	    if ( f > 108f )
		f = 87.5f;
	    s = ((double)Math.round(f*10) / 10) + "";
	}
	else {
	    f += 10f;
	    if ( f > 1710f )
		f = 530f;
	    s = Math.round( f ) + "";
	}

	Message msg = new Message.StateChangeRequest( m_ApplObj.m_sName,
						      s );
	
	try { m_Appliance.m_pConnection.send( msg ); }
	catch( Throwable t ) { }
	
	refreshDisplay();	
    }
    
    protected void goPrevStation() {

	float f = getStation();
	String s;

	if ( m_bBand ) {
	    f -= 0.1f;
	    if ( f < 87.5f )
		f = 108f;
	    s = ((double)Math.round(f*10) / 10) + "";
	}
	else {
	    f -= 10f;
	    if ( f < 530f )
		f = 1710f;
	    s = Math.round( f ) + "";
	}

	Message msg = new Message.StateChangeRequest( m_ApplObj.m_sName,
						      s );
	
	try { m_Appliance.m_pConnection.send( msg ); }
	catch( Throwable t ) { }
	
	refreshDisplay();
    }

    protected void goToStation( String sNewStation) {

	if (! isGoodStation( sNewStation ) )
	    return;

	Message msg = new Message.StateChangeRequest( m_ApplObj.m_sName,
						      sNewStation );
	
	try { m_Appliance.m_pConnection.send( msg ); }
	catch( Throwable t ) { }
	
	refreshDisplay();
    }

    protected boolean isGoodStation( String s ) {
	try {
	    return isGoodStation( new Float( s ).floatValue() );
	}
	catch( Exception e ) {

	    return false;
	}
    }

    protected boolean isGoodStation( float d ) {

	if ( m_bBand ) { // band == FM

	    return !( d > 108f || d < 87.5f );
	}
	else { // band == AM
	    
	    if ( d > 1710 || d < 530 )
		return false;

	    if ( (int)(d*10) == (Math.round( d )*10) )
		return true;

	    return false;
	}
    }

    protected void refreshDisplay() {

	String lbl;
	ApplianceState as = (ApplianceState)m_ApplObj;

	m_pTextField.setText( as.m_Type.getValueSpace().getValue().toString() );
    }
    

    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() { }

    public boolean prefersFullWidth() { return false; }

    public void addNotify() {

	refreshDisplay();
    }    
}
