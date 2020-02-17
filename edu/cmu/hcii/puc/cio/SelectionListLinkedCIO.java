/**
 * SelectionListLinkedCIO.java
 *
 * A sub-class of StateLinkedCIO.
 *
 * The concrete representation of a SelectionList widget.
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.Panel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.lang.*;

import java.util.Hashtable;
import java.util.Vector;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.ObjectListener;
import edu.cmu.hcii.puc.Globals;
import edu.cmu.hcii.puc.StateListener;
import edu.cmu.hcii.puc.LabelLibrary;

import edu.cmu.hcii.puc.awt.Choice;

import edu.cmu.hcii.puc.types.BooleanSpace;
import edu.cmu.hcii.puc.types.EnumeratedSpace;
import edu.cmu.hcii.puc.types.ValueSpace;

import edu.cmu.hcii.puc.registry.WidgetRegistry;

import com.maya.puc.common.Message;


// Class Definition

public class SelectionListLinkedCIO extends StateLinkedCIO {

    //**************************
    // Dynamic Loading Static 
    //**************************

    static class SelectionListLinkedCIOFactory implements CIOFactory {

	public ConcreteInteractionObject createCIO( Appliance a,
						    ApplianceObject ao ) {

	    return new SelectionListLinkedCIO( a, ao );
	}
    }

    static {
	// register the factory with the WidgetRegistry
	WidgetRegistry.addCIOFactory( "SelectionListLinkedCIO", new SelectionListLinkedCIOFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    Hashtable m_hItemIndexMap;
    Hashtable m_hIndexItemMap;

    int       m_nCurrentSel;


    //**************************
    // Constructor
    //**************************

    public SelectionListLinkedCIO( Appliance appl, ApplianceObject applObj ) {

	super( appl, applObj, null );

	m_Widget = new Choice( this );

	m_hItemIndexMap = new Hashtable();
	m_hIndexItemMap = new Hashtable();

	m_nCurrentSel = 0;

	((Choice)m_Widget).addItemListener( new ItemListener() {
		public void itemStateChanged( ItemEvent e ) {
		    Message msg;
		    
		    String lbl = (String)m_hItemIndexMap.get( new Integer( ((Choice)m_Widget).getSelectedIndex() ) );
		    msg = new Message.StateChangeRequest( m_ApplObj.m_sName,
							  lbl );

		    try { SelectionListLinkedCIO.this.m_Appliance.m_pConnection.send( msg ); }
		    catch( Throwable t ) { }

		    refreshDisplay();
		}
	    });

	if ( !m_ApplObj.isState() ) {
	    System.err.println( "SelectionListLinkedCIO can't represent a command!" );
	}
	else {
	    ((ApplianceState)m_ApplObj).addStateListener( new StateListener() {

		    public void enableChanged( ApplianceObject obj ) { 
			m_Widget.setEnabled( m_ApplObj.isEnabled() );
		    }

		    public void labelChanged( ApplianceObject obj ) {
			typeChangeRefresh();
		    }

		    public void typeChanged( ApplianceState obj ) { 
			typeChangeRefresh();
		    }
		    
		    public void valueChanged( ApplianceState obj ) { 
			refreshDisplay();
		    }
		});
	}

	refreshDisplay();
    }


    //**************************
    // Protected Methods
    //**************************

    protected void typeChangeRefresh() {

	System.out.println( "Got type change refresh: " + m_ApplObj.m_sName );

	ApplianceState as = (ApplianceState)m_ApplObj;

	try {
	    FontMetrics fm = Globals.getFontMetricsObj( m_Widget.getFont() );

	    // FIXME:JWN: Lots of assumptions here (EnumeratedSpace or
	    // BooleanSpace caused creation of this type of widget,
	    // labels for all elements, etc.)

	    if ( as.m_Type.getValueSpace().getSpace() == ValueSpace.ENUMERATED_SPACE ) {
		
		EnumeratedSpace e =
		    (EnumeratedSpace)as.m_Type.getValueSpace();
		
		Vector labels = as.m_Type.getValueLabels();
		
		((Choice)m_Widget).removeAll();
		m_hItemIndexMap.clear();
		m_hIndexItemMap.clear();
		for( int i = 1; i <= e.getNumItems(); i++ ) {
		    LabelLibrary pLib = (LabelLibrary)labels.elementAt(i);

		    if ( pLib.getEnabled() ) {
			String label = pLib.getLabelByPixelLength( fm, m_Widget.getSize().width );
			((Choice)m_Widget).add( label );
			
			m_hItemIndexMap.put( new Integer( ((Choice)m_Widget).getItemCount()-1 ), i + "" );
			m_hIndexItemMap.put( i + "", new Integer( ((Choice)m_Widget).getItemCount()-1 ) );

			if ( i == ((Integer)e.getValue()).intValue() )
			    ((Choice)m_Widget).select( ((Choice)m_Widget).getItemCount() - 1 );
		    }
		}
	    }
	    else if ( as.m_Type.getValueSpace().getSpace() == ValueSpace.BOOLEAN_SPACE ) {
		
		BooleanSpace e =
		    (BooleanSpace)as.m_Type.getValueSpace();
		
		Vector labels = as.m_Type.getValueLabels();
		
		((Choice)m_Widget).removeAll();
		m_hItemIndexMap.clear();
		m_hIndexItemMap.clear();
		for( int i = 0; i < 2; i++ ) {
		    LabelLibrary pLib = (LabelLibrary)labels.elementAt(i);
		    
		    if ( pLib.getEnabled() ) {
			String label = pLib.getLabelByPixelLength( fm, m_Widget.getSize().width );
			((Choice)m_Widget).add( label );
			
			m_hItemIndexMap.put( new Integer( ((Choice)m_Widget).getItemCount()-1 ), new Boolean( i == 1 ).toString() );
			m_hIndexItemMap.put( new Boolean( i == 1 ).toString(), new Integer( i ) );

			if ( i == ( ((Boolean)e.getValue()).booleanValue() ? 1 : 0 ) )
			    ((Choice)m_Widget).select( ((Choice)m_Widget).getItemCount() - 1 );
		    }
		}
	    }
	    else {
		System.err.println( "SelectionListLinkedCIO does not know how to handle non-boolean/enumerated spaces" );
	    }
	}
	catch( Throwable t ) {
		
	    ((Choice)m_Widget).add( "--" );
	}
    }

    protected void refreshDisplay() {

	ApplianceState as = (ApplianceState)m_ApplObj;

	if ( ((Choice)m_Widget).getItemCount() == 0 ) {
	    typeChangeRefresh();
	}

	if ( ((Choice)m_Widget).getItemCount() > 0 ) {

	    Integer idx = (Integer)m_hIndexItemMap.get( as.m_Type.getValueSpace().getValue().toString() );

	    if ( idx != null )
		((Choice)m_Widget).select( idx.intValue() );
	}
    }
    

    //**************************
    // Member Methods
    //**************************

    public void useMinimumLabel() { 
	// TODO:JWN: Implement this
    }

    public boolean hasLabel() { return true; }

    public ConcreteInteractionObject getLabelCIO() { 
	
	if ( m_ApplObj.m_Labels != null )
	    return new LabelCIO( m_ApplObj.m_Labels ); 

	return null;
    }

    public void addNotify() {

	refreshDisplay();
	typeChangeRefresh();
    }
}
