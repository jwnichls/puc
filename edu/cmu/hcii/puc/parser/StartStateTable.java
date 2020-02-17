/**
 * StartStateTable.java
 * 
 * This table represents the initial state of the parser, before any
 * tags have been parsed.
 *
 * Out Edges
 * ---------
 * SPEC_TAG --> SPEC_PARSE_STATE
 * 
 * In Edges
 * --------
 *  none
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import org.xml.sax.Attributes;


// Class Definition

public class StartStateTable extends ParseStateTable {
    
    public StartStateTable( SpecParser parser ) {
	super( parser );
	
	m_StartTagTable.put( SpecParser.SPEC_TAG,
			     new Integer( 
					 SpecParser.SPEC_PARSE_STATE ) );
    }

    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {
	
	if ( name.equals( SpecParser.SPEC_TAG ) ) {

	    m_Parser.setName( atts.getValue( SpecParser.NAME_ATTRIBUTE ) );
	}

	return super.handleStartTag( name, atts );
    }
}

