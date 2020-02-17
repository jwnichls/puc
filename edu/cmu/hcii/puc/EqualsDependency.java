/**
 * EqualsDependency.java
 * 
 * Implements the Dependency interface.  Represents a dependency in
 * which the the dependent object is enabled when another state is
 * equal to a particular value.
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


// Class Definition

public class EqualsDependency extends Dependency {

    //**************************
    // Constructor
    //**************************

    public EqualsDependency( ApplianceState s, String val ) {

	super( s, val );
    }

    public EqualsDependency( String state_name, String val ) {

	super( state_name, val );
    }


    //**************************
    // Public Methods
    //**************************

    public int compare( Dependency d ) {
	
	if (! d.getStateName().equals( m_sStateName ) )
	    return Dependency.COMPARE_DISJOINT;

	return d.compareEquals( this );
    }

    public Dependency merge( Dependency d ) {

	int r = d.compareEquals( this );

	switch( r ) {
	case Dependency.COMPARE_SAME:
	    return d;
	case Dependency.COMPARE_DISJOINT:
	    return null;
	case Dependency.COMPARE_OVERLAP:
	    return d;
	}

	return null;
    }

    public boolean isSatisfied() {

	return m_pValue.compareValues( m_pState.m_Type.getValueSpace() ) == 0;
    }

    public String toString() {
	
	return "(" + m_sStateName + " = " + m_sValue + ")";
    }


    //**************************
    // Protected Methods
    //**************************

    protected int compareEquals( EqualsDependency d ) {

	if ( m_pValue.compareValues( d.m_pValue ) == 0 )
	    return Dependency.COMPARE_SAME;

	return Dependency.COMPARE_DISJOINT;
    }

    protected int compareGreaterThan( GreaterThanDependency d ) {

	if ( m_pValue.compareValues( d.m_pValue ) == -1 )
	    return Dependency.COMPARE_OVERLAP;

	return Dependency.COMPARE_DISJOINT;
    }

    protected int compareLessThan( LessThanDependency d ) {

	if ( m_pValue.compareValues( d.m_pValue ) == 1 )
	    return Dependency.COMPARE_OVERLAP;

	return Dependency.COMPARE_DISJOINT;
    }
}
