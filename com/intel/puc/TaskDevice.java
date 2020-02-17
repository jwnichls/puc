/*
 * TaskDevice.java
 * 
 * This device provides access to a task list (todo list) that is
 * stored on the Personal Server.  Simple priority and completion
 * information is included in the list, and a filter is available to
 * sort the tasks in the list.
 *
 * 12/9/2003 - JWN
 */

// Package Definition

package com.intel.puc;


// Import Declarations

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import com.maya.puc.common.*;

import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ListIterator;

import edu.cmu.hcii.puc.devices.*;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


// Class Definiton

public class TaskDevice extends AbstractDevice2 implements Runnable {

    //**************************
    // Constants
    //**************************

    protected final static String TASKDATA_FILE    = "taskdata.xml";

    protected final static String SPEC_NAME        = "todo-list.xml";

    protected final static String LIST_PREFIX      = "Tasks.List";
    protected final static String COMPLETED_SUFFIX = "Completed";
    protected final static String PRIORITY_SUFFIX  = "Priority";
    protected final static String DESCRIPT_SUFFIX  = "Description";
    protected final static String SELECTION_SUFFIX = "Selection";

    protected final static String FILTER_STATE     = "Tasks.Operations.CompletedFilter";
    protected final static String DUP_COMMAND      = "Tasks.Operations.Commands.Duplicate";
    protected final static String REMOVE_COMMAND   = "Tasks.Operations.Commands.Remove";

    protected static final String ROOT_TAG     = "tasks";
    protected static final String TASK_TAG     = "task";
    protected static final String COMPLETED_TAG = "completed";
    protected static final String PRIORITY_TAG = "priority";
    protected static final String DESCRIP_TAG  = "description";

    protected final static int ALL_TASK_FILTER   = 1;
    protected final static int INCOMPLETE_FILTER = 2;
    protected final static int COMPLETED_FILTER  = 3;


    //**************************
    // Static Inner Classes
    //**************************

    public static class TaskItem implements Cloneable {

	public boolean m_bCompleted;
	public int     m_nPriority;
	public String  m_sDescription;

	public Object clone() {

	    TaskItem dup = new TaskItem();
	    dup.m_bCompleted = m_bCompleted;
	    dup.m_nPriority = m_nPriority;
	    dup.m_sDescription = m_sDescription;

	    return dup;
	}
    }


    //**************************
    // Member Variables
    //**************************

    // variable data
    protected Vector    m_vItems;

    protected Thread    m_tWriteThread;
    protected File      m_pTaskFile;

    protected TaskItem  m_pAddNewItem;
    

    //**************************
    // Constructor
    //**************************
    
    public TaskDevice() {
	
	m_pTaskFile = new File( TASKDATA_FILE );

	m_vItems = TaskDevice.loadDatabase( m_pTaskFile );

	m_pAddNewItem = new TaskItem();
	m_pAddNewItem.m_bCompleted = false;
	m_pAddNewItem.m_nPriority = 1;
	m_pAddNewItem.m_sDescription = "Add New Item...";

	m_tWriteThread = new Thread( this );
	m_tWriteThread.start();
    }


    //**************************
    // Helper Methods
    //**************************

    protected static Vector loadDatabase( File f ) {

	Vector items = new Vector();

	try {
	    // open and parse the image database

	    FileReader pIn = new FileReader( f );
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
		if ( !elChild.getName().equals( TASK_TAG ) )
		    throw new NullPointerException();

		Element elCompleted = elChild.getChild( COMPLETED_TAG );
		Element elPriority = elChild.getChild( PRIORITY_TAG );
		Element elDescription = elChild.getChild( DESCRIP_TAG );

		TaskItem i = new TaskItem();

		i.m_bCompleted = new Boolean( elCompleted.getText() ).booleanValue();
		i.m_nPriority = new Integer( elPriority.getText() ).intValue();
		i.m_sDescription = elDescription.getText();

		items.add( i );
	    }
	}
	catch( Exception e ) { 
	    System.err.println( "Problem loading task data." );
	    e.printStackTrace();
	}

	return items;
    }

    protected static void writeDatabase( File f, Vector items ) {

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
	synchronized( items ) {
	    try {
		Element root = new Element( ROOT_TAG );

		Enumeration e = items.elements();
		while( e.hasMoreElements() ) {

		    Element item = new Element( TASK_TAG );
		    TaskItem r = (TaskItem)e.nextElement();

		    Element elCompleted = new Element( COMPLETED_TAG );
		    elCompleted.setText( r.m_bCompleted + "" );
		    item.addContent( elCompleted );

		    Element elPriority = new Element( PRIORITY_TAG );
		    elPriority.setText( r.m_nPriority + "" );
		    item.addContent( elPriority );

		    Element elDescription = new Element( DESCRIP_TAG );
		    elDescription.setText( r.m_sDescription );
		    item.addContent( elDescription );

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
		System.err.println( "Could not write task database." );
	    }
	}
    }


    //**************************
    // Runnable Methods
    //**************************

    public void run() {

	while( true ) {

	    try {
		// sleep for a minute
		Thread.sleep( 60000 ); 
	    }
	    catch( Exception e ) {
	    }

	    TaskDevice.writeDatabase( m_pTaskFile, m_vItems );
	}
    }


    //**************************
    // AbstractDevice2 Methods
    //**************************

    public void addConnection(PUCServer.Connection c) {
	
	super.addConnection( c );

	// add default property values

	c.setProperty( FILTER_STATE, new Integer( 1 ) );
	c.setProperty( LIST_PREFIX + "." + SELECTION_SUFFIX, new Integer( 1 ) );
    }

    public void start() {

	super.start( AbstractDevice2.STATUS_ACTIVE );
    }
    
    public void stop() {

	super.stop();
    }
    
    public String getName() {

	return "Task List";
    }
    
    public int getDefaultPort() {

	return 5185;
    }
    
    public String getSpecFileName() {

	return SPEC_NAME;
    }
    
    public void configure() {

    }
    
    public void handleMessage( PUCServer.Connection conn, Message msg ) {

	int filter = ((Integer)conn.getProperty( FILTER_STATE )).intValue();

	if ( msg instanceof Message.FullStateRequest ) {
	    
	    int sel = ((Integer)conn.getProperty( LIST_PREFIX + "." + SELECTION_SUFFIX )).intValue();

	    refreshTaskList( filter, conn );

	    Message.StateChangeNotification filtermsg = 
		new Message.StateChangeNotification( FILTER_STATE, filter + "" );

	    Message.StateChangeNotification selmsg = null;
	    if ( m_vItems.size() > 0 )
		selmsg = new Message.StateChangeNotification( LIST_PREFIX + "." + SELECTION_SUFFIX, sel + "" );

	    try {

		conn.send( filtermsg );

		if ( selmsg != null )
		    conn.send( selmsg );
	    }
	    catch( Exception e ) { }
	}
	else if ( msg instanceof Message.StateChangeRequest ) {
	    
	    synchronized( m_vItems ) {
		
		PUCData data = ((Message.StateChangeRequest)msg).getData();
		
		if ( data instanceof PUCData.ListData && 
		     ((PUCData.ListData)data).getSelectionType() == PUCData.ListData.SELTYPE_ONE ) {
		    
		    // don't currently support any other operations
		    
		    int idx = filteredToRealIndex( filter, ((PUCData.ListData)data).getIndex() );
		    System.out.println( "chgrqst f->r: " + ((PUCData.ListData)data).getIndex() + ", " + idx );

		    TaskItem r = null;
		    boolean insert = false;

		    // adding a new task
		    if ( idx > m_vItems.size() ) {

			r = new TaskItem();
			r.m_bCompleted = ( filter == COMPLETED_FILTER );
			r.m_nPriority = m_pAddNewItem.m_nPriority;
			r.m_sDescription = m_pAddNewItem.m_sDescription;

			m_vItems.add( r );

			insert = true;
		    }
		    else
			// all list indices are 1-indexed
			r = (TaskItem)m_vItems.get( idx-1 );
		    
		    // determine which field has been modified
		    if ( ((PUCData.Elements)data).get( 0 ) == null )
			System.out.println( "elements null" );
		    
		    PUCData.MultipleValues mv = (PUCData.MultipleValues)((PUCData.Elements)data).get( 0 );
		
		    String val;
		    boolean completeChanged = false;

		    if ( mv.get( COMPLETED_SUFFIX ) != null ) {

			val = mv.get( COMPLETED_SUFFIX ).getValue();
			completeChanged = true;

			r.m_bCompleted = new Boolean( val ).booleanValue();

			refreshAll();
		    }
		    else if ( mv.get( PRIORITY_SUFFIX ) != null ) {

			val = mv.get( PRIORITY_SUFFIX ).getValue();

			r.m_nPriority = new Integer( val ).intValue();
			
			if ( !insert )
			    refreshNonCompletionData( idx, (PUCData.ListData)data );
			else
			    refreshAll();
		    }
		    else if ( mv.get( DESCRIPT_SUFFIX ) != null ) {

			r.m_sDescription = mv.get( DESCRIPT_SUFFIX ).getValue();

			if ( !insert )
			    refreshNonCompletionData( idx, (PUCData.ListData)data );
			else
			    refreshAll();
		    }
		}
		else if ( data instanceof PUCData.Value ) {

		    PUCData.Value val = (PUCData.Value)data;

		    if ( val.getState().equals( LIST_PREFIX + "." + SELECTION_SUFFIX ) ) {

			try {
			    Integer sel = new Integer( val.getValue() ); 
			    
			    if ( sel.intValue() > 0 && sel.intValue() <= ( m_vItems.size() + 1 ) ) {

				System.out.println( "Selection changed to: " + sel.intValue() );
				conn.setProperty( val.getState(), sel );
				conn.send( new Message.StateChangeNotification( data ) );
			    }
			    else {

				int oldSel = ((Integer)conn.getProperty( val.getState() )).intValue();
				conn.send( new Message.StateChangeNotification( val.getState(), oldSel + "" ) );
			    }
			}
			catch( Exception e ) { 
			    e.printStackTrace();
			}
		    }
		    else if ( val.getState().equals( FILTER_STATE ) ) {

			try {
			    int oldSel = ((Integer)conn.getProperty( LIST_PREFIX + "." + SELECTION_SUFFIX )).intValue();
			    int realSel = filteredToRealIndex( filter, oldSel );
			    Integer newFilter = new Integer( val.getValue() );

			    if ( newFilter.intValue() > 0 && newFilter.intValue() <= COMPLETED_FILTER ) {

				refreshTaskList( newFilter.intValue(), conn );
				
				int newSel = constrainSelectionToList( newFilter.intValue(), realToFilteredIndex( newFilter.intValue(), realSel ) );
				conn.setProperty( LIST_PREFIX + "." + SELECTION_SUFFIX, new Integer( newSel ) );
				conn.send( new Message.StateChangeNotification( LIST_PREFIX + "." + SELECTION_SUFFIX, newSel + "" ) );

				conn.setProperty( val.getState(), newFilter );
				conn.send( new Message.StateChangeNotification( data ) );
			    }
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

	    if ( cimsg.getCommand().equals( DUP_COMMAND ) ) {

		// synchronize with the data
		synchronized( m_vItems ) {

		    int idx = filteredToRealIndex( filter, ((Integer)conn.getProperty( LIST_PREFIX + "." + SELECTION_SUFFIX )).intValue() );

		    if ( idx > m_vItems.size() ) {

			m_vItems.add( m_pAddNewItem.clone() );
		    }
		    else {
			TaskItem item = (TaskItem)m_vItems.get( idx-1 );

			if ( idx == m_vItems.size() )
			    m_vItems.add( item.clone() );
			else
			    m_vItems.insertElementAt( item.clone(), idx );
		    }

		    refreshAll();
		}
	    }
	    else if ( cimsg.getCommand().equals( REMOVE_COMMAND ) ) {

		// synchronize with the data
		synchronized( m_vItems ) {

		    int idx = filteredToRealIndex( filter, ((Integer)conn.getProperty( LIST_PREFIX + "." + SELECTION_SUFFIX )).intValue() );

		    m_vItems.remove( idx-1 );

		    refreshAll();		    
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

    public void refreshNonCompletionData( int realIndex, PUCData.ListData data ) {

	for (int i = 0; i < connections.size(); i++) {

	    try {
		PUCServer.Connection c = (PUCServer.Connection) connections.get(i);
		
		int filter = ((Integer)c.getProperty( FILTER_STATE )).intValue();
		
		int idx = realToFilteredIndex( filter, realIndex );
		
		data.setIndex( idx );

		c.send( new Message.StateChangeNotification( data ) );
	    }
	    catch( Exception e ) { }
	}	
    }

    public int constrainSelectionToList( int filter, int sel ) {

	if ( m_vItems.size() == 0 )
	    return 0;

	if ( sel <= 0 )
	    return 1;

	if ( filter == ALL_TASK_FILTER && sel > m_vItems.size() )
	    return m_vItems.size() + 1;

	int compCount = 0;
	int incompCount = 0;
	for( int i = 0; i < m_vItems.size(); i++ ) {
	    TaskItem item = (TaskItem)m_vItems.get( i );
	    if ( item.m_bCompleted )
		compCount++;
	    else
		incompCount++;
	}

	if ( filter == INCOMPLETE_FILTER && sel > incompCount )
	    return incompCount + 1;

	if ( filter == COMPLETED_FILTER && sel > compCount )
	    return compCount + 1;

	return sel;
    }

    public int filteredToRealIndex( int filter, int index ) {

	if ( filter == ALL_TASK_FILTER )
	    return index;

	int filterCount = 0;
	for( int i = 0; i < m_vItems.size(); i++ ) {
	    
	    TaskItem item = (TaskItem)m_vItems.get( i );
	    if ( ( item.m_bCompleted && filter == COMPLETED_FILTER ) ||
		 ( !item.m_bCompleted && filter == INCOMPLETE_FILTER ) )
		filterCount++;

	    if ( filterCount == index )
		return ( i + 1 );
	}

	return m_vItems.size() + 1;
    }

    public int realToFilteredIndex( int filter, int index ) {

	if ( filter == ALL_TASK_FILTER )
	    return index;

	int filterCount = 0;
	for( int i = 0; i < index && i < m_vItems.size(); i++ ) {

	    TaskItem item = (TaskItem)m_vItems.get( i );
	    if ( ( item.m_bCompleted && filter == COMPLETED_FILTER ) ||
		 ( !item.m_bCompleted && filter == INCOMPLETE_FILTER ) )
		filterCount++;
	}

	if ( index > m_vItems.size() )
	    return filterCount + 1;

	return filterCount;
    }

    public void refreshAll() {

	for (int i = 0; i < connections.size(); i++) {

	    PUCServer.Connection c = (PUCServer.Connection) connections.get(i);
	    int filter = ((Integer)c.getProperty( FILTER_STATE )).intValue();
		
	    refreshTaskList( filter, c );
	}	
    }

    public void refreshTaskList( int filterType, PUCServer.Connection conn ) {

	PUCData.ListData d = new PUCData.ListData( LIST_PREFIX );
	
	for( int i = 0; i < m_vItems.size(); i++ ) {

	    TaskItem record = (TaskItem)m_vItems.elementAt( i );

	    if ( ( record.m_bCompleted && filterType == INCOMPLETE_FILTER ) ||
		 ( !record.m_bCompleted && filterType == COMPLETED_FILTER ) )
		continue;

	    PUCData.MultipleValues mv = new PUCData.MultipleValues();
	    mv.put( new PUCData.Value( COMPLETED_SUFFIX, record.m_bCompleted + "" ) );
	    mv.put( new PUCData.Value( PRIORITY_SUFFIX, record.m_nPriority + "" ) );
	    mv.put( new PUCData.Value( DESCRIPT_SUFFIX, record.m_sDescription ) );

	    d.addElement( mv );
	}

	PUCData.MultipleValues mv = new PUCData.MultipleValues();
	mv.put( new PUCData.Value( COMPLETED_SUFFIX, ( filterType == COMPLETED_FILTER ) + "" ) );
	mv.put( new PUCData.Value( PRIORITY_SUFFIX, m_pAddNewItem.m_nPriority + "" ) );
	mv.put( new PUCData.Value( DESCRIPT_SUFFIX, m_pAddNewItem.m_sDescription ) );
	
	d.addElement( mv );

	Message.StateChangeNotification scn = new Message.StateChangeNotification( d );

	conn.send( scn );
    }
}
