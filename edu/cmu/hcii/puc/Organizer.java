/**
 * Organizer.java
 * 
 * Represents some knowledge of organization, which will be applied at
 * user interface generation time.  When a specification is parsed,
 * all organizers are set to "none."  During the generation process,
 * these organizers are set to various values, based in part on
 * dependency information and in part by how many widgets are able to
 * fit on to a single screen.
 *
 * Revision History:
 * -----------------
 * 11/03/2001: (JWN) Created file.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.util.Hashtable;

import edu.cmu.hcii.puc.uigen.InterfaceNode;


// Interface Definition

public interface Organizer {

    public Hashtable addOrganization( GroupNode pGroup, InterfaceNode pCurrentNode ); 
}
