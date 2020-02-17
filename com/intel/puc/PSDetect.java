/*
 * PSDetect.java
 *
 * This source file attempts to detect whether this Java virtual
 * machine is running on a Personal Server or not.  All this class
 * does is check to see if the AWT class library is supported by this
 * Java virtual machine.  If not, then we assume that we are on a
 * personal server.
 *
 * We determine if AWT is available by attempting to load the current
 * Toolkit and catching any errors that occur during this process.
 *
 * Jeff Nichols
 * 11/10/2003
 */

// Package Definition

package com.intel.puc;


// Import Declarations

import java.awt.Toolkit;


// Class Definition

public class PSDetect {

    //**************************
    // Static Member Variables
    //**************************

    protected static boolean m_bPersonalServer;


    //**************************
    // Static Constructor
    //**************************

    static {

	m_bPersonalServer = false;

	try {

	    Toolkit tk = Toolkit.getDefaultToolkit();
	}
	catch( Throwable t ) { // catch any errors or exceptions

	    m_bPersonalServer = true;
	}
    }


    //**************************
    // Static Member Methods
    //**************************

    public static boolean isPersonalServer() {

	return m_bPersonalServer;
    }


    //**************************
    // Constructor
    //**************************

    private PSDetect() {

	// prevent people from instantiating this object.
    }
}


