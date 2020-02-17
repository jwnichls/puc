/**
 * BooleanSpace.java
 *
 * The type objects have three functions: defining what values can be
 * stored in a type, validating that a value stored in a variable is
 * within the acceptable range, and storing the value for a typed variable.
 *
 * This is the boolean type.
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

public class BooleanSpace extends ValueSpace {

  //**************************
  // Constructors
  //**************************
  
  //**************************
  // Member Variables
  //**************************
  
  protected boolean m_bValue;
  
  
  //**************************
  // Public Methods
  //**************************
  
  public String getName() { return "boolean"; }
  public int    getSpace() { return ValueSpace.BOOLEAN_SPACE; }
  
  public Object  getValue() { return new Boolean( m_bValue ); }
  
  public boolean validate( Object value ) { 
      
    if ( value instanceof java.lang.String ) {
      String s = (String)value;

      return ( s.equalsIgnoreCase( "true" ) || 
	       s.equalsIgnoreCase( "false" ) ||
	       s.equalsIgnoreCase( "1" ) ||
	       s.equalsIgnoreCase( "0" ) ); 
    }
    else return value instanceof java.lang.Boolean;
  }

  public void  setValue( Object value )
    throws edu.cmu.hcii.puc.types.SpaceMismatchException {
      
      if ( validate( value ) ) {
	if ( value instanceof java.lang.Boolean ) {
	  m_bValue = ((Boolean)value).booleanValue();
	}
	else {
	  m_bValue = ((Boolean)convertString( (String)value )).booleanValue();
	}
      }
      else
	throw new SpaceMismatchException( "Not a Boolean value." );
  }

  protected Object convertString( String value ) 
    throws edu.cmu.hcii.puc.types.SpaceMismatchException
  {
    try {
      return new Boolean( ((String)value).equalsIgnoreCase( "true" ) ||
			  ((String)value).equalsIgnoreCase( "1" ));
    }
    catch( Throwable t ) {
      throw new SpaceMismatchException( "Not a Boolean value." );
    }
  }
  
  public int compareValues( ValueSpace pVS ) {

      if ( m_bValue == ((BooleanSpace)pVS).m_bValue )
	  return 0;
      
      return -1;
  }

  
  //**************************
  // Clone Method
  //**************************

  public Object clone() {

    BooleanSpace cln = new BooleanSpace();

    cln.m_bValue = this.m_bValue;

    return cln;
  }


  //**************************
  // Static Testing Method
  //**************************

  public static void main( String[] args ) {
    
    System.out.println( "BooleanSpace Test Sequence" );
    System.out.println( "-------------------------" );
    
    BooleanSpace regBool  = new BooleanSpace();
    BooleanSpace regBool2 = new BooleanSpace();
    
    int test = 0;
    
    /* Test #1 */
    test++; 
    try {
      regBool.setValue( new Boolean( true ) );
      
      if ( ((Boolean)regBool.getValue()).booleanValue() == true ) {
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
      regBool2.setValue( new Boolean( false ) );
      
      if ( ((Boolean)regBool2.getValue()).booleanValue() == false ) {
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
      regBool.setValue( new Boolean( false ) );
      
      if ( ((Boolean)regBool.getValue()).booleanValue() == false ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #4 */
    test++; 
    try {
      regBool.setValue( new Integer( 5 ) );
      
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Boolean)regBool.getValue()).booleanValue() == false ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #5 */
    test++; 
    try {
      regBool.setValue( "true" );
      
      if ( ((Boolean)regBool.getValue()).booleanValue() == true ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #6 */
    test++; 
    try {
      regBool.setValue( "false" );
      
      if ( ((Boolean)regBool.getValue()).booleanValue() == false ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #7 */
    test++; 
    try {
      regBool.setValue( "1234" );
      
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Boolean)regBool.getValue()).booleanValue() == false ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }


    System.out.println( "BooleanSpace PASSED" );
  }
}
