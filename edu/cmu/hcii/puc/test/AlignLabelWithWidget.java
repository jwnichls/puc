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

public class AlignLabelWithWidget extends MinimumMinimizationEvaluation
{
    protected GadgetDisplayObject m_gdoLabel;
    protected GadgetDisplayObject m_gdoWidget;
    protected double              m_dThreshold;
    
    public AlignLabelWithWidget(GadgetDisplayObject gdoLabel,
				GadgetDisplayObject gdoWidget,
				double dThreshold ) {
	
	m_gdoLabel = gdoLabel;
	m_gdoWidget = gdoWidget;
	m_dThreshold = dThreshold;
    }
    
    /**
     * Override this method to create the result of the evaluation.
     **/
    public GadgetEvaluationResult doEvaluate() {
	double[] dAlign = new double[ 1 ];
	
	Rectangle rLabel = m_gdoLabel.getBounds();
	Rectangle rWidget = m_gdoWidget.getBounds();
	
	double d = Math.abs( rLabel.y - rWidget.y );

	if ( d >= m_dThreshold )
	    dAlign[ 0 ] = d;

	d = Math.abs( rLabel.height - rWidget.height );
	
	if ( d >= m_dThreshold )
	    dAlign[ 0 ] += d;

	d = rLabel.x + rLabel.width - rWidget.x;
	
	if ( d > 0 )
	    dAlign[ 0 ] += d;

	return new GadgetEvaluationResult(this, dAlign);
    }
}



