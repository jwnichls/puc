/**
 * LayoutVariables.java
 *
 * This is an object used to pass variables used for layout to all of
 * the relevant components.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.lang.*;

import edu.cmu.hcii.puc.PUC;


// Class Definition

public class LayoutVariables {

    //**************************
    // Member Variables
    //**************************

    /**
     * Specifies the percentage of the column that a label should
     * occupy in a one column row.  This should be a value between 0
     * and 1.0.
     */
    public double m_dOneColLabelPcnt;  

    /**
     * Specifies the percentage of the column that the first label
     * should occupy in a two column row.  This should be a value
     * between 0 and 1.0.  
     */
    public double m_dTwoColLabel1Pcnt;  

    /**
     * Specifies the percentage of the column that the second label
     * should occupy in a two column row.  This should be a value
     * between 0 and 1.0.  
     */
    public double m_dTwoColLabel2Pcnt;  

    /**
     * Specifies the padding between rows.
     */
    public int m_nRowPadding;

    /**
     * Specifies whether the size of the panel network should be
     * confined to the size of the screen.  Set this to false if the
     * interface is being created in a panel that scrolls.  The H
     * variable controls constraints in the horizontal dimension, and
     * the V variable controls constraints in the vertical dimension.
     */
    public boolean m_bConstrainPanelsToScreenH;
    public boolean m_bConstrainPanelsToScreenV;


    //**************************
    // Constructor
    //**************************

    public LayoutVariables( int nRowPad, 
			    double d1CLabelPcnt, 
			    double d2CLabel1Pcnt,
			    double d2CLabel2Pcnt ) {

	m_nRowPadding = nRowPad;
	m_dOneColLabelPcnt = d1CLabelPcnt;
	m_dTwoColLabel1Pcnt = d2CLabel1Pcnt;
	m_dTwoColLabel2Pcnt = d2CLabel2Pcnt;

	m_bConstrainPanelsToScreenH = true;
	m_bConstrainPanelsToScreenV = true;
    }

    public LayoutVariables() {

	m_nRowPadding = 3;
	m_dOneColLabelPcnt = 0.40;
	m_dTwoColLabel1Pcnt = 0.35;
	m_dTwoColLabel2Pcnt = 0.35;

	m_bConstrainPanelsToScreenH = true;
	m_bConstrainPanelsToScreenV = true;
    }


    //**************************
    // Member Methods
    //**************************
}
