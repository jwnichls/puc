/**
 * WidgetDecision.java
 *
 * A specific form of decision which decides on a particular widget.
 *
 * Revision History
 * ----------------
 * 02/02/2002: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.registry;


// Import Declarations

import java.lang.*;

import java.util.Enumeration;
import java.util.Hashtable;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;

import edu.cmu.hcii.puc.cio.*;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;


// Class Definition

public class WidgetDecision extends Decision {

    //**************************
    // Member Variables
    //**************************

    protected String m_sWidgetName;


    //**************************
    // Constructor
    //**************************

    public WidgetDecision( String sName ) {

	super( null );

	m_sWidgetName = sName;

	try {
	    Class.forName( sName );
	    // System.err.println( "Loaded " + sName );
	}
	catch( Exception e ) {
	    try {
		Class.forName( WidgetRegistryParser.m_sDefaultPackage + "." + sName );
		// System.err.println( "Loaded " + sName + " on 2." );
	    }
	    catch( Exception e2 ) {
		
		System.err.println( "Fatal Error: Widget not available!" );
		e.printStackTrace();
		System.exit(-1);
	    }
	}
    }


    //**************************
    // Abstract Member Methods
    //**************************
    
    public ConcreteInteractionObject chooseWidget( Appliance a, 
						   ApplianceObject ao ) {

	// System.err.println( "Widget name:" + m_sWidgetName + ":" );

	return WidgetRegistry.getCIOFactory( m_sWidgetName ).createCIO( a, ao );
    }


    public String toString() {
	
	return "Widget: " + m_sWidgetName + "\n";
    }
}
