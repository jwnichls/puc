/**
 * InterfaceNode.java
 *
 * This is the base class for the set of objects that can be
 * represented in the interface tree.  The interface tree is created
 * from the modified group tree in the dependency graph analysis
 * phase. 
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Dimension;
import java.awt.Rectangle;

import edu.cmu.hcii.puc.cio.ContainerCIO;


// Class Definition

public abstract class InterfaceNode extends Object {

    //**************************
    // Member Variables
    //**************************

    protected Rectangle m_rBounds;
    protected MultiplePanelNode m_pParent;
    

    //**************************
    // Constructor
    //**************************
    
    public InterfaceNode() {
	m_rBounds = new Rectangle( 0, 0, 0, 0 );
    }


    //**************************
    // Abstract Methods
    //**************************

    public MultiplePanelNode getParent() {

	return m_pParent;
    }

    public Rectangle getBounds() {

	return m_rBounds;
    }

    protected void setParent( MultiplePanelNode pNode ) {
	
	m_pParent = pNode;
    }

    public void setLocation( int nX, int nY ) {

	m_rBounds.x = nX;
	m_rBounds.y = nY;
    }

    public void setSize( int nWidth, int nHeight ) {

	m_rBounds.width = nWidth;
	m_rBounds.height = nHeight;
    }

    public abstract void addComponents( ContainerCIO pContainer );

    public abstract Dimension getPreferredSize( LayoutVariables pVars );

    public abstract void doSizing( LayoutVariables pVars );

    public abstract void doLayout( ContainerCIO pContainer, LayoutVariables pVars );

    /**
     * Inserts the <i>pNode</i> parameter as the parent of this
     * node. <i>pNode</i> becomes the child of the former parent and
     * this node becomes the child of <i>pNode</i>. 
     */
    public void insertAsParent( MultiplePanelNode pNode ) {

	if ( m_pParent != null ) {
	    MultiplePanelNode pParent = m_pParent;
	    pParent.removePanel( this );
	    pParent.addPanel( pNode );
	}

	pNode.addPanel( this );	
    }

    public abstract String toString();
}
