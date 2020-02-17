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

public class KeepDisplayObjectAboveAnother extends MinimumMinimizationEvaluation
{
    protected GadgetDisplayObject m_gdoUpper;
    protected GadgetDisplayObject m_gdoLower;
    
    public KeepDisplayObjectAboveAnother(GadgetDisplayObject gdoUpper,
					 GadgetDisplayObject gdoLower ) {
	
	m_gdoUpper = gdoUpper;
	m_gdoLower = gdoLower;
    }
    
    /**
     * Override this method to create the result of the evaluation.
     **/
    public GadgetEvaluationResult doEvaluate() {
	double[] dHeight = new double[ 1 ];
	
	Rectangle rUpper = m_gdoUpper.getBounds();
	Rectangle rLower = m_gdoLower.getBounds();
	
	double d = ( rUpper.y + rUpper.height ) - rLower.y;

	if ( d > 0 )
	    dHeight[ 0 ] = d;

	return new GadgetEvaluationResult(this, dHeight);
    }
}



