/**
 * SpecGenerator.java
 *
 * A collection of static functions for automatically generating the text
 * of a specification file.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.lang.*;

import edu.cmu.hcii.puc.parser.SpecParser;


// Class Definition

public class SpecGenerator extends Object {

    //**************************
    // Constants
    //**************************

    protected static final String XML_INIT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    protected static final String DOCTYPE = "<!DOCTYPE spec SYSTEM \"http://www-2.cs.cmu.edu/~jeffreyn/controller/pucspec.dtd\">";
    protected static final String NEWLINE = "\n";


    //**************************
    // Constructor
    //**************************

    /**
     * SpecGenerator()
     * 
     * This class is purely a collection of static methods and as
     * such it cannot be instantiated.
     */
    private SpecGenerator() { }


    //**************************
    // Static Utility Methods
    //**************************

    public static String startTagify( String tag ) {

	return new StringBuffer( "<" ).append( tag ).append( ">" ).toString();
    }

    public static String terminalTagify( String tag ) {

	return new StringBuffer( "<" ).append( tag ).append( "/>" )
	       .append( NEWLINE ).toString();
    }

    public static String endTagify( String tag ) {

	return new StringBuffer( "</" ).append( tag ).append( ">" ).toString();
    }

    public static String genericNoAttribTag( String tag, String contents ) {

	return new StringBuffer( NEWLINE ).append( startTagify( tag ) )
	       .append( contents ).append( endTagify( tag ) )
	       .toString();
    }

    public static String genericOneAttribTag( String tag, String attrib,
					      String attribValue,
					      String contents ) {

	return new StringBuffer( NEWLINE ).append( "<" ).append( tag )
	       .append( " " ).append( attrib ).append( "=\"" )
	       .append( attribValue ).append( "\">" ).append( contents )
	       .append( endTagify( tag ) ).toString();
    }

    public static String genericTwoAttribTag( String tag, String attrib1,
					      String attrib1Value,
					      String attrib2,
					      String attrib2Value,
					      String contents ) {

	return new StringBuffer( NEWLINE ).append( "<" ).append( tag )
	       .append( " " ).append( attrib1 ).append( "=\"" )
	       .append( attrib1Value ).append( "\" " ).append( attrib2 )
	       .append( "=\"" ).append( attrib2Value ).append( "\">" )
	       .append( NEWLINE ).append( contents )
	       .append( endTagify( tag ) ).toString();
    }

    public static String genericThreeAttribTag( String tag, String attrib1,
						String attrib1Value,
						String attrib2,
						String attrib2Value,
						String attrib3,
						String attrib3Value,
						String contents ) {

	return new StringBuffer( NEWLINE ).append( "<" ).append( tag )
	       .append( " " ).append( attrib1 ).append( "=\"" )
	       .append( attrib1Value ).append( "\" " ).append( attrib2 )
	       .append( "=\"" ).append( attrib2Value ).append( " " )
	       .append( attrib3 ).append( "=\"" ).append( attrib3Value )
	       .append( "\">" ).append( NEWLINE ).append( contents )
	       .append( endTagify( tag ) ).toString();
    }


    //**************************
    // Static Generation Methods
    //**************************

    public static String spec( String name, String contents ) {

	return new StringBuffer( XML_INIT ).append( NEWLINE )
	    // .append( DOCTYPE ).append( NEWLINE )
	       .append( genericOneAttribTag( SpecParser.SPEC_TAG,
					     SpecParser.NAME_ATTRIBUTE,
					     name,
					     contents ) )
	       .toString();
    }

    public static String groupings( String contents ) {

	return genericNoAttribTag( SpecParser.GROUPINGS_TAG,
				   contents );
    }

    public static String group( String contents ) {
	
	return genericNoAttribTag( SpecParser.GROUP_TAG,
				   contents );
    }

    public static String group( String contents, int nPriority ) {
	
	return genericOneAttribTag( SpecParser.GROUP_TAG,
				    SpecParser.PRIORITY_ATTRIBUTE,
				    nPriority + "",
				    contents );
    }

    public static String objectref( String name ) {

	return terminalTagify( SpecParser.OBJECT_REF_TAG + " " +
			       SpecParser.NAME_ATTRIBUTE + "=\"" +
			       name + "\"" );
    }

    public static String state( String name, String contents ) {

	return genericOneAttribTag( SpecParser.STATE_TAG,
				    SpecParser.NAME_ATTRIBUTE,
				    name,
				    contents );
    }

    public static String state( String name, String access, String contents ) {

	return genericTwoAttribTag( SpecParser.STATE_TAG,
				    SpecParser.NAME_ATTRIBUTE,
				    name,
				    SpecParser.ACCESS_ATTRIBUTE,
				    access,
				    contents );
    }

    public static String state( String name, int nPriority, String contents ) {

	return genericTwoAttribTag( SpecParser.STATE_TAG,
				    SpecParser.NAME_ATTRIBUTE,
				    name,
				    SpecParser.PRIORITY_ATTRIBUTE,
				    nPriority + "",
				    contents );
    }

    public static String state( String name, int nPriority, String access, String contents ) {

	return genericThreeAttribTag( SpecParser.STATE_TAG,
				      SpecParser.NAME_ATTRIBUTE,
				      name,
				      SpecParser.PRIORITY_ATTRIBUTE,
				      nPriority + "",
				      SpecParser.ACCESS_ATTRIBUTE,
				      access,
				      contents );
    }

    public static String command( String name, String contents ) {

	return genericOneAttribTag( SpecParser.COMMAND_TAG,
				    SpecParser.NAME_ATTRIBUTE,
				    name,
				    contents );
    }

    public static String command( String name, int nPriority, String contents ) {

	return genericTwoAttribTag( SpecParser.COMMAND_TAG,
				    SpecParser.NAME_ATTRIBUTE,
				    name,
				    SpecParser.PRIORITY_ATTRIBUTE,
				    nPriority + "",
				    contents );
    }

    public static String explanation( String name, String contents ) {

	return genericOneAttribTag( SpecParser.EXPLANATION_TAG,
				    SpecParser.NAME_ATTRIBUTE,
				    name,
				    contents );
    }

    public static String explanation( String name, int nPriority, String contents ) {

	return genericTwoAttribTag( SpecParser.EXPLANATION_TAG,
				    SpecParser.NAME_ATTRIBUTE,
				    name,
				    SpecParser.PRIORITY_ATTRIBUTE,
				    nPriority + "",
				    contents );
    }

    public static String applytype( String name ) {

	return terminalTagify( SpecParser.APPLY_TYPE_TAG + " " +
			       SpecParser.NAME_ATTRIBUTE + "=\"" +
			       name + "\"" );
    }

    public static String type( String name, String contents ) {

	return genericOneAttribTag( SpecParser.TYPE_TAG,
				    SpecParser.NAME_ATTRIBUTE,
				    name,
				    contents );
    }

    public static String type( String contents ) {

	return genericNoAttribTag( SpecParser.TYPE_TAG,
				   contents );
    }
 
    public static String valueSpace( String contents ) {

	return genericNoAttribTag( SpecParser.VALUE_SPACE_TAG,
				   contents );
    }

    public static String expectedSpace( String contents ) {

	return genericNoAttribTag( SpecParser.EXPCT_SPACE_TAG,
				   contents );
    }

    public static String bool() {

	return terminalTagify( SpecParser.BOOLEAN_TAG );
    }

    public static String string() {

	return terminalTagify( SpecParser.STRING_TAG );
    }

    public static String integer() {

	return terminalTagify( SpecParser.INTEGER_TAG );
    }

    public static String integer( String min, String max ) {

	String contents = genericNoAttribTag( SpecParser.MINIMUM_TAG, min );
	contents += genericNoAttribTag( SpecParser.MAXIMUM_TAG, max );

	return genericNoAttribTag( SpecParser.INTEGER_TAG,
				   contents );
    }

    public static String integer( String min, String max, String incr ) {

	String contents = genericNoAttribTag( SpecParser.MINIMUM_TAG, min );
	contents += genericNoAttribTag( SpecParser.MAXIMUM_TAG, max );
	contents += genericNoAttribTag( SpecParser.INCREMENT_TAG, incr );

	return genericNoAttribTag( SpecParser.INTEGER_TAG,
				   contents );
    }

    public static String fixedpt( int nPointPos ) {

	String contents = genericNoAttribTag( SpecParser.POINTPOS_TAG, 
					      new Integer( nPointPos ).toString() );

	return genericNoAttribTag( SpecParser.FIXEDPT_TAG,
				   contents );
    }

    public static String fixedpt( int nPointPos, String min, String max ) {

	String contents = genericNoAttribTag( SpecParser.POINTPOS_TAG, 
					      new Integer( nPointPos ).toString() );
	contents += genericNoAttribTag( SpecParser.MINIMUM_TAG, min );
	contents += genericNoAttribTag( SpecParser.MAXIMUM_TAG, max );

	return genericNoAttribTag( SpecParser.FIXEDPT_TAG,
				   contents );
    }

    public static String fixedpt( int nPointPos, String min, String max, String incr ) {

	String contents = genericNoAttribTag( SpecParser.POINTPOS_TAG, 
					      new Integer( nPointPos ).toString() );
	contents += genericNoAttribTag( SpecParser.MINIMUM_TAG, min );
	contents += genericNoAttribTag( SpecParser.MAXIMUM_TAG, max );
	contents += genericNoAttribTag( SpecParser.INCREMENT_TAG, incr );

	return genericNoAttribTag( SpecParser.FIXEDPT_TAG,
				   contents );
    }

    public static String floatingpt() {

	return terminalTagify( SpecParser.FLOATINGPT_TAG );
    }

    public static String floatingpt( String min, String max ) {

	String contents = genericNoAttribTag( SpecParser.MINIMUM_TAG, min );
	contents += genericNoAttribTag( SpecParser.MAXIMUM_TAG, max );

	return genericNoAttribTag( SpecParser.FLOATINGPT_TAG,
				   contents );
    }

    public static String enumerated( int nItems ) {

	String contents = genericNoAttribTag( SpecParser.ITEMS_TAG,
					      new Integer( nItems ).toString() );

	return genericNoAttribTag( SpecParser.ENUMERATED_TAG,
				   contents );
    }

    public static String custom( String sCustomName ) {

	return genericNoAttribTag( SpecParser.CUSTOM_TAG,
				   sCustomName );
    }

    public static String number( int nPriority ) {

	return genericNoAttribTag( SpecParser.NUMBER_TAG,
				   nPriority + "" );
    }

    public static String refvalue( String name ) {

	return terminalTagify( SpecParser.REFVALUE_TAG + " " +
			       SpecParser.STATE_ATTRIBUTE + "=\"" +
			       name + "\"" );
    }

    public static String refstring( String name ) {

	return terminalTagify( SpecParser.REFSTRING_TAG + " " +
			       SpecParser.STATE_ATTRIBUTE + "=\"" +
			       name + "\"" );
    }

    public static String valueLabels( String contents ) {

	return genericNoAttribTag( SpecParser.VALUE_LABEL_TAG,
				   contents );
    }

    public static String map( String value, String[] labels ) {

	String contents = "";
	for( int i = 0; i < labels.length; i++ )
	    contents += label( labels[ i ] );

	return genericOneAttribTag( SpecParser.MAP_TAG,
				    SpecParser.INDEX_ATTRIBUTE,
				    value,
				    contents );
    }

    public static String map( String value, String enable, String[] labels ) {

	String contents = "";
	for( int i = 0; i < labels.length; i++ )
	    contents += label( labels[ i ] );


	return genericTwoAttribTag( SpecParser.MAP_TAG,
				    SpecParser.INDEX_ATTRIBUTE,
				    value,
				    SpecParser.ENABLE_ATTRIBUTE,
				    enable,
				    contents );
    }

    public static String map( int index, String[] labels ) {

	return map( index + "", labels );
    }

    public static String map( int index, String enable, String[] labels ) {

	return map( index + "", enable, labels );
    }

    public static String label( String label ) {

	return genericNoAttribTag( SpecParser.LABEL_TAG,
				   label );
    }

    public static String phonetic( String label ) {

	return genericNoAttribTag( SpecParser.PHONETIC_TAG,
				   label );
    }

    public static String texttospeech( String text, String recording ) {

	return new StringBuffer( NEWLINE ).append( "<" )
	    .append( SpecParser.TTS_TAG ).append( " " )
	    .append( SpecParser.TEXT_ATTRIBUTE ).append( "=\"" )
	    .append( text ).append( "\" " )
	    .append( SpecParser.RECORDING_ATTRIBUTE ).append( "=\"" )
	    .append( recording ).append( "\"/>" ).toString();
    }

    public static String labels( String[] labels ) {

	String contents = "";

	for( int i = 0; i < labels.length; i++ )
	    contents += label( labels[i] );
	
	return genericNoAttribTag( SpecParser.LABELS_TAG,
				   contents );
    }

    public static String activeif( String contents ) {

	return genericNoAttribTag( SpecParser.ACTIVEIF_TAG,
				   contents );
    }

    public static String activeif( String ignore, String contents ) {

	return genericOneAttribTag( SpecParser.ACTIVEIF_TAG,
				    SpecParser.IGNORE_ATTRIBUTE,
				    ignore,
				    contents );
    }

    public static String and( String contents ) {

	return genericNoAttribTag( SpecParser.AND_TAG,
				   contents );
    }

    public static String or( String contents ) {

	return genericNoAttribTag( SpecParser.OR_TAG,
				   contents );
    }

    public static String equals( String state, String contents ) {

	return genericOneAttribTag( SpecParser.EQUALS_TAG,
				    SpecParser.STATE_ATTRIBUTE,
				    state,
				    contents );
    }

    public static String greaterthan( String state, String contents ) {

	return genericOneAttribTag( SpecParser.GREATERTHAN_TAG,
				    SpecParser.STATE_ATTRIBUTE,
				    state,
				    contents );
    }

    public static String lessthan( String state, String contents ) {

	return genericOneAttribTag( SpecParser.LESSTHAN_TAG,
				    SpecParser.STATE_ATTRIBUTE,
				    state,
				    contents );
    }
}
