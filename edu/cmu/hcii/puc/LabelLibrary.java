/**
  * LabelLibrary.java
  * 
  * The LabelLibrary is a container for sets of similar labels.  It
  * provides an interface that the PUC can use to find the most
  * appropriate label.
  *
  * Revision History:
  * -----------------
  * 07/23/2001: (JWN) Created file and implemented initial interface.
  *
  */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.awt.FontMetrics;

import java.lang.*;

import java.util.Vector;

import edu.cmu.hcii.puc.types.EnableConstraint;
import edu.cmu.hcii.puc.types.PUCString;


// Class Definition

public class LabelLibrary extends Object {

  //**************************
  // Member Variables
  //**************************

  protected Vector m_vLabels;
  protected EnableConstraint m_bEnable;

  //**************************
  // Constructor
  //**************************

  public LabelLibrary( Vector vLabels, EnableConstraint bEnable ) {

    m_vLabels = (Vector)vLabels.clone();
    m_bEnable = bEnable;
  }

  public LabelLibrary( Vector vLabels ) {

    this( vLabels, null );
  }

  public LabelLibrary( EnableConstraint bEnable ) {

    this( new Vector(), bEnable );
  }

  public LabelLibrary() {

    this( new Vector() );
  }


  //**************************
  // Public Member Methods
  //**************************

  public void addLabel( PUCString lbl ) {

    m_vLabels.addElement( lbl );
  }

  public boolean getEnabled() {

    return m_bEnable == null || m_bEnable.booleanValue();
  }

  public String getLabelByPixelLength( FontMetrics fm, int nPixels ) {

    if ( m_vLabels.size() == 0 ) { throw new  // shouldn't ever happen
				     ArrayIndexOutOfBoundsException(); };

    int nClosestIdx = 0;
    int nClosest = Math.abs(
		     fm.stringWidth(m_vLabels.elementAt( 0 ).toString()) -
	             nPixels
		   );
    boolean bFoundShorter = false;

    for( int i = nClosestIdx; i < m_vLabels.size(); i++ ) {
      int nDif = fm.stringWidth(m_vLabels.elementAt( i ).toString()) -
	             nPixels; 

      if ( nDif > 0 ) continue;

      nDif = Math.abs( nDif );

      if ( ( !bFoundShorter ) || ( nDif < nClosest ) ) {
	nClosest = nDif;
	nClosestIdx = i;
	bFoundShorter = true;
      }
    }

    return m_vLabels.elementAt( nClosestIdx ).toString();
  }

  public String getShortestLabel() {

    if ( m_vLabels.size() == 0 ) { throw new // shouldn't even happen
	                             ArrayIndexOutOfBoundsException(); }

    int nShortestLength = m_vLabels.elementAt( 0 ).toString().length();
    int nShortestIndex = 0;

    for( int i = 1; i < m_vLabels.size(); i++ ) {
      int nStringLength = m_vLabels.elementAt( i ).toString().length();

      if ( nShortestLength > nStringLength ) {
	nShortestLength = nStringLength;
	nShortestIndex = i;
      }
    }

    return m_vLabels.elementAt( nShortestIndex ).toString();
  }

  public String getLabelByCharLength( int nChars ) {

    if ( m_vLabels.size() == 0 ) { throw new  // shouldn't ever happen
				     ArrayIndexOutOfBoundsException(); };

    int nClosestIdx = 0;
    int nClosest = Math.abs( 
		     m_vLabels.elementAt( 0 ).toString().length() -
	             nChars
		   );
    boolean bFoundShorter = false;

    for( int i = 1; i < m_vLabels.size(); i++ ) {
      int nDif = m_vLabels.elementAt( i ).toString().length() -
	           nChars;

      if ( nDif > 0 ) continue;

      nDif = Math.abs( nDif );

      if ( ( !bFoundShorter ) || ( nDif < nClosest ) ) {
	nClosest = nDif;
	nClosestIdx = i;
	bFoundShorter = true;
      }
    }

    return m_vLabels.elementAt( nClosestIdx ).toString();
  }

  public void setEnableConstraint( EnableConstraint bEnable ) {

    m_bEnable = bEnable;
  }

  public EnableConstraint getEnableConstraint() {

    return m_bEnable;
  }

  public String getFirstLabel() {

    return m_vLabels.elementAt( 0 ).toString();
  }
}
