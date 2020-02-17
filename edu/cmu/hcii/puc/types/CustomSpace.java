/**
 * CustomSpace.java
 * 
 * An object representing a custom value space.  Custom spaces are
 * string-based.  It is expected that the widget that handles a
 * CustomSpace will either know how to parse the state, or will simply
 * make it available to the user as a String.
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

public class CustomSpace extends ValueSpace {

    //**************************
    // Member Variables
    //**************************

    protected String m_sCustomName;
    protected String m_sCustomValue;


    //**************************
    // Constructor
    //**************************

    public CustomSpace( String sName ) { 

	m_sCustomName = sName;
	m_sCustomValue = "1";
    }


    //**************************
    // Abstract Methods
    //**************************

    public String toString() {

	return getName() + " = " + getValue().toString();
    }

    public String  getName() { return m_sCustomName; }
    public int     getSpace() { return ValueSpace.CUSTOM_SPACE; }

    public Object  getValue() { return m_sCustomValue; }
    public boolean validate( Object value ) { return true; }
    public void    setValue( Object value ) 
	throws edu.cmu.hcii.puc.types.SpaceMismatchException {
	
	if ( value instanceof java.lang.String )
	    m_sCustomValue = (String)value;
	else
	    throw new edu.cmu.hcii.puc.types.SpaceMismatchException( "Non-string value to a custom type" );
    }

    /**
     * compareValues( ValueSpace pVS )
     *
     * Do a string compare for now...
     */
    public int compareValues( ValueSpace pVS ) { 
      if ( m_sCustomValue.equals( ((CustomSpace)pVS).m_sCustomValue ) )
	  return 0;

      return -1;	
    }

    public Object clone() {

	CustomSpace s = new CustomSpace( m_sCustomName );
	s.m_sCustomValue = m_sCustomValue;

	return s;
    }
}
