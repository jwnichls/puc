/**
 * ObjectListener.java
 * 
 * The interface that is used by objects that want to be informed when
 * a appliance object has been enabled or disabled.
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

public interface ObjectListener {

    //**************************
    // Member Methods
    //**************************

    public void enableChanged( ApplianceObject obj );

    public void labelChanged( ApplianceObject obj );
}
