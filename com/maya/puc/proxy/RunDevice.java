package com.maya.puc.proxy;

import com.maya.puc.common.Device2;
import com.maya.puc.common.DeviceFactory;
import com.maya.puc.common.DeviceFactoryListener;
import com.maya.puc.common.DeviceModuleFinder;
import com.maya.puc.common.PUCServer;
import com.maya.puc.common.StatusListener;

import java.io.File;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A class that loads a single PUC device jar file and runs it in a PUC
 * server.
 *
 * @author Joseph Hughes
 * @version $Id: RunDevice.java,v 1.2 2004/07/23 19:34:21 jeffreyn Exp $
 */

public class RunDevice extends Object
	implements StatusListener
{
	public static final String VERSION = "v1.0";
	public Device2 m_pDevice;
	public PUCServer m_pServer;

	public static void main(String argv[]) 
	{
		if (argv.length == 1) 
		{
			try 
			{
				File deviceFile = new File( "."  + File.separatorChar + argv[ 0 ] );
				
				System.out.println("PUC Single-Device Server Infrastructure " + VERSION);
				System.out.println("Loading device file: " + argv[ 0 ] );

				Device2 d = DeviceModuleFinder.loadDevice( deviceFile );
				new RunDevice( d );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else
			System.out.println( "usage: java -jar PUCDevice.jar <device jar>" );
	}

	public RunDevice( Device2 device ) 
	{
		m_pDevice = device;

		if ( m_pDevice == null )
		{
			System.out.println( "Device load failed.  Exiting..." );
			System.exit( -1 );
		}

		m_pDevice.addStatusListener( this );

		m_pServer = new PUCServer();
		m_pServer.addDevice( m_pDevice );
		m_pServer.startListener( m_pDevice );
		m_pDevice.start();
		System.out.println( "Started listening on port " + m_pDevice.getPort() );
	}

	public void statusChanged( Device2 device, String status )
	{
		System.out.println( "Device state: " + status );
	}

	public void activeChanged( Device2 device)
	{
		if ( !device.isRunning() )
		{
			System.out.println( "Device is no longer active.  Exiting..." );
			System.exit( 0 );
		}
	}
}
