/**
 * Title: GMC Yukon Denali 2003 Navigation Unit class file for PUC interface
 * This is the server side Navigation object for the PUC interface.
 * Simulation of remote control for Navigation features.
 * @author Rajesh Seenichamy
 *
 */

// Package Definition

package edu.cmu.hcii.puc.devices;
import java.awt.*;
import java.lang.*;
import java.net.URL;


// Import user-defined package for PUC

import com.maya.puc.common.Device;
import com.maya.puc.common.PUCServer;
import com.maya.puc.common.StateListener;
import com.maya.puc.common.TextResource;

// Import standard classes
import java.util.Enumeration;
import java.util.Hashtable;

// Class GMC Yukon Navigation Unit Definition

public class DenaliNavigationDevice implements Device {
    
    // Device Constants
    public static final String DEVICE_NAME = "GMC Denali Navigation";
    public static final String SPEC_FILE = "DenaliNavigationSpec.xml";
    private int port = 5162;
    
    // StateName Constants
    public static final String STATE_MODE = "Denali.Mode.UnitMode";
    
    public static final String STATE_CONTRAST			  = "Denali.Config.ContrastState";
    public static final String STATE_BRIGHTNESS			  = "Denali.Config.BrightnessState";
    public static final String STATE_SCREEN_COLOR		  = "Denali.Config.ScreenColorState";
    public static final String STATE_BEEP				  = "Denali.Config.BeepState";
    public static final String STATE_TIME_HRS             = "Denali.Config.Clock.Hrs";
    public static final String STATE_TIME_MINS            = "Denali.Config.Clock.Mins";
    public static final String STATE_TIME_HRMODE          = "Denali.Config.Clock.HrMode";
    public static final String STATE_TIME_GPSUPDATE       = "Denali.Config.Clock.GPSUpdate";
    public static final String STATE_TIME_DAYLIGHT        = "Denali.Config.Clock.DaylightSavings";
    public static final String STATE_TIME_TIMEZONE        = "Denali.Config.Clock.TimeZone";
    
    public static final String STATE_NAV_MODE   = "Denali.Nav.NavMode";
    
    public static final String STATE_VOICE_CONTROL        = "Denali.Nav.Settings.AdaptiveVoiceVolumeState";
    public static final String STATE_VOICE_VOLUME	  = "Denali.Nav.Settings.VoiceVolumeState";
    public static final String STATE_SUSPEND_GUIDE        = "Denali.Nav.Settings.SuspendGuidance";
    public static final String STATE_MAP_ORIENTATION      = "Denali.Nav.Settings.Map.MapOrientationState";
    public static final String STATE_GUIDANCE_MODE        = "Denali.Nav.Settings.Map.GuidanceModeState";
    public static final String STATE_MAP_MODE             = "Denali.Nav.Settings.Map.MapModeState";

    public static final String STATE_UNITS				  = "Denali.Nav.Settings.Setup.UnitsState";
    public static final String STATE_SEASONAL_RESTRICT    = "Denali.Nav.Settings.Setup.SeasonalRestrictState";
    public static final String STATE_TRAVEL_TIME          = "Denali.Nav.Settings.Setup.TravelTimeState";
    public static final String STATE_OPERATION_GUIDE      = "Denali.Nav.Settings.Setup.OperationGuide";
    public static final String STATE_AUTO_REROUTE         = "Denali.Nav.Settings.Setup.AutoRerouteState";
    public static final String STATE_VOICE_GUIDE          = "Denali.Nav.Settings.Setup.VoiceGuideState";
    public static final String STATE_CURRENT_STREET_NAME  = "Denali.Nav.Settings.Setup.CurrentStreetState";
    public static final String STATE_DAY_MAP_COLOR        = "Denali.Nav.Settings.Setup.DayMapColorState";
    public static final String STATE_NIGHT_MAP_COLOR      = "Denali.Nav.Settings.Setup.NightMapColorState";
    public static final String STATE_FREEWAY_SPEED        = "Denali.Nav.Settings.Setup.Speed.Freeway";
    public static final String STATE_MAINSTREET_SPEED     = "Denali.Nav.Settings.Setup.Speed.MainStreet";
    public static final String STATE_RESIDENTIAL_SPEED    = "Denali.Nav.Settings.Setup.Speed.Residential";
    public static final String STATE_EXIT_INFO            = "Denali.Nav.Settings.Setup.ExitInfoState";
    //public static final String STATE_HOME_ADDRESS         = "Denali.Nav.Settings.Home.Address";
    
    public static final String STATE_USE_FREEWAY          = "Denali.Nav.Route.Prefs.FreewayState";
    public static final String STATE_USE_FERRY            = "Denali.Nav.Route.Prefs.FerryState";
    public static final String STATE_USE_TOLLROAD         = "Denali.Nav.Route.Prefs.TollRoadState";
    public static final String STATE_USE_RESTRICTEDROAD   = "Denali.Nav.Route.Prefs.RestrictedRoadState";
    //public static final String STATE_AVOID_AREAS         = "Denali.Nav.Route.Prefs.Avoid";
        
    // StateName values
    public int unit_mode = 1;
    public int nav_mode = 1;
    public int contrast = 20;
    public int brightness = 30;
    public int screen_color = 1;
    public boolean beep = true;
    public int hr = 22, min = 15, timeZone = 4;
    public boolean is12Hr = false, GPSUpdate = false, daylightSavings = false;
    public boolean voice_control = true;
    public int voice_volume = 25;
    public int units = 1;
    public boolean freeway = true;
    public boolean toll_road = false;
    public boolean ferry = false;
    public boolean restricted_road = false;
    public int map_orientation = 1;
    public int guidance_mode = 1;
    public int map_mode = 1;
    public boolean seasonal_restrict = true;
    public boolean travel_time = true;
    public boolean auto_reroute = true;
    public boolean voice_guide = true;
    public boolean current_street_name = true;
    public int day_map_color = -1;
    public int night_map_color = -1;
    public boolean guide_map = true;
    public boolean exit_info = true;
    
    // Instance variables
    private Listeners listeners;
    private boolean isRunning;
    private String status = null;
    private DenaliNavigationGUI gui = null;
    private Frame f = null;
    
    // Hash table that contains all the state values
    Hashtable states = new Hashtable();
    
    // Constructor
    public DenaliNavigationDevice() {
        
        gui = new DenaliNavigationGUI(this);
        f = new Frame("Denali Navigation Simulator");
        URL url = getClass().getResource("pucproxy.jpg");
        f.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
        
        isRunning = false;
        status = "Ready";
        listeners = new Listeners(getName());
        
        f.setLayout(new BorderLayout());
        f.add(gui);
        f.setSize(650, 390);
    }
    
    /* Device Methods **/
    /**
     * Get the human-readable name of the device supported by this class.
     */
    public String getName() {
        return DEVICE_NAME;
    }
    
    /**
     * Get the XML spec used to generate the interface for this device.
     */
    public String getSpec() {
        return TextResource.readToString(this.getClass(), SPEC_FILE);
    }
    
    /**
     * Request that the full device state be sent out.
     */
    public void requestFullState(){
        
        System.out.println("Full state update requested...");
        
        getAllStates();
        Enumeration keys = states.keys();
        while (keys.hasMoreElements()) {
            String state = (String) keys.nextElement();
            listeners.dispatchStateEvent(state, (String) states.get(state));
        }
    }
    
    /**
     * Request that certain state be changed.
     *
     * state A state name specified in the spec.
     * value The string value to change the named state to.
     */
    public void requestStateChange(String state, String value) {
        
        if (state.equals(STATE_MAP_ORIENTATION)) {
            map_orientation = Integer.parseInt(value);
            gui.buttonclickflag = true;
            gui.CurrentScreen = 3;
            gui.CurrentCtrl = 9;
            gui.render();
            gui.repaint();
        }
        if (state.equals(STATE_GUIDANCE_MODE)) {
            guidance_mode = Integer.parseInt(value);
            gui.buttonclickflag = true;
            gui.CurrentScreen = 3;
            gui.CurrentCtrl = 9;
            gui.render();
            gui.repaint();
        }
        if (state.equals(STATE_MAP_MODE)) {
            map_mode = Integer.parseInt(value);
            gui.buttonclickflag = true;
            gui.CurrentScreen = 3;
            gui.CurrentCtrl = 9;
            gui.render();
            gui.repaint();
        }
        if (state.equals(STATE_USE_FREEWAY)) {
            freeway = Boolean.valueOf(value).booleanValue();
            gui.buttonclickflag = true;
            gui.CurrentScreen = 15;
            gui.CurrentCtrl = 9;
            gui.render();
            gui.repaint();
        }
        if (state.equals(STATE_USE_TOLLROAD)) {
            toll_road = Boolean.valueOf(value).booleanValue();
            gui.buttonclickflag = true;
            gui.CurrentScreen = 15;
            gui.CurrentCtrl = 9;
            gui.render();
            gui.repaint();
        }
        if (state.equals(STATE_USE_FERRY)) {
            ferry = Boolean.valueOf(value).booleanValue();
            gui.buttonclickflag = true;
            gui.CurrentScreen = 15;
            gui.CurrentCtrl = 9;
            gui.render();
            gui.repaint();
        }
        if (state.equals(STATE_USE_RESTRICTEDROAD)) {
            restricted_road = Boolean.valueOf(value).booleanValue();
            gui.buttonclickflag = true;
            gui.CurrentScreen = 15;
            gui.CurrentCtrl = 9;
            gui.render();
            gui.repaint();
        }
        
        if (state != null && value != null) {
            if (states.containsKey(state)) {
                states.put(state,value);
                listeners.dispatchStateEvent(state,value);
            }
            else {
                System.out.println("State not found in the HashTable");
            }
        }
        
                /*gui.render();
                gui.repaint();*/
    }
    
    /**
     * Request that a certain command be sent to the device.
     *
     * command A command name, as specified in the spec.
     */
    public void requestCommandInvoke(String command){
        /*
        if (command.equals(C_NAVIGATION)) {
            gui.buttonclickflag = true;
            gui.CurrentScreen = 9;
        } else if (command.equals(C_ZOOMIN)) {
            gui.buttonclickflag = true;
            gui.CurrentScreen = 8;
        } else if (command.equals(C_ZOOMOUT)) {
            gui.buttonclickflag = true;
            gui.CurrentScreen = 9;
        } else if (command.equals(C_ROUTE_OVERVIEW)) {
            gui.buttonclickflag = true;
            gui.CurrentScreen = 2;
            gui.CurrentCtrl = 2;
            gui.buttonno = 2;
        } else if (command.equals(C_ROUTE_PREVIEW)) {
            gui.buttonclickflag = true;
            gui.CurrentScreen = 2;
            gui.CurrentCtrl = 7;
            gui.buttonno = 2;
        }
        gui.render();
        gui.repaint();
         */
    }
    
    /**
     * Register a StateListener to receive state change notifications.
     */
    public void addStateListener(StateListener sl) {
        listeners.addElement(sl);
    }
    
    /**
     * Remove a StateListener from the notification list.
     */
    public void removeStateListener(StateListener sl){
        listeners.removeElement(sl);
    }
    
    /**
     * Take steps to configure the device (i.e. pop up a configuration dialog).
     */
    public void configure() {
        
    }
    
    /**
     * Determine whether the device has a (non-configuration) GUI.
     */
    public boolean hasGUI(){
        return true;
    }
    
    /**
     * Specify whether the device GUI should be visible on the screen.
     */
    public void setGUIVisibility(boolean isVisible) {
        f.setVisible(isVisible);
    }
    
    /**
     * Determine whether the device's GUI is visible on the screen.
     */
    public boolean isGUIVisible() {
        return (f.isVisible());
    }
    
    /**
     * Establish a connection with the device, and begin reporting state.
     */
    public void start() {
        isRunning = true;
        status = "Listening";
        listeners.updateStatus();
        listeners.setActive(true);
    }
    
    /**
     * Stop generating state updates.
     */
    public void stop() {
        isRunning = false;
        status = "Stopped";
        listeners.updateStatus();
        listeners.setActive(false);
    }
    
    /**
     * Determine whether the device is currently active.
     */
    public boolean isRunning(){
        return isRunning;
    }
    
    /**
     * Get the current status of this device.  Useful for reporting
     * connection errors.  Incidentally, to notify the PUCProxy program
     * that the status string has changed, send a state update
     * with state and value set to null to the StateListeners.
     */
    public String getStatus(){
        return status;
    }
    
    /**
     * Set the port that clients will connect to in order to
     * access this device.
     */
    public int getPort(){
        return port;
    }
    
    /**
     * Retrieve the number of the port that PUC clients will
     * connect to in order to access this device.
     */
    public void setPort(int port){
        this.port = port;
    }
    
    private void dispatchStateEvent(String state, String value) {
        StateListener l;
        
        for (int i = 0; i < listeners.size(); i++) {
            l = (StateListener) listeners.elementAt(i);
            l.stateChanged(getName(), state, value);
        }
    }
    
    // Public State Accessor Methods
    
    /**
     * Accessor for all states, to be used in a requestFullState call.  The
     * returned Hashtable contains the state string constants as keys, and
     * their values (converted to Strings if necessary) as values
     */
    public void getAllStates() {
        states.put(STATE_MODE, String.valueOf(unit_mode));
        states.put(STATE_CONTRAST, String.valueOf(contrast));
        states.put(STATE_BRIGHTNESS, String.valueOf(brightness));
        states.put(STATE_SCREEN_COLOR, String.valueOf(screen_color));
        states.put(STATE_BEEP, String.valueOf(beep));
        states.put(STATE_TIME_HRS, String.valueOf(hr));
        states.put(STATE_TIME_MINS, String.valueOf(min));
        states.put(STATE_TIME_HRMODE, String.valueOf(is12Hr));
        states.put(STATE_TIME_GPSUPDATE, String.valueOf(GPSUpdate));
        states.put(STATE_TIME_DAYLIGHT, String.valueOf(daylightSavings));
        states.put(STATE_TIME_TIMEZONE, String.valueOf(timeZone));
        states.put(STATE_NAV_MODE, String.valueOf(nav_mode));
        states.put(STATE_VOICE_CONTROL, String.valueOf(voice_control));
        states.put(STATE_VOICE_VOLUME, String.valueOf(voice_volume));
        
        states.put(STATE_MAP_ORIENTATION, String.valueOf(map_orientation));
        states.put(STATE_GUIDANCE_MODE, String.valueOf(guidance_mode));
        states.put(STATE_MAP_MODE, String.valueOf(map_mode));
        
        states.put(STATE_UNITS, String.valueOf(units));
        states.put(STATE_SEASONAL_RESTRICT, String.valueOf(seasonal_restrict));
        states.put(STATE_TRAVEL_TIME, String.valueOf(travel_time));
        states.put(STATE_OPERATION_GUIDE, String.valueOf(guide_map));
        states.put(STATE_AUTO_REROUTE, String.valueOf(auto_reroute));
        states.put(STATE_VOICE_GUIDE, String.valueOf(voice_guide));
        states.put(STATE_CURRENT_STREET_NAME, String.valueOf(current_street_name));
        states.put(STATE_DAY_MAP_COLOR, String.valueOf(day_map_color));
        states.put(STATE_NIGHT_MAP_COLOR, String.valueOf(night_map_color));
        
        
        
        states.put(STATE_EXIT_INFO, String.valueOf(exit_info));
        states.put(STATE_USE_FREEWAY, String.valueOf(freeway));
        states.put(STATE_USE_FERRY, String.valueOf(ferry));
        states.put(STATE_USE_TOLLROAD, String.valueOf(toll_road));
        states.put(STATE_USE_RESTRICTEDROAD, String.valueOf(restricted_road));
    }
    
    /**
     * Set Methods
     **/
    
    public void SetScreenColor(int value) {
        screen_color = value;
        listeners.dispatchStateEvent(STATE_SCREEN_COLOR, String.valueOf(screen_color));
    }
    
    public void SetContrast(int value) {
        contrast = value;
        listeners.dispatchStateEvent(STATE_CONTRAST, String.valueOf(contrast));
    }
    
    public void SetBrightness(int value) {
        brightness = value;
        listeners.dispatchStateEvent(STATE_BRIGHTNESS, String.valueOf(brightness));
    }
    
    public void SetBeep(int value) {
        if (value == 1)
            beep = true;
        else
            beep = false;
        listeners.dispatchStateEvent(STATE_BEEP, String.valueOf(beep));
    }
    
    public void SetTimeHr(int value) {
        hr = value;
        listeners.dispatchStateEvent(STATE_TIME_HRS, String.valueOf(hr));
    }
    
    public void SetTimeMin(int value) {
        min = value;
        listeners.dispatchStateEvent(STATE_TIME_MINS, String.valueOf(min));
    }
    
    public void SetHrMode(int value) {
        if (value == 1)
            is12Hr = true;
        else
            is12Hr = false;
        listeners.dispatchStateEvent(STATE_TIME_HRMODE, String.valueOf(is12Hr));
    }
        
    public void SetGPSUpdate(int value) {
        if (value == 1)
            GPSUpdate = true;
        else 
            GPSUpdate = false;
        listeners.dispatchStateEvent(STATE_TIME_GPSUPDATE, String.valueOf(GPSUpdate));
    }
    
    public void SetDaylightSavings(int value) {
        if (value == 1)
            daylightSavings = true;
        else
            daylightSavings = false;
        listeners.dispatchStateEvent(STATE_TIME_DAYLIGHT, String.valueOf(daylightSavings));
    }
    
    public void SetTimeZone(int value) {
        timeZone = value;
        listeners.dispatchStateEvent(STATE_TIME_TIMEZONE, String.valueOf(timeZone));
    }
    
    public void SetMapOrientation(int value) {
        map_orientation = value;
        listeners.dispatchStateEvent(STATE_MAP_ORIENTATION, String.valueOf(map_orientation));
    }
    public void SetGuidanceMode(int value) {
        guidance_mode = value;
        listeners.dispatchStateEvent(STATE_GUIDANCE_MODE, String.valueOf(guidance_mode));
    }
    public void SetMapMode(int value) {
        map_mode = value;
        listeners.dispatchStateEvent(STATE_MAP_MODE, String.valueOf(map_mode));
    }
    public void SetFreeway(int value) {
        if (value == 1)
            freeway = true;
        else
            freeway = false;
        listeners.dispatchStateEvent(STATE_USE_FREEWAY, String.valueOf(freeway));
    }
    public void SetTollRoad(int value) {
        if (value == 1)
            toll_road = true;
        else
            toll_road = false;
        listeners.dispatchStateEvent(STATE_USE_TOLLROAD, String.valueOf(toll_road));
    }
    public void SetFerry(int value) {
        if (value == 1)
            ferry = true;
        else
            ferry = false;
        listeners.dispatchStateEvent(STATE_USE_FERRY, String.valueOf(ferry));
    }
    public void SetRestrictedRoad(int value) {
        if (value == 1)
            restricted_road = true;
        else
            restricted_road = false;
        listeners.dispatchStateEvent(STATE_USE_RESTRICTEDROAD, String.valueOf(restricted_road));
    }
    
    public void SetSeasonalRestrict(int value) {
        if (value == 1)
            seasonal_restrict = true;
        else
            seasonal_restrict = false;
        listeners.dispatchStateEvent(STATE_SEASONAL_RESTRICT, String.valueOf(seasonal_restrict));
    }
    
    public void SetTravelTime(int value) {
        if (value ==1)
            travel_time = true;
        else
            travel_time = false;
        listeners.dispatchStateEvent(STATE_TRAVEL_TIME, String.valueOf(travel_time));
    }
    
    public void SetAutoReroute(int value) {
        if (value == 1)
            auto_reroute = true;
        else
            auto_reroute = false;
        listeners.dispatchStateEvent(STATE_AUTO_REROUTE, String.valueOf(auto_reroute));
    }
    public void SetDayMapColor(int value) {
        day_map_color = value;
        listeners.dispatchStateEvent(STATE_DAY_MAP_COLOR, String.valueOf(day_map_color));
    }
    public void SetNightMapColor(int value) {
        night_map_color = value;
        listeners.dispatchStateEvent(STATE_NIGHT_MAP_COLOR, String.valueOf(night_map_color));
    }    
    public void SetVoiceGuide(int value) {
        if (value == 1)
            voice_guide = true;
        else
            voice_guide = false;
        listeners.dispatchStateEvent(STATE_VOICE_GUIDE, String.valueOf(voice_guide));
    }

    public void SetCurrentStreetName(int value) {
        if (value == 1)
            current_street_name = true;
        else
            current_street_name = false;
        listeners.dispatchStateEvent(STATE_CURRENT_STREET_NAME, String.valueOf(current_street_name));
    }
    
    public void SetGuideMap(int value) {
        if (value == 1)
            guide_map = true;
        else
            guide_map = false;
        listeners.dispatchStateEvent(STATE_OPERATION_GUIDE, String.valueOf(guide_map));
    }

    public void SetExitInfo(int value) {
        if (value == 1)
            exit_info = true;
        else
            exit_info = false;
        listeners.dispatchStateEvent(STATE_EXIT_INFO, String.valueOf(exit_info));
    }
    
    public void SetEnglishMetric(int value) {
        units = value;
        listeners.dispatchStateEvent(STATE_UNITS, String.valueOf(units));
    }
    
    /**
     * Get Methods
     **/
    public int GetScreenColor() {
        return screen_color;
    }
    
    public int GetContrast() {
        return contrast;
    }
    
    public int GetBrightness() {
        return brightness;
    }
    
    public boolean GetBeep() {
        return beep;
    }
    
    public int GetTimeHr() {
        return hr;
    }
    
    public int GetTimeMin() {
        return min;
    }
    public boolean GetHrMode() {
        return is12Hr;
    }
    
    public boolean GetGPSUpdate() {
        return GPSUpdate;
    }
    
    public boolean GetDaylightSavings() {
        return daylightSavings;
    }
    
    public int GetTimeZone() {
        return timeZone;
    }
    
    public int GetMapOrientation() {
        return map_orientation;
    }
    public int GetGuidanceMode() {
        return guidance_mode;
    }
    public int GetMapMode() {
        return map_mode;
    }
    public boolean GetFreeway() {
        return freeway;
    }
    public boolean GetTollRoad() {
        return toll_road;
    }
    public boolean GetFerry() {
        return ferry;
    }
    public boolean GetRestrictedRoad() {
        return restricted_road;
    }
    public boolean GetSeasonalRestrict() {
        return seasonal_restrict;
    }    
    public boolean GetTravelTime() {
        return travel_time;
    }
    public boolean GetAutoReroute() {
        return auto_reroute;
    }
    public boolean GetVoiceGuide() {
        return voice_guide;
    }
    public boolean GetCurrentStreetName() {
        return current_street_name;
    }
    public boolean GetGuideMap() {
        return guide_map;
    }
    public boolean GetExitInfo() {
        return exit_info;
    }
    public int GetEnglishMetric() {
        return units;
    }
    public int GetDayMapColor() {
        return day_map_color;
    }
    public int GetNightMapColor() {
        return night_map_color;
    }
}

