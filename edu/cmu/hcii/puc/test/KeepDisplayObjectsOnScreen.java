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

public class KeepDisplayObjectsOnScreen extends MinimumMinimizationEvaluation
{
    private   GadgetDisplayObjectList     m_gdolList;
    private   Rectangle                   m_rBounds;

    public KeepDisplayObjectsOnScreen(GadgetDisplayObjectList gdolList, 
				       Rectangle rBounds ) {
	
	m_gdolList = gdolList;
	m_rBounds = rBounds;
    }
    
    /**
     * Override this method to create the result of the evaluation.
     **/
    public GadgetEvaluationResult doEvaluate() {
	double[]                            dOffscreenArea;
	GadgetDisplayObject                 gdoRef;
	GadgetDisplayObject                 gdoTest;
	int                                 iNumObjects;
	
	iNumObjects = m_gdolList.size();
	dOffscreenArea = new double[iNumObjects];
	
	// Go through each object 
	for( int i = 0; i < iNumObjects; i++) {
	    
	    Rectangle rItemBounds = m_gdolList.get( i ).getBounds();
	    double dItemArea = rItemBounds.width * rItemBounds.height;

	    Rectangle rOnScreen = rItemBounds.intersection( m_rBounds );
	    double dOnScreenArea = Math.abs( rOnScreen.width * rOnScreen.height );

	    dOffscreenArea[ i ] = dItemArea - dOnScreenArea;
	}
	/*
	for( int i = 0; i < iNumObjects; i++ ) {
	    // print the totals
	    System.out.println( "Area " + i + " is " + dOverlapArea[ i ] ); 
	}
	*/
	return new GadgetEvaluationResult(this, dOffscreenArea);
    }
}



