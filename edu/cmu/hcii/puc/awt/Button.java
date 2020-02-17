/**
 * Button.java
 * 
 * An sub-class of a java.awt widget, so that the CIO objects can
 * receive the addNotify message.
 * 
 * Revision History:
 * -----------------
 * 07/07/2001: (JWN) Created file and added parsing variables.
 *
 */

// Package Definition

package edu.cmu.hcii.puc.awt;


// Import Declarations

import java.lang.*;

import edu.cmu.hcii.puc.cio.ConcreteInteractionObject;


// Class Definition

public class Button extends java.awt.Button {

    //**************************
    // Member Variables
    //**************************

    ConcreteInteractionObject m_pCIO;


    //**************************
    // Constructor
    //**************************


    public Button( ConcreteInteractionObject pCIO, String sLabel ) {

	super( sLabel );

	m_pCIO = pCIO;
    }

    public Button( ConcreteInteractionObject pCIO ) {

	this( pCIO, "" );
    }


    //**************************
    // Member Methods
    //**************************

    public void addNotify() {

	super.addNotify();

	m_pCIO.addNotify();
    }
}
