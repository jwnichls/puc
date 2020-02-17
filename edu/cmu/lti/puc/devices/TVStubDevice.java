/* This is a stub of a TV device. It is purposely stubbed so that users of a
   tutorial can experience multiple devices without having the complication
   of actually dealing with multiple devices.
*/

package edu.cmu.lti.puc.devices;

import java.util.Hashtable;
import edu.cmu.hcii.puc.devices.AbstractDevice;

public class TVStubDevice extends AbstractDevice {
    // Constants
    private static final int DEFAULT_PORT = 5169;
    private static final String SPEC_FILE = "TVStubSpec.xml";
    private static final String NAME = "Television Stub";

    // Bogus values
    private String channel = "3";
    private String playstate = "stop";

    // Constant methods
    protected int getDefaultPort() { return DEFAULT_PORT; }
    protected String getSpecFileName() { return SPEC_FILE; }
    public String getName(){ return NAME; }
    public boolean hasGUI() { return false; }
    public boolean isGUIVisible() { return false; }
    public void setGUIVisibility(boolean isVisible) { }
    public void configure() { }
    public void requestStateChange(String state, String value) {
	if(state == "Channel") {
	    channel = value;
	} else if(state == "PlayState") {
	    playstate = value;
	}
	dispatchStateEvent(state, value);
    }
    public void requestCommandInvoke(String command) {}
    public void requestFullState() {
	dispatchStateEvent("Channel", channel);
	dispatchStateEvent("PlayState", playstate);
    }
    public Hashtable getAllStates() {
	throw new UnsupportedOperationException();
    }
}
