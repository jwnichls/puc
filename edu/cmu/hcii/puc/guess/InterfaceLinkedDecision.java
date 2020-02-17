/**
 * InterfaceLinkedDecision.java
 *
 * An interface that should be implemented for any design decision
 * object that links to a location in the group tree.
 *
 * Revision History
 * ----------------
 * 07/23/2002: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.guess;


// Import Declarations

import edu.cmu.hcii.puc.uigen.InterfaceNode;


// Interface Definition

public interface InterfaceLinkedDecision {

    //**************************
    // Group Functions
    //**************************

    public void setInterfaceLink( InterfaceNode pINode );
    public InterfaceNode getInterfaceLink();
}
