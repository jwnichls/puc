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
import edu.cmu.hcii.puc.cio.LineCIO;


// Class Definition

public class NonOverlappingPanelsNode extends MultiplePanelNode {

    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public NonOverlappingPanelsNode() {
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
	    
	    ret.height += d.height;
	    
	    if ( d.width > ret.width ) {
		ret.width = d.width;
	    }
	}

	return ret;
    }

    public void addComponents( ContainerCIO pContainer ) {

	Enumeration e = m_vPanels.elements();
	while( e.hasMoreElements() )
	    ((InterfaceNode)e.nextElement()).addComponents( pContainer );
    }

    public void doSizing( LayoutVariables pVars ) {

	int nTopY = m_rBounds.y;

	for( int i = 0; i < m_vPanels.size(); i++ ) {
	    InterfaceNode pNode = (InterfaceNode)m_vPanels.elementAt( i );

	    Dimension d = pNode.getPreferredSize( pVars );

	    if ( pVars.m_bConstrainPanelsToScreenH ) {
		d.width = m_rBounds.width;
	    }
	    else {
		if ( d.width > m_rBounds.width )
		    m_rBounds.width = d.width;
	    }

	    if ( pVars.m_bConstrainPanelsToScreenV ) {
		if ( ( nTopY + d.height ) > m_rBounds.height )
		    d.height = m_rBounds.height - nTopY;
		if ( m_vPanels.size() == i + 1 )
		    // if this is the last iteration,
		    // allocate remaining space to last panel
		    // (probably not the right decision always)
		    d.height = m_rBounds.height - nTopY;
	    }
	    else {
		m_rBounds.height = d.height + nTopY;
	    }

	    pNode.setSize( d.width,
			   d.height );
	    pNode.setLocation( m_rBounds.x,
			       nTopY );

	    nTopY += d.height + 3;  // +3 accounts for horizontal line
	                            // and spacing 

	    pNode.doSizing( pVars );
	}
    }

    public void doLayout( ContainerCIO pContainer, 
			  LayoutVariables pVars ) {
	
	for( int i = 0; i < m_vPanels.size(); i++ ) {
	    InterfaceNode pNode = (InterfaceNode)m_vPanels.elementAt( i );

	    LineCIO pLine = new LineCIO();
	    pLine.getWidget().setSize( m_rBounds.width, 1 );
	    pLine.getWidget().setLocation( 0, pNode.getBounds().y + 
					      pNode.getBounds().height );
	    pContainer.addCIO( pLine );

	    pNode.doLayout( pContainer, pVars );
	}
    }

    public String toString() {

	return "NonOverlappingPanelsNode - " + super.toString();
    }
}
