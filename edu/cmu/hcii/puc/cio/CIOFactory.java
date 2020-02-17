/**
 * CIOFactory.java
 *
 * The CIOFactory is an object used for dynamic loading CIOs.  It is a
 * part of the Factory design pattern.
 *
 * Revision History 
 * ---------------- 
 * 01/28/2002: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.lang.*;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;


// Class Definition

public interface CIOFactory {

    //**************************
    // Member Methods
    //**************************

    public ConcreteInteractionObject createCIO( Appliance a,
						ApplianceObject ao ); 
}
