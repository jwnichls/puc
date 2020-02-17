/**
 * GreaterThanDependency.java
 * 
 * Implements the Dependency interface.  Represents a dependency in
 * which the the dependent object is enabled when another state is
 * greater than a particular value.
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

import edu.cmu.hcii.puc.types.NumberSpace;


// Class Definition

public class GreaterThanDependency extends Dependency {

    //**************************
    // Constructor
    //**************************

    public GreaterThanDependency( ApplianceState s, String val ) {

	super( s, val );
    }

    public GreaterThanDependency( String state_name, String val ) {

	super( state_name, val );
    }


    //**************************
    // Public Methods
    //**************************

    public int compare( Dependency d ) {

	if (! d.getStateName().equals( m_sStateName ) )
	    return Dependency.COMPARE_DISJOINT;

	return d.compareGreaterThan( this );
    }

    public Dependency merge( Dependency d ) {

	int r = d.compareGreaterThan( this );

	switch( r ) {
	case Dependency.COMPARE_SAME:
	    return d;
	case Dependency.COMPARE_DISJOINT:
	    return null;
	case Dependency.COMPARE_OVERLAP:
	    if ( d instanceof EqualsDependency )
		return this;
	    else if ( d instanceof GreaterThanDependency ) {
		if ( m_pValue.compareValues( d.m_pValue ) == 1 )
		    return this;
		else
		    return d;
	    }
	    else if ( d instanceof LessThanDependency ) {
		return new WholeSetDependency( m_pState );
	    }
	}

	return null;
    }

    public boolean isSatisfied() {

	return m_pValue.compareValues( m_pState.m_Type.getValueSpace() ) == 1;
    }

    public String toString() {
	
	return "(" + m_sStateName + " > " + m_sValue + ")";
    }


    //**************************
    // Protected Methods
    //**************************

    public boolean resolveState( Hashtable h ) 
	throws edu.cmu.hcii.puc.types.SpaceMismatchException
    {
	boolean r = super.resolveState( h );

	if ( r )
	    if (! ( m_pValue instanceof NumberSpace ) )  {
		throw new edu.cmu.hcii.puc.types.SpaceMismatchException( "GreaterThanDependencies cannot be associated with non-Number value spaces!" );
	    }

	return r;
    }

    protected int compareEquals( EqualsDependency d ) {

	return d.compareGreaterThan( this );
    }

    protected int compareGreaterThan( GreaterThanDependency d ) {

	if ( m_pValue.compareValues( d.m_pValue ) == 0 )
	    return Dependency.COMPARE_SAME;

	return Dependency.COMPARE_OVERLAP;
    }

    protected int compareLessThan( LessThanDependency d ) {

	if ( m_pValue.compareValues( d.m_pValue ) == 1 )
	    return Dependency.COMPARE_OVERLAP;

	return Dependency.COMPARE_DISJOINT;
    }
}
