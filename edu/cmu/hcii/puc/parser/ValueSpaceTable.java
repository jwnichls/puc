/**
 * ValueSpaceTable.java
 * 
 * This table parses ValueSpace information from the specification
 * language.  It is used in two different state instances, as shown in
 * the edge descriptions below.
 *
 * This table works differently than some other tables.  When
 * different tags are encountered for the different types of spaces
 * that are possible (integer, fixed pt., etc.), a new table object is
 * created to parse that space.  When all the information has been
 * parsed, the new table returns to this state and the information is
 * extracted from it.
 *
 * Out Edges
 * ---------
 * end( VALUE_SPACE_TAG ) --> TYPE_PARSE_STATE
 * end( EXPCT_SPACE_TAG ) --> TYPE_PARSE_STATE
 * <TYPE>_TAG --> new <Type>Table
 * 
 * In Edges
 * --------
 * TYPE_PARSE_STATE --> VALUE_SPACE_TAG
 * TYPE_PARSE_STATE --> EXPCT_SPACE_TAG
 * <Type>Table --> end( <TYPE>_TAG ) 
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;

import org.xml.sax.Attributes;

import edu.cmu.hcii.puc.types.ValueSpace;
import edu.cmu.hcii.puc.types.BooleanSpace;
import edu.cmu.hcii.puc.types.CustomSpace;
import edu.cmu.hcii.puc.types.StringSpace;


// Class Definition

public class ValueSpaceTable extends ParseStateTable {
    
    public ValueSpaceTable( SpecParser parser ) {
	super( parser );
    }
    
    // Member Variables (initialized by TypeTable, using initValSpcParser() )
    public ValueSpace m_LastVS;

    protected int m_nCustomState; // 0 = not custom, 1 = custom!
    

    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {
	
	if ( name.equals( SpecParser.BOOLEAN_TAG ) ) {
	    //System.out.println( "Got the boolean type!" );
	    m_LastVS = new BooleanSpace();
	}
	else if ( name.equals( SpecParser.STRING_TAG ) ) {
	    //System.out.println( "Got the string type!" );
	    m_LastVS = new StringSpace();
	}
	else if ( name.equals( SpecParser.INTEGER_TAG ) ) {
	    //System.out.println( "Got the integer type!" );
	    return new IntegerTable( m_Parser, this );
	}
	else if ( name.equals( SpecParser.FIXEDPT_TAG ) ) {
	    return new FixedPtTable( m_Parser, this );
	}
	else if ( name.equals( SpecParser.FLOATINGPT_TAG ) ) {
	    return new FloatingPtTable( m_Parser, this );
	}
	else if ( name.equals( SpecParser.ENUMERATED_TAG ) ) {
	    return new EnumeratedTable( m_Parser, this );
	}
	else if ( name.equals( SpecParser.CUSTOM_TAG ) ) {
	    m_nCustomState = 1;
	}

	return super.handleStartTag( name, atts );
    }
    
    public ParseStateTable handleEndTag( String name ) {
	
	if ( name.equals( SpecParser.VALUE_SPACE_TAG ) || 
	     name.equals( SpecParser.EXPCT_SPACE_TAG ) ) {

	    return m_Parser.m_States[ SpecParser.TYPE_PARSE_STATE ];
	}
	
	return this;
    }

    public void characters( String ch ) { 

	if ( m_nCustomState == 1 ) {
	    m_LastVS = new CustomSpace( ch );

	    m_nCustomState = 0;
	}
    }
    
    public void initValSpcParser() {
	m_LastVS = null;
	m_nCustomState = 0;
    }
}
