/**
 * StringValue.java
 *
 * The StringValue class is a mutable form of the String class.  It is
 * used in instances where a String may be constrained, but is not in
 * the particular case.
 *
 * These objects require "validation" after parsing, because the Value
 * state name is only known as a String at the end of the process.
 * One of the later phases of the parser.SpecParser resolves these
 * Strings to ApplianceState objects.  The java.lang.Boolean methods
 * cannot be used until after the class is valid.  A
 * NullPointerException is thrown if these methods are called
 * before validation.
 *
 * Revision History
 * ----------------
 * 02/03/2002: (JWN) Created file.  
 */

// Package Definition

package edu.cmu.hcii.puc.types;


// Import Declarations

import java.lang.*;


// Class Definition

public class StringValue implements PUCString  {

    //**************************
    // Member Variables
    //**************************

    protected String m_sValue;


    //**************************
    // Constructor
    //**************************

    public StringValue( String sValue ) {

	m_sValue = sValue;
    }


    //**************************
    // Member Methods
    //**************************

    public void setString( String sValue ) {

	m_sValue = sValue;
    }

    public String toString() {

	return m_sValue;
    }    
}
