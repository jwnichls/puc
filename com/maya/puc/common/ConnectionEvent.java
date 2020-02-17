package com.maya.puc.common;


/**
 * A class of messages relating to Connection objects.
 *
 * Update 07/29/2002 (JWN): Added two new events, ConnectionLost and
 * ConnectionRegained, that are sent when the Client loses connection
 * with the server and later regains it.  It is expected that the
 * ConnectionRegained event will allow the client to send a new
 * FullStateRequest, for example.
 *
 * @author Joseph Hughes
 * @version $Id: ConnectionEvent.java,v 1.4 2002/07/30 00:54:55 jeffreyn Exp $
 */

public abstract class ConnectionEvent {
    public static class MessageReceived extends ConnectionEvent {
        private Connection conn;
        private Message msg;

        public MessageReceived(Connection _conn, com.maya.puc.common.Message _msg) {
            conn = _conn;
            msg = _msg;
        }

        public Connection getConnection() {
            return conn;
        }

        public com.maya.puc.common.Message getMessage() {
            return msg;
        }
    }

    public static class ConnectionLost extends ConnectionEvent {
        private Connection conn;

        public ConnectionLost(Connection _conn) {
            conn = _conn;
        }

        public Connection getConnection() {
            return conn;
        }
    }

    public static class ConnectionRegained extends ConnectionEvent {
        private Connection conn;

        public ConnectionRegained(Connection _conn) {
            conn = _conn;
        }

        public Connection getConnection() {
            return conn;
        }
    }
}

