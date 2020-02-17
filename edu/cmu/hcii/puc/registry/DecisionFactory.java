/**
 * DecisionFactory.java
 *
 * The DecisionFactory is an object used for dynamically loading
 * Decisions.  It is a part of the Factory design pattern.
 *
 * Revision History 
 * ---------------- 
 * 02/02/2002: (JWN) Created file.  
 */

// Package Definition

package edu.cmu.hcii.puc.registry;


// Import Declarations

import java.lang.*;

import java.util.Hashtable;


// Class Definition

public interface DecisionFactory {

    //**************************
    // Member Methods
    //**************************

    public Decision createDecision( Hashtable pChoices );
}


