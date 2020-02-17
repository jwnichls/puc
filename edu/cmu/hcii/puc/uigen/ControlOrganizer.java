/**
 * ControlOrganizer.java
 *
 * This is an organizer that changes the display on the screen based
 * upon the value of a state variable.  It is an abstract class.
 * Other organization can be created on top of it with specific visual
 * instantiations. 
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.Dependency;
import edu.cmu.hcii.puc.GroupNode;
import edu.cmu.hcii.puc.Organizer;
import edu.cmu.hcii.puc.Globals;
import edu.cmu.hcii.puc.StateListener;

import edu.cmu.hcii.puc.cio.ContainerCIO;


// Class Definition

public abstract class ControlOrganizer implements Organizer,
						  StateListener {

    //**************************
    // Member Variables
    //**************************

    protected ApplianceState m_pState;

    protected boolean m_bUIValid;

    /**
     * m_hDependencies
     *
     * This hashtable contains different data, depending on whether
     * this Organizers addOrganizer method has been called.  Before
     * the call, the keys are children and the values are vectors of
     * dependencies.  Afterward, the keys are dependencies and the
     * values are PanelNode objects.
     */
    protected Hashtable m_hDependencies;

    protected PanelNode m_pActivePanel;
    
    protected UIGenerator m_pUI;


    //**************************
    // Constructor
    //**************************
    
    public ControlOrganizer( UIGenerator ui, ApplianceState s ) {

	m_pState = s;
	m_pUI = ui;

	m_bUIValid = false;

	m_pState.addStateListener( this );
    }


    //**************************
    // StateListener Methods
    //**************************

    public void labelChanged( ApplianceObject obj ) { }

    public void enableChanged( ApplianceObject obj ) { }

    public void typeChanged( ApplianceState state ) { }

    public void valueChanged( ApplianceState pState ) {
	if (! m_bUIValid ) return;

	Enumeration en = m_hDependencies.keys();
DEP:	while( en.hasMoreElements() ) {
	    Vector vDeps = (Vector)en.nextElement();

	    Enumeration en2 = vDeps.elements();
	    while( en2.hasMoreElements() ) {
		if (! ((Dependency)en2.nextElement()).isSatisfied() ) 
		    continue DEP;
	    }

	    PanelNode c = (PanelNode)m_hDependencies.get( vDeps );
	    
	    m_pActivePanel.getContainerCIO().getWidget().setVisible( false );
	    m_pActivePanel = c;
	    m_pActivePanel.getContainerCIO().getWidget().setVisible( true );

	    m_pUI.validate();
	    Globals.validateMainFrame();

	    break;
	}
    }


    //**************************
    // Organizer Method
    //**************************

    public abstract Hashtable addOrganization( GroupNode pGroup, InterfaceNode pCurrentNode ); 
}
