/**
 * Panel.java
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

import java.awt.Graphics;
import java.awt.LayoutManager;

import java.lang.*;

import edu.cmu.hcii.puc.cio.ConcreteInteractionObject;


// Class Definition

public class Panel extends java.awt.Panel {

    //**************************
    // Member Variables
    //**************************

    ConcreteInteractionObject m_pCIO;


    //**************************
    // Constructor
    //**************************


    public Panel( ConcreteInteractionObject pCIO, LayoutManager pLayout ) {

	super( pLayout );

	m_pCIO = pCIO;
    }

    public Panel( ConcreteInteractionObject pCIO ) {

	m_pCIO = pCIO;
    }


    //**************************
    // Member Methods
    //**************************

    public void update( Graphics g ) {

	g.clearRect( 0, 0, getSize().width, getSize().height );

	super.update( g );
    }

    public void addNotify() {

	super.addNotify();

	m_pCIO.addNotify();
    }
}
