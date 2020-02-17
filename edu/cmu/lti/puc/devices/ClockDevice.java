//package edu.cmu.lti.puc.devices;

import edu.cmu.hcii.puc.devices.AbstractDevice;

import com.maya.puc.common.DeviceWrapper;
import com.maya.puc.common.Device;
import com.maya.puc.common.PUCServer;
import com.maya.puc.common.Device2;

import java.util.Hashtable;
import java.util.Enumeration;

public class ClockDevice extends AbstractDevice {
    // Constants
    private static final int DEFAULT_PORT = 5168;
    private static final String SPEC_FILE = "AlarmClockSpec.xml";
    private static final String NAME = "Alarm Clock";

    // Device constants
    public static final String COMMAND_STOP_ALARM = "StopAlarm";

    public static final String STATE_TIME = "TimeState";
    public static final String STATE_DOW = "DayOfWeekState";
    public static final String STATE_ALARMSET = "AlarmState";
    public static final String STATE_ALARMTIME = "AlarmTimeState";

    // Instance variables
    ClockApplet clock = null;
    Device2 d2 = null;
    PUCServer server = null;

    // Constructor
    public ClockDevice(ClockApplet app) {
	clock = app;
	server = new PUCServer();
	d2 = new DeviceWrapper((Device)this);
    }

    public void init() {
	server.addDevice(d2);
	server.startListener(d2.getPort(),d2);
    }

    // Constant methods
    protected int getDefaultPort() { return DEFAULT_PORT; }
    protected String getSpecFileName() { return SPEC_FILE; }
    public String getName(){ return NAME; }
    public boolean hasGUI() { return false; }
    public boolean isGUIVisible() { return false; }
    public void setGUIVisibility(boolean isVisible) { }
    public void configure() { }

    public void destroy() {
	server.stopListener(d2);
	server.removeDevice(d2);
	server.stop();
    }

    public void requestStateChange(String state, String value) {
	if (state.equals(STATE_ALARMTIME)) {
	    clock.setAlarmTime(value);
	} else if (state.equals(STATE_ALARMSET)) {
	    clock.setAlarm(value.equals("true"));
	} else if (state.equals(STATE_TIME)) {
	    clock.setTime(value);
	} else if (state.equals(STATE_DOW)) {
	    clock.setDOW(value);
	} else {
	    System.out.println("requested state change for unknown state: " +
			       state);
	}
    }

    public void requestCommandInvoke(String command) {
	System.out.println("Applet Received command: " + command);
	if (command.equals(COMMAND_STOP_ALARM)) {
	    clock.stopAlarm();
	}
    }

    public void requestFullState() {
	System.out.println("Full state update requested...");	
	if (clock == null) return;

	clock.dispatchTime();
	clock.dispatchDOW();
	clock.dispatchAlarmSet();
	clock.dispatchAlarmTime();
    }

    public Hashtable getAllStates() {
	throw new UnsupportedOperationException();
    }

    public void dispatchTime(String time) {
	dispatchStateEvent(STATE_TIME, time);
    }

    public void dispatchDOW(String dow) {
	dispatchStateEvent(STATE_DOW, dow);
    }

    public void dispatchAlarmSet(String alarmSet) {
	dispatchStateEvent(STATE_ALARMSET, alarmSet);
    }

    public void dispatchAlarmTime(String alarmTime) {
	dispatchStateEvent(STATE_ALARMTIME, alarmTime);
    }
}
