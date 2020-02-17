/**
 * DependedUponOnceDecision.java
 *
 * A decision based upon the ApplianceState is depended upon by only
 * one other ApplianceObject. 
 *
 * Revision History
 * ----------------
 * 02/02/2002: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.registry;


// Import Declarations

import java.lang.*;

import java.util.Hashtable;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.cio.*;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;


// Class Definition

public class DependedUponOnceDecision extends Decision {


    //**************************
    // Constants
    //**************************

    public static final String DECISION_LABEL = "depended-upon-once";

    public static final String TRUE           = "true";
    public static final String FALSE          = "false";


    //**************************
    // Dynamic Loading Code
    //**************************

    public static class DependedUponOnceDecisionFactory implements DecisionFactory {
	public Decision createDecision( Hashtable pChoices ) {
	    return new DependedUponOnceDecision( pChoices );
	}
    }

    static {
	// register the factory with the WidgetRegistryParser
	WidgetRegistryParser.addDecisionFactory( DECISION_LABEL, new DependedUponOnceDecisionFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public DependedUponOnceDecision( Hashtable pChoices ) {

	super( pChoices );
    }


    //**************************
    // Abstract Member Methods
    //**************************
    
    public ConcreteInteractionObject chooseWidget( Appliance a,
						   ApplianceObject ao ) {
	
	ApplianceState as = (ApplianceState)ao;
	Decision d;

	if ( as.getReverseDependencyCount() == 1 )
	    d = (Decision)m_pChoices.get( TRUE );
	else
	    d = (Decision)m_pChoices.get( FALSE );

	if ( d != null )
	    return d.chooseWidget( a, ao );
	else
	    return null;
    }


    public String toString() {
	
	return "DEPENDED-UPON-ONCE Decision\n" + super.toString(); 
    }
}
