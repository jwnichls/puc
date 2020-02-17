/**
 * OneColumnRow.java
 *
 * This node represents a row in the interface that use a one column
 * layout with a single label and a single component. Attempts to add
 * more than one label or more than one component will replace the
 * previously added label or component.
 *
 * @author Jeffrey Nichols
 */

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

public class OneColumnRow extends Row {

    //**************************
    // Constants
    //**************************

    protected static final int LABEL_INDEX = 0;
    protected static final int COMPONENT_INDEX = 1;


    //**************************
    // Constructor
    //**************************

    public OneColumnRow( PanelNode pPanel, ConcreteInteractionObject pCIO ) {
	super( pPanel );

	addCIO( pCIO.getLabelCIO() );
	addCIO( pCIO );
    }

    //**************************
    // Member Methods
    //**************************

    public void addComponents( ContainerCIO pContainer ) {
	LabelCIO pLabelCIO = (LabelCIO)m_vCIOs.elementAt( LABEL_INDEX );
	StateLinkedCIO pStateCIO = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT_INDEX );

	pContainer.addCIO( pLabelCIO );
	pContainer.addCIO( pStateCIO );
    }

    public Dimension getPreferredSize( LayoutVariables pVars ) {

	LabelCIO pLabelCIO = (LabelCIO)m_vCIOs.elementAt( LABEL_INDEX );
	StateLinkedCIO pStateCIO = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT_INDEX );

	pStateCIO.useMinimumLabel();
	Dimension ret = null;

	if ( m_pParent.isVertical() ) {
	    ret = pStateCIO.getMinimumSize();
	    ret.height += pVars.m_nRowPadding;

	    if ( pLabelCIO != null ) {
		pLabelCIO.useMinimumLabel();

		ret.height += pLabelCIO.getMinimumSize().height +
		              pVars.m_nRowPadding;
		ret.width = Math.max( ret.width, 
				      pLabelCIO.getMinimumSize().width );
	    }
	}
	else {
	    ret = pStateCIO.getPreferredSize();

	    if ( pLabelCIO != null )
		ret.width += pLabelCIO.getPreferredSize().width + 
		             pVars.m_nRowPadding; 
	}
	ret.height += pVars.m_nRowPadding;
	ret.width += 2 * pVars.m_nRowPadding;

	return ret;
    }

    public int doLayout( ContainerCIO pContainer, 
			 int nTopY, 
			 LayoutVariables pVars ) {
	
	LabelCIO pLabelCIO = (LabelCIO)m_vCIOs.elementAt( LABEL_INDEX );
	StateLinkedCIO pStateCIO = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT_INDEX );

	Label pLabel;
	Component pComp = pStateCIO.getWidget();
	int nUsableWidth;

	if ( m_pParent.isVertical() ) {
	    nUsableWidth = pContainer.getWidget().getSize().width -
		2 * pVars.m_nRowPadding;

	    pLabel = null;
	    if ( pLabelCIO != null ) {
		pLabel = (Label)pLabelCIO.getWidget();
		pLabel.setAlignment( Label.CENTER );
		pLabel.setSize( nUsableWidth, 
				pLabel.getPreferredSize().height );
		pLabel.setLocation( pVars.m_nRowPadding,
				    nTopY + pVars.m_nRowPadding );
		nTopY += pVars.m_nRowPadding + 
		         pLabel.getPreferredSize().height;
	    }

	    pComp.setSize( nUsableWidth,
			   pComp.getPreferredSize().height );
	    pComp.setLocation( pVars.m_nRowPadding,
			       nTopY + pVars.m_nRowPadding );

	    nTopY += 2 * pVars.m_nRowPadding +
		     pComp.getPreferredSize().height;

	    return nTopY;
	}
	else {
	    nUsableWidth = pContainer.getWidget().getSize().width - 
		3 * pVars.m_nRowPadding;
	    int nLabelWidth = (int)Math.round( pVars.m_dOneColLabelPcnt * nUsableWidth );
	    int nCompWidth = nUsableWidth - nLabelWidth;

	    pComp.setSize( nCompWidth,
			   pComp.getPreferredSize().height );
	    pComp.setLocation( nLabelWidth + 2 * pVars.m_nRowPadding,
			       nTopY + pVars.m_nRowPadding );
	    pStateCIO.addNotify();

	    pLabel = null;
	    if ( pLabelCIO != null ) {
		pLabel = (Label)pLabelCIO.getWidget();
		pLabel.setAlignment( Label.RIGHT );
		pLabel.setSize( nLabelWidth,
				pComp.getPreferredSize().height );
		pLabel.setLocation( pVars.m_nRowPadding, 
				    nTopY + pVars.m_nRowPadding );
		pLabelCIO.addNotify();
	    }

	    return nTopY + pComp.getPreferredSize().height + pVars.m_nRowPadding;
	}
    }

    public String toString() {

	String ret = "OneColumnRow\n";

	LabelCIO pLabelCIO = (LabelCIO)m_vCIOs.elementAt( LABEL_INDEX );
	StateLinkedCIO pStateCIO = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT_INDEX );

	if ( pLabelCIO != null ) 
	    ret += "0. LabelCIO - " + pLabelCIO.getWidget().getBounds().toString() + "\n";
	ret += "1. StateCIO = " + pStateCIO.getApplObj().m_sName + " - " + pStateCIO.getWidget().getBounds().toString() + "\n";

	return ret;
    }
}
