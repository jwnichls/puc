/**
 * OR.java
 * 
 * The OR dependency formula.
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

public class OR extends DependencyFormula {

    //**************************
    // Public Methods
    //**************************

    public boolean isMutuallyExclusive( DependencyFormula df, ApplianceState exclState ) {
	
	return false;
    }

    public DependencyFormula simplify() {

	if ( this.isEmpty() ) return null;

	DependencyFormula pNewFormula = new OR();
	
	Enumeration e = m_vFormulas.elements();

	while( e.hasMoreElements() ) {
	    DependencyFormula df = (DependencyFormula)e.nextElement();

	    df = df.simplify();

	    if ( df instanceof OR ) {
		Enumeration e2 = df.getDependencies();

		while( e2.hasMoreElements() ) {
		    pNewFormula.addDependency( (Dependency)e2.nextElement() );
		}

		// BUG!: Why not copy formulas too?
	    }
	    else
		pNewFormula.addFormula( df );
	}

	e = m_vDependencies.elements();

	while( e.hasMoreElements() )
	    pNewFormula.addDependency( (Dependency)e.nextElement() );

	return pNewFormula;
    }

    public boolean isSatisfied() {

	System.out.println( "Evaluating OR clause" );
	
	boolean bTotal = false;

	for( int i = 0; i < m_vDependencies.size(); i++ )
	    bTotal = bTotal || ((Dependency)m_vDependencies.elementAt( i )).isSatisfied();

	if ( bTotal ) return bTotal;

	for( int i = 0; i < m_vFormulas.size(); i++ )
	    bTotal = bTotal || ((DependencyFormula)m_vFormulas.elementAt( i )).isSatisfied();

	return bTotal;
    }

    public String toString() {
	
	return "OR" + super.toString();
    }
}
