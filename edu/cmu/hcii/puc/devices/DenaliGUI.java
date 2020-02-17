package edu.cmu.hcii.puc.devices;

import java.awt.*;
import java.awt.event.MouseListener;
import java.net.URL;

public class DenaliGUI extends Panel implements MouseListener{

	private String DenaliImageName = "newsteeringwheel.jpg";

	Image buffer = null;
	Image DenaliImage = null;
	
	Font DenaliDICFont;

	Dimension prefSize = new Dimension(50, 50);

	String DisplayLabel[][] = { 
	{"SEASON ODOMETER: " , "PERSONAL TRIP", "BUSINESS TRIP", "HOURMETER: ", "ANNUAL LOG: ", "TIMER: ",
		 "PERSONAL: ", "PERSONAL: ", "PERSONAL: ", "PERSONAL: ", "PERSONAL: ", 
		 "BUSINESS: ", "BUSINESS: ", "BUSINESS: ", "BUSINESS: ", "BUSINESS: " }, 
	{"RANGE: ", "AVG ECON: ", "INST ECON: ", "ENGINE OIL LIFE: " },
	{"ALARM WARNING TYPE", "AUTOMATIC LOCKING", "AUTOMATIC UNLOCKING", "SEAT POSITION RECALL", "PERIMETER LIGHTING",
	 "REMOTE LOCK FEEDBACK", "REMOTE UNLOCK FEEDBACK", "HEADLAMPS ON AT EXIT", "CURB VIEW ASSIST", "EASY EXIT SEAT", "DISPLAY UNITS",
	 "DISPLAY LANGUAGE"}
	};

	String TripFlag [] = { "", "" };

	String DisplayLabelPersonalization[][] = {
	{ "ALARM WARNING: BOTH", "ALARM WARNING: OFF", "ALARM WARNING: HORN", "ALARM WARNING: LAMPS" },
	{ "LOCK DOORS OUT OF PARK", "LOCK DOORS MANUALLY", "LOCK DOORS WITH SPEED" },
	{ "UNLOCK ALL IN PARK", "UNLOCK ALL AT KEY OUT", "UNLOCK DOORS MANUALLY", "UNLOCK DRIVER IN PARK" },
	{ "SEAT POSITION RECALL OFF", "SEAT POSITION RECALL AT KEY IN", "SEAT POSITION RECALL ON REMOTE" },
	{ "PERIMETER LIGHTING ON", "PERIMETER LIGHTING OFF" },
	{ "LOCK FEEDBACK: BOTH", "LOCK FEEDBACK: OFF", "LOCK FEEDBACK: HORN", "LOCK FEEDBACK: LAMPS" },
	{ "UNLOCK FEEDBACK: LAMPS", "UNLOCK FEEDBACK: BOTH", "UNLOCK FEEDBACK: OFF", "UNLOCK FEEDBACK: HORN" },
	{ "HEADLAMP DELAY: 10 SEC", "HEADLAMP DELAY: 20 SEC", "HEADLAMP DELAY: 40 SEC", "HEADLAMP DELAY: 60 SEC", "HEADLAMP DELAY: 120 SEC", 
		"HEADLAMP DELAY: 180 SEC", "HEADLAMP DELAY OFF" } ,
	{ "CURB VIEW: OFF", "CURB VIEW: PASSENGER", "CURB VIEW: DRIVER", "CURB VIEW: BOTH" },
	{ "SEAT POSITION EXIT OFF", "SEAT POSITION EXIT ON" },
	{ "UNITS: ENGLISH", "UNITS: METRIC" },
	{ "English", "French", "Spanish" }
	};

	Rectangle buttonArea[] = {
        new Rectangle(195, 462, 31, 26), // Trip Information 226,488
        new Rectangle(185, 486, 29, 26), // Fuel Information 214, 512
        new Rectangle(437, 452, 33, 30), // Personalization 470, 482
        new Rectangle(445, 480, 37, 25), // Select 482, 505
	};

	int buttonindex;
	
	int previousclick;
	int currentclick;

	int submenu = 0;

	int personalize;

	int currentset;
		
	int scrollcount = -1;

	boolean buttonclickflag = false;

	boolean GUIPersonalTripFlag;
	boolean GUIBusinessTripFlag;

	boolean showtripinfo = false;

	int setwhattrip;

	int rootclick;

	boolean rotateflag = true;
	boolean bothtripflag = false;

	boolean personal = true;

	DenaliDevice denalidevice = null;

	public DenaliGUI ( DenaliDevice dev ) {
		 denalidevice = dev;
		 DenaliDICFont = new Font("Monospaced", Font.BOLD, 35);
		 this.addMouseListener(this);
		 init();		
	}
	
	public void init () {

        URL url;
        Toolkit tk = Toolkit.getDefaultToolkit();
        try {
            MediaTracker mt = new MediaTracker(this);
            int id = 0;
            url = getClass().getResource (DenaliImageName);
            DenaliImage = tk.getImage (url);
            mt.addImage (DenaliImage, 0);
            mt.waitForAll();
        } catch (InterruptedException e) {
            System.err.println(e);
        }
        prefSize = new Dimension (DenaliImage.getWidth(this), DenaliImage.getHeight(this));
		SetDisplayLabels ();
		requestFocus();
		render();
		repaint();
	}

	void SetDisplayLabels () {
		DisplayLabel[0][0]  = DisplayLabel[0][0]  + denalidevice.getSeasonOdometer();
		DisplayLabel[0][3]  = DisplayLabel[0][3]  + denalidevice.getHourmeter(); 
		DisplayLabel[0][4]  = DisplayLabel[0][4]  + denalidevice.getAnnual_Log();
		DisplayLabel[0][5]  = DisplayLabel[0][5]  + denalidevice.getTimer();
		DisplayLabel[1][0]  = DisplayLabel[1][0]  + denalidevice.getFuel_Range();
		DisplayLabel[1][1]  = DisplayLabel[1][1]  + denalidevice.getAverage_Fuel_Economy();
		DisplayLabel[1][2]  = DisplayLabel[1][2]  + denalidevice.getInstant_Fuel_Economy();
		DisplayLabel[1][3]  = DisplayLabel[1][3]  + denalidevice.getOil_Life();

		DisplayLabel[0][6]  = DisplayLabel[0][6]  + denalidevice.getP_Current_Distance_Travelled() + " MI";
		DisplayLabel[0][7]  = DisplayLabel[0][7]  + denalidevice.getP_Fuel_Used() + " MPG";
		DisplayLabel[0][8]  = DisplayLabel[0][8]  + "AVG ECONOMY " + denalidevice.getP_Average_Economy() ;
		DisplayLabel[0][9]  = DisplayLabel[0][9]  + "AVG MPH " + denalidevice.getP_Average_Speed();
		DisplayLabel[0][10] = DisplayLabel[0][10] + denalidevice.getP_Trip_To_Annual_Trip_Miles_Ratio() + "% ANNUAL";

		DisplayLabel[0][11] = DisplayLabel[0][11] + denalidevice.getB_Current_Distance_Travelled() + " MI";
		DisplayLabel[0][12] = DisplayLabel[0][12] + denalidevice.getB_Fuel_Used() + " MPG";
		DisplayLabel[0][13] = DisplayLabel[0][13] + "AVG ECONOMY " + denalidevice.getB_Average_Economy();
		DisplayLabel[0][14] = DisplayLabel[0][14] + "AVG MPH " + denalidevice.getB_Average_Speed();
		DisplayLabel[0][15] = DisplayLabel[0][15] + denalidevice.getB_Trip_To_Annual_Trip_Miles_Ratio() + "% ANNUAL";;

		GUIPersonalTripFlag = denalidevice.getPersonal_Trip_Flag();
		GUIBusinessTripFlag = denalidevice.getBusiness_Trip_Flag();

		if (GUIPersonalTripFlag)
			TripFlag[0] = "ON";
		else 
			TripFlag[0] = "OFF";
		if (GUIBusinessTripFlag)
			TripFlag[1] = "ON";
		else
			TripFlag[1] = "OFF";
	}

	public void paint (Graphics g) {

		if (buffer == null) {
            buffer = createImage(prefSize.width, prefSize.height);
            render();
        }
		g.drawImage (buffer, 0,0, this);
		
	}

	public void update (Graphics g) {
		paint (g);
	}

	public void render() {
        if (buffer == null)
            return;

        Graphics g = buffer.getGraphics();
		if ( DenaliImage != null ) {
	        g.drawImage (DenaliImage, 0, 0, this);
			g.setColor (new Color (198,251,255));
			g.setFont (DenaliDICFont);
			g.drawString ("DRIVER 1", 255, 217);
			if (buttonclickflag) {
				
				g.drawImage (DenaliImage, 0, 0, this);
				
				if ((buttonindex == 0 || buttonindex == 1) || (previousclick == 0 && currentclick == 3) || rootclick == 0) {
					//System.out.println ("root" + rootclick);

					if (denalidevice.tripflagchanged) {
						denalidevice.tripflagchanged = false;
						
						GUIPersonalTripFlag = denalidevice.getPersonal_Trip_Flag();

						if (GUIPersonalTripFlag) TripFlag[0] = "ON";
						else TripFlag[0] = "OFF";

						GUIBusinessTripFlag = denalidevice.getBusiness_Trip_Flag();
						if (GUIBusinessTripFlag) TripFlag[1] = "ON";
						else TripFlag[1] = "OFF";

						if (GUIPersonalTripFlag || GUIBusinessTripFlag)
							showtripinfo = true;
						else 
							showtripinfo = false;
					}
					
					if (currentclick == 3 && rootclick == 0) {
								//System.out.println("setwhattrip:" + setwhattrip);
								if (setwhattrip == 1) {
									if (GUIPersonalTripFlag) {

										GUIPersonalTripFlag = false;
										TripFlag[0] = "OFF";
										g.drawString (DisplayLabel[0][1] + ": " + TripFlag[0], 15, 217);
									}
									else {
										GUIPersonalTripFlag = true;
										TripFlag[0] = "ON";
										g.drawString (DisplayLabel[0][1] + ": " + TripFlag[0], 15, 217);
									}
								}

								if (setwhattrip == 2) {
									if (GUIBusinessTripFlag) {
										GUIBusinessTripFlag = false;
										TripFlag[1] = "OFF";
										g.drawString (DisplayLabel[0][2] + ": " + TripFlag[1], 15, 217);
									}
									else {
										GUIBusinessTripFlag = true;
										TripFlag[1] = "ON";
										g.drawString (DisplayLabel[0][2] + ": " + TripFlag[1], 15, 217);
									}
								}
								if (GUIPersonalTripFlag && GUIBusinessTripFlag)
											bothtripflag = true;
								else
											bothtripflag = false;

								if (GUIPersonalTripFlag || GUIBusinessTripFlag)
									showtripinfo = true;
								else 
									showtripinfo = false;
					}
					else {

						if (buttonindex == 0) {
							rootclick = buttonindex;
							if ( 5 < scrollcount ) {
								if (!showtripinfo || scrollcount == 11 || scrollcount == 16) {
						
										if (bothtripflag) {
											if (scrollcount == 11)
												scrollcount = 12;
											if (scrollcount == 16)
												scrollcount = 0;
										}
										else {
											scrollcount = 0; rotateflag = true; 
										}
								}
								else {
									if (rotateflag) {	
										if (GUIPersonalTripFlag) {
											scrollcount = 6; rotateflag = false;
										}
										else {
											if (GUIBusinessTripFlag) {
												scrollcount = 11; rotateflag = false;
											}
										}
										if (GUIPersonalTripFlag && GUIBusinessTripFlag)
											bothtripflag = true;
										else
											bothtripflag = false;
									}
								}
							}


							if (previousclick == 3) scrollcount = setwhattrip + 1;

							if (scrollcount == 1 || scrollcount == 2) {

								if (scrollcount == 2) 
									if (GUIPersonalTripFlag != denalidevice.getPersonal_Trip_Flag()) 
										denalidevice.setPersonal_Trip_Flag (GUIPersonalTripFlag);
									
								g.drawString (DisplayLabel[buttonindex][scrollcount] + ": " + TripFlag[scrollcount-1], 15, 217);
								setwhattrip = scrollcount ;
							}
							else {
								if (scrollcount == 3)
									if (GUIBusinessTripFlag != denalidevice.getBusiness_Trip_Flag())
										denalidevice.setBusiness_Trip_Flag (GUIBusinessTripFlag);

								g.drawString (DisplayLabel[buttonindex][scrollcount], 15, 217);								
							}
						}

						else if (buttonindex == 1) { /** for button 1 **/
							if ((DisplayLabel[buttonindex].length - 1) < scrollcount ) scrollcount = 0;
							g.drawString (DisplayLabel[buttonindex][scrollcount], 15, 217);
						}
					}
				}

				/**** for buttons 2 and 3  ***/
				if (buttonindex == 2 || buttonindex == 3) {
					if (buttonindex == 2) {
						rootclick = buttonindex;
						if ( previousclick == 3) { 
							scrollcount = submenu + 1;
							denalidevice.setPersonalization (submenu, personalize + 1); 
							personal = true;
						}
						if ((DisplayLabel[buttonindex].length - 1) < scrollcount) scrollcount = 0;
						g.drawString (DisplayLabel[buttonindex][scrollcount], 15, 217);
						submenu = scrollcount;
					}
					else { /** for button 3 **/
						if ((previousclick == 2 && currentclick == 3) || (previousclick == 3 && currentclick == 3)) {
							if (rootclick == 2) {
								//System.out.println("Submenu: " + submenu);
								//System.out.println("Scrollcount: " + scrollcount);
								
								if(personal) {
									currentset = denalidevice.getPersonalization (submenu) - 1;
									personal = false;
								}
								
								if ((DisplayLabelPersonalization[submenu].length - 1) < currentset) currentset = 0;
								g.drawString (DisplayLabelPersonalization[submenu][currentset], 15, 217);
								personalize = currentset;
								currentset++;
								//System.out.println("personalize" + personalize);
							}
						}
					}
				}
				buttonclickflag = false;
				
			}
			else { /*** Displaying current changed PUC stuffs in GUI **/
				if (denalidevice.displaypersonalflag) {
					denalidevice.displaypersonalflag = false;
					g.drawImage (DenaliImage, 0, 0, this);

					GUIPersonalTripFlag = denalidevice.getPersonal_Trip_Flag();
		
					if (GUIPersonalTripFlag) TripFlag[0] = "ON";
					else TripFlag[0] = "OFF";
								
					g.drawString (DisplayLabel[0][1] + ": " + TripFlag[0], 15, 217);
				}

				else if (denalidevice.displaybusinessflag) {
					denalidevice.displaybusinessflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					GUIBusinessTripFlag = denalidevice.getBusiness_Trip_Flag();			
					if (GUIBusinessTripFlag) TripFlag[1] = "ON";
					else TripFlag[1] = "OFF";
					
					g.drawString (DisplayLabel[0][2] + ": " + TripFlag[1], 15, 217);
				}

				else if(denalidevice.displayalarmflag) {
					denalidevice.displayalarmflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[0][denalidevice.alarm_warning_type - 1], 15, 217);
				}

				else if(denalidevice.displayautolockflag) {
					denalidevice.displayautolockflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[1][denalidevice.automatic_locking - 1], 15, 217);
				}

				else if(denalidevice.displayautounlockflag) {
					denalidevice.displayautounlockflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[2][denalidevice.automatic_unlocking - 1], 15, 217);
				}

				else if(denalidevice.displayseatpositionflag) {
					denalidevice.displayseatpositionflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[3][denalidevice.seat_position_recall - 1], 15, 217);
				}

				else if(denalidevice.displayperimeterlighting) {
					denalidevice.displayperimeterlighting= false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[4][denalidevice.perimeter_lighting - 1], 15, 217);
				}

				else if(denalidevice.displayremotelockflag) {
					denalidevice.displayremotelockflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[5][denalidevice.remote_lock_feedback - 1], 15, 217);
				}

				else if(denalidevice.displayremoteunlockflag) {
					denalidevice.displayremoteunlockflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[6][denalidevice.remote_unlock_feedback - 1], 15, 217);
				}

				else if(denalidevice.displayheadlampflag) {
					denalidevice.displayheadlampflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[7][denalidevice.head_lamps_on_at_exit - 1], 15, 217);
				}

				else if(denalidevice.displaycurbviewflag) {
					denalidevice.displaycurbviewflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[8][denalidevice.curbview_assist - 1], 15, 217);
				}

				else if(denalidevice.displayeasyexitflag) {
					denalidevice.displayeasyexitflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[9][denalidevice.easy_exit_seat - 1], 15, 217);
				}

				else if(denalidevice.displayunitsflag) {
					denalidevice.displayunitsflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[10][denalidevice.display_units - 1], 15, 217);
				}

				else if(denalidevice.displaylanguageflag) {
					denalidevice.displaylanguageflag = false;
					g.drawImage (DenaliImage, 0, 0, this);
					g.drawString (DisplayLabelPersonalization[11][denalidevice.display_language - 1], 15, 217);
				}
			}
		}
		else
			System.err.println( "Image is null" );
    }

	public void mouseReleased(java.awt.event.MouseEvent e) {
        Rectangle rect;
        Point pt = e.getPoint();
        for (int i = 0; i < buttonArea.length; i++) {
            if (buttonArea[i].contains(pt)) {
				
				buttonclickflag = true;
				buttonindex = i;
				
				currentclick = i;

				if (scrollcount < 0) {
					scrollcount++;
					previousclick = i;
				}
				else {
					if (currentclick == previousclick) {
						scrollcount++;
					}
					else {
						scrollcount = 0;
						//previousclick = i;
					}
				}				
            }			
        }
		if (!buttonclickflag) rootclick = -1;
		render();
		repaint();
		previousclick = currentclick;
    }

    public void mousePressed(java.awt.event.MouseEvent arg1) {
    }

    public void mouseExited(java.awt.event.MouseEvent arg1) {
    }

    public void mouseEntered(java.awt.event.MouseEvent arg1) {
    }

    public void mouseClicked(java.awt.event.MouseEvent arg1) {
    }

	/*public static void main( String[] args ) {

		Frame f = new Frame ("Denali DIC Simulator");
		DenaliGUI panel = new DenaliGUI();
		f.setLayout( new BorderLayout() );
		f.add( panel );
		f.setSize (765, 700);
		f.show();
	}*/
}