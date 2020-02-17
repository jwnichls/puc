/**
 * Row.java
 *
 * This node represents a row in the interface.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Dimension;

import java.util.Vector;

import edu.cmu.hcii.puc.cio.ConcreteInteractionObject;
import edu.cmu.hcii.puc.cio.ContainerCIO;


// Class Definition

public abstract class Row {

    //**************************
    // Member Variables
    //**************************

    protected Vector m_vCIOs;
    protected PanelNode m_pParent;


    //**************************
    // Constructor
    //**************************

    public Row( PanelNode pPanel ) {
	m_vCIOs = new Vector( 2 );

	m_pParent = pPanel;
    }

    //**************************
    // Member Methods
    //**************************

    protected void addCIO( ConcreteInteractionObject pCIO ) {
	
	m_vCIOs.addElement( pCIO );
    }
    
    public abstract void addComponents( ContainerCIO pContainer );

    public abstract Dimension getPreferredSize( LayoutVariables pVars );

    public abstract int doLayout( ContainerCIO pContainer, 
				  int nTopY, 
				  LayoutVariables pVars );

    public abstract String toString();
}
