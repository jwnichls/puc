/**
 * RowLayout.java
 *
 * A layout manager that puts each child in a different row.  All the
 * rows may have different heights, but each has a set width,
 * PUC.SCREEN_WIDTH. 
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

public class RowLayout implements LayoutManager {

    //**************************
    // Member Variables
    //**************************

    protected int m_nWidgetSpacing;

    protected int m_nNextRowStart;

    
    //**************************
    // Constructor
    //**************************

    public RowLayout( int nSpacing ) {

	m_nWidgetSpacing = nSpacing;
    }


    //**************************
    // Layout Manager Methods
    //**************************

    public void addLayoutComponent(String name,
				   Component widget) {
	
	// JWN: Don't know if anything meaningful can be done here.
    }

    public void removeLayoutComponent(Component comp) {

	// JWN: Don't know if anything meaningful can be done here.
    }

    public Dimension preferredLayoutSize(Container parent) {
	Component[] comps = parent.getComponents();

	if ( m_nNextRowStart == 0 && comps.length > 0 )
	    layoutContainer(parent);

	Insets i = parent.getInsets();

	return new Dimension( parent.getSize().width - 20,
			      m_nNextRowStart + i.top + i.bottom );
    }

    public Dimension minimumLayoutSize(Container parent) {
	Component[] comps = parent.getComponents();

	if ( m_nNextRowStart == 0 && comps.length > 0 )
	    layoutContainer(parent);

	return new Dimension( parent.getSize().width - 20,
			      m_nNextRowStart );
    }
    

    public void layoutContainer(Container parent) {
	Insets pInsets = parent.getInsets();
	Component[] comps = parent.getComponents();

	m_nNextRowStart = pInsets.top;

	for( int i = 0; i < comps.length; i++ ) {
	    Component widget = comps[i];
	    
	    int height = widget.getPreferredSize().height;

	    widget.setSize( parent.getSize().width,
			    height );
	    widget.setLocation( pInsets.left, m_nNextRowStart );

	    if ( widget instanceof Container ) 
		((Container)widget).doLayout();

	    m_nNextRowStart += height + m_nWidgetSpacing;
	}
    }
}
