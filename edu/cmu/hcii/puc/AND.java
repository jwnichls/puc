/**
 * AND.java
 * 
 * The AND dependency formula.
 *
 * Revision History:
 * -----------------
 * 07/07/2001: (JWN) Created file.
 * */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.util.Enumeration;
import java.util.Vector;


// Class Definition

public class AND extends DependencyFormula {

    //**************************
    // Public Methods
    //**************************

    public boolean isMutuallyExclusive( DependencyFormula df, ApplianceState exclState ) {
	
	return false;
    }

    public DependencyFormula simplify() {

	if ( this.isEmpty() ) return null;

	DependencyFormula pNewFormula = new AND();
	
	Enumeration e = m_vFormulas.elements();

	while( e.hasMoreElements() ) {
	    DependencyFormula df = (DependencyFormula)e.nextElement();

	    df = df.simplify();

	    if ( df instanceof AND ) {
		Enumeration e2 = df.getDependencies();

		while( e2.hasMoreElements() ) {
		    pNewFormula.addDependency( (Dependency)e2.nextElement() );
		}

		// BUG!:  Why not copy the formulas too?
	    }
	    else if ( df != null ) 
		pNewFormula.addFormula( df );
	}

	e = m_vDependencies.elements();

	while( e.hasMoreElements() )
	    pNewFormula.addDependency( (Dependency)e.nextElement() );

	return pNewFormula;
    }

    public boolean isSatisfied() {
	
	System.out.println( "Evaluating AND clause!" );
	
	boolean bTotal = true;

	for( int i = 0; i < m_vDependencies.size(); i++ )
	    bTotal = bTotal && ((Dependency)m_vDependencies.elementAt( i )).isSatisfied();

	if (! bTotal ) return bTotal;

	for( int i = 0; i < m_vFormulas.size(); i++ )
	    bTotal = bTotal && ((DependencyFormula)m_vFormulas.elementAt( i )).isSatisfied();

	return bTotal;
    }

    public String toString() {
	
	return "AND" + super.toString();
    }
}
