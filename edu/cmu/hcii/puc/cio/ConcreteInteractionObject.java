/**
 * ConcreteInteractionObject.java
 *
 * Concrete Interaction Objects (CIOs), are the basis of the
 * instantiated interface that is constructed by the personal
 * universal controller.  Each state and command from the
 * specification are mapped to a CIO, which in turn is linked to the
 * actual widget that appears in the interface.
 *
 * CIOs are not always mapped to states or commands from the
 * specification however.  CIOs may also be widgets that represent
 * organizational elements within the interface.  Tabs, like the ones
 * seen in our prototype phone interface, are an example of an
 * organizational CIO that is not linked to the actual state of the
 * phone. 
 *
 * These different types of CIOs are represented by sub-classes.  The
 * basic ConcreteInteractionObject cannot instantiated directly.  It
 * can only be instantiated as the super-class of something else.
 *
 * NOTE: Most CIOs are dynamically loaded by the WidgetPicker class.
 * Unless you have a special reason, any new CIO should register a
 * CIOFactory with the WidgetPicker when its class is loaded.
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.lang.*;


// Class Definition

public abstract class ConcreteInteractionObject extends Object {

    //**************************
    // Member Variables
    //**************************

    protected Component       m_Widget;


    //**************************
    // Constructor
    //**************************

    protected ConcreteInteractionObject( Component widget ) {

	m_Widget = widget;
    }


    //**************************
    // Member Methods
    //**************************

    public Component getWidget() {

	return m_Widget;
    }

    public void validate() {

	m_Widget.validate();
    }

    public void invalidate() {

	m_Widget.invalidate();
    }

    public void setVisible( boolean v ) {
	
	m_Widget.setVisible( v );
    }

    public boolean hasLabel() { return false; }
    public ConcreteInteractionObject getLabelCIO() { return null; }

    public Dimension getPreferredSize() { return m_Widget.getPreferredSize(); }
    public Dimension getMinimumSize() { return m_Widget.getMinimumSize(); }

    public boolean prefersFullWidth() { return false; }

    /**
     * addNotify()
     * 
     * Used to informed components when their UI components have been
     * instantiated and it is possible to calculate accurate widths
     * for labels, etc.
     */
    public abstract void addNotify();
}
