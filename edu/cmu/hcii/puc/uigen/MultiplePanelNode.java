/**
 * MultiplePanelNode.java
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

import java.util.Vector;

import edu.cmu.hcii.puc.cio.ContainerCIO;


// Class Definition

public abstract class MultiplePanelNode extends InterfaceNode {

    //**************************
    // Member Variables
    //**************************

    protected Vector m_vPanels;
    

    //**************************
    // Constructor
    //**************************

    public MultiplePanelNode() {
	m_vPanels = new Vector();
    }

    //**************************
    // Member Methods
    //**************************

    public void addPanel( InterfaceNode pNode ) {

	m_vPanels.addElement( pNode );

	pNode.setParent( this );
    }

    public void removePanel( InterfaceNode pNode ) {

	m_vPanels.removeElement( pNode );

	pNode.setParent( null );
    }

    //**************************
    // Interface Methods
    //**************************

    public abstract void addComponents( ContainerCIO pContainer );

    public abstract void doSizing( LayoutVariables pVars );

    public abstract void doLayout( ContainerCIO pContainer, 
				   LayoutVariables pVars );

    public abstract Dimension getPreferredSize( LayoutVariables pVars );

    public String toString() {

	String ret = m_rBounds.toString() + "\n";

	for( int i = 0; i < m_vPanels.size(); i++ ) {
	    
	    ret += i + ". " + m_vPanels.elementAt( i ).toString() + "\n";
	}

	return ret;
    }
}
