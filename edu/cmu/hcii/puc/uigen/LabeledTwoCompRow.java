/**
 * LabeledTwoCompRow.java
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

import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.Component;
import java.awt.Dimension;

import java.util.Vector;

import edu.cmu.hcii.puc.Globals;

import edu.cmu.hcii.puc.cio.ConcreteInteractionObject;
import edu.cmu.hcii.puc.cio.ContainerCIO;
import edu.cmu.hcii.puc.cio.LabelCIO;
import edu.cmu.hcii.puc.cio.StateLinkedCIO;


// Class Definition

public class LabeledTwoCompRow extends Row {

    //**************************
    // Constants
    //**************************

    protected static final int LABEL_INDEX = 0;
    protected static final int COMPONENT1_INDEX = 1;
    protected static final int COMPONENT2_INDEX = 2;


    //**************************
    // Constructor
    //**************************

    public LabeledTwoCompRow( PanelNode pPanel, 
			      LabelCIO pLabelCIO,
			      ConcreteInteractionObject pCIO1,
			      ConcreteInteractionObject pCIO2 ) {
	super( pPanel );

	addCIO( pLabelCIO );
	addCIO( pCIO1 );
	addCIO( pCIO2 );
    }

    //**************************
    // Member Methods
    //**************************

    public void addComponents( ContainerCIO pContainer ) {
	LabelCIO pLabelCIO = (LabelCIO)m_vCIOs.elementAt( LABEL_INDEX );
	StateLinkedCIO pStateCIO1 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT1_INDEX );
	StateLinkedCIO pStateCIO2 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT2_INDEX );

	pContainer.addCIO( pLabelCIO );
	pContainer.addCIO( pStateCIO1 );
	pContainer.addCIO( pStateCIO2 );
    }

    public Dimension getPreferredSize( LayoutVariables pVars ) {

	LabelCIO pLabelCIO = (LabelCIO)m_vCIOs.elementAt( LABEL_INDEX );
	StateLinkedCIO pStateCIO1 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT1_INDEX );
	StateLinkedCIO pStateCIO2 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT2_INDEX );

	Dimension ret = new Dimension();

	if ( m_pParent.isVertical() ) {
	    pLabelCIO.useMinimumLabel();
	    pStateCIO1.useMinimumLabel();
	    pStateCIO2.useMinimumLabel();

	    System.out.println( "test lbl: " + ((java.awt.Button)pStateCIO1.getWidget()).getLabel() );

	    ret.height = pLabelCIO.getPreferredSize().height +
		pStateCIO1.getPreferredSize().height +
		pStateCIO2.getPreferredSize().height +
		4 * pVars.m_nRowPadding;

	    ret.width = Math.max( pLabelCIO.getPreferredSize().width,
				  Math.max( pStateCIO1.getPreferredSize().width,
					    pStateCIO2.getPreferredSize().width ) );
	}
	else {
	    ret.height = Math.max( pStateCIO1.getWidget().getPreferredSize().height,
				   pStateCIO2.getWidget().getPreferredSize().height );
	    ret.width = pLabelCIO.getPreferredSize().width +
		pStateCIO1.getPreferredSize().width +
		pStateCIO2.getPreferredSize().width;
	}
	ret.height += pVars.m_nRowPadding;
	ret.width += 2 * pVars.m_nRowPadding;

	return ret;
    }

    public int doLayout( ContainerCIO pContainer, 
			 int nTopY, 
			 LayoutVariables pVars ) {
	
	LabelCIO pLabelCIO = (LabelCIO)m_vCIOs.elementAt( LABEL_INDEX );
	StateLinkedCIO pStateCIO1 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT1_INDEX );
	StateLinkedCIO pStateCIO2 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT2_INDEX );

	Label pLabel = (Label)pLabelCIO.getWidget();
	Component pComp;
	int nUsableWidth;
	FontMetrics pFM = Globals.getFontMetricsObj( pLabel.getFont() );

	if ( m_pParent.isVertical() ) {
	    pLabelCIO.useMinimumLabel();
	    pStateCIO1.useMinimumLabel();
	    pStateCIO2.useMinimumLabel();

	    nUsableWidth = pContainer.getWidget().getSize().width -
		2 * pVars.m_nRowPadding;

	    pLabel.setAlignment( Label.CENTER );

	    int nFontHeight = pFM.getHeight();
	    pLabel.setSize( nUsableWidth,
			    nFontHeight );
	    pLabel.setLocation( pVars.m_nRowPadding,
				nTopY + pVars.m_nRowPadding);

	    nTopY += nFontHeight + 
		     pVars.m_nRowPadding;

	    pComp = pStateCIO1.getWidget();
	    pComp.setSize( nUsableWidth,
			   pComp.getMinimumSize().height );
	    pComp.setLocation( pVars.m_nRowPadding,
			       nTopY + pVars.m_nRowPadding / 2 );

	    nTopY += pComp.getMinimumSize().height +
		     pVars.m_nRowPadding / 2;

	    pComp = pStateCIO2.getWidget();
	    pComp.setSize( nUsableWidth,
			   pComp.getMinimumSize().height );
	    pComp.setLocation( pVars.m_nRowPadding,
			       nTopY + pVars.m_nRowPadding );

	    pLabelCIO.addNotify();
	    pStateCIO1.addNotify();
	    pStateCIO2.addNotify();

	    return nTopY + pComp.getMinimumSize().height +
		2 * pVars.m_nRowPadding;
	}
	else {
	    
	    int nRowHeight = Math.max( pStateCIO1.getWidget().getPreferredSize().height,
				       pStateCIO2.getWidget().getPreferredSize().height );
	    
	    nUsableWidth = pContainer.getWidget().getSize().width - 
		3 * pVars.m_nRowPadding;
	    int nLabelWidth = (int)Math.round( pVars.m_dOneColLabelPcnt * nUsableWidth );
	    int nBothCompWidth = nUsableWidth - nLabelWidth;
	    int nCompWidth = ( nBothCompWidth - pVars.m_nRowPadding ) / 2;
	    
	    pLabel.setAlignment( Label.RIGHT );
	    pLabel.setSize( nLabelWidth,
			    nRowHeight );
	    pLabel.setLocation( pVars.m_nRowPadding, 
				nTopY + pVars.m_nRowPadding );
	    
	    pComp = pStateCIO1.getWidget();
	    pComp.setSize( nCompWidth,
			   nRowHeight );
	    pComp.setLocation( nLabelWidth + 2 * pVars.m_nRowPadding,
			       nTopY + pVars.m_nRowPadding );
	    
	    pComp = pStateCIO2.getWidget();
	    pComp.setSize( nCompWidth,
			   nRowHeight );
	    pComp.setLocation( nLabelWidth + nCompWidth + 3 * pVars.m_nRowPadding,
			       nTopY + pVars.m_nRowPadding );
	    
	    pLabelCIO.addNotify();
	    pStateCIO1.addNotify();
	    pStateCIO2.addNotify();

	    return nTopY + nRowHeight + pVars.m_nRowPadding;
	}
    }

    public String toString() {

	String ret = "LabeledTwoCompRow\n";

	LabelCIO pLabelCIO = (LabelCIO)m_vCIOs.elementAt( LABEL_INDEX );
	StateLinkedCIO pStateCIO1 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT1_INDEX );
	StateLinkedCIO pStateCIO2 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT2_INDEX );

	if ( pLabelCIO != null ) 
	    ret += "0. LabelCIO - " + pLabelCIO.getWidget().getBounds().toString() + "\n";
	ret += "1. StateCIO1 = " + pStateCIO1.getApplObj().m_sName + " - " + pStateCIO1.getWidget().getBounds().toString() + "\n";
	ret += "2. StateCIO2 = " + pStateCIO2.getApplObj().m_sName + " - " + pStateCIO2.getWidget().getBounds().toString() + "\n";

	return ret;
    }
}
