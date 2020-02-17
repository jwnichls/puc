/**
 * OverlappingPanelsNode.java
 *
 * This node represents nodes that branch to multiple panels.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Dimension;
import java.awt.Rectangle;

import java.util.Enumeration;
import java.util.Vector;

import edu.cmu.hcii.puc.cio.ContainerCIO;


// Class Definition

public class OverlappingPanelsNode extends MultiplePanelNode {

    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public OverlappingPanelsNode() {
	super();
    }

    //**************************
    // Member Methods
    //**************************

    //**************************
    // Interface Methods
    //**************************

    public Dimension getPreferredSize( LayoutVariables pVars ) {
	Dimension ret = new Dimension();
	
	Enumeration e = m_vPanels.elements();
	while( e.hasMoreElements() ) {
	    Dimension d = ((InterfaceNode)e.nextElement()).getPreferredSize( pVars );
	    
	    if ( d.width > ret.width )
		ret.width = d.width;

	    if ( d.height > ret.height )
		ret.height = d.height;
	}

	return ret;
    }

    public void addComponents( ContainerCIO pContainer ) {

	Enumeration e = m_vPanels.elements();
	while( e.hasMoreElements() )
	    ((InterfaceNode)e.nextElement()).addComponents( pContainer );
    }

    public void doSizing( LayoutVariables pVars ) {

	Enumeration e = m_vPanels.elements();
	while( e.hasMoreElements() ) {
	    InterfaceNode pNode = (InterfaceNode)e.nextElement();

	    Rectangle rBounds = (Rectangle)m_rBounds.clone();

	    if (! pVars.m_bConstrainPanelsToScreenH ) {
		rBounds.width = pNode.getPreferredSize( pVars ).width;
	    }
	    if (! pVars.m_bConstrainPanelsToScreenV ) {
		rBounds.height = pNode.getPreferredSize( pVars ).height;
	    }

	    pNode.setSize( rBounds.width,
			   rBounds.height );
	    pNode.setLocation( rBounds.x,
			       rBounds.y );

	    pNode.doSizing( pVars );
	}
    }

    public void doLayout( ContainerCIO pContainer, 
			  LayoutVariables pVars ) {
	
	Enumeration e = m_vPanels.elements();
	while( e.hasMoreElements() ) {
	    InterfaceNode pNode = (InterfaceNode)e.nextElement();

	    pNode.doLayout( pContainer, pVars );
	}
    }

    public String toString() {

	return "OverlappingPanelsNode - " + super.toString();
    }
}
