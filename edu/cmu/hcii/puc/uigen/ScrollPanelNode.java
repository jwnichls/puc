/**
 * ScrollPanelNode.java
 *
 * This node represents nodes that represent a single panel with
 * built-in scrollbars that appear automatically. 
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Panel;

import java.util.Enumeration;
import java.util.Vector;

import edu.cmu.hcii.puc.cio.ContainerCIO;
import edu.cmu.hcii.puc.cio.ScrollPanelCIO;


// Class Definition

public class ScrollPanelNode extends PanelNode {

    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public ScrollPanelNode() {

	m_pPanel = new ScrollPanelCIO();
    }

    //**************************
    // Member Methods
    //**************************

    //**************************
    // Interface Methods
    //**************************

    public String toString() {
	String ret = "ScrollPanelNode: - " + m_pPanel.getWidget().getBounds().toString() + "\n";

	Component c = m_pPanel.getWidget();
	while( c.getParent() != null ) {
	    c = c.getParent();
	}

	for( int i = 0; i < m_vRows.size(); i++ ) {
	    Row r = (Row)m_vRows.elementAt( i );

	    ret += r.toString();
	}

	return ret;
    }
}
