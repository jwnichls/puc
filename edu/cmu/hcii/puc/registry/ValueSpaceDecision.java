/**
 * ValueSpaceDecision.java
 *
 * A decision based upon the type of the ApplianceObject.
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
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.cio.*;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;


// Class Definition

public class ValueSpaceDecision extends Decision {


    //**************************
    // Constants
    //**************************

    public static final String DECISION_LABEL = "valuespace";


    //**************************
    // Dynamic Loading Code
    //**************************

    public static class ValueSpaceDecisionFactory implements DecisionFactory {
	public Decision createDecision( Hashtable pChoices ) {
	    return new ValueSpaceDecision( pChoices );
	}
    }

    static {
	// register the factory with the WidgetRegistryParser
	WidgetRegistryParser.addDecisionFactory( DECISION_LABEL, new ValueSpaceDecisionFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public ValueSpaceDecision( Hashtable pChoices ) {

	super( pChoices );
    }


    //**************************
    // Abstract Member Methods
    //**************************
    
    public ConcreteInteractionObject chooseWidget( Appliance a,
						   ApplianceObject ao ) {
	
	ApplianceState as = (ApplianceState)ao;

	Decision d = (Decision)m_pChoices.get( as.m_Type.getValueSpace().getName() );

	if ( d == null ) 
	    d = (Decision)m_pChoices.get( Decision.DEFAULT );

	if ( d != null )
	    return d.chooseWidget( a, ao );
	else
	    return null;
    }


    public String toString() {
	
	return "VALUESPACE Decision\n" + super.toString(); 
    }
}
