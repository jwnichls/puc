/*
 * IntersectEnglishKeypad.java
 *
 * Created on December 4, 2003, 11:59 AM
 */

package edu.cmu.hcii.puc.devices;

import java.awt.*;

/**
 *
 * @author  pegeen
 */
public class IntersectEnglishKeypad extends EnglishKeypad {
    private Keypad backKeypad;
    private String street;

    public IntersectEnglishKeypad(Image i1, Image i2, Image i3, Image okay,
                                  String s1, String value, String s2, 
                                  int x, int y, Keypad kp) {
        super(i1, i2, i3, okay, s1, value, x, y);
        backKeypad = kp;
        street = s2;
    }

    public Keypad getBackKeypad() {
        return backKeypad;
    }

    public Keypad doOkay() {
        return null;
    }
    
    public Keypad goNumber() {
        return null;
    }
    
    public Keypad goExtend() {
        return null;
    }
    
    public void draw(Graphics g, Panel p) {
        super.draw(g, p);
        if (street != null) {
            g.drawString(street, 262, 128);
        }
    }
}
