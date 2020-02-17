/*
 * KnobControl.java
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


// Class Definition

public class KnobControl extends JComponent {

    //**************************
    // Constants
    //**************************

    //**************************
    // Member Variables
    //**************************

    protected boolean m_bMouseWithin;

    protected double m_dAngle;
    protected double m_dMinAngle;
    protected double m_dIncr;
    protected int m_nMaxIncr;
    protected int m_nIncr;

    protected ActionListener m_pListener;


    //**************************
    // Constructor
    //**************************

    public KnobControl( double minAngle, double incr, int maxIncr ) {

	m_dMinAngle = minAngle;
	m_dIncr = incr;
	m_dAngle = m_dMinAngle;
	m_nMaxIncr = maxIncr;
	m_nIncr = 0;

	m_bMouseWithin = false;

	m_pListener = null;

	this.addMouseListener( new MouseAdapter() {

		public void mouseClicked( MouseEvent e ) {

		    int oldIncr = m_nIncr;

		    if ( e.getPoint().x > ( getSize().width / 2 ) )
			m_nIncr--;
		    else
			m_nIncr++;

		    if ( m_nIncr > m_nMaxIncr )
			m_nIncr = m_nMaxIncr;

		    if ( m_nIncr < 0 )
			m_nIncr = 0;

		    m_dAngle = m_dIncr * m_nIncr + m_dMinAngle;

		    if ( oldIncr != m_nIncr && m_pListener != null )
			m_pListener.actionPerformed( new ActionEvent( this, 0, "knob" ) );

		    repaint();
		}

		public void mouseEntered( MouseEvent e ) {

		    m_bMouseWithin = true;
		    repaint();
		}

		public void mouseExited( MouseEvent e ) {

		    m_bMouseWithin = false;
		    repaint();
		}
	    });
    }


    //**************************
    // Member Methods
    //**************************

    public void paint( Graphics g ) {

	Color clr = g.getColor();

	if ( m_bMouseWithin )
	    g.setColor( Color.red );

	Dimension size = this.getSize();
	int xRadius = ( size.width - 1 ) / 2;
	int yRadius = ( size.height - 1 ) / 2;
	Point center = new Point( xRadius, yRadius );
	Point indic = new Point( xRadius - (int)(Math.cos( m_dAngle ) * xRadius),
				 yRadius - (int)(Math.sin( m_dAngle ) * yRadius) );
	
	g.drawOval( 0, 0, size.width-1, size.height-1 );

	Graphics2D g2 = (Graphics2D)g;
	Stroke oldStroke = g2.getStroke();
	g2.setStroke( new BasicStroke( 4 ) );
	g.drawLine( xRadius, yRadius, (size.width-1) - indic.x, indic.y );
	g2.setStroke( oldStroke );

	g.setColor ( clr );
    }

    public int getIndex() {

	return m_nIncr;
    }

    public void setIndex( int incr ) {

	if ( incr >= 0 && incr <= m_nMaxIncr ) {
	    m_nIncr = incr;
	    m_dAngle = m_dIncr * m_nIncr + m_dMinAngle;
	    repaint();
	}
    }


    public void addActionListener( ActionListener listener ) {

	m_pListener = AWTEventMulticaster.add( m_pListener, listener );
    }

    public void removeActionListener( ActionListener listener ) {

	m_pListener = AWTEventMulticaster.remove( m_pListener, listener );
    }
}

