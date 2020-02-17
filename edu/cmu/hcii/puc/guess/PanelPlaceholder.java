/**
 * PanelPlaceholder.java
 *
 * A placeholder for locations where the interface generator believes
 * a panel will be needed.
 *
 * Revision History
 * ----------------
 * 07/23/2002: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.guess;


// Import Declarations

import java.lang.*;

import edu.cmu.hcii.puc.GroupNode;


// Class Definition

public class PanelPlaceholder extends PlaceholderDecision
                              implements GroupLinkedDecision {

    //**************************
    // Member Variables
    //**************************

    protected GroupNode m_pGroup;


    //**************************
    // Constructor
    //**************************

    public PanelPlaceholder( DesignDecision pParent ) {

	super( pParent );
    }
    
    public PanelPlaceholder() {

	super();
    }


    //**************************
    // Description Functions
    //**************************

    public String getDescription() {

	return "Panel Placeholder";
    }

    public String toString() {

	return getDescription() + "\nChildren: " + m_vChildren.toString();
    }


    //**************************
    // Group Functions
    //**************************

    public void setGroupLink( GroupNode pGroup ) {

	m_pGroup = pGroup;
    }

    public GroupNode getGroupLink() {

	return m_pGroup;
    }
}
