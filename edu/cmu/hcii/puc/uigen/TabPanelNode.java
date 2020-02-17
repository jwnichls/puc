/**
 * TabPanelNode.java
 *
 * This node represents nodes that represent a single panel.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Panel;

import java.util.Enumeration;
import java.util.Vector;

import edu.cmu.hcii.puc.cio.ContainerCIO;
import edu.cmu.hcii.puc.cio.TabbedLinkedCIO;


// Class Definition

public class TabPanelNode extends MultiplePanelNode {

    //**************************
    // Member Variables
    //**************************

    ContainerCIO  m_pPanel;


    //**************************
    // Constructor
    //**************************

    public TabPanelNode( TabbedLinkedCIO.TabbedPanelCIO pCIO ) {
	super();

	m_pPanel = pCIO;
	addPanel( new PanelNode() );
    }

    //**************************
    // Member Methods
    //**************************

    public InterfaceNode getChildNode() {

	return (InterfaceNode)m_vPanels.elementAt( 0 );
    }

    public ContainerCIO getContainerCIO() {

	return m_pPanel;
    }


    //**************************
    // Interface Methods
    //**************************

    public void addComponents( ContainerCIO pContainer ) {

	((Container)m_pPanel.getWidget()).setLayout( null );

	((InterfaceNode)m_vPanels.elementAt( 0 )).addComponents( m_pPanel );
    }

    public Dimension getPreferredSize( LayoutVariables pVars ) {

	return ((InterfaceNode)m_vPanels.elementAt( 0 )).getPreferredSize( pVars );
    }

    public void doSizing( LayoutVariables pVars ) {
	
	Dimension d = m_pPanel.getWidget().getSize();
	this.setSize( d.width, d.height );

	InterfaceNode pNode = (InterfaceNode)m_vPanels.elementAt( 0 );

	pNode.setSize( m_rBounds.width, m_rBounds.height );
	pNode.setLocation( 0, 0 );

	pNode.doSizing( pVars );
    }

    public void doLayout( ContainerCIO pContainer, LayoutVariables pVars ) {

	((InterfaceNode)m_vPanels.elementAt( 0 )).doLayout( m_pPanel, pVars );
    }


    public String toString() {

	return "TabPanelNode - " + super.toString();
    }
}
