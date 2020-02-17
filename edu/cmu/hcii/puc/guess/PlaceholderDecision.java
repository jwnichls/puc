/**
 * PlaceholderDecision.java
 *
 * These design decision objects are used in areas where we know some
 * decision will need to be made, but we haven't done the processing
 * to know what the alternatives are yet. 
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

public abstract class PlaceholderDecision extends DesignDecision {

    //**************************
    // Constructor
    //**************************

    public PlaceholderDecision( DesignDecision pParent ) {

	super( pParent );
    }
    
    public PlaceholderDecision() {

	super();
    }


    //**************************
    // Tree Manipulations
    //**************************

    public void replace( DesignDecision pDecision ) {

	pDecision.m_pParent = m_pParent;
	pDecision.m_vChildren = m_vChildren;

	Enumeration e = m_vChildren.elements();
	while( e.hasMoreElements() ) 
	    ((DesignDecision)e.nextElement()).m_pParent = pDecision;
    }
}
