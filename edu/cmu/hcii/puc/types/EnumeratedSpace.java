/**
 * EnumeratedSpace.java
 *
 * The type objects have three functions: defining what values can be
 * stored in a type, validating that a value stored in a variable is
 * within the acceptable range, and storing the value for a typed variable.
 *
 * This is the enumerated type.
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

import java.util.Vector;


// Class Definition

public class EnumeratedSpace extends NumberSpace {

  //**************************
  // Constructors
  //**************************
  
  public EnumeratedSpace( int nItems ) {

    m_nItems = nItems;

    if ( m_nItems <= 0 )
	throw new IllegalArgumentException( "EnumeratedSpace must have more than 0 items" );

    m_nValue = 0;
  }


  //**************************
  // Member Variables
  //**************************
  
  protected int m_nValue;

  protected int m_nItems;
  
  
  //**************************
  // Public Methods
  //**************************
  
  public String getName() { return "enumerated"; }
  public int    getSpace() { return ValueSpace.ENUMERATED_SPACE; }
  
  public Object  getValue() { return new Integer( m_nValue ); }
  
  public boolean validate( Object val ) {
	
    if ( val instanceof java.lang.String ) {
      try {
	Integer i = new Integer( (String)val );
	return validateNoStrChk( i );
      }
      catch( Throwable t ) {
	return false; 
      }
    }
    else 
      return validateNoStrChk( val );
  }

  private boolean validateNoStrChk( Object val ) {
    if ( val instanceof java.lang.Number ) {

      Number value = (Number)val;

      return ( value.intValue() >= 1 &&
	       value.intValue() <= m_nItems );       
    }

    return false;
  }
  
  public void  setValue( Object value )
      throws edu.cmu.hcii.puc.types.SpaceMismatchException {

      if ( value instanceof java.lang.String ) {
	  m_nValue = ((Integer)convertString( (String)value )).intValue();
      }
      else if ( validateNoStrChk( value ) ) {
	  m_nValue = ((Integer)value).intValue();
      }
      else throw new SpaceMismatchException( "Not an integer within correct range of the enumerated type." );
  }

  protected Object convertString( String value ) 
    throws edu.cmu.hcii.puc.types.SpaceMismatchException
  {
    try { 
      Integer i = new Integer( (String)value );    
      if ( validateNoStrChk( i ) ) {
	return i;
      }
      else throw new SpaceMismatchException( "Not an integer within correct range of the enumerated type." );
    }
    catch( Throwable t ) {
      throw new SpaceMismatchException( "No integer value within String for enumerated type." );
    }
  }
  
  public int compareValues( ValueSpace pVS ) {

      int nVSValue = ((EnumeratedSpace)pVS).m_nValue;
      
      if ( m_nValue == nVSValue ) return 0;
      else if (  nVSValue > m_nValue ) return 1;
      else return -1;      
  }

 
  //**************************
  // EnumeratedSpace Methods
  //**************************

  public int getIntValue() { return m_nValue; }

  public int getNumItems() { return m_nItems; }

  
  //**************************
  // Clone Method
  //**************************

  public Object clone() {

    EnumeratedSpace cln = new EnumeratedSpace( this.m_nItems );
    
    cln.m_nValue = this.m_nValue;

    return cln;
  }


  //**************************
  // Static Testing Method
  //**************************

  public static void main( String[] args ) {
    
    System.out.println( "EnumeratedSpace Test Sequence" );
    System.out.println( "-------------------------" );

    System.out.println( "no test sequence for this type yet...JWN" );
    System.exit(1);
  }
}

