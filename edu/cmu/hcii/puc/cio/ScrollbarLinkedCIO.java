/**
 * ScrollbarLinkedCIO.java
 *
 * A sub-class of StateLinkedCIO.
 *
 * The concrete representation of a TextField widget.
 *
 * Revision History
 * ----------------
 * 10/02/2001: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import com.maya.puc.common.Message;

import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.Panel;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.PUC;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.Scrollbar;

import edu.cmu.hcii.puc.registry.WidgetRegistry;

import edu.cmu.hcii.puc.types.FixedPtSpace;
import edu.cmu.hcii.puc.types.IntegerSpace;



// Class Definition

public class ScrollbarLinkedCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static
    //**************************

    static class ScrollbarLinkedCIOFactory implements CIOFactory {

        public ConcreteInteractionObject createCIO(Appliance a,
                                                   ApplianceObject ao) {

            return new ScrollbarLinkedCIO(a, ao);
        }
    }

    static {
        // register the factory with the WidgetRegistry
        WidgetRegistry.addCIOFactory("ScrollbarLinkedCIO", new ScrollbarLinkedCIOFactory());
    }


    //**************************
    // Member Variables
    //**************************

    long m_lCorrFac;


    //**************************
    // Constructor
    //**************************

    public ScrollbarLinkedCIO(Appliance appl, ApplianceObject applObj) {

        super(appl, applObj, null);

        m_Widget = new Scrollbar(this, Scrollbar.HORIZONTAL);

        ApplianceState s = (ApplianceState) applObj;

        if (s.m_Type.getValueSpace() instanceof FixedPtSpace) {
            FixedPtSpace fs = (FixedPtSpace) s.m_Type.getValueSpace();

            int pp = fs.getPointPosition();
            for (int i = 0; i < pp; i++) {
                m_lCorrFac *= 10;
            }
        }
	else
	    m_lCorrFac = 1;

        s.addStateListener(new StateListener() {

            public void enableChanged(ApplianceObject obj) {
                ((Scrollbar)m_Widget).setEnabled(m_ApplObj.isEnabled());
            }

	    public void labelChanged( ApplianceObject obj ) {
		refreshDisplay();
	    }

            public void typeChanged(ApplianceState obj) {
                refreshDisplay();
            }

            public void valueChanged(ApplianceState obj) {
		((Scrollbar)m_Widget).setValue((int) (((Number) ((ApplianceState)m_ApplObj).m_Type.getValueSpace().getValue()).doubleValue() * m_lCorrFac));
            }
        });

        ((Scrollbar)m_Widget).addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                Message msg = new Message.StateChangeRequest(ScrollbarLinkedCIO.this.m_ApplObj.m_sName, e.getValue() + "");

                try {
                    ScrollbarLinkedCIO.this.m_Appliance.m_pConnection.send(msg);
                } catch (Throwable t) {
                }
            }
        });

        refreshDisplay();
    }


    //**************************
    // Protected Methods
    //**************************

    protected void refreshDisplay() {

        String lbl;

        ApplianceState s = (ApplianceState) m_ApplObj;

        int val, min, max, incr = 1;

        if (s.m_Type.getValueSpace() instanceof IntegerSpace) {
            IntegerSpace is = (IntegerSpace) s.m_Type.getValueSpace();

            val = ((Integer) is.getValue()).intValue();
            min = is.getBottomRange().intValue();
            max = is.getTopRange().intValue();
            if (is.isIncremented())
                incr = is.getIncrement().intValue();
        } else if (s.m_Type.getValueSpace() instanceof FixedPtSpace) {
            FixedPtSpace fs = (FixedPtSpace) s.m_Type.getValueSpace();

            val = (int) Math.round(((Double) fs.getValue()).doubleValue() * m_lCorrFac);
            min = (int) Math.round(fs.getBottomRange().doubleValue() * m_lCorrFac);
            max = (int) Math.round(fs.getTopRange().doubleValue() * m_lCorrFac);
            if (fs.isIncremented())
                incr = (int) Math.round(fs.getIncrement().doubleValue() * m_lCorrFac);
        } else
            return;

        int pagesize = incr > 1 ? incr * 5 : 5;

        ((Scrollbar)m_Widget).setValues(val, pagesize, min, max + pagesize);

        ((Scrollbar)m_Widget).setValue((int) (((Number) s.m_Type.getValueSpace().getValue()).doubleValue() * m_lCorrFac));
    }


    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() { }

    public boolean hasLabel() { return true; }

    public ConcreteInteractionObject getLabelCIO() { 
	
	if ( m_ApplObj.m_Labels != null )
	    return new LabelCIO( m_ApplObj.m_Labels ); 
	
	return null;
    }

    public void addNotify() {

        refreshDisplay();
    }
}
