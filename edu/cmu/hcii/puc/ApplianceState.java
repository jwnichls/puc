/**
 * ApplianceState.java
 * 
 * An object for representing a state variable of an appliance.
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

public class ApplianceState extends ApplianceObject {

    //**************************
    // Constants
    //**************************

    //**************************
    // Member Variables
    //**************************
    
    // State properties
    public boolean m_bReadOnly;
    protected boolean m_bConstraintVariable;

    // Type information
    public PUCType m_Type;

    // Organization Attribute
    protected boolean m_bInternalController;

    // listeners
    protected Vector m_vStateListeners;

    // reverse dependencies
    protected Vector m_vReverseDeps;


    //**************************
    // Constructor
    //**************************

    public ApplianceState() {

	m_vStateListeners = new Vector();
	m_vReverseDeps = new Vector();

	m_bInternalController = false;
	m_bConstraintVariable = false;
    }


    //**************************
    // Member Methods
    //**************************

    public boolean isState() { return true; }

    public boolean isConstraintVariable() { return m_bConstraintVariable; }

    public void makeConstraintVariable() {
	
	m_bConstraintVariable = true;
    }

    public void setInternalController( boolean b ) {

	m_bInternalController = b;
    }

    public boolean getInternalController() {
	
	return m_bInternalController;
    }

    public void addReverseDependency( ApplianceObject ao ) {
	
	if (! m_vReverseDeps.contains( ao ) )
	    m_vReverseDeps.addElement( ao );
    }

    public int getReverseDependencyCount() {

	return m_vReverseDeps.size();
    }

    public Vector getReverseDeps() {

	return m_vReverseDeps;
    }

    public void addStateListener( StateListener sl ) {

	m_vStateListeners.addElement( sl );

	addObjListener( sl );
    }

    public boolean removeStateListener( StateListener sl ) {

	if ( m_vStateListeners.removeElement( sl ) ) {

	    return removeObjListener( sl );
	}

	return false;
    }

    public void setValue( String value ) {

	System.err.println( "Attempting to setValue( " + value + " ) of " + m_sName );

	try {
	    m_Type.getValueSpace().setValue( value );

	    System.out.println( "Setting value of " + m_sName + " to " + value );
	    
	    Enumeration en = m_vStateListeners.elements();
	    
	    while( en.hasMoreElements() ) {
		((StateListener)en.nextElement()).valueChanged( this );
	    }
	}
	catch( Throwable t ) {

	    Globals.printLog( "Attempt to set state " + m_sName + " to " + value + " failed." );
	}
    }

    public void typeChanged() {

	Enumeration en = m_vStateListeners.elements();
	
	while( en.hasMoreElements() ) {
	    ((StateListener)en.nextElement()).typeChanged( this );
	}
    }
}
