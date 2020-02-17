/**
 * ConnectDialog.java
 *
 * A simple connection dialog so that users can specify which
 * specification and connection information they want to use for their
 * controller.
 *
 * @author Jeff Nichols
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import java.awt.*;
import java.awt.event.*;


// Class Definition

public class ConnectDialog extends java.awt.Dialog {

    // Member Variables

    static String  m_sServerName = "localhost";
    static boolean m_bOpenAll = true;

    protected PUC m_PUC;

    TextField m_pServerName;
    Checkbox  m_pOpenAll;

    Button m_pOkay;
    Button m_pCancel;


    // Constructor

    public ConnectDialog( PUC pPuc ) {
	super( pPuc, "Connect to Server...", true );

	m_PUC = pPuc;

	GridBagLayout gbl = new GridBagLayout();
	GridBagConstraints pGC = new GridBagConstraints();
	setLayout( gbl );

	pGC.gridx = 0;
	pGC.gridy = 0;
	pGC.gridwidth = 1;
	pGC.gridheight = 1;
	pGC.weightx = 0.0;
	pGC.weighty = 0.0;
	pGC.anchor = GridBagConstraints.EAST;
	pGC.fill = GridBagConstraints.NONE;
	pGC.insets = new Insets( 0, 0, 0, 0 );
	pGC.ipadx = 0;
	pGC.ipady = 0;

	Label l2 = new Label( "Server:" );
	l2.setAlignment( Label.RIGHT );
	pGC.gridy = 1;
	gbl.setConstraints( l2, pGC );

	pGC.gridx = 1;
	pGC.gridwidth = 2;

	m_pServerName = new TextField( m_sServerName, 25 );
	pGC.anchor = GridBagConstraints.WEST;
	pGC.gridy = 1;
	gbl.setConstraints( m_pServerName, pGC );

	m_pOpenAll = new Checkbox( "Load All Appliances", null, m_bOpenAll );
	pGC.gridy = 2;
	gbl.setConstraints( m_pOpenAll, pGC );

	m_pCancel = new Button( "Cancel" );
	pGC.gridy = 3;
	pGC.gridwidth = 1;
	pGC.weightx = 1.0;
	pGC.weighty = 1.0;
	pGC.anchor = GridBagConstraints.CENTER;
	gbl.setConstraints( m_pCancel, pGC );

	m_pOkay = new Button( "OK" );
	pGC.gridx = 2;
	gbl.setConstraints( m_pOkay, pGC );

	m_pCancel.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
		    ConnectDialog.this.dispose();
		}
	    });

	m_pOkay.addActionListener( new ActionListener() {
		public void actionPerformed( ActionEvent e ) {

		    // m_sSpecName = m_pSpecName.getText();
		    m_sServerName = m_pServerName.getText();
		    m_bOpenAll = m_pOpenAll.getState();
		    
		    m_PUC.connectToServer( m_sServerName,
					   m_bOpenAll );
		    
		    ConnectDialog.this.dispose();
		}
	    });
	
	add( l2 );
	add( m_pServerName );
	add( m_pOpenAll );
	add( m_pCancel );
	add( m_pOkay );

	this.setSize( PUC.SCREEN_WIDTH, 125 );

	this.show();
    }
}

