/**
 * LineCIO.java
 *
 * This is a stupid little CIO that creates a line on the screen.  It
 * is used for dividing panels in situations where they are called.
 *
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.*;


// Class Definition

public class LineCIO extends IndependentCIO {

    public static class LineComponent extends Canvas {

	public Dimension getPreferredSize() {

	    return new Dimension( 100, 1 );
	}

	public void paint( Graphics g ) {
	    // System.out.println( "painting LineComponent" );

	    g.setColor( Color.gray );
	    g.drawLine( 0, 0, getSize().width, 0 );
	}
    }

    public LineCIO() {

	super( new LineComponent() );
    }

    public void addNotify() {

    }
}
