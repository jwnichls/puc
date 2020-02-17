/**
 * PanelCIO.java
 *
 * A sub-class of ContainerCIO.
 *
 * A generic "hacked" implementation of the Panel CIO to check if CIO
 * system is actually working. 
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Insets;

import edu.cmu.hcii.puc.PUC;

import edu.cmu.hcii.puc.awt.Panel;

import java.lang.*;


// Class Definition

public class PanelCIO extends ContainerCIO {

    //**************************
    // Member Variables
    //**************************


    //**************************
    // Constructor
    //**************************

    public PanelCIO() {
	super( null );

	m_Widget = new Panel( this );

	//((Panel)m_Widget).setLayout( new GridLayout( 0, 1, 5, 5 ) );
	((Panel)m_Widget).setLayout( new RowLayout( 3 ) );
    }

    public PanelCIO( Panel pWidget ) {
	super( pWidget );

	pWidget.setLayout( new RowLayout( 3 ) );
    }


    //**************************
    // Member Methods
    //**************************

    public void addNotify() {

	super.addNotify();
    }
}
