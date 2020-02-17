/**
 * ValueLabelTable.java
 * 
 * This table parses label information that is associated with entries
 * in the value space.  Label information is stored inside of <map>
 * tags, as is parsed by creating an instance of the LabelStateTable,
 * which parses a list of label tags into a LabelLibrary object.
 * These LabelLibrary objects are stored in a Vector, corresponding to
 * their index into the value space.
 *
 * Out Edges
 * ---------
 * MAP_TAG --> new LabelStateTable
 * end( VALUE_LABEL_TAG ) --> TYPE_PARSE_STATE
 * 
 * In Edges
 * --------
 * TYPE_PARSE_STATE --> VALUE_LABEL_TAG
 * LabelStateTable --> end( MAP_TAG )
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import java.util.Vector;

import org.xml.sax.Attributes;

import edu.cmu.hcii.puc.ApplianceState;

import edu.cmu.hcii.puc.types.EnableConstraint;


// Class Definition

public class ValueLabelTable extends ParseStateTable {
    
    // Member Variable (initialized by TypeTable)
    public Vector m_LastLbls;
    public int m_LastIndex;
    
    protected LabelStateTable m_Labels;
    
    // Constructor
    public ValueLabelTable( SpecParser parser ) { 
	super( parser );
	
	m_LastIndex = -1; 
	m_LastLbls = null;
	m_Labels = null;
    }
    
    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {
	
	if ( m_Labels != null ) {
	    if ( m_LastIndex >= m_LastLbls.size() )
		m_LastLbls.setSize( m_LastIndex + 1 );
	    
	    m_LastLbls.setElementAt( m_Labels.getLabelLib(), m_LastIndex );
	}

	if ( name.equals( SpecParser.MAP_TAG ) ) {
	    String idx = atts.getValue( SpecParser.INDEX_ATTRIBUTE );
	    String enable = atts.getValue( SpecParser.ENABLE_ATTRIBUTE );

	    if ( idx.equals( "false" ) ) {
		m_LastIndex = 0;
	    }
	    else if ( idx.equals( "true" ) ) {
		m_LastIndex = 1;
	    }
	    else try {
		m_LastIndex = Integer.parseInt( idx );
	    }
	    catch( Throwable t ) {
		m_Parser.m_LogString += "WARNING: map string with invalid index (must be true/false/integer)\n";
		m_Parser.m_bWarnings = true;
		m_LastIndex = m_LastLbls.size();
	    }

	    m_Labels = new LabelStateTable( m_Parser, SpecParser.MAP_TAG, this );


	    if ( enable != null ) {
		m_Labels.getLabelLib().setEnableConstraint( new EnableConstraint( (ApplianceState)m_Parser.m_CurrentGroup.m_Object,
										  enable ) );

		m_Parser.addConstraint( m_Labels.getLabelLib().getEnableConstraint() );
	    }

	    return m_Labels;
	}
	
	return super.handleStartTag( name, atts );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	
	if ( name.equals( SpecParser.VALUE_LABEL_TAG ) ) {
	    if ( m_Labels != null ) {
		if ( m_LastIndex >= m_LastLbls.size() ) 
		    m_LastLbls.setSize( m_LastIndex + 1 );

		m_LastLbls.setElementAt( m_Labels.getLabelLib(), m_LastIndex );
	    }
	    
	    m_LastIndex = -1;
	    m_Labels = null;
	    return m_Parser.m_States[ SpecParser.TYPE_PARSE_STATE ];
	}
	
	return this;
    }
  }
