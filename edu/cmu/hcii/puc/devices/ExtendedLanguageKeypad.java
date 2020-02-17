/**
   @Pegeen Shen
*/
package edu.cmu.hcii.puc.devices;

import java.awt.*;

public class ExtendedLanguageKeypad implements Keypad {

    Image keysImage;
    Image engKeysImage;
    Image numKeysImage;
    Image houseNoImage;
    Image okayImage;
    String inputValue = new String();
    String defaultDispString;
    Rectangle keypadArea[] = {
	new Rectangle (0, 0, 0, 0),
	new Rectangle (144, 172, 36, 25),
	new Rectangle (186, 172, 36, 25),
	new Rectangle (227, 172, 36, 25),
	new Rectangle (269, 172, 36, 25),
	new Rectangle (311, 172, 36, 25),
	new Rectangle (353, 172, 36, 25),
	new Rectangle (396, 172, 36, 25),
	new Rectangle (438, 172, 36, 25),
	new Rectangle (144, 202, 36, 25),
	new Rectangle (186, 202, 36, 25),
	new Rectangle (227, 202, 36, 25),
	new Rectangle (269, 202, 36, 25),
	new Rectangle (311, 202, 36, 25),
	new Rectangle (353, 202, 36, 25),
	new Rectangle (396, 202, 36, 25),
	new Rectangle (438, 202, 36, 25),
	new Rectangle (144, 232, 36, 25),
	new Rectangle (186, 232, 36, 25),
	new Rectangle (227, 232, 36, 25),
	new Rectangle (269, 232, 36, 25),
	new Rectangle (311, 232, 36, 25),
	new Rectangle (353, 232, 36, 25),
	new Rectangle (396, 232, 36, 25),
	new Rectangle (438, 232, 36, 25),
	new Rectangle (144, 261, 36, 25),
	new Rectangle (186, 261, 36, 25),
	new Rectangle (488, 172, 54, 25),
	new Rectangle (227, 261, 36, 25),
	new Rectangle (269, 261, 36, 25),
	new Rectangle (311, 261, 36, 25),
	new Rectangle (361, 261, 55, 25),
	new Rectangle (420, 261, 55, 25),
        new Rectangle (489, 249, 53, 34),
    };

    int key = 1;
    Font inputfont = new Font("SansSerif", Font.BOLD, 17);
    int inputX, inputY;

    public ExtendedLanguageKeypad(Image i1, Image i2, Image i3, 
                                  Image okay,
                                  String s, String value, int x, int y) {
	keysImage = i1;
	engKeysImage = i2;
	numKeysImage = i3;
        okayImage = okay;
	defaultDispString = s;
	inputValue = value;
	inputX = x;
	inputY = y;
    }
    public ExtendedLanguageKeypad(Image i1, Image i2, Image i3, Image i4,
                                  Image okay,
                                  String s, String value, int x, int y) {
	keysImage = i1;
	engKeysImage = i2;
	numKeysImage = i3;
        houseNoImage = i4;
        okayImage = okay;
	defaultDispString = s;
	inputValue = value;
	inputX = x;
	inputY = y;
    }

    public String getInput() {
	return inputValue;
    }

    public Keypad doAction(int action) {
	    switch (key)
		{
		case 1:
		    switch (action)
			{
			case UP:
			    key = 25;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00C0';
			    break;
			case RIGHT:
			    key = 2;
			    break;
			case DOWN:
			    key = 9;
			    break;
			case LEFT:
			    key = 27;
			    break;
			}
		    break;
		case 2:
		    switch (action)
			{
			case UP:
			    key = 26;
			    break;
			case DOWN:
			    key = 10;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00C1';
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
		    switch (action)
			{
			case UP:
			    key = 28;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00C2';
			    break;
			case RIGHT:
			    key = 4;
			    break;
			case LEFT:
			    key = 2;
			    break;
			case DOWN:
			    key = 11;
			    break;
			}
		    break;
		case 4:
		    switch (action)
			{
			case UP:
			    key = 29;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00C3';
			    break;
			case RIGHT:
			    key = 5;
			    break;
			case LEFT:
			    key = 3;
			    break;
			case DOWN:
			    key = 12;
			    break;
			}
		    break;
		case 5:
		    switch (action)
			{
			case UP:
			    key = 30;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00C4';
			    break;
			case RIGHT:
			    key = 6;
			    break;
			case LEFT:
			    key = 4;
			    break;
			case DOWN:
			    key = 13;
			    break;
			}
		    break;
		case 6:
		    switch (action)
			{
			case UP:
			    key = 31;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00C5';
			    break;
			case RIGHT:
			    key = 7;
			    break;
			case LEFT:
			    key = 5;
			    break;
			case DOWN:
			    key = 14;
			    break;
			}
		    break;
		case 7:
		    switch (action)
			{
			case UP:
			    key = 31;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00C6';
			    break;
			case RIGHT:
			    key = 8;
			    break;
			case LEFT:
			    key = 6;
			    break;
			case DOWN:
			    key = 15;
			    break;
			}
		    break;
		case 8:
		    switch (action)
			{
			case UP:
			    key = 32;
			    break;
			case LEFT:
			    key = 7;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00C7';
			    break;
			case RIGHT:
			    key = 27;
			    break;
			case DOWN:
			    key = 16;
			    break;
			}
		    break;
		case 9:
		    switch (action)
			{
			case UP:
			    key = 1;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00c8';
			    break;
			case RIGHT:
			    key = 10;
			    break;
			case DOWN:
			    key = 17;
			    break;
			}
		    break;
		case 10:
		    switch (action)
			{
			case UP:
			    key = 2;
			    break;
			case LEFT:
			    key = 9;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00c9';
			    break;
			case RIGHT:
			    key = 11;
			    break;	
			case DOWN:
			    key = 18;
			    break;
			}
		    break;
		case 11:
		    switch (action)
			{
			case 0:
			    key = 3;
			    break;
			case 1:
			    key = 10;
			    break;
			case 2:
			    inputValue = inputValue + '\u00ca';
			    break;
			case 3:
			    key = 12;
			    break;
			case 4:
			    key = 19;
			    break;
			}
		    break;
		case 12:
		    switch (action)
			{
			case 0:
			    key = 4;
			    break;
			case 1:	
			    key = 11;
			    break;
			case 2:
			    inputValue = inputValue + '\u00cb';
			    break;
			case 3:
			    key = 13;
			    break;
			case 4:
			    key = 20;
			    break;
			}
		    break;
		case 13:
		    switch (action)
			{
			case 0:
			    key = 5;
			    break;
			case 1:
			    key = 12;
			    break;
			case 2:
			    inputValue = inputValue + '\u00cc';
			    break;
			case 3:
			    key = 14;
			    break;
			case 4:
			    key = 21;
			    break;
			}
		    break;
		case 14:
		    switch (action)
			{
			case 0:
			    key = 6;
			    break;
			case 1:
			    key = 13;
			    break;
			case 2:
			    inputValue = inputValue + '\u00cd';
			    break;
			case 3:
			    key = 15;
			    break;
			case 4:
			    key = 22;
			    break;
			}
		    break;
		case 15:
		    switch (action)
			{
			case 0:
			    key = 7;
			    break;
			case 1:
			    key = 14;
			    break;
			case 2:
			    inputValue = inputValue + '\u00ce';
			    break;
			case 3:
			    key = 16;
			    break;
			case 4:
			    key = 23;
			    break;
			}
		    break;
		case 16:
		    switch (action)
			{
			case 0:
			    key = 8;
			    break;
			case 1:
			    key = 15;
			    break;
			case 2:
			    inputValue = inputValue + '\u00cf';
			    break;
			case 4:
			    key = 24;
			    break;
			}
		    break;
		case 17:
		    switch (action)
			{
			case 0:
			    key = 9;
			    break;
			case 2:
			    inputValue = inputValue + '\u00d1';
			    break;
			case 3:
			    key = 18;
			    break;
			case 4:
			    key = 25;
			    break;
			}
		    break;
		case 18:
		    switch (action)
			{
			case 0:
			    key = 10;
			    break;
			case 1:
			    key = 17;
			    break;
			case 2:
			    inputValue = inputValue + '\u00d2';
			    break;
			case 3:
			    key = 19;
			    break;
			case 4:
			    key = 26;
			    break;
			}
		    break;
		case 19:
		    switch (action)
			{
			case 0:
			    key = 11;
			    break;
			case 1:
			    key = 18;
			    break;
			case 2:
			    inputValue = inputValue + '\u00d3';
			    break;
			case 3:
			    key = 20;
			    break;
			case 4:
			    key = 28;
			    break;
			}
		    break;
		case 20:
		    switch (action)
			{
			case 0:
			    key = 12;
			    break;
			case 1:
			    key = 19;
			    break;
			case 2:
			    inputValue = inputValue + '\u00d4';
			    break;
			case 3:
			    key = 21;
			    break;
			case 4:
			    key = 29;
			    break;
			}
		    break;
		case 21:
		    switch (action)
			{
			case 0:
			    key = 13;
			    break;
			case 1:
			    key = 20;
			    break;
			case 2:
			    inputValue = inputValue + '\u00d5';
			    break;
			case 3:
			    key = 22;
			    break;
			case 4:
			    key = 30;
			    break;
			}
		    break;
		case 22:
		    switch (action)
			{
			case 0:
			    key = 14;
			    break;
			case 1:
			    key = 21;
			    break;
			case 2:
			    inputValue = inputValue + '\u00d6';
			    break;
			case 3:
			    key = 23;
			    break;
			case 4:
			    key = 31;
			    break;
			}
		    break;
		case 23:
		    switch (action)
			{
			case 0:
			    key = 15;
			    break;
			case 1:
			    key = 22;
			    break;
			case 2:
			    inputValue = inputValue + '\u00d8';
			    break;
			case 3:
			    key = 24;
			    break;
			case 4:
			    key = 31;
			    break;
			}
		    break;
		case 24:	
		    switch (action)
			{
			case 0:
			    key = 16;
			    break;
			case 1:
			    key = 23;
			    break;
			case 2:
			    inputValue = inputValue + '\u00d9';
			    break;
			case 4:
			    key = 32;
			    break;
			}
		    break;
		case 25:
		    switch (action)
			{
			case 0:
			    key = 17;
			    break;
			case 1:
			    key = 33;
			    break;
			case 2:
			    inputValue = inputValue + '\u00da';
			    break;
			case 3:
			    key = 26;
			    break;
			case 4:
			    key = 1;
			    break;
			}
		    break;
		case 26:
		    switch (action)
			{
			case 0:
			    key = 18;
			    break;
			case 1:
			    key = 25;
			    break;
			case 2:
			    inputValue = inputValue + '\u00db';
			    break;
			case 3:
			    key = 28;
			    break;
			case 4:
			    key = 2;
			    break;
			}
		    break;
		case 27:
		    switch (action)
			{
                        case 0:
                        case 4:
                            key = 33;
                            break;
			case 1:
			    key = 8;
			    break;
			case 2:
			    if (inputValue.length() > 0)
				inputValue = inputValue.substring (0, inputValue.length() - 1);
			    break;
			case 3:
			    key = 1;
			    break;
			}
		    break;
		case 28:
		    switch (action)
			{
			case UP:
			    key = 19;
			    break;
			case LEFT:
			    key = 26;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00dc';
			    break;
			case RIGHT:
			    key = 29;
			    break;
			case DOWN:
			    key = 3;
			    break;
			}
		    break;
		case 29:
		    switch (action)
			{
			case UP:
			    key = 20;
			    break;
			case LEFT:
			    key = 28;
			    break;
			case SELECT:
			    inputValue = inputValue + '\u00dd';
			    break;
			case RIGHT:
			    key = 30;
			    break;
			case DOWN:
			    key = 4;
			    break;
			}
		    break;
		case 30:
		    switch (action)
			{
			case UP:
			    key = 21;
			    break;
			case LEFT:
			    key = 29;
			    break;
			case SELECT:
			    inputValue = inputValue + ' ';
			    break;
			case RIGHT:
			    key = 31;
			    break;
			case DOWN:
			    key = 5;
			    break;
			}
		    break;
		case 31:
		    switch (action)
			{
			case UP:
			    key = 22;
			    break;
			case LEFT:
			    key = 30;
			    break;
			case SELECT:
			    return goEnglish();
			case RIGHT:
			    key = 32;
			    break;
			case DOWN:
			    key = 6;
			    break;
			}
		    break;
		case 32:
		    switch (action)
			{
			case UP:
			    key = 24;
			    break;
			case LEFT:
			    key = 31;
			    break;
			case SELECT:
                            return goNumber();
			case DOWN:
			    key = 8;
			    break;
                        case RIGHT:
                            key = 33;
                            break;
			}
		    break;
                case 33:
                    switch (action)
                    {
                        case UP:
                        case DOWN:
                            key = 27;
                            break;
                        case LEFT:
                            key = 32;
                            break;
                        case RIGHT:
                            key = 25;
                            break;
                        case SELECT:
                            return doOkay();
                    }
		}
	    return this;
    }
    
    public Keypad doOkay() {
        if (houseNoImage != null)
            return new HouseNoKeypad(houseNoImage, this, inputValue);
        return this;
    }
 
    public Keypad goNumber() {
        return new NumberKeypad(numKeysImage, engKeysImage, keysImage,
                                houseNoImage, okayImage, defaultDispString,
                                inputValue, inputX,inputY);
    }
    
    public Keypad goEnglish() {
        return new EnglishKeypad(engKeysImage, keysImage, numKeysImage,
                                 houseNoImage, okayImage, defaultDispString,
                                 inputValue, inputX,inputY);
    }

	public void draw (Graphics g, Panel p) {
	    g.drawImage (keysImage, 0, 0, p);
            g.drawImage (okayImage, 494, 250, p);
	    g.setColor (new Color(247, 204, 139));
	    g.drawRect ((int)keypadArea[key].getX(),
			(int)keypadArea[key].getY(),
			(int)keypadArea[key].getWidth(),
			(int)keypadArea[key].getHeight());
	    g.setColor (new Color (0 ,0, 0));
	    g.setFont (inputfont);
	    if (inputValue.compareTo("") == 0)
		g.drawString (defaultDispString, inputX, inputY);
	    else 
		g.drawString (inputValue+"_", inputX, inputY);
	}
}
