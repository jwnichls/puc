/**
 * TabbedControlPanelOrganizer.java
 *
 * This organizer creates a set of panels that are controlled by a
 * widget outside of the panels.  It is assumed that such a widget
 * exists (and is typically enforced by some manipulation to the group
 * tree earlier in the generation process) 
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.EqualsDependency;
import edu.cmu.hcii.puc.GroupNode;


// Class Definition

public class TabbedControlPanelOrganizer extends ControlOrganizer {

    //**************************
    // Member Variables
    //**************************

    protected boolean m_bVertical;


    //**************************
    // Constructor
    //**************************

    public TabbedControlPanelOrganizer( UIGenerator ui,
					ApplianceState pState,
					Vector vChildren,
					Vector vDependencies,
					boolean bVertical
					) {
	super( ui, pState );

	m_bVertical = bVertical;

	m_hDependencies = new Hashtable( vChildren.size() );

	for( int i = 0; i < vChildren.size(); i++ ) {
	    GroupNode g = (GroupNode)vChildren.elementAt( i );
	    Vector vDeps = (Vector)vDependencies.elementAt( i );

	    m_hDependencies.put( g, vDeps );
	}
    }


    //**************************
    // Organizer Method
    //**************************

    public Hashtable addOrganization( GroupNode pGroup, 
				      InterfaceNode pCurrentNode ) {

	TabbedOverlappingPanelsNode pTab = new TabbedOverlappingPanelsNode( m_pUI.m_pAppliance, m_pState );
	MultiplePanelNode pNon;
	if ( m_bVertical ) 
	    pNon = new VerticalNoOverlapPanelsNode( (PanelNode)pCurrentNode,
						    pTab );
	else {
	    pNon = new NonOverlappingPanelsNode();
	    pCurrentNode.insertAsParent( pNon );
	    pNon.addPanel( pTab );
	}

	Hashtable hDeps = new Hashtable( m_hDependencies.size() );
	Hashtable hPanels = new Hashtable( m_hDependencies.size() );

	Enumeration en = m_hDependencies.keys();
	PanelNode newP = null;
	while( en.hasMoreElements() ) {
	    GroupNode g = (GroupNode)en.nextElement();
	    Vector vDeps = (Vector)m_hDependencies.get( g );

	    EqualsDependency pDep = (EqualsDependency)vDeps.elementAt( 0 );

	    try {
		// assuming that getChildNode() returns an
		// InterfaceNode that is a PanelNode
		newP = (PanelNode)pTab.getNodeByValue( new Integer( pDep.getValueString() ).intValue() ).getChildNode();
	    }
	    catch( Throwable t ) { t.printStackTrace(); }

	    hDeps.put( vDeps, newP );
	    hPanels.put( g, newP );
	}

	m_pActivePanel = newP;

	m_hDependencies = hDeps;
	m_bUIValid = true;

	this.valueChanged( m_pState );

	return hPanels;
    }
}
