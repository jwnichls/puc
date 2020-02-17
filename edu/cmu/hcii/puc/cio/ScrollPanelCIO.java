/**
 * ScrollPanelCIO.java
 *
 * A sub-class of PanelCIO.
 *
 * Revision History 
 * ---------------- 
 * 6/21/2002: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Insets;

import edu.cmu.hcii.puc.PUC;

import edu.cmu.hcii.puc.awt.ScrollPane;

import java.lang.*;


// Class Definition

public class ScrollPanelCIO extends PanelCIO {

    //**************************
    // Member Variables
    //**************************


    //**************************
    // Constructor
    //**************************

    public ScrollPanelCIO() {
	super();

	m_Widget = new ScrollPane( this );

	//((Container)m_Widget).setLayout( new GridLayout( 0, 1, 5, 5 ) );
	((Container)m_Widget).setLayout( new RowLayout( 3 ) );
    }


    //**************************
    // Member Methods
    //**************************

    public void addNotify() {

	super.addNotify();
    }
}
