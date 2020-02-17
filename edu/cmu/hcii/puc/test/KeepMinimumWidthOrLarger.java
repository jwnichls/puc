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
import edu.cmu.hcii.jfogarty.gadget.evaluation.MinimumMinimizationEvaluation;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectList;
import edu.cmu.hcii.jfogarty.gadget.util.GadgetDisplayObjectListIterator;

import java.awt.Rectangle;

import java.util.Vector;


// Class Definition

public class KeepMinimumWidthOrLarger extends MinimumMinimizationEvaluation
{
    private   GadgetDisplayObject         m_gdoItem;
    protected double                      m_dMinimumWidth;


    public KeepMinimumWidthOrLarger(GadgetDisplayObject gdoItem, 
				    double dMinimumWidth
				    ) {
	
	m_gdoItem = gdoItem;
	m_dMinimumWidth = dMinimumWidth;
    }

    /**
     * Override this method to create the result of the evaluation.
     **/
    public GadgetEvaluationResult doEvaluate() {
	double[]                            dWidthOffset;

	dWidthOffset = new double[ 1 ];
	
	Rectangle rItemBounds = m_gdoItem.getBounds();
	
	double d = m_dMinimumWidth - rItemBounds.width;
	
	if ( d > 0 )
	    dWidthOffset[ 0 ] = d;

	return new GadgetEvaluationResult(this, dWidthOffset);
    }
}

