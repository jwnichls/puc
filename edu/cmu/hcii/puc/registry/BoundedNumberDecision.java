/**
 * BoundedNumberDecision.java
 *
 * A decision which assumes the the type of variable is Integer or
 * FixedPt.  The decision is made based upon whether the Integer or
 * FixedPt space is bounded by a minimum and maximum value.
 *
 * Revision History
 * ----------------
 * 07/26/2002: (JWN) Created file.
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
import edu.cmu.hcii.puc.types.*;

import edu.cmu.hcii.puc.parser.WidgetRegistryParser;


// Class Definition

public class BoundedNumberDecision extends Decision {


    //**************************
    // Constants
    //**************************

    public static final String DECISION_LABEL = "boundednumber";

    public static final String TRUE           = "true";
    public static final String FALSE          = "false";


    //**************************
    // Dynamic Loading Code
    //**************************

    public static class BoundedNumberDecisionFactory implements DecisionFactory {
	public Decision createDecision( Hashtable pChoices ) {
	    return new BoundedNumberDecision( pChoices );
	}
    }

    static {
	// register the factory with the WidgetRegistryParser
	WidgetRegistryParser.addDecisionFactory( DECISION_LABEL, new BoundedNumberDecisionFactory() );
    }


    //**************************
    // Member Variables
    //**************************

    //**************************
    // Constructor
    //**************************

    public BoundedNumberDecision( Hashtable pChoices ) {

	super( pChoices );
    }


    //**************************
    // Abstract Member Methods
    //**************************
    
    public ConcreteInteractionObject chooseWidget( Appliance a,
						   ApplianceObject ao ) {
	
	ApplianceState as = (ApplianceState)ao;
	ValueSpace vs = as.m_Type.getValueSpace();
	
	Decision d;

	if ( vs.getSpace() == ValueSpace.INTEGER_SPACE &&
	     ((IntegerSpace)vs).isRanged() ) 

	    d = (Decision)m_pChoices.get( TRUE );

	else if ( vs.getSpace() == ValueSpace.FIXED_PT_SPACE &&
		  ((FixedPtSpace)vs).isRanged() ) 

	    d = (Decision)m_pChoices.get( TRUE );

	else
	    d = (Decision)m_pChoices.get( FALSE );

	if ( d != null )
	    return d.chooseWidget( a, ao );
	else
	    return null;
    }


    public String toString() {
	
	return "BOUNDED-NUMBER Decision\n" + super.toString(); 
    }
}
