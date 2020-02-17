/**
 * ActiveIfTable.java
 * 
 * This table represents the parsing of active-if statements which
 * appear within group, state, and command tags.  This table parses
 * these statements into RawDependency objects, which are converted
 * into a dependency graph in the second parsing iteration (see
 * SpecParser.java) 
 *
 * Out Edges
 * ---------
 * end( ACTIVEIF_TAG ) --> STATE_PARSE_STATE || 
 *                         COMMAND_PARSE_STATE || 
 *                         GROUP_PARSE_STATE
 * 
 * In Edges
 * --------
 * STATE_PARSE_STATE --> ACTIVEIF_TAG
 * COMMAND_PARSE_STATE --> ACTIVEIF_TAG
 * GROUP_PARSE_STATE --> ACTIVEIF_TAG
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import java.util.Vector;

import org.xml.sax.Attributes;

import edu.cmu.hcii.puc.Dependency;
import edu.cmu.hcii.puc.DependencyFormula;
import edu.cmu.hcii.puc.AND;
import edu.cmu.hcii.puc.OR;
import edu.cmu.hcii.puc.EqualsDependency;
import edu.cmu.hcii.puc.GreaterThanDependency;
import edu.cmu.hcii.puc.LessThanDependency;

// Class Definition

public class ActiveIfTable extends ParseStateTable {
    
    //**************************
    // Member Variables
    //**************************

    DependencyFormula m_Formula;
    ParseStateTable m_ReturnTable;

    DependencyFormula m_Cursor;
    
    boolean m_bProcessingTag;
    String m_sTagName;
    String m_sStateName;

    Vector m_pStack;


    //**************************
    // Constructor
    //**************************

    public ActiveIfTable( SpecParser parser, ParseStateTable returnTable ) {
	super( parser );

	m_ReturnTable = returnTable;
	m_bProcessingTag = false;

	m_Cursor = m_Formula = new AND();

	m_pStack = new Vector();
	m_pStack.addElement( m_Cursor );
    }


    //**************************
    // Parsing Methods
    //**************************

    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {

	if ( name.equals( SpecParser.OR_TAG ) ) {

	    DependencyFormula temp = new OR();
	    m_Cursor.addFormula( temp );

	    m_Cursor = temp;
	    m_pStack.addElement( m_Cursor );
	}
	else if ( name.equals( SpecParser.AND_TAG ) ) {
	    
	    DependencyFormula temp = new AND();
	    m_Cursor.addFormula( temp );

	    m_Cursor = temp;
	    m_pStack.addElement( m_Cursor );
	}
	else {
	    // assume that only possible tags here are equals,
	    // greaterthan, and lessthan.

	    m_bProcessingTag = true;
	    m_sTagName = name;
	    m_sStateName = atts.getValue( SpecParser.STATE_ATTRIBUTE );
	} 

	return this;
    }

    public ParseStateTable handleEndTag( String name ) {

	if ( name.equals( SpecParser.ACTIVEIF_TAG ) ) {

	    // probably should check for some error conditions here

	    if ( m_pStack.size() != 1 ) {
		m_Parser.addWarning( "active-if didn't have proper stack size" );
	    }

	    return m_ReturnTable;
	}
	else if ( name.equals( SpecParser.OR_TAG ) || 
		  name.equals( SpecParser.AND_TAG ) ) {

	    m_Cursor = (DependencyFormula)m_pStack.lastElement();
	    m_pStack.removeElementAt( m_pStack.size() - 1 );
	}

	return super.handleEndTag( name );
    }

    public void characters( String str ) {
	if ( m_bProcessingTag ) {

	    Dependency d;

	    if ( m_sTagName.equals( SpecParser.EQUALS_TAG ) ) {
		
		d = new EqualsDependency( m_sStateName, str );
	    }
	    else if ( m_sTagName.equals( SpecParser.GREATERTHAN_TAG ) ) {

		d = new GreaterThanDependency( m_sStateName, str );
	    }
	    else if ( m_sTagName.equals( SpecParser.LESSTHAN_TAG ) ) {

		d = new LessThanDependency( m_sStateName, str );
	    }
	    else {
		d = null;
		m_Parser.addWarning( "Invalid dependency tag in specification." );
	    }

	    m_Cursor.addDependency( d );
	    m_bProcessingTag = false;
	}
    }

    public DependencyFormula getFormula() {
	
	return m_Formula;
    }
}

