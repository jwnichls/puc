/**
 * FullWidthRow.java
 *
 * This node represents a row in the interface that places the single
 * component across the entire width of the column.  Attempts to add
 * more than one one component will replace the previously added
 * component.
 *
 * @author Jeffrey Nichols */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Label;
import java.awt.Component;
import java.awt.Dimension;

import java.util.Vector;

import edu.cmu.hcii.puc.cio.ConcreteInteractionObject;
import edu.cmu.hcii.puc.cio.ContainerCIO;
import edu.cmu.hcii.puc.cio.LabelCIO;
import edu.cmu.hcii.puc.cio.StateLinkedCIO;


// Class Definition

public class FullWidthRow extends Row {

    //**************************
    // Constants
    //**************************

    protected static final int COMPONENT_INDEX = 0;


    //**************************
    // Constructor
    //**************************

    public FullWidthRow( PanelNode pPanel, ConcreteInteractionObject pCIO ) {
	super( pPanel );

	addCIO( pCIO );
    }

    //**************************
    // Member Methods
    //**************************

    public void addComponents( ContainerCIO pContainer ) {
	StateLinkedCIO pStateCIO = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT_INDEX );

	pContainer.addCIO( pStateCIO );
    }

    public Dimension getPreferredSize( LayoutVariables pVars ) {

	StateLinkedCIO pStateCIO = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT_INDEX );

	return pStateCIO.getPreferredSize();
    }

    public int doLayout( ContainerCIO pContainer, 
			 int nTopY, 
			 LayoutVariables pVars ) {
	
	StateLinkedCIO pStateCIO = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT_INDEX );
	pStateCIO.useMinimumLabel();
	
	Component pComp = pStateCIO.getWidget();
	pComp.setSize( pContainer.getWidget().getSize().width - 
		       2 * pVars.m_nRowPadding,
		       pComp.getPreferredSize().height );
	pComp.setLocation( pVars.m_nRowPadding,
			   nTopY + pVars.m_nRowPadding );
	pStateCIO.addNotify();

	return nTopY + pComp.getPreferredSize().height + pVars.m_nRowPadding;
    }

    public String toString() {

	String ret = "FullWidthRow\n";

	StateLinkedCIO pStateCIO = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT_INDEX );

	ret += "0. StateCIO = " + pStateCIO.getApplObj().m_sName + " - " + pStateCIO.getWidget().getBounds().toString() + "\n";

	return ret;
    }
}
