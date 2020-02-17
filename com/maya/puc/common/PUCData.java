/*
 * PUCData.java
 * 
 * This set of classes represents the data that is being sent over the
 * wire between PUC clients and servers.  This representation allows
 * simple state/value pairs (as before) and also complex list data as
 * allowed by newer versions of the PUC specification.
 *
 * Jeff Nichols
 * October 18, 2003
 */

// Package Definition

package com.maya.puc.common;


// Import Declarations

import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ListIterator;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;


// Class Definition

/**
 * This class represent arbitrary data sent between PUC devices.
 *
 * @author Jeff Nichols
 * @version $Id: PUCData.java,v 1.4 2004/07/23 19:34:20 jeffreyn Exp $
 */

public abstract class PUCData {

    /*
     * Constructor
     */

    protected PUCData() {}

    protected PUCData( Element xml ) throws IOException {

	parseContents( xml );
    }


    /*
     * Member Variables
     */

    public abstract void writeXML( Element xmlOut );
    public abstract void parseContents( Element xml ) 
	throws IOException;

    public boolean isValue() {

	return false;
    }

    
    /*
     * Static Parsing Method
     */

    public static PUCData Parse( Element xml, boolean topLevel ) 
	throws IOException {

	if ( xml.getName().equals( Value.VALUE_TAG ) ) {

	    if ( topLevel ) {
		Attribute stateAttrib = xml.getAttribute( Value.STATE_ATTRIBUTE );
		if ( stateAttrib == null )
		    return ParseOldStateValue( xml.getParent() );
		else
		    return new Value( xml );
	    }
	    else
		return new MultipleValues( xml.getParent() );
	}
	else if ( xml.getName().equals( Value.STATE_ATTRIBUTE ) ) {

	    if ( !topLevel )
		throw new IOException( Value.STATE_ATTRIBUTE + " is only allowed as an element name on the top level." );

	    return ParseOldStateValue( xml.getParent() );
	}
	else if ( xml.getName().equals( ListData.DATA_TAG ) ) {

	    return new ListData( xml );
	}
	else if ( xml.getName().equals( ListData.CHANGE_TAG ) ) {

	    return new ListData( xml );
	}
	else if ( xml.getName().equals( ListInsert.INSERT_TAG ) ) {

	    return new ListInsert( xml );
	}
	else if ( xml.getName().equals( ListReplace.REPLACE_TAG ) ) {

	    return new ListReplace( xml );
	}
	else if ( xml.getName().equals( ListDelete.DELETE_TAG ) ) {

	    return new ListDelete( xml );
	}

	return null;
    }

    public static Value ParseOldStateValue( Element xml ) 
	throws IOException {

	String state = null;
	String value = null;
	boolean defined = false;

	Element elState = xml.getChild( Value.STATE_ATTRIBUTE );
	if (elState == null)
	    throw new IOException( Value.STATE_ATTRIBUTE + " element must be defined." );

	state = elState.getText();

	if ( state == null )
	    throw new IOException( Value.STATE_ATTRIBUTE + " element must be defined." ); 
	
	Element elValue = xml.getChild( Value.VALUE_TAG );
	if (elValue == null)
	    throw new IOException( Value.VALUE_TAG + " element must be defined." );

	Element elUndefined = elValue.getChild( Value.UNDEFINED_TAG );
	if ( elUndefined != null ) {
	    defined = false;
	    value = null;
	}
	else {
	    value = elValue.getText();
	    defined = true;

	    if ( value == null )
		throw new IOException( Value.VALUE_TAG + " element must contain text." );
	}

	if ( defined )
	    return new Value( state, value );
	else
	    return new Value( state );
    }


    /*
     * Inner Classes
     */

    public static class Value extends PUCData {

	/*
	 * Constants
	 */
	
	public final static String VALUE_TAG       = "value";
	public final static String STATE_ATTRIBUTE = "state";
	public final static String UNDEFINED_TAG   = "undefined";
	
	
	/*
	 * Member Variables
	 */
	
	protected String  m_sState;
	protected String  m_sValue;
	protected boolean m_bDefined;
	
	
	/*
	 * Constructors
	 */
	
	public Value( String state, String value ) {
	    
	    m_sState = state;
	    m_sValue = value;
	    m_bDefined = true;
	}

	public Value( String state ) {

	    m_sState = state;
	    m_bDefined = false;
	}

	public Value( Element xml ) throws IOException {

	    super( xml );
	}


	/*
	 * Member Methods
	 */

	public String getState() {

	    return m_sState;
	}

	public void setState( String state ) {

	    m_sState = state;
	}

	public String getValue() {

	    return m_sValue;
	}

	public void setValue( String value ) {

	    m_sValue = value;
	    
	    m_bDefined = ( value != null );
	}

	public boolean getDefined() {

	    return m_bDefined;
	}

	public boolean isValue() {

	    return true;
	}

	public void writeXML( Element xmlout ) {

	    Element elValue = new Element( VALUE_TAG );
	    elValue.setAttribute( STATE_ATTRIBUTE, m_sState );

	    if ( m_bDefined )
		elValue.setText( m_sValue );
	    else {
		Element elUndefined = new Element( UNDEFINED_TAG );
		elValue.addContent( elUndefined );
	    }

	    xmlout.addContent( elValue );
	}

	public void writeOldXML( Element xmlout ) {

	    Element elState = new Element( STATE_ATTRIBUTE );
	    elState.setText( m_sState );
	    xmlout.addContent( elState );

	    Element elValue = new Element( VALUE_TAG );
	    if ( m_bDefined )
		elValue.setText( m_sValue );
	    else {
		Element elUndefined = new Element( UNDEFINED_TAG );
		elValue.addContent( elUndefined );
	    }
	    xmlout.addContent( elValue );
	}

	public void parseContents( Element xml ) throws IOException {

	    if ( !xml.getName().equals( VALUE_TAG ) )
		throw new IOException( "Value does not start with " + VALUE_TAG );
	    
	    Attribute stateAttrib = xml.getAttribute( STATE_ATTRIBUTE );
	    if ( stateAttrib == null )
		throw new IOException( VALUE_TAG + " tag does not include required " + STATE_ATTRIBUTE + " attribute." );
	    m_sState = stateAttrib.getValue();

	    Element elUndefined = xml.getChild( UNDEFINED_TAG );
	    if ( elUndefined != null ) {

		m_bDefined = false;
		m_sValue = null;
	    }
	    else {

		m_bDefined = true;
		m_sValue = xml.getText();

		if ( m_sValue == null )
		    throw new IOException( "No value given for " + VALUE_TAG );
	    }
	}
    }

    public static class MultipleValues extends PUCData {

	/*
	 * Member Variables
	 */

	protected Hashtable m_hValues;


	/*
	 * Constructors
	 */

	public MultipleValues() {

	    m_hValues = new Hashtable();
	}

	public MultipleValues( Element xml ) throws IOException {

	    super( xml );
	}


	/*
	 * Member Variables
	 */

	public Value get( String stateName ) {

	    return (Value)m_hValues.get( stateName );
	}

	public void put( Value value ) {

	    m_hValues.put( value.getState(), value );
	}

	public void remove( String stateName ) {

	    m_hValues.remove( stateName );
	}

	public int size() {

	    return m_hValues.size();
	}

	public void parseContents( Element xml ) throws IOException {

	    List l = xml.getChildren( Value.VALUE_TAG );
	    m_hValues = new Hashtable( l.size() );

	    for( int i = 0; i < l.size(); i++ )
		put( new Value( (Element)l.get( i ) ) );
	}

	public void writeXML( Element xmlout ) {

	    Enumeration e = m_hValues.elements();
	    while( e.hasMoreElements() )
		((Value)e.nextElement()).writeXML( xmlout );
	}
    }

    public static abstract class Elements extends PUCData {

	/*
	 * Constants
	 */

	public final static String ELEMENT_TAG     = "el";
	public final static String STATE_ATTRIBUTE = "state";


	/* 
	 * Member Variables
	 */

	protected Vector m_vElements;
	protected String m_sState;


	/*
	 * Constructors
	 */

	protected Elements( String state ) {
	    
	    m_sState = state;
	    m_vElements = new Vector();
	}

	protected Elements( Element xml ) throws IOException {

	    super( xml );
	}


	/*
	 * Member Methods
	 */

	public String getState() {

	    return m_sState;
	}

	public void setState( String state ) {

	    m_sState = state;
	}

	public PUCData get( int index ) {

	    return (PUCData)m_vElements.get( index );
	}

	public int size() {

	    return m_vElements.size();
	}

	public void addElement( PUCData data ) {

	    m_vElements.addElement( data );
	}

	public void insertElementAt( PUCData data, int index ) {

	    m_vElements.insertElementAt( data, index );
	}

	public void remove( int index ) {

	    m_vElements.remove( index );
	}

	public void writeXML( Element xmlout ) {

	    Enumeration e = m_vElements.elements();
	    while( e.hasMoreElements() ) {
		
		Element elElement = new Element( ELEMENT_TAG );
		((PUCData)e.nextElement()).writeXML( elElement );
		xmlout.addContent( elElement );
	    }
	}

	public void parseContents( Element xml ) throws IOException {

	    List l = xml.getChildren( ELEMENT_TAG );
	    m_vElements = new Vector( l.size() );

	    for ( int i = 0; i < l.size(); i++ ) {

		Element elElement = (Element)l.get( i );
		addElement( PUCData.Parse( (Element)elElement.getChildren().get( 0 ), false ) );
	    }
	}
    }

    public static class ListData extends Elements {

	/*
	 * Constants
	 */

	public final static String DATA_TAG        = "data";
	public final static String CHANGE_TAG      = "change";
	public final static String INDEX_ATTRIBUTE = "index";

	public final static int SELTYPE_ONE        = 0;
	public final static int SELTYPE_ALL        = 1;


	/*
	 * Member Variables
	 */

	protected int m_nSelectType;
	protected int m_nIndex;


	/*
	 * Constructors
	 */

	public ListData( String state ) {

	    super( state );
	    setIndex( -1 );
	}

	public ListData( String state, int index ) {

	    super( state );
	    setIndex( index );
	}

	public ListData( Element xml ) throws IOException {

	    super( xml );
	}


	/*
	 * Member Methods
	 */

	public int getSelectionType() {

	    return m_nSelectType;
	}

	public int getIndex() {

	    return m_nIndex;
	}

	public void setIndex( int index ) {

	    m_nIndex = index;

	    if ( m_nIndex < 0 )
		m_nSelectType = SELTYPE_ALL;
	    else
		m_nSelectType = SELTYPE_ONE;
	}

	public void parseContents( Element xml ) throws IOException {

	    if ( !(xml.getName().equals( DATA_TAG ) ||
                   xml.getName().equals( CHANGE_TAG ) ) )
		throw new IOException( "Incorrect parsing function called." );

	    try {
		Attribute attr = xml.getAttribute( STATE_ATTRIBUTE );
		m_sState = attr.getValue();

		attr = xml.getAttribute( INDEX_ATTRIBUTE );
		if ( attr == null )
		    setIndex( -1 );
		else
		    setIndex( Integer.parseInt( attr.getValue() ) );

		super.parseContents( xml );
	    }
	    catch( Exception e ) {

		throw new IOException( e.getMessage() );
	    }
	}

	public void writeXML( Element xmlout ) {

	    Element elData = null;

	    if ( m_nSelectType == SELTYPE_ONE )
		elData = new Element( CHANGE_TAG );
	    else
		elData = new Element( DATA_TAG );

	    elData.setAttribute( STATE_ATTRIBUTE, m_sState );
	    if ( m_nSelectType == SELTYPE_ONE )
		elData.setAttribute( INDEX_ATTRIBUTE, m_nIndex + "" );

	    super.writeXML( elData );

	    xmlout.addContent( elData );
	}
    }

    public static class ListInsert extends Elements {

	/*
	 * Constants
	 */

	public final static String INSERT_TAG      = "insert";
	public final static String AFTER_ATTRIBUTE = "after";


	/*
	 * Member Variables
	 */

	protected int m_nAfter;


	/*
	 * Constructors
	 */

	public ListInsert( String state, int after ) {

	    super( state );
	    m_nAfter = after;
	}

	public ListInsert( Element xml ) throws IOException {

	    super( xml );
	}


	/*
	 * Member Methods
	 */

	public int getAfter() {

	    return m_nAfter;
	}

	public void setAfter( int after ) {

	    m_nAfter = after;
	}

	public void parseContents( Element xml ) throws IOException {

	    if ( !xml.getName().equals( INSERT_TAG ) )
		throw new IOException( "Incorrect parsing function called." );

	    try {
		Attribute attr = xml.getAttribute( STATE_ATTRIBUTE );
		m_sState = attr.getValue();
		
		attr = xml.getAttribute( AFTER_ATTRIBUTE );
		setAfter( Integer.parseInt( attr.getValue() ) );

		super.parseContents( xml );
	    }
	    catch( Exception e ) {

		throw new IOException( e.getMessage() );
	    }
	}

	public void writeXML( Element xmlout ) {

	    Element elInsert = new Element( INSERT_TAG );
	    elInsert.setAttribute( STATE_ATTRIBUTE, m_sState );
	    elInsert.setAttribute( AFTER_ATTRIBUTE, m_nAfter + "" );

	    super.writeXML( elInsert );

	    xmlout.addContent( elInsert );
	}
    }

    public static class ListReplace extends Elements {

	/*
	 * Constants
	 */

	public final static String REPLACE_TAG      = "replace";
	public final static String BEGIN_ATTRIBUTE  = "begin";
	public final static String LENGTH_ATTRIBUTE = "length";


	/*
	 * Member Variables
	 */

	protected int m_nBegin;
	protected int m_nLength;


	/*
	 * Constructors
	 */

	public ListReplace( String state, int begin, int length ) {

	    super( state );
	    m_nBegin = begin;
	    m_nLength = length;
	}

	public ListReplace( Element xml ) throws IOException {

	    super( xml );
	}


	/*
	 * Member Methods
	 */

	public int getBegin() {

	    return m_nBegin;
	}

	public void setBegin( int begin ) {

	    m_nBegin = begin;
	}

	public int getLength() {

	    return m_nLength;
	}

	public void setLength( int length ) {

	    m_nLength = length;
	}

	public void parseContents( Element xml ) throws IOException {

	    if ( !xml.getName().equals( REPLACE_TAG ) )
		throw new IOException( "Incorrect parsing function called." );

	    try {
		Attribute attr = xml.getAttribute( STATE_ATTRIBUTE );
		m_sState = attr.getValue();

		attr = xml.getAttribute( BEGIN_ATTRIBUTE );
		setBegin( Integer.parseInt( attr.getValue() ) );

		attr = xml.getAttribute( LENGTH_ATTRIBUTE );
		setLength( Integer.parseInt( attr.getValue() ) );

		super.parseContents( xml );
	    }
	    catch( Exception e ) {

		throw new IOException( e.getMessage() );
	    }
	}

	public void writeXML( Element xmlout ) {

	    Element elReplace = new Element( REPLACE_TAG );
	    elReplace.setAttribute( STATE_ATTRIBUTE, m_sState );
	    elReplace.setAttribute( BEGIN_ATTRIBUTE, m_nBegin + "" );
	    elReplace.setAttribute( LENGTH_ATTRIBUTE, m_nLength + "" );

	    super.writeXML( elReplace );

	    xmlout.addContent( elReplace );
	}
    }

    public static class ListDelete extends PUCData {

	/*
	 * Constants
	 */

	public final static String DELETE_TAG       = "delete";
	public final static String BEGIN_ATTRIBUTE  = "begin";
	public final static String LENGTH_ATTRIBUTE = "length";


	/*
	 * Member Variables
	 */

	protected int    m_nBegin;
	protected int    m_nLength;
	protected String m_sState;


	/*
	 * Constructors
	 */

	public ListDelete( String state, int begin, int length ) {

	    m_sState = state;
	    m_nBegin = begin;
	    m_nLength = length;
	}

	public ListDelete( Element xml ) throws IOException {

	    super( xml );
	}


	/*
	 * Member Methods
	 */

	public String getState() {

	    return m_sState;
	}

	public void setState( String state ) {

	    m_sState = state;
	}

	public int getBegin() {

	    return m_nBegin;
	}

	public void setBegin( int begin ) {

	    m_nBegin = begin;
	}

	public int getLength() {

	    return m_nLength;
	}

	public void setLength( int length ) {

	    m_nLength = length;
	}

	public void parseContents( Element xml ) throws IOException {

	    if ( !xml.getName().equals( DELETE_TAG ) )
		throw new IOException( "Incorrect parsing function called." );

	    try {
		Attribute attr = xml.getAttribute( Elements.STATE_ATTRIBUTE );
		m_sState = attr.getValue();

		attr = xml.getAttribute( BEGIN_ATTRIBUTE );
		setBegin( Integer.parseInt( attr.getValue() ) );

		attr = xml.getAttribute( LENGTH_ATTRIBUTE );
		setLength( Integer.parseInt( attr.getValue() ) );
	    }
	    catch( Exception e ) {

		throw new IOException( e.getMessage() );
	    }
	}

	public void writeXML( Element xmlout ) {

	    Element elDelete = new Element( DELETE_TAG );
	    elDelete.setAttribute( Elements.STATE_ATTRIBUTE, m_sState );
	    elDelete.setAttribute( BEGIN_ATTRIBUTE, m_nBegin + "" );
	    elDelete.setAttribute( LENGTH_ATTRIBUTE, m_nLength + "" );

	    xmlout.addContent( elDelete );
	}
    }    
}



