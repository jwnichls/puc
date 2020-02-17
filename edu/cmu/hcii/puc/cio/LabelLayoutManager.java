/**
 * LabelLayoutManager.java
 *
 * A silly little device / quick hack to make labels appear nicely
 * within my interfaces.  This layout manager makes a big assumption,
 * which is that there are only two containers, one of which is a
 * label and the other is some widget.  If this assumption is not
 * maintained, odd things may occur.
 *
 * Revision History
 * ----------------
 * 10/10/2001 (JWN) Created file.
 *
 */

// Package Definition

package edu.cmu.hcii.puc.cio;


// Import Declarations

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Insets;
import java.lang.*;

import edu.cmu.hcii.puc.PUC;


// Class Definition

public class LabelLayoutManager implements LayoutManager {

    // constants

    private static final int LABEL_WIDTH = 75;


    public void addLayoutComponent(String name,
				   Component comp) {

    }

    public void removeLayoutComponent(Component comp) {

    }

    public Dimension preferredLayoutSize(Container parent) {
	Insets i = parent.getInsets();
	Component[] comps = parent.getComponents();

	Dimension d0 = comps[0].getPreferredSize();
	Dimension d1 = comps[1].getPreferredSize();

	return new Dimension( parent.getSize().width,
			      Math.max( d0.height, d1.height ) );
    }

    public Dimension minimumLayoutSize(Container parent) {
	Component[] comps = parent.getComponents();

	Dimension d0 = comps[0].getMinimumSize();
	Dimension d1 = comps[1].getMinimumSize();

	return new Dimension( Math.max( d0.width, d1.width ) + LABEL_WIDTH,
			      Math.max( d0.height, d1.height ) );
    }
    
    public void layoutContainer(Container parent) {
	Insets i = parent.getInsets();
	Component[] comps = parent.getComponents();

	Component lbl = comps[0] instanceof Label ? comps[0] :
	    comps[1];
	Component widget = comps[0] instanceof Label ? comps[1] :
	    comps[0]; 

	lbl.setSize( LABEL_WIDTH, parent.getSize().height );
	lbl.setLocation( 0, 0 );

	widget.setSize( parent.getSize().width - LABEL_WIDTH,
			parent.getSize().height );
	widget.setLocation( LABEL_WIDTH, 0 );
    }
}
