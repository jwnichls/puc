/**
 * ReferenceValue.java
 *
 * The ReferenceValue class is used to create ValueSpace objects that
 * are constrained by other ValueSpaces.  This enables a very basic
 * constraint system.  The ReferenceValue is a sub-class of
 * java.lang.Number in order to enable transparent inclusion of a
 * constraint where a Number would normally go.
 *
 * These objects require "validation" after parsing, because the Value
 * state name is only known as a String at the end of the process.
 * One of the later phases of the parser.SpecParser resolves these
 * Strings to ApplianceState objects.  The java.lang.Number methods
 * cannot be used until after the class is valid.  A
 * NullPointerException is thrown if these methods are called
 * before validation.
 *
 * This class should be renamed to NumberConstraint at some point.
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

public class ReferenceValue extends Number implements Constraint {

    //**************************
    // Member Variables
    //**************************

    protected String m_sValueState;

    protected ApplianceState m_pConstrainedState;
    protected ApplianceState m_pValueState;

    protected boolean m_bValid;


    //**************************
    // Constructor
    //**************************

    public ReferenceValue( ApplianceState pConstrainedState, 
			   String sValueState ) {

	m_bValid = false;

	m_pConstrainedState = pConstrainedState;
	m_sValueState = sValueState;
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
	
	return m_pConstrainedState;
    }

    public boolean resolveState( Hashtable pStates ) {

	m_pValueState = (ApplianceState)pStates.get( m_sValueState );
	
	m_bValid = m_pValueState != null;

	if ( m_bValid ) {

	    // add listener

	    m_pValueState.addStateListener( new StateListener() {

		    public void enableChanged( ApplianceObject obj ) { }
		    public void labelChanged( ApplianceObject obj ) { }

		    public void typeChanged( ApplianceState state ) { }
		    public void valueChanged( ApplianceState state ) { 
			m_pConstrainedState.typeChanged();
		    }
		});
	}

	return m_bValid;
    }

    //**************************
    // java.lang.Number Methods
    //**************************

    public byte byteValue() {

	return ((Number)m_pValueState.m_Type.getValueSpace().getValue()).byteValue();
    }

    public double doubleValue() {

	return ((Number)m_pValueState.m_Type.getValueSpace().getValue()).doubleValue();
    }

    public float floatValue() {

	return ((Number)m_pValueState.m_Type.getValueSpace().getValue()).floatValue();
    }

    public int intValue() {

	return ((Number)m_pValueState.m_Type.getValueSpace().getValue()).intValue();
    }

    public long longValue() {

	return ((Number)m_pValueState.m_Type.getValueSpace().getValue()).longValue();
    }

    public short shortValue() {

	return ((Number)m_pValueState.m_Type.getValueSpace().getValue()).shortValue();
    }
}
