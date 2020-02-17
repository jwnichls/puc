/**
 * SpecParser.java
 * 
 * Uses the SAX API to parse a PUC specification file.
 *
 * Revision History
 * ----------------
 * 07/07/2001: (JWN) Created file and wrote initial parser
 * 
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.io.*;
import java.lang.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import edu.cmu.hcii.puc.AND;
import edu.cmu.hcii.puc.Appliance;
import edu.cmu.hcii.puc.ApplianceObject;
import edu.cmu.hcii.puc.ApplianceState;
import edu.cmu.hcii.puc.Dependency;
import edu.cmu.hcii.puc.DependencyFormula;
import edu.cmu.hcii.puc.GroupNode;
import edu.cmu.hcii.puc.LabelLibrary;
import edu.cmu.hcii.puc.PUCType;

import edu.cmu.hcii.puc.types.*;


// Class Definition

public class SpecParser extends DefaultHandler {
  
  //**************************
  // Parsing Function
  //**************************
  
  public static void parse( InputStream in,
			    Appliance   out ) {

      // System.out.println( in );
    
    // first parsing pass

    SpecParser handler = new SpecParser();

    SAXParserFactory factory = SAXParserFactory.newInstance();
    
    try {
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse( in, handler );
    }
    catch( Throwable t ) {
      t.printStackTrace();
    }

    // The second and third parsing passes operate recursively.
    
    // SECOND PARSING PASS (remove dependency hierarchy) -- This
    // second pass is used to clean up the parsing of dependencies.
    // So far, no group-level active-if blocks have been integrated
    // into the states within that group.

    fixDepend( handler.m_Root, null, null );

    // THIRD PARSING PASS (resolve names and create reverse dependency
    // lists) -- The third pass is an additional pass at cleaning up
    // the dependencies.  So far, no dependency is associated with its
    // state object (just a string name), and no state knows anything
    // about which objects are dependent on it.  This pass also checks
    // that values associated with each dependency has the correct type.

    Hashtable h = handler.m_ApplianceObjects;
    Enumeration e = h.elements();

    Hashtable hDList = new Hashtable();

    while( e.hasMoreElements() ) {
	ApplianceObject ao = (ApplianceObject)e.nextElement();

	resolveNames( ao.m_pDependencies, hDList, h, handler, ao );
    }

    Vector vDList = new Vector();
    e = hDList.elements();

    while( e.hasMoreElements() ) {
	ApplianceState as = (ApplianceState)e.nextElement();

	int s = as.getReverseDependencyCount();
	boolean bFound = false;

	for( int i = 0; i < vDList.size(); i++ ) {
	    if ( s >= ((ApplianceState)vDList.elementAt(i)).getReverseDependencyCount() ) {
		vDList.insertElementAt( as, i );
		bFound = true;
		break;
	    }
	}

	if ( !bFound )
	    vDList.addElement( as );
    }    

    // FOURTH PARSING PASS: Validate all of the Constraint objects.

    Enumeration e2 = handler.m_vConstraints.elements();
    while( e2.hasMoreElements() ) {
	Constraint pC = (Constraint)e2.nextElement();

	if ( pC.resolveState( h ) )
	    pC.getValueState().makeConstraintVariable();
    }

    // END PARSING
    
    if ( handler.isWarnings() ) 
      System.out.println( handler.getLogString() );

    out.setParseVariables( handler.m_sName, handler.m_Root, handler.m_ApplianceObjects, vDList );
  } 
  

  public static void fixDepend( GroupNode group, 
				DependencyFormula pFromRoot,
				DependencyFormula pFromParent ) {

    if ( group.m_Object != null ) {

      AND a = new AND();

      if ( group.m_Object.m_pDependencies != null )
	a.addFormula( group.m_Object.m_pDependencies );
      
      if ( pFromRoot != null && 
	   group.m_Parent.m_nDepIgnore != GroupNode.IGNORE_ALL) {
	  
	a.addFormula( pFromRoot );
      }
      
      if ( pFromParent != null && 
	   group.m_Parent.m_nDepIgnore < GroupNode.IGNORE_PARENT ) {

	a.addFormula( pFromParent );
      }

      group.m_Object.m_pDependencies = a.simplify();

      return;
    }

    DependencyFormula pNewRoot = null;

    if ( group.m_nDepIgnore != GroupNode.IGNORE_ALL ) {
	pNewRoot = new AND();

	if ( pFromRoot != null )
	    pNewRoot.addFormula( pFromRoot );

	if ( group.m_nDepIgnore < GroupNode.IGNORE_PARENT && 
	     pFromParent != null ) 
	    pNewRoot.addFormula( pFromParent );

	pNewRoot = pNewRoot.simplify();
    }

    if ( group.m_Children == null ) {
      System.out.print( "Group with no object or children?  " );

      String lbl = "";
      
      if ( group.m_Labels != null )
	lbl = group.m_Labels.getFirstLabel();

      System.out.println( lbl );

      group.m_Children = new Vector();
    }

    Enumeration e = group.m_Children.elements();

    while ( e.hasMoreElements() ) {
      GroupNode g = (GroupNode)e.nextElement();

      fixDepend( g, pNewRoot, group.m_pDependencies );
    }
  }


  public static void resolveNames( DependencyFormula df, Hashtable hDStates,
				   Hashtable hStates, SpecParser pParser,
				   ApplianceObject ao ) {

    if ( df == null ) return;

    Enumeration e = df.getFormulas();

    while( e.hasMoreElements() )
      resolveNames( (DependencyFormula)e.nextElement(), hDStates,
		    hStates, pParser, ao );

    e = df.getDependencies();

    while( e.hasMoreElements() ) {
      Dependency d = (Dependency)e.nextElement();

      try {
	  if (! d.resolveState( hStates ) ) {
	      pParser.addWarning( "Couldn't resolve a dependency's" + 
				  " state name! " + d.getStateName() );
	      continue;
	  }
      }
      catch( Throwable t ) {
	  pParser.addWarning( "A dependency has a value with the" + 
			      " wrong type for its state. " +
			      d.getStateName() + " " + 
			      d.getValueString() );
      }

      ApplianceState as = d.getState();

      as.addReverseDependency( ao );
      hDStates.put( as.m_sName, as );
    }
  }
  

  //**************************
  // Constants
  //**************************
  
  // Parse State Constants
  public static final int START_PARSE_STATE       = 0;
  public static final int GROUPINGS_PARSE_STATE   = 1;
  public static final int GROUP_PARSE_STATE       = 2;
  public static final int STATE_PARSE_STATE       = 3;
  public static final int PRIORITY_PARSE_STATE    = 4;
  public static final int SPEC_PARSE_STATE        = 5;
  public static final int TYPE_PARSE_STATE        = 6;
  public static final int VALUE_SPC_PARSE_STATE   = 7;
  public static final int VALUE_LBL_PARSE_STATE   = 8;
  public static final int EXPCT_SPC_PARSE_STATE   = 9;
  public static final int COMMAND_PARSE_STATE     = 10;
  public static final int EXPLANATION_PARSE_STATE = 11;
  public static final int NUM_PARSE_STATES        = 12;
  
  // spec xml file tag names
  public static final String SPEC_TAG         = "spec";
  public static final String GROUPINGS_TAG    = "groupings";
  public static final String GROUP_TAG        = "group";
  public static final String STATE_TAG        = "state";
  public static final String TYPE_TAG         = "type";
  public static final String PRIORITY_TAG     = "priority";
  public static final String VALUE_SPACE_TAG  = "valueSpace";
  public static final String VALUE_LABEL_TAG  = "valueLabels";
  public static final String EXPCT_SPACE_TAG  = "expectedValues";
  public static final String MAP_TAG          = "map";
  public static final String BOOLEAN_TAG      = "boolean";
  public static final String INTEGER_TAG      = "integer";
  public static final String FIXEDPT_TAG      = "fixedpt";
  public static final String FLOATINGPT_TAG   = "floatingpt";
  public static final String STRING_TAG       = "string";
  public static final String ENUMERATED_TAG   = "enumerated";
  public static final String MINIMUM_TAG      = "min";
  public static final String MAXIMUM_TAG      = "max";
  public static final String INCREMENT_TAG    = "incr";
  public static final String POINTPOS_TAG     = "pointpos";
  public static final String ACTIVEIF_TAG     = "active-if";
  public static final String LABEL_TAG        = "label";
  public static final String LABELS_TAG       = "labels";
  public static final String COMMAND_TAG      = "command";
  public static final String AND_TAG          = "and";
  public static final String OR_TAG           = "or";
  public static final String EQUALS_TAG       = "equals";
  public static final String LESSTHAN_TAG     = "lessthan";
  public static final String GREATERTHAN_TAG  = "greaterthan";
  public static final String ITEMS_TAG        = "items";
  public static final String REFVALUE_TAG     = "refvalue";
  public static final String REFSTRING_TAG    = "refstring";
  public static final String CUSTOM_TAG       = "custom";
  public static final String EXPLANATION_TAG  = "explanation";
  public static final String PHONETIC_TAG     = "phonetic";
  public static final String TTS_TAG          = "text-to-speech";
  public static final String OBJECT_REF_TAG   = "object-ref";
  public static final String APPLY_TYPE_TAG   = "apply-type";
  public static final String NUMBER_TAG       = "number";

  // spec xml file attribute names
  public static final String NAME_ATTRIBUTE   = "name";
  public static final String INDEX_ATTRIBUTE  = "index";
  public static final String ACCESS_ATTRIBUTE = "access";
  public static final String IGNORE_ATTRIBUTE = "ignore";
  public static final String STATE_ATTRIBUTE  = "state";
  public static final String ENABLE_ATTRIBUTE = "enable";
  public static final String PRIORITY_ATTRIBUTE = "priority";
  public static final String TEXT_ATTRIBUTE   = "text";
  public static final String RECORDING_ATTRIBUTE = "recording";

  public static final String IGNORE_ATTR_ALL  = "all";
  public static final String IGNORE_ATTR_PRNT = "parent";

  public static final String ACCESS_READ_ONLY = "ReadOnly";
  public static final String ACCESS_WRITE_ONLY= "WriteOnly";
  
  // Parsing Constants
  protected static final int GROUPINGS_DEPTH = 3;
  
  
  //**************************
  // Constructor
  //**************************
  
  protected SpecParser() { 
    
    m_States = new ParseStateTable[ NUM_PARSE_STATES ];
    
    m_LogString = "";
    m_bWarnings = false;
    
    m_States[START_PARSE_STATE]       = new StartStateTable( this );
    m_States[SPEC_PARSE_STATE]        = new SpecTable( this );
    m_States[GROUPINGS_PARSE_STATE]   = new GroupingTable( this );
    m_States[GROUP_PARSE_STATE]       = new GroupTable( this );
    m_States[STATE_PARSE_STATE]       = new StateTable( this );
    m_States[PRIORITY_PARSE_STATE]    = new PriorityTable( this );
    m_States[TYPE_PARSE_STATE]        = new TypeTable( this );
    m_States[VALUE_SPC_PARSE_STATE]   = new ValueSpaceTable( this );
    m_States[VALUE_LBL_PARSE_STATE]   = new ValueLabelTable( this );
    m_States[EXPCT_SPC_PARSE_STATE]   = new ValueSpaceTable( this );
    m_States[COMMAND_PARSE_STATE]     = new CommandTable( this );
    m_States[EXPLANATION_PARSE_STATE] = new ExplanationTable( this );

    m_ApplianceObjects = new Hashtable();
    m_TypeObjects = new Hashtable();
    m_Root = new GroupNode();
    m_CurrentGroup = m_Root;
    m_vConstraints = new Vector();
  }
  
  
  //**************************
  // Member Variables
  //**************************
  
  protected ParseStateTable m_CurrentState;
  
  protected ParseStateTable[] m_States; // central storage location for
                                        // states so that transition tables
                                        // can find them (indexed by string) 
  
  protected int m_nDepth;               // Depth within the hierarchy of
                                        // the document.
  
  protected String m_LogString;         // maintains a list of all log
                                        // messages
  protected boolean m_bWarnings;  
  
  protected GroupNode m_CurrentGroup;
  protected Hashtable m_ApplianceObjects;
  protected Hashtable m_TypeObjects;
  protected GroupNode m_Root;

  protected String m_sName;

  protected Vector m_vConstraints;      // All Constraint objects created 
                                        // by the parser. Used for validation.
  
  //**************************
  // Data Structure Helpers
  //**************************

  public void setName( String s ) {

    m_sName = s;
  }
  
  public GroupNode newGroup() {
    
    GroupNode g = new GroupNode();
    
    g.m_Parent = m_CurrentGroup;
    
    if ( m_CurrentGroup != null ) {
      if ( m_CurrentGroup.m_Children == null )
	m_CurrentGroup.m_Children = new Vector();
	    
      m_CurrentGroup.m_Children.addElement( g );
    }
    
    m_CurrentGroup = g;
    
    return m_CurrentGroup;
  }
  
  public void endGroup() {
    
    m_CurrentGroup = m_CurrentGroup.m_Parent;
  }
  
  public void newState( ApplianceObject s ) {

    m_ApplianceObjects.put( s.m_sName, s );
  }

  public boolean isExistingObject( String name ) {

    return m_ApplianceObjects.get( name ) != null;
  }
  
  public ApplianceObject getExistingObject( String name ) {

    return (ApplianceObject)m_ApplianceObjects.get( name );
  }

  public void newType( PUCType t ) {
    
    m_TypeObjects.put( t.getName(), t );
  }

  public boolean isExistingType( String name ) {

    return m_TypeObjects.get( name ) != null;
  }
  
  public PUCType getExistingType( String name ) {

    return (PUCType)((PUCType)m_TypeObjects.get( name )).clone();
  }

  /**
   * addReferenceValue()
   * 
   * Maintained for backwards compatibility...  Use addConstraint instead.
   * @deprecated
   */
  public void addReferenceValue( ReferenceValue pRV ) {

    m_vConstraints.addElement( pRV );
  }

  public void addConstraint( Constraint pC ) {

    m_vConstraints.addElement( pC );
  }
  

  //**************************
  // Public Data Accessors
  //**************************
  
  public boolean isWarnings() {
    
    return m_bWarnings;
  }
  
  public String getLogString() {
    
    return m_LogString;
  }
  
  
  //**************************
  // Parsing Event Handlers
  //**************************
  
  public void startDocument() 
    throws SAXException {
      
      m_CurrentState = m_States[ START_PARSE_STATE ];
      m_nDepth = 0;
      
      // System.out.println( "Spec Parsing Beginning..." );
  }
  
  public void endDocument() 
    throws SAXException {
      
      // System.out.println( "End Spec." );
  }
  
  public void startElement( String namespaceURI,
			    String localName,
			    String qName,
			    Attributes atts )
    throws SAXException {

      //System.out.println( "Start Tag: " + qName );
      
      m_nDepth++;
      m_CurrentState = m_CurrentState.handleStartTag( qName, atts );
  }
  
  public void endElement( String namespaceURI,
			  String localName,
			  String qName )
    throws SAXException {
      
      //System.out.println( "End Tag: " + qName );

      m_nDepth--;
      m_CurrentState = m_CurrentState.handleEndTag( qName );
  }
  
  public void characters( char[] ch,
			  int start,
			  int length )
    throws SAXException {
      
      StringBuffer sb = new StringBuffer();
      sb.append(ch,start,length);
      
      String s = sb.toString();
      
      if ( s.trim().length() != 0 )
	m_CurrentState.characters( s );
  }
  
  public void skippedEntity( String name ) 
    throws SAXException {
      
      m_LogString += "Entity Skipped: " + name + "\n";
      m_bWarnings = true;
  }
  
  public void error( SAXParseException e ) 
    throws SAXException {
      
      m_LogString += "Error: " + e.getMessage() + "\n";
      m_bWarnings = true;
  }
  
  public void fatalError( SAXParseException e ) 
    throws SAXException {
      
      m_LogString += "Fatal Error: " + e.getMessage() + "\nParse Halted."; 
      m_bWarnings = true;
  }
  
  public void warning( SAXParseException e ) 
    throws SAXException {
      
      m_LogString += "Warning: " + e.getMessage() + "\n";
      m_bWarnings = true;
  }
  

  //**************************
  // Public Methods
  //**************************

  public void addWarning( String msg ) {

    m_LogString += "External Warning: " + msg + "\n";
    m_bWarnings = true;
  }
}
