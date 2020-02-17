/**
 * TabbedOverlappingPanelsNode.java
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

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceState;

import edu.cmu.hcii.puc.cio.ContainerCIO;
import edu.cmu.hcii.puc.cio.TabbedLinkedCIO;

import edu.cmu.hcii.puc.types.EnumeratedSpace;


// Class Definition

public class TabbedOverlappingPanelsNode extends MultiplePanelNode {

    //**************************
    // Member Variables
    //**************************

    TabbedLinkedCIO m_pTabbedPanel;

    Vector m_vTabNodes;


    //**************************
    // Constructor
    //**************************

    public TabbedOverlappingPanelsNode( Appliance pAppliance,
					ApplianceState pState ) {
	super();

	m_pTabbedPanel = new TabbedLinkedCIO( pAppliance,
					      pState );

	m_vTabNodes = new Vector();

	EnumeratedSpace pType = (EnumeratedSpace)pState.m_Type.getValueSpace();

	for( int i = 1; i <= pType.getNumItems(); i++ ) {
	    TabbedLinkedCIO.TabbedPanelCIO p = (TabbedLinkedCIO.TabbedPanelCIO)m_pTabbedPanel.getContainerByValue( i );

	    TabPanelNode pTP = new TabPanelNode( p );
	    m_vTabNodes.addElement( pTP );
	    addPanel( pTP );
	}
    }

    //**************************
    // Member Methods
    //**************************

    public TabPanelNode getNodeByValue( int nIndex ) {

	return (TabPanelNode)m_vTabNodes.elementAt( nIndex-1 );
    }

    public TabbedLinkedCIO getTabbedCIO() {

	return m_pTabbedPanel;
    }

    //**************************
    // Interface Methods
    //**************************

    public void setLocation( int nX, int nY ) {

	super.setLocation( nX, nY );

	m_pTabbedPanel.getWidget().setLocation( nX, nY );
    }

    public void setSize( int nWidth, int nHeight ) {

	super.setSize( nWidth, nHeight );

	m_pTabbedPanel.getWidget().setSize( nWidth, nHeight );
    }

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

	ret.height += edu.cmu.hcii.puc.awt.TabbedControl.TAB_HEIGHT;

	return ret;
    }

    public void addComponents( ContainerCIO pContainer ) {

	pContainer.addCIO( m_pTabbedPanel );

	Enumeration e = m_vPanels.elements();
	while( e.hasMoreElements() )
	    ((InterfaceNode)e.nextElement()).addComponents( pContainer );
    }

    public void doSizing( LayoutVariables pVars ) {

	Enumeration e = m_vTabNodes.elements();
	while( e.hasMoreElements() ) {
	    TabPanelNode pNode = (TabPanelNode)e.nextElement();
	    Dimension d = pNode.getContainerCIO().getWidget().getSize();
	    pNode.setSize( d.width, d.height );

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

	return "TabbedOverlappingPanelsNode - " + super.toString();
    }
}
