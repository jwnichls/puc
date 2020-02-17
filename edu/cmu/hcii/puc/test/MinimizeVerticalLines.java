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

public class MinimizeVerticalLines extends ScoreMinimizationEvaluation
{
    private   GadgetDisplayObjectList     m_gdolList;
    private   double                      m_dThreshold;

    public MinimizeVerticalLines(GadgetDisplayObjectList gdolList,
				 double dThreshold ) {
	
	m_gdolList = gdolList;
	m_dThreshold = dThreshold;
    }

    /**
     * Override this method to create the result of the evaluation.
     **/
    public GadgetEvaluationResult doEvaluate() {
	double[]                            dNewLineCount;
	int                                 iNumObjects;
	double[]                            dLeftLines;
	double[]                            dRightLines;
	int                                 dLeftLinesLength;
	int                                 dRightLinesLength;
	Rectangle                           rItemBounds;

	iNumObjects = m_gdolList.size();
	dNewLineCount = new double[iNumObjects];
	dLeftLines = new double[ iNumObjects ];
	dRightLines = new double[ iNumObjects ];

	rItemBounds = m_gdolList.get( 0 ).getBounds();
	dLeftLines[ 0 ] = rItemBounds.x;
	dLeftLinesLength = 1;
	dRightLines[ 0 ] = rItemBounds.x + rItemBounds.width;
	dRightLinesLength = 1;
	dNewLineCount[ 0 ] = 2;
	
	
	// Go through each object 
	for( int i = 1; i < iNumObjects; i++) {
	    
	    boolean bAddLine1 = true;
	    boolean bAddLine2 = true;

	    rItemBounds = m_gdolList.get( i ).getBounds();
	    int nRightLine = rItemBounds.x + rItemBounds.width;

	    for( int j = 0; j < dLeftLinesLength; j++ ) {
		if ( Math.abs( rItemBounds.x - dLeftLines[ j ] ) < m_dThreshold )
		    bAddLine1 = false;
	    }

	    for( int j = 0; j < dRightLinesLength; j++ ) {
		if ( Math.abs( nRightLine - dRightLines[ j ] ) < m_dThreshold )
		    bAddLine2 = false;
	    }
	    
	    if ( bAddLine1 ) {
		dLeftLines[ dLeftLinesLength ] = rItemBounds.x;
		dLeftLinesLength++;
		dNewLineCount[ i ]++;
	    }

	    if ( bAddLine2 ) {
		dRightLines[ dRightLinesLength ] = nRightLine;
		dRightLinesLength++;
		dNewLineCount[ i ]++;
	    }
	}

	return new GadgetEvaluationResult(this, dNewLineCount);
    }
}



