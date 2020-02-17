/**
 * AllLabelsDecision.java
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
import java.util.Vector;

import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.LabelLibrary;

import edu.cmu.hcii.puc.cio.*;

import edu.cmu.hcii.puc.types.ValueSpace;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;


// Class Definition

public class AllLabelsDecision extends Decision {


    //**************************
    // Constants
    //**************************

    public static final String DECISION_LABEL = "alllabels";

    public static final String TRUE           = "true";
    public static final String FALSE          = "false";


    //**************************
    // Dynamic Loading Code
    //**************************

    public static class AllLabelsDecisionFactory implements DecisionFactory {
	public Decision createDecision( Hashtable pChoices ) {
	    return new AllLabelsDecision( pChoices );
	}
    }

    static {
	// register the factory with the WidgetRegistryParser
	WidgetRegistryParser.addDecisionFactory( DECISION_LABEL, new AllLabelsDecisionFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public AllLabelsDecision( Hashtable pChoices ) {

	super( pChoices );
    }


    //**************************
    // Member Methods
    //**************************
    
    public ConcreteInteractionObject chooseWidget( Appliance a,
						   ApplianceObject ao ) {
	
	ApplianceState as = (ApplianceState)ao;
	Decision d;

	System.out.println( "Examining state: " + as.m_sName );

	switch( as.m_Type.getValueSpace().getSpace() ) {
	case ValueSpace.BOOLEAN_SPACE:
	    if ( as.m_Type.getValueLabels() != null &&
		 as.m_Type.getValueLabels().size() == 2 ) {
		d = (Decision)m_pChoices.get( TRUE );
		System.out.println( "true!" );
	    }
	    else {
		d = (Decision)m_pChoices.get( FALSE );
		System.out.println( "false!" );
	    }
	    break;
	default:
	    d = (Decision)m_pChoices.get( FALSE );
	    System.out.println( "false!" );
	}

	if ( d != null )
	    return d.chooseWidget( a, ao );
	else
	    return null;
    }


    public String toString() {
	
	return "READONLY Decision\n" + super.toString(); 
    }
}
