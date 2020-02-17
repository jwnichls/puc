/**
 * TextField.java
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

public class TextField extends java.awt.TextField {

    //**************************
    // Member Variables
    //**************************

    ConcreteInteractionObject m_pCIO;


    //**************************
    // Constructor
    //**************************


    public TextField( ConcreteInteractionObject pCIO, String sText ) {

	super( sText );

	m_pCIO = pCIO;
    }

    public TextField( ConcreteInteractionObject pCIO ) {

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
