package com.maya.puc.device.cheapstereo;


/**
 * A listener interface for CSConnectionEvent messages.
 *
 * @author Joseph Hughes
 * @version $Id: CSConnectionListener.java,v 1.2 2002/03/29 00:28:52 maya Exp $
 */

public interface CSConnectionListener extends java.util.EventListener {
    public void messagesWaiting(CSConnectionEvent.MessagesWaiting e);

    public void disconnected(CSConnectionEvent.Disconnected e);
}