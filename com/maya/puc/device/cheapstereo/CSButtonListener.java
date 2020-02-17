package com.maya.puc.device.cheapstereo;

/**
 * Provides an interface for classes who are interested in remote
 * control codes.
 *
 * @author Joseph Hughes
 * @version $Id: CSButtonListener.java,v 1.2 2002/03/29 00:28:51 maya Exp $
 */

public interface CSButtonListener extends java.util.EventListener {

    /**
     * Message receiving function.
     *
     * @param button The code of the button pressed.
     */

    public void buttonPressed(byte button);
}

