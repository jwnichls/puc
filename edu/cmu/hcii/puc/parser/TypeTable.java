/**
 * TypeTable.java
 * 
 * This table puts the parser in the state of parsing a type group. 
 *
 * This state transfers to other states, which parse several types of
 * information that must be incorporated into a instance of the
 * PUCType class, which in turn is stored in the state object that is
 * currently being parsed.  To facilitate this, there are three tables
 * that the TypeTable delegates to for the parsing of type
 * information.  These tables store the last parsed values within
 * their own object, which are then accessed by the TypeTable when the
 * </type> tag is encountered.  The TypeTable creates a new instance
 * of PUCType object, and stores this in the object for the state
 * currently being parsed.
 *
 * Out Edges
 * ---------
 * VALUE_SPACE_TAG --> VALUE_SPC_PARSE_STATE
 * EXPCT_SPACE_TAG --> EXPCT_SPC_PARSE_STATE
 * VALUE_LABEL_TAG --> VALUE_LBL_PARSE_STATE
 * end( TYPE_TAG ) --> STATE_PARSE_STATE
 * 
 * In Edges
 * --------
 * STATE_PARSE_STATE --> TYPE_TAG
 * VALUE_SPC_PARSE_STATE --> end( VALUE_SPACE_TAG )
 * EXPCT_SPC_PARSE_STATE --> end( EXPCT_SPACE_TAG )
 * VALUE_LBL_PARSE_STATE --> end( VALUE_LABEL_TAG ) 
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import java.util.Vector;

import org.xml.sax.Attributes;

import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.PUCType;


// Class Definition

public class TypeTable extends ParseStateTable {

    public String m_sTypeName;
    
    public TypeTable( SpecParser parser ) {
	super( parser );
	
	m_sTypeName = null;

	m_StartTagTable.put( SpecParser.VALUE_SPACE_TAG,
			     new Integer(
					 SpecParser.VALUE_SPC_PARSE_STATE ) );
	m_StartTagTable.put( SpecParser.EXPCT_SPACE_TAG,
			     new Integer(
					 SpecParser.EXPCT_SPC_PARSE_STATE ) );
	m_StartTagTable.put( SpecParser.VALUE_LABEL_TAG,
			     new Integer(
					 SpecParser.VALUE_LBL_PARSE_STATE ) );
    }

    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {

	if ( name.equals( SpecParser.VALUE_LABEL_TAG ) ) {
	    ((ValueLabelTable)m_Parser.m_States[ SpecParser.VALUE_LBL_PARSE_STATE ]).m_LastLbls = new Vector();
	}

	return super.handleStartTag( name, atts );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	
	if ( name.equals( SpecParser.TYPE_TAG ) ) {
	    ValueSpaceTable VSTable = 
		(ValueSpaceTable)m_Parser.m_States[ SpecParser.VALUE_SPC_PARSE_STATE ];
	    ValueLabelTable VLTable = 
		(ValueLabelTable)m_Parser.m_States[ SpecParser.VALUE_LBL_PARSE_STATE ];
	    ValueSpaceTable EVTable = 
		(ValueSpaceTable)m_Parser.m_States[ SpecParser.EXPCT_SPC_PARSE_STATE ];
	    
	    PUCType t = new PUCType( m_sTypeName, VSTable.m_LastVS, VLTable.m_LastLbls, EVTable.m_LastVS );

	    if ( m_sTypeName != null )
		m_Parser.newType( t );

	    m_sTypeName = null;

	    ((ApplianceState)m_Parser.m_CurrentGroup.m_Object).m_Type = t;
	    
	    return m_Parser.m_States[ SpecParser.STATE_PARSE_STATE ];
	}
	
	return this;
    }
}

