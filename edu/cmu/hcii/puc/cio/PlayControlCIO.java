/**
 * PlayControlCIO.java
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.ObjectListener;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.Button;

import edu.cmu.hcii.puc.registry.WidgetRegistry;

import com.maya.puc.common.Message;


// Class Definition

public class PlayControlCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static 
    //**************************

    static class PlayControlCIOFactory implements CIOFactory {

	public ConcreteInteractionObject createCIO( Appliance a,
						    ApplianceObject ao ) {

	    return new PlayControlCIO( a, ao );
	}
    }

    static {
	// register the factory with the WidgetRegistry
	WidgetRegistry.addCIOFactory( "PlayControlCIO", new PlayControlCIOFactory() );
    }


    //**************************
    // Constants
    //**************************

    protected final static int STOPPED = 1;
    protected final static int PLAYING = 2;
    protected final static int PAUSED  = 3;


    //**************************
    // Member Variables
    //**************************

    protected Label m_pLabel;
    protected Button m_pPlayButton;
    protected java.awt.Button m_pStopButton;
    protected java.awt.Button m_pPauseButton;


    //**************************
    // Constructor
    //**************************

    public PlayControlCIO( Appliance appl, ApplianceObject applObj ) {

	super( appl, applObj, null );

	m_Widget = new Panel();

	m_pLabel = new Label( "" );
	m_pPlayButton = new Button( this, ">" );
	m_pStopButton = new java.awt.Button( "[]" );
	m_pPauseButton = new java.awt.Button( "\"" );

	GridBagLayout pLayout = new GridBagLayout();
	GridBagConstraints pC = new GridBagConstraints();
	((Panel)m_Widget).setLayout( pLayout );

	pC.fill = GridBagConstraints.HORIZONTAL;
	pC.weightx = 1.0;
	pLayout.setConstraints( m_pStopButton, pC );
	pLayout.setConstraints( m_pPauseButton, pC );

	pC.gridwidth = 2;
	pC.weightx = 2.0;
	pLayout.setConstraints( m_pPlayButton, pC );
	((Panel)m_Widget).add( m_pPlayButton );

	((Panel)m_Widget).add( m_pStopButton );
	((Panel)m_Widget).add( m_pPauseButton );

	m_pPlayButton.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
		    Message msg;
		    
		    msg = new Message.StateChangeRequest( m_ApplObj.m_sName,
							  PLAYING + "" );

		    try { m_Appliance.m_pConnection.send( msg ); }
		    catch( Throwable t ) { }

		    refreshDisplay();
		}
	    });

	m_pStopButton.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
		    Message msg;
		    
		    msg = new Message.StateChangeRequest( m_ApplObj.m_sName,
							  STOPPED + "" );

		    try { m_Appliance.m_pConnection.send( msg ); }
		    catch( Throwable t ) { }

		    refreshDisplay();
		}
	    });

	m_pPauseButton.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
		    Message msg;
		    
		    msg = new Message.StateChangeRequest( m_ApplObj.m_sName,
							  PAUSED + "" );

		    try { m_Appliance.m_pConnection.send( msg ); }
		    catch( Throwable t ) { }

		    refreshDisplay();
		}
	    });

	((ApplianceState)m_ApplObj).addStateListener( new StateListener() {
		
		public void enableChanged( ApplianceObject obj ) { 
		    setButtonState();
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

	// TODO:  add action handler code

	refreshDisplay();
    }


    //**************************
    // Protected Methods
    //**************************

    protected void setButtonState() {

	ApplianceState as = (ApplianceState)m_ApplObj;

	int nState;
	try {
	    nState = new Integer( (String)as.m_Type.getValueSpace().getValue() ).intValue();
	}
	catch( Exception e ) {
	    nState = 0;
	}

	boolean bEn = as.isEnabled() && !as.m_bReadOnly;

	m_pPlayButton.setEnabled( bEn && nState == STOPPED || nState == PAUSED );
	m_pStopButton.setEnabled( bEn && nState == PLAYING || nState == PAUSED );
	m_pPauseButton.setEnabled( bEn && nState == PLAYING );
    }

    protected void refreshDisplay() {

	setButtonState();
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
