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

public class WholeSetDependency extends Dependency {

    //**************************
    // Constructor
    //**************************

    protected WholeSetDependency( ApplianceState s ) {

	super( s );
    }


    //**************************
    // Public Methods
    //**************************

    public int compare( Dependency d ) {

	if (! d.getStateName().equals( m_sStateName ) )
	    return Dependency.COMPARE_DISJOINT;

	if ( d instanceof WholeSetDependency ) 
	    return Dependency.COMPARE_SAME;

	return Dependency.COMPARE_OVERLAP;
    } 

    public Dependency merge( Dependency d ) {

	if ( d.compare( this ) == Dependency.COMPARE_DISJOINT )
	    return null;
	else
	    return this;
    }

    public boolean isSatisfied() {

	return true;
    }

    public String toString() {
	
	return "(" + m_sStateName + " = Anything)";
    }


    //**************************
    // Protected Methods
    //**************************

    protected int compareEquals( EqualsDependency d ) {

	return Dependency.COMPARE_OVERLAP;
    }

    protected int compareGreaterThan( GreaterThanDependency d ) {

	return Dependency.COMPARE_OVERLAP;
    }
					       
    protected int compareLessThan( LessThanDependency d ) {

	return Dependency.COMPARE_OVERLAP;
    }
}
