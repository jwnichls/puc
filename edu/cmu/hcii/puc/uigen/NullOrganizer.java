/**
 * NullOrganizer.java
 * 
 * Implements the Organizer interface.
 *
 * This organizer represents a lack of organization.  It is the
 * default organizer for all groups.
 *
 * To eliminate object creation overhead, the NullOrganizer is created
 * a Singleton object, and the same object is referenced wherever a
 * NullOrganizer is needed.  To get a NullOrganizer object, use the
 * static method getTheNullOrganizer().
 *
 * Revision History:
 * -----------------
 * 11/03/2001: (JWN) Created file.
 *
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.util.Hashtable;

import edu.cmu.hcii.puc.GroupNode;
import edu.cmu.hcii.puc.Organizer;


// Class Definition

public class NullOrganizer implements Organizer {

    //**************************
    // Static Methods
    //**************************

    protected static NullOrganizer m_pNullOrganizer;

    public static NullOrganizer getTheNullOrganizer() {
	if ( m_pNullOrganizer == null )
	    m_pNullOrganizer = new NullOrganizer();
   
	return m_pNullOrganizer;
    }


    //**************************
    // Constructor
    //**************************
    
    protected NullOrganizer() { }


    //**************************
    // Interface Methods
    //**************************

    public Hashtable addOrganization( GroupNode pGroup, InterfaceNode pCurrentNode ) {

	return null;
    }
}
