/**
 * CheckboxLinkedCIO.java
 *
 * A sub-class of StateLinkedCIO.
 *
 * The concrete representation of a Button widget.
 *
 * Revision History
 * ----------------
 * 10/02/2001: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import com.maya.puc.common.Message;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.Globals;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.awt.Checkbox;

import edu.cmu.hcii.puc.registry.WidgetRegistry;

import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.Panel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;




// Class Definition

public class CheckboxLinkedCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static
    //**************************

    static class CheckboxLinkedCIOFactory implements CIOFactory {

        public ConcreteInteractionObject createCIO(Appliance a,
                                                   ApplianceObject ao) {

            return new CheckboxLinkedCIO(a, ao);
        }
    }

    static {
        // register the factory with the WidgetRegistry
        WidgetRegistry.addCIOFactory("CheckboxLinkedCIO", new CheckboxLinkedCIOFactory());
    }


    //**************************
    // Member Variables
    //**************************


    //**************************
    // Constructor
    //**************************

    public CheckboxLinkedCIO(Appliance appl, ApplianceObject applObj) {

        super(appl, applObj, null);

        m_Widget = new Checkbox( this );

        ((ApplianceState) m_ApplObj).addStateListener(new StateListener() {

            public void enableChanged(ApplianceObject obj) {
                ((Checkbox)m_Widget).setEnabled(m_ApplObj.isEnabled());
            }

	    public void labelChanged( ApplianceObject obj ) {
		refreshDisplay();
	    }

            public void typeChanged(ApplianceState obj) {
                refreshDisplay();
            }

            public void valueChanged(ApplianceState obj) {
                refreshDisplay();
            }
        });

        ((Checkbox) m_Widget).addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                Message msg = new Message.StateChangeRequest(CheckboxLinkedCIO.this.m_ApplObj.m_sName, ((Checkbox) m_Widget).getState() + "");

                try {
                    CheckboxLinkedCIO.this.m_Appliance.m_pConnection.send(msg);
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

        if (m_ApplObj.m_Labels != null) {
            try {
                FontMetrics fm = Globals.getFontMetricsObj(m_Widget.getFont());

                lbl = m_ApplObj.m_Labels.getLabelByPixelLength(fm, m_Widget.getSize().width);
            } catch (Throwable t) {
                lbl = m_ApplObj.m_Labels.getLabelByCharLength(10);
            }
        } else
            lbl = m_ApplObj.m_sName;

        ((Checkbox) m_Widget).setLabel(lbl);
        ((Checkbox) m_Widget).setState(((Boolean) ((ApplianceState) m_ApplObj).m_Type.getValueSpace().getValue()).booleanValue());
    }


    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() {

	((Checkbox)m_Widget).setLabel( m_ApplObj.m_Labels.getShortestLabel() );
    }

    public void addNotify() {

        refreshDisplay();
    }
}
