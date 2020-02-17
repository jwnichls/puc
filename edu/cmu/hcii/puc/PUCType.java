/**
 * PUCType.java
 * 
 * An object representing the type of a state.
 *
 * Revision History:
 * -----------------
 * 07/12/2001: (JWN) Created file.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.lang.*;

import java.util.Vector;

import edu.cmu.hcii.puc.types.ValueSpace;


// Class Definition

public class PUCType implements Cloneable {

  //**************************
  // Constructors
  //**************************
  
  public PUCType( String name, ValueSpace spc, Vector lbls, ValueSpace exp ) { 

    this( spc, lbls, exp );

    m_sName = name;
  }

  public PUCType( ValueSpace spc, Vector lbls, ValueSpace exp ) { 

    m_ValueSpace = (ValueSpace)spc.clone();
    if ( exp != null )
      m_ExpectedSpace = (ValueSpace)exp.clone();
    if ( lbls != null ) 
      m_ValueLabels = (Vector)lbls.clone();

    m_sName = null;
  }
  
  public PUCType( ValueSpace spc, Vector lbls ) {
    this( spc, lbls, null );
  }

  public PUCType( ValueSpace spc ) {
    this( spc, null, null );
  }
  
  

  //**************************
  // Member Variables
  //**************************

  protected String     m_sName;
  protected ValueSpace m_ValueSpace;
  protected ValueSpace m_ExpectedSpace;
  protected Vector     m_ValueLabels;


  //**************************
  // Public Methods
  //**************************

  public String     getName() { return m_sName; }
  public ValueSpace getValueSpace() { return m_ValueSpace; }
  public ValueSpace getExpectedSpace() { return m_ExpectedSpace; }
  public Vector     getValueLabels() { return m_ValueLabels; }

  public Object clone() {

    return new PUCType( m_sName, 
			(ValueSpace)m_ValueSpace.clone(),
			m_ValueLabels,
			m_ExpectedSpace );
  }
}
