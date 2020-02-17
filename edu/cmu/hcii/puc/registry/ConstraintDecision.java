/**
 * ConstraintDecision.java
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

import java.util.Hashtable;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.cio.*;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;


// Class Definition

public class ConstraintDecision extends Decision {


    //**************************
    // Constants
    //**************************

    public static final String DECISION_LABEL = "constraint";

    public static final String TRUE           = "true";
    public static final String FALSE          = "false";


    //**************************
    // Dynamic Loading Code
    //**************************

    public static class ConstraintDecisionFactory implements DecisionFactory {
	public Decision createDecision( Hashtable pChoices ) {
	    return new ConstraintDecision( pChoices );
	}
    }

    static {
	// register the factory with the WidgetRegistryParser
	WidgetRegistryParser.addDecisionFactory( DECISION_LABEL, new ConstraintDecisionFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public ConstraintDecision( Hashtable pChoices ) {

	super( pChoices );
    }


    //**************************
    // Abstract Member Methods
    //**************************
    
    public ConcreteInteractionObject chooseWidget( Appliance a,
						   ApplianceObject ao ) {
	
	ApplianceState as = (ApplianceState)ao;

	Decision d;

	if ( as.isConstraintVariable() )
	    d = (Decision)m_pChoices.get( TRUE );
	else
	    d = (Decision)m_pChoices.get( FALSE );

	if ( d != null )
	    return d.chooseWidget( a, ao );
	else
	    return null;
    }


    public String toString() {
	
	return "READONLY Decision\n" + super.toString(); 
    }
}
