package com.maya.puc.proxy;

import com.maya.puc.common.Device2;
import com.maya.puc.common.DeviceFactory;
import com.maya.puc.common.DeviceFactoryListener;
import com.maya.puc.common.DeviceModuleFinder;
import com.maya.puc.common.PUCServer;
import com.maya.puc.common.StatusListener;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A class that implements the functionality of PUCProxy.
 *
 * @author Joseph Hughes
 * @version $Id: PUCProxy.java,v 1.15 2003/08/04 22:23:34 klitwack Exp $
 */

public class PUCProxy extends Object
    implements WindowListener, StatusListener, DeviceFactoryListener
{

    public static final String VERSION = "v2.1";
    public static final String CONFIG_FILE = "pucproxy.cfg";

    private static String searchPath = ".";

    private static PUCProxy theInstance = null;

    private Dimension minsize = null;
    private boolean limitMaxSize = true;
    private JFrame frame = null;
    private java.util.List devices = (java.util.List) new ArrayList();
    private java.util.List deviceFactories = (java.util.List) new ArrayList();
    private JTable deviceTable = null;
    private DeviceTableModel tableModel = null;
    private JScrollPane scroll = null;

    public static void main(String argv[]) {

      if (argv.length == 0)
        searchPath = ".";
      else
        searchPath = argv[0];

      PUCProxy pp;
      pp = getInstance();
    }

    private PUCProxy()
    {
      init();
    }

    /**
     * PUCProxy is now implemented as a single instance, so any single VM has
     * exactly one static PUCProxy object.  This is so devices can make their
     * "configure" dialogs be children of the PUCProxy frame.
     *
     * @return The instance of PUCProxy
     */
    public static PUCProxy getInstance()
    {
      if (theInstance == null)
        theInstance = new PUCProxy();

      return theInstance;
    }

    /**
     * Use this method to get a reference to the main frame of a running
     * PUCProxy.  See documentation for PUCProxy.getInstance() for an
     * explanation of getting a reference to the PUCProxy object.
     *
     * @return The main frame
     */
    public JFrame getFrame()
    {
      return frame;
    }

    public void init()
    {
      System.out.println("PUCProxy " + VERSION);
      System.out.println("Search for devices and factories in path "
                         + new File(searchPath).getAbsolutePath());

      frame = new JFrame();

      /**
       * First search for devices, i.e. .jar files in the search path
       * whose Manifest.mf file has the following attribute:
       *
       *  Device-Class: package.ClassName
       *
       * Then, adds these devices to the table.
       */
      devices = DeviceModuleFinder.getDevices(searchPath);
      Iterator it = devices.iterator();
      while (it.hasNext())
      {
        Device2 device = (Device2) it.next();
        device.addStatusListener(this);
        System.out.println("Found Device: " + device.getName());
      }

      tableModel = new DeviceTableModel(devices);

      /**
       * After the intial table of existing devices is made, search for
       * Device Factories.  These are .jar files in the search path whose
       * Manifest.mf file has the following attribute:
       *
       *  Device-Factory: package.ClassName
       *
       * Then the DeviceFactory.addListener() method is called on each
       * DeviceFactory, with this PUCProxy as the argument.  This gives
       * the DeviceFactory a pointer to PUCProxy.loadNewDevice and
       * PUCProxy.removeDevice, which should be called as devices are found
       * and removed, respectively.
       */
      deviceFactories = DeviceModuleFinder.getDeviceFactories(searchPath);
      it = deviceFactories.iterator();
      while (it.hasNext())
      {
        DeviceFactory factory = (DeviceFactory) it.next();
        factory.addListener(this);
        System.out.println("Found Device Factory: " + factory.getName());
      }

      // Then initialize the GUI as necessary

      initMenus();

      deviceTable = new JTable(tableModel);

      // Set column widths
      TableColumn col = null;
      int numCols = deviceTable.getModel().getColumnCount();
      for (int i = 0; i < numCols; i++)
      {
        col = deviceTable.getColumnModel().getColumn(i);
        col.setPreferredWidth(DeviceTableModel.COL_WIDTHS[i]);
      }

      int preferredTableWidth = 590;
      int preferredTableHeight =
          (deviceTable.getRowCount() + 1) * deviceTable.getRowHeight();
      deviceTable.setPreferredScrollableViewportSize(
        new Dimension(preferredTableWidth, preferredTableHeight));

      scroll = new JScrollPane(deviceTable);
      frame.getContentPane().add(scroll, BorderLayout.CENTER);

      frame.setLocation(250, 250);
      try {
        URL url = getClass().getResource("pucproxy.jpg");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
      }
      catch (Exception ex) { }

      String ipaddr;
      try
      {
        ipaddr = InetAddress.getLocalHost().getHostAddress();
      }
      catch (UnknownHostException uhEx)
      {
        ipaddr = "Unknown IP Address";
      }

      frame.setTitle("PUCProxy on \"" + ipaddr + "\"");
      frame.addWindowListener(this);

      frame.pack();
      frame.show();
    }

    public void initMenus() {

	JMenuBar pMenuBar = new JMenuBar();

	JMenu pMenu = new JMenu( "Device" );

	JMenuItem pMI = new JMenuItem( "Load From .JAR..." );
	pMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JFileChooser chooser = new JFileChooser();
		    FileFilter pFilter = new FileFilter() {

			    public boolean accept( File f ) {

				return f.isDirectory() ||
				    ( f.isFile() && f.canRead() && f.getName().endsWith( ".jar" ) );
			    }

			    public String getDescription() {

				return "PUC Device Modules (*.jar)";
			    }
			};
		    chooser.setFileFilter( pFilter );
		    if ( chooser.showOpenDialog( null ) ==
			 JFileChooser.APPROVE_OPTION ) {
			try {
			    Device2 d = DeviceModuleFinder.loadDevice( chooser.getSelectedFile() );
			    loadNewDevice(d);
			}
			catch( Exception ex ) {
			    System.out.println( "Trouble loading device..." );
			    // ex.printStackTrace();
			}
		    }
		}
	    });
	pMenu.add( pMI );

	pMenu.addSeparator();

	pMI = new JMenuItem( "Exit" );
	pMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    exit();
		}
	    });
	pMenu.add( pMI );

	pMenuBar.add( pMenu );

	frame.setJMenuBar( pMenuBar );
    }

    private void initColumnWidths(JTable table, TableModel model)
    {
      TableColumn column = null;
      Component comp = null;
      int headerWidth = 0;
      int cellWidth = 0;

      for (int i = 0; i < 6; i++)
      {
        column = table.getColumnModel().getColumn(i);

        try
        {
          comp = column.getHeaderRenderer().
              getTableCellRendererComponent(
              null, column.getHeaderValue(),
              false, false, 0, 0);
          headerWidth = comp.getPreferredSize().width;
        }
        catch (NullPointerException e)
        {
          System.err.println("Null pointer exception!");
          System.err.println("  getHeaderRenderer returns null in 1.3.");
          System.err.println("  The replacement is getDefaultRenderer.");
        }

        int numRows = table.getRowCount();
        int maxWidth = 0;
        for (int j = 0; j < numRows; j++)
        {
          comp = table.getDefaultRenderer(model.getColumnClass(i)).
              getTableCellRendererComponent(
              table, model.getValueAt(j, i),
              false, false, j, i);
          cellWidth = comp.getPreferredSize().width;
          if (cellWidth > maxWidth) maxWidth = cellWidth;
        }

        column.setPreferredWidth(Math.max(headerWidth, maxWidth));
      }
    }

    public void exit() {

        Iterator it = devices.iterator();
        while (it.hasNext()) {
            Device2 dev = (Device2) it.next();
            tableModel.stopDevice( dev );
        }
        System.exit(0);
    }

    public void windowClosing(WindowEvent e) {

        exit();
    };

    public void windowOpened(WindowEvent e) {
    };
    public void windowClosed(WindowEvent e) {
    };
    public void windowIconified(WindowEvent e) {
    };
    public void windowDeiconified(WindowEvent e) {
    };
    public void windowActivated(WindowEvent e) {
    };
    public void windowDeactivated(WindowEvent e) {
    };

    public void connectionStatusUpdated() {
    }

    public void statusChanged( Device2 device, String status )
    {
      tableModel.update();
    }

    public void activeChanged( Device2 device)
    {
      tableModel.setActive(device, device.isRunning());
    }

    // DeviceFactoryListener methods

    /**
     * Loads a new device.  Does this by calling tableModel.addDevice(), to
     * update the UI, and then adding this PUCProxy object as a StatusListener
     * to the device.
     *
     * @param device The device to load
     */
    public void loadNewDevice(Device2 device)
    {
      if (device != null)
      {
        tableModel.addDevice(device);
        device.addStatusListener(this);
      }
    }

    /**
     * Removes a device.
     *
     * @param device The device to remove.
     */
    public void removeDevice(Device2 device)
    {
      if (device != null)
      {
        tableModel.removeDevice(device);
      }
    }

}
