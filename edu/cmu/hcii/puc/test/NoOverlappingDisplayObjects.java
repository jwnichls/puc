/**
 * 3/3/2002
 * Created class.  
 *
 * @author Jeffrey Nichols
 **/

// Package Definitions

package edu.cmu.hcii.puc.test;

// Import Declarations

import edu.cmu.hcii.jfogarty.gadget.displayobject.GadgetDisplayObject;
import edu.cmu.hcii.jfogarty.gadget.evaluation.GadgetEvaluationResult;
import edu.cmu.hcii.jfogarty.gadget.evaluation.MinimumMinimizationEvaluation;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectList;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectListIterator;

import java.awt.Rectangle;

import java.util.Vector;


// Class Definition

public class NoOverlappingDisplayObjects extends MinimumMinimizationEvaluation
{
    private   GadgetDisplayObjectList     m_gdolList;

    public NoOverlappingDisplayObjects(GadgetDisplayObjectList gdolList) {
	
	m_gdolList = gdolList;
    }
    
    /**
     * Override this method to create the result of the evaluation.
     **/
    public GadgetEvaluationResult doEvaluate() {
	double                              dOverlap;
	double[]                            dOverlapArea;
	GadgetDisplayObject                 gdoRef;
	GadgetDisplayObject                 gdoTest;
	int                                 iNumObjects;
	
	iNumObjects = m_gdolList.size();
	dOverlapArea = new double[iNumObjects];
	
	// Go through each object 
	for( int i = 0; i < iNumObjects; i++) {

	    gdoRef = m_gdolList.get( i );

	    for( int j = i+1; j < iNumObjects; j++ ) {

		gdoTest = m_gdolList.get( j );
	    
		Rectangle rIntersect = gdoRef.getBounds().intersection( gdoTest.getBounds() );
		int nArea = Math.abs( rIntersect.width * rIntersect.height );
		
		dOverlapArea[ i ] += nArea;
		dOverlapArea[ j ] += nArea;

		//System.out.println( "Area " + i + "x" + j + " is " + nArea );
	    }
	}
	/*
	for( int i = 0; i < iNumObjects; i++ ) {
	    // print the totals
	    System.out.println( "Area " + i + " is " + dOverlapArea[ i ] ); 
	}
	*/
	return new GadgetEvaluationResult(this, dOverlapArea);
    }
}



