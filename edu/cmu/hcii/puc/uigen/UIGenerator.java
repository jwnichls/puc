/**
 * UIGenerator.java
 *
 * Generates and displays a user interface from a spec.
 *
 * Revision History
 * ----------------
 * 07/07/2001: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.MenuBar;
import java.awt.Panel;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.lang.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import edu.cmu.hcii.puc.*;
import edu.cmu.hcii.puc.cio.*;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;
import edu.cmu.hcii.puc.registry.WidgetRegistry;

import edu.cmu.hcii.puc.types.*;

import com.maya.puc.common.*;


// Class Definition

public abstract class UIGenerator extends Panel {

    //**************************
    // Member Variables
    //**************************

    protected MenuBar m_MenuBar;

    protected PanelCIO m_Panel;

    protected WidgetRegistry m_pWidgetRegistry;

    protected Appliance m_pAppliance; // pointer to current appliance

    protected InterfaceNode m_pInterfaceRoot; // root of the interface tree


    //**************************
    // Constructor
    //**************************

    public UIGenerator( MenuBar m ) {

	m_MenuBar = m;

	m_Panel = new PanelCIO();

	((Container)m_Panel.getWidget()).setLayout( null );

	this.setLayout( null );
	this.add( m_Panel.getWidget() );

	this.addComponentListener( new ComponentAdapter() {
		public void componentResized( ComponentEvent e ) {
		    m_Panel.getWidget().setSize( getSize() );
		    ((Container)m_Panel.getWidget()).doLayout();
		}
	    });
    }


    //**************************
    // DependencyListener Helper Class
    //**************************

    public class DependencyListener implements edu.cmu.hcii.puc.StateListener {

	Vector m_vDepObjs;

	public DependencyListener( Vector vDepObjs ) {

	    m_vDepObjs = vDepObjs;
	}

	public void enableChanged( ApplianceObject obj ) { }
	public void labelChanged( ApplianceObject obj ) { }
	public void typeChanged( ApplianceState state ) { }
	
	public void valueChanged( ApplianceState state ) {
	    
	    Enumeration e = m_vDepObjs.elements();
	    while( e.hasMoreElements() ) {
		ApplianceObject pObj = (ApplianceObject)e.nextElement();
		pObj.evalDependencies();
	    }
	}
    }


    //**************************
    // Generation
    //**************************

    public void generateUI( Appliance a ) {

	m_pAppliance = a;

	Enumeration e = m_pAppliance.m_vDependedObjects.elements();
	while( e.hasMoreElements() ) {
	    ApplianceState pState = (ApplianceState)e.nextElement();

	    pState.addStateListener( new DependencyListener( pState.getReverseDeps() ) );
	}
    }


    //**************************
    // Overridden Super-Class Methods
    //**************************

    public void addNotify() {

	super.addNotify();
	m_Panel.addNotify();
    }
}
