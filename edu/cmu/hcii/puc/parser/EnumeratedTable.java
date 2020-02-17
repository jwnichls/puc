/**
 * EnumeratedTable.java
 * 
 * This table creates an EnumeratedSpace object by parsing information
 * from the specification.  This information is retrieved by the
 * ValueSpaceTable when needed.
 *
 * Out Edges
 * ---------
 * end( ENUMERATED_TAG ) --> m_ReturnTable
 * 
 * In Edges
 * --------
 * VALUE_SPC_PARSE_STATE --> ENUMERATED_TAG
 * EXPCT_SPC_PARSE_STATE --> ENUMERATED_TAG
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import org.xml.sax.Attributes;

import edu.cmu.hcii.puc.types.EnumeratedSpace;


// Class Definition

public class EnumeratedTable extends ParseStateTable {
    
    // Unlike the other state tables, this one is created dynamically
    // as it is needed
    
    // Member Variables
    protected ValueSpaceTable m_ReturnTable;
    
    protected int m_nParseState; // 0 = nothing, 1 = min, 2 = max
    
    protected int m_nItems;


    EnumeratedTable( SpecParser parser, ValueSpaceTable returnTable ) {
	super( parser );
	m_ReturnTable = returnTable;

	m_nItems = 0;  // not a valid value for an enumerated type
	m_nParseState = 0;
    }
    
    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {
	
	if ( name.equals( SpecParser.ITEMS_TAG ) ) {
	    m_nParseState = 1;
	}
	
	return super.handleStartTag( name, atts );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	if ( name.equals( SpecParser.ENUMERATED_TAG ) ) {
	    
	    m_ReturnTable.m_LastVS = new EnumeratedSpace( m_nItems );
	    
	    return m_ReturnTable;
	}
	
	return super.handleEndTag( name );
    }
    
    public void characters( String ch ) {
	try {
	    int num = Integer.parseInt( ch );
	    
	    switch( m_nParseState ) {
	    case 1:
		m_nItems = num;
	    }
	    
	    m_nParseState = 0;
	}
	catch( Throwable t ) {
	    m_Parser.m_LogString += "WARNING: integer parse had invalid type.\n";
	    m_Parser.m_bWarnings = true;
	}
    }
}
