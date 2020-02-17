package com.maya.puc.device.cheapstereo;

/**
 * A class of messages relating to CSConnection objects.
 *
 * @author Joseph Hughes
 * @version $Id: CSConnectionEvent.java,v 1.2 2002/03/29 00:28:52 maya Exp $
 */

public abstract class CSConnectionEvent {
    public static class MessagesWaiting extends CSConnectionEvent {
        private int messageCount;

        public MessagesWaiting(int _messageCount) {
            messageCount = _messageCount;
        }

        public int getMessageCount() {
            return messageCount;
        }
    }

    public static class Disconnected extends CSConnectionEvent {
        public Disconnected() {
        }
    }
}
