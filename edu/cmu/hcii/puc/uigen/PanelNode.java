/**
 * PanelNode.java
 *
 * This node represents nodes that represent a single panel.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.uigen;


// Import Declarations

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Panel;

import java.util.Enumeration;
import java.util.Vector;

import edu.cmu.hcii.puc.cio.ContainerCIO;
import edu.cmu.hcii.puc.cio.PanelCIO;


// Class Definition

public class PanelNode extends InterfaceNode {

    //**************************
    // Member Variables
    //**************************

    protected Vector m_vRows;
    protected ContainerCIO m_pPanel;

    protected boolean m_bVertical;


    //**************************
    // Constructor
    //**************************

    public PanelNode() {
	m_vRows = new Vector();

	m_pPanel = new PanelCIO();

	m_bVertical = false;
    }

    //**************************
    // Member Methods
    //**************************

    public void addRow( Row pRow ) {

	m_vRows.addElement( pRow );
    }

    public ContainerCIO getContainerCIO() {

	return m_pPanel;
    }

    public boolean isVertical() {

	return m_bVertical;
    }

    public void setVertical( boolean bVertical ) {

	m_bVertical = bVertical;
    }

    //**************************
    // Interface Methods
    //**************************

    public void setLocation( int nX, int nY ) {

	super.setLocation( nX, nY );

	if ( m_pPanel != null )
	    m_pPanel.getWidget().setLocation( nX, nY );
    }

    public void setSize( int nWidth, int nHeight ) {

	super.setSize( nWidth, nHeight );

	if ( m_pPanel != null )
	    m_pPanel.getWidget().setSize( nWidth, nHeight );
    }

    public Dimension getPreferredSize( LayoutVariables pVars ) {

	Dimension ret = new Dimension();

	Enumeration e = m_vRows.elements();
	while( e.hasMoreElements() ) {
	    Dimension d = ((Row)e.nextElement()).getPreferredSize( pVars );
	    
	    ret.height += d.height;
	    
	    if ( d.width > ret.width ) {
		ret.width = d.width;
	    }
	}

	return ret;
    }

    public void addComponents( ContainerCIO pContainer ) {

	((Container)m_pPanel.getWidget()).setLayout( null );

	pContainer.addCIO( m_pPanel );

	Enumeration e = m_vRows.elements();
	while( e.hasMoreElements() )
	    ((Row)e.nextElement()).addComponents( m_pPanel );
    }

    public void doSizing( LayoutVariables pVars ) {

	return;
    }

    public void doLayout( ContainerCIO pContainer, LayoutVariables pVars ) {

	m_pPanel.getWidget().setSize( m_rBounds.width, m_rBounds.height );
	m_pPanel.getWidget().setLocation( m_rBounds.x, m_rBounds.y );

	Enumeration e = m_vRows.elements();
	int nTopY = 0;

	while( e.hasMoreElements() ) {
	    Row r = (Row)e.nextElement();

	    nTopY = r.doLayout( m_pPanel, nTopY, pVars );
	}
    }

    public String toString() {
	String ret = "PanelNode: - " + m_pPanel.getWidget().getBounds().toString() + "\n";

	Component c = m_pPanel.getWidget();
	while( c.getParent() != null ) {
	    c = c.getParent();
	}

	for( int i = 0; i < m_vRows.size(); i++ ) {
	    Row r = (Row)m_vRows.elementAt( i );

	    ret += r.toString();
	}

	return ret;
    }
}
