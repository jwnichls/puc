/**
 * InternalControlPanelOrganizer.java
 *
 * This organizer creates a set of panels that are controlled by a
 * widget within each panel.  It is assumed that such a widget exists
 * (and is typically enforced by some manipulation to the group tree
 * earlier in the generation process)
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.GroupNode;

import edu.cmu.hcii.puc.cio.ContainerCIO;
import edu.cmu.hcii.puc.cio.PanelCIO;


// Class Definition

public class InternalControlPanelOrganizer extends ControlOrganizer {

    //**************************
    // Member Variables
    //**************************



    //**************************
    // Constructor
    //**************************

    public InternalControlPanelOrganizer( UIGenerator ui,
					  ApplianceState pState,
					  Vector vChildren,
					  Vector vDependencies
					  ) {
	super( ui, pState );

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

	NonOverlappingPanelsNode pNon = new NonOverlappingPanelsNode();
	OverlappingPanelsNode pOver = new OverlappingPanelsNode();

	pCurrentNode.insertAsParent( pNon );
	pNon.addPanel( pOver );

	Hashtable hDeps = new Hashtable( m_hDependencies.size() );
	Hashtable hPanels = new Hashtable( m_hDependencies.size() );

	Enumeration en = m_hDependencies.keys();
	PanelNode newP = null;
	while( en.hasMoreElements() ) {
	    GroupNode g = (GroupNode)en.nextElement();
	    Vector vDeps = (Vector)m_hDependencies.get( g );
	    newP = new PanelNode();
	    
	    pOver.addPanel( newP );

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
