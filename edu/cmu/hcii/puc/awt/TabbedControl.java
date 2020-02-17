/**
 * TabbedControl.java
 *
 * This is a simple TabbedControl that does its own painting. The
 * style is intentionally drawn to match the PocketPC tabbed
 * component. This is *not* a good long-term solution but it may work
 * nicely for now.
 *
 * @author Jeffrey Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.awt;


// Import Declarations

import java.awt.*;
import java.awt.Label;
import java.awt.event.*;

import java.util.*;

import edu.cmu.hcii.puc.cio.ConcreteInteractionObject;


// Class Definition

public class TabbedControl extends java.awt.Panel
                           implements ItemSelectable  {

    //**************************
    // Helper Class
    //**************************

    public static class TabbedPane extends Panel {
	protected String  m_sLabel;

	public TabbedPane( ConcreteInteractionObject pCIO, String sLabel ) {
	    super( pCIO );

	    m_sLabel = sLabel;

	    setBackground( Color.white );
	    setLayout( new FlowLayout() );
	}

	public String getLabel() { return m_sLabel; }

	public void setLabel( String sLabel ) {

	    m_sLabel = sLabel;
	}
    }


    //**************************
    // Constants
    //**************************

    public final static int  TAB_HORIZ_PAD = 8;
    public final static int  TAB_HEIGHT    = 25;
    public final static Font TAB_FONT      = new Font( "SansSerif", Font.PLAIN, 11 );

    //**************************
    // Member Variables
    //**************************

    protected Vector m_vPanes;
    protected Vector m_vLabelRects;
    protected int    m_nSelectedPane;

    protected ConcreteInteractionObject m_pCIO;

    protected Vector m_vListeners;


    //**************************
    // Constructor
    //**************************

    public TabbedControl( ConcreteInteractionObject pCIO ) {

	m_pCIO = pCIO;

	m_vPanes = new Vector( 4 );
	m_vLabelRects = new Vector( 4 );
	m_vListeners = new Vector();

	setLayout( null );

	m_nSelectedPane = 0;

	addComponentListener( new ComponentAdapter() {
		public void componentResized( ComponentEvent e ) {
		    resizePanes();
		}
	    });

	addMouseListener( new MouseAdapter() {
		public void mousePressed( MouseEvent e ) {
		    if ( !isEnabled() ) return;

		    for( int i = 0; i < m_vLabelRects.size(); i++ ) {
			Rectangle r = (Rectangle)m_vLabelRects.elementAt( i );

			if ( r.contains( e.getPoint() ) ) {
			    if ( m_nSelectedPane != i ) {
				setSelectedPane( i );
				callListeners();
				return;
			    }
			}
		    }
		}
	    });
    }


    //**************************
    // Member Methods
    //**************************

    public void setSize( Dimension d ) {

	super.setSize( d );

	resizePanes();
    }

    public void setSize( int nWidth, int nHeight ) {

	super.setSize( nWidth, nHeight );

	resizePanes();
    }

    public void addItemListener( ItemListener pListener ) {

	m_vListeners.addElement( pListener );
    }

    public Object[] getSelectedObjects() {

	Object ary[] = new Object[ 1 ];

	ary[ 0 ] = m_vPanes.elementAt( m_nSelectedPane );

	return ary;
    }

    public void removeItemListener( ItemListener pListener ) {

	m_vListeners.removeElement( pListener );
    }

    protected void callListeners() {

	ItemEvent evt = new ItemEvent( this,
				       m_nSelectedPane,
				       m_vPanes.elementAt( m_nSelectedPane ),
				       ItemEvent.ITEM_STATE_CHANGED );

	Enumeration e = m_vListeners.elements();
	while( e.hasMoreElements() ) {
	    ((ItemListener)e.nextElement()).itemStateChanged( evt );
	}
    }

    public Dimension getPreferredSize() {

	Dimension ret = new Dimension();

	Enumeration e = m_vPanes.elements();
	while( e.hasMoreElements() ) {
	    Dimension d = ((Panel)e.nextElement()).getPreferredSize();

	    if ( d.width > ret.width )
		ret.width = d.width;
	    if ( d.height > ret.height )
		ret.height = d.height;
	}

	ret.height += TAB_HEIGHT;

	return ret;
    }

    public Dimension getMinimumSize() {

	Dimension ret = new Dimension();

	Enumeration e = m_vPanes.elements();
	while( e.hasMoreElements() ) {
	    Dimension d = ((Panel)e.nextElement()).getMinimumSize();

	    if ( d.width > ret.width )
		ret.width = d.width;
	    if ( d.height > ret.height )
		ret.height = d.height;
	}

	ret.height += TAB_HEIGHT;

	return ret;
    }

    public void setSelectedPane( TabbedPane pPane ) {

	int nPaneIndex = m_vPanes.indexOf( pPane );

	if ( nPaneIndex >= 0 )
	    setSelectedPane( nPaneIndex );
    }

    public void setSelectedPane( int nIndex ) {
	TabbedPane p1 = (TabbedPane)m_vPanes.elementAt( m_nSelectedPane );
	TabbedPane p2 = (TabbedPane)m_vPanes.elementAt( nIndex );

	if ( p2.isEnabled() && m_nSelectedPane != nIndex ) {

	    // System.out.println( "TabbedControl: " + m_nSelectedPane + " to " + nIndex );

	    m_nSelectedPane = nIndex;
	    p1.setVisible( false );
	    p2.setVisible( true );

	    repaint();
	}
    }

    public int getPaneCount() { return m_vPanes.size(); }

    public Font getLabelFont() { return TAB_FONT; }

    public int getSelectedPaneIndex() {

	return m_nSelectedPane;
    }

    public TabbedPane getSelectedPane() {

	return (TabbedPane)m_vPanes.elementAt( m_nSelectedPane );
    }

    public void setVisible( boolean bVisible) {

	super.setVisible( bVisible );
	getSelectedPane().setVisible( bVisible );
    }

    public void addNotify() {
	super.addNotify();

	if ( m_pCIO != null )
	    m_pCIO.addNotify();
    }

    public void update( Graphics g ) {

	paint( g );

	Component[] pComps = getComponents();
	for( int i = 0; i < pComps.length; i++ ) {
	    if ( pComps[ i ].isVisible() )
		pComps[ i ].update( g );
	}
    }

    public void paint( Graphics g ) {

	System.out.println( "Starting a TabbedControl paint" );

	g.setFont( TAB_FONT );
	FontMetrics pFM = getFontMetrics( TAB_FONT );

	Color lineTextColor = isEnabled() ? Color.black : Color.gray;

	Dimension d = getSize();
	int nTabY  = d.height - TAB_HEIGHT;
	int nTextY = d.height - ( TAB_HEIGHT / 2 ) +
	    ( pFM.getAscent() / 2 );

	g.setColor( Color.lightGray );
	g.fillRect( 0, nTabY+1, d.width, TAB_HEIGHT - 1 );

	g.setColor( lineTextColor );
	g.drawLine( 0, nTabY, d.width, nTabY );

	m_vLabelRects.removeAllElements();

	int nXCursor = 0;
	for( int i = 0; i < m_vPanes.size(); i++ ) {
	    TabbedPane pTP = (TabbedPane)m_vPanes.elementAt( i );

	    int nLabelLen = pFM.stringWidth( pTP.getLabel() );
	    int nTabWidth = nLabelLen + ( TAB_HORIZ_PAD * 2 );

	    // save rect for testing mouse clicks with
	    m_vLabelRects.addElement( new Rectangle( nXCursor, nTabY, nTabWidth, TAB_HEIGHT ) );

	    if ( i == m_nSelectedPane ) {
		g.setColor( Color.white );
		g.fillRect( nXCursor, nTabY, nTabWidth, TAB_HEIGHT );
	    }

	    if ( pTP.isEnabled() ) pTP.paintAll( g );

	    g.setColor( pTP.isEnabled() ? lineTextColor : Color.gray );
	    g.drawString( pTP.getLabel(), nXCursor + TAB_HORIZ_PAD, nTextY );
	    g.setColor( lineTextColor );

	    nXCursor += nTabWidth;
	    g.drawLine( nXCursor, nTabY, nXCursor, d.height );
	    nXCursor += 1;
	}
    }

    public void addTabbedPane( TabbedPane pPane ) {
	m_vPanes.addElement( pPane );

	pPane.setSize( getSize().width, getSize().height - TAB_HEIGHT - 1 );
	this.add( pPane );

	// System.out.println( "TabbedPane setsize: " + pPane.getSize() );

	if ( m_vPanes.size() == 1 )
	    pPane.setVisible( true );
	else
	    pPane.setVisible( false );

	repaint();
    }

    public TabbedPane getTabbedPane( int nIndex ) {

	return (TabbedPane)m_vPanes.elementAt( nIndex );
    }

    //**************************
    // Private Helper Methods
    //**************************

    private void resizePanes() {
	Dimension d = getSize();

	Enumeration e = m_vPanes.elements();
	while( e.hasMoreElements() ) {
	    TabbedPane p = (TabbedPane)e.nextElement();

	    p.setLocation( 0, 0 );
	    p.setSize( d.width, d.height - TAB_HEIGHT - 1 );
	    p.doLayout();
	}
    }


    //**************************
    // Private Helper Methods
    //**************************

    public static void main( String[] args ) {

	Frame f = new Frame();
	f.setSize( 400, 400 );

	TabbedControl pTC = new TabbedControl( null );
	f.add( pTC );

	TabbedControl.TabbedPane pTB = new TabbedControl.TabbedPane( null, "Label 1" );
	pTB.add( new java.awt.Label( pTB.getLabel(), java.awt.Label.CENTER ) );
	pTC.addTabbedPane( pTB );

	pTB = new TabbedControl.TabbedPane( null, "Label 2" );
	pTB.add( new java.awt.Label( pTB.getLabel(), java.awt.Label.CENTER ) );
	pTB.setEnabled( false );
	pTC.addTabbedPane( pTB );

	pTB = new TabbedControl.TabbedPane( null, "Label 3" );
	pTB.add( new java.awt.Label( pTB.getLabel(), java.awt.Label.CENTER ) );
	pTC.addTabbedPane( pTB );

	f.doLayout();
	f.setVisible( true );
    }
}
