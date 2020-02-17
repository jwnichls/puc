/**
 * ScrollPane.java
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

import java.awt.LayoutManager;

import java.lang.*;

import edu.cmu.hcii.puc.cio.ConcreteInteractionObject;


// Class Definition

public class ScrollPane extends java.awt.ScrollPane {

    //**************************
    // Member Variables
    //**************************

    ConcreteInteractionObject m_pCIO;


    //**************************
    // Constructor
    //**************************


    public ScrollPane( ConcreteInteractionObject pCIO, int nDisplayPolicy ) {

	super( nDisplayPolicy );

	m_pCIO = pCIO;
    }

    public ScrollPane( ConcreteInteractionObject pCIO ) {

	m_pCIO = pCIO;
    }


    //**************************
    // Member Methods
    //**************************

    public void addNotify() {

	super.addNotify();

	m_pCIO.addNotify();
    }
}
