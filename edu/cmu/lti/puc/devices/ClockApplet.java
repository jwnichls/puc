/**************************************************************************
*  A Java Alarm Clock PUC Device
*
*  by Thomas Harris (tkharris@cs.cmu.edu)
*  portions written by Dave Mitchell (zeno@vnet.ibm.com)
**************************************************************************/

//package edu.cmu.lti.puc.devices;

//import ClockDevice;

import java.io.*;
import java.awt.*;      
import java.applet.*;
import java.util.*;     
import javax.swing.*;
import java.net.*;
import java.text.SimpleDateFormat;

public class ClockApplet extends Applet {
    Calendar alarmTime = Calendar.getInstance();
    boolean alarmset = false;
    boolean bong = false;        // true when alarm ringing
    Button ab;                   // button to set alarm time
    Button bb;                   // button to set/unset alarm
    TextField tstr;              // displays current time (hh:mm)
    TextField secstr;            // displays current seconds (ss)
    TextField ampmstr;           // displays am or pm
    TextField dowstr;            // displays day of week
    TextField astr;              // displays alarm time (hh:mm)
    static final int CLOCK_TICK = 1000;  // frame dislay time for tick-tock
    Dimension dim; 
    AppletSoundList soundList;
    AudioClip alarm;
    SimpleDateFormat timef, alarmf, dowf, ampmf, secf, sayf, saydowf;
    ClockDevice clockdevice = new ClockDevice(this);
    Ticker tickthread = null;
    Calendar now = Calendar.getInstance();
    long drift = 0;             //the time drift in milliseconds
                                //displayed time is system time plus drift
    
    public void init() {
	//get the alarm sound file
	soundList = new AppletSoundList(this, getCodeBase());
	soundList.startLoading("spacemusic.au");

	// pick a common font
	setFont(new Font("TimesRoman", Font.PLAIN, 12)); 
	setForeground(Color.black);

	alarmTime.set(Calendar.HOUR_OF_DAY, 0);
	alarmTime.set(Calendar.MINUTE, 0);
	
	// set up date formats
	timef = new SimpleDateFormat("h:mm");
	alarmf = new SimpleDateFormat("h:mm a");
	dowf = new SimpleDateFormat("E");
	ampmf = new SimpleDateFormat("a");
	secf = new SimpleDateFormat("ss");
	sayf = new SimpleDateFormat("H:mm");
	saydowf = new SimpleDateFormat("EEEE");

	// layout the window
	BorderLayout bdl = new BorderLayout();
	setLayout(bdl);
	ab = new Button("Set Alarm Time");// set alarm time button
	bb = new Button("Set Alarm");     // set/unset alarm button
	tstr = new TextField("",5);       // min/sec display
	tstr.setFont(new Font("DialogInput", Font.BOLD, 72));
	tstr.setBackground(Color.black);
	tstr.setForeground(Color.green);
	tstr.setEditable(false);          // is read-only
	dowstr = new TextField("",3);
	dowstr.setFont(new Font("DialogInput", Font.BOLD, 24));
	dowstr.setBackground(Color.black);
	dowstr.setForeground(Color.green);
	dowstr.setEditable(false);
	secstr = new TextField("",2);
	secstr.setFont(new Font("DialogInput", Font.BOLD, 24));
	secstr.setBackground(Color.black);
	secstr.setForeground(Color.green);
	secstr.setEditable(false);
	ampmstr = new TextField("",2);
	ampmstr.setFont(new Font("DialogInput", Font.BOLD, 24));
	ampmstr.setBackground(Color.black);
	ampmstr.setForeground(Color.green);
	ampmstr.setEditable(false);
	astr = new TextField("");         // and so is alarm display
	astr.setEditable(false);
	Panel right = new Panel();
	right.setBackground(Color.black);
	right.setLayout(new BorderLayout());
	right.add("North",dowstr);
	right.add("Center",ampmstr);
	right.add("South",secstr);
	Panel buttonPanel = new Panel();
	buttonPanel.setBackground(Color.black);
	buttonPanel.setLayout(new BorderLayout());
	buttonPanel.add("Center", ab);
	buttonPanel.add("South", bb);
	add("West",tstr);
	add("Center",right);
	add("East",buttonPanel);
	add("South",astr);
	validate();
	dim = bdl.minimumLayoutSize(this);
	//System.err.println(dim.height + " x " + dim.width);
	stopAlarm();                      // ensure alarm is off
	clockdevice.init();
	repaint();
	tickthread = new Ticker();
	tickthread.initMe();
    }
    
    public void stop() {
	System.out.println("stopping...");
	tickthread.interrupt();
	//clockdevice.stop();
	clockdevice.destroy();
    }

    public void destroy() {
	System.out.println("destroying...");
	super.destroy(); //shouldn't be necessary but can't hurt
    }

    public int tellHeight() {
	return dim.height;
    }

    public int tellWidth() {
	return dim.width;
    }

    public void start() {
	setVisible(true);                           // make ourselves visible
	clockdevice.start();
	tickthread.start();
	tickthread.initMe();
    }

    class Ticker extends Thread {
	boolean go = true;
	public void initMe() {
	    go = true;
	}
	public void run() {
	    while (true) {
		// do whatever animation is needed
		if (go) {
		    repaint();
		}
		// wait nicely
		try {
		    sleep(CLOCK_TICK);
		} catch (InterruptedException e){
		    go = false;
		}
	    }
	}
    }
    
    public boolean action(Event e, Object o) {
	if (ab == e.target) {      // Either Stop or set time
	    if (bong) {            // if it's Stop
		stopAlarm();        // we stop it
	    } else {               // else we ask for a new time
		SetAlarmDialog ab = new SetAlarmDialog(this, alarmTime);
		ab.setVisible(true);
	    }
	} else if (bb == e.target) {
	    setAlarm(!alarmset);
	}
	return true;
    }

    // this method turns a SMALL integer into a 2 character string
    // e.g. 12 -> "12" and 9 -> "09"
    String twoDigits(int i, char pad) {
	String s;
	s = "" + i;       // ensure it's got 3 digits
	if (s.length() < 2) {s = pad + s;}
	return s;
    }

    public void setAlarmTime(int h, int m) {
	alarmTime.set(Calendar.HOUR_OF_DAY, h);
	alarmTime.set(Calendar.MINUTE, m);
	setAlarm(true);
	dispatchAlarmTime();
    }

    public void setTime(int h, int m) {
	long oldmillis = now.getTimeInMillis();
	now.set(Calendar.HOUR_OF_DAY, h);
	now.set(Calendar.MINUTE, m);
	drift += now.getTimeInMillis() - oldmillis;
	dispatchTime();
    }
    
    public void setDOW(String dowstring) {
	long oldmillis = now.getTimeInMillis();
	if(dowstring.equals("Sunday")) {
	    now.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	} else if(dowstring.equals("Monday")) {
	    now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	} else if(dowstring.equals("Tuesday")) {
	    now.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
	} else if(dowstring.equals("Wednesday")) {
	    now.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
	} else if(dowstring.equals("Thursday")) {
	    now.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
	} else if(dowstring.equals("Friday")) {
	    now.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
	} else if(dowstring.equals("Saturday")) {
	    now.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
	} else {
	    //some error, ignore
	    System.err.println("unknown dowstring: " + dowstring);
	}
	drift += now.getTimeInMillis() - oldmillis;
	dispatchTime();
    }
	
    public void setAlarmTime(String timestring) {
	String s;
	int h, m;

	// try to parse the time as "hh:mm" or "hh/mm" or "hh mm"
	StringTokenizer st = new StringTokenizer(timestring,":/ ");
	try {
	    // get the hour
	    s = st.nextToken();
	    // convert to an integer
	    h = Integer.parseInt(s);
	    // get the minute
	    s = st.nextToken();
	    // convert to an integer
	    m = Integer.parseInt(s);
	} catch (Exception e) {
	    // ignore errors
	    h = -1; m = -1;
	}
	// if valid time, reset the alarm
	if (h != -1)
	    setAlarmTime(h,m);
    }

    public void setTime(String timestring) {
	String s;
	int h, m;

	// try to parse the time as "hh:mm" or "hh/mm" or "hh mm"
	StringTokenizer st = new StringTokenizer(timestring,":/ ");
	try {
	    // get the hour
	    s = st.nextToken();
	    // convert to an integer
	    h = Integer.parseInt(s);
	    // get the minute
	    s = st.nextToken();
	    // convert to an integer
	    m = Integer.parseInt(s);
	} catch (Exception e) {
	    // ignore errors
	    h = -1; m = -1;
	}
	// if valid time, reset the alarm
	if (h != -1)
	    setTime(h,m);
    }

    public void setAlarm(boolean set) {
	if (set) {
	    bb.setLabel("Unset Alarm");
	} else {
	    if (bong) {stopAlarm();}
	    bb.setLabel("Set Alarm");
	}
	alarmset = set;
	paintAlarm();
	dispatchAlarmSet();
    }
    
    public void soundAlarm() {
	// set the alarm display to show we're ringing
	astr.setText("Alarm ringing!");
	ab.setLabel("Stop Alarm");// label the buttton "Stop"
	alarm = soundList.getClip("spacemusic.au");
	alarm.loop();
	bong = true;              // we are ringing
    }
    
    private void paintAlarm() {
	astr.setText("Alarm time is " + alarmf.format(alarmTime.getTime()) + " -- Alarm is " + (alarmset? "enabled": "disabled"));
    }

    public void stopAlarm() {
	// set the alarm display to show there isn't one set
	paintAlarm();
	ab.setLabel("Set Alarm Time"); // label the button "Set"
	if(bong) {alarm.stop();}
	bong = false;             // we're not ringing
    }
    
    public void paint(Graphics g) {
	now.setTimeInMillis(Calendar.getInstance().getTimeInMillis() + drift);
	Date time = now.getTime();

        // display the time
	String timetext = timef.format(time);
	if (timetext.length() < 5) timetext = ' ' + timetext;
	tstr.setText(timetext);
	secstr.setText(secf.format(time));
	ampmstr.setText(ampmf.format(time));
	dowstr.setText(dowf.format(time));

	// update time state on the top of each minute
	if (now.get(Calendar.SECOND) == 0) {
	    dispatchTime();
	    if (bong == false && 
		alarmTime.get(Calendar.MINUTE) == now.get(Calendar.MINUTE) && 
		alarmTime.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) && 
		alarmset) {
		// it's time to ring the alarm
		soundAlarm();
	    }

	    // update the day of week state at midnight
	    if (now.get(Calendar.MINUTE) == 0 && 
		now.get(Calendar.HOUR_OF_DAY) == 0) {
		dispatchDOW();
	    }
	}

	super.paint(g);
    }
    
    public void update(Graphics g) {
	paint(g); // don't clear the background
    }

    //accessor methods
    public void dispatchTime() {
	clockdevice.dispatchTime(sayf.format(now.getTime()));
    }

    public void dispatchDOW() {
	clockdevice.dispatchDOW(saydowf.format(now.getTime()));
    }

    public void dispatchAlarmSet() {
	//stop alarm is called even before the clockdevice is created
	if (clockdevice != null) { 
	    clockdevice.dispatchAlarmSet(String.valueOf(alarmset));
	}
    }

    public void dispatchAlarmTime() {
	clockdevice.dispatchAlarmTime(sayf.format(alarmTime.getTime()));
    }

    //Loads and holds a bunch of audio files whose locations are specified
    //relative to a fixed base URL.
    class AppletSoundList extends java.util.Hashtable {
	Applet applet;
	URL baseURL;
	
	public AppletSoundList(Applet applet, URL baseURL) {
	    super(5); //Initialize Hashtable with capacity of 5 entries.
	    this.applet = applet;
	    this.baseURL = baseURL;
	}
	
	public void startLoading(String relativeURL) {
	    new AppletSoundLoader(applet, this,
				  baseURL, relativeURL);
	}
	
	public AudioClip getClip(String relativeURL) {
	    return (AudioClip)get(relativeURL);
	}
	
	public void putClip(AudioClip clip, String relativeURL) {
	    put(relativeURL, clip);
	}
    }
    
    class AppletSoundLoader extends Thread {
	Applet applet;
	AppletSoundList soundList;
	URL baseURL;
	String relativeURL;
	
	public AppletSoundLoader(Applet applet, 
				 AppletSoundList soundList,
				 URL baseURL,
				 String relativeURL) {
	    this.applet = applet;
	    this.soundList = soundList;
	    this.baseURL = baseURL;
	    this.relativeURL = relativeURL;
	    setPriority(MIN_PRIORITY);
	    start();
	}
	
	public void run() {
	    AudioClip audioClip = applet.getAudioClip(baseURL, relativeURL);
	    soundList.putClip(audioClip, relativeURL);
	}
    }
    
    // this class handles getting a new Alarm time
    class SetAlarmDialog extends Dialog {
	TextField at;
	ClockApplet parent;
	
	public SetAlarmDialog(ClockApplet par, Calendar deftime)
	{
	    // make a dialog
	    super(new Frame(), "Set Alarm Time", true);
	    // fill it up with all the necessary widgets
	    TextField t;
	    parent = par;
	    t = new TextField("Enter new alarm time:");
	    t.setEditable(false);
	    add("West",t);
	    at = new TextField(sayf.format(deftime.getTime()));
	    add("East",at);
	    Panel p = new Panel();
	    p.add("West", new Button("Set Alarm Time"));
	    p.add("East", new Button("Cancel"));
	    add("South",p);
	    pack();                           // make it just fit
	    setSize(getPreferredSize());
	    setLocation(200,200);
	}
	
	public boolean handleEvent(Event evt) {
	    switch(evt.id) {
	    case Event.WINDOW_DESTROY:     // time to go
		dispose();
		return true;
	    case Event.ACTION_EVENT:
		if ("Set Alarm Time".equals(evt.arg)) {
		    parent.setAlarmTime(at.getText());
		    // OK button was pressed
		} else if ("Cancel".equals(evt.arg)) {
		    // Cancel button was pressed - do nothing
		}
		dispose();
		return true;
	    default:
		return false;
	    }
	}
    }   
}



