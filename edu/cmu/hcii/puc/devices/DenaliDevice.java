 /**
 * Title: GMC Yukon Denali 2003 class file for PUC interface
 * This is the server side Denali object for the PUC interface.
 * Simulation of remote control for car functions.
 * Copyright: Copyright (c) 2003
 * @author Rajesh Seenichamy
 * @version 1.0
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


// Class GMC Yukon Denali 2003 definition

public class DenaliDevice implements Device {


/*** Device Constants ***/
	public static final String DEVICE_NAME = "GMC Denali DIC";
	public static final String SPEC_FILE = "YukonDenali2003.xml";
	private int port = 5160;

/** StateName Constants **/

	// Ignition State
	public static final String S_IGNITION = "Denali.Ignition.IgnitionState";

	// Passenger Air Bag Indicator State
	public static final String S_BAG_INDICATOR = "Denali.Ignition.AirBag.BagIndicatorState";

	// DIC -> Trip Information -> Personal Trip flag state

	public static final String S_PERSONAL_TRIP = "Denali.Ignition.DIC.Trip.PersonalTrip.PersonalTripState";

	// DIC -> Trip Information -> Personal Trip States
	public static final String S_P_CURRENT_DISTANCE_TRAVELLED = "Denali.Ignition.DIC.Trip.PersonalTrip.PCurrentDistanceTravelledState";
	public static final String S_P_FUEL_USED = "Denali.Ignition.DIC.Trip.PersonalTrip.PFuelUsedState";
	public static final String S_P_AVERAGE_ECONOMY = "Denali.Ignition.DIC.Trip.PersonalTrip.PAverageEconomyState";
	public static final String S_P_AVERAGE_SPEED = "Denali.Ignition.DIC.Trip.PersonalTrip.PAverageSpeedState";
	public static final String S_P_TRIP_TO_ANNUAL_TRIP_MILES_RATIO = "Denali.Ignition.DIC.Trip.PersonalTrip.PTripToAnnualTripMilesRatioState";

	// DIC -> Trip Information -> Business Trip flag state

	public static final String S_BUSINESS_TRIP = "Denali.Ignition.DIC.Trip.BusinessTrip.BusinessTripState";

	// DIC -> Trip Information -> Business Trip States
	public static final String S_B_CURRENT_DISTANCE_TRAVELLED = "Denali.Ignition.DIC.Trip.BusinessTrip.BCurrentDistanceTravelledState";
	public static final String S_B_FUEL_USED = "Denali.Ignition.DIC.Trip.BusinessTrip.BFuelUsedState";
	public static final String S_B_AVERAGE_ECONOMY = "Denali.Ignition.DIC.Trip.BusinessTrip.BAverageEconomyState";
	public static final String S_B_AVERAGE_SPEED = "Denali.Ignition.DIC.Trip.BusinessTrip.BAverageSpeedState";
	public static final String S_B_TRIP_TO_ANNUAL_TRIP_MILES_RATIO = "Denali.Ignition.DIC.Trip.BusinessTrip.BTripToAnnualTripMilesRatioState";

	// DIC -> Trip Information -> Other States
	public static final String S_SEASON_ODOMETER = "Denali.Ignition.DIC.Trip.Odometer.SeasonOdometerState";
	public static final String S_HOURMETER = "Denali.Ignition.DIC.Trip.Hourmeter.HourmeterState";
	public static final String S_ANNUAL_LOG = "Denali.Ignition.DIC.Trip.AnnualLog.AnnualLogState";
	public static final String S_TIMER = "Denali.Ignition.DIC.Trip.Timer.TimerState";
	
	// DIC -> Fuel Information States
	public static final String S_FUEL_RANGE             = "Denali.Ignition.DIC.Fuel.Range.FuelRangeState";
	public static final String S_AVERAGE_FUEL_ECONOMY   = "Denali.Ignition.DIC.Fuel.AveEconomy.AverageFuelEconomyState";
	public static final String S_INSTANT_FUEL_ECONOMY   = "Denali.Ignition.DIC.Fuel.InstantEconomy.InstantFuelEconomyState";
	public static final String S_OIL_LIFE               = "Denali.Ignition.DIC.Fuel.OilLife.OilLifeState";

	// DIC -> Personalization
	public static final String S_ALARM_WARNING_TYPE     = "Denali.Ignition.DIC.Personalization.AlarmWarningTypeState";
	public static final String S_AUTOMATIC_LOCKING      = "Denali.Ignition.DIC.Personalization.AutomaticLockingState";
	public static final String S_AUTOMATIC_UNLOCKING    = "Denali.Ignition.DIC.Personalization.AutomaticUnlockingState";
	public static final String S_SEAT_POSITION_RECALL   = "Denali.Ignition.DIC.Personalization.SeatPositionRecallState";
	public static final String S_PERIMETER_LIGHTING     = "Denali.Ignition.DIC.Personalization.PerimeterLightingState";
	public static final String S_REMOTE_LOCK_FEEDBACK   = "Denali.Ignition.DIC.Personalization.RemoteLockFeedbackState";
	public static final String S_REMOTE_UNLOCK_FEEDBACK = "Denali.Ignition.DIC.Personalization.RemoteUnlockFeedbackState";
	public static final String S_HEAD_LAMPS_ON_AT_EXIT  = "Denali.Ignition.DIC.Personalization.HeadlampsOnatExitState";
	public static final String S_CURBVIEW_ASSIST        = "Denali.Ignition.DIC.Personalization.CurbViewAssistState";
	public static final String S_EASY_EXIT_SEAT         = "Denali.Ignition.DIC.Personalization.EasyExitSeatState";
	public static final String S_DISPLAY_UNITS          = "Denali.Ignition.DIC.Personalization.DisplayUnitsState";
	public static final String S_DISPLAY_LANGUAGE       = "Denali.Ignition.DIC.Personalization.DisplayLanguageState";

/** StateName variable values **/
	
	// Ignition value
	private boolean ignition = false;

	// Passenger Air Bag Indicator value
	private boolean bagindicator = false;

	public static boolean PersonalTripFlag = false;
	
	// DIC -> Trip Information -> Personal Trip values
	public static double p_current_distance_travelled = 62.7;
	public static double p_fuel_used = 5;
	public static double p_average_economy = 20.5;
	public static int p_average_speed = 40;
	public static double p_trip_to_annual_trip_miles_ratio = 35;

	public static boolean BusinessTripFlag = false;

	// DIC -> Trip Information -> Business Trip values
	public static double b_current_distance_travelled = 81.4;
	public static double b_fuel_used = 10;
	public static double b_average_economy = 21;
	public static int b_average_speed = 45;
	public static double b_trip_to_annual_trip_miles_ratio = 65;

	// DIC -> Trip Information -> Other values
	public static double season_odometer = 144.1;
	public static int hourmeter = 39;
	public static double annual_log = 144.1;
	public static double timer = 0;

	// DIC -> Fuel Information values
	public static double fuel_range = 10.5;
	public static double average_fuel_economy = 13.5;
	public static double instant_fuel_economy = 19.2;
	public static int oil_life = 65;

	// DIC -> Personalization values
	public static int alarm_warning_type = 1;
	public static int automatic_locking = 1;
	public static int automatic_unlocking = 1;
	public static int seat_position_recall = 1;
	public static int perimeter_lighting = 1;
	public static int remote_lock_feedback = 1;
	public static int remote_unlock_feedback = 1;
	public static int head_lamps_on_at_exit = 1;
	public static int curbview_assist = 1;
	public static int easy_exit_seat = 1;
	public static int display_units = 1;
	public static int display_language = 1;


/***display flags **/
	boolean displaypersonalflag = false;
	boolean displaybusinessflag = false;

	boolean displayalarmflag = false;
	boolean displayautolockflag = false;
	boolean displayautounlockflag = false;
	boolean displayseatpositionflag = false;
	boolean displayperimeterlighting = false;
	boolean displayremotelockflag = false;
	boolean displayremoteunlockflag = false;
	boolean displayheadlampflag = false;
	boolean displaycurbviewflag = false;
	boolean displayeasyexitflag = false;
	boolean displayunitsflag = false;
	boolean displaylanguageflag = false;

/** **/
// Hash table that contains all the state values
	Hashtable states = new Hashtable();

// Instance varaibles
	private Listeners listeners;
	private boolean isRunning;
	private String status = null;
	private DenaliGUI gui = null;
	private Frame f = null;

	boolean tripflagchanged = false;

// Constructor
	public DenaliDevice () {
		gui = new DenaliGUI( this );	
		f = new Frame ("Denali DIC Simulator");
		URL url = getClass().getResource("pucproxy.jpg");
		f.setIconImage(Toolkit.getDefaultToolkit().getImage(url));
		isRunning = false;
		status = "Ready";
		listeners = new Listeners(getName());

		
		f.setLayout( new BorderLayout() );
		f.add( gui );
		f.setSize (672, 687);
	}

/**** Device Methods ****/	
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
     * @param state A state name specified in the spec.
     * @param value The string value to change the named state to.
     */
    public void requestStateChange(String state, String value) {

		if (state.equals (S_PERSONAL_TRIP)) {
			PersonalTripFlag = Boolean.valueOf (value).booleanValue();
			tripflagchanged = true;
			displaypersonalflag = true;
		}

		else if (state.equals (S_BUSINESS_TRIP)) {
			BusinessTripFlag = Boolean.valueOf (value).booleanValue();
			tripflagchanged = true;
			displaybusinessflag = true;
		}

		else if (state.equals (S_ALARM_WARNING_TYPE)) {
			alarm_warning_type = Integer.parseInt (value);
			displayalarmflag = true;
		}

		else if (state.equals (S_AUTOMATIC_LOCKING)) {
			automatic_locking = Integer.parseInt (value);
			displayautolockflag = true;
		}

		else if (state.equals (S_AUTOMATIC_UNLOCKING)) {
			automatic_unlocking = Integer.parseInt (value);
			displayautounlockflag = true;
		}

		else if (state.equals (S_SEAT_POSITION_RECALL)) {
			seat_position_recall = Integer.parseInt (value);
			displayseatpositionflag = true;
		}

		else if (state.equals (S_PERIMETER_LIGHTING)) {
			perimeter_lighting = Integer.parseInt (value);
			displayperimeterlighting = true;
		}

		else if (state.equals (S_REMOTE_LOCK_FEEDBACK)) {
			remote_lock_feedback = Integer.parseInt (value);
			displayremotelockflag = true;
		}

		else if (state.equals (S_REMOTE_UNLOCK_FEEDBACK)) {
			remote_unlock_feedback = Integer.parseInt (value);
			displayremoteunlockflag = true;
		}

		else if (state.equals (S_HEAD_LAMPS_ON_AT_EXIT)) {
			head_lamps_on_at_exit = Integer.parseInt (value);
			displayheadlampflag = true;
		}

		else if (state.equals (S_CURBVIEW_ASSIST)) {
			curbview_assist = Integer.parseInt (value);
			displaycurbviewflag = true;
		}

		else if (state.equals (S_EASY_EXIT_SEAT)) {
			easy_exit_seat = Integer.parseInt (value);
			displayeasyexitflag = true;
		}

		else if (state.equals (S_DISPLAY_UNITS)) {
			display_units = Integer.parseInt (value);
			displayunitsflag = true;
		}

		else if (state.equals (S_DISPLAY_LANGUAGE)) {
			display_language = Integer.parseInt (value);
			displaylanguageflag = true;
		}

		
		if (state != null && value != null) {
			if (states.containsKey(state)) {
				states.put (state,value);
				listeners.dispatchStateEvent (state,value);
			}
			else {
				System.out.println ("State not found in the HashTable");
			}
		}

		gui.render();
		gui.repaint();

	}

	/**
     * Request that a certain command be sent to the device.
     *
     * @param command A command name, as specified in the spec.
     */
    public void requestCommandInvoke(String command){
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
		f.setVisible (isVisible); 			
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

      states.put (S_IGNITION, String.valueOf (ignition));
      states.put (S_BAG_INDICATOR, String.valueOf (bagindicator));
	  states.put (S_PERSONAL_TRIP, String.valueOf (PersonalTripFlag));
	  states.put (S_P_CURRENT_DISTANCE_TRAVELLED, String.valueOf (p_current_distance_travelled));
	  states.put (S_P_FUEL_USED, String.valueOf (p_fuel_used));
	  states.put (S_P_AVERAGE_ECONOMY, String.valueOf (p_average_economy));
	  states.put (S_P_AVERAGE_SPEED, String.valueOf (p_average_speed));
	  states.put (S_P_TRIP_TO_ANNUAL_TRIP_MILES_RATIO, String.valueOf (p_trip_to_annual_trip_miles_ratio));
	  states.put (S_BUSINESS_TRIP, String.valueOf (BusinessTripFlag));
	  states.put (S_B_CURRENT_DISTANCE_TRAVELLED, String.valueOf(b_current_distance_travelled));
	  states.put (S_B_FUEL_USED, String.valueOf (b_fuel_used));
	  states.put (S_B_AVERAGE_ECONOMY, String.valueOf (b_average_economy));
	  states.put (S_B_AVERAGE_SPEED, String.valueOf (b_average_speed));
	  states.put (S_B_TRIP_TO_ANNUAL_TRIP_MILES_RATIO, String.valueOf (b_trip_to_annual_trip_miles_ratio));
	  states.put (S_SEASON_ODOMETER, String.valueOf (season_odometer));
	  states.put (S_HOURMETER, String.valueOf (hourmeter));
	  states.put (S_ANNUAL_LOG, String.valueOf (annual_log));
	  states.put (S_TIMER, String.valueOf (timer));
	  states.put (S_FUEL_RANGE, String.valueOf (fuel_range));
	  states.put (S_AVERAGE_FUEL_ECONOMY, String.valueOf (average_fuel_economy));
	  states.put (S_INSTANT_FUEL_ECONOMY, String.valueOf (instant_fuel_economy));
	  states.put (S_OIL_LIFE, String.valueOf (oil_life));
	  states.put (S_ALARM_WARNING_TYPE, String.valueOf (alarm_warning_type));
	  states.put (S_AUTOMATIC_LOCKING, String.valueOf (automatic_locking));
	  states.put (S_AUTOMATIC_UNLOCKING, String.valueOf (automatic_unlocking));
	  states.put (S_SEAT_POSITION_RECALL, String.valueOf (seat_position_recall));
	  states.put (S_PERIMETER_LIGHTING, String.valueOf (perimeter_lighting));
	  states.put (S_REMOTE_LOCK_FEEDBACK, String.valueOf (remote_lock_feedback));
	  states.put (S_REMOTE_UNLOCK_FEEDBACK, String.valueOf (remote_unlock_feedback));
	  states.put (S_HEAD_LAMPS_ON_AT_EXIT, String.valueOf (head_lamps_on_at_exit));
	  states.put (S_CURBVIEW_ASSIST, String.valueOf (curbview_assist));
	  states.put (S_EASY_EXIT_SEAT, String.valueOf (easy_exit_seat));
	  states.put (S_DISPLAY_UNITS, String.valueOf (display_units));
	  states.put (S_DISPLAY_LANGUAGE, String.valueOf (display_language));
           
    }

/***Get Methods ***/

	// DIC -> Trip Information -> Season Odometer
	public  String getSeasonOdometer() {
		return String.valueOf(season_odometer); 
	}

	public  boolean getPersonal_Trip_Flag () {
		return PersonalTripFlag;
	}

	// DIC -> Trip Information -> Personal Trip
	public  String getP_Current_Distance_Travelled () {
		return String.valueOf(p_current_distance_travelled);
	}

	public String getP_Fuel_Used () {
		return String.valueOf(p_fuel_used);
	}

	public  String getP_Average_Economy () {
		return String.valueOf(p_average_economy);
	}

	public  String getP_Average_Speed () {
		return String.valueOf(p_average_speed);
	}

	public  String getP_Trip_To_Annual_Trip_Miles_Ratio () {
		return String.valueOf(p_trip_to_annual_trip_miles_ratio);
	} 

	public  boolean getBusiness_Trip_Flag () {
		return BusinessTripFlag;
	}

	// DIC -> Trip Information -> Business Trip
	public String getB_Current_Distance_Travelled () {
		return String.valueOf(b_current_distance_travelled);
	}

	public String getB_Fuel_Used () {
		return String.valueOf(b_fuel_used);
	}

	public String getB_Average_Economy () {
		return String.valueOf(b_average_economy);
	}

	public String getB_Average_Speed () {
		return String.valueOf(b_average_speed);
	}

	public String getB_Trip_To_Annual_Trip_Miles_Ratio () {
		return String.valueOf(b_trip_to_annual_trip_miles_ratio);
	} 

	// DIC -> Trip Information -> Other values

	public String getHourmeter () {
		return String.valueOf(hourmeter);
	}

	public String getAnnual_Log () {
		return String.valueOf(annual_log);
	}

	public String getTimer () {
		return String.valueOf(timer);
	}

	// DIC -> Fuel Information values
	public String getFuel_Range() {
		return String.valueOf(fuel_range);
	}

	public String getAverage_Fuel_Economy () {
		return String.valueOf(average_fuel_economy) ;
	}
	
	public String getInstant_Fuel_Economy () {
		return String.valueOf(instant_fuel_economy) ;
	}

	public String getOil_Life () {
		return String.valueOf(oil_life) ;
	}

	// DIC -> Personalization values

	public int getPersonalization (int choice) {

		if (choice == 0) {
			return alarm_warning_type ;
		}
		else if (choice == 1) {
			return automatic_locking ;
		}
		else if (choice == 2) {
			return automatic_unlocking ;
		}
		else if (choice == 3) {
			return seat_position_recall ;
		}
		else if (choice == 4) {
			return perimeter_lighting ;
		}
		else if (choice == 5) {
			return remote_lock_feedback ;
		}
		else if (choice == 6) {
			return remote_unlock_feedback ;
		}
		else if (choice == 7) {
			return head_lamps_on_at_exit ;
		}
		else if (choice == 8) {
			return  curbview_assist ;
		}
		else if (choice == 9) {
			return easy_exit_seat ;
		}
		else if (choice == 10) {
			return display_units ;
		}
		else if (choice == 11) {
			return display_language ;
		}
		return -1; 
	}

	public int getAlarm_Warning_Type () {
		return alarm_warning_type ;
	}

	public int getAutomatic_Locking () {
		return automatic_locking ;
	}

	public int getAutomatic_Unlocking () {
		return automatic_unlocking ;
	}

	public int getSeat_Position_Recall () {
		return seat_position_recall ;
	}

	public int getPerimeter_Lighting () {
		return perimeter_lighting ;
	}

	public int getRemote_Lock_Feedback () {
		return remote_lock_feedback ;
	}

	public int getRemote_Unlock_Feedback () {
		return remote_unlock_feedback ;
	}

	public int getHead_Lamps_On_At_Exit () {
		return head_lamps_on_at_exit ;
	}

	public int getCurbview_Assist () {
		return curbview_assist ;
	}

	public int getEasy_Exit_Seat () {
		return easy_exit_seat ;
	}

	public int getDisplay_Units () {
		return display_units ;
	}

	public int getDisplay_Language () {
		return display_language ;
	}

/*** Set Methods **/
	public void setPersonal_Trip_Flag (boolean flag) {
		PersonalTripFlag = flag;
		listeners.dispatchStateEvent (S_PERSONAL_TRIP, String.valueOf (PersonalTripFlag));
	}
	
	public void setBusiness_Trip_Flag (boolean flag) {
		BusinessTripFlag = flag;
		listeners.dispatchStateEvent (S_BUSINESS_TRIP, String.valueOf (BusinessTripFlag));
	}

	public void setPersonalization (int choice, int personalvalue) {

		if (choice == 0) {
			alarm_warning_type = personalvalue;
			listeners.dispatchStateEvent (S_ALARM_WARNING_TYPE, String.valueOf (alarm_warning_type));
		}

		else if (choice == 1) {
			automatic_locking = personalvalue;	
			listeners.dispatchStateEvent (S_AUTOMATIC_LOCKING, String.valueOf (automatic_locking));
		}

		else if (choice == 2) {
			automatic_unlocking = personalvalue;
			listeners.dispatchStateEvent (S_AUTOMATIC_UNLOCKING, String.valueOf (automatic_unlocking));
		}

		else if (choice == 3) {
			seat_position_recall = personalvalue;
			listeners.dispatchStateEvent (S_SEAT_POSITION_RECALL, String.valueOf (seat_position_recall));
		}

		else if (choice == 4) {
			perimeter_lighting = personalvalue;
			listeners.dispatchStateEvent (S_PERIMETER_LIGHTING, String.valueOf (perimeter_lighting));
		}

		else if (choice == 5) {
			remote_lock_feedback = personalvalue;
			listeners.dispatchStateEvent (S_REMOTE_LOCK_FEEDBACK, String.valueOf (remote_lock_feedback));
		}

		else if (choice == 6) {
			remote_unlock_feedback = personalvalue;
			listeners.dispatchStateEvent (S_REMOTE_UNLOCK_FEEDBACK, String.valueOf (remote_unlock_feedback));
		}

		else if (choice == 7) {
			head_lamps_on_at_exit = personalvalue;
			listeners.dispatchStateEvent (S_HEAD_LAMPS_ON_AT_EXIT, String.valueOf (head_lamps_on_at_exit));
		}

		else if (choice == 8) {
			curbview_assist = personalvalue;
			listeners.dispatchStateEvent (S_CURBVIEW_ASSIST, String.valueOf (curbview_assist));
		}

		else if (choice == 9) {
			easy_exit_seat = personalvalue;
			listeners.dispatchStateEvent (S_EASY_EXIT_SEAT, String.valueOf (easy_exit_seat));
		}

		else if (choice == 10) {
			display_units = personalvalue;
			listeners.dispatchStateEvent (S_DISPLAY_UNITS, String.valueOf (display_units));
		}

		else if (choice == 11) {
			display_language  = personalvalue;
			listeners.dispatchStateEvent (S_DISPLAY_LANGUAGE, String.valueOf (display_language));
		}
	}


} // End DenaliDevice class
