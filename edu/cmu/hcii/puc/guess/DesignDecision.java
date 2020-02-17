/**
 * DesignDecision.java
 *
 * The superclass for all design decision "command objects".  These
 * objects represent the possible design choices available in response
 * to specific condition, and the current choice being used.  e.g. an
 * abstract interaction object (AIO) is a design decision object that
 * represents the possible components that could be used to interact
 * with an appliance object, and the component that is currently being
 * used. 
 * 
 * There is also a separate class of placeholder decisions, that are
 * used in areas where we know some decision will need to be made, but
 * we haven't done the processing to know what the alternatives are yet.
 *
 * Collectively, the design decisions arrange themselves in a tree.
 * This gives the interface generator some idea of which decisions are
 * independent of each other, and which are not.
 *
 * Revision History
 * ----------------
 * 07/23/2002: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.guess;


// Import Declarations

import java.lang.*;

import java.util.Enumeration;
import java.util.Vector;


// Class Definition

public abstract class DesignDecision {

    //**************************
    // Member Variables
    //**************************

    protected DesignDecision m_pParent;
    protected Vector         m_vChildren;


    //**************************
    // Constructor
    //**************************

    public DesignDecision( DesignDecision pParent ) {

	this();

	m_pParent = pParent;
	m_pParent.m_vChildren.addElement( this );
    }

    public DesignDecision() {

	m_vChildren = new Vector();
    }


    //**************************
    // Tree Manipulations
    //**************************

    public void addChild( DesignDecision pChild ) {

	m_vChildren.addElement( pChild );
	pChild.m_pParent = this;
    }

    public void insertAsParent( DesignDecision pNewParent ) {
	
	if ( m_pParent != null ) {
	    m_pParent.m_vChildren.removeElement( this );
	    m_pParent.m_vChildren.addElement( pNewParent );
	}  /* FIXME:JWN:??
	else // make sure the root pointer is updated?
        */

        m_pParent = pNewParent;
	pNewParent.m_vChildren.addElement( this );
    }

    public void insertAboveChildren( DesignDecision pOnlyChild ) {

	pOnlyChild.m_vChildren = m_vChildren;

	Enumeration e = m_vChildren.elements();
	while( e.hasMoreElements() )
	    ((DesignDecision)e.nextElement()).m_pParent = pOnlyChild;

	m_vChildren = new Vector();
	m_vChildren.addElement( pOnlyChild );
	pOnlyChild.m_pParent = this;
    }


    //**************************
    // Description Functions
    //**************************

    public abstract String getDescription();
    public abstract String toString();
}
