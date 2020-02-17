/**
 * TwoColumnRow.java
 *
 * This node represents a row in the interface that uses a two column
 * layout, each with a single label and a single component.
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

public class TwoColumnRow extends Row {

    //**************************
    // Constants
    //**************************

    protected static final int LABEL1_INDEX = 0;
    protected static final int COMPONENT1_INDEX = 1;
    protected static final int LABEL2_INDEX = 2;
    protected static final int COMPONENT2_INDEX = 3;


    //**************************
    // Constructor
    //**************************

    public TwoColumnRow( PanelNode pPanel,
			 ConcreteInteractionObject pCIO1,
			 ConcreteInteractionObject pCIO2 ) {
	super( pPanel );

	addCIO( pCIO1.getLabelCIO() );
	addCIO( pCIO1 );

	addCIO( pCIO2.getLabelCIO() );
	addCIO( pCIO2 );
    }

    //**************************
    // Member Methods
    //**************************

    public void addComponents( ContainerCIO pContainer ) {
	LabelCIO pLabelCIO1 = (LabelCIO)m_vCIOs.elementAt( LABEL1_INDEX );
	StateLinkedCIO pStateCIO1 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT1_INDEX );
	LabelCIO pLabelCIO2 = (LabelCIO)m_vCIOs.elementAt( LABEL2_INDEX );
	StateLinkedCIO pStateCIO2 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT2_INDEX );

	pContainer.addCIO( pLabelCIO1 );
	pContainer.addCIO( pStateCIO1 );
	pContainer.addCIO( pLabelCIO2 );
	pContainer.addCIO( pStateCIO2 );
    }

    public Dimension getPreferredSize( LayoutVariables pVars ) {

	LabelCIO pLabelCIO1 = (LabelCIO)m_vCIOs.elementAt( LABEL1_INDEX );
	StateLinkedCIO pStateCIO1 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT1_INDEX );
	LabelCIO pLabelCIO2 = (LabelCIO)m_vCIOs.elementAt( LABEL2_INDEX );
	StateLinkedCIO pStateCIO2 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT2_INDEX );

	Dimension ret = new Dimension();

	ret.height = Math.max( pStateCIO1.getWidget().getPreferredSize().height,
			       pStateCIO2.getWidget().getPreferredSize().height ) + 
	             pVars.m_nRowPadding;
	ret.width = ( pLabelCIO1 != null ? 
		      pLabelCIO1.getWidget().getPreferredSize().width : 0 )
	            + pStateCIO1.getWidget().getPreferredSize().width + 
	            ( pLabelCIO2 != null ? 
	              pLabelCIO2.getWidget().getPreferredSize().width : 0 )
	            + pStateCIO2.getWidget().getPreferredSize().width ;


	return ret;
    }

    public int doLayout( ContainerCIO pContainer, 
			 int nTopY, 
			 LayoutVariables pVars ) {
	
	LabelCIO pLabelCIO1 = (LabelCIO)m_vCIOs.elementAt( LABEL1_INDEX );
	StateLinkedCIO pStateCIO1 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT1_INDEX );
	LabelCIO pLabelCIO2 = (LabelCIO)m_vCIOs.elementAt( LABEL2_INDEX );
	StateLinkedCIO pStateCIO2 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT2_INDEX );

	int nRowHeight = Math.max( pStateCIO1.getWidget().getPreferredSize().height,
				   pStateCIO2.getWidget().getPreferredSize().height );

	int nColumnWidth = ( pContainer.getWidget().getSize().width - 3 * pVars.m_nRowPadding ) / 2;
	int nCol1LabelWidth = (int)Math.round( nColumnWidth * pVars.m_dTwoColLabel1Pcnt );
	int nCol1CompWidth = nColumnWidth - nCol1LabelWidth;
	int nCol2LabelWidth = (int)Math.round( nColumnWidth * pVars.m_dTwoColLabel2Pcnt );
	int nCol2CompWidth = nColumnWidth - nCol2LabelWidth;

	Component pComp = pStateCIO1.getWidget();
	pComp.setSize( nCol1CompWidth,
		       nRowHeight );
	pComp.setLocation( nCol1LabelWidth + 2 * pVars.m_nRowPadding,
			   nTopY + pVars.m_nRowPadding );

	Label pLabel = null;
	if ( pLabelCIO1 != null ) {
	    pLabel = (Label)pLabelCIO1.getWidget();
	    pLabel.setAlignment( Label.RIGHT );
	    pLabel.setSize( nCol1LabelWidth,
			    nRowHeight );
	    pLabel.setLocation( pVars.m_nRowPadding, 
				nTopY + pVars.m_nRowPadding );
	}

	pComp = pStateCIO2.getWidget();
	pComp.setSize( nCol2CompWidth,
		       nRowHeight );
	pComp.setLocation( nColumnWidth + nCol2LabelWidth + 3 * pVars.m_nRowPadding,
			   nTopY + pVars.m_nRowPadding );

	pLabel = null;
	if ( pLabelCIO2 != null ) {
	    pLabel = (Label)pLabelCIO2.getWidget();
	    pLabel.setAlignment( Label.RIGHT );
	    pLabel.setSize( nCol2LabelWidth,
			    nRowHeight );
	    pLabel.setLocation( nColumnWidth + 2 * pVars.m_nRowPadding, 
				nTopY + pVars.m_nRowPadding );
	}

	return nTopY + nRowHeight + pVars.m_nRowPadding;
    }

    public String toString() {

	String ret = "TwoColumnRow\n";

	LabelCIO pLabelCIO1 = (LabelCIO)m_vCIOs.elementAt( LABEL1_INDEX );
	StateLinkedCIO pStateCIO1 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT1_INDEX );
	LabelCIO pLabelCIO2 = (LabelCIO)m_vCIOs.elementAt( LABEL2_INDEX );
	StateLinkedCIO pStateCIO2 = (StateLinkedCIO)m_vCIOs.elementAt( COMPONENT2_INDEX );

	if ( pLabelCIO1 != null ) 
	    ret += "0. LabelCIO1 - " + pLabelCIO1.getWidget().getBounds().toString() + "\n";
	ret += "1. StateCIO1 = " + pStateCIO1.getApplObj().m_sName + " - " + pStateCIO1.getWidget().getBounds().toString() + "\n";
	if ( pLabelCIO2 != null ) 
	    ret += "2. LabelCIO2 - " + pLabelCIO2.getWidget().getBounds().toString() + "\n";
	ret += "3. StateCIO2 = " + pStateCIO2.getApplObj().m_sName + " - " + pStateCIO2.getWidget().getBounds().toString() + "\n";


	return ret;
    }
}
