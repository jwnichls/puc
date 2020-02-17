/**
 * GroupingTable.java
 * 
 * This table represents the state of the parser being with the <groupings> block but not currently within a group.
 *
 * Out Edges
 * ---------
 * GROUP_TAG --> GROUP_PARSE_STATE
 * end( GROUPINGS_TAG ) --> SPEC_PARSE_STATE
 * 
 * In Edges
 * --------
 * SPEC_PARSE_STATE --> GROUPINGS_TAG
 * GROUP_PARSE_STATE --> end( GROUP_TAG )
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import org.xml.sax.Attributes;


// Class Definition

public class GroupingTable extends ParseStateTable {
    
    public GroupingTable( SpecParser parser ) {
	super( parser );
	
	m_StartTagTable.put( SpecParser.GROUP_TAG, 
			     new Integer( 
					 SpecParser.GROUP_PARSE_STATE ) );
    }
    
    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {
	
	if ( name.equals( SpecParser.GROUP_TAG ) ) {
	    m_Parser.newGroup();

	    try {
		m_Parser.m_CurrentGroup.setPriority( Integer.parseInt( atts.getValue( SpecParser.PRIORITY_ATTRIBUTE ) ) );
	    }
	    catch( Throwable t ) { }
	    
	    // System.out.println( "new group!" );
	}
	
	return super.handleStartTag( name, atts );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	
	// System.out.println( "end groupings!" );
	
	return m_Parser.m_States[ SpecParser.SPEC_PARSE_STATE ];
    }
}
