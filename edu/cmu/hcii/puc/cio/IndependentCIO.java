/**
 * IndependentCIO.java
 *
 * A sub-class of ConcreteInteractionObject.
 *
 * This sub-class represents a CIO that is not associated with a state
 * or command.  Typically this would mean that the CIO is used for an
 * organizational purpose (e.g. opening a dialog box or a tab control
 * that switches between panes).
 *
 * Note that this class is used for symmetry, and may be the basis for
 * real functionality in the future.
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.Component;
import java.lang.*;


// Class Definition

public abstract class IndependentCIO extends ConcreteInteractionObject {

    //**************************
    // Member Variables
    //**************************


    //**************************
    // Constructor
    //**************************

    protected IndependentCIO( Component widget ) {

	super( widget );
    }


    //**************************
    // Member Methods
    //**************************

    // none
}
