/**
 * GenericProxy.java
 *
 * This is a generic proxy for PUC appliances.  It contains no extra
 * UI code for a specialized appliance.
 *
 * OUT OF DATE : OUT OF DATE : OUT OF DATE : OUT OF DATE
 *
 * The new generic PUCProxy removes the need for this class.
 *
 * @author Jeff Nichols
 */

// Package Definition

package edu.cmu.hcii.puc.devices;


// Import Declarations

import com.maya.puc.common.PUCServer;


// Class Definition

public class GenericProxy extends Object implements Runnable {

    public static final String VERSION = "v0.00";

    private WinAmpDevice winamp = null;
    private FauxdiophaseDevice faux = null;
    private EchoDevice echo = null;
    private PUCServer server = null;

    public static void main(String argv[]) {
        GenericProxy pp = new GenericProxy();
        pp.run();
    }

    public GenericProxy() {
    }

    public void run() {
        System.out.println("GenericProxy " + VERSION);

        server = new PUCServer();

        winamp = new WinAmpDevice();
        echo = new EchoDevice();
	faux = new FauxdiophaseDevice();

        server.addDevice(winamp);
        server.addDevice(echo);
	server.addDevice(faux);

	server.startListener(5153, faux.getName());
        server.startListener(5152, winamp.getName());
        server.startListener(5150, echo.getName());
    }
}
