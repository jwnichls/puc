/**
 * StateTable.java
 * 
 * This table puts the parser in the mode of parsing a state group.
 * 
 * The state object that is currently being parsed can be found at:
 *   m_Parser.m_CurrentGroup.m_State 
 *
 * Note that this table does not initialize the current state
 * variable.  That is done in the GroupTable.handleStartTag(), where a
 * beginning <state> tag is encountered.
 *
 * Out Edges
 * ---------
 * TYPE_TAG --> TYPE_PARSE_STATE
 * LABELS_TAG --> new LabelStateTable
 * ACTIVEIF_TAG --> new ActiveIfTable
 * end( STATE_TAG ) --> GROUP_PARSE_STATE
 *
 * In Edges
 * --------
 * TYPE_PARSE_STATE --> end( TYPE_TAG )
 * LabelStateTable --> end( LABELS_TAG )
 * ActiveIfTable --> end( ACTIVEIF_TAG )
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;
import java.util.Vector;

import org.xml.sax.Attributes;

import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.GroupNode;


// Class Definition

public class StateTable extends ParseStateTable {
    
    protected LabelStateTable m_Labels;
    protected ActiveIfTable   m_ActiveIf;
    
    public StateTable( SpecParser parser ) {
	super( parser );
	
	m_StartTagTable.put( SpecParser.TYPE_TAG, 
			     new Integer(
					 SpecParser.TYPE_PARSE_STATE ) );
	
	// Also responds to the SpecParser.LABELS_TAG and
	// SpecParser.ACTIVEIF_TAG (see below in handleStartTag) 
    }
    
    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {

	if ( name.equals( SpecParser.APPLY_TYPE_TAG ) ) {

	    String sTypeName = atts.getValue( SpecParser.NAME_ATTRIBUTE );

	    if ( sTypeName != null && m_Parser.isExistingType( sTypeName ) ) {

		((ApplianceState)m_Parser.m_CurrentGroup.m_Object).m_Type =
		    m_Parser.getExistingType( sTypeName );
		
		return this;
	    }
	    else
		m_Parser.addWarning( "Can't apply type " + sTypeName + " because it does not exist." );
	}
	else if ( name.equals( SpecParser.TYPE_TAG ) ) {
	    
	    String sTypeName = atts.getValue( SpecParser.NAME_ATTRIBUTE );
	    if ( sTypeName != null ) {
		((TypeTable)m_Parser.m_States[ SpecParser.TYPE_PARSE_STATE ]).m_sTypeName = sTypeName;
	    }

	    // initialize the type parsers
	
	    ValueSpaceTable VSTable = 
		(ValueSpaceTable)m_Parser.m_States[ SpecParser.VALUE_SPC_PARSE_STATE ];
	    ValueLabelTable VLTable = 
		(ValueLabelTable)m_Parser.m_States[ SpecParser.VALUE_LBL_PARSE_STATE ];
	    ValueSpaceTable EVTable = 
		(ValueSpaceTable)m_Parser.m_States[ SpecParser.EXPCT_SPC_PARSE_STATE ];
	    
	    VSTable.initValSpcParser();
	    EVTable.initValSpcParser();
	    VLTable.m_LastLbls = null;
	}
	else if ( name.equals( SpecParser.LABELS_TAG ) ) {
	    m_Labels = new LabelStateTable( m_Parser, m_Parser.LABELS_TAG, this );
	    
	    // System.out.println( "Trying to get labels..." );
	    
	    return m_Labels;
	}
	else if ( name.equals( SpecParser.ACTIVEIF_TAG ) ) {

	    m_ActiveIf = new ActiveIfTable( m_Parser, this );

	    String ignore = atts.getValue( SpecParser.IGNORE_ATTRIBUTE );

	    if ( ignore != null ) {
		if ( ignore.equals( SpecParser.IGNORE_ATTR_ALL ) ) 
		    m_Parser.m_CurrentGroup.m_nDepIgnore = GroupNode.IGNORE_ALL;
		else if ( ignore.equals( SpecParser.IGNORE_ATTR_PRNT ) ) 
		    m_Parser.m_CurrentGroup.m_nDepIgnore = GroupNode.IGNORE_PARENT;
	    }
	    else 
		m_Parser.m_CurrentGroup.m_nDepIgnore = GroupNode.NO_IGNORE;

	    return m_ActiveIf;
	}
	
	return super.handleStartTag( name, atts );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	
	if ( name.equals( SpecParser.STATE_TAG ) ) {
	    
	    if ( m_Labels != null ) {
		m_Parser.m_CurrentGroup.m_Object.m_Labels = m_Labels.getLabelLib();
		m_Labels = null;
	    }

	    if ( m_ActiveIf != null ) {
		m_Parser.m_CurrentGroup.m_Object.m_pDependencies = m_ActiveIf.getFormula();
		m_ActiveIf = null;
	    }
	    
	    m_Parser.endGroup();
	    
	    return m_Parser.m_States[ SpecParser.GROUP_PARSE_STATE ];
	}
	else
	    return super.handleEndTag( name );
    }
}
