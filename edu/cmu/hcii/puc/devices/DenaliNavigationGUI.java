/**
 * This is the GMC Denali 2003 Navigation GUI class file that simulates the navigation unit and
 * communicates with the PUC protocol through the DenaliNavigationDevice class
 * @Rajesh Seenichamy
 * 
 * Revised by Pegeen Shen
 * Notes: Not guaranteed to be thread safe (possibly non-atomic operations)
 */

package edu.cmu.hcii.puc.devices;

import java.awt.*;
import java.awt.event.MouseListener;
import java.net.URL;

public class DenaliNavigationGUI extends Panel implements MouseListener {
    
    /* Images used by GUI */
    Image CautionImage = null;
    Image MenuImage = null;
    Image MapAppearanceImage = null;
    Image EditRouteImage = null;
    Image RouteOverviewImage = null;
    Image RoutePreviewImage = null;
    Image MapImage = null;
    Image ZoomInImage = null;
    Image NavigationKey = null;
    Image backImage, DestinationImage = null;
    Image AddressImage, AddressImage2 = null;
    Image RoutePreferenceImage = null;
    Image buffer = null;
    Image useHighlight, avoidHighlight = null;
    Image ma3dImage, maArrowImage, maHeadingImage, maMapImage = null;
    Image maNorthImage, maSingleImage, maSplitImage, maTurnImage = null;
    Image POIImage, POIImage2 = null;
    Image FreewayImage, FreewayImage2 = null;
    Image Intersection1Image, Intersection1Image2 = null;
    Image Intersection2Image, Intersection2Image2 = null;
    Image AddressImage3 = null;
    Image POIImage3 = null;
    Image FreewayImage3 = null;
    Image Intersection1Image3 = null;
    Image Intersection2Image3 = null;
    Image onHighlight, offHighlight = null;
    Image milesHighlight, kmHighlight = null;
    Image normalHighlight, autoHighlight1, autoHighlight2 = null;
    Image oneHighlight, twoHighlight, threeHighlight, fourHighlight = null;
    Image dayHighlight, nightHighlight;
    Image phoneImage = null;
    Image searchImages[] = new Image[8], searchButtonImages[] = new Image[8];
    Image okayImage;
    Image HouseAddressImage;
    Image caliberateImage;
    Image clockImage, TwelveHr, TwentyFourHr;
    Image PSTImage, MSTImage, CSTImage, ESTImage;
    Image configImage;
    
    Dimension prefSize = new Dimension(50, 50);
    
    Font addressfont;
    
    /**** Navigation System State variables *****/
    int CurrentScreen = 1;
    int CurrentCtrl = -1;
    int afterCautionScreen = 7;
    boolean cautionok = false;
    boolean inNavSys = true;
    
    /* Cursor states */
    int destCursor = 1;
    int menuCursor = 1;
    int adjustClockCursor = 1; //0 and -1 are the hr and minute adjusting
    int adjustClockCursor2 = -1;
    int mapZoom;
    int prevDestCursor = 1;
    int emergencyCursor = 1;
    int navSetupCursor = 1;
    int editRouteCursor = 1;
    int routePreferenceCursor = 1;
    int mapAppearCursor = 1;
    ///note: do not need cursor for voice volume
    int memoryPoints = 1;
    int searchArea = 0;
    int searchCursor = 0;
    
    int buttonno = -1;
    
    int key = 0;
    
    String address = "";
    
    /**** State variables (Device) *****/
    int hr = 22, min = 15, timeZone = 4;
    boolean is12Hr = false, GPSUpdate = false, daylightSavings = false;
    
    public boolean buttonclickflag = false;
    /**************************/
    
    Rectangle buttonArea[] = {
        new Rectangle(55, 275, 21, 17), // Top Button 
        new Rectangle(39, 289, 19, 22), // Left Button
        new Rectangle(58, 292, 16, 19), // Select Button
        new Rectangle(71, 291, 19, 20), // Right Button 
        new Rectangle(54, 309, 22, 14), // Bottom Button
        new Rectangle(578, 253, 33, 30), // Back Button
        new Rectangle(574, 72, 36, 32), // Map Button
        new Rectangle(577, 179, 36, 32), // Zoom In Button
        new Rectangle(577, 215, 36, 32), // Zoom Out Button
        new Rectangle(576, 143, 36, 33), // Menu Button
        new Rectangle(63, 94, 46, 31), // Nav Key Button
        new Rectangle(576, 109, 35, 30), // Dest Button
        
        new Rectangle(62, 130, 46, 27), // Band Key Button
        new Rectangle(63, 163, 43, 27), // CD Key Button
        new Rectangle(62, 197, 47, 29), // Music Key Button
        new Rectangle(62, 232, 45, 29), // Config Key Button
    };
    
    Keypad keypad;
    
    final int UP = 0;
    final int LEFT = 1;
    final int SELECT = 2;
    final int RIGHT = 3;
    final int DOWN = 4;
    final int BACK = 5;
    final int MAP = 6;
    final int ZOOMIN = 7;
    final int ZOOMOUT = 8;
    final int MENU = 9;
    final int NAV = 10;
    final int DEST = 11;
    final int BAND = 12;
    final int CD = 13;
    final int MUSIC = 14;
    final int CONFIG = 15;
    
    public static final int CAUTION_SCREEN = 1;
    public static final int MENU_SCREEN = 2;
    public static final int MAP_APPEARANCE_SCREEN = 3;
    public static final int EDIT_ROUTE_SCREEN = 4;
    public static final int ROUTE_OVERVIEW_SCREEN = 5;
    public static final int ROUTE_PREVIEW_SCREEN = 6;
    public static final int MAP_SCREEN = 7;
    public static final int DESTINATION_SCREEN = 12;
    public static final int NAV_SETUP_SCREEN = 16;
    public static final int SEARCH_AREA_SCREEN = 19;
    public static final int HOUSE_NO_SCREEN = 20;
    public static final int CALIBERATE_SYSTEM_SCREEN = 22;
    public static final int CLOCK_SCREEN = 23;
    
    DenaliNavigationDevice denalinavigationdevice = null;
    DestinationMenu dm, editRouteDM, screen12menu;
    NavSetupMenu nsm = null;
    
    public DenaliNavigationGUI(DenaliNavigationDevice dev) {
        
        denalinavigationdevice = dev;
        addressfont = new Font("SansSerif", Font.BOLD, 17);
        this.addMouseListener(this);
        init();
        
    }
    
    public void init() {
        URL url;
        Toolkit tk = Toolkit.getDefaultToolkit();
        int j  = 0;
        
        try {
            MediaTracker mt = new MediaTracker(this);
            
            url = getClass().getResource("Navigation2.jpg");
            CautionImage = tk.getImage(url);
            mt.addImage(CautionImage, j++);
            
            url = getClass().getResource("Navigation3.jpg");
            MenuImage = tk.getImage(url);
            mt.addImage(MenuImage, j++);
            
            url = getClass().getResource("Navigation4.jpg");
            MapAppearanceImage = tk.getImage(url);
            mt.addImage(MapAppearanceImage, j++);
            
            url = getClass().getResource("Navigation5.jpg");
            EditRouteImage = tk.getImage(url);
            mt.addImage(EditRouteImage, j++);
            
            url = getClass().getResource("Navigation6.jpg");
            RouteOverviewImage = tk.getImage(url);
            mt.addImage(RouteOverviewImage, j++);
            
            url = getClass().getResource("Navigation7.jpg");
            RoutePreviewImage = tk.getImage(url);
            mt.addImage(RoutePreviewImage, j++);
            
            url = getClass().getResource("mapwindow.jpg");
            MapImage = tk.getImage(url);
            mt.addImage(MapImage, j++);
            
            url = getClass().getResource("zoomin.jpg");
            ZoomInImage = tk.getImage(url);
            mt.addImage(ZoomInImage, j++);
            
            url = getClass().getResource("NavigationKey.jpg");
            NavigationKey = tk.getImage(url);
            mt.addImage(NavigationKey, j++);
            
            url = getClass().getResource("Destination.jpg");
            DestinationImage = tk.getImage(url);
            mt.addImage(DestinationImage, j++);
            
            url = getClass().getResource("Address.jpg");
            AddressImage = tk.getImage(url);
            mt.addImage(AddressImage, j++);
            
            url = getClass().getResource("HouseAddress.jpg");
            HouseAddressImage = tk.getImage(url);
            mt.addImage(HouseAddressImage, j++);

            url = getClass().getResource("RoutePreference.jpg");
            RoutePreferenceImage = tk.getImage(url);
            mt.addImage(RoutePreferenceImage, j++);
            
            Image[] destinationmenus = new Image[16];
            Image[] destinationscrolls = new Image[16];
            Image[] destinationsearches = new Image[8];
            for (int i = 0; i < 16; i++) {
                url = getClass().getResource("destination/menu"+(i+1)+".jpg");
                destinationmenus[i] = tk.getImage(url);
                mt.addImage(destinationmenus[i], j++);
            }
            for (int i = 0; i < 16; i++) {
                url = getClass().getResource("destination/scroll"+(i+1)+".jpg");
                destinationscrolls[i] = tk.getImage(url);
                mt.addImage(destinationscrolls[i], j++);
            }
            for (int i = 0; i < 8; i++) {
                url = getClass().getResource("destination/search"+(i+1)+".jpg");
                destinationsearches[i] = tk.getImage(url);
                mt.addImage(destinationsearches[i], j++);
            }
            for (int i = 0; i < 8; i++) {
                url = getClass().getResource("search/menu"+(i+1)+".jpg");
                searchImages[i] = tk.getImage(url);
                mt.addImage(searchImages[i], j++);
            }
            for (int i = 0; i < 8; i++) {
                url = getClass().getResource("search/"+(i+1)+".jpg");
                searchButtonImages[i] = tk.getImage(url);
                mt.addImage(searchButtonImages[i], j++);
            }

            url = getClass().getResource("AddressNumber.jpg");
            AddressImage2 = tk.getImage(url);
            mt.addImage(AddressImage2, j++);
            
            url = getClass().getResource("OK.jpg");
            okayImage = tk.getImage(url);
            mt.addImage(okayImage, j++);
            
            url = getClass().getResource("use.jpg");
            useHighlight = tk.getImage(url);
            mt.addImage(useHighlight, j++);

            url = getClass().getResource("avoid.jpg");
            avoidHighlight = tk.getImage(url);
            mt.addImage(avoidHighlight, j++);

            url = getClass().getResource("appearance/3d.jpg");
            ma3dImage = tk.getImage(url);
            mt.addImage(ma3dImage, j++);

            url = getClass().getResource("appearance/arrow.jpg");
            maArrowImage = tk.getImage(url);
            mt.addImage(maArrowImage, j++);

            url = getClass().getResource("appearance/heading.jpg");
            maHeadingImage = tk.getImage(url);
            mt.addImage(maHeadingImage, j++);

            url = getClass().getResource("appearance/map.jpg");
            maMapImage = tk.getImage(url);
            mt.addImage(maMapImage, j++);

            url = getClass().getResource("appearance/north.jpg");
            maNorthImage = tk.getImage(url);
            mt.addImage(maNorthImage, j++);

            url = getClass().getResource("appearance/single.jpg");
            maSingleImage = tk.getImage(url);
            mt.addImage(maSingleImage, j++);

            url = getClass().getResource("appearance/split.jpg");
            maSplitImage = tk.getImage(url);
            mt.addImage(maSplitImage, j++);

            url = getClass().getResource("appearance/turn.jpg");
            maTurnImage = tk.getImage(url);
            mt.addImage(maTurnImage, j++);

            url = getClass().getResource("POI.jpg");
            POIImage = tk.getImage(url);
            mt.addImage(POIImage, j++);

            url = getClass().getResource("POINumber.jpg");
            POIImage2 = tk.getImage(url);
            mt.addImage(POIImage2, j++);

            url = getClass().getResource("Freeway.jpg");
            FreewayImage = tk.getImage(url);
            mt.addImage(FreewayImage, j++);

            url = getClass().getResource("FreewayNumber.jpg");
            FreewayImage2 = tk.getImage(url);
            mt.addImage(FreewayImage2, j++);

            url = getClass().getResource("Intersection1.jpg");
            Intersection1Image = tk.getImage(url);
            mt.addImage(Intersection1Image, j++);

            url = getClass().getResource("Intersection1Number.jpg");
            Intersection1Image2 = tk.getImage(url);
            mt.addImage(Intersection1Image2, j++);

            url = getClass().getResource("Intersection2.jpg");
            Intersection2Image = tk.getImage(url);
            mt.addImage(Intersection2Image, j++);

            url = getClass().getResource("Intersection2Number.jpg");
            Intersection2Image2 = tk.getImage(url);
            mt.addImage(Intersection2Image2, j++);

            url = getClass().getResource("AddressExtend.jpg");
            AddressImage3 = tk.getImage(url);
            mt.addImage(AddressImage3, j++);
            
            url = getClass().getResource("POIExtend.jpg");
            POIImage3 = tk.getImage(url);
            mt.addImage(POIImage3, j++);

            url = getClass().getResource("FreewayExtend.jpg");
            FreewayImage3 = tk.getImage(url);
            mt.addImage(FreewayImage3, j++);

            url = getClass().getResource("Intersection1Extend.jpg");
            Intersection1Image3 = tk.getImage(url);
            mt.addImage(Intersection1Image3, j++);

            url = getClass().getResource("Intersection2Extend.jpg");
            Intersection2Image3 = tk.getImage(url);
            mt.addImage(Intersection2Image3, j++);

            Image[] navmenus = new Image[11];
            Image[] navscrolls = new Image[13];
            for (int i = 0; i < 11; i++) {
                url = getClass().getResource("nav/menu"+(i+1)+".jpg");
                navmenus[i] = tk.getImage(url);
                mt.addImage(navmenus[i], j++);
            }
            for (int i = 0; i < 13; i++) {
                url = getClass().getResource("nav/scroll"+(i+1)+".jpg");
                navscrolls[i] = tk.getImage(url);
                mt.addImage(navscrolls[i], j++);
            }

            url = getClass().getResource("on.jpg");
            onHighlight = tk.getImage(url);
            mt.addImage(onHighlight, j++);

            url = getClass().getResource("off.jpg");
            offHighlight = tk.getImage(url);
            mt.addImage(offHighlight, j++);

            url = getClass().getResource("miles.jpg");
            milesHighlight = tk.getImage(url);
            mt.addImage(milesHighlight, j++);

            url = getClass().getResource("km.jpg");
            kmHighlight = tk.getImage(url);
            mt.addImage(kmHighlight, j++);

            url = getClass().getResource("normal.jpg");
            normalHighlight = tk.getImage(url);
            mt.addImage(normalHighlight, j++);

            url = getClass().getResource("auto1.jpg");
            autoHighlight1 = tk.getImage(url);
            mt.addImage(autoHighlight1, j++);

            url = getClass().getResource("auto2.jpg");
            autoHighlight2 = tk.getImage(url);
            mt.addImage(autoHighlight2, j++);

            url = getClass().getResource("1.jpg");
            oneHighlight = tk.getImage(url);
            mt.addImage(oneHighlight, j++);

            url = getClass().getResource("2.jpg");
            twoHighlight = tk.getImage(url);
            mt.addImage(twoHighlight, j++);

            url = getClass().getResource("3.jpg");
            threeHighlight = tk.getImage(url);
            mt.addImage(threeHighlight, j++);

            url = getClass().getResource("4.jpg");
            fourHighlight = tk.getImage(url);
            mt.addImage(fourHighlight, j++);

            url = getClass().getResource("phone.jpg");
            phoneImage = tk.getImage(url);
            mt.addImage(phoneImage, j++);

            url = getClass().getResource("destination/back.jpg");
            backImage = tk.getImage(url);
            mt.addImage(backImage, j++);

            url = getClass().getResource("CaliberateMenu.jpg");
            caliberateImage = tk.getImage(url);
            mt.addImage(caliberateImage, j++);

            url = getClass().getResource("Clock.jpg");
            clockImage = tk.getImage(url);
            mt.addImage(clockImage, j++);

            url = getClass().getResource("Config.jpg");
            configImage = tk.getImage(url);
            mt.addImage(configImage, j++);
            
            url = getClass().getResource("12Hr.jpg");
            TwelveHr = tk.getImage(url);
            mt.addImage(TwelveHr, j++);
            
            url = getClass().getResource("24Hr.jpg");
            TwentyFourHr = tk.getImage(url);
            mt.addImage(TwentyFourHr, j++);
            
            url = getClass().getResource("PST.jpg");
            PSTImage = tk.getImage(url);
            mt.addImage(PSTImage, j++);
            
            url = getClass().getResource("MST.jpg");
            MSTImage = tk.getImage(url);
            mt.addImage(MSTImage, j++);
            
            url = getClass().getResource("CST.jpg");
            CSTImage = tk.getImage(url);
            mt.addImage(CSTImage, j++);
            
            url = getClass().getResource("EST.jpg");
            ESTImage = tk.getImage(url);
            mt.addImage(ESTImage, j++);
            
            url = getClass().getResource("day.jpg");
            dayHighlight = tk.getImage(url);
            mt.addImage(dayHighlight, j++);

            url = getClass().getResource("night.jpg");
            nightHighlight = tk.getImage(url);
            mt.addImage(nightHighlight, j++);

            mt.waitForAll();
            dm = new DestinationMenu(destinationscrolls, 
                                     destinationmenus, 
                                     destinationsearches, 0, 0, 3, 2);
            editRouteDM = new DestinationMenu(destinationscrolls, 
                                              destinationmenus, 
                                              destinationsearches, 0, 0, 3, 2,
                                              backImage);
            nsm = new NavSetupMenu(navmenus, navscrolls,
                                   onHighlight, offHighlight,
                                   milesHighlight, kmHighlight,
                                   normalHighlight, autoHighlight1,
                                   oneHighlight, twoHighlight,
                                   threeHighlight, fourHighlight,
                                   denalinavigationdevice);
        }
        catch (Exception e) { }
        
        prefSize = new Dimension(CautionImage.getWidth(this),
                                 CautionImage.getHeight(this));

        requestFocus();
        render();
        repaint();
    }
    
    public void paint(Graphics g) {
        
        if (buffer == null) {
            buffer = createImage(prefSize.width, prefSize.height);
            render();
        }
        
        g.drawImage(buffer, 0, 0, this);
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    public void render() {
        
        if (buffer == null)
            return;
        
        Graphics g = buffer.getGraphics();
        
        Graphics2D g2d = (Graphics2D)g;
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke( new BasicStroke( 3 ) );
        
        if (CurrentScreen == 1) {
            g.drawImage(CautionImage, 0, 0, this);
        }
        
        if (buttonclickflag) {
            switch (CurrentScreen) {
                case 1: // Caution Screen
                    screen1(g);
                    break;
                case 2: // Menu Screen
                    screen2(g);
                    break;
                case 3: // Map Appearance Screen
                    screen3(g);
                    break;
                case 4: // Edit Route Screen
                    screen4(g);
                    break;
                case 5: // Route Overview Screen
                    screen5(g);
                    break;
                case 6: // Route Preview Screen
                    screen6(g);
                    break;
                case 7: //Map Screen
                    screen7(g);
                    break;
                case 8: // Zoom In Screen
                    screen8(g);
                    break;
                case 9: // Zoom Out Screen
                    screen9(g);
                    break;
                case 10: // Menu Button
                    screen10(g);
                    break;
                case 11: // Nav Key Button
                    screen11(g);
                    break;
                case 12: // Destination Button
                    screen12(g);
                    break;
                case 13: // Address screen
                    screen13(g);
                    break;
                case 14: // keypad screen
                    keypad_screen14(g);
                    break;
                case 15: // Route Preference screen
                    screen15(g);
                    break;
                case 16: // Nav Setup screen
                    screen16(g);
                    break;
                case 17: // Phone No screen
                    screen17(g);
                    break;
                case 19:
                    screen19(g); // Search Area screen
                    break;
                case 20:
                    screen20(g); // House No screen
                    break;
                case 21:
                    screen21(g); // Intersection screens
                    break;
                case 22:
                    screen22(g); // Caiberate System screen
                    break;
                case 23:
                    screen23(g); // Adjust Clock Screen
                    break;
                case 24:
                    screen24(g); // Configure Screen
                    break;
            }
        }
        g2d.setStroke( oldStroke );
    }
    
    public void DrawMenu(Graphics g) {
        switch (menuCursor) {
            case 1:
                DrawMenuRectangle(g, 148, 115, 48, 37); // 1
                break;
            case 2:
                DrawMenuRectangle(g, 145, 159, 48, 37); //2
                break;
            case 3:
                DrawMenuRectangle(g, 145, 204, 48, 37); // 3
                break;
            case 4:
                DrawMenuRectangle(g, 145, 250, 48, 37); //4
                break;
            case 5:
                DrawMenuRectangle(g, 353, 77, 50, 25); //5
                break;
            case 6:
                DrawMenuRectangle(g, 355, 115, 48, 37); //6
                break;
            case 7:
                DrawMenuRectangle(g, 355, 159, 48, 37); // 7
                break;
            case 8:
                DrawMenuRectangle(g, 355, 204, 48, 37); //8
                break;
            case 9:
                DrawMenuRectangle(g, 355, 250, 48, 37); //9
                break;
        }
    }

    public void DrawAdjustClock(Graphics g) {
        switch (adjustClockCursor) {
            case -1:
                DrawAdjustClockRectangle(g, 245, 115, 43, 30);
                break;
            case 0:
                DrawAdjustClockRectangle(g, 303, 115, 43, 30);
                break;
            case 1:
                DrawAdjustClockRectangle(g, 205, 110, 182, 39);
                break;
            case 2:
                DrawAdjustClockRectangle(g, 134, 154, 326, 33);
                break;
            case 3:
                DrawAdjustClockRectangle(g, 134, 186, 326, 33);
                break;
            case 4:
                DrawAdjustClockRectangle(g, 134, 218, 326, 33);
                break;
            case 5:
                DrawAdjustClockRectangle(g, 134, 251, 326, 33);
                break;
        }
    }
    
    public void DrawAdjustClockRectangle(Graphics g, int x, int y, int width, int height) {
        g.drawImage(clockImage, 0, 0, this);
        g.setColor(new Color(0, 255, 0));
        int displayHr = hr;
        if (is12Hr) {
            g.drawImage(TwelveHr, 344, 156, this);
            displayHr = hr % 12;
            if (displayHr == 0)
                displayHr = 12;
        }
        else {
            g.drawImage(TwentyFourHr, 395, 156, this);
        }
        if (GPSUpdate)
            g.drawImage(onHighlight, 344, 189, this);
        else
            g.drawImage(offHighlight, 396, 189, this);
        if (daylightSavings)
            g.drawImage(onHighlight, 344, 222, this);
        else
            g.drawImage(offHighlight, 396, 222, this);
        switch (timeZone) {
            case 1:
                g.drawImage(PSTImage, 238, 257, this);
                break;
            case 2:
                g.drawImage(MSTImage, 291, 257, this);
                break;
            case 3:
                g.drawImage(CSTImage, 344, 257, this);
                break;
            case 4:
                g.drawImage(ESTImage, 396, 257, this);
                break;
        }
        g.setFont(new Font("SansSerif", Font.BOLD, 25));
        g.setColor(new Color(83, 195, 183));
        if (displayHr < 10)
            g.drawString(Integer.toString(displayHr), 267, 140);
        else
            g.drawString(Integer.toString(displayHr), 253, 140);
        if (min < 10)
            g.drawString("0"+Integer.toString(min), 310, 140);
        else
            g.drawString(Integer.toString(min), 310, 140);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        if (is12Hr)
            if (hr > 11)
                g.drawString("PM", 350, 140);
            else
                g.drawString("AM", 350, 140);
        g.setColor(new Color(247, 204, 139));
        g.drawRect(x, y, width, height);
    }

    public void DrawSetup(Graphics g) {
        switch (navSetupCursor) {
            case 1:
                DrawSetupRectangle(g, 145, 119, 325, 34); // 1
                break;
            case 2:
                DrawSetupRectangle(g, 145, 151, 325, 34); // 2
                break;
            case 3:
                DrawSetupRectangle(g, 145, 185, 325, 34); // 3
                break;
            case 4:
                DrawSetupRectangle(g, 145, 219, 325, 34); // 4
                break;
        }
    }

    public void DrawSetupRectangle(Graphics g, int x, int y, int width, int height) {
        g.drawImage(configImage, 0, 0, this);
        int screenColor = denalinavigationdevice.GetScreenColor();
        boolean beep = denalinavigationdevice.GetBeep();
        if (screenColor == 0)
            g.drawImage(autoHighlight2, 290, 191, this);
        else if (screenColor == 1)
            g.drawImage(dayHighlight, 339, 191, this);
        else g.drawImage(nightHighlight, 383, 189, this);
        if (beep)
            g.drawImage(onHighlight, 309, 223, 55, 26, this);
        else
            g.drawImage(offHighlight, 365, 223, 55, 26, this);
        g.setColor(new Color(247, 204, 139));
        g.drawRect(x, y, width, height);
    }

    public void DrawMenuRectangle(Graphics g, int x, int y, int width, int height) {
        g.drawImage(MenuImage, 0, 0, this);
        g.setColor(new Color(247, 204, 139));
        g.drawRect(x, y, width, height);
    }

    public void DrawMapAppearance(Graphics g) {
        switch (mapAppearCursor) {
            case 1:
                DrawMapAppearanceRectangle(g, 151, 149, 49, 34);
                break;
            case 2:
                DrawMapAppearanceRectangle(g, 152, 194, 49, 34);
                break;
            case 3:
                DrawMapAppearanceRectangle(g, 148, 242, 49, 34);
                break;
            case 4:
                DrawMapAppearanceRectangle(g, 298, 147, 49, 34);
                break;
            case 5:
                DrawMapAppearanceRectangle(g, 297, 194, 49, 34);
                break;
            case 6:
                DrawMapAppearanceRectangle(g, 298, 241, 49, 34);
                break;
            case 7:
                DrawMapAppearanceRectangle(g, 435, 145, 49, 34);
                break;
            case 8:
                DrawMapAppearanceRectangle(g, 436, 192, 49, 34);
                break;
        }
    }
    
    public void DrawMapAppearanceRectangle(Graphics g, int x, int y, int width, int height) {
        g.drawImage(MapAppearanceImage, 0, 0, this);
        g.setColor(new Color(0, 255, 0));
        switch (denalinavigationdevice.GetMapOrientation()) {
            case 1:
                g.drawImage(maNorthImage, 151, 149, this);
                break;
            case 2:
                g.drawImage(maHeadingImage, 152, 194, this);
                break;
            case 3:
                g.drawImage(ma3dImage, 148, 242, this);
                break;
        }
        switch (denalinavigationdevice.GetGuidanceMode()) {
            case 1:
                g.drawImage(maArrowImage, 298, 147, this);
                break;
            case 2:
                g.drawImage(maTurnImage, 297, 194, this);
                break;
            case 3:
                g.drawImage(maMapImage, 298, 241, this);
                break;
        }
        switch (denalinavigationdevice.GetMapMode()) {
            case 1:
                g.drawImage(maSingleImage, 435, 145, this);
                break;
            case 2:
                g.drawImage(maSplitImage, 436, 192, this);
                break;
        }
        g.setColor(new Color(247, 204, 139));
        g.drawRect(x, y, width, height);
        
    }
    
    public void DrawRoutePreferenceRectangle(Graphics g, int x, int y, int width, int height) {
        g.drawImage(RoutePreferenceImage, 0, 0, this);
        if (denalinavigationdevice.GetFreeway() == true){
            g.drawImage(useHighlight, 363, 129, this);
        }
        else{
            g.drawImage(avoidHighlight, 435, 129, this);
        }
        if (denalinavigationdevice.GetTollRoad() == true) {
            g.drawImage(useHighlight, 364, 167, this);
        }
        else{
            g.drawImage(avoidHighlight, 436, 167, this);
        }
        if (denalinavigationdevice.GetFerry() == true)	{
            g.drawImage(useHighlight, 365, 207, this);
        }
        else{
            g.drawImage(avoidHighlight, 437, 207, this);
        }
        if (denalinavigationdevice.GetRestrictedRoad() == true) {
            g.drawImage(useHighlight, 366, 247, this);
        }
        else{
            g.drawImage(avoidHighlight, 438, 247, this);
        }
        g.setColor(new Color(247, 204, 139));
        g.drawRect(x, y, width, height);
    }
    
    public void DrawEditRouteRectangle(Graphics g, int x, int y, int width, int height) {
        g.drawImage(EditRouteImage, 0, 0, this);
        g.setColor(new Color(247, 204, 139));
        g.drawRect(x, y, width, height);
    }
    
    public void DrawEditRoute(Graphics g) {
        switch (editRouteCursor) {
            case 1:
                DrawEditRouteRectangle(g, 143, 143, 26, 23);
                break;
            case 2:
                DrawEditRouteRectangle(g, 143, 212, 26, 23);
                break;
            case 3:
                DrawEditRouteRectangle(g, 284, 143, 26, 23);
                break;
            case 4:
                DrawEditRouteRectangle(g, 284, 174, 26, 23);
                break;
            case 5:
                DrawEditRouteRectangle(g, 284, 212, 26, 23);
                break;
            case 6:
                DrawEditRouteRectangle(g, 419, 143, 26, 23);
                break;
            case 7:
                DrawEditRouteRectangle(g, 419, 174, 26, 23);
                break;
            case 8:
                DrawEditRouteRectangle(g, 419, 212, 26, 23);
                break;
            case 9:
                DrawEditRouteRectangle(g, 419, 256, 54, 23);
                break;
        }
}
    
    public void DrawRoutePreference(Graphics g) {
        switch (routePreferenceCursor) {
            case 1:
                DrawRoutePreferenceRectangle(g, 166, 118, 356, 42);
                break;
            case 2:
                DrawRoutePreferenceRectangle(g, 166, 157, 356, 42);
                break;
            case 3:
                DrawRoutePreferenceRectangle(g, 166, 197, 356, 42);
                break;
            case 4:
                DrawRoutePreferenceRectangle(g, 166, 238, 356, 42);
                break;
        }
    }
    
    public void screen24(Graphics g) {
        int screenColor = denalinavigationdevice.GetScreenColor();
        boolean beep = denalinavigationdevice.GetBeep();
        switch (buttonno) {
            case MAP:
                CurrentScreen = 1;
                screen1(g);
                return;
            case DEST:
                CurrentScreen = 23;
                CurrentCtrl = 24;
                screen23(g);
                return;
            case DOWN:
                if (navSetupCursor < 4)
                    navSetupCursor++;
                break;
            case UP:
                if (navSetupCursor > 1)
                    navSetupCursor--;
                break;
            case LEFT:
                if (navSetupCursor == 3 && screenColor > 0)
                    denalinavigationdevice.SetScreenColor(--screenColor);
                else if (navSetupCursor == 4 && !beep) {
                    beep = true;
                    denalinavigationdevice.SetBeep(1);
                }
                break;
            case RIGHT:
                if (navSetupCursor == 3 && screenColor < 4)
                    denalinavigationdevice.SetScreenColor(++screenColor);
                else if (navSetupCursor == 4 && beep) {
                    beep = false;
                    denalinavigationdevice.SetBeep(2);
                }
                break;
        }
        DrawSetup(g);
    }
    
    public void screen23(Graphics g) {
        if (buttonno == BACK) {
            CurrentScreen = 24;
            screen24(g);
            return;
        }
        
        if (CurrentCtrl == 24) {
            hr = denalinavigationdevice.GetTimeHr();
            min = denalinavigationdevice.GetTimeMin();
            GPSUpdate = denalinavigationdevice.GetGPSUpdate();
            is12Hr = denalinavigationdevice.GetHrMode();
            daylightSavings = denalinavigationdevice.GetDaylightSavings();
            timeZone = denalinavigationdevice.GetTimeZone();
        }
        
        g.drawImage(clockImage, 0, 0, this);
        if (buttonno == ZOOMOUT) {
            denalinavigationdevice.SetTimeHr(hr);
            denalinavigationdevice.SetTimeMin(min);
            denalinavigationdevice.SetTimeZone(timeZone);
            if (GPSUpdate)
                denalinavigationdevice.SetGPSUpdate(1);
            else denalinavigationdevice.SetGPSUpdate(2);
            if (daylightSavings)
                denalinavigationdevice.SetDaylightSavings(1);
            else denalinavigationdevice.SetDaylightSavings(2);
            if (is12Hr)
                denalinavigationdevice.SetHrMode(1);
            else denalinavigationdevice.SetHrMode(2);
            
            CurrentScreen = 24;
            screen24(g);
            return;
        }
            
        if (buttonno == UP && adjustClockCursor > 1)
            adjustClockCursor--;
        else if (buttonno == DOWN && 
                 adjustClockCursor < 5 && adjustClockCursor > 0)
            adjustClockCursor++;
        else
        switch (adjustClockCursor) {
            case -1:
                if (buttonno == RIGHT)
                    adjustClockCursor = adjustClockCursor2 = 0;
                else if (buttonno == UP) {
                    hr++;
                    hr = hr % 24;
                    GPSUpdate = false;
                }
                else if (buttonno == DOWN) {
                    hr--;
                    hr = (hr + 24) % 24;
                    GPSUpdate = false;
                }
                else if (buttonno == SELECT) {
                    adjustClockCursor = 1;
                }
                break;
            case 0:
                if (buttonno == LEFT)
                    adjustClockCursor = adjustClockCursor2 = -1;
                else if (buttonno == UP) {
                    min++;
                    min = min % 60;
                    GPSUpdate = false;
                }
                else if (buttonno == DOWN) {
                    min--;
                    min = (min + 60) % 60;
                    GPSUpdate = false;
                }
                else if (buttonno == SELECT) {
                    adjustClockCursor = 1;
                }
                break;
            case 1:
                if (buttonno == SELECT)
                    adjustClockCursor = adjustClockCursor2;
                break;
            case 2:
                if (buttonno == LEFT && !is12Hr)
                    is12Hr = true;
                else if (buttonno == RIGHT && is12Hr)
                    is12Hr = false;
                break;
            case 3:
                if (buttonno == LEFT && !GPSUpdate)
                    GPSUpdate = true;
                else if (buttonno == RIGHT && GPSUpdate)
                    GPSUpdate = false;
                break;
            case 4:
                if (buttonno == LEFT && !daylightSavings) {
                    daylightSavings = true;
                    hr++;
                    hr = hr % 24;                  
                }
                else if (buttonno == RIGHT && daylightSavings) {
                    daylightSavings = false;
                    hr--;
                    hr = (hr + 24) % 24;
                }
                break;          
            case 5:
                if (buttonno == LEFT && timeZone > 1) {
                    timeZone--;
                    hr--;
                    hr = (hr + 24) % 24;
                }
                else if (buttonno == RIGHT && timeZone < 4) {
                    timeZone++;
                    hr++;
                    hr = hr % 24;
                }
                break;
        }
        CurrentCtrl = 23;
        DrawAdjustClock(g);
    }
    
    public void screen22(Graphics g) {
        if (buttonno == BACK) {
            CurrentScreen = 16;
            nsm.draw(g, this);
            return;
        }
        g.setColor(new Color(247, 204, 139));
        g.drawImage(caliberateImage, 0, 0, this);
        if (buttonno == SELECT) {
            CurrentScreen = 7;
            screen7(g);
            return;
        }
        if (CurrentCtrl == 1)
            if (buttonno == DOWN)
                g.drawRect(249, 240, 30, 30);
            else g.drawRect(250, 144, 30, 30);
        else if (buttonno == UP)
            g.drawRect(250,  144, 30, 30);
        else g.drawRect(249, 240, 30, 30);
    }
    
    public void screen21(Graphics g) {
        if (buttonno == BACK) {
            keypad = ((IntersectEnglishKeypad)keypad).getBackKeypad();
            if (keypad == null) {
                CurrentScreen = 12;
                screen12menu.draw(g, this);
            }
            else
                keypad.draw(g, this);
            return;
        }
        Keypad tempkp = keypad.doAction(buttonno);
        if (tempkp == null) {
            if (((IntersectEnglishKeypad)keypad).getBackKeypad() != null) {
                CurrentScreen = 7;
                screen7(g);
                return;
            }
            String s = keypad.getInput();
            keypad = new IntersectEnglishKeypad(Intersection2Image,
                                                Intersection2Image2, 
                                                Intersection2Image3, 
                                                okayImage,
                                                "Input 2nd street name", 
                                                "", s, 262, 156, keypad);
        }
        keypad.draw(g, this);
    }
    
    public void screen20(Graphics g) {
        if (buttonno == BACK) {
            CurrentScreen = 14;
            keypad = ((HouseNoKeypad)keypad).getBackKeypad();
            keypad.draw(g, this);
            return;
        }
        Keypad tempkp = keypad.doAction(buttonno);
        if (tempkp != null)
            keypad.draw(g, this);
        else {
            CurrentScreen = 7;
            screen7(g);
        }
    }

    public void screen19(Graphics g) {
        if (buttonno == SELECT) {
            searchArea = searchCursor;
            dm.setSearchArea(searchArea);
            editRouteDM.setSearchArea(searchArea);
        }
        if (buttonno == BACK || buttonno == SELECT) {
            CurrentScreen = 12;
            screen12menu.moveLeft();
            screen12menu.draw(g, this);
            return;
        }
        if (buttonno == -1) {
            searchCursor = searchArea;
            g.drawImage(searchImages[searchArea], 0, 0, this);
            switch (searchArea) {
                case 0:
                    g.drawImage(searchButtonImages[searchArea],
                                144, 120, this);
                    break;
                case 1:
                    g.drawImage(searchButtonImages[searchArea],
                                196, 120, this);
                    break;
                case 2:
                    g.drawImage(searchButtonImages[searchArea],
                                143, 161, this);
                    break;
                case 3:
                    g.drawImage(searchButtonImages[searchArea],
                                196, 161, this);
                    break;
                case 4:
                    g.drawImage(searchButtonImages[searchArea],
                                142, 202, this);
                    break;
                case 5:
                    g.drawImage(searchButtonImages[searchArea],
                                196, 202, this);
                    break;
                case 6:
                    g.drawImage(searchButtonImages[searchArea],
                                141, 245, this);
                    break;
                case 7:
                    g.drawImage(searchButtonImages[searchArea],
                                196, 245, this);
                    break;
            }
            return;
        }
        switch (buttonno) {
            case UP:
                if (searchCursor > 1)
                    searchCursor -= 2;
                break;
            case DOWN:
                if (searchCursor < 6)
                    searchCursor += 2;
                break;
            case LEFT:
                if (searchCursor % 2 == 1)
                    searchCursor--;
                break;
            case RIGHT:
                if (searchCursor % 2 == 0)
                    searchCursor++;
                break;
        }
        g.drawImage(searchImages[searchCursor], 0, 0, this);
    }
    
    public void screen17(Graphics g) {
        if (key == 0) {
            key = 1;
            keypad.draw(g,this);
        }
        if (buttonno == BACK) {
            dm.draw(g, this);
            CurrentScreen = 12;
            screen12menu = dm;
            key = 0;
            return;
        }
        keypad = keypad.doAction(buttonno);
        keypad.draw(g, this);
    }
    
    public void screen16(Graphics g) {
        if (buttonno == BACK) {
            DrawMenu(g);
            CurrentScreen = 2;
            return;
        }
        switch (buttonno) {
            case UP:
                nsm.moveUp();
                break;
            case LEFT:
                CurrentScreen = nsm.moveLeft();
                if (CurrentScreen == 22) {
                    CurrentCtrl = 1;
                    screen22(g);
                    return;
                }
                break;
            case RIGHT:
                nsm.moveRight();
                break;
            case DOWN:
                nsm.moveDown();
                break;
        }
        nsm.draw(g, this);
    }
    
    public void screen15(Graphics g) {
        if (buttonno == 5) {
            DrawEditRoute(g);
            CurrentScreen = 4;
            CurrentCtrl = 2;
            return;
        }
        switch (routePreferenceCursor) {
            case 1:
                if (denalinavigationdevice.GetFreeway() == true &&
                buttonno == RIGHT)
                    denalinavigationdevice.SetFreeway(0);
                else if (denalinavigationdevice.GetFreeway() == false &&
                buttonno == LEFT)
                    denalinavigationdevice.SetFreeway(1);
                else if (buttonno == DOWN) {
                    routePreferenceCursor = 2;
                    break;
                }
                break;
            case 2:
                if (denalinavigationdevice.GetTollRoad() == true &&
                buttonno == RIGHT)
                    denalinavigationdevice.SetTollRoad(0);
                else if (denalinavigationdevice.GetTollRoad() == false &&
                buttonno == LEFT)
                    denalinavigationdevice.SetTollRoad(1);
                else if (buttonno == UP) {
                    routePreferenceCursor = 1;
                    break;
                }
                else if (buttonno == DOWN) {
                    routePreferenceCursor = 3;
                    break;
                }
                break;
            case 3:
                if (denalinavigationdevice.GetFerry() == true &&
                buttonno == RIGHT)
                    denalinavigationdevice.SetFerry(0);
                else if (denalinavigationdevice.GetFerry() == false &&
                buttonno == LEFT)
                    denalinavigationdevice.SetFerry(1);
                else if (buttonno == UP) {
                    routePreferenceCursor = 2;
                    break;
                }
                else if (buttonno == DOWN) {
                    routePreferenceCursor = 4;
                    break;
                }
                break;
            case 4:
                if (denalinavigationdevice.GetRestrictedRoad() == true &&
                buttonno == RIGHT)
                    denalinavigationdevice.SetRestrictedRoad(0);
                else if (denalinavigationdevice.GetRestrictedRoad() == false &&
                buttonno == LEFT)
                    denalinavigationdevice.SetRestrictedRoad(1);
                else if (buttonno == UP) {
                    routePreferenceCursor = 3;
                    break;
                }
                break;
        }
        DrawRoutePreference(g);
    }
    
    public void screen1(Graphics g) {
        CurrentCtrl = buttonno;
        g.drawImage(CautionImage, 0, 0, this);
        if (CurrentCtrl == SELECT) {
            cautionok = true;
            CurrentScreen = afterCautionScreen;
            CurrentCtrl = 1;
            buttonno = -1;
            render();
            repaint();
        }
    }
    
    public void screen2(Graphics g) {
        switch (menuCursor) {
            case 1: // Suspend Guidance
                switch (buttonno) {
                    case RIGHT:
                        menuCursor = 6;
                        break;
                    case DOWN:
                        menuCursor = 2;
                        break;
                }
                break;
            case 2: // Route Overview
                switch (buttonno) {
                    case UP:
                        menuCursor = 1;
                        break;
                    case SELECT:
                        g.drawImage(RouteOverviewImage, 0, 0, this);
                        CurrentScreen = 5;
                        CurrentCtrl = 1;
                        return;
                    case RIGHT:
                        menuCursor = 7;
                        break;
                    case DOWN:
                        menuCursor = 3;
                        break;
                }
                break;
            case 3: // Map Appearance
                switch (buttonno) {
                    case 0: // Top
                        menuCursor = 2;
                        break;
                    case 2: // select
                        DrawMapAppearance(g);
                        CurrentScreen = 3;
                        CurrentCtrl = 1;
                        return;
                    case 3: // Right
                        menuCursor = 8;
                        break;
                    case 4: // Down
                        menuCursor = 4;
                        break;
                }
                break;
            case 4: //Voice Volume
                switch (buttonno) {
                    case 0://Top
                        menuCursor = 3;
                        break;
                    case 3://Right
                        menuCursor = 9;
                        break;
                }
                break;
            case 5: //DVD
                switch (buttonno) {
                    case 4:
                        menuCursor = 6;
                        break;
                }
                break;
            case 6: // Edit Route
                switch (buttonno) {
                    case 0:
                        menuCursor = 5;
                        break;
                    case 1:
                        menuCursor = 1;
                        break;
                    case 2:
                        DrawEditRoute(g);
                        CurrentScreen = 4;
                        CurrentCtrl = 1;
                        return;
                    case 4:
                        menuCursor = 7;
                        break;
                }
                break;
            case 7: // Route Preview
                switch (buttonno) {
                    case 0:
                        menuCursor = 6;
                        break;
                    case 1:
                        menuCursor = 2;
                        break;
                    case 2:
                        g.drawImage(RoutePreviewImage, 0, 0, this);
                        CurrentScreen = 6;
                        CurrentCtrl = 1;
                        return;
                    case 4:
                        menuCursor = 8;
                        break;
                }
                break;
            case 8: // Nav Setup
                switch (buttonno) {
                    case 0:
                        menuCursor = 7;
                        break;
                    case 1:
                        menuCursor = 3;
                        break;
                    case 2:
                        nsm.draw(g, this);
                        CurrentScreen = 16;
                        CurrentCtrl = 1;
                        return;
                    case 4:
                        menuCursor = 9;
                        break;
                }
                break;
            case 9: // Memory Points
                switch (buttonno) {
                    case 0:
                        menuCursor = 8;
                        break;
                    case 1:
                        menuCursor = 4;
                        break;
                }
                break;
        }
        DrawMenu(g);
    }
    
    public void screen3(Graphics g) {
        if (buttonno == 5) {
            g.drawImage(MenuImage, 0, 0, this);
            DrawMenu(g);
            CurrentScreen = 2;
            CurrentCtrl = 3;
            return;
        }
        switch (mapAppearCursor) {
            case 1:
                switch (buttonno) {
                    case 2:
                        denalinavigationdevice.SetMapOrientation(1);
                        break;
                    case 3:
                        mapAppearCursor = 4;
                        break;
                    case 4:
                        mapAppearCursor = 2;
                        break;
                }
                break;
            case 2:
                switch (buttonno) {
                    case 0:
                        mapAppearCursor = 1;
                        break;
                    case 2:
                        denalinavigationdevice.SetMapOrientation(2);
                        break;
                    case 3:
                        mapAppearCursor = 5;
                        break;
                    case 4:
                        mapAppearCursor = 3;
                        break;
                }
                break;
            case 3:
                switch (buttonno) {
                    case 0:
                        mapAppearCursor = 2;
                        break;
                    case 2:
                        denalinavigationdevice.SetMapOrientation(3);
                        break;
                    case 3:
                        mapAppearCursor = 6;
                        break;
                }
                break;
            case 4:
                switch (buttonno) {
                    case 1:
                        mapAppearCursor = 1;
                        break;
                    case 2:
                        denalinavigationdevice.SetGuidanceMode(1);
                        break;
                    case 3:
                        mapAppearCursor = 7;
                        break;
                    case 4:
                        mapAppearCursor = 5;
                        break;
                }
                break;
            case 5:
                switch (buttonno) {
                    case 0:
                        mapAppearCursor = 4;
                        break;
                    case 1:
                        mapAppearCursor = 2;
                        break;
                    case 2:
                        denalinavigationdevice.SetGuidanceMode(2);
                        break;
                    case 3:
                        mapAppearCursor = 8;
                        break;
                    case 4:
                        mapAppearCursor = 6;
                        break;
                }
                break;
            case 6:
                switch (buttonno) {
                    case 0:
                        mapAppearCursor = 5;
                        break;
                    case 1:
                        mapAppearCursor = 3;
                        break;
                    case 2:
                        denalinavigationdevice.SetGuidanceMode(3);
                        break;
                }
                break;
            case 7:
                switch (buttonno) {
                    case 1:
                        mapAppearCursor = 4;
                        break;
                    case 2:
                        denalinavigationdevice.SetMapMode(1);
                        break;
                    case 4:
                        mapAppearCursor = 8;
                        break;
                }
                break;
            case 8:
                switch (buttonno) {
                    case 0:
                        mapAppearCursor = 7;
                        break;
                    case 1:
                        mapAppearCursor = 5;
                        break;
                    case 2:
                        denalinavigationdevice.SetMapMode(2);
                        break;
                }
                break;
        }
        DrawMapAppearance(g);        
    }
    
    public void screen4(Graphics g) {
        if (buttonno == 5) {
            DrawMenu(g);
            CurrentScreen = 2;
            CurrentCtrl = 6;
            return;
        }
        switch (editRouteCursor) {
            case 1:
                switch (buttonno) {
                    case 3:
                        editRouteCursor = 3;
                        break;
                    case 4:
                        editRouteCursor = 2;
                        break;
                }
                break ;
            case 2:
                switch (buttonno) {
                    case 0:
                        editRouteCursor = 1;
                        break;
                    case 2: // select of route preference
                        DrawRoutePreference(g);
                        CurrentScreen = 15;
                        editRouteCursor = 2;
                        return;
                    case 3:
                        editRouteCursor = 5;
                        break;
                    case 4:
                        editRouteCursor = 9;
                        break;
                }
                break ;
            case 3:
                switch (buttonno) {
                    case 1:
                        editRouteCursor = 1;
                        break;
                    case 2:
                        CurrentScreen = 12;
                        editRouteCursor = 1;
                        buttonno = -1;
                        screen12menu = editRouteDM;
                        screen12(g);
                        return;
                    case 3:
                        editRouteCursor = 6;
                        break;
                    case 4:
                        editRouteCursor = 4;
                        break;
                }
                break ;
            case 4:
                switch (buttonno) {
                    case 0:
                        editRouteCursor = 3;
                        break;
                    case 1:
                        editRouteCursor = 1;
                        break;
                    case 3:
                        editRouteCursor = 7;
                        break;
                    case 4:
                        editRouteCursor = 5;
                        break;
                }
                break ;
            case 5:
                switch (buttonno) {
                    case 0:
                        editRouteCursor = 4;
                        break;
                    case 1:
                        editRouteCursor = 2;
                        break;
                    case 3:
                        editRouteCursor = 8;
                        break;
                    case 4:
                        editRouteCursor = 9;
                        break;
                }
                break ;
            case 6:
                switch (buttonno) {
                    case 1:
                        editRouteCursor = 3;
                        break;
                    case 4:
                        editRouteCursor = 7;
                        break;
                }
                break ;
            case 7:
                switch (buttonno) {
                    case 0:
                        editRouteCursor = 6;
                        break;
                    case 1:
                        editRouteCursor = 4;
                        break;
                    case 4:
                        editRouteCursor = 8;
                        break;
                }
                break ;
            case 8:
                switch (buttonno) {
                    case 0:
                        editRouteCursor = 7;
                        break;
                    case 1:
                        editRouteCursor = 5;
                        break;
                    case 4:
                        editRouteCursor = 9;
                        break;
                }
                break ;
            case 9:
                switch (buttonno) {
                    case 0:
                        editRouteCursor = 8;
                        break;
                    case 1:
                        editRouteCursor = 5;
                        break;
                }
                break;
        }
        DrawEditRoute(g);
    }
    
    public void screen5(Graphics g) {
        /*
        if (buttonno == 5) {
            g.drawImage (MenuImage, 0, 0, this);
            DrawMenuRectangle (g, 154, 161, 43, 31); //2
            CurrentScreen = 2;
            CurrentCtrl = 2;
        }
         */
    }
    
    public void screen6(Graphics g) {
        /*
        if (buttonno == 5) {
            DrawMenu(g);
            CurrentScreen = 2;
            CurrentCtrl = 7;
        }
         */
    }
    
    public void screen7(Graphics g) {
        g.drawImage(MapImage, 0, 0, this);
        CurrentCtrl = buttonno;
        if (CurrentCtrl == DEST) {
            DrawDestination(g);
            CurrentScreen = 12;
            CurrentCtrl = 2;
        }
        else if (CurrentCtrl == MENU) {
            DrawMenu(g);
            CurrentScreen = 2;
            CurrentCtrl = 7;
        }
    }
    
    public void screen8(Graphics g) {
        g.drawImage(ZoomInImage, 0, 0, this);
        CurrentCtrl = 1;
    }
    
    public void screen9(Graphics g) {
        g.drawImage(MapImage, 0, 0, this);
        CurrentCtrl = 1;
    }
    
    public void screen10(Graphics g) {
        DrawMenu(g);
        CurrentScreen = 2;
        CurrentCtrl = 1;
    }
    
    public void screen11(Graphics g) {
        g.drawImage(NavigationKey, 0, 0, this);
        CurrentCtrl = 1;
    }
    
    public void screen12(Graphics g) {
        if (CurrentCtrl == 1)
            screen12menu.draw(g, this);
        switch (buttonno) {
            case UP:
                screen12menu.moveUp();
                screen12menu.draw(g, this);
                break;
            case LEFT:
                screen12menu.moveLeft();
                screen12menu.draw(g, this);
                break;
            case RIGHT:
                screen12menu.moveRight();
                screen12menu.draw(g, this);
                break;
            case DOWN:
                screen12menu.moveDown();
                screen12menu.draw(g, this);
                break;
            case SELECT:
                if (!screen12menu.isInSearchOptions()) {
                    switch (screen12menu.getMenuItem()) {
                        case 0:
                            CurrentScreen = 7;
                            screen7(g); // shouldn't be screen 7
                            break;
                        case 1:
                            keypad = new EnglishKeypad(AddressImage,
                                                       AddressImage3,
                                                       AddressImage2,
                                                       HouseAddressImage,
                                                       okayImage,
                                                       "Input street name","",
                                                       262,156);
                            CurrentScreen = 14;
                            buttonno = -1;
                            destCursor = 2;
                            //denalinavigationdevice.SetDestinationOption(2);
                            keypad_screen14(g);
                            break;
                        case 2:
                            keypad = new EnglishKeypad(POIImage, POIImage3,
                                                       POIImage2,
                                                       okayImage,
                                                       "Input POI name","",
                                                       262,128);
                            CurrentScreen = 14;
                            buttonno = -1;
                            destCursor = 2;
                            //denalinavigationdevice.SetDestinationOption(2);
                            keypad_screen14(g);
                            break;
                        case 11:
                            keypad = new PhoneKeypad(phoneImage);
                            CurrentScreen = 17;
                            buttonno = -1;
                            destCursor = 2;
                            //denalinavigationdevice.SetDestinationOption(2);
                            screen17(g);
                            break;
                        case 13:
                            keypad = 
                             new IntersectEnglishKeypad(Intersection1Image,
                                                       Intersection1Image3,
                                                       Intersection1Image2,
                                                       okayImage,
                                                       "Input 1st street name",
                                                       "", null,262,128, null);
                            CurrentScreen = 21;
                            buttonno = -1;
                            destCursor = 2;
                            //denalinavigationdevice.SetDestinationOption(2);
                            keypad_screen14(g);
                            break;
                        case 14:
                            keypad = new EnglishKeypad(FreewayImage,
                                                       FreewayImage3,
                                                       FreewayImage2,
                                                       okayImage,
                                                       "Input F-way name","",
                                                       262,128);
                            CurrentScreen = 14;
                            buttonno = -1;
                            destCursor = 2;
                            //denalinavigationdevice.SetDestinationOption(2);
                            keypad_screen14(g);
                            break;
                    }
                }
                else if (screen12menu.getSearchOptionItem() == 1) {
                    CurrentScreen = 19;
                    buttonno = -1;
                    screen19(g);
                }
                break;
        }
    }
    
    public void screen13(Graphics g) {
        System.out.println("in screen13");
        if (buttonno == BACK) {
            dm.draw(g, this);
            CurrentScreen = 12;
            screen12menu = dm;
            CurrentCtrl = 2;
        }
        switch (CurrentCtrl) {
            case 1:
                switch (buttonno) {
                    case DOWN:
                        keypad = new EnglishKeypad(AddressImage,
                                                   AddressImage,
                                                   AddressImage,
                                                   HouseAddressImage,
                                                   okayImage,
                                                   "Input street name","",
                                                   262,156);
                        keypad.draw(g, this);
                        //DrawAddressRectangle (g, 150, 142, 90, 20); //2
                        CurrentCtrl  = 2;
                        break;
                }
                break;
            case 2:
                switch (buttonno) {
                    case UP:
                        //DrawAddressRectangle (g, 150, 112, 90, 21); 	// 1
                        keypad = new EnglishKeypad(AddressImage,
                                                   AddressImage,
                                                   AddressImage,
                                                   okayImage,
                                                   "Input street name","",
                                                   262,156);
                        keypad.draw(g, this);
                        CurrentCtrl = 1;
                        break;
                    case SELECT:
                        CurrentScreen = 14;
                        buttonno = -1;
                        //denalinavigationdevice.SetDestinationOption(2);
                        keypad_screen14(g);
                        //if selected street, then go to the keypad and implement keypad with string
                        break;
                }
                break;
        }
    }
    
    public void keypad_screen14(Graphics g) {
        if (key == 0) {
            key = 1;
            keypad.draw(g,this);
        }
        if (buttonno == BACK) {
            dm.draw(g, this);
            CurrentScreen = 12;
            screen12menu = dm;
            CurrentCtrl = 2;
            address = "";
            key = 0;
            return;
        }
        keypad = keypad.doAction(buttonno);
        keypad.draw(g, this);
        if (keypad.getClass().getName().endsWith("HouseNoKeypad")) {
            CurrentScreen = 20;
        }
    }
    
    public void DrawDestination(Graphics g) {
        switch (destCursor) {
            case 1:
                DrawDestinationRectangle(g, 196, 118, 38, 30); 
                break;
            case 2:
                DrawDestinationRectangle(g, 196, 159, 38, 30); 
                break;
            case 3:
                DrawDestinationRectangle(g, 196, 200, 38, 32); 
                break;
        }
    }
    
    public void DrawDestinationRectangle(Graphics g, int x, int y, int width, int height) {
        g.drawImage(DestinationImage, 0, 0, this);
        g.setColor(new Color(247, 204, 139));
        g.drawRect(x, y, width, height);
    }
    
    public void mouseReleased(java.awt.event.MouseEvent e) {
        Rectangle rect;
        Point pt = e.getPoint();
        buttonclickflag = false;
        for (int i = 0; i < buttonArea.length; ++i) {
            if (buttonArea[i].contains(pt)) {
                buttonclickflag = true;
                buttonno = i;
            }
        }
        if (buttonno >= 6) {
            address = "";
            key = 0;
        }
        
        if (inNavSys) {        
            switch (buttonno) {
                case MAP: // Map Button
                    if (cautionok)
                        CurrentScreen = 7;
                    else afterCautionScreen = 7;
                    break;
                case ZOOMIN: // Zoom In Button
                    if (cautionok)
                        CurrentScreen = 8;
                    break;
                case ZOOMOUT: // Zoom Out Button
                    if (cautionok)
                        CurrentScreen = 9;
                    break;
                case MENU: // Menu Button
                    if (cautionok) {
                        CurrentScreen = 2;
                        CurrentCtrl = 1;
                    }
                    else afterCautionScreen = 2;
                    break;
                case DEST: // Destination Button
                    if (cautionok) {
                        screen12menu = dm;
                        CurrentScreen = 12;
                        CurrentCtrl = 1;
                    }
                    else {
                        afterCautionScreen = 12;
                        screen12menu = dm;
                    }
                    break;
            }
        }
        switch (buttonno) {
            case NAV: // Nav Button
                if (cautionok)
                    CurrentScreen = 7;
                else CurrentScreen = 1;
                inNavSys = true;
                break;
            case CONFIG:
                inNavSys = false;
                CurrentScreen = 24;
                break;
        }
        render();
        repaint();
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
     
                Frame f = new Frame ("Denali Navigation Simulator");
                DenaliNavigationGUI panel = new DenaliNavigationGUI();
                f.setLayout( new BorderLayout() );
                f.add( panel );
                f.setSize (650, 390);
                f.show();
                }*/
}
