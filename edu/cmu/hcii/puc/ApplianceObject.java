/**
 * ApplianceObject.java
 *
 * An object for representing the objects of an appliance.  This is
 * either a state variable, a command, or an explanation.
 *
 * Revision History:
 * -----------------
 * 07/07/2001: (JWN) Created file and added parsing variables.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.lang.*;

import java.util.Enumeration;
import java.util.Vector;


// Class Definition

public abstract class ApplianceObject extends Object {

    //**************************
    // Constants
    //**************************

    //**************************
    // Member Variables
    //**************************

    // Object properties
    public String m_sName;
    public int m_nPriority;
    protected boolean m_bEnabled;

    // Label Information
    public LabelLibrary m_Labels;

    // Listeners
    public Vector m_vObjListeners;

    // Dependency Information
    public DependencyFormula m_pDependencies;


    //**************************
    // Constructor
    //**************************

    public ApplianceObject() {

	m_vObjListeners = new Vector();

	m_nPriority = 5; // FIXME:JWN: Arbitrary default
	m_bEnabled = true;
    }


    //**************************
    // Member Methods
    //**************************

    public boolean isState() { return false; }

    public boolean isExplanation() { return false; }

    public void addObjListener( ObjectListener sl ) {

	m_vObjListeners.addElement( sl );
    }

    public boolean removeObjListener( ObjectListener sl ) {

	return m_vObjListeners.removeElement( sl );
    }

    public boolean isEnabled() {
	return m_bEnabled;
    }

    public void setEnabled( boolean enabled ) {

	if ( enabled != m_bEnabled ) {

	    m_bEnabled = enabled;

	    Enumeration e = m_vObjListeners.elements();
	    while( e.hasMoreElements() ) {
	    	((ObjectListener)e.nextElement()).enableChanged( this );
	    }
	}
    }

    public void evalDependencies() {

	setEnabled( m_pDependencies.isSatisfied() );
    }

    public void labelChanged() {

	Enumeration e = m_vObjListeners.elements();
	while( e.hasMoreElements() ) {
	    ((ObjectListener)e.nextElement()).labelChanged( this );
	}
    }
}

