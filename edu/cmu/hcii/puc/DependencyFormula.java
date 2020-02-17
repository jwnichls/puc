/**
 * DependencyFormula.java
 * 
 * Represents a logic formula that involves Dependency objects.  Two
 * lists are stored internally, one for further formulas and one for
 * Dependency objects.
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

public abstract class DependencyFormula extends Object {

    //**************************
    // Member Variables
    //**************************
    
    protected Vector m_vDependencies;
    protected Vector m_vFormulas;


    //**************************
    // Constructor
    //**************************
    
    public DependencyFormula() {

	m_vDependencies = new Vector();
	m_vFormulas = new Vector();
    }


    //**************************
    // Public Methods
    //**************************

    public void addFormula( DependencyFormula df ) {
	if ( df == null ) System.out.println( "tried to add null formula" );

	if ( df != null )
	    m_vFormulas.addElement( df );
    }

    public void addDependency( Dependency d ) {
	if ( d != null )
	    m_vDependencies.addElement( d );
    }

    public Enumeration getFormulas() {
	return m_vFormulas.elements();
    }

    public Enumeration getDependencies() {
	return m_vDependencies.elements();
    }

    public boolean isEmpty() {
	return ( m_vFormulas.size() == 0 ) && ( m_vDependencies.size() == 0 );
    }

    public String toString() {

	String s = "( ";

	Enumeration e = getDependencies();
	while( e.hasMoreElements() ) {
	    s += e.nextElement().toString() + " ";
	}

	e = getFormulas();
	while( e.hasMoreElements() ) {
	    s += e.nextElement().toString() + " ";
	}

	s += ")";

	return s;
    }


    //**************************
    // Abstract Methods
    //**************************

    public abstract boolean isMutuallyExclusive( DependencyFormula df, ApplianceState exclState );

    public abstract DependencyFormula simplify();

    public abstract boolean isSatisfied();
}
