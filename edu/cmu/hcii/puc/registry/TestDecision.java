/**
 * TestDecision.java
 *
 * A random decision object used for testing.  Not for general use.
 *
 * Revision History
 * ----------------
 * 02/02/2002: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.registry;


// Import Declarations

import java.lang.*;

import java.util.Hashtable;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.cio.*;


// Class Definition

public class TestDecision extends Decision {

    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public TestDecision( Hashtable pChoices ) {

	super( pChoices );
    }


    //**************************
    // Abstract Member Methods
    //**************************
    
    public ConcreteInteractionObject chooseWidget( Appliance a,
						   ApplianceObject ao ) {

	return ((Decision)m_pChoices.get( Decision.DEFAULT )).chooseWidget( a, ao );
    }


    public String toString() {
	
	return "TEST Decision\n" + super.toString(); 
    }
}
