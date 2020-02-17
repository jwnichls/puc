/**
 * FloatingPtSpace.java
 *
 * The type objects have three functions: defining what values can be
 * stored in a type, validating that a value stored in a variable is
 * within the acceptable range, and storing the value for a typed variable.
 *
 * This is the floating point type.
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

public class FloatingPtSpace extends NumberSpace {
  
  //**************************
  // Constructors
  //**************************
  
  public FloatingPtSpace() {
	
    m_bRanged = false;
  }
  
  public FloatingPtSpace( Number dBotRange, Number dTopRange ) {
    this();

    m_bRanged = true;
    m_dBottomRange = dBotRange;
    m_dTopRange = dTopRange;

    m_dValue = m_dBottomRange.doubleValue();
  }
  
  
  //**************************
  // Member Variables
  //**************************
  
  protected double  m_dValue;
  
  protected boolean m_bRanged;
  protected Number  m_dBottomRange;
  protected Number  m_dTopRange;


  //**************************
  // ValueSpace Public Methods
  //**************************

  public String getName() { return "floating point"; }
  public int    getSpace() { return ValueSpace.FLOATING_PT_SPACE; }
  
  public Object  getValue() { return new Double( m_dValue ); }

  public boolean validate( Object val ) {
    if ( val instanceof java.lang.String ) {
      try {
	Double d = new Double( (String)val );
	return validateNoStrChk( d );
      }
      catch( Throwable t ) {
	return false;
      }
    }
    else return validateNoStrChk( val );
  }
    
  private boolean validateNoStrChk( Object val ) {
	
    if ( val instanceof java.lang.Number ) {

      Number value = (Number)val;

      boolean rng = !m_bRanged || 
	                  ( value.doubleValue() >= m_dBottomRange.doubleValue() &&
			    value.doubleValue() <= m_dTopRange.doubleValue() ); 

      return rng;
    }

    return false;
  }
  
  public void  setValue( Object value )
	throws edu.cmu.hcii.puc.types.SpaceMismatchException {
	
      if ( value instanceof java.lang.String ) {
	  m_dValue = ((Double)convertString( (String)value )).doubleValue();
      }
      else if ( validateNoStrChk( value ) ) {
	  m_dValue = ((Number)value).doubleValue();
      }
      else throw new SpaceMismatchException( "Not an FloatingPoint value or not within correct range." );
  }

  protected Object convertString( String value ) 
    throws edu.cmu.hcii.puc.types.SpaceMismatchException
  {
    try {
      Double d = new Double( (String)value );
      if ( validateNoStrChk( d ) ) {
	return d;
      }
      else throw new SpaceMismatchException( "Not an FloatingPoint value or not within correct range." );
    }
    catch( Throwable t ) {
      throw new SpaceMismatchException( "Not an FloatingPoint value within string." );
    }
  }

  public int compareValues( ValueSpace pVS ) {

      double pVSValue = ((FloatingPtSpace)pVS).m_dValue;
      
      if ( m_dValue == pVSValue ) return 0;
      else if (  pVSValue > m_dValue ) return 1;
      else return -1;
  }


  //**************************
  // FloatingPtSpace Public Methods
  //**************************

  public boolean isRanged() { return m_bRanged; }

  public Number getBottomRange() { return m_dBottomRange; }

  public Number getTopRange() { return m_dTopRange; }
  

  //**************************
  // Clone Method
  //**************************

  public Object clone() {

	FloatingPtSpace cln = new FloatingPtSpace();

	cln.m_dValue = this.m_dValue;
	cln.m_bRanged = this.m_bRanged;
	cln.m_dBottomRange = this.m_dBottomRange;
	cln.m_dTopRange = this.m_dTopRange;

	return cln;
  }


  //**************************
  // Static Testing Method
  //**************************

  public static void main( String[] args ) {

    System.out.println( "FloatingPtSpace Test Sequence" );
    System.out.println( "----------------------------" );

    FloatingPtSpace regDbl = new FloatingPtSpace();
    FloatingPtSpace rngDbl = new FloatingPtSpace( new Double(0), 
						  new Double(10) );

    int test = 0;

    /* Test #1 */
    test++; 
    try {
      regDbl.setValue( new Double( 5.3 ) );

      if ( ((Number)regDbl.getValue()).doubleValue() == 5.3 ) {
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
      rngDbl.setValue( new Double( 5.3 ) );

      if ( ((Number)rngDbl.getValue()).doubleValue() == 5.3 ) {
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
      rngDbl.setValue( new Double( 11 ) );
      System.out.println( "1. Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)rngDbl.getValue()).doubleValue() == 5.3 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "2. Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #4 */
    test++;
    try {
      rngDbl.setValue( new Double( 10 ) );
      if ( ((Double)rngDbl.getValue()).doubleValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "2. Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #5 */
    test++;
    try {
      regDbl.setValue( new Double( 11 ) );

      if ( ((Double)regDbl.getValue()).doubleValue() == 11 ) {
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
      regDbl.setValue( new Boolean( true ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      System.out.println( "Passed #" + test );
    }

    FloatingPtSpace clnDbl = (FloatingPtSpace)rngDbl.clone();

    /* Test #7 */
    test++;
    try {
      clnDbl.setValue( new Double( 11 ) );
      System.out.println( "1. Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)clnDbl.getValue()).doubleValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "2. Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #8 */
    test++;
    try {
      clnDbl.setValue( new Double( 5.3 ) );

      if ( ((Number)clnDbl.getValue()).doubleValue() == 5.3 ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #9 */
    test++;
    try {
      clnDbl.setValue( new Double( 10 ) );
      if ( ((Double)clnDbl.getValue()).doubleValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {

      System.out.println( "2. Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #10 */
    test++;
    try {
      clnDbl.setValue( "10" );
      if ( ((Double)clnDbl.getValue()).doubleValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {

      System.out.println( "2. Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #11 */
    test++;
    try {
      clnDbl.setValue( "abcd" );

      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)clnDbl.getValue()).doubleValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    System.out.println( "FloatingPtSpace PASSED" );
  }
}
