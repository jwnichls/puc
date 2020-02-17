/**
 * StateLinkedCIO.java
 *
 * A sub-class of ConcreteInteractionObject.
 *
 * Represents a CIO that is linked to a state or command of the
 * appliance.  This class cannot be instantiated directly, only as the
 * super-class of another object.
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.Component;
import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;


// Class Definition

public abstract class StateLinkedCIO extends ConcreteInteractionObject {

    //**************************
    // Member Variables
    //**************************

    protected ApplianceObject m_ApplObj;

    protected Appliance m_Appliance;


    //**************************
    // Constructor
    //**************************

    protected StateLinkedCIO( Appliance appliance,
			      ApplianceObject applObj,
			      Component widget ) {

	super( widget );

	m_ApplObj = applObj;
	m_Appliance = appliance;
    }


    //**************************
    // Member Methods
    //**************************

    public abstract void useMinimumLabel();

    public ApplianceObject getApplObj() {

	return m_ApplObj;
    }
}
