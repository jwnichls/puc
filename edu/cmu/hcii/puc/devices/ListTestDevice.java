/*
 * ListTestDevice.java
 * 
 * This device tests the list support of a PUC client by implementing
 * the simple_list.xml specification.  It will most likely be used for
 * testing the list communication protocol features.
 *
 * 10/3/2003 - JWN
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;

import com.maya.puc.common.*;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


// Class Definiton

public class ListTestDevice extends AbstractDevice2 {

    //**************************
    // Constants
    //**************************

    protected final static String LISTDATA_FILE   = "listdata.dat";

    protected final static String SPEC_NAME       = "simple_list.xml";

    protected final static String LIST_PREFIX     = "Test.List";
    protected final static String PLAYING_SUFFIX  = ".Playing";
    protected final static String TITLE_SUFFIX    = ".Title";
    protected final static String TIME_SUFFIX     = ".Time";
    protected final static String SELECTION_SUFFIX= ".Selection";

    protected final static int    LIST_MAX_LENGTH = 10;

    protected final static String HEADER          = "<?xml version=\"1.0\"?>\n<message>\n\t<state-change-notification>\n\t\t\n\t</state-change-notification>\n</message>\n";

    //**************************
    // Member Variables
    //**************************

    // variable data
    protected Boolean[] m_bPlaying;
    protected String[]  m_sTitle;
    protected String[]  m_sTime;

    protected int       m_nLength;

    protected boolean   m_bSelectionDefined;
    protected int       m_nSelectedIndex;

    protected Frame     m_pFrame;
    protected TextArea  m_pTextArea;
    

    //**************************
    // Constructor
    //**************************
    
    public ListTestDevice() {
	
	m_bPlaying = new Boolean[ LIST_MAX_LENGTH ];
	m_sTitle = new String[ LIST_MAX_LENGTH ];
	m_sTime = new String[ LIST_MAX_LENGTH ];

	m_nLength = -1;

	m_bSelectionDefined = false;
	m_nSelectedIndex = -1;

	// Create GUI

	m_pFrame = new Frame( "XML Message Testing App" );
	m_pFrame.setLayout( new BorderLayout() );
	m_pFrame.setSize( 500, 500 );
	m_pTextArea = new TextArea( HEADER, 30, 80 );
	m_pFrame.add( m_pTextArea, BorderLayout.CENTER );
	Button b = new Button( "Send" );
	m_pFrame.add( b, BorderLayout.SOUTH );
	
	b.addActionListener( new ActionListener() {

		public void actionPerformed( ActionEvent e ) {

		    try {
			SAXBuilder saxbuild = new SAXBuilder();
			Document doc = saxbuild.build(new StringReader(m_pTextArea.getText()));
			Message msg = Message.decode(doc,null,0);
			
			sendAll( msg );
		    }
		    catch( Exception ex ) {
			ex.printStackTrace();
		    }

		    m_pTextArea.setText( HEADER );
		}
	    });

	// loadListDataFromFile();
    }


    //**************************
    // Helper Methods
    //**************************

    protected void loadListDataFromFile() {

	try {
	    BufferedReader in = new BufferedReader( new FileReader( LISTDATA_FILE ) );

	    String line = in.readLine();
	    for( m_nLength = 0; line != null; m_nLength++ ) {
		
		StringTokenizer st = new StringTokenizer( line, "," );
		
		m_bPlaying[ m_nLength ] = new Boolean( st.nextToken() );
		m_sTitle[ m_nLength ] = st.nextToken();
		m_sTime[ m_nLength ] = st.nextToken();
		
		line = in.readLine();
	    }
	}
	catch( Exception e ) {
	    
	    System.out.println( "Couldn't read from list data file." );
	    e.printStackTrace();
	}
    }


    //**************************
    // AbstractDevice2 Methods
    //**************************

    public void start() {

	super.start( AbstractDevice2.STATUS_ACTIVE );
    }

    public void stop() {

	super.stop();
    }

    public String getName() {

	return "List Test Device";
    }

    public int getDefaultPort() {

	return 5180;
    }

    public String getSpecFileName() {

	return SPEC_NAME;
    }

    public void configure() {

    }

    public boolean hasGUI() { 

	return true; 
    }

    public void setGUIVisibility(boolean isVisible) {

	m_pFrame.setVisible( isVisible );
    }

    public boolean isGUIVisible() { 

	return m_pFrame.isVisible(); 
    }

    public void handleMessage( PUCServer.Connection conn, Message msg ) {

	if ( msg instanceof Message.FullStateRequest ) {

	    PUCData.ListData d = new PUCData.ListData( "Test.List" );
	    
	    //element one
	    PUCData.MultipleValues mv = new PUCData.MultipleValues();
	    mv.put( new PUCData.Value( "Playing", "true" ) );
	    mv.put( new PUCData.Value( "Title", "Sweet Home Alabama" ) );
	    mv.put( new PUCData.Value( "Time", "341" ) );

	    d.addElement( mv );

	    // element two
	    mv = new PUCData.MultipleValues();
	    mv.put( new PUCData.Value( "Playing", "false" ) );
	    mv.put( new PUCData.Value( "Title", "A Little Less Conversation" ) );
	    mv.put( new PUCData.Value( "Time", "211" ) );

	    d.addElement( mv );

	    // element three
	    mv = new PUCData.MultipleValues();
	    mv.put( new PUCData.Value( "Playing", "false" ) );
	    mv.put( new PUCData.Value( "Title", "One Sweet World" ) );
	    mv.put( new PUCData.Value( "Time", "343" ) );

	    d.addElement( mv );

	    // element four
	    mv = new PUCData.MultipleValues();
	    mv.put( new PUCData.Value( "Playing", "false" ) );
	    mv.put( new PUCData.Value( "Title", "Why Georgia" ) );
	    mv.put( new PUCData.Value( "Time", "444" ) );

	    d.addElement( mv );

	    // element five
	    mv = new PUCData.MultipleValues();
	    mv.put( new PUCData.Value( "Playing", "false" ) );
	    mv.put( new PUCData.Value( "Title", "My Stupid Mouth" ) );
	    mv.put( new PUCData.Value( "Time", "302" ) );

	    d.addElement( mv );

	    Message.StateChangeNotification scn = new Message.StateChangeNotification( d );

	    conn.send( scn );
	}
	else if ( msg instanceof Message.StateChangeRequest ) {

	    Message.StateChangeRequest scrmsg = (Message.StateChangeRequest)msg;
	    if ( scrmsg.getState().equals( LIST_PREFIX + SELECTION_SUFFIX ) ) {
		
		Message scn = new Message.StateChangeNotification( LIST_PREFIX + SELECTION_SUFFIX, scrmsg.getValue() );

		conn.send( scn );
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
}
