/**
 * StringConstraint.java
 *
 * The StringConstraint class is used to create ValueSpace objects that
 * are constrained by other ValueSpaces.  This enables a very basic
 * constraint system.  
 *
 * These objects require "validation" after parsing, because the Value
 * state name is only known as a String at the end of the process.
 * One of the later phases of the parser.SpecParser resolves these
 * Strings to ApplianceState objects.  The java.lang.Boolean methods
 * cannot be used until after the class is valid.  A
 * NullPointerException is thrown if these methods are called
 * before validation.
 *
 * Revision History
 * ----------------
 * 02/03/2002: (JWN) Created file.  
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

public class StringConstraint implements Constraint,
                                         PUCString  {

    //**************************
    // Member Variables
    //**************************

    protected String m_sValueState;

    protected ApplianceObject m_pConstrainedObject;
    protected ApplianceState m_pValueState;

    protected boolean m_bValid;


    //**************************
    // Constructor
    //**************************

    public StringConstraint( ApplianceObject pConstrainedObject, 
			     String sValueState ) {

	m_bValid = false;

	m_sValueState = sValueState;
	m_pConstrainedObject = pConstrainedObject;
	m_pValueState = null;
    }


    //**************************
    // Member Methods
    //**************************
    
    public boolean isValid() { return m_bValid; }

    public ApplianceState getValueState() {
	
	return m_pValueState;
    }

    public ApplianceObject getConstrainedObject() {
	
	return m_pConstrainedObject;
    }

    public boolean resolveState( Hashtable pStates ) {

	m_pValueState = (ApplianceState)pStates.get( m_sValueState );
	
	m_bValid = 
	    m_pValueState != null && 
	    m_pValueState.m_Type.getValueSpace().getSpace() == ValueSpace.STRING_SPACE;

	if ( m_bValid ) {

	    // add listener

	    m_pValueState.addStateListener( new StateListener() {

		    public void enableChanged( ApplianceObject obj ) { }
		    public void labelChanged( ApplianceObject obj ) { }

		    public void typeChanged( ApplianceState state ) { }
		    public void valueChanged( ApplianceState state ) { 
			m_pConstrainedObject.labelChanged();
		    }
		});
	}

	return m_bValid;
    }

    //**************************
    // Member Methods
    //**************************

    public String toString() {

	return (String)m_pValueState.m_Type.getValueSpace().getValue();
    }    
}
