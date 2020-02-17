/**
 * SpaceMismatchException.java
 * 
 * This exception is thrown whenever an attempt is made to store a
 * value in a variable whose type does not support it.
 * 
 * Revision History:
 * -----------------
 * 07/12/2001: (JWN) Created file.
 *
 */

// Package Definition

package edu.cmu.hcii.puc.types;


// Import Declarations

import java.lang.*;


// Class Definition

public class SpaceMismatchException extends java.lang.Exception {

    public SpaceMismatchException() {
	super();
    }
    public SpaceMismatchException( String msg ) {
	super( msg );
    }
}
