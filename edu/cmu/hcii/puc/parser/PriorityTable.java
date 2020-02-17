/**
 * PriorityTable.java
 * 
 * This table parses and saves the priority information for the state
 * that is currently being parsed. 
 *
 * Out Edges
 * ---------
 * end( PRIORITY_TAG ) --> STATE_PARSE_STATE || COMMAND_PARSE_STATE
 * 
 * In Edges
 * --------
 * STATE_PARSE_STATE --> PRIORITY_TAG
 * COMMAND_PARSE_STATE --> PRIORITY_TAG
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;


// Class Definition

public class PriorityTable extends ParseStateTable {
    
    public PriorityTable( SpecParser parser ) {
	super( parser );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	
	if ( m_Parser.m_CurrentGroup.m_Object.isState() ) 
	    return m_Parser.m_States[ SpecParser.STATE_PARSE_STATE ];
	else if ( m_Parser.m_CurrentGroup.m_Object.isExplanation() ) 
	    return m_Parser.m_States[ SpecParser.EXPLANATION_PARSE_STATE ];
	else
	    return m_Parser.m_States[ SpecParser.COMMAND_PARSE_STATE ];
    }
    
    public void characters( String ch ) {
	
	try {
	    int p = Integer.parseInt( ch );
	    
	    m_Parser.m_CurrentGroup.m_Object.m_nPriority = p;
	}
	catch( Throwable t ) { }
    }
}
