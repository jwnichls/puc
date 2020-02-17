/**
   @Pegeen Shen
*/
package edu.cmu.hcii.puc.devices;

import java.awt.*;

public class PhoneKeypad implements Keypad {

    private Image keysImage;
    private String inputValue;
    private int key = 5;
    private Rectangle keypadArea[] = {
	new Rectangle (0, 0, 0, 0),
	new Rectangle (210, 163, 67, 25),
	new Rectangle (285, 163, 67, 25),
	new Rectangle (360, 163, 67, 25),
	new Rectangle (210, 193, 67, 25),
	new Rectangle (285, 193, 67, 25),
	new Rectangle (360, 193, 67, 25),
	new Rectangle (210, 223, 67, 25),
	new Rectangle (285, 223, 67, 25),
	new Rectangle (360, 223, 67, 25),
	new Rectangle (285, 253, 67, 25),
	new Rectangle (460, 162, 67, 25),
	new Rectangle (460, 245, 67, 29),
    };
    private Font inputfont = new Font("SansSerif", Font.BOLD, 17);

    public PhoneKeypad(Image i) {
	keysImage = i;
        inputValue = "";
    }

    public String getInput() {
	return inputValue;
    }

    public Keypad doAction(int action) {
        switch (key) {
            case 1:
                switch (action) {
                    case UP:
                        key = 7;
                        break;
                    case SELECT:
                        inputValue = inputValue + "1";
                        break;
                    case RIGHT:
                        key = 2;
                        break;
                    case DOWN:
                        key = 4;
                        break;
                    case LEFT:
                        key = 11;
                        break;
                }
                break;
            case 2:
                switch (action) {
                    case UP:
                        key = 10;
                        break;
                    case DOWN:
                        key = 5;
                        break;
                    case SELECT:
                        inputValue = inputValue + "2";
                        break;
                    case RIGHT:
                        key = 3;
                        break;
                    case LEFT:
                        key = 1;
                        break;
                }
                break;
            case 3:
                switch (action) {
                    case UP:
                        key = 9;
                        break;
                    case SELECT:
                        inputValue = inputValue + "3";
                        break;
                    case RIGHT:
                        key = 11;
                        break;
                    case LEFT:
                        key = 2;
                        break;
                    case DOWN:
                        key = 6;
                        break;
                }
                break;
            case 4:
                switch (action) {
                    case UP:
                        key = 1;
                        break;
                    case SELECT:
                        inputValue = inputValue + "4";
                        break;
                    case RIGHT:
                        key = 5;
                        break;
                    case LEFT:
                        key = 6;
                        break;
                    case DOWN:
                        key = 7;
                        break;
                }
                break;
            case 5:
                switch (action) {
                    case UP:
                        key = 2;
                        break;
                    case SELECT:
                        inputValue = inputValue + "5";
                        break;
                    case RIGHT:
                        key = 6;
                        break;
                    case LEFT:
                        key = 4;
                        break;
                    case DOWN:
                        key = 8;
                        break;
                }
                break;
            case 6:
                switch (action) {
                    case UP:
                        key = 3;
                        break;
                    case SELECT:
                        inputValue = inputValue + "6";
                        break;
                    case RIGHT:
                        key = 4;
                        break;
                    case LEFT:
                        key = 5;
                        break;
                    case DOWN:
                        key = 9;
                        break;
                }
                break;
            case 7:
                switch (action) {
                    case UP:
                        key = 4;
                        break;
                    case SELECT:
                        inputValue = inputValue + "7";
                        break;
                    case RIGHT:
                        key = 8;
                        break;
                    case LEFT:
                        key = 9;
                        break;
                    case DOWN:
                        key = 1;
                        break;
                }
                break;
            case 8:
                switch (action) {
                    case UP:
                        key = 5;
                        break;
                    case LEFT:
                        key = 7;
                        break;
                    case SELECT:
                        inputValue = inputValue + "8";
                        break;
                    case RIGHT:
                        key = 9;
                        break;
                    case DOWN:
                        key = 10;
                        break;
                }
                break;
            case 9:
                switch (action) {
                    case UP:
                        key = 6;
                        break;
                    case SELECT:
                        inputValue = inputValue + "9";
                        break;
                    case RIGHT:
                        key = 7;
                        break;
                    case DOWN:
                        key = 3;
                        break;
                }
                break;
            case 10:
                switch (action) {
                    case UP:
                        key = 8;
                        break;
                    case SELECT:
                        inputValue = inputValue + "0";
                        break;
                    case DOWN:
                        key = 2;
                        break;
                }
                break;
            case 11:
                switch (action) {
                    case LEFT:
                        key = 3;
                        break;
                    case RIGHT:
                        key = 1;
                        break;
                    case SELECT:
                        if (inputValue.length() > 0)
                            inputValue = inputValue.substring(0, inputValue.length() - 1);
                        break;
                }
        }
	return this;
    }

    public void draw (Graphics g, Panel p) {
	    g.drawImage (keysImage, 0, 0, p);
            g.setColor(new Color(247, 204, 139));
	    g.drawRect ((int)keypadArea[key].getX(),
			(int)keypadArea[key].getY(),
			(int)keypadArea[key].getWidth(),
			(int)keypadArea[key].getHeight());
	    g.setColor (new Color (0, 0, 0));
	    g.setFont(inputfont);
	    if (inputValue.compareTo("") == 0)
		g.drawString ("Input phone number", 234, 145);
	    else 
		g.drawString (inputValue+"_", 234, 145);
    }
}
