/**
 * 3/4/2002
 * Created class.  
 *
 * @author Jeffrey Nichols
 **/

// Package Definitions

package edu.cmu.hcii.puc.test;

// Import Declarations

import edu.cmu.hcii.jfogarty.gadget.displayobject.GadgetDisplayObject;
import edu.cmu.hcii.jfogarty.gadget.evaluation.GadgetEvaluationResult;
import edu.cmu.hcii.jfogarty.gadget.evaluation.ScoreMinimizationEvaluation;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectList;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectListIterator;

import java.awt.Rectangle;

import java.util.Vector;


// Class Definition

public class AlignDisplayObjectsToGrid extends ScoreMinimizationEvaluation
{
    private   GadgetDisplayObjectList     m_gdolList;
    protected int                         m_nCols;
    protected int                         m_nColWidth;
    protected int                         m_nColMargin;
    protected int                         m_nTopY;
    protected int                         m_nBotY;
    protected double                      m_dThreshold;

    protected int                         m_nGridColumns[];


    public AlignDisplayObjectsToGrid(GadgetDisplayObjectList gdolList, 
				     int nCols,
				     int nColWidth,
				     int nColMargin,
				     int nTopY,
				     int nBotY,
				     double dThreshold
				     ) {
	
	m_gdolList = gdolList;
	m_nCols = nCols;
	m_nColWidth = nColWidth;
	m_nColMargin = nColMargin;
	m_nTopY = nTopY;
	m_nBotY = nBotY;
	m_dThreshold = dThreshold;

	computeGridLocations();
    }
    
    protected void computeGridLocations() {

	m_nGridColumns = new int[ m_nCols * 2 ];
	
	int nCurrentCol = m_nColMargin;
	for( int i = 0; i < m_nCols * 2; i += 2 ) {
	    m_nGridColumns[ i ] = nCurrentCol;
	    m_nGridColumns[ i+1 ] = nCurrentCol + m_nColWidth;

	    nCurrentCol += m_nColWidth + m_nColMargin;
	}
    }

    protected int findClosestGridColumn( int nX ) {

	int nCenter = m_nGridColumns[ 0 ] + 
	    ( m_nGridColumns[ 1 ] - m_nGridColumns[ 0 ] ) / 2;
	long nBestDiff = Math.abs( nX - nCenter );

	for( int i = 2; i < m_nCols * 2; i += 2 ) {
	    nCenter = m_nGridColumns[ i ] + 
		( m_nGridColumns[ i+1 ] - m_nGridColumns[ i ] ) / 2;

	    int nDiff = Math.abs( nX - nCenter );
	    
	    if ( nDiff > nBestDiff )
		return i-2;
	}
	
	return m_nGridColumns.length-2;
    }

    /**
     * Override this method to create the result of the evaluation.
     **/
    public GadgetEvaluationResult doEvaluate() {
	double[]                            dGridOffset;
	GadgetDisplayObject                 gdoTest;
	int                                 iNumObjects;
	
	iNumObjects = m_gdolList.size();
	dGridOffset = new double[iNumObjects];
	
	// Go through each object 
	for( int i = 0; i < iNumObjects; i++) {
	    
	    Rectangle rItemBounds = m_gdolList.get( i ).getBounds();
	    int nLeftEdge = rItemBounds.x;
	    int nRightEdge = rItemBounds.x + rItemBounds.width;
	    int nCenter = rItemBounds.x + rItemBounds.width / 2;

	    int nGridIdx = findClosestGridColumn( nCenter );

	    int nLeftColEdge = m_nGridColumns[ nGridIdx ];
	    int nRightColEdge = m_nGridColumns[ nGridIdx + 1 ];
	    int nCenterCol = nLeftColEdge + 
		( nRightColEdge - nLeftColEdge ) / 2;

	    int nTopOffset = m_nTopY - rItemBounds.y;
	    nTopOffset = nTopOffset > 0 ? nTopOffset : 0;

	    int nBottomOffset = ( rItemBounds.y + rItemBounds.width ) - m_nBotY;
	    nBottomOffset = nBottomOffset > 0 ? nBottomOffset : 0;

	    dGridOffset[ i ] = Math.abs( nLeftEdge - nLeftColEdge ) + Math.abs( nRightEdge - nRightColEdge ) + nTopOffset + nBottomOffset;


	    /* + Math.abs( nCenter - nCenterCol );*/
	}

	return new GadgetEvaluationResult(this, dGridOffset);
    }
}



