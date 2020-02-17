/*
 * PhotoBrowserDevice.java
 * 
 * An implementation of the photo browser service.  This service will
 * eventually run on a Personal Server device, and will probably not
 * be implemented in Java.
 *
 * This file contains markers around code that uses Java's AWT
 * library, so that those code pieces can stripped out by a
 * script. This is necessary for the Personal Server to be able to run
 * this device.
 *
 * Jeffrey Nichols
 * 10/27/2003
 */

// Package Definition

package com.intel.puc;


// Import Declarations

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import java.text.*;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.filechooser.*;

import com.maya.puc.common.*;

import com.sun.image.codec.jpeg.*;

import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ListIterator;

import edu.cmu.hcii.puc.devices.*;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


// Class Definition

public class PhotoBrowserDevice 
    extends AbstractDevice2 
    implements Runnable {

    //**************************
    // Constants
    //**************************

    protected final static String SPEC_NAME       = "photo-browser.xml";

    protected static final String ROOT_TAG     = "photos";
    protected static final String PHOTO_TAG    = "photo";
    protected static final String FILENAME_TAG = "filename";
    protected static final String CONTENT_TYPE_TAG = "content-type";
    protected static final String TITLE_TAG    = "title";
    protected static final String DATE_TAG     = "date";

    protected static final String CONFIG_FILE      = "photobrowser.cfg";
    protected static final String CONFIG_FILE_ATTR = "PhotoDB";

    protected final static String IMAGE_LIST = "Photos.Browser.List";
    protected final static String IMAGE_SUFFIX = "Image";
    protected final static String TITLE_SUFFIX = "Title";
    protected final static String DATE_SUFFIX = "Date";
    protected final static String SELECTION_SUFFIX = "Selection";
    
    protected final static String MOVE_UP_CMD = "Photos.Browser.List.Move.MoveUp";
    protected final static String MOVE_DOWN_CMD = "Photos.Browser.List.Move.MoveDown";
    protected final static String SHARE_CMD = "Photos.Browser.Share";


    //**************************
    // Inner Classes
    //**************************

    public static class ImageRecord {

	public String m_sFilename;
	public String m_sContentType;
	public String m_sTitle;
	public String m_sDate;
    }


    //**************************
    // Member Variables
    //**************************
    
    protected Vector m_vImages;
    protected int    m_nSelection;

    protected String m_sImageLocation;

    protected Thread m_tWriteThread;


    //**************************
    // Constructors
    //**************************
    
    public PhotoBrowserDevice() {

	m_sImageLocation = "c:\\Documents and Settings\\jnichols\\My Documents\\My Pictures\\photodb.xml";

	FauxEform pSettings = new FauxEform();
	pSettings.load( CONFIG_FILE );

	String sFile = pSettings.getStringAttr( CONFIG_FILE_ATTR, "" );

	if ( sFile.equals( "" ) ) {
	    // present a dialog box to get the name of the x10 spec file
	    configure();
	}
	else {
	    // parse the chosen file
	    fileChosen( new File( sFile ) );
	}

	m_tWriteThread = new Thread( this );
	m_tWriteThread.start();

	m_nSelection = 1;
    }


    //**************************
    // Runnable Methods
    //**************************

    public void run() {

	FauxEform pSettings = new FauxEform();
	pSettings.load( CONFIG_FILE );
	String sFile = pSettings.getStringAttr( CONFIG_FILE_ATTR, "" );
	File pFile = new File( sFile );

	while( true ) {

	    try {
		// sleep for a minute
		Thread.sleep( 60000 ); 
	    }
	    catch( Exception e ) {
	    }

	    PhotoBrowserDevice.writeDatabase( pFile, m_vImages );
	}
    }


    //**************************
    // AbstractDevice2 Methods
    //**************************

    public void configure() {

	if ( PSDetect.isPersonalServer() ) {

	    System.out.println( "Devices on the personal server must be pre-configured." );
	    return;
	}

	JFileChooser chooser = new JFileChooser();

	chooser.setSelectedFile( new File( m_sImageLocation ) );

	javax.swing.filechooser.FileFilter pFilter = new javax.swing.filechooser.FileFilter() {

		public boolean accept( File f ) {

		    return f.isDirectory() ||
			   ( f.isFile() && f.canRead() && f.getName().endsWith( ".xml" ) );
		}

		public String getDescription() {

		    return "Photo Databases (*.xml)";
		}
	    };

	chooser.setFileFilter(pFilter);
	int returnVal = chooser.showOpenDialog( null );
	if( returnVal == JFileChooser.APPROVE_OPTION ) {
	    fileChosen( chooser.getSelectedFile() );
	}
    }

    public void fileChosen( File pFile ) {

	FileReader pIn = null;

	try {
	    m_sImageLocation = pFile.getAbsolutePath();
	    m_vImages = new Vector();

	    // open and parse the image database

	    pIn = new FileReader( pFile );
	    SAXBuilder saxbuild = new SAXBuilder();
	    Document pDoc = saxbuild.build(pIn);

	    // read image database

	    Element root = pDoc.getRootElement();
	    if ( !root.getName().equals( ROOT_TAG ) )
		throw new NullPointerException();

	    List l = root.getChildren();
	    ListIterator pI = l.listIterator();

	    while( pI.hasNext() ) {

		Element elChild = (Element)pI.next();
		if ( !elChild.getName().equals( PHOTO_TAG ) )
		    throw new NullPointerException();

		Element elFilename = elChild.getChild( FILENAME_TAG );
		Element elType = elChild.getChild( CONTENT_TYPE_TAG );
		Element elTitle = elChild.getChild( TITLE_TAG );
		Element elDate = elChild.getChild( DATE_TAG );

		ImageRecord i = new ImageRecord();

		File test = new File( elFilename.getText() );
		if ( test.isAbsolute() )
		    i.m_sFilename = elFilename.getText();
		else
		    i.m_sFilename =  pFile.getParent() + File.separatorChar + elFilename.getText();

		i.m_sContentType = elType.getText();
		i.m_sTitle = elTitle.getText();
		i.m_sDate = elDate.getText();

		m_vImages.add( i );
	    }

	    // if everything went okay, save the file name for the
	    // future 

	    FauxEform pSettings = new FauxEform();
	    pSettings.setAttr( CONFIG_FILE_ATTR, m_sImageLocation );
	    pSettings.save( CONFIG_FILE );

	    if ( isRunning() )
		refreshImageList();
	    else
		stop(AbstractDevice2.STATUS_INACTIVE);
	}
	catch( Exception e ) {

	    e.printStackTrace();
	    System.err.println( "Could not parse photo database." );
	    stop(AbstractDevice2.STATUS_ERROR);
	}
	finally {

	    try {
		if ( pIn != null )
		    pIn.close();
	    }
	    catch( Exception e2 ) { }
	}
    }

    public void start() {

	if ( m_vImages != null )
	    super.start( AbstractDevice2.STATUS_ACTIVE );
	else
	    stop( AbstractDevice2.STATUS_ERROR );
    }

    public void stop() {

	super.stop();
    }

    public String getName() {

	return "Photo Browser";
    }

    public int getDefaultPort() {

	return 5183;
    }

    public String getSpecFileName() {

	return SPEC_NAME;
    }

    public boolean hasGUI() { 

	return false; 
    }

    public void setGUIVisibility(boolean isVisible) {
    }

    public boolean isGUIVisible() { 

	return false; 
    }

    public void handleMessage( PUCServer.Connection conn, Message msg ) {

	if ( msg instanceof Message.FullStateRequest ) {

	    refreshImageList( conn );

	    Message.StateChangeNotification scnmsg = 
		new Message.StateChangeNotification( IMAGE_LIST + "." + SELECTION_SUFFIX, m_nSelection + "" );

	    try {

		conn.send( scnmsg );
	    }
	    catch( Exception e ) { }
	}
	else if ( msg instanceof Message.StateValueRequest ) {

	    try {
		String state = ((Message.StateValueRequest)msg).getState();
		
		// determine if/which this state is in the image list
		if ( state.startsWith( IMAGE_LIST + "[" ) ) {
		    
		    int prefixLen = (IMAGE_LIST + "[").length();
		    int endIdx = state.indexOf( "]." + IMAGE_SUFFIX,
						prefixLen );
		    
		    // extract index
		    int listIdx = Integer.parseInt( state.substring( prefixLen,
								     endIdx ) );

		    // -1 because list indices are 1-indexed
		    ImageRecord record = (ImageRecord)m_vImages.get( listIdx-1 );
		    
		    byte[] imageData = getImage( record, (Message.StateValueRequest)msg );

		    conn.send( new Message.BinaryStateChangeNotification( state, new ByteArrayInputStream( imageData ), record.m_sContentType, imageData.length ) );
		}
	    }
	    catch( Exception e ) {
		
		e.printStackTrace();
	    }
	}
	else if ( msg instanceof Message.StateChangeRequest ) {

	    synchronized( m_vImages ) {

		PUCData data = ((Message.StateChangeRequest)msg).getData();

		if ( data instanceof PUCData.ListData && 
		     ((PUCData.ListData)data).getSelectionType() == PUCData.ListData.SELTYPE_ONE ) {

		    // don't currently support any other operations

		    int idx = ((PUCData.ListData)data).getIndex();
		    // all list indices are 1-indexed
		    ImageRecord r = (ImageRecord)m_vImages.get( idx-1 );

		    // only the Title state may be modified, so this
		    // must be a Title value.
		    if ( ((PUCData.Elements)data).get( 0 ) == null )
			System.out.println( "elements null" );

		    if ( ((PUCData.MultipleValues)((PUCData.Elements)data).get( 0 )).get( TITLE_SUFFIX ) == null )
			System.out.println(  "value null" );

		    String val = ((PUCData.MultipleValues)((PUCData.Elements)data).get( 0 )).get( TITLE_SUFFIX ).getValue();

		    r.m_sTitle = val;

		    Message rmsg = new Message.StateChangeNotification(data);
		    
		    try {
			// send StateChangeNotification to all clients
			sendAll( rmsg );
		    }
		    catch( Exception e ) {
			
			// shouldn't ever happen
		    }
		}
		else if ( data instanceof PUCData.Value ) {

		    PUCData.Value val = (PUCData.Value)data;

		    if ( val.getState().equals( IMAGE_LIST + "." + SELECTION_SUFFIX ) ) {

			try {
			    m_nSelection = Integer.parseInt( val.getValue() );

			    if ( m_nSelection > 0 && m_nSelection <= m_vImages.size() )
				sendAll( new Message.StateChangeNotification( data ) );
			}
			catch( Exception e ) { 
			    e.printStackTrace();
			}
		    }
		}
	    }
	}
	else if ( msg instanceof Message.CommandInvokeRequest ) {

	    Message.CommandInvokeRequest cimsg = 
		(Message.CommandInvokeRequest)msg;

	    if ( cimsg.getCommand().equals( MOVE_UP_CMD ) ) {

		// synchronize with the data
		synchronized( m_vImages ) {

		    int idx = m_nSelection - 1;
		    
		    // make sure we're within the correct bounds
		    if ( m_nSelection >= m_vImages.size() )
			return;

		    // make the appropriate changes to the database
		    Object o = m_vImages.get( idx );
		    m_vImages.set( idx, m_vImages.get( idx+1 ) );
		    m_vImages.set( idx+1, o );

		    // send the changes to all browsers
		    sendReplaceMessage( idx, 2 );

		    // ensure the database is updated
		    m_tWriteThread.interrupt();
		}
	    }
	    else if ( cimsg.getCommand().equals( MOVE_DOWN_CMD ) ) {

		// synchronize with the data
		synchronized( m_vImages ) {

		    int idx = m_nSelection - 1;

		    // make sure we're within the correct bounds
		    if ( idx <= 0 )
			return;

		    // make the appropriate changes to the database
		    Object o = m_vImages.get( idx );
		    m_vImages.set( idx, m_vImages.get( idx-1 ) );
		    m_vImages.set( idx-1, o );

		    // send the changes to all browsers
		    sendReplaceMessage( idx-1, 2 );

		    // ensure the database is updated
		    m_tWriteThread.interrupt();
		}
	    }
	    else {
		System.out.println( "command called: " + cimsg.getCommand() );
	    }
	}
	else if ( msg instanceof Message.SpecRequest ) {
	    
	    try {
		conn.send( new Message.DeviceSpec( getSpec() ) );
	    }
	    catch( Exception e ) {
		e.printStackTrace();
	    }
	}	
    }
    
    protected byte[] getImage( ImageRecord r,
			       Message.StateValueRequest msg ) 
			       throws IOException {

	byte[] ret;

	File f = new File( r.m_sFilename );
	FileInputStream fin = new FileInputStream( f );

	if ( msg.get( Message.StateValueRequest.DESIRED_HEIGHT_OPT ) == null &&
	     msg.get( Message.StateValueRequest.DESIRED_WIDTH_OPT ) == null ||
	     PSDetect.isPersonalServer() ) {

	    ret = new byte[ (int)f.length() ];

	    int cursor = 0;
	    int readlen = 0;

	    while( readlen >= 0 ) {
		cursor += readlen = fin.read( ret, cursor, ret.length - cursor );
		
		if ( cursor == ret.length )
		    break;
	    }

	    fin.close();

	    return ret;
	}
	else {
	    
	    // determine if image is to be scaled
	    if ( !PSDetect.isPersonalServer() ) {
		
		// load in the unscaled image
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder( fin );
		BufferedImage oldImage = decoder.decodeAsBufferedImage();
		
		fin.close();

		double ratio = ((double)oldImage.getWidth()) / 
		               ((double)oldImage.getHeight());
		int h, w;
		
		if ( msg.get( Message.StateValueRequest.DESIRED_HEIGHT_OPT ) == null ) {
		    
		    w = Integer.parseInt( msg.get( Message.StateValueRequest.DESIRED_WIDTH_OPT ) );
		    h = (int)(((double)w)*1/ratio);
		}
		else if ( msg.get( Message.StateValueRequest.DESIRED_WIDTH_OPT ) == null ) {
		    
		    h = Integer.parseInt( msg.get( Message.StateValueRequest.DESIRED_HEIGHT_OPT ) );
		    w = (int)(((double)h)*ratio);
		}
		else {
		    w = Integer.parseInt( msg.get( Message.StateValueRequest.DESIRED_WIDTH_OPT ) );
		    h = Integer.parseInt( msg.get( Message.StateValueRequest.DESIRED_HEIGHT_OPT ) );
		    
		    if ( w >= (int)( ((double)h) * ratio ) )
			w = (int)( ((double)h) * ratio );
		    else
			h = (int)( ((double)w) * 1/ratio );
		}
		
		// scale the image
		BufferedImage newImage = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );
		Graphics2D g2 = newImage.createGraphics();
		g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, 
				     RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g2.drawImage( oldImage, 0, 0, w, h, null );

		// get image into a InputStream
		ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder( tempOut );
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(newImage);
		param.setQuality( 50f, false );
		encoder.setJPEGEncodeParam( param );
		encoder.encode( newImage );
		
		oldImage.flush();
		newImage.flush();

		return tempOut.toByteArray();
	    }
	    else {

		// TODO: Find some way to scale image on personal
		// server

		return null; // shouldn't get here
	    }
	}
    }

    protected void refreshImageList() {

	refreshImageList( null );
    }

    protected void refreshImageList( PUCServer.Connection conn ) {

	if ( m_vImages == null )
	    return;

	PUCData.ListData d = new PUCData.ListData( IMAGE_LIST );
	
	for( int i = 0; i < m_vImages.size(); i++ ) {

	    ImageRecord record = (ImageRecord)m_vImages.elementAt( i );

	    PUCData.MultipleValues mv = new PUCData.MultipleValues();
	    mv.put( new PUCData.Value( TITLE_SUFFIX, record.m_sTitle ) );
	    mv.put( new PUCData.Value( DATE_SUFFIX, record.m_sDate ) );

	    d.addElement( mv );

	    // +1 because list indicies are 1-indexed
	    String elemName = IMAGE_LIST + "[" + (i+1) + "]." +
		IMAGE_SUFFIX;
	    Message msg = new Message.BinaryStateChangeNotification( elemName, record.m_sContentType );

	    if ( conn != null )
		conn.send( msg );
	    else
		sendAll( msg );
	}

	Message.StateChangeNotification scn = new Message.StateChangeNotification( d );

	if ( conn != null )
	    conn.send( scn );
	else
	    sendAll( scn );
    }

    protected void sendReplaceMessage( int index, int len ) {

	PUCData.ListReplace data = new PUCData.ListReplace( IMAGE_LIST, index+1, len );

	for( int i = index; i < (index+len); i++ ) {

	    ImageRecord record = (ImageRecord)m_vImages.elementAt( i );

	    PUCData.MultipleValues mv = new PUCData.MultipleValues();
	    mv.put( new PUCData.Value( TITLE_SUFFIX, record.m_sTitle ) );
	    mv.put( new PUCData.Value( DATE_SUFFIX, record.m_sDate ) );

	    data.addElement( mv );

	    // +1 because list indicies are 1-indexed
	    String elemName = IMAGE_LIST + "[" + (i+1) + "]." +
		IMAGE_SUFFIX;
	    Message msg = new Message.BinaryStateChangeNotification( elemName, record.m_sContentType );

	    sendAll( msg );
	}

	Message.StateChangeNotification scn = new Message.StateChangeNotification( data );

	sendAll( scn );	
    }

    // static main method
    // ------------------
    // This method is a utility function that can be used to create
    // a new database from a directory of files.  As this device can
    // only currently scale jpeg files, this method will include 
    // only the files ending in .jpg in the database.  The dates will
    // the modification date and the title will be the name of the 
    // file.
    //
    // The first argument is expected to be the name of the directory 
    // to examine.

    public static void main( String[] args ) {

	if ( args.length != 1 ) {

	    System.err.println( "Usage: java -jar photobrowser.jar <directory>" );
	    System.exit( -1 );
	}

	File dir = new File( args[ 0 ] );

	if ( !dir.isDirectory() ) {

	    System.err.println( "Usage: java -jar photobrowser.jar <directory>" );
	    System.exit( -1 );
	}

	// get the listing of files in the directory ending
	// with the .jpg extension
	
	File[] imageFiles = dir.listFiles( new FilenameFilter() {

		public boolean accept( File dir, String name ) {

		    return name.endsWith( ".jpg" );
		}
	    });


	// create a vector of database information
	Vector images = new Vector();
	SimpleDateFormat dt = new SimpleDateFormat( "MMMMMMMMM dd, yyyy" );

	for( int i = 0; i < imageFiles.length; i++ ) {

	// 
	    ImageRecord item = new ImageRecord();

	    item.m_sFilename = imageFiles[ i ].getAbsolutePath();
	    item.m_sContentType = "image/jpeg";
	    item.m_sTitle = imageFiles[ i ].getName();
	    Date d = new Date( imageFiles[ i ].lastModified() );
	    item.m_sDate = dt.format( d );

	    images.add( item );
	}

	// write database to file
	File db = new File( dir, "photodb.xml" );
	writeDatabase( db, images );
    }


    protected static void writeDatabase( File f, Vector images ) {

	System.out.println( "Writing database to file." );

	try {
	    // move the existing database to a backup file
	    // we would have overwritten the file in the next step
	    File backup = new File( f.getParent(), "dbbackup.xml" );
	    f.renameTo( backup );
	}
	catch( Exception fileMove ) { }

	// make sure other database changes cannot take effect until
	// the database write is finished
	synchronized( images ) {
	    try {
		Element root = new Element( ROOT_TAG );

		Enumeration e = images.elements();
		while( e.hasMoreElements() ) {

		    Element item = new Element( PHOTO_TAG );
		    ImageRecord r = (ImageRecord)e.nextElement();

		    Element elFilename = new Element( FILENAME_TAG );
		    elFilename.setText( r.m_sFilename );
		    item.addContent( elFilename );

		    Element elType = new Element( CONTENT_TYPE_TAG );
		    elType.setText( r.m_sContentType );
		    item.addContent( elType );

		    Element elTitle = new Element( TITLE_TAG );
		    elTitle.setText( r.m_sTitle );
		    item.addContent( elTitle );

		    Element elDate = new Element( DATE_TAG );
		    elDate.setText( r.m_sDate );
		    item.addContent( elDate );

		    root.addContent( item );
		}

		Document d = new Document( root );
		XMLOutputter xmlgen = new XMLOutputter();
		
		BufferedWriter out = new BufferedWriter( new
		    FileWriter( f ) );
		xmlgen.output( d, out );
		out.close();
	    }
	    catch( Exception e ) {
		
		e.printStackTrace();
		System.err.println( "Could not write photo database." );
	    }
	}
    }
}
