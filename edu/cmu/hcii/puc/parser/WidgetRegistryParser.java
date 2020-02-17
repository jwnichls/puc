/**
 * WidgetRegistryParser.java
 *
 * See uigen.WidgetRegistry for more information on the Widget
 * Registry itself. 
 *
 * This parser creates a WidgetRegistry object from an XML file.  A
 * WidgetRegistry is basically a decision tree that gives the decisions
 * that are used to pick widgets for ApplianceObjects.  Each decision
 * is represented by a sub-class of the registry.Decision object.  These
 * registry.Decisions are dynamically loaded.
 *
 * Revision History
 * ----------------
 * 07/07/2001: (JWN) Created file.
 */

// Package Definition

package edu.cmu.hcii.puc.parser;


// Import Declarations

import java.io.*;

import java.lang.*;

import java.util.Enumeration;
import java.util.Hashtable;

import edu.cmu.hcii.puc.cio.*;
import edu.cmu.hcii.puc.registry.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;


// Class Definition

public class WidgetRegistryParser extends Object {

    //**************************
    // Dynamic Loading Code
    //**************************

    protected static Hashtable decisionFactories;

    static {
	decisionFactories = new Hashtable();
    }

    public static void addDecisionFactory( String name, DecisionFactory f ) {

	decisionFactories.put( name, f );
    }


    //**************************
    // Constants
    //**************************

    protected static final String CONDITION_TAG       = "condition";
    protected static final String DECISION_TAG        =	"decision";
    protected static final String DECISION_LIB_TAG    =	"decision-library";
    protected static final String TYPE_TAG            = "type";
    protected static final String WIDGET_REGISTRY_TAG =	"widget-registry";
    protected static final String WIDGET_TAG          = "widget";

    protected static final String DEF_PACKAGE_ATTRIB  = "default-package";
    protected static final String NAME_ATTRIB         = "name";
    protected static final String TYPE_ATTRIB         = "type";
    protected static final String VALUE_ATTRIB        = "value";


    //**************************
    // Member Variables
    //**************************

    public static String m_sDefaultPackage;


    //**************************
    // Static Parsing Function
    //**************************

    public static WidgetRegistry parse( String sFilename ) {

	try {
	    return parse( new FileInputStream( sFilename ) );
	}
	catch( Exception e ) {
	    e.printStackTrace();
	    System.err.println( "Registry parsing failed (file not found)." );
	}

	return null;
    }

    public static WidgetRegistry parse( Class pSrc, String sResourceName ) {

	return parse( pSrc.getResourceAsStream( sResourceName ) );
    }

    public static WidgetRegistry parse( InputStream pIn ) {
	try {
	    Document pDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( pIn );

	    // find the WIDGET_REGISTRY_TAG

	    Node pWidReg = findFirstTag( pDoc, WIDGET_REGISTRY_TAG );

	    String sRegName = pWidReg.getAttributes().getNamedItem( NAME_ATTRIB ).getNodeValue();
	    m_sDefaultPackage = pWidReg.getAttributes().getNamedItem( DEF_PACKAGE_ATTRIB ).getNodeValue();
	    
	    System.out.println( "Parsing widget registry: " + sRegName );

	    // parse the decision-library and dynamically load the
	    // appropriate decision objects 

	    loadDecisionLibrary( pWidReg );

	    // iterate through the file and build the decision tree
	    // data structure

	    Decision pD = decisionHelper( findFirstTag( pWidReg, DECISION_TAG ) );

	    // System.out.println( pD.toString() );

	    System.out.println( "Registry parse complete." );

	    return new WidgetRegistry( pD );
	}
	catch( Throwable t ) {

	    t.printStackTrace();
	    System.err.println( "Registry parsing failed." );
	}

	return null;
    }

    public static Node findFirstTag( Node pNode, String sTagName ) {
	
	NodeList pList = pNode.getChildNodes();

	for( int i = 0; i < pList.getLength(); i++ ) {

	    Node n = pList.item(i);
	    if ( sTagName.equals( n.getNodeName() ) ) return n;

	    n = findFirstTag( n, sTagName );
	    if ( n != null ) return n;
	}	

	return null;
    }

    public static void loadDecisionLibrary( Node pWidReg ) {
	
	Node pLibNode = findFirstTag( pWidReg, DECISION_LIB_TAG );

	NodeList pList = pLibNode.getChildNodes();
	for( int i = 0; i < pList.getLength(); i++ ) {
	    Node n = pList.item(i);
	    if ( n.getNodeName() != null &&
		 n.getNodeName().equals( TYPE_TAG ) ) {

		NodeList pTypeList = n.getChildNodes();
		for( int j = 0; j < pTypeList.getLength(); j++ ) {
		    if ( pTypeList.item(j).getNodeType() == Node.TEXT_NODE ) {
			// System.out.println( "Found decision: " + pTypeList.item(j).getNodeValue() );

			try {
			    Class.forName( pTypeList.item(j).getNodeValue() );
			}
			catch( Exception e ) {
			    System.err.println( "Fatal Error! Decision object " +
						pTypeList.item(j).getNodeValue() + "does not exist." );
			    System.exit(-1);
			}
		    }
		}
	    }
	}
    }

    public static Decision decisionHelper( Node pDNode ) {

	NamedNodeMap pAttribs = pDNode.getAttributes();

	String sType = pAttribs.getNamedItem( TYPE_ATTRIB ).getNodeValue();
	// System.out.println( "Found a decision of type: " + sType );

	Hashtable pDTable = new Hashtable();
	
	NodeList pList = pDNode.getChildNodes();
	for( int i = 0; i < pList.getLength(); i++ ) {
	    if ( pList.item(i).getNodeName() != null &&
		 pList.item(i).getNodeName().equals( CONDITION_TAG ) )
		conditionHelper( pList.item(i), pDTable );
	}

	DecisionFactory pDF = (DecisionFactory)decisionFactories.get( sType );

	if ( pDF == null ) {
	    System.err.println( "Fatal Error!  No decision of type: " + sType );
	    System.exit(-1);
	}

	return pDF.createDecision( pDTable );
    }

    public static void conditionHelper( Node pCNode, Hashtable pDTable ) {

	NamedNodeMap pAttribs = pCNode.getAttributes();

	String sValue = pAttribs.getNamedItem( VALUE_ATTRIB ).getNodeValue();

	// System.out.println( "Found a condition of value: " + sValue );
	
	Decision pD = null;

	NodeList pList = pCNode.getChildNodes();
	for( int i = 0; i < pList.getLength(); i++ ) {
	    Node n = pList.item(i);

	    if ( n.getNodeName() != null ) {
		if ( n.getNodeName().equals( DECISION_TAG ) )
		    pD = decisionHelper( n );
	        else if ( n.getNodeName().equals( WIDGET_TAG ) )
		    pD = widgetHelper( n );
	    }
	}

	if ( pD == null ) {
	    System.err.println( "Fatal Error! Unknown or nonexistent tag." );
	    System.exit(-1);
	}

	pDTable.put( sValue, pD );
    }

    public static WidgetDecision widgetHelper( Node pWNode ) {
	
	NodeList pList = pWNode.getChildNodes();
	for( int i = 0; i < pList.getLength(); i++ ) {
	    if ( pList.item(i).getNodeType() == Node.TEXT_NODE ) {
		// System.out.println( "Found widget: " + pList.item(i).getNodeValue() );
		return new WidgetDecision( pList.item(i).getNodeValue() );
	    }
	}
	
	System.err.println( "Fatal Error!  Widget with no name!" );
	System.exit(-1);

	return null;
    }


    //**************************
    // Static Testing Function
    //**************************

    public static void main( String[] args ) {

	parse( "test.xml" );
    }
}

