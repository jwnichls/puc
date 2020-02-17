/**
 * StringSpace.java
 *
 * The type objects have three functions: defining what values can be
 * stored in a type, validating that a value stored in a variable is
 * within the acceptable range, and storing the value for a typed variable.
 *
 * This is the string type.
 *
 * Revision History:
 * -----------------
 * 07/12/2001: (JWN) Created file.
 * 
 */

// Package Definition

package edu.cmu.hcii.puc.types;


// Import Declarations

import edu.cmu.hcii.puc.*;

import java.lang.*;


// Class Definition

public class StringSpace extends ValueSpace {

  //**************************
  // Constructors
  //**************************

  public StringSpace() {
    m_sValue = "";
  }

  
  //**************************
  // Member Variables
  //**************************
  
  protected String m_sValue;
  
  
  //**************************
  // Public Methods
  //**************************
  
  public String getName() { return "string"; }
  public int    getSpace() { return ValueSpace.STRING_SPACE; }
  
  // don't need to clone because string objects are immutable
  public Object  getValue() { return m_sValue; }
  
  public boolean validate( Object value ) { 
    return value instanceof java.lang.String;
  }
  
  public void  setValue( Object value )
    throws edu.cmu.hcii.puc.types.SpaceMismatchException {
      
      if ( validate( value ) ) {
	// don't need to clone because string objects are immutable
	m_sValue = (String)value;
      }
      else
	throw new SpaceMismatchException( "Not a Boolean value." );
  }
  
  public int compareValues( ValueSpace pVS ) {

      if ( m_sValue.equals( ((StringSpace)pVS).m_sValue ) )
	  return 0;

      return -1;
  }

  
  //**************************
  // Clone Method
  //**************************

  public Object clone() {

    StringSpace cln = new StringSpace();
    
    // don't need to clone because string objects are immutable
    cln.m_sValue = m_sValue;

    return cln;
  }


  //**************************
  // Static Testing Method
  //**************************

  public static void main( String[] args ) {
    
    System.out.println( "StringSpace Test Sequence" );
    System.out.println( "-------------------------" );
    
    StringSpace regStr  = new StringSpace();
    
    int test = 0;
    
    /* Test #1 */
    test++; 
    try {
      regStr.setValue( new String( "hello" ) );
      
      if ( ((String)regStr.getValue()).equals( "hello" ) ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #2 */
    test++; 
    try {
      regStr.setValue( new String( "dumb" ) );
      
      if ( ((String)regStr.getValue()).equals( "dumb" ) ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #3 */
    test++; 
    try {
      regStr.setValue( new Integer( 5 ) );
      
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((String)regStr.getValue()).equals( "dumb" ) ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    StringSpace clnStr = (StringSpace)regStr.clone();

    /* Test #4 */
    test++;
    if ( ((String)clnStr.getValue()).equals( "dumb" ) ) {
      System.out.println( "Passed #" + test );
    }
    else {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #5 */
    test++; 
    try {
      clnStr.setValue( new String( "hello" ) );
      
      if ( ((String)clnStr.getValue()).equals( "hello" ) ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    System.out.println( "StringSpace PASSED" );
  }
}
