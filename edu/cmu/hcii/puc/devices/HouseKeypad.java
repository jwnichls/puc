/**
   @Pegeen Shen
*/
package edu.cmu.hcii.puc.devices;

import java.awt.*;

public class HouseKeypad implements Keypad {

    private Image keysImage;

    public HouseKeypad(Image i) {
	keysImage = i;
    }

    public String getInput() {
	return "";
    }

    public Keypad doAction(int action) {
	return this;
    }

    public void draw (Graphics g, Panel p) {
    }
}
