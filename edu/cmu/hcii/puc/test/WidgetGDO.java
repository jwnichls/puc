/**
 * GadgetPUCLayout.java
 *
 * An experiment in using the gadget toolkit to automatically layout
 * dialog-box style interfaces.  Hopefully this will be the basis for
 * new layout phase in the PUC system.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.test;


// Import Declarations

import java.awt.Component;

import edu.cmu.hcii.jfogarty.gadget.displayobject.basicshape.FilledRectangle;


// Class Definition

public class WidgetGDO extends FilledRectangle {

    //**************************
    // Member Variables
    //**************************

    protected Component m_pWidget;


    //**************************
    // Constructor
    //**************************

    public WidgetGDO( Component pC ) {

	super( pC.getLocation().x + ( pC.getSize().width / 2 ), 
	       pC.getLocation().y + ( pC.getSize().height / 2 ), 
	       pC.getSize().width, 
	       pC.getSize().height );

	m_pWidget = pC;
    }


    //**************************
    // Constructor
    //**************************

    public Component getWidget() {

	return m_pWidget;
    }
}
