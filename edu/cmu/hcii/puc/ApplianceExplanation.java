/**
 * ApplianceExplanation.java
 * 
 * An object used for including some explanatory text in the user
 * interface that is not necessarily associated with a state or
 * command. 
 * 
 * Revision History:
 * -----------------
 * 02/07/2002: (JWN) Created file.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.lang.*;


// Class Definition

public class ApplianceExplanation extends ApplianceObject {

    //**************************
    // Constants
    //**************************

    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public ApplianceExplanation() { }


    //**************************
    // Member Methods
    //**************************

    public boolean isExplanation() { return true; }
}

