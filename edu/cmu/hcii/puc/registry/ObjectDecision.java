/**
 * ObjectDecision.java
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
import edu.cmu.hcii.puc.cio.*;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;


// Class Definition

public class ObjectDecision extends Decision {


    //**************************
    // Constants
    //**************************

    public static final String DECISION_LABEL = "object";

    public static final String COMMAND        = "command";
    public static final String STATE          = "state";
    public static final String EXPLANATION    = "explanation";


    //**************************
    // Dynamic Loading Code
    //**************************

    public static class ObjectDecisionFactory implements DecisionFactory {
	public Decision createDecision( Hashtable pChoices ) {
	    return new ObjectDecision( pChoices );
	}
    }

    static {
	// register the factory with the WidgetRegistryParser
	WidgetRegistryParser.addDecisionFactory( DECISION_LABEL, new ObjectDecisionFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public ObjectDecision( Hashtable pChoices ) {

	super( pChoices );
    }


    //**************************
    // Abstract Member Methods
    //**************************
    
    public ConcreteInteractionObject chooseWidget( Appliance a,
						   ApplianceObject ao ) {
	Decision d;
	
	if ( ao.isState() )
	    d = (Decision)m_pChoices.get( STATE );
	else if ( ao.isExplanation() )
	    d = (Decision)m_pChoices.get( EXPLANATION );
	else
	    d = (Decision)m_pChoices.get( COMMAND );

	if ( d == null )
	    d = (Decision)m_pChoices.get( Decision.DEFAULT );
	
	if ( d != null )
	    return d.chooseWidget( a, ao );
	else
	    return null;
    }


    public String toString() {
	
	return "OBJECT Decision\n" + super.toString(); 
    }
}
