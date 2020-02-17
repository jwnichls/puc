/**
 * FauxdiophaseDevice.java
 *
 * This is a server-side object for a device that pretends to control 
 * a cheap Audiophase stereo. It's mostly stubbed out for now.
 *
 * @author Thomas K. Harris
 */

//Package Definition

package edu.cmu.hcii.puc.devices;

//Import Declarations

import java.lang.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

import com.maya.puc.common.Device;
import com.maya.puc.common.Message;
import com.maya.puc.common.PUCServer;
import com.maya.puc.common.StateListener;

//Class Definition

public class FauxdiophaseDevice implements Device {
    private static String m_sName = "fauxdiophase";
    private static String m_sSpec = null;
    private Vector m_vListeners = null;
    private PUCServer m_pServer;

    //State Variables
    private boolean PowerState; //true is ON
    private boolean XBassState; //true is ON
    private interface Mode {
	public final static int TAPE = 1, CD = 2, AUX = 3, TUNER = 4;
    }
    private int ModeState;
    private boolean RadioBandState; //true is FM
    private double FMStation; //in MHz
    private int FMPresetNumber; //number from 1 to 20
    private double FMPresetValue[] = new double[20];
    private int AMStation; //in KHz
    private int AMPresetNumber; //number from 1 to 20
    private int AMPresetValue[] = new int[20];
    private interface CDPlayMode {
	public final static int Stopped = 0, Playing = 1, Paused = 2;
    }
    private int CDPlayState;
    private int CDDiscActive; //number from 1 to 5
    private boolean DiscAvail[] = new boolean[5]; //true means available
    private String CDTrackState; //the name of the current song?
    private boolean CDRandomState; //true means random is ON
    private interface CDRepeatMode {
	public final static int Off = 0, One = 1, OneDisc = 2, AllDisc = 3;
    }
    private int CDRepeatState;
    //End State Variables

    public FauxdiophaseDevice(PUCServer server) {
	m_vListeners = new Vector();
	m_pServer = server;

	//cache spec, someone will surely need it soon
	this.getSpec();

	//state inits
	PowerState = false;
	XBassState = false;
	ModeState = Mode.TUNER;
	RadioBandState = true;
	FMStation = 88.5;
	FMPresetNumber = 1;
	FMPresetValue[0] = 88.5; FMPresetValue[1] = 108;
	AMStation = 530;
	AMPresetNumber = 1;
	AMPresetValue[0] = 530; AMPresetValue[1] = 1710;
	CDPlayState = CDPlayMode.Stopped;
	CDDiscActive = 1;
	DiscAvail[0] = true; 
	DiscAvail[1] = true;
	DiscAvail[2] = true;
	DiscAvail[3] = false;
	DiscAvail[4] = false;
	CDTrackState = "Only You";
	CDRandomState = false;
	CDRepeatState = CDRepeatMode.Off;
    }

    public String getName() {
	return m_sName;
    }

    public String getSpec() {
	if (m_sSpec == null) {
	    //try to cache spec
	    try {
		File f = new File("specs", m_sName + ".xml");
		FileReader fr = new FileReader(f);
		char buf[] = new char[(int)f.length()];
		fr.read(buf, 0, (int)f.length());
		m_sSpec = new String(buf);
	    } catch (Exception e) {
		System.err.println("Couldn't read xml spec " + e.toString());
	    }
	}
	return m_sSpec;
    }
	    
    public void requestFullState() {
	dispatchStateEvent("PowerState", PowerState);
	dispatchStateEvent("XBassState", XBassState);
	dispatchStateEvent("ModeState", ModeState);
	dispatchStateEvent("RadioBandState", RadioBandState);
	dispatchStateEvent("FMStation", FMStation);
	dispatchStateEvent("FMPresetNumber", FMPresetNumber);
	for(int i=0; i<20; i++) {
	    dispatchStateEvent("FMPresetValue" + i, FMPresetValue[i]);
	}
	dispatchStateEvent("AMStation", AMStation);
	dispatchStateEvent("AMPresetNumber", AMPresetNumber);
	for(int i=0; i<20; i++) {
	    dispatchStateEvent("AMPresetValue" + i, AMPresetValue[i]);
	}
	dispatchStateEvent("CDPlayState", CDPlayState);
	dispatchStateEvent("CDDiscActive", CDDiscActive);
	for(int i=0; i<5; i++) {
	    dispatchStateEvent("DiscAvail" + i, DiscAvail[i]);
	}
	dispatchStateEvent("CDTrackState", CDTrackState);
	dispatchStateEvent("CDRandomState", CDRandomState);
	dispatchStateEvent("CDRepeatState", CDRepeatState);
    }

    public void requestStateChange(String state, String value) {
	if (state.equals("PowerState")) {
	    System.out.println("Received change request for PowerState... " 
			       + PowerState + " to " + value);
	    PowerState = Boolean.valueOf(value).booleanValue();
	    m_pServer.stateChanged(m_sName, state, value);
	} else if (state.equals("XBassState")) {
	    System.out.println("Received change request for XBassState... " 
			       + XBassState + " to " + value);
	    XBassState = Boolean.valueOf(value).booleanValue();	    
	} else if (state.equals("ModeState")) {
	    System.out.println("Received change request for ModeState... " 
			       + ModeState + " to " + value);
	    ModeState = Integer.parseInt(value);	    
	} else if (state.equals("RadioBandState")) {
	    System.out.println("Received change request for RadioBandState... " 
			       +  RadioBandState + " to " + value);
	    RadioBandState = Boolean.valueOf(value).booleanValue();	    
	} else if (state.equals("FMStation")) {
	    System.out.println("Received change request for FMStation... " 
			       + FMStation + " to " + value);
	    FMStation = Double.parseDouble(value);	    
	} else if (state.equals("FMPresetNumber")) {
	    System.out.println("Received change request for FMPresetNumber... " 
			       + FMPresetNumber + " to " + value);
	    FMPresetNumber = Integer.parseInt(value);	    
	} else if (state.startsWith("FMPresetValue")) {
	    int num = Integer.parseInt(state.substring(13)) - 1;
	    System.out.println("Received change request for " + state + "... " 
			       + FMPresetValue[num] + " to " + value);
	    FMPresetValue[num] = Double.parseDouble(value);	    
	} else if (state.equals("AMStation")) {
	    System.out.println("Received change request for AMStation... " 
			       +  AMStation + " to " + value);
	    AMStation = Integer.parseInt(value);	    
	} else if (state.equals("AMPresetNumber")) {
	    System.out.println("Received change request for AMPresetNumber... " 
			       +  AMPresetNumber + " to " + value);
	    AMPresetNumber = Integer.parseInt(value);	    
	} else if (state.startsWith("AMPresetValue")) {
	    int num = Integer.parseInt(state.substring(13));
	    System.out.println("Received change request for " + state + "... " 
			       + AMPresetValue[num] + " to " + value);
	    AMPresetValue[num] = Integer.parseInt(value);	    
	} else if (state.equals("CDPlayMode")) {
	    System.out.println("Received change request for CDPlayState... " 
			       + CDPlayState + " to " + value);
	    if (value.equals("Stopped")) {
		CDPlayState = CDPlayMode.Stopped;
	    } else if (value.equals("Playing")) {
		CDPlayState = CDPlayMode.Playing;
	    } else if (value.equals("Paused")) {
		CDPlayState = CDPlayMode.Paused;
	    }
	} else if (state.equals("CDDiscActive")) {
	    System.out.println("Received change request for CDDiscActive... " 
			       + CDDiscActive + " to " + value);
	    CDDiscActive = Integer.parseInt(value);	    
	} else if (state.startsWith("DiscAvail")) {
	    int num = Integer.parseInt(state.substring(10));
	    System.out.println("Received change request for " + state + "... " 
			       + DiscAvail[num] + " to " + value);
	    DiscAvail[num] = Boolean.valueOf(value).booleanValue();	    
	} else if (state.equals("CDTrackState")) {
	    System.out.println("Received change request for CDTrackState... " 
			       + CDTrackState + " to " + value);
	    CDTrackState = value;	    
	} else if (state.equals("CDRandomState")) {
	    System.out.println("Received change request for CDRandomState... " 
			       + CDRandomState + " to " + value);
	    CDRandomState = Boolean.valueOf(value).booleanValue();	    
	} else if (state.equals("CDRepeatState")) {
	    System.out.println("Received change request for CDRepeatState... " 
			       + CDRepeatState + " to " + value);
	    CDRepeatState = Integer.parseInt(value);	    
	}
    }

    public void requestCommandInvoke(String command) {
	if (command.equals("VolumeUp")) {
	    System.out.println("Attempting volume up");
	} else if (command.equals("VolumeDown")) {
	    System.out.println("Attempting volume down");
	} else if (command.equals("SeekForward")) {
	    System.out.println("Attempting seek forward");
	} else if (command.equals("SeekReverse")) {
	    System.out.println("Attempting seek backward");
	} else if (command.equals("CDNextTrack")) {
	    System.out.println("Attempting next track");
	} else if (command.equals("CDPrevTrack")) {
	    System.out.println("Attempting previous track");
	}
    }

    public void addStateListener(StateListener sl) {
	m_vListeners.addElement( sl );
    }

    public void removeStateListener(StateListener sl) {
	m_vListeners.removeElement( sl );
    }


    private void dispatchStateEvent(String state, String value)  {
	StateListener l;
	Enumeration en;

	en = m_vListeners.elements();
	while( en.hasMoreElements() ) {
	    l = (StateListener)en.nextElement();
	    l.stateChanged(getName(), state, value);	    
	}
    }

    private void dispatchStateEvent(String state, boolean value) {
	dispatchStateEvent(state, value?"true":"false");
    }

    private void dispatchStateEvent(String state, int value) {
	dispatchStateEvent(state, Integer.toString(value));
    }

    private void dispatchStateEvent(String state, double value) {
	dispatchStateEvent(state, Double.toString(value));
    }
}
