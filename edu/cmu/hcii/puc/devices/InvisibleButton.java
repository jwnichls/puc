/*
 * InvisibleButton.java
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


// Class Definition

public class InvisibleButton extends JComponent {

    //**************************
    // Constants
    //**************************

    //**************************
    // Member Variables
    //**************************

    protected boolean m_bMouseWithin;

    protected ActionListener m_pListener;


    //**************************
    // Constructor
    //**************************

    public InvisibleButton() {

	m_bMouseWithin = false;

	this.addMouseListener( new MouseAdapter() {

		public void mouseClicked( MouseEvent e ) {

		    if ( m_pListener != null )
			m_pListener.actionPerformed( new ActionEvent( this, 0, "click" ) );
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

	if ( m_bMouseWithin ) {

	    Dimension size = this.getSize();
	    Color clr = g.getColor();

	    g.setColor( Color.red );

	    g.drawRect( 0, 0, size.width-1, size.height-1 );

	    g.setColor ( clr );
	}
    }


    public void addActionListener( ActionListener listener ) {

	m_pListener = AWTEventMulticaster.add( m_pListener, listener );
    }

    public void removeActionListener( ActionListener listener ) {

	m_pListener = AWTEventMulticaster.remove( m_pListener, listener );
    }
}
