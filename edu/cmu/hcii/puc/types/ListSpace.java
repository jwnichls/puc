/**
 * ListSpace.java
 *
 * The type objects have three functions: defining what values can be
 * stored in a type, validating that a value stored in a variable is
 * within the acceptable range, and storing the value for a typed variable.
 *
 * This is the list type.  It represents a list of variables in a
 * certain value space.
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

public class ListSpace extends ValueSpace {
  
  //**************************
  // Constructors
  //**************************
  
  public ListSpace() {
	
    m_bRanged = false;
    m_bIncremented = false;
  }
  
  public ListSpace( int nBotRange, int nTopRange ) {
    this();

    m_bRanged = true;
    m_nBottomRange = nBotRange;
    m_nTopRange = nTopRange;

    m_nValue = m_nBottomRange;
  }

  public ListSpace( int nBotRange, int nTopRange, int nIncrement ) {
    this( nBotRange, nTopRange );

    m_bIncremented = true;
    m_nIncrement = nIncrement;
  }
  
  
  //**************************
  // Member Variables
  //**************************
  
  protected int m_nValue;
  
  protected boolean m_bRanged;
  protected int     m_nBottomRange;
  protected int     m_nTopRange;

  protected boolean m_bIncremented;
  protected int     m_nIncrement;


  //**************************
  // ValueSpace Public Methods
  //**************************

  public String getName() { return "integer"; }
  public int    getSpace() { return ValueSpace.INTEGER_SPACE; }
  
  public Object  getValue() { return new Integer( m_nValue ); }
  
  public boolean validate( Object val ) {
	
    if ( val instanceof java.lang.Number ) {

      Number value = (Number)val;

      boolean rng = !m_bRanged || 
	                  ( value.intValue() >= m_nBottomRange &&
			    value.intValue() <= m_nTopRange ); 
      
      boolean incr = !m_bIncremented ||
	                  ( Math.IEEEremainder( value.doubleValue() - 
						(double)m_nBottomRange, 
						(double)m_nIncrement ) 
			    == 0 );

      return rng && incr;
    }

    return false;
  }
  
  public void  setValue( Object value )
	throws edu.cmu.hcii.puc.types.SpaceMismatchException {
	  
	  if ( validate( value ) ) {
	    m_nValue = ((Integer)value).intValue();
	  }
	  else
	    throw new SpaceMismatchException( "Not an Integer value or not within correct range." );
  }

  public Object convertString( String value ) 
    throws edu.cmu.hcii.puc.types.SpaceMismatchException
  {
      return value;
  }

  public int compareValues( ValueSpace pVS ) {

      return -1;
  }


  //**************************
  // ListSpace Public Methods
  //**************************

  public boolean isRanged() { return m_bRanged; }

  public int getBottomRange() { return m_nBottomRange; }

  public int getTopRange() { return m_nTopRange; }

  public boolean isIncremented() { return m_bIncremented; }

  public int getIncrement() { return m_nIncrement; }
  

  //**************************
  // Clone Method
  //**************************

  public Object clone() {

	ListSpace cln = new ListSpace();

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

    System.out.println( "ListSpace Test Sequence" );
    System.out.println( "-------------------------" );

    ListSpace regInt = new ListSpace();
    ListSpace rngInt = new ListSpace( 0, 10 );
    ListSpace incInt = new ListSpace( 0, 10, 2 );

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

    ListSpace clnInt = (ListSpace)incInt.clone();

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


    System.out.println( "ListSpace PASSED" );
  }
}
