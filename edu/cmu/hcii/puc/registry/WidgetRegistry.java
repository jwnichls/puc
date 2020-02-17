/**
 * WidgetRegistry.java
 *
 * Chooses the appropriate widget for representing a given appliance
 * object.  These decisions are pre-specified in the widget registry,
 * which is stored as a file on the PUC device.  Ideally, this file
 * is different for each type of controlling device.
 *
 * This class manages the loading and storing of the widget registry
 * file, and also the dynamic loading of widgets specified in the
 * registry.  See edu.cmu.hcii.puc.cio.ConcreteInteractionObject for
 * more information about the dynamic loading of widgets (CIOs).
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
import edu.cmu.hcii.puc.types.*;


// Class Definition

public class WidgetRegistry extends Object {

    //**************************
    // Dynamic Loading Code
    //**************************

    protected static Hashtable cioFactories;

    static {
	cioFactories = new Hashtable();
    }

    public static void addCIOFactory( String name, CIOFactory f ) {

	cioFactories.put( name, f );
    }

    public static CIOFactory getCIOFactory( String name ) {
	
	return (CIOFactory)cioFactories.get( name );
    }


    //**************************
    // Member Variables
    //**************************

    protected Decision m_pDecisionTree;


    //**************************
    // Constructor
    //**************************

    public WidgetRegistry( Decision pTree ) {

	m_pDecisionTree = pTree;
    }


    //**************************
    // Member Variables
    //**************************

    public ConcreteInteractionObject chooseWidget( Appliance a,
						   ApplianceObject ao ) {

	ConcreteInteractionObject pCIO = m_pDecisionTree.chooseWidget( a, ao );

	// workaround for getting preferred size of CIOs early on
	if ( pCIO != null )
	    a.getUIGenerator().add( pCIO.getWidget() );

	return pCIO;
    }
}
