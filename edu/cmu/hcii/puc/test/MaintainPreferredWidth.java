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

public class MaintainPreferredWidth extends MinimumMinimizationEvaluation
{
    private   GadgetDisplayObject         m_gdoItem;
    protected double                      m_dPreferredWidth;
    protected double                      m_dThreshold;


    public MaintainPreferredWidth(GadgetDisplayObject gdoItem, 
				   double dPreferredWidth,
				   double dThreshold
				   ) {
	
	m_gdoItem = gdoItem;
	m_dPreferredWidth = dPreferredWidth;
	m_dThreshold = dThreshold;
    }

    /**
     * Override this method to create the result of the evaluation.
     **/
    public GadgetEvaluationResult doEvaluate() {
	double[]                            dWidthOffset;

	dWidthOffset = new double[ 1 ];
	
	Rectangle rItemBounds = m_gdoItem.getBounds();
	
	double d = Math.abs( rItemBounds.width - m_dPreferredWidth );
	
	if ( d > m_dThreshold )
	    dWidthOffset[ 0 ] = d;

	return new GadgetEvaluationResult(this, dWidthOffset);
    }
}

