/** 
 * X10Proxy.java
 *
 * This is a generic proxy for PUC appliances.  It contains no extra
 * UI code for a specialized appliance.
 *
 * @author Jeff Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.devices;
 

// Import Declarations

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;

import com.maya.puc.common.*;


// Class Definition

public class X10Proxy extends Object implements Runnable {
	
    public static final String VERSION = "v1.00";
    
    private X10Device x10 = null;
    private PUCServer server = null;
    
    public static void main(String argv[])  {
	X10Proxy pp = new X10Proxy();
	pp.run();
    }
    
    public X10Proxy()  {
    }
    
    public void run()  {
	System.out.println("X10Proxy " + VERSION);
	
	server = new PUCServer();
	
	x10 = new X10Device();

	server.addDevice(x10);
	
	server.startListener(5151, x10.getName());
    }
}
