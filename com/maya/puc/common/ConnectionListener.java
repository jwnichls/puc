package com.maya.puc.common;


/**
 * A listener interface for ConnectionEvent messages.
 *
 * @author Joseph Hughes
 * @version $Id: ConnectionListener.java,v 1.4 2002/07/30 00:54:55 jeffreyn Exp $
 */

public interface ConnectionListener extends java.util.EventListener {
    public void messageReceived(ConnectionEvent.MessageReceived e);
    public void connectionLost(ConnectionEvent.ConnectionLost e);
    public void connectionRegained(ConnectionEvent.ConnectionRegained e);
}
