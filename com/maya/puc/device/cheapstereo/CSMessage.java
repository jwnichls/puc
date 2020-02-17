package com.maya.puc.device.cheapstereo;


/**
 *
 * @author Joseph Hughes
 * @version $Id: CSMessage.java,v 1.2 2002/03/29 00:28:52 maya Exp $
 */

public abstract class CSMessage {
    public static final byte BLOCK_0 = 0x30;

    /**
     * Attempts to determine the type of message encoded in byte buffer
     * _data starting at offset _offset, and to decode it and return the
     * appropriate type of message.
     *
     * @param _data The byte buffer containing a serialized message to
     *              be decoded.
     * @param _offset The index of the first byte of the message to be
     *                decoded.  This is useful when _data contains
     *                a sequence of several serialized messages.
     * @param _valid The number of valid bytes in _data -- the index of the
     *               first invalid byte in _data.
     *
     * @return The message decoded from the buffer, or null in the
     *         case of bad parameter input data.
     */

    public static CSMessage decode(byte[] _data, int _offset, int _valid) {
        if ((_data.length <= 0) || (_offset >= _data.length) || (_offset < 0) ||
                (_valid > _data.length) || (_valid < 0))
            return null;

        CSMessage msg = null;
        if (_data[0] == BLOCK_0)
            msg = new ScreenDump(_data, _offset, _valid);

        if ((msg == null) || (!msg.isValid()))
            msg = new Unknown(_data, _offset, _valid);

        return msg;
    }

    /**
     * Encodes this message for transmission over the wire, as an array of
     * bytes.  The number of bytes that the encoding will require can be
     * determined with getLength().
     *
     * @return An array of bytes representing the encoded version of this message;
     *         null if this message is not well-formed, as determined by isValid().
     *
     * @see #getLength()
     * @see #isValid()
     */

    public abstract byte[] serialization();

    /**
     * Determines whether a message is well-formed.  serialization() will not
     * attempt to encode this message if isValid() returns false.  The programmer
     * should call this function on a newly decoded message to verify that there
     * were no problems in the decoding.
     *
     * @return true if the message is well-formed, false otherwise.
     *
     * @see #serialization()
     */

    public abstract boolean isValid();

    /**
     * Determines whether a message is equivalent to another message.
     *
     * @return true if the parameter is equivalent to the current message, false otherwise.
     */

    public abstract boolean equals(CSMessage msg);

    /**
     * Returns the serialized length in bytes of this message.
     *
     * @return The number of bytes that this message will occupy when
     *         encoded for transmission by serialization();
     *
     * @see #serialization()
     */

    public abstract int getLength();

    /**
     * Converts the message to human-readable String form for debugging.
     *
     * @return A String that represents the contents of this message;
     *         may contain newline characters.
     */

    public abstract String toString();

    /**
     * Converts a char array whose end is padded with extra zeros to a
     * normal string, since "new String(array)" seems to include some
     * of the padding zeros, which is incorrect behavior.
     *
     * @param array the array of characters to be converted into a
     *              proper string.
     *
     * @return A String that represents the contents of array.
     */

    public static String charArrayToString(char[] array) {
        if (array == null)
            return null;

        int last = 0;
        while ((last < array.length) && (array[last] != 0))
            last++;
        return new String(array, 0, last);
    }

    /**
     * Converts a byte array containing an LSB-ordered integer into
     * a Java int.
     *
     * @param array the array of bytes containing a 1, 2, or 4-byte LSB
     *              integer.
     *
     * @returns An integer containing the decoded value, -1 if an error
     *          occurred.
     */

    public static int lsbByteArrayToInt(byte[] array) {
        int output = -1;

        if (array == null)
            return output;

        if (array.length > 0)
            output = 0;
        output |= (array[0]) & (long) 0xFF;
        if (array.length > 1)
            output |= (array[1] << 8) & (long) 0xFF00;
        if (array.length > 2) {
            output |= (array[2] << 16) & (long) 0xFF0000;
            output |= (array[3] << 24) & (long) 0xFF000000;
        }
        return output;
    }

    public static byte[] intToLsbByteArray(int num, int len) {
        byte[] array = null;

        if ((len == 4) || (len == 2) || (len == 1)) {
            array = new byte[len];
            int idx = 0;

            array[0] = (byte) (num & (long) 0xFF);
            if (len == 1) return array;
            array[1] = (byte) ((num & (long) 0xFF00) >> 8);
            if (len == 2) return array;
            array[2] = (byte) ((num & (long) 0xFF0000) >> 16);
            array[3] = (byte) ((num & (long) 0xFF000000) >> 24);
            return array;
        } else {
            return array;
        }
    }

    public static String dumpBytes(byte[] _data, int _offset, int _valid) {
        String dump = "";
        for (int i = 0; i < _valid - _offset; i++) {
            dump += i + ":" + Integer.toHexString(_data[i + _offset] & 0xFF) + "(";
            if (_data[i + _offset] != 0)
                dump += (char) _data[i + _offset];
            dump += ") ";
        }

        return dump;
    }

    /**
     * Represents a message of unknown type; it encapsulates the sequence
     * of bytes that ArqMessage.decode() was unable to recognize as a known
     * message type.  This type of message is always "valid" (as it's just
     * a sequence of bytes), and will serialize to the same sequence of bytes
     * that it received as the input to its constructor.
     */

    public static class Unknown extends CSMessage {
        private byte[] data = null;
        private boolean valid = true;
        private int useless_bytes = 1;

        /**
         * Constructs an instance of this message from its serialized "wire"
         * form.
         *
         * @param _data A byte array containing a serialized message.
         * @param _offset The index of the first byte of the serialized
         *                message.
         * @param _valid The number of valid bytes in _data -- the index of the
         *               first invalid byte in _data.
         */

        public Unknown(byte[] _data, int _offset, int _valid) {
            data = new byte[_valid - _offset];
            for (int i = _offset; i < _valid; i++)
                data[i - _offset] = _data[i];

            int i = 0;
            while ((i < data.length) && (data[i] != 0x0))
                i++;

            useless_bytes = i - 1;
            useless_bytes = 1;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean equals(CSMessage msg) {
            return false;
        }

        public int getLength() {
            return useless_bytes;
        }

        public String toString() {
            String msg = "CSMessage.Unknown" + (valid?" (valid)":" (invalid)");
            msg += "\n\tContents";
            if (data == null)
                msg += ": Null";
            else {
                msg += " (" + data.length + "): ";
                int max = Math.min(data.length, 20);
                for (int idx = 0; idx < max; idx++) {
                    msg += Integer.toHexString((int) data[idx] & 0xFF) + " ";
                }
                if (data.length > max)
                    msg += "...";
/*
				msg += "\n\t(";
				for (int idx = 0; idx < data.length; idx++)  {
					if (Character.isISOControl((char)data[idx]) || (data[idx] == 0))
						System.out.print('?');
					else
						System.out.print((char)data[idx]);
				}
				System.out.println(')');
*/
            }
            return msg;
        }

        public byte[] serialization() {
            return data;
        }
    }

    /**
     * Represents a "connection closing" message, to be sent to the ARQ over
     * TCP right before the TCP socket is closed. If this message is not sent,
     * the ARQ may exhibit erratic behavior.
     */

    public static class ScreenDump extends CSMessage {
        private static int LENGTH = 24;
        private boolean valid = false;
        private byte[] data = null;

        /**
         * Constructs an instance of this message from its serialized "wire"
         * form.
         *
         * @param _data A byte array containing a serialized message.
         * @param _offset The index of the first byte of the serialized
         *                message.
         * @param _valid The number of valid bytes in _data -- the index of the
         *               first invalid byte in _data.
         */

        public ScreenDump(byte[] _data, int _offset, int _valid) {
            if (((_valid - _offset) >= LENGTH) && (_data[_offset] == BLOCK_0)) {

                data = new byte[LENGTH];
                for (int i = _offset; i < LENGTH; i++)
                    data[i - _offset] = _data[i];

                int sum = 6;
                for (int i = _offset; i < LENGTH; i++)
                    sum = sum + _data[i];

                if ((sum & 0xFF) == 0)
                    valid = true;
            }
        }

        /**
         * Constructs a new instance of this message.
         */

        public ScreenDump() {
            valid = true;
        }

        public boolean isValid() {
            return valid;
        }

        public int getLength() {
            return LENGTH;
        }

        public byte[] getData() {
            return data;
        }

        public boolean isLit(int block, int bit) {
            if (block == 5)
                block = 5;
            else if (block == 0)
                block = 4;
            else
                block--;
            int offset = 4 * (block) + 1;
            if (bit < 8) {
                offset += 1;
            } else {
                bit -= 8;
            }

            return ((data[offset] & (1 << bit)) != 0);
        }


        public String toString() {
            String msg = "CSMessage.ScreenDump" + (valid?" (valid)":" (invalid)");
            msg += "\n\tContents";
            if (data == null)
                msg += ": Null";
            else {
                msg += " (" + data.length + "): ";
                int max = Math.min(data.length, LENGTH);
                for (int idx = 0; idx < max; idx++) {
                    msg += Integer.toHexString((int) data[idx] & 0xFF) + " ";
                }
                if (data.length > max)
                    msg += "...";
/*
			msg += "\n\t(";
			for (int idx = 0; idx < data.length; idx++)  {
				if (Character.isISOControl((char)data[idx]) || (data[idx] == 0))
					System.out.print('?');
				else
					System.out.print((char)data[idx]);
			}
			System.out.println(')');
*/
            }
            return msg;
        }

        public boolean equals(CSMessage msg) {
            if (!(msg instanceof ScreenDump)) return false;
            if (msg == null) return false;

            byte msgdata[] = ((ScreenDump) msg).serialization();
            if (data.length != msgdata.length) return false;
            for (int i = 0; i < msgdata.length; i++) {
                if (data[i] != msgdata[i]) return false;
            }
            return true;
        }

        public byte[] serialization() {
            byte[] ser = data;
            return ser;
        }
    }

    /**
     * Represents a message containing the equivalent of a remote control
     * button press that can be sent via TCP to the ARQ.
     */

    public static class RemoteCommand extends CSMessage {
        private static final int LENGTH = 1;

        // for quick sanity checking

        public static final byte MIN_CMD = 0x30;
        public static final byte MAX_CMD = 0x45;

        public static final byte CMD_REPEAT = 0x30;
        public static final byte CMD_SLEEP = 0x31;
        public static final byte CMD_MEMORY = 0x32;
        public static final byte CMD_TIMER = 0x33;
        public static final byte CMD_BAND = 0x34;
        public static final byte CMD_TUNE_DOWN = 0x36;
        public static final byte CMD_TUNE_UP = 0x35;
        public static final byte CMD_X_BASS = 0x37;
        public static final byte CMD_FUNCTION = 0x38;
        public static final byte CMD_WIDE = 0x39;
        public static final byte CMD_EJECT = 0x3A;
        public static final byte CMD_DISC = 0x3B;
        public static final byte CMD_PREV = 0x42;
        public static final byte CMD_NEXT = 0x41;
        public static final byte CMD_VOL_UP = 0x3C;
        public static final byte CMD_VOL_DOWN = 0x3D;
        public static final byte CMD_STOP = 0x3E;
        public static final byte CMD_PLAY = 0x3F;
        public static final byte CMD_POWER = 0x40;
        public static final byte CMD_HOLD_DOWN = 0x43;
        public static final byte CMD_SCAN_UP = 0x44;
        public static final byte CMD_SCAN_DOWN = 0x45;

        private boolean valid = false;
        private byte command = 0x0;

        /**
         * Constructs an instance of this message from its serialized "wire"
         * form.
         *
         * @param _data A byte array containing a serialized message.
         * @param _offset The index of the first byte of the serialized
         *                message.
         * @param _valid The number of valid bytes in _data -- the index of the
         *               first invalid byte in _data.
         */

        public RemoteCommand(byte[] _data, int _offset, int _valid) {
            if ((_valid - _offset) >= LENGTH) {
                command = _data[_offset];
                valid = isValidCommand(command);
            }
        }

        /**
         * Constructs an instance of this message.
         *
         * @param _command A command code -- for legibility, the programmer should
         *                 use one of the B_* fields from this class.
         */

        public RemoteCommand(byte _command) {
            command = _command;
            valid = isValidCommand(command);
        }

        private boolean isValidCommand(byte _command) {
            return ((_command >= MIN_CMD) && (_command <= MAX_CMD));
        }

        public boolean isValid() {
            return valid;
        }

        public int getLength() {
            return LENGTH;
        }

        public boolean equals(CSMessage msg) {
            if (msg instanceof RemoteCommand) {
                if (((RemoteCommand) msg).getCommand() == command) {
                    return true;
                }
            }

            return false;
        }

        public String toString() {
            String msg = "CSMessage.RemoteCommand" + (valid?" (valid)":" (invalid)");
            if (valid) {
                msg += "\n\tCommand: " + command;
            }
            return msg;
        }

        /**
         * Gets the command specified in this message.
         *
         * @return The command code in this message, should correspond to one
         *         of the B_* public fields in this class.
         */

        public byte getCommand() {
            return command;
        }

        public byte[] serialization() {
            if (!valid)
                return null;

            byte[] ser = {command};
            return ser;
        }
    }
}
