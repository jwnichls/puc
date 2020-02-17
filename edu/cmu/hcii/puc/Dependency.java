/**
 * Dependency.java
 * 
 * Represents a dependency that an ApplianceObject that may have on
 * another ApplianceState.  This is a class that is sub-classed by
 * several different kinds of dependencies.
 *
 * Revision History:
 * -----------------
 * 07/07/2001: (JWN) Created file.
 * 
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.lang.*;

import java.util.Hashtable;

import edu.cmu.hcii.puc.types.ValueSpace;


// Interface Definition

public abstract class Dependency {

    //**************************
    // Constants
    //**************************
    
    public static final int COMPARE_SAME     = 2; 
    public static final int COMPARE_OVERLAP  = 1;
    public static final int COMPARE_DISJOINT = 0;   


    //**************************
    // Member Variables
    //**************************

    protected String m_sStateName;
    protected String m_sValue;
    protected ValueSpace m_pValue;

    protected ApplianceState m_pState;
    protected boolean m_bStateResolved;


    //**************************
    // Constructor
    //**************************

    protected Dependency( String sn, String value ) {

	m_sStateName = sn;
	m_sValue = value;
	m_bStateResolved = false;
    }

    protected Dependency( ApplianceState s, String value ) {

	m_pState = s;
	m_sStateName = s.m_sName;
	m_sValue = value;

	try {
	    m_pValue = (ValueSpace)m_pState.m_Type.getValueSpace().clone();
	    m_pValue.setValue( m_sValue );
	}
	catch(Throwable t) {
	    throw new ClassCastException( "Dependency created with a bad value." );
	}

	m_bStateResolved = true;
    }

    /**
     * This constructor is for use only by the WholeSetDependency
     * class of objects.
     */
    protected Dependency( ApplianceState s ) {
	m_pState = s;
	m_sStateName = s.m_sName;

	m_sValue = null;
	m_pValue = null;

	m_bStateResolved = true;
    }


    //**************************
    // Public Methods
    //**************************

    public boolean resolveState( Hashtable h ) 
	throws edu.cmu.hcii.puc.types.SpaceMismatchException
    {

	m_pState = (ApplianceState)h.get( m_sStateName );

	m_bStateResolved = (m_pState != null);

	if ( m_bStateResolved ) {
	    m_pValue = (ValueSpace)m_pState.m_Type.getValueSpace().clone();
	    m_pValue.setValue( m_sValue );
	}

	return m_bStateResolved;
    }

    public String getStateName() {
	return m_sStateName;
    }
    
    public ApplianceState getState() {
	return m_pState;
    }

    public String getValueString() {

	return m_sValue;
    }

    public ValueSpace getValueSpace() {

	if (! m_bStateResolved ) return null;

	return m_pValue;
    }

    public abstract int compare( Dependency d ); 

    public abstract Dependency merge( Dependency d );

    public abstract boolean isSatisfied();


    //**************************
    // Protected Methods
    //**************************

    protected abstract int compareEquals( EqualsDependency d );
    protected abstract int compareGreaterThan( GreaterThanDependency d );
    protected abstract int compareLessThan( LessThanDependency d );
}
