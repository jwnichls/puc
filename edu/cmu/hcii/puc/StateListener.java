/**
 * StateListener.java
 * 
 * The interface that is used by objects that want to be informed when
 * a appliance state changes.
 * 
 * Revision History:
 * -----------------
 * 10/03/2001: (JWN) Created file.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.lang.*;


// Class Definition

public interface StateListener extends ObjectListener {

    //**************************
    // Member Methods
    //**************************

    public void typeChanged( ApplianceState state );

    public void valueChanged( ApplianceState state );
}
