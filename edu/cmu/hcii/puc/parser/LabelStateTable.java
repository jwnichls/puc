/**
 * LabelStateTable.java
 * 
 * This table parses a list of label tags until a predefined end-tag
 * is encountered.  This end-tag is specified at instantiation time.
 *
 * TODO: Support the other types of label dictionary entries: phonetic
 * and text-to-speech. 
 *
 * Out Edges
 * ---------
 * end( m_ReturnTag ) --> m_ReturnTable
 * 
 * In Edges
 * --------
 * VALUE_LBL_PARSE_STATE --> MAP_TAG
 * STATE_PARSE_STATE --> LABELS_TAG
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.LabelLibrary;

import edu.cmu.hcii.puc.types.StringConstraint;
import edu.cmu.hcii.puc.types.StringValue;

import org.xml.sax.Attributes;


// Class Definition
  
public class LabelStateTable extends ParseStateTable {
    
    // Unlike most other state tables, this one is created dynamically
    // as it is needed
    
    // Member Variables 
    protected String m_ReturnTag;
    protected ParseStateTable m_ReturnTable;
    protected LabelLibrary m_LabelLib;
    protected int m_nLblState; // 0 = no lbl, 1 = lbl!
    protected int m_nRefState; // 0 = no ref, 1 = ref!
    
    public LabelStateTable( SpecParser parser, String returnTag, ParseStateTable returnTable ) {
	super( parser );
	
	m_ReturnTag = returnTag;
	m_ReturnTable = returnTable;
	
	m_LabelLib = new LabelLibrary();
	m_nLblState = 0;
    }
    
    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {
	
	if ( name.equals( SpecParser.REFSTRING_TAG ) ) {
	    
	    String sName = atts.getValue( SpecParser.STATE_ATTRIBUTE );

	    if ( sName != null ) {
		StringConstraint pSC = new StringConstraint( m_Parser.m_CurrentGroup.m_Object,
							     sName );
		m_Parser.addConstraint( pSC );
		m_LabelLib.addLabel( pSC );
	    }
	}
	else if ( name.equals( SpecParser.LABEL_TAG ) ) {
	    
	    m_nLblState = 1;
	}

	return super.handleStartTag( name, atts );
    }

    public ParseStateTable handleEndTag( String name ) {
	
	if ( name.equals( m_ReturnTag ) )
	    return m_ReturnTable;
	
	return super.handleEndTag( name );
    }
    
    public void characters( String ch ) {
	
	if ( m_nLblState == 1 )
	    m_LabelLib.addLabel( new StringValue( ch ) );
	
	m_nLblState = 0;
    }
    
    public LabelLibrary getLabelLib() {
	return m_LabelLib;
    }
}
