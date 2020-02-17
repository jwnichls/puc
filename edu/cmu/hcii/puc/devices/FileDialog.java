/**
 * FileDialog.java
 *
 * A simple connection dialog so that users can specify which
 * specification and connection information they want to use for their
 * controller.
 *
 * @author Jeff Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import java.awt.*;
import java.awt.event.*;

import edu.cmu.hcii.puc.PUC;


// Class Definition

public class FileDialog extends java.awt.Dialog {

    // Member Variables

    protected FileDialogListener m_pListener;

    TextField m_pSpecName;

    Button m_pOkay;
    Button m_pCancel;


    // Constructor

    public FileDialog( String sTitle, String sDefault, FileDialogListener pListener ) {
	super( new Frame(), sTitle, true );

	m_pListener = pListener;

	GridBagLayout gbl = new GridBagLayout();
	setLayout( gbl );

	Label l1 = new Label( "Spec:" );
	l1.setAlignment( Label.RIGHT );
	gbl.setConstraints( l1, new GridBagConstraints( 0, 0, 1, 1, 0, 0,
							GridBagConstraints.EAST,
							GridBagConstraints.NONE,
							new Insets( 0, 0, 0, 0 ),
							0, 0
							) );

	m_pSpecName = new TextField( sDefault, 25 );
	gbl.setConstraints( m_pSpecName, new GridBagConstraints( 1, 0, 2, 1, 0, 0,
								 GridBagConstraints.WEST,
								 GridBagConstraints.NONE,
								 new Insets( 0, 0, 0, 0 ),
								 0, 0
								 ) );

	m_pCancel = new Button( "Cancel" );
	gbl.setConstraints( m_pCancel, new GridBagConstraints( 1, 3, 1, 1, 1, 1,
							       GridBagConstraints.CENTER,
							       GridBagConstraints.NONE,
							       new Insets( 0, 0, 0, 0 ),
							       0, 0
							       ) );

	m_pOkay = new Button( "OK" );
	gbl.setConstraints( m_pOkay, new GridBagConstraints( 2, 3, 1, 1, 1, 1,
							     GridBagConstraints.CENTER,
							     GridBagConstraints.NONE,
							     new Insets( 0, 0, 0, 0 ),
							     0, 0
							     ) );

	m_pCancel.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {

		    m_pListener.fileChosen( true, null );
		    FileDialog.this.dispose();
		}
	    });

	m_pOkay.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {

		    m_pListener.fileChosen( false, m_pSpecName.getText() );
		    FileDialog.this.dispose();
		}
	    });

	add( l1 );
	add( m_pSpecName );
	add( m_pCancel );
	add( m_pOkay );

	this.setSize( 200, 75 );

	this.show();
    }
}

