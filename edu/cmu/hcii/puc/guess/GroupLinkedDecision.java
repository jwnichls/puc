/**
 * GroupLinkedDecision.java
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

import edu.cmu.hcii.puc.GroupNode;


// Interface Definition

public interface GroupLinkedDecision {

    //**************************
    // Group Functions
    //**************************

    public void setGroupLink( GroupNode pGroup );
    public GroupNode getGroupLink();
}
