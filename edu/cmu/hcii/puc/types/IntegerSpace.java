/**
 * IntegerSpace.java
 *
 * The type objects have three functions: defining what values can be
 * stored in a type, validating that a value stored in a variable is
 * within the acceptable range, and storing the value for a typed variable.
 *
 * This is the integer type.
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

public class IntegerSpace extends NumberSpace {
  
  //**************************
  // Constructors
  //**************************
  
  public IntegerSpace() {
	
    m_bRanged = false;
    m_bIncremented = false;
  }
  
  public IntegerSpace( Number nBotRange, Number nTopRange ) {
    this();

    m_bRanged = true;
    m_nBottomRange = nBotRange;
    m_nTopRange = nTopRange;

    m_nValue = m_nBottomRange.intValue();
  }

  public IntegerSpace( Number nBotRange, Number nTopRange, Number nIncrement ) {
    this( nBotRange, nTopRange );

    m_bIncremented = true;
    m_nIncrement = nIncrement;
  }
  
  
  //**************************
  // Member Variables
  //**************************
  
  protected int m_nValue;
  
  protected boolean m_bRanged;
  protected Number  m_nBottomRange;
  protected Number  m_nTopRange;

  protected boolean m_bIncremented;
  protected Number  m_nIncrement;


  //**************************
  // ValueSpace Public Methods
  //**************************

  public String toString() {

      return super.toString() + " : max = " + m_nTopRange.toString() + " min = " + m_nBottomRange.toString() + " incr = " + m_nIncrement.toString();
  }

  public String getName() { return "integer"; }
  public int    getSpace() { return ValueSpace.INTEGER_SPACE; }
  
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

      boolean rng = !m_bRanged || 
	                  ( value.intValue() >= m_nBottomRange.intValue() &&
			    value.intValue() <= m_nTopRange.intValue() ); 
      
      boolean incr = !m_bIncremented ||
	                  ( Math.IEEEremainder( value.doubleValue() - 
						m_nBottomRange.doubleValue(), 
						m_nIncrement.doubleValue() ) 
			    == 0 );

      return rng && incr;
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
      else throw new SpaceMismatchException( "Not an Integer value or not within correct range." );
  }
    
  protected Object convertString( String value ) 
    throws edu.cmu.hcii.puc.types.SpaceMismatchException
  {
    try { 
      Integer i = new Integer( (String)value );    
      if ( validateNoStrChk( i ) ) {
	return i;
      }
      else throw new SpaceMismatchException( "Not an Integer value or not within correct range." );
    }
    catch( Throwable t ) {
      throw new SpaceMismatchException( "No integer value within String." );
    }	
  }

  public int compareValues( ValueSpace pVS ) {

      int pVSValue = ((IntegerSpace)pVS).m_nValue;
      
      if ( m_nValue == pVSValue ) return 0;
      else if (  pVSValue > m_nValue ) return 1;
      else return -1;      
  }


  //**************************
  // IntegerSpace Public Methods
  //**************************

  public boolean isRanged() { return m_bRanged; }

  public Number getBottomRange() { return m_nBottomRange; }

  public Number getTopRange() { return m_nTopRange; }

  public boolean isIncremented() { return m_bIncremented; }

  public Number getIncrement() { return m_nIncrement; }
  

  //**************************
  // Clone Method
  //**************************

  public Object clone() {

	IntegerSpace cln = new IntegerSpace();

	cln.m_nValue = this.m_nValue;
	cln.m_bRanged = this.m_bRanged;
	cln.m_nBottomRange = this.m_nBottomRange;
	cln.m_nTopRange = this.m_nTopRange;
	cln.m_bIncremented = this.m_bIncremented;
	cln.m_nIncrement = this.m_nIncrement;

	return cln;
  }


  //**************************
  // Static Testing Method
  //**************************

  public static void main( String[] args ) {

    System.out.println( "IntegerSpace Test Sequence" );
    System.out.println( "-------------------------" );

    IntegerSpace regInt = new IntegerSpace();
    IntegerSpace rngInt = new IntegerSpace( new Integer(0), 
					    new Integer(10) );
    IntegerSpace incInt = new IntegerSpace( new Integer(0), 
					    new Integer(10), 
					    new Integer(2) );

    int test = 0;

    /* Test #1 */
    test++; 
    try {
      regInt.setValue( new Integer( 5 ) );

      if ( ((Integer)regInt.getValue()).intValue() == 5 ) {
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
      rngInt.setValue( new Integer( 5 ) );

      if ( ((Integer)rngInt.getValue()).intValue() == 5 ) {
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
      incInt.setValue( new Integer( 5 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Integer)incInt.getValue()).intValue() == 0 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #4 */
    test++;
    try {
      incInt.setValue( new Integer( 6 ) );

      if ( ((Integer)incInt.getValue()).intValue() == 6 ) {
	System.out.println( "Passed #" + test );
      }    
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #5 */
    test++;
    try {
      rngInt.setValue( new Integer( 11 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Integer)rngInt.getValue()).intValue() == 5 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #6 */
    test++;
    try {
      regInt.setValue( new Integer( 11 ) );

      if ( ((Integer)regInt.getValue()).intValue() == 11 ) {
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
      incInt.setValue( new Integer( 11 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Integer)incInt.getValue()).intValue() == 6 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #8 */
    test++;
    try {
      incInt.setValue( new Integer( 12 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Integer)incInt.getValue()).intValue() == 6 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #9 */
    test++;
    try {
      incInt.setValue( new Integer( 10 ) );

      if ( ((Integer)incInt.getValue()).intValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }    
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #10 */
    test++;
    try {
      regInt.setValue( new Boolean( true ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      System.out.println( "Passed #" + test );
    }

    IntegerSpace clnInt = (IntegerSpace)incInt.clone();

    /* Test #11 */
    test++;
    try {
      clnInt.setValue( new Integer( 5 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Integer)clnInt.getValue()).intValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #12 */
    test++;
    try {
      clnInt.setValue( new Integer( 6 ) );

      if ( ((Integer)clnInt.getValue()).intValue() == 6 ) {
	System.out.println( "Passed #" + test );
      }    
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #13 */
    test++;
    try {
      clnInt.setValue( new Integer( 11 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Integer)clnInt.getValue()).intValue() == 6 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #14 */
    test++;
    try {
      clnInt.setValue( new Integer( 12 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Integer)clnInt.getValue()).intValue() == 6 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }

    /* Test #15 */
    test++;
    try {
      clnInt.setValue( new Integer( 10 ) );

      if ( ((Integer)clnInt.getValue()).intValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }    
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #16 */
    test++;
    try {
      clnInt.setValue( "10" );

      if ( ((Integer)clnInt.getValue()).intValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }    
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #17 */
    test++;
    try {
      clnInt.setValue( "abcd" );

      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Integer)clnInt.getValue()).intValue() == 10 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }


    System.out.println( "IntegerSpace PASSED" );
  }
}
