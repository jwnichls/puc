/**
   @Pegeen Shen
*/
package edu.cmu.hcii.puc.devices;

import java.awt.*;

public interface Keypad {
    static final int UP = 0;
    static final int SELECT = 2;
    static final int LEFT = 1;
    static final int RIGHT = 3;
    static final int DOWN = 4;

    public String getInput();
    public Keypad doAction(int action);
    public void draw (Graphics g, Panel p);
}
