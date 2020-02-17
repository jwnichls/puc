/**
 * TabbedLinkedCIO.java
 *
 * A sub-class of ContainerCIO.
 *
 * A generic "hacked" implementation of the Panel CIO to check if CIO
 * system is actually working. 
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.FontMetrics;
import java.awt.Insets;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.lang.*;

import java.util.*;

import edu.cmu.hcii.puc.Globals;

import edu.cmu.hcii.puc.awt.TabbedControl;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.LabelLibrary;
import edu.cmu.hcii.puc.Globals;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.registry.WidgetRegistry;

import edu.cmu.hcii.puc.types.EnumeratedSpace;
import edu.cmu.hcii.puc.types.ValueSpace;

import edu.cmu.hcii.puc.awt.TabbedControl;

import com.maya.puc.common.Message;


// Class Definition

public class TabbedLinkedCIO extends StateLinkedCIO 
                             implements ItemListener {

    //**************************
    // Helper Classes
    //**************************

    public static class TabbedPanelCIO extends ContainerCIO {

	public TabbedPanelCIO( String sLabel ) {
	    super( null );

	    m_Widget = new TabbedControl.TabbedPane( this, sLabel );

	    ((TabbedControl.TabbedPane)m_Widget).setLayout( new RowLayout( 3 ) );
	}

	public String getLabel() {
	    
	    return ((TabbedControl.TabbedPane)m_Widget).getLabel();
	}

	public void setLabel( String sLabel ) {
	    
	    ((TabbedControl.TabbedPane)m_Widget).setLabel( sLabel );
	}

	public TabbedControl.TabbedPane getTabbedPane() { 
	    return (TabbedControl.TabbedPane)m_Widget; 
	}
    }


    //**************************
    // Member Variables
    //**************************

    Hashtable m_hIndexItemMap;
    Vector    m_vPanelCIOs;


    //**************************
    // Constructor
    //**************************

    public TabbedLinkedCIO( Appliance appl, 
			    ApplianceObject applObj ) {
	
	super( appl, applObj, null );

	m_Widget = new TabbedControl( this );

	m_hIndexItemMap = new Hashtable();
	m_vPanelCIOs = new Vector();

	ApplianceState as = (ApplianceState)m_ApplObj;
	TabbedControl pTC = (TabbedControl)m_Widget;

	pTC.addItemListener( this );

	if ( !m_ApplObj.isState() 
	     || as.m_Type.getValueSpace().getSpace() != ValueSpace.ENUMERATED_SPACE ) {
	    System.err.println( "TabbedLinkedCIO can only represent enumerated space state variables!" + as.m_sName + " " + as.m_Type.getValueSpace().getName() );
	}
	else {

	    ((ApplianceState)m_ApplObj).addStateListener( new StateListener() {
		    
		    public void enableChanged( ApplianceObject obj ) { 
			m_Widget.setEnabled( m_ApplObj.isEnabled() &&
					     !((ApplianceState)m_ApplObj).m_bReadOnly );
		    }

		    public void labelChanged( ApplianceObject obj ) {
			refreshDisplay();
		    }

		    public void typeChanged( ApplianceState obj ) { 
			typeChangeRefresh();
		    }
		    
		    public void valueChanged( ApplianceState obj ) { 
			refreshDisplay();
		    }
		});

	    try {
		FontMetrics fm = Globals.getFontMetricsObj( pTC.getLabelFont() );
		
		// FIXME:JWN: Lots of assumptions here (EnumeratedSpace
		// caused creation of this type of widget, labels for all
		// elements, etc.) 
		
		EnumeratedSpace e =
		    (EnumeratedSpace)as.m_Type.getValueSpace();
		
		Vector labels = as.m_Type.getValueLabels();
		
		m_hIndexItemMap.clear();
		for( int i = 1; i <= e.getNumItems(); i++ ) {
		    LabelLibrary pLib = (LabelLibrary)labels.elementAt(i);
		    
		    String label = pLib.getLabelByCharLength( 20 );
		    
		    TabbedPanelCIO pTPCIO = new TabbedPanelCIO( label );
		    TabbedControl.TabbedPane p = pTPCIO.getTabbedPane();
		    p.setEnabled( pLib.getEnabled() );
		    
		    pTC.addTabbedPane( p );
		    m_vPanelCIOs.addElement( pTPCIO );
		    
		    m_hIndexItemMap.put( i + "", p );
		    
		    if ( i == ((Integer)e.getValue()).intValue() )
			pTC.setSelectedPane( i - 1 );
		}
	    }
	    catch( Throwable t ) { 
		t.printStackTrace();
	    }
	}

    }

	
    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() { }

    public void itemStateChanged( ItemEvent e ) {
	Message msg;
	TabbedControl pTC = (TabbedControl)m_Widget;
	
	msg = new Message.StateChangeRequest( m_ApplObj.m_sName,
					      pTC.getSelectedPaneIndex() + 1 + "" );
	
	try { m_Appliance.m_pConnection.send( msg ); }
	catch( Throwable t ) { }
    }

    public ContainerCIO getContainerByValue( int nIndex ) {

	return (ContainerCIO)m_vPanelCIOs.elementAt( nIndex - 1 );
    }

    public void addNotify() {
	    
	// Initialize tabs (here because we know FontMetrics are available)
	
	// ASSUMPTION: This code will only be executed once...

	TabbedControl pTC = (TabbedControl)m_Widget;
	ApplianceState as = (ApplianceState)m_ApplObj;

	if ( pTC.getPaneCount() > 0 ) return; // this should ensure
	                                      // assumption is true.

	try {
	    FontMetrics fm = Globals.getFontMetricsObj( pTC.getLabelFont() );

	    // FIXME:JWN: Lots of assumptions here (EnumeratedSpace
	    // caused creation of this type of widget, labels for all
	    // elements, etc.) 

	    if ( as.m_Type.getValueSpace().getSpace() == ValueSpace.ENUMERATED_SPACE ) {
		
		EnumeratedSpace e =
		    (EnumeratedSpace)as.m_Type.getValueSpace();

		int nLblWidth = m_Widget.getSize().width /
		    e.getNumItems() - TabbedControl.TAB_HORIZ_PAD * 2;
		
		Vector labels = as.m_Type.getValueLabels();
		
		m_hIndexItemMap.clear();
		for( int i = 1; i <= e.getNumItems(); i++ ) {
		    LabelLibrary pLib = (LabelLibrary)labels.elementAt(i);

		    TabbedControl.TabbedPane p = (TabbedControl.TabbedPane)m_hIndexItemMap.get( new Integer( i ) );
		    p.setLabel( pLib.getLabelByPixelLength( fm, nLblWidth ) );
		    p.setEnabled( pLib.getEnabled() );
		}
	    }
	    else {
		System.err.println( "TabbedLinkedCIO does not know how to handle non-enumerated spaces" );
	    }
	}
	catch( Throwable t ) {
	    t.printStackTrace();
	}
    }
    
    protected void typeChangeRefresh() {

	TabbedControl pTC = (TabbedControl)m_Widget;
	ApplianceState as = (ApplianceState)m_ApplObj;

	EnumeratedSpace e =
	    (EnumeratedSpace)as.m_Type.getValueSpace();
	
	Vector labels = as.m_Type.getValueLabels();
	
	for( int i = 1; i <= e.getNumItems(); i++ ) {
	    LabelLibrary pLib = (LabelLibrary)labels.elementAt(i);
	    
	    pTC.getTabbedPane( i-1 ).setEnabled( pLib.getEnabled() );
	}

	pTC.repaint();
    }

    protected void refreshDisplay() {

	TabbedControl pTC = (TabbedControl)m_Widget;
	ApplianceState as = (ApplianceState)m_ApplObj;

	if ( pTC.getPaneCount() > 0 ) {

	    pTC.setSelectedPane( (TabbedControl.TabbedPane)m_hIndexItemMap.get( as.m_Type.getValueSpace().getValue().toString() ) );
	}
    }
}
