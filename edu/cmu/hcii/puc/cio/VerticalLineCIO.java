/**
 * VerticalLineCIO.java
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

public class VerticalLineCIO extends IndependentCIO {

    public static class VertLineComponent extends Canvas {

	public Dimension getPreferredSize() {

	    return new Dimension( 1, 100 );
	}

	public void paint( Graphics g ) {
	    // System.out.println( "vertical line painting" );

	    g.setColor( Color.gray );
	    g.drawLine( 0, 0, 0, getSize().height );
	}
    }

    public VerticalLineCIO() {

	super( new VertLineComponent() );
    }

    public void addNotify() {

    }
}
