/**
 * FloatingPtTable.java
 * 
 * This table creates an FloatingPtSpace object by parsing information
 * from the specification.  This information is retrieved by the
 * ValueSpaceTable when needed.
 *
 * Out Edges
 * ---------
 * end( FLOATING_PT_TAG ) --> m_ReturnTable
 * 
 * In Edges
 * --------
 * VALUE_SPC_PARSE_STATE --> FLOATING_PT_TAG
 * EXPCT_SPC_PARSE_STATE --> FLOATING_PT_TAG
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import org.xml.sax.Attributes;

import edu.cmu.hcii.puc.ApplianceState;

import edu.cmu.hcii.puc.types.FloatingPtSpace;
import edu.cmu.hcii.puc.types.ReferenceValue;


// Class Definition

public class FloatingPtTable extends ParseStateTable {
    
    // Unlike the other state tables, this one is created dynamically
    // as it is needed
    
    // Member Variables
    protected ValueSpaceTable m_ReturnTable;
    
    protected int m_nParseState; // 0 = nothing, 1 = min, 2 = max
    protected String m_sRefState;   // null = no ref, !null = ref!

    protected boolean m_bRanged;
    protected Number m_Max;
    protected Number m_Min;
    
    public FloatingPtTable( SpecParser parser, ValueSpaceTable returnTable ) {
	super( parser );
	m_ReturnTable = returnTable;
	
	m_bRanged = false;      
	m_nParseState = 0;
	m_sRefState = null;
    }
    
    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {
	
	if ( name.equals( SpecParser.MINIMUM_TAG ) ) {
	    m_bRanged = true;
	    m_nParseState = 1;
	}
	else if ( name.equals( SpecParser.MAXIMUM_TAG ) ) {
	    m_bRanged = true;
	    m_nParseState = 2;
	}
	else if ( name.equals( SpecParser.REFVALUE_TAG ) ) {
	    m_sRefState = atts.getValue( SpecParser.STATE_ATTRIBUTE );
	}
	
	return super.handleStartTag( name, atts );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	if ( name.equals( SpecParser.FLOATINGPT_TAG ) ) {
	    
	    if ( m_bRanged )
		m_ReturnTable.m_LastVS = new FloatingPtSpace( m_Min, m_Max );
	    else 
		m_ReturnTable.m_LastVS = new FloatingPtSpace();
	    
	    return m_ReturnTable;
	}
	
	return super.handleEndTag( name );
    }
    
    public void characters( String ch ) {
	try {
	    Number pNum;

	    if ( m_sRefState != null ) {
		pNum = new ReferenceValue( (ApplianceState)m_Parser.m_CurrentGroup.m_Object,
					   m_sRefState );
		m_Parser.addReferenceValue( (ReferenceValue)pNum );
	    }
	    else
		pNum = new Double( ch );
	    
	    switch( m_nParseState ) {
	    case 1:
		m_Min = pNum;
		break;
	    case 2:
		m_Max = pNum;
		break;
	    }
	    
	    m_nParseState = 0;
	    m_sRefState = null;
	}
	catch( Throwable t ) {
	    m_Parser.m_LogString += "WARNING: integer parse had invalid type.\n";
	    m_Parser.m_bWarnings = true;
	}
    }
}
