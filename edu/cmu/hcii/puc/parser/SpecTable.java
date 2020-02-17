/**
 * SpecTable.java
 * 
 * This table represents the state of the parser being at the level of
 * the <spec> tags. 
 *
 * Out Edges
 * ---------
 * GROUPINGS_TAG --> GROUPINGS_PARSE_STATE
 * 
 * In Edges
 * --------
 * START_PARSE_STATE --> SPEC_TAG
 * GROUPINGS_PARSE_STATE --> end( GROUPINGS_TAG )
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;


// Class Definition

public class SpecTable extends ParseStateTable {
    
    public SpecTable( SpecParser parser ) {
	super( parser );
	
	m_StartTagTable.put( SpecParser.GROUPINGS_TAG,
			     new Integer(
					 SpecParser.GROUPINGS_PARSE_STATE
					 ) );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	
	return m_Parser.m_States[ SpecParser.START_PARSE_STATE ];
    }
}
