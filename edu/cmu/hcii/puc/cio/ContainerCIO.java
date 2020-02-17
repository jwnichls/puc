/**
 * ContainerCIO.java
 *
 * A sub-class of IndependentCIO.
 *
 * The super-class of all "container" CIOs.
 *
 * Revision History 
 * ---------------- 
 * 10/02/2001: (JWN) Created file. 
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.Component;
import java.awt.Container;

import java.lang.*;

import java.util.Enumeration;
import java.util.Vector;


// Class Definition

public class ContainerCIO extends IndependentCIO {

    //**************************
    // Member Variables
    //**************************

    protected Vector m_vCIOs;


    //**************************
    // Constructor
    //**************************

    protected ContainerCIO( Container pWidget ) {

	super( pWidget );

	m_vCIOs = new Vector();
    }


    //**************************
    // Member Methods
    //**************************

    public void addCIO( ConcreteInteractionObject cio ) {

	if ( cio == null ) return;

	Container c = (Container)m_Widget;

	c.add( cio.getWidget() );
	m_vCIOs.addElement( cio );

	cio.setVisible( true );
    }

    public void removeCIO( ConcreteInteractionObject cio ) {

	if ( cio == null ) return;

	cio.setVisible( false );

	Container c = (Container)m_Widget;

	c.remove( cio.getWidget() );
	m_vCIOs.removeElement( cio );
    }

    public void removeAllCIOs() {

	Container c = (Container)m_Widget;

	c.removeAll();
	m_vCIOs.removeAllElements();
    }

    public int getComponentCount() {

	return ((Container)m_Widget).getComponentCount();
    }
    
    public void addNotify() {

	Enumeration e = m_vCIOs.elements();

	while( e.hasMoreElements() ) {
	    ((ConcreteInteractionObject)e.nextElement()).addNotify();
	    //System.out.println( "notifying..." );
	}
    }    
}
