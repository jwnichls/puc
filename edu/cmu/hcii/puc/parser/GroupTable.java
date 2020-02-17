/**
 * GroupTable.java
 *
 * This table puts the parser in the state of parsing within the group
 * tree.  Because group nodes may contain each other, there are two
 * possibilities when a </group> tag is encountered.  If the parser is
 * GROUPINGS_DEPTH deep in the xml tree, then the parser will return
 * to the GROUPINGS_PARSE_STATE.  Otherwise, the parser remains in the
 * GROUP_PARSE_STATE.
 *
 * The group information is stored in GroupNode objects.  All of the
 * leaf nodes in the group tree are states, which are stored within
 * GroupNodes.  The current group being parsed can be found at:
 *   m_Parser.m_CurrentGroup
 *
 * Out Edges
 * ---------
 * STATE_TAG --> STATE_PARSE_STATE
 * COMMAND_TAG --> COMMAND_PARSE_STATE
 * EXPLANATION_TAG --> EXPLANATION_PARSE_STATE
 * ACTIVEIF_TAG --> new ActiveIfTable
 * end( GROUP_TAG ) --> GROUPINGS_PARSE_STATE
 *
 * In Edges
 * --------
 * GROUPINGS_PARSE_STATE --> GROUP_TAG
 * COMMAND_PARSE_STATE --> end( COMMAND_TAG )
 * EXPLANATION_PARSE_STATE --> end( EXPLANATION_TAG )
 * STATE_PARSE_STATE --> end( STATE_TAG )
 * ActiveIfTable --> end( ACTIVEIF_TAG )
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.lang.*;
import java.util.Vector;

import org.xml.sax.Attributes;

import edu.cmu.hcii.puc.GroupNode;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.ApplianceCommand;
import edu.cmu.hcii.puc.ApplianceExplanation;


// Class Definition

public class GroupTable extends ParseStateTable {

    LabelStateTable m_Labels;
    ActiveIfTable   m_ActiveIf;

    public GroupTable( SpecParser parser ) {
	super( parser );

	m_StartTagTable.put( SpecParser.GROUP_TAG,
			     new Integer(
					 SpecParser.GROUP_PARSE_STATE ) );
	m_StartTagTable.put( SpecParser.STATE_TAG,
			     new Integer(
					 SpecParser.STATE_PARSE_STATE ) );
	m_StartTagTable.put( SpecParser.COMMAND_TAG,
			     new Integer(
					 SpecParser.COMMAND_PARSE_STATE ) );
	m_StartTagTable.put( SpecParser.EXPLANATION_TAG,
			     new Integer(
					 SpecParser.EXPLANATION_PARSE_STATE ) );

	// Also responds to the SpecParser.LABELS_TAG and
	// SpecParser.ACTIVEIF_TAG (see below in handleStartTag)
    }

    public ParseStateTable handleStartTag( String name,
					   Attributes atts ) {

	if ( name.equals( SpecParser.GROUP_TAG ) ) {

	    if ( m_Labels != null ) {
		m_Parser.m_CurrentGroup.m_Labels = m_Labels.getLabelLib();
		m_Labels = null;
	    }

	    if ( m_ActiveIf != null ) {
		m_Parser.m_CurrentGroup.m_pDependencies = m_ActiveIf.getFormula();
		m_ActiveIf = null;
	    }

	    m_Parser.newGroup();

	    try {
		m_Parser.m_CurrentGroup.setPriority( Integer.parseInt( atts.getValue( SpecParser.PRIORITY_ATTRIBUTE ) ) );
	    }
	    catch( Throwable t ) { }

	    // System.out.println( "new group!" );
	}
	else if ( name.equals( SpecParser.OBJECT_REF_TAG ) ) {

	    String sStateName = atts.getValue( SpecParser.NAME_ATTRIBUTE );

	    if ( m_Parser.isExistingObject( sStateName ) ) {

		GroupNode g = m_Parser.newGroup();

		g.m_Object = m_Parser.getExistingObject( sStateName );

		m_Parser.endGroup();

		return this;
	    }
	    else {
		m_Parser.addWarning( "Object ref exists for " + sStateName + " that has no corresponding existing object." );
	    }
	}
	else if ( name.equals( SpecParser.STATE_TAG ) ) {
	    GroupNode g = m_Parser.newGroup();

	    // System.out.println( "new state! : " +
	    //    atts.getValue( SpecParser.NAME_ATTRIBUTE) );

	    String sStateName = atts.getValue( SpecParser.NAME_ATTRIBUTE );

	    ApplianceState s = new ApplianceState();
	    s.m_sName = sStateName;

	    String access = atts.getValue( SpecParser.ACCESS_ATTRIBUTE );
	    s.m_bReadOnly = (access != null && access.equals( SpecParser.ACCESS_READ_ONLY ));

	    try {
		s.m_nPriority = Integer.parseInt( atts.getValue( SpecParser.PRIORITY_ATTRIBUTE ) );
	    }
	    catch( Throwable t ) { }

	    m_Parser.newState( s );

	    g.m_Object = s;
	}
	else if ( name.equals( SpecParser.COMMAND_TAG ) ) {
	    GroupNode g = m_Parser.newGroup();

	    // System.out.println( "new command! : " +
	    //     atts.getValue( SpecParser.NAME_ATTRIBUTE) );

	    String sCmdName = atts.getValue( SpecParser.NAME_ATTRIBUTE );

	    ApplianceCommand s = new ApplianceCommand();
	    s.m_sName = atts.getValue( SpecParser.NAME_ATTRIBUTE );

	    try {
		s.m_nPriority = Integer.parseInt( atts.getValue( SpecParser.PRIORITY_ATTRIBUTE ) );
	    }
	    catch( Throwable t ) { }

	    m_Parser.newState( s );

	    g.m_Object = s;

	}
	else if ( name.equals( SpecParser.EXPLANATION_TAG ) ) {
	    GroupNode g = m_Parser.newGroup();

	    // System.out.println( "new explanation! : " +
	    //     atts.getValue( SpecParser.NAME_ATTRIBUTE) );

	    String sExplName = atts.getValue( SpecParser.NAME_ATTRIBUTE );

	    ApplianceExplanation s = new ApplianceExplanation();
	    s.m_sName = atts.getValue( SpecParser.NAME_ATTRIBUTE );

	    try {
		s.m_nPriority = Integer.parseInt( atts.getValue( SpecParser.PRIORITY_ATTRIBUTE ) );
	    }
	    catch( Throwable t ) { }

	    m_Parser.newState( s );

	    g.m_Object = s;
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

	if ( name.equals( SpecParser.GROUP_TAG ) ) {

	    // System.out.println( "end group!" );

	    if ( m_Labels != null ) {
		m_Parser.m_CurrentGroup.m_Labels = m_Labels.getLabelLib();
		m_Labels = null;
	    }

	    if ( m_ActiveIf != null ) {
		m_Parser.m_CurrentGroup.m_pDependencies = m_ActiveIf.getFormula();
		m_ActiveIf = null;
	    }

	    m_Parser.endGroup();

	    return m_Parser.m_nDepth < SpecParser.GROUPINGS_DEPTH ?
		m_Parser.m_States[ SpecParser.GROUPINGS_PARSE_STATE ]
		: this;
	}

	return super.handleEndTag( name );
    }
}
