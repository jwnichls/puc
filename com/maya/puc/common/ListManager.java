/*
 * ListManager.java
 *
 * Manages the versioning of lists so that appliances don't have to.
 * Appliance don't need to register list variables, as they are
 * inferred from the messages sent between the server and the
 * client. Version numbers are automatically incremented up each time
 * a new message is sent with that list variable.  The current version
 * associated with a list can also be queried from the ListManager,
 * which can be used for validating the versions of list selections.
 *
 * All list manager methods take a connection as a parameter, so that 
 * there is no need to instantiate a list manager object.
 */

// Package Definition

package com.maya.puc.common;


// Import Declarations

import java.util.Enumeration;
import java.util.Hashtable;


// Class Definition

public abstract class ListManager extends Object {

    //**************************
    // Static Inner Classes
    //**************************

    public static class ListData implements Cloneable {

	public String m_sListName;
	public int    m_nVersion;

	public Object clone() {
	    
	    ListData clone = new ListData();
	    clone.m_sListName = m_sListName;
	    clone.m_nVersion  = m_nVersion;

	    return clone;
	}
    }


    //**************************
    // Static Member Variables
    //**************************

    /**
     * This hashtable stores all of the list data.  It is a
     * hierarchical two-tiered table, with the first tier hashed
     * on Connections and the second tier hashed on the name of the
     * list variable.  The objects stored at the leaves of the table
     * are ListData objects (see above).
     */
    protected static Hashtable m_hData;


    //**************************
    // Static Constructor
    //**************************

    static {

	m_hData = new Hashtable();
    }

    //**************************
    // Static Methods
    //**************************

    public static int incrementListVersion( PUCServer.Connection c, String sListName ) {
	
	Hashtable connTable = (Hashtable)m_hData.get( c );
	if ( connTable == null ) {
	    connTable = new Hashtable();
	    m_hData.put( c, connTable );
	}

	ListData data = (ListData)connTable.get( sListName );
	if ( data == null ) {
	    data = new ListData();
	    data.m_sListName = sListName;
	    data.m_nVersion = -1;
	    connTable.put( sListName, data );
	}

	return ++data.m_nVersion;
    }

    protected static ListData getListVersion( PUCServer.Connection c, String sListName ) {

	Hashtable connTable = (Hashtable)m_hData.get( c );
	if ( connTable == null )
	    return null;

	ListData data = (ListData)connTable.get( sListName );
	if ( data == null )
	    return null;

	return (ListData)data.clone();
    }

    public static boolean compareListVersions( PUCServer.Connection c, String sListName, int nVersion ) {

	ListData data = getListVersion( c, sListName );

	if ( data == null ) return false;

	return data.m_nVersion == nVersion;
    }
}
