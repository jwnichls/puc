/**
 * VerticalNoOverlapPanelsNode.java
 *
 * This node represents two non-overlapping panels that share a
 * vertical division between them.  Additional panels added beyond the
 * first two will be ignored.
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
import edu.cmu.hcii.puc.cio.VerticalLineCIO;


// Class Definition

public class VerticalNoOverlapPanelsNode extends MultiplePanelNode {

    //**************************
    // Member Variables
    //**************************

    protected int m_nLineLoc;


    //**************************
    // Constructor
    //**************************

    public VerticalNoOverlapPanelsNode( PanelNode pVPanel,
					InterfaceNode pHPanel ) {
	super();

	pVPanel.insertAsParent( this );
	addPanel( pHPanel );

	pVPanel.setVertical( true );
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

	InterfaceNode pNode = (InterfaceNode)m_vPanels.elementAt( 0 );
	Dimension d = pNode.getPreferredSize( pVars );

	int nFirstWidth = d.width;
	
	pNode.setSize( d.width,
		       m_rBounds.height );
	pNode.setLocation( m_rBounds.x,
			   m_rBounds.y );
	pNode.doSizing( pVars );

	pNode = (InterfaceNode)m_vPanels.elementAt( 1 );
	d = pNode.getPreferredSize( pVars );
	
	pNode.setSize( m_rBounds.width - nFirstWidth,
		       m_rBounds.height );
	m_nLineLoc = m_rBounds.x + nFirstWidth;
	pNode.setLocation( m_nLineLoc + 1,
			   m_rBounds.y );
	pNode.doSizing( pVars );
    }

    public void doLayout( ContainerCIO pContainer, 
			  LayoutVariables pVars ) {
	
	VerticalLineCIO pVLine = new VerticalLineCIO();
	pVLine.getWidget().setSize( 1, m_rBounds.height );
	pVLine.getWidget().setLocation( m_nLineLoc, 0 );
	pContainer.addCIO( pVLine );

	Enumeration e = m_vPanels.elements();
	while( e.hasMoreElements() ) {
	    InterfaceNode pNode = (InterfaceNode)e.nextElement();

	    pNode.doLayout( pContainer, pVars );
	}
    }

    public String toString() {

	return "VerticalNoOverlapPanelsNode - " + super.toString();
    }
}
