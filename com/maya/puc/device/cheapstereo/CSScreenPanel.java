package com.maya.puc.device.cheapstereo;

import com.maya.puc.common.FauxEform;

import java.awt.*;
import java.net.URL;

/**
 * An AWT panel that displays the contents of the stereo screen.
 *
 * @author Joseph Hughes
 * @version $Id: CSScreenPanel.java,v 1.2 2002/03/29 00:28:52 maya Exp $
 */

public class CSScreenPanel extends Panel {

    private String backgroundImageName = "stereo-base.jpg";
    private Image backgroundImage = null;

    private String litImageName = "stereo-base-lit.jpg";
    private Image litImage = null;

    private CSMessage.ScreenDump state = null;
    private Image buffer = null;
    private Image buffer2 = null;
    private Rectangle segment[][] = null;
    private Dimension prefSize = new Dimension(50, 50);
    Font f = null;
    FontMetrics fm = null;

    public CSScreenPanel() {
        this.setSkin(new FauxEform());

        segment = new Rectangle[6][16];
        segment[2][14] = new Rectangle(186, 84, 12, 10); // am
        segment[2][15] = new Rectangle(198, 85, 113, 8); // pm
        segment[3][7] = new Rectangle(186, 107, 6, 16); // colon
        segment[2][7] = new Rectangle(143, 113, 9, 3); // minus
        segment[5][1] = new Rectangle(308, 75, 27, 7);
        segment[5][2] = new Rectangle(308, 82, 28, 6);
        segment[5][3] = new Rectangle(298, 110, 7, 9);
        segment[5][4] = new Rectangle(298, 97, 6, 9);
        segment[5][5] = new Rectangle(320, 113, 6, 9);
        segment[5][6] = new Rectangle(320, 101, 6, 8);
        segment[5][7] = new Rectangle(321, 89, 4, 7);
        segment[5][8] = new Rectangle(289, 88, 20, 8);
        segment[5][11] = new Rectangle(289, 113, 9, 10);
        segment[5][12] = new Rectangle(289, 100, 9, 11);
        segment[5][13] = new Rectangle(311, 118, 10, 8);
        segment[5][14] = new Rectangle(311, 106, 10, 7);
        segment[5][15] = new Rectangle(311, 92, 10, 9);

        // 1st minutes digit
        segment[2][0] = new Rectangle(154, 103, 11, 3);
        segment[2][1] = new Rectangle(162, 106, 4, 8);
        segment[2][2] = new Rectangle(152, 106, 4, 8);
        segment[2][3] = new Rectangle(154, 113, 9, 3);
        segment[2][4] = new Rectangle(161, 115, 4, 8);
        segment[2][5] = new Rectangle(151, 115, 4, 8);
        segment[2][6] = new Rectangle(152, 123, 11, 3);
        segment[2][9] = new Rectangle(168, 76, 19, 7);
        segment[2][10] = new Rectangle(188, 76, 9, 7);
        segment[2][11] = new Rectangle(198, 76, 12, 7);
        segment[2][13] = new Rectangle(166, 86, 20, 8);
        segment[2][15] = new Rectangle(198, 85, 13, 8);

        // 2nd minutes digit
        segment[3][0] = new Rectangle(196, 102, 11, 4);
        segment[3][1] = new Rectangle(203, 106, 5, 8);
        segment[3][2] = new Rectangle(194, 105, 5, 9);
        segment[3][3] = new Rectangle(196, 113, 8, 3);
        segment[3][4] = new Rectangle(203, 115, 4, 8);
        segment[3][5] = new Rectangle(193, 114, 4, 10);
        segment[3][6] = new Rectangle(194, 122, 11, 4);

        // 1st seconds digit
        segment[3][8] = new Rectangle(174, 102, 12, 4);
        segment[3][9] = new Rectangle(183, 106, 4, 8);
        segment[3][10] = new Rectangle(173, 106, 4, 8);
        segment[3][11] = new Rectangle(175, 113, 9, 3);
        segment[3][12] = new Rectangle(182, 115, 4, 8);
        segment[3][13] = new Rectangle(172, 115, 4, 8);
        segment[3][14] = new Rectangle(173, 123, 11, 3);
        segment[3][15] = new Rectangle(206, 121, 5, 6);

        // 2nd seconds digit
        segment[4][0] = new Rectangle(219, 103, 11, 3);
        segment[4][1] = new Rectangle(227, 106, 4, 8);
        segment[4][2] = new Rectangle(217, 106, 4, 8);
        segment[4][3] = new Rectangle(219, 113, 9, 3);
        segment[4][4] = new Rectangle(226, 115, 4, 8);
        segment[4][5] = new Rectangle(216, 115, 4, 8);
        segment[4][6] = new Rectangle(217, 123, 11, 3);
        segment[4][8] = new Rectangle(241, 110, 13, 6);
        segment[4][9] = new Rectangle(241, 118, 13, 6);
        segment[4][10] = new Rectangle(217, 75, 37, 13);
        segment[4][12] = new Rectangle(231, 88, 11, 7);
        segment[4][13] = new Rectangle(243, 88, 11, 7);

        segment[0][8] = new Rectangle(82, 89, 17, 6);
        segment[0][0] = new Rectangle(259, 74, 23, 15); // play
        segment[0][1] = new Rectangle(260, 93, 22, 7);
        segment[0][2] = new Rectangle(265, 111, 13, 6);
        segment[0][3] = new Rectangle(265, 104, 13, 7);
        segment[0][4] = new Rectangle(265, 117, 13, 6);
        segment[0][5] = new Rectangle(259, 100, 6, 27);
        segment[5][0] = new Rectangle(287, 73, 15, 15); // pause
        segment[1][7] = new Rectangle(107, 74, 36, 11); // tuner
        segment[1][15] = new Rectangle(108, 84, 28, 12); // tape
        segment[2][8] = new Rectangle(143, 74, 24, 11); // aux
        segment[2][12] = new Rectangle(147, 85, 16, 11); // cd

        segment[1][0] = new Rectangle(125, 98, 13, 4);
        segment[1][1] = new Rectangle(134, 102, 5, 10);
        segment[1][2] = new Rectangle(123, 101, 4, 11);
        segment[1][3] = new Rectangle(125, 111, 10, 3);
        segment[1][4] = new Rectangle(133, 113, 4, 10);
        segment[1][5] = new Rectangle(121, 113, 5, 10);
        segment[1][6] = new Rectangle(122, 123, 13, 3);

        segment[1][8] = new Rectangle(107, 99, 11, 3);
        segment[1][9] = new Rectangle(115, 102, 5, 10);
        segment[1][10] = new Rectangle(104, 102, 4, 10);
        segment[1][11] = new Rectangle(106, 111, 10, 3);
        segment[1][12] = new Rectangle(114, 113, 5, 10);
        segment[1][13] = new Rectangle(102, 113, 5, 10);
        segment[1][14] = new Rectangle(104, 122, 12, 4);


    }

    public void setState(CSMessage.ScreenDump _state) {
        state = _state;
//        System.out.println(state);
        render();
        repaint();
    }

    public void update(Graphics g) {
        paint(g);
    }

    public Dimension getPreferredSize() {
        return prefSize;
    }

    public void render() {
//		System.out.println("render");
        if ((buffer == null) || (buffer2 == null)) {
            buffer = createImage(prefSize.width, prefSize.height);
            buffer2 = createImage(prefSize.width, prefSize.height);

            if ((buffer == null) || (buffer2 == null))
                return;
        }


        Graphics g = buffer.getGraphics();

        g.drawImage(backgroundImage, 0, 0, this);
        if (state != null) {
            for (int block = 5; block >= 0; block--) {
                for (int seg = 15; seg >= 0; seg--) {
                    if ((segment[block][seg] != null) &&
                            state.isLit(block, seg)) {
                        g.setClip(segment[block][seg]);
                        g.drawImage(litImage, 0, 0, this);
                    }
                }
            }
        }

        Graphics g2 = buffer2.getGraphics();
        g2.drawImage(buffer, 0, 0, this);
    }


    public void paint(Graphics g) {
//		System.out.println("paint");
        if (buffer2 == null) {
            render();
        }
        g.drawImage(buffer2, 0, 0, this);
    }

    public void setSkin(FauxEform skin) {
        backgroundImageName = skin.getStringAttr("backgroundImageName", backgroundImageName);
        litImageName = skin.getStringAttr("litImageName", litImageName);

        URL url;
        Toolkit tk = Toolkit.getDefaultToolkit();
        try {
            MediaTracker mt = new MediaTracker(this);
            int id = 0;
            url = getClass().getResource(backgroundImageName);
            backgroundImage = tk.getImage(url);
            mt.addImage(backgroundImage, id++);
            url = getClass().getResource(litImageName);
            litImage = tk.getImage(url);
            mt.addImage(litImage, id++);
            mt.waitForAll();
        } catch (Exception e) {
            System.err.println(e);
        }
        prefSize = new Dimension(backgroundImage.getWidth(this), backgroundImage.getHeight(this));

        render();
        repaint();
    }
}