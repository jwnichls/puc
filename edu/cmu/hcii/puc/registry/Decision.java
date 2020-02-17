/**
 * Decision.java
 *
 * Represents a particular branch in the widget selection decision
 * tree.  Each branch object understands how to compute its particular
 * decision from a particular ApplianceObject.
 *
 * Revision History
 * ----------------
 * 02/02/2002: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.registry;


// Import Declarations

import java.lang.*;

import java.util.Enumeration;
import java.util.Hashtable;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.cio.ConcreteInteractionObject;


// Class Definition

public abstract class Decision extends Object {

    //**************************
    // Constants
    //**************************

    public static final String DEFAULT = "default";


    //**************************
    // Member Variables
    //**************************

    protected Hashtable m_pChoices;


    //**************************
    // Constructor
    //**************************

    public Decision( Hashtable pChoices ) {

	m_pChoices = pChoices;
    }


    //**************************
    // Abstract Member Methods
    //**************************
    
    public abstract ConcreteInteractionObject chooseWidget( Appliance a, 
							    ApplianceObject ao );

    public String toString() {
	
	String sResult = "Conditions:\n";

	Enumeration e = m_pChoices.keys();
	while( e.hasMoreElements() ) {
	    String key = (String)e.nextElement();

	    sResult += key + "\n=\n" + m_pChoices.get(key).toString();
	}

	return sResult;
    }
}
