/**
 * GroupNode.java
 * 
 * Represents a node within the grouping tree.  Usually associated
 * with a state variable and a priority.
 *
 * Revision History:
 * -----------------
 * 07/07/2001: (JWN) Created file.
 *
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import edu.cmu.hcii.puc.uigen.InterfaceNode;
import edu.cmu.hcii.puc.uigen.NullOrganizer;
import edu.cmu.hcii.puc.uigen.PanelNode;


// Class Definition

public class GroupNode {

    //**************************
    // Constants
    //**************************

    public final static int NO_IGNORE = 0;
    public final static int IGNORE_PARENT = 1;
    public final static int IGNORE_ALL = 2;


    //**************************
    // Constructor
    //**************************

    public GroupNode() {
	m_Organizer = NullOrganizer.getTheNullOrganizer();
    }


    //**************************
    // Member Variables
    //**************************

    public ApplianceObject m_Object;

    protected Organizer m_Organizer;

    public Vector m_Children;
    public GroupNode m_Parent;
    public LabelLibrary m_Labels;

    // used by the UI generation
    protected Hashtable m_hChildContainers;

    // following used only during parsing 
    public int    m_nDepIgnore;       
    public DependencyFormula m_pDependencies;

    protected int m_nPriority;


    //**************************
    // Member Methods
    //**************************

    public boolean containsGroups() {

	if ( m_Children == null ) return false;

	Enumeration e = m_Children.elements();
	while( e.hasMoreElements() )
	    if ( ((GroupNode)e.nextElement()).m_Object == null )
		return true;

	return false;
    }

    public boolean hasNullOrganizer() {
	return m_Organizer instanceof NullOrganizer;
    }

    public Organizer getOrganizer() {

	return m_Organizer;
    }

    public void setOrganizer( Organizer o ) {

	m_Organizer = o;
    }
    
    public void callOrganizer( InterfaceNode pCurrentNode ) {

	m_hChildContainers = m_Organizer.addOrganization( this, pCurrentNode );
    }

    public PanelNode getContainerForChild( GroupNode pChild ) {

	if ( m_hChildContainers == null ) return null;

	return (PanelNode)m_hChildContainers.get( pChild );
    }

    public int getPriority() {

	if ( m_Object != null ) 
	    return m_Object.m_nPriority;

	return m_nPriority;
    }

    public void setPriority( int nPriority ) {

	m_nPriority = nPriority;
    }

    public int size() {
	
	return m_Children == null ? 0 : m_Children.size();
    }
}

