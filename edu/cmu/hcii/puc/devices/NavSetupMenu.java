/*
 * NavSetupMenu.java
 *
 * Created on October 21, 2003, 1:15 PM
 */

package edu.cmu.hcii.puc.devices;

import java.awt.*;

/**
 *
 * @author  pegeen
 */
public class NavSetupMenu implements ScrollableMenu {
    
    private DenaliNavigationDevice dnd;
    private final int METRIC_SYS = 1;
    private final int SEASONAL_RESTRICT = 2;
    private final int TRAVEL_TIME = 3;
    private final int SPEED_CHANGE = 4;
    private final int AUTO_REROUTE = 5;
    private final int VOICE_GUIDE = 6;
    private final int CURRENT_STREET_NAME = 7;
    private final int DAY_MAP_COLOR = 8;
    private final int NIGHT_MAP_COLOR = 9;
    private final int OPERATION_GUIDE = 10;
    private final int CALIBERATE = 11;
    private final int DEFAULTS = 12;
    private final int EXIT_INFO = 13;
    private Image appearance;
    private Image[] menuViews;
    private Image onHighlight, offHighlight;
    private Image milesHighlight, kmHighlight;
    private Image normalHighlight, autoHighlight;
    private Image oneHighlight, twoHighlight, threeHighlight, fourHighlight;
    private int menuView = 0;
    private int cursorPos = 0;
    private int menuItem = 0;
    private Image[] scrollbarViews;
    private Rectangle defMenuArea[] = {
        new Rectangle(175, 114, 352, 34),
        new Rectangle(175, 148, 352, 34),
        new Rectangle(175, 181, 352, 34),
        new Rectangle(175, 217, 352, 34),
        new Rectangle(175, 251, 352, 34),
    };
    private Rectangle menu3Area[] = {
        new Rectangle(175, 114, 352, 34),
        new Rectangle(175, 148, 352, 34),
        new Rectangle(175, 181, 352, 34),
        new Rectangle(175, 217, 352, 68),
    };
    private Rectangle menu4Area[] = {
        new Rectangle(175, 114, 352, 34),
        new Rectangle(175, 148, 352, 68),
        new Rectangle(175, 217, 352, 68),
    };
    private Rectangle menu5Area[] = {
        new Rectangle(175, 114, 352, 68),
        new Rectangle(175, 181, 352, 68),
        new Rectangle(175, 251, 352, 34),
    };
    private Rectangle menu6Area[] = {
        new Rectangle(175, 114, 352, 68),
        new Rectangle(175, 181, 352, 34),
        new Rectangle(175, 217, 352, 34),
        new Rectangle(175, 251, 352, 34),
    };
    private int xposOnOff[] = {189, 261, 312, 363, 416, 467};
    private int yposOnOff[] = {118, 150, 185, 221, 256};
    private int menuViewItems[] = { 5, 5, 5, 4, 3, 3, 4, 5, 5, 5, 5 };
    
    public NavSetupMenu(Image[] menus, Image[] scrollbars, 
                        Image on, Image off,
                        Image miles, Image km,
                        Image normal, Image auto, Image one, Image two,
                        Image three, Image four,
                        DenaliNavigationDevice device) {
        menuViews = menus;
        scrollbarViews = scrollbars;
        dnd = device;
        onHighlight = on;
        offHighlight = off;
        milesHighlight = miles;
        kmHighlight = km;
        normalHighlight = normal;
        autoHighlight = auto;
        oneHighlight = one;
        twoHighlight = two;
        threeHighlight = three;
        fourHighlight = four;
    }
    
    public void draw(Graphics g, Panel p) {
        g.drawImage(menuViews[menuView], 0, 0, p);
        g.drawImage(scrollbarViews[menuItem], 145, 121, p);
        g.setColor(new Color(247, 204, 139));
        switch (menuView) {
            case 0:
                if (dnd.GetEnglishMetric() == 1)
                    g.drawImage(milesHighlight, xposOnOff[4], yposOnOff[0], p);
                else g.drawImage(kmHighlight, xposOnOff[5], yposOnOff[0], p);
                if (dnd.GetSeasonalRestrict())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[1], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[1], p);
                if (dnd.GetTravelTime())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[2], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[2], p);
                if (dnd.GetAutoReroute())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 1:
                if (dnd.GetSeasonalRestrict())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[0], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[0], p);
                if (dnd.GetTravelTime())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[1], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[1], p);
                if (dnd.GetAutoReroute())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[3], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[3], p);
                if (dnd.GetVoiceGuide())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 2:
                if (dnd.GetTravelTime())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[0], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[0], p);
                if (dnd.GetAutoReroute())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[2], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[2], p);
                if (dnd.GetVoiceGuide())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[3], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[3], p);
                if (dnd.GetCurrentStreetName())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 3:
                if (dnd.GetAutoReroute())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[0], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[0], p);
                if (dnd.GetVoiceGuide())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[1], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[1], p);
                if (dnd.GetCurrentStreetName())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[2], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[2], p);
                if (dnd.GetDayMapColor() == -1)
                    g.drawImage(normalHighlight, xposOnOff[0], yposOnOff[4], p);
                else if (dnd.GetDayMapColor() == 0)
                    g.drawImage(autoHighlight, xposOnOff[1], yposOnOff[4], p);
                else if (dnd.GetDayMapColor() == 1)
                    g.drawImage(oneHighlight, xposOnOff[2], yposOnOff[4], p);
                else if (dnd.GetDayMapColor() == 2)
                    g.drawImage(twoHighlight, xposOnOff[3], yposOnOff[4], p);
                else if (dnd.GetDayMapColor() == 3)
                    g.drawImage(threeHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(fourHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 4:
                if (dnd.GetCurrentStreetName())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[0], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[0], p);
                if (dnd.GetDayMapColor() == -1)
                    g.drawImage(normalHighlight, xposOnOff[0], yposOnOff[2], p);
                else if (dnd.GetDayMapColor() == 0)
                    g.drawImage(autoHighlight, xposOnOff[1], yposOnOff[2], p);
                else if (dnd.GetDayMapColor() == 1)
                    g.drawImage(oneHighlight, xposOnOff[2], yposOnOff[2], p);
                else if (dnd.GetDayMapColor() == 2)
                    g.drawImage(twoHighlight, xposOnOff[3], yposOnOff[2], p);
                else if (dnd.GetDayMapColor() == 3)
                    g.drawImage(threeHighlight, xposOnOff[4], yposOnOff[2], p);
                else g.drawImage(fourHighlight, xposOnOff[5], yposOnOff[2], p);
                if (dnd.GetNightMapColor() == -1)
                    g.drawImage(normalHighlight, xposOnOff[0], yposOnOff[4], p);
                else if (dnd.GetNightMapColor() == 0)
                    g.drawImage(autoHighlight, xposOnOff[1], yposOnOff[4], p);
                else if (dnd.GetNightMapColor() == 1)
                    g.drawImage(oneHighlight, xposOnOff[2], yposOnOff[4], p);
                else if (dnd.GetNightMapColor() == 2)
                    g.drawImage(twoHighlight, xposOnOff[3], yposOnOff[4], p);
                else if (dnd.GetNightMapColor() == 3)
                    g.drawImage(threeHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(fourHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 5:
                if (dnd.GetDayMapColor() == -1)
                    g.drawImage(normalHighlight, xposOnOff[0], yposOnOff[1], p);
                else if (dnd.GetDayMapColor() == 0)
                    g.drawImage(autoHighlight, xposOnOff[1], yposOnOff[1], p);
                else if (dnd.GetDayMapColor() == 1)
                    g.drawImage(oneHighlight, xposOnOff[2], yposOnOff[1], p);
                else if (dnd.GetDayMapColor() == 2)
                    g.drawImage(twoHighlight, xposOnOff[3], yposOnOff[1], p);
                else if (dnd.GetDayMapColor() == 3)
                    g.drawImage(threeHighlight, xposOnOff[4], yposOnOff[1], p);
                else g.drawImage(fourHighlight, xposOnOff[5], yposOnOff[1], p);
                if (dnd.GetNightMapColor() == -1)
                    g.drawImage(normalHighlight, xposOnOff[0], yposOnOff[3], p);
                else if (dnd.GetNightMapColor() == 0)
                    g.drawImage(autoHighlight, xposOnOff[1], yposOnOff[3], p);
                else if (dnd.GetNightMapColor() == 1)
                    g.drawImage(oneHighlight, xposOnOff[2], yposOnOff[3], p);
                else if (dnd.GetNightMapColor() == 2)
                    g.drawImage(twoHighlight, xposOnOff[3], yposOnOff[3], p);
                else if (dnd.GetNightMapColor() == 3)
                    g.drawImage(threeHighlight, xposOnOff[4], yposOnOff[3], p);
                else g.drawImage(fourHighlight, xposOnOff[5], yposOnOff[3], p);
                if (dnd.GetGuideMap())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 6:
                if (dnd.GetNightMapColor() == -1)
                    g.drawImage(normalHighlight, xposOnOff[0], yposOnOff[1], p);
                else if (dnd.GetNightMapColor() == 0)
                    g.drawImage(autoHighlight, xposOnOff[1], yposOnOff[1], p);
                else if (dnd.GetNightMapColor() == 1)
                    g.drawImage(oneHighlight, xposOnOff[2], yposOnOff[1], p);
                else if (dnd.GetNightMapColor() == 2)
                    g.drawImage(twoHighlight, xposOnOff[3], yposOnOff[1], p);
                else if (dnd.GetNightMapColor() == 3)
                    g.drawImage(threeHighlight, xposOnOff[4], yposOnOff[1], p);
                else g.drawImage(fourHighlight, xposOnOff[5], yposOnOff[1], p);
                if (dnd.GetGuideMap())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[2], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[2], p);
                break;
            case 7:
                if (dnd.GetGuideMap())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[0], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[0], p);
                if (dnd.GetExitInfo())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[3], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[3], p);
                if (dnd.GetEnglishMetric() == 1)
                    g.drawImage(milesHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(kmHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 8:
                if (dnd.GetExitInfo())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[2], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[2], p);
                if (dnd.GetEnglishMetric() == 1)
                    g.drawImage(milesHighlight, xposOnOff[4], yposOnOff[3], p);
                else g.drawImage(kmHighlight, xposOnOff[5], yposOnOff[3], p);
                if (dnd.GetSeasonalRestrict())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 9:
                if (dnd.GetExitInfo())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[1], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[1], p);
                if (dnd.GetEnglishMetric() == 1)
                    g.drawImage(milesHighlight, xposOnOff[4], yposOnOff[2], p);
                else g.drawImage(kmHighlight, xposOnOff[5], yposOnOff[2], p);
                if (dnd.GetSeasonalRestrict())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[3], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[3], p);
                if (dnd.GetTravelTime())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[4], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[4], p);
                break;
            case 10:
                if (dnd.GetExitInfo())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[0], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[0], p);
                if (dnd.GetEnglishMetric() == 1)
                    g.drawImage(milesHighlight, xposOnOff[4], yposOnOff[1], p);
                else g.drawImage(kmHighlight, xposOnOff[5], yposOnOff[1], p);
                if (dnd.GetSeasonalRestrict())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[2], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[2], p);
                if (dnd.GetTravelTime())
                    g.drawImage(onHighlight, xposOnOff[4], yposOnOff[3], p);
                else g.drawImage(offHighlight, xposOnOff[5], yposOnOff[3], p);
                break;
        }
        if (menuView == 3) {
            g.drawRect((int)menu3Area[cursorPos].getX(),
                       (int)menu3Area[cursorPos].getY(),
                       (int)menu3Area[cursorPos].getWidth(),
                       (int)menu3Area[cursorPos].getHeight());
        }
        else if (menuView == 4) {
            g.drawRect((int)menu4Area[cursorPos].getX(),
                       (int)menu4Area[cursorPos].getY(),
                       (int)menu4Area[cursorPos].getWidth(),
                       (int)menu4Area[cursorPos].getHeight());
        }
        else if (menuView == 5) {
            g.drawRect((int)menu5Area[cursorPos].getX(),
                       (int)menu5Area[cursorPos].getY(),
                       (int)menu5Area[cursorPos].getWidth(),
                       (int)menu5Area[cursorPos].getHeight());
        }
        else if (menuView == 6) {
            g.drawRect((int)menu6Area[cursorPos].getX(),
                       (int)menu6Area[cursorPos].getY(),
                       (int)menu6Area[cursorPos].getWidth(),
                       (int)menu6Area[cursorPos].getHeight());
        }
        else {
            g.drawRect((int)defMenuArea[cursorPos].getX(),
                       (int)defMenuArea[cursorPos].getY(),
                       (int)defMenuArea[cursorPos].getWidth(),
                       (int)defMenuArea[cursorPos].getHeight());
        }
    }
    
    public void moveDown() {
        menuItem = (menuItem + 1) % scrollbarViews.length;
        if (cursorPos == menuViewItems[menuView] - 1) {
            menuView = (menuView + 1) % menuViews.length;
            if (cursorPos >= menuViewItems[menuView])
                cursorPos = menuViewItems[menuView] - 1;
        }
        else cursorPos++;
    }
    
    public void moveUp() {
        menuItem = (menuItem + scrollbarViews.length - 1) % scrollbarViews.length;
        if (cursorPos == 0) {
            int oldCount = menuViewItems[menuView];
            menuView = (menuView + menuViews.length - 1) % menuViews.length;
            if (oldCount < menuViewItems[menuView])
                cursorPos = menuViewItems[menuView] - oldCount;
        }
        else cursorPos--;
    }

    public int moveLeft() {
        switch (menuItem) {
            case 0:
                dnd.SetEnglishMetric(1);
                break;
            case 1:
                dnd.SetSeasonalRestrict(1);
                break;
            case 2:
                dnd.SetTravelTime(1);
                break;
            case 4:
                dnd.SetAutoReroute(1);
                break;
            case 5:
                dnd.SetVoiceGuide(1);
                break;
            case 6:
                dnd.SetCurrentStreetName(1);
                break;
            case 7:
                if (dnd.GetDayMapColor() != -1)
                    dnd.SetDayMapColor(dnd.GetDayMapColor()-1);
                break;
            case 8:
                if (dnd.GetNightMapColor() != -1)
                    dnd.SetNightMapColor(dnd.GetNightMapColor()-1);
                break;
            case 9:
                dnd.SetGuideMap(1);
                break;
            case 10:
                return DenaliNavigationGUI.CALIBERATE_SYSTEM_SCREEN;
            case 11:
                dnd.SetEnglishMetric(1);
                dnd.SetSeasonalRestrict(1);
                dnd.SetTravelTime(1);
                dnd.SetAutoReroute(1);
                dnd.SetVoiceGuide(1);
                dnd.SetCurrentStreetName(1);
                dnd.SetDayMapColor(-1);
                dnd.SetNightMapColor(-1);
                dnd.SetGuideMap(1);
                dnd.SetExitInfo(1);
                break;
            case 12:
                dnd.SetExitInfo(1);
                break;
        }
        return DenaliNavigationGUI.NAV_SETUP_SCREEN;
    }
    
    public void moveRight() {
        switch (menuItem) {
            case 0:
                dnd.SetEnglishMetric(0);
                break;
            case 1:
                dnd.SetSeasonalRestrict(0);
                break;
            case 2:
                dnd.SetTravelTime(0);
                break;
            case 4:
                dnd.SetAutoReroute(0);
                break;
            case 5:
                dnd.SetVoiceGuide(0);
                break;
            case 6:
                dnd.SetCurrentStreetName(0);
                break;
            case 7:
                if (dnd.GetDayMapColor() != 4)
                    dnd.SetDayMapColor(dnd.GetDayMapColor()+1);
                break;
            case 8:
                if (dnd.GetNightMapColor() != 4)
                    dnd.SetNightMapColor(dnd.GetNightMapColor()+1);
                break;
            case 9:
                dnd.SetGuideMap(0);
                break;
            case 12:
                dnd.SetExitInfo(0);
                break;
        }
    }
    
}
