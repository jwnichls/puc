/**
 * ValueSpace.java
 * 
 * An object representing the type of a state.
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

public abstract class ValueSpace implements Cloneable {

    //**************************
    // Constants
    //**************************

    public static final int BOOLEAN_SPACE     = 0;
    public static final int INTEGER_SPACE     = 1;
    public static final int FLOATING_PT_SPACE = 2;
    public static final int FIXED_PT_SPACE    = 3;
    public static final int STRING_SPACE      = 4;
    public static final int ENUMERATED_SPACE  = 5;
    public static final int COMPOUND_SPACE    = 6;
    public static final int LIST_SPACE        = 7;
    public static final int CUSTOM_SPACE      = 8;


    //**************************
    // Constructor
    //**************************

    protected ValueSpace() { }


    //**************************
    // Abstract Methods
    //**************************

    public String toString() {

	return getName() + " = " + getValue().toString();
    }

    public abstract String  getName();
    public abstract int     getSpace();

    public abstract Object  getValue();
    public abstract boolean validate( Object value );
    public abstract void    setValue( Object value ) 
	throws edu.cmu.hcii.puc.types.SpaceMismatchException;

    /**
     * compareValues( ValueSpace pVS )
     *
     * Compares the values within two value spaces, and returns a
     * result based on a comparison of the values stored within the
     * space objects.  The function returns 0 if the values are equal,
     * and non-zero if the values are not equal.  If greater-than and
     * less-than relations make sense for the space (NumberSpaces), -1
     * is returned when pVS < this.value and 1 is returned when pVS >
     * this.value
     *
     * This function assumes that the ValueSpaces are equivalent.  The
     * results of this function are not meaningful otherwise,
     * and a ClassCastException may be thrown if the spaces are
     * significantly different.
     */
    public abstract int     compareValues( ValueSpace pVS );

    public abstract Object  clone();
}
