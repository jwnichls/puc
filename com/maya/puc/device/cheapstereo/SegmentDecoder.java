/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Feb 2, 2002
 * Time: 3:07:00 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.maya.puc.device.cheapstereo;

public class SegmentDecoder {
    public static int decodeDigit(boolean a, boolean b, boolean c, boolean d, boolean e, boolean f, boolean g) {
        if (a && b && c && d && e && f && !g)
            return 0;
        else if (!a && b && c && !d && !e && !f && !g)
            return 1;
        else if (a && b && !c && d && e && !f && g)
            return 2;
        else if (a && b && c && d && !e && !f && g)
            return 3;
        else if (!a && b && c && !d && !e && f && g)
            return 4;
        else if (a && !b && c && d && !e && f && g)
            return 5;
        else if (a && !b && c && d && e && f && g)
            return 6;
        else if (a && b && c && !d && !e && f && !g)
            return 7;
        else if (a && b && c && d && e && f && g)
            return 8;
        else if (a && b && c && d && !e && f && g)
            return 9;
        else
            return -1;
    }
}
