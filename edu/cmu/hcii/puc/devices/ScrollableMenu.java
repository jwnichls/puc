/**
   @Pegeen Shen
*/
package edu.cmu.hcii.puc.devices;

import java.awt.*;

public interface ScrollableMenu {
    public void moveUp();
    public void moveDown();
    public void draw(Graphics g, Panel p);
}
