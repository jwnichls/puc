/**
 * ParseStateTable.java
 *
 * This the root class for all state table objects used by the SpecParser.
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import java.util.Hashtable;

import org.xml.sax.Attributes;


// Class Definition

public class ParseStateTable extends Object {
    
    //**************************
    // Member Variables
    //**************************    
    
    protected Hashtable m_StartTagTable;
    protected SpecParser m_Parser;
    
    //**************************
    // Member Variables
    //**************************    
    
    protected ParseStateTable( SpecParser parser ) {
	m_Parser = parser;
	m_StartTagTable = new Hashtable();
    }
    
    
    //**************************
    // Abstract Methods
    //**************************    
    
    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {
	
	Integer n = (Integer)m_StartTagTable.get( name );
	// if ( n != null ) System.out.println( "parse state is: " + n.toString() );
	return n == null ? this : m_Parser.m_States[ n.intValue() ];
    }
    
    public ParseStateTable handleEndTag( String name ) {

	return this;
    }
    
    public void characters( String ch ) { }
}
