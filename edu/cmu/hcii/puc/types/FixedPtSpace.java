/**
 * FixedPtSpace.java
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

public class FixedPtSpace extends NumberSpace {
  
  //**************************
  // Constructors
  //**************************
  
    /**
     * nPointPos is the position of the fixed point from the right
     */
  public FixedPtSpace( int nPointPos ) {
	
    m_nPointPos = nPointPos;

    m_bRanged = false;
    m_bIncremented = false;

    m_nPointCorrectionFactor = calculateCorrectionFactor( m_nPointPos );
  }
  
  public FixedPtSpace( int nPointPos, Number pBotRange, Number pTopRange ) {
    this( nPointPos );

    m_bRanged = true;
    m_pBottomRange = pBotRange;
    m_pTopRange = pTopRange;

    m_nValue = m_pBottomRange.intValue();
  }

  public FixedPtSpace( int nPointPos, Number pBotRange, Number pTopRange, Number pIncrement ) {
    this( nPointPos, pBotRange, pTopRange );

    m_bIncremented = true;
    m_pIncrement = pIncrement;
  }
  
  
  //**************************
  // Member Variables
  //**************************
  
  protected int m_nValue;
  protected int m_nPointPos;
  protected int m_nPointCorrectionFactor; // 10 ** m_nPointPos
  
  protected boolean m_bRanged;
  protected Number  m_pBottomRange;
  protected Number  m_pTopRange;

  protected boolean m_bIncremented;
  protected Number  m_pIncrement;


  //**************************
  // Static Helper Methods
  //**************************

  private int calculateCorrectionFactor( int pos ) {

    int rslt = 1;

    for( int i = 0; i < pos; i++ ) {
      rslt *= 10;
    }

    return rslt;
  }

  private int correctNumber( Number pN ) {

    return (int)Math.round( pN.doubleValue() * m_nPointCorrectionFactor );
  }


  //**************************
  // ValueSpace Public Methods
  //**************************

  public String getName() { return "fixed point"; }
  public int    getSpace() { return ValueSpace.FIXED_PT_SPACE; }
  
  public Object  getValue() { return new Double( (double)m_nValue / (double)m_nPointCorrectionFactor ); }
  
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
	
    int nBR = correctNumber( m_pBottomRange );
    int nTR = correctNumber( m_pTopRange );
    int nIc = correctNumber( m_pIncrement );

    if ( val instanceof java.lang.Number ) {

      Number value = (Number)val;
      int intValue = (int)Math.round( value.doubleValue() * m_nPointCorrectionFactor );

      boolean rng = !m_bRanged || 
	                  ( intValue >= nBR &&
			    intValue <= nTR ); 
      
      boolean incr = !m_bIncremented ||
	                  ( Math.IEEEremainder( (double)intValue - 
						(double)nBR, 
						(double)nIc ) 
			    == 0 );

      return rng && incr;
    }

    return false;
  }
  
  public void  setValue( Object value )
	throws edu.cmu.hcii.puc.types.SpaceMismatchException {
	  
      if ( value instanceof java.lang.String ) {
	  m_nValue = correctNumber( (Double)convertString( (String)value ) );
      }
      else if ( validateNoStrChk( value ) ) {
	  m_nValue = correctNumber( (Number)value );
      }
      else throw new SpaceMismatchException( "Not an Fixed Pt value or not within correct range." );
  }

  protected Object convertString( String value ) 
    throws edu.cmu.hcii.puc.types.SpaceMismatchException
  {
    try {
      Double d = new Double( (String)value );
      if ( validateNoStrChk( d ) ) {
	return d;
      }
      else throw new SpaceMismatchException( "Not an FixedPt value or not within correct range." );
    }
    catch( Throwable t ) {
      throw new SpaceMismatchException( "Not an FixedPt value within string." );
    }
  }

  public int compareValues( ValueSpace pVS ) {
      
      int pVSValue = ((FixedPtSpace)pVS).m_nValue;
      
      if ( m_nValue == pVSValue ) return 0;
      else if (  pVSValue > m_nValue ) return 1;
      else return -1;
  }
    

  //**************************
  // FixedPtSpace Public Methods
  //**************************

  public boolean isRanged() { return m_bRanged; }

  public Number getBottomRange() { return m_pBottomRange; }

  public Number getTopRange() { return m_pTopRange; }

  public boolean isIncremented() { return m_bIncremented; }

  public Number getIncrement() { return m_pIncrement; }
  
  public int getPointPosition() { return m_nPointPos; }


  //**************************
  // Clone Method
  //**************************

  public Object clone() {

	FixedPtSpace cln = new FixedPtSpace( this.m_nPointPos );

	cln.m_nValue = this.m_nValue;
	cln.m_bRanged = this.m_bRanged;
	cln.m_pBottomRange = this.m_pBottomRange;
	cln.m_pTopRange = this.m_pTopRange;
	cln.m_bIncremented = this.m_bIncremented;
	cln.m_pIncrement = this.m_pIncrement;

	return cln;
  }


  //**************************
  // Static Testing Method
  //**************************

  public static void main( String[] args ) {

    System.out.println( "FixedPtSpace Test Sequence" );
    System.out.println( "-------------------------" );

    FixedPtSpace regFxd = new FixedPtSpace( 1 );
    FixedPtSpace rngFxd = new FixedPtSpace( 1, new Double(0), 
					       new Double(10) );
    FixedPtSpace incFxd = new FixedPtSpace( 1, new Double(0), 
					       new Double(12), 
					       new Double(0.3) );

    int test = 0;

    /* Test #1 */
    test++; 
    try {
      regFxd.setValue( new Double( 5.3 ) );

      if ( ((Double)regFxd.getValue()).doubleValue() == 5.3 ) {
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
      rngFxd.setValue( new Double( 5.3 ) );

      if ( ((Double)rngFxd.getValue()).doubleValue() == 5.3 ) {
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
      incFxd.setValue( new Double( 5.3 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)incFxd.getValue()).doubleValue() == 0 ) {
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
      incFxd.setValue( new Double( 5.1 ) );

      if ( ((Double)incFxd.getValue()).doubleValue() == 5.1 ) {
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
      rngFxd.setValue( new Double( 11 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)rngFxd.getValue()).doubleValue() == 5.3 ) {
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
      regFxd.setValue( new Double( 11 ) );

      if ( ((Double)regFxd.getValue()).doubleValue() == 11 ) {
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
      incFxd.setValue( new Double( 13 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)incFxd.getValue()).doubleValue() == 5.1 ) {
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
      incFxd.setValue( new Double( 15 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)incFxd.getValue()).doubleValue() == 5.1 ) {
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
      incFxd.setValue( new Double( 12 ) );

      if ( ((Double)incFxd.getValue()).doubleValue() == 12 ) {
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
      regFxd.setValue( new Boolean( true ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      System.out.println( "Passed #" + test );
    }

    FixedPtSpace clnFxd = (FixedPtSpace)incFxd.clone();

    /* Test #11 */
    test++;
    try {
      clnFxd.setValue( new Double( 5.3 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)clnFxd.getValue()).doubleValue() == 12 ) {
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
      clnFxd.setValue( new Double( 6 ) );

      if ( ((Double)clnFxd.getValue()).intValue() == 6 ) {
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
      clnFxd.setValue( new Double( 11 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)clnFxd.getValue()).doubleValue() == 6 ) {
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
      clnFxd.setValue( new Double( 15 ) );
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)clnFxd.getValue()).doubleValue() == 6 ) {
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
      clnFxd.setValue( new Double( 12 ) );

      if ( ((Double)clnFxd.getValue()).doubleValue() == 12 ) {
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
      clnFxd.setValue( "12" );

      if ( ((Double)clnFxd.getValue()).doubleValue() == 12 ) {
	System.out.println( "Passed #" + test );
      }    
      else throw new NullPointerException( "dumb" );
    }
    catch( Throwable t ) {
	t.printStackTrace();
      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }

    /* Test #17 */
    test++;
    try {
      clnFxd.setValue( "abcd" );

      System.out.println( "Error setting value #" + test );
      System.exit(-1);
    }
    catch( Throwable t ) {
      if ( ((Double)clnFxd.getValue()).doubleValue() == 12 ) {
	System.out.println( "Passed #" + test );
      }
      else {
	System.out.println( "Error setting value #" + test );
	System.exit(-1);
      }
    }


    System.out.println( "FixedPtSpace PASSED" );
  }
}
