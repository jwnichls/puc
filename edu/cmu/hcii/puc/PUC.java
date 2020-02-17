/**
 * PUC.java
 *
 * The internal guts of the PUC.  Also serves as a repository for
 * static objects that are of global use to the PUC, such as the log
 * file service.
 *
 *
 * Revision History
 * ----------------
 * 07/07/2001: (JWN) Created file. Added static main and logging
 *                   functions
 */

// Package Definition

package edu.cmu.hcii.puc;


// Import Declarations

import com.maya.puc.common.Connection;
import com.maya.puc.common.ConnectionEvent;
import com.maya.puc.common.ConnectionListener;
import com.maya.puc.common.Message;

import edu.cmu.hcii.puc.uigen.ClientUIGenerator;
import edu.cmu.hcii.puc.uigen.UIGenerator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;


// Class Definition

public class PUC extends Frame {
    
    //**************************
    // Constants
    //**************************
    
    protected static final String VERSION_STRING = "1.0";
    
    protected static final int SCREEN_WIDTH = 240; // FIXME:JWN This
    // should be in the
    // device specification
    
    protected static final String WIDGET_REGISTRY_FILE = "registry" + File.separatorChar + "pocketpc.xml";
    
    
    //**************************
    // Member Variables
    //**************************
    
    MenuBar m_MenuBar;
    
    Menu m_pDeviceMenu;
    Menu m_pServerMenu;
    
    Vector m_vAppliances;
    Appliance m_pCurrentAppliance;

    Vector m_vServers;
    
    
    //**************************
    // Inner Classes
    //**************************
    
    protected class DeviceSwitcher implements ActionListener {
	
	Appliance m_pAppliance;
	
	public DeviceSwitcher( Appliance a ) {
	    
	    m_pAppliance = a;
	}
	
	public void actionPerformed( ActionEvent e ) {
	    
	    setCurrentAppliance( m_pAppliance );
	}
    }
    
    
    //**************************
    // Constructor
    //**************************
    
    public PUC(String prefix) {
        super("PUC v" + VERSION_STRING);
	
	Globals.init( VERSION_STRING, SCREEN_WIDTH,
		      WIDGET_REGISTRY_FILE, prefix, this );
	
        m_vAppliances = new Vector();
	m_vServers = new Vector();
	
        m_MenuBar = new MenuBar();
        setMenuBar(m_MenuBar);
	
        initMenus();
	
	setLayout( null );
        setSize(SCREEN_WIDTH, 290);
        setVisible(true);
	
        addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    PUC.this.shutdown();
		}
	    });
	
	addComponentListener(new ComponentAdapter() {
		public void componentResized( ComponentEvent e ) {
		    Enumeration en = m_vAppliances.elements();
		    while( en.hasMoreElements() ) {
			Appliance a = (Appliance)en.nextElement();
			
			a.getUIGenerator().setSize( Globals.getUIPaneSize().width, 
						    Globals.getUIPaneSize().height );
			a.getUIGenerator().setLocation( Globals.getUIPaneSize().x,
							Globals.getUIPaneSize().y );
		    }
		}
	    });
    }
    
    private void initMenus() {
	
        MenuItem m;
	
        // FILE MENU
	
        Menu file = new Menu("PUC");
        m_MenuBar.add(file);
	
        m = new MenuItem("Connect to Server...");
        m.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ConnectDialog dlg = new ConnectDialog(PUC.this);
		}
	    });
        file.add(m);
	
        file.addSeparator();
	
        m = new MenuItem("Fit Window To Panel");
        m.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Insets pI = getInsets();
		    Dimension pD = m_pCurrentAppliance.getUIGenerator().getPreferredSize();
		    PUC.this.setSize( SCREEN_WIDTH, pD.height + pI.top + pI.bottom );
		    validate();
		}
	    });
        file.add(m);
	
        m = new MenuItem("validate");
        m.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    validate();
		}
	    });
        file.add(m);
	
	m = new MenuItem("repaint");
	m.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // PUC.this.setCurrentAppliance( m_pCurrentAppliance );
		    
		    PUC.this.invalidate();
		    PUC.this.repaint();
		    PUC.this.validate();
		}
	    });
	file.add(m);
	
	file.addSeparator();
	
	m = new MenuItem("Audiophase Stereo");
	file.add( m );
	m = new MenuItem("X10 Devices (Lights & Fan)");
	file.add( m );
	m = new MenuItem("Windows Media Player");
	file.add( m );
	m = new MenuItem("Audio Request Hardware MP3 Player");
	file.add( m );
	m = new MenuItem("Sony Digital-8 Camcorder");
	file.add( m );
	m = new MenuItem("Echo Debug Device");
	file.add( m );
	
        file.addSeparator();
	
        m = new MenuItem("Exit");
        m.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    PUC.this.shutdown();
		}
	    });
        file.add(m);
	
	m_pServerMenu = new Menu( "Servers" );
	m_MenuBar.add( m_pServerMenu );

	m_pDeviceMenu = new Menu( "Devices" );

	m = new MenuItem( "Disconnect" );
	m.setEnabled( false );
	m.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Appliance pAppl = m_pCurrentAppliance;
		    m_vAppliances.removeElement( pAppl );
		    if ( m_vAppliances.size() > 0 )
			setCurrentAppliance( (Appliance)m_vAppliances.elementAt( 0 ) );
		    else {
			m_pCurrentAppliance = null;
			PUC.this.remove( pAppl.getUIGenerator() );
			m_pDeviceMenu.getItem(0).setEnabled(false);
		    }

		    m_pDeviceMenu.remove( pAppl.getMenuItem() );
		    pAppl.getServer().unload( pAppl );
		}
	    });
	m_pDeviceMenu.add( m );
	
	m_pDeviceMenu.addSeparator();

	m_MenuBar.add( m_pDeviceMenu );
    }
    

    //**************************
    // Public Methods
    //**************************

    public Menu getServerMenu() {

	return m_pServerMenu;
    }

    public void addActiveAppliance( Appliance pAppl ) {

	m_vAppliances.addElement( pAppl );

	pAppl.getMenuItem().addActionListener( new DeviceSwitcher( pAppl ) );
	m_pDeviceMenu.add( pAppl.getMenuItem() );
	
	pAppl.setUIGenerator( new ClientUIGenerator( m_MenuBar ) );

	setCurrentAppliance( pAppl );

	pAppl.getUIGenerator().setSize( Globals.getUIPaneSize().width,
					Globals.getUIPaneSize().height );
	pAppl.getUIGenerator().setLocation( Globals.getUIPaneSize().x,
					    Globals.getUIPaneSize().y );
		
	pAppl.getUIGenerator().generateUI( pAppl );
    }

    public void setCurrentAppliance( Appliance a ) {

	if ( m_pCurrentAppliance != null ) {

	    this.remove( m_pCurrentAppliance.getUIGenerator() );
	    m_pCurrentAppliance.getMenuItem().setState( false );
	}
	else
	    m_pDeviceMenu.getItem(0).setEnabled( true );

	m_pCurrentAppliance = a;
	m_pCurrentAppliance.getMenuItem().setState( true );
	this.add( m_pCurrentAppliance.getUIGenerator() );
	validate();
    }

    public void connectToServer( String sServer, boolean bOpenAll ) {

        try {
	    InetAddress pAddr = InetAddress.getByName( sServer );

	    ServerInfo pServer = new ServerInfo( pAddr, this, bOpenAll );
	    m_vServers.addElement( pServer );

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }



    //**************************
    // Shutdown
    //**************************

    public void shutdown() {

        Enumeration en = m_vAppliances.elements();

        while (en.hasMoreElements()) {
            ((Appliance) en.nextElement()).m_pConnection.disconnect();
        }

        Globals.printLog("PUC shutting down...");
        Globals.stopLogging();
        System.exit(0);
    }


    //**************************
    // Main
    //**************************

    public static void main(String[] args) {

        String pfx = "";
        if (args.length > 0)
            pfx = args[0];

        Globals.startLogging(pfx);

        new PUC(pfx);
    }
}
