/**
 * @Pegeen Shen
 */
package edu.cmu.hcii.puc.devices;

import java.awt.*;

public class DestinationMenu implements ScrollableMenu {
    
    private Image[] menuViews;
    private Image[] scrollbarViews;
    private Image[] searchAreaViews;
    private int searchArea;
    private int searchCursorPos = 0;
    private boolean inMenu = true;
    private int menuView = 0;
    private int cursorPos = 2;
    private int menuItem = 0;
    private boolean isSearchOptions = false;
    private Image backImage = null;
    private Rectangle menuArea[] = {
        new Rectangle (0, 0, 0, 0),
	new Rectangle (196, 200, 38, 32),
	new Rectangle (186, 162, 46, 32),
	new Rectangle (186, 203, 46, 32),
    };
    private Rectangle searchOptionArea[] = {
        new Rectangle (0, 0, 0, 0),
        new Rectangle (440, 205, 109, 28),
        new Rectangle (440, 247, 109, 28),
    };
    
    public DestinationMenu(Image[] images1, Image[] images2, Image[] images3,
                           int search, int menu, int pos, int menuItem) {
        menuViews = images2;
        scrollbarViews = images1;
        searchAreaViews = images3;
        searchArea = search;
        menuView = menu;
        cursorPos = pos;
        this.menuItem = menuItem;
    }
    
    public DestinationMenu(Image[] images1, Image[] images2, Image[] images3,
                           int search, int menu, int pos, int menuItem,
                           Image back) {
        menuViews = images2;
        scrollbarViews = images1;
        searchAreaViews = images3;
        searchArea = search;
        menuView = menu;
        cursorPos = pos;
        this.menuItem = menuItem;
        backImage = back;
    }

    public void moveUp() {
        if (!isSearchOptions) {
            menuItem = (menuItem + scrollbarViews.length - 1) % scrollbarViews.length;
            if (scrollbarViews[menuItem] == null) {
                while (scrollbarViews[menuItem] == null) {
                    menuItem = (menuItem + scrollbarViews.length - 1) % scrollbarViews.length;
                    menuView = (menuView + menuViews.length - 1) % menuViews.length;
                }
            }
            else if (cursorPos == 2) {
                menuView = (menuView + menuViews.length - 1) % menuViews.length;
            }
            else {
                cursorPos--;
            }
        }
        else {
            searchCursorPos = 1;
        }
    }
    
    public void draw(Graphics g, Panel p) {
        g.drawImage(menuViews[menuView], 0, 0, p);
        g.drawImage(scrollbarViews[menuItem], 140, 110, p);
        g.drawImage(searchAreaViews[searchArea], 431, 100, p);
        if (backImage != null)
            g.drawImage(backImage, 410, 296, p);
        g.setColor(new Color(247, 204, 139));
        if (!isSearchOptions)
            g.drawRect((int)menuArea[cursorPos].getX(),
                       (int)menuArea[cursorPos].getY(),
                       (int)menuArea[cursorPos].getWidth(),
                       (int)menuArea[cursorPos].getHeight());
        else
            g.drawRect((int)searchOptionArea[searchCursorPos].getX(),
                       (int)searchOptionArea[searchCursorPos].getY(),
                       (int)searchOptionArea[searchCursorPos].getWidth(),
                       (int)searchOptionArea[searchCursorPos].getHeight());
        g.setColor(new Color(0 ,0, 0));
    }
    
    public void moveDown() {
        if (!isSearchOptions) {
            menuItem = (menuItem + 1) % scrollbarViews.length;
            if (scrollbarViews[menuItem] == null) {
                while (scrollbarViews[menuItem] == null) {
                    menuItem = (menuItem + 1) % scrollbarViews.length;
                    menuView = (menuView + 1) % menuViews.length;
                }
            }
            else if (cursorPos == 3) {
                menuView = (menuView + 1) % menuViews.length;
            }
            else {
                cursorPos++;
            }
        }
        else {
            searchCursorPos = 2;
        }
    }
    
    public int getMenuItem() {
        return menuItem;
    }
    
    public boolean isInSearchOptions() {
        return isSearchOptions;
    }
    
    public int getSearchOptionItem() {
        return searchCursorPos;
    }
    
    public void moveRight() {
        if (cursorPos == 3) {
            isSearchOptions = true;
            searchCursorPos = 1;
        }
    }
    
    public void moveLeft() {
        isSearchOptions = false;
    }
    
    public void setSearchArea(int i) {
        searchArea = i;
    }
}
