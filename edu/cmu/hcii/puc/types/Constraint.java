/**
 * Constraint.java
 *
 * The Constraint interface is implemented by other objects to create
 * ValueSpace objects that are constrained by other ValueSpaces.  This
 * enables a very basic constraint system.  
 *
 * These objects require "validation" after parsing, because the Value
 * state name is only known as a String at the end of the process.
 * One of the later phases of the parser.SpecParser resolves these
 * Strings to ApplianceState objects.  The getter-methods will throw
 * a NullPointerException if they are called before validation.
 *
 * Revision History
 * ----------------
 * 02/07/2002: (JWN) Created file.  
 */

// Package Definition

package edu.cmu.hcii.puc.types;


// Import Declarations

import java.lang.*;

import java.util.Hashtable;

import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.StateListener;


// Class Definition

public interface Constraint {

    //**************************
    // Interface Methods
    //**************************
    
    public boolean isValid();
    public ApplianceState getValueState();
    public ApplianceObject getConstrainedObject();

    public boolean resolveState( Hashtable pStates );
}
