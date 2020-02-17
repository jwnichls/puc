package com.maya.puc.device.cheapstereo;

import com.maya.puc.common.Device;
import com.maya.puc.common.StateListener;
import com.maya.puc.common.TextResource;

import java.awt.*;
import java.io.IOException;
import java.util.Vector;

public class CSDevice implements Device, CSConnectionListener, CSButtonListener {
    public static final int NUM_FM_PRESETS = 20;
    public static final int NUM_AM_PRESETS = 20;
    private static String name = "Audiophase Stereo";
    private Vector listeners = new Vector();
    private State currentState = null;
    private CSButtonListener buttonListener = null;
    private Vector pendingCommands = new Vector();
    private int lastKnownDisc = 0;
    private float[] FMPresetValue = new float[NUM_FM_PRESETS];
    private int[] AMPresetValue = new int[NUM_AM_PRESETS];
    public static final String S_POWER = "PowerState";
    public static final String S_OUTPUT = "ModeState";
    public static final String S_AM_STATION = "AMStation";
    public static final String S_FM_STATION = "FMStation";
    public static final String S_AM_PRESET = "AMPresetNumber";
    public static final String S_FM_PRESET = "FMPresetNumber";
    public static final String S_CD_RANDOM = "CDRandomState";
    public static final String S_CD_REPEAT = "CDRepeatState";
    public static final String S_RADIO_BAND = "RadioBandState";
    public static final String S_CD_PLAY_MODE = "CDPlayMode";
    public static final String S_CD_DISC = "CDDiscActive";
    public static final String S_XBASS = "XBassState";
    public static final String S_CD_TRACK = "CDTrackState";
    public static final String S_VOL_UP = "VolumeUp";
    public static final String S_VOL_DOWN = "VolumeDn";
    public static final String S_SEEK_FORWARD = "SeekForward";
    public static final String S_SEEK_REVERSE = "SeekReverse";
    public static final String S_LOAD_PRESETS = "LoadPresetValues";
    public static final String CONFIG_FILE = "audiophase.cfg";
    public static final String SPEC_FILE = "audiophase.xml";
    private int port = 5150;
    protected CSConnectionConfigurator cc = null;
    private CSConnection conn = null;
    private String status = null;
    private CSMessage oldMessage1 = null;
    private CSMessage oldMessage2 = null;
    private CSDeviceGUI gui = null;
    private boolean closeConnection = false;

    protected void sendToStereo(CSMessage msg) {
        try {
            if ((conn != null) && conn.isConnected()) {
                conn.send(msg);
            }
        } catch (IOException e) {
            System.err.println("Exception in CSDevice.sendToStereo(): " + e);
        }
    }

    public CSDevice() {
        cc = new CSConnectionConfigurator(new Frame(), CONFIG_FILE);
        gui = new CSDeviceGUI(this);
        buttonListener = this;
    }

    public CSDevice(CSButtonListener _buttonListener) {
        buttonListener = _buttonListener;
    }

    public void buttonPressed(byte cmd) {
        try {
            if (cmd != 0) {
                System.out.println("Sending Command " + cmd);
                sendToStereo(new CSMessage.RemoteCommand(cmd));
            }
        } catch (Exception ex) {
        }
    }

    private void addCommand(CSCommand cmd) {
        pendingCommands.addElement(cmd);
    }

    private CSCommand getNextCommand() {
        if (pendingCommands.size() <= 0) return null;
        return (CSCommand) (pendingCommands.elementAt(0));
    }

    private void discardFirstCommand() {
        if (pendingCommands.size() <= 0) return;
        pendingCommands.removeElementAt(0);
    }

    public void requestStateChange(String state, String value) {
        System.out.println("SCR: " + state + ", " + value);

        if (currentState == null)
            return;
        System.out.println("pre-foo");
        State st = currentState;

        if (state.equals(S_POWER))
            changePowerState(value, st);
        if (state.equals(S_XBASS))
            changeXBassState(value, st);
        if (state.equals(S_RADIO_BAND))
            changeRadioBand(value, st);
        if (state.equals(S_OUTPUT))
            changeOutputMode(value, st);
        if (state.equals(S_CD_PLAY_MODE))
            changeCDPlayMode(value, st);
        if (state.equals(S_CD_RANDOM))
            changeCDRandomState(value, st);
        if (state.equals(S_CD_REPEAT))
            changeCDRepeatMode(value, st);
        if (state.equals(S_CD_DISC))
            changeCDDiscActive(value, st);
        if (state.equals(S_FM_STATION))
            changeFMStation(value, st);
        if (state.equals(S_AM_STATION))
            changeAMStation(value, st);
        if (state.equals(S_FM_PRESET))
            changeFMPreset(value, st);
        if (state.equals(S_AM_PRESET))
            changeAMPreset(value, st);
    }

    private void changeFMStation(String value, State st) {
        float newFreq = Float.valueOf(value).floatValue();
        addCommand(new CSCommand.ChangeFMStation(buttonListener, newFreq, true));
    }

    private void changeAMStation(String value, State st) {
        int newFreq = Integer.valueOf(value).intValue();
        addCommand(new CSCommand.ChangeAMStation(buttonListener, newFreq, true));
    }

    private void changeFMPreset(String value, State st) {
        int newPreset = Integer.valueOf(value).intValue();
        addCommand(new CSCommand.ChangeFMPreset(buttonListener, newPreset, true));
    }

    private void changeAMPreset(String value, State st) {
        int newPreset = Integer.valueOf(value).intValue();
        addCommand(new CSCommand.ChangeAMPreset(buttonListener, newPreset, true));
    }

    private void changeCDDiscActive(String value, State st) {
        int newDisc = Integer.valueOf(value).intValue();
        addCommand(new CSCommand.ChangeDisc(buttonListener, newDisc));
    }

    private void changeCDRepeatMode(String value, State st) {
        int newRepeat = Integer.valueOf(value).intValue();
        addCommand(new CSCommand.ChangeRepeat(buttonListener, newRepeat));
    }

    private void changeRadioBand(String value, State st) {
        boolean newBand = Boolean.valueOf(value).booleanValue();
        addCommand(new CSCommand.ChangeBand(buttonListener, newBand));
    }

    private void changeXBassState(String value, State st) {
        boolean newBass = Boolean.valueOf(value).booleanValue();
        if (newBass != st.getXBassState()) {
            buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_X_BASS);
        }
    }

    private void changePowerState(String value, State st) {
        boolean newPower = Boolean.valueOf(value).booleanValue();
        addCommand(new CSCommand.ChangePower(buttonListener, newPower));
    }

    private void changeOutputMode(String value, State st) {
        int newMode = Integer.valueOf(value).intValue();
        addCommand(new CSCommand.ChangeMode(buttonListener, newMode));
    }

    private void changeCDPlayMode(String value, State st) {
        int newMode = Integer.valueOf(value).intValue();
        addCommand(new CSCommand.ChangePlay(buttonListener, newMode));
    }

    private void changeCDRandomState(String value, State st) {
        boolean newRandom = Boolean.valueOf(value).booleanValue();
        if (st.isInCDMode() && (newRandom != st.getCDRandomState())) {
            if (!st.isCDStopped()) {
                buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_STOP);
            }
            buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_TUNE_DOWN);
        }
    }

    public void requestFullState() {
        if (currentState == null)
            return;

        Vector changes = currentState.getAllStates();
        int i = 0;
        while (i < changes.size()) {
            dispatchStateEvent((String) changes.elementAt(i++),
                    (String) changes.elementAt(i++));
        }
    }

    public void update(CSMessage.ScreenDump msg) {
        State newState = new State(msg, lastKnownDisc, FMPresetValue, AMPresetValue);
        if (newState.getCDDiscActive() != 0) {
            lastKnownDisc = newState.getCDDiscActive();
        }
        FMPresetValue = (float[]) newState.FMPresetValue.clone();
        AMPresetValue = (int[]) newState.AMPresetValue.clone();

        Vector changes = newState.getChangedStates(currentState);
        int i = 0;
        while (i < changes.size()) {
            dispatchStateEvent((String) changes.elementAt(i++),
                    (String) changes.elementAt(i++));
        }

        if (changes.size() > 0) {
            System.out.println("State Changes: " + changes);
//            System.out.println(newState);
        }
        currentState = newState;

        if (pendingCommands.size() > 0)
            System.out.println("Commands: " + pendingCommands);
        CSCommand cmd = getNextCommand();
        if (cmd != null) {
            boolean done = cmd.execute(currentState);
            if (done) discardFirstCommand();
        }
    }

    public String getSpec() {
        return TextResource.readToString(this.getClass(), SPEC_FILE);
    }

    public String getName() {
        return name;
    }

    public void addStateListener(StateListener sl) {
        listeners.addElement(sl);
    }

    public void requestCommandInvoke(String command) {
        if (command.equals(S_SEEK_FORWARD)) {
            buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_SCAN_UP);
        } else if (command.equals(S_SEEK_REVERSE)) {
            buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_SCAN_DOWN);
        } else if (command.equals(S_VOL_UP)) {
            buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_VOL_UP);
        } else if (command.equals(S_VOL_DOWN)) {
            buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_VOL_DOWN);
        } else if (command.equals("CDNextTrack")) {
            buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_NEXT);
        } else if (command.equals("CDPrevTrack")) {
            buttonListener.buttonPressed(CSMessage.RemoteCommand.CMD_PREV);
        } else if (command.equals(S_LOAD_PRESETS)) {
            addCommand(new CSCommand.ChangePower(buttonListener, true));
            addCommand(new CSCommand.ChangeMode(buttonListener, 4));
            addCommand(new CSCommand.ChangeBand(buttonListener, true));
            addCommand(new CSCommand.ChangeFMPreset(buttonListener, 1, true));
            addCommand(new CSCommand.ChangeFMPreset(buttonListener, 20, false));
            addCommand(new CSCommand.ChangeBand(buttonListener, false));
            addCommand(new CSCommand.ChangeAMPreset(buttonListener, 1, true));
            addCommand(new CSCommand.ChangeAMPreset(buttonListener, 20, false));
        } else {
            System.err.println("Unknown Command Received: " + command);
        }
    }

    public void removeStateListener(StateListener sl) {
        listeners.removeElement(sl);
    }

    private void dispatchStateEvent(String state, String value) {
        StateListener l;

        for (int i = 0; i < listeners.size(); i++) {
            l = (StateListener) listeners.elementAt(i);
            l.stateChanged(getName(), state, value);
        }
    }

    public void configure() {
        cc.show();
        cc.saveSettings(CONFIG_FILE);
    }

    public boolean hasGUI() {
        return true;
    }

    public void setGUIVisibility(boolean isVisible) {
        gui.setVisibility(isVisible);
    }

    public boolean isGUIVisible() {
        return gui.isVisible();
    }

    public void start() {
        closeConnection = false;
        try {
            // initialize connection
            status = "Connecting...";
            dispatchStateEvent(null, null);

            conn = new CSConnection(cc.getConnIP(), cc.getConnPort());

            conn.addCSConnectionListener(this);
            conn.connect();
            if (conn.isConnected()) {
                status = null;
                dispatchStateEvent(null, null);
                System.out.println("Connected to stereo");
            } else {
                status = "Unable to connect.";
                dispatchStateEvent(null, null);
                System.out.println("Not connected to stereo. :[");
                return;
            }
        } catch (Exception e) {
            System.out.println("Exception in CSDevice.start(): " + e);
        }
    }

    public void stop() {
        if (conn != null) {
            closeConnection = true;
            conn.disconnect();
            updateStatus();
        }
    }

    public void updateStatus() {
        dispatchStateEvent(null, null);
    }

    public boolean isRunning() {
        if (conn == null) return false;
        return conn.isConnected();
    }

    public String getStatus() {
        return status;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void messagesWaiting(CSConnectionEvent.MessagesWaiting e) {
        CSMessage msg = conn.getMessage();
        while (msg != null) {
            if ((msg.equals(oldMessage1)) && (!msg.equals(oldMessage2)) && (msg instanceof CSMessage.ScreenDump)) {
                if (gui.isVisible()) {
                    gui.setState((CSMessage.ScreenDump) msg);
                }
                this.update((CSMessage.ScreenDump) msg);
//				System.out.println(msg);
            }
            oldMessage2 = oldMessage1;
            oldMessage1 = msg;
            msg = conn.getMessage();
        }
    }

    public void disconnected(CSConnectionEvent.Disconnected e) {
        if (closeConnection) return;
        do {
            System.out.println("Disconnecting...");
            conn.disconnect();
            updateStatus();
            System.out.println("Reconnecting...");
            conn.connect();
            updateStatus();
        } while (!conn.isConnected());
        System.out.println("Reconnected.");
    }

    protected class State {
        private boolean PowerState = false;
        private int OutputMode = 0;
        private int AMStation = 0;
        private float FMStation = 0.0f;
        private int CDPlayMode = 0;
        private int CDTrack = 0;
        private boolean RadioBandState = true;
        private boolean XBassState = false;
        private boolean CDRandomState = false;
        private int AMPreset = 0;
        private int FMPreset = 0;
        private int CDRepeatState = 0;
        private boolean DiscAvail[] = new boolean[5];
        private float FMPresetValue[] = new float[20];
        private int AMPresetValue[] = new int[20];
        private int CDDiscActive = 0;
        private int bigDigits[] = new int[2];
        private int mainDigits[] = new int[4];

        public State(CSMessage.ScreenDump dump, int lastKnownDisc, float[] FMPresetValue, int[] AMPresetValue) {
            this.FMPresetValue = (float[]) FMPresetValue.clone();
            this.AMPresetValue = (int[]) AMPresetValue.clone();

            decodeOutputModeAndPowerState(dump);
            decodeRadioBandState(dump);
            decodeDigits(dump);
            decodeCDPlayMode(dump);
            decodeFMStation(dump);
            decodeAMStation(dump);
            decodeXBassState(dump);
            decodeDiscAvailAndCDDiscActive(dump, lastKnownDisc);
            decodeCDRandomState(dump);
            decodeCDRepeatState(dump);
            decodePresets(dump);
            decodeCDTrack(dump);
        }

        private void decodeCDRepeatState(CSMessage.ScreenDump dump) {
            if (isInCDMode()) {
                if (dump.isLit(0, 1)) { // repeat
                    if (dump.isLit(0, 3)) { // one
                        if (dump.isLit(0, 4)) { // disc
                            CDRepeatState = 4; // one disc
                        } else {
                            CDRepeatState = 2; // one track
                        }
                    } else if (dump.isLit(0, 2)) { // all
                        if (dump.isLit(0, 4)) { // disc
                            CDRepeatState = 5; // all discs
                        } else {
                            CDRepeatState = 3; // all tracks
                        }
                    }

                } else {
                    CDRepeatState = 1; // off
                }
            }
        }

        private void decodeCDRandomState(CSMessage.ScreenDump dump) {
            if (isInCDMode()) {
                CDRandomState = dump.isLit(5, 2);
            }
        }

        private void decodeDiscAvailAndCDDiscActive(CSMessage.ScreenDump dump, int lastKnownDisc) {
            if (isInCDMode()) {
                for (int i = 0; i < DiscAvail.length; i++) {
                    DiscAvail[i] = dump.isLit(5, (7 - i));
                    if (!dump.isLit(5, (15 - i))) {
                        CDDiscActive = i + 1;
                    }
                }
                if (CDDiscActive == 0) {
                    CDDiscActive = lastKnownDisc;
                }
            }
        }

        private void decodeXBassState(CSMessage.ScreenDump dump) {
            if (PowerState) {
                XBassState = dump.isLit(4, 10);
            }
        }

        private void decodeAMStation(CSMessage.ScreenDump dump) {
            if (dump.isLit(4, 8)) {
                if (mainDigits[0] >= 0)
                    AMStation += 1000 * mainDigits[0];
                if (mainDigits[1] >= 0)
                    AMStation += 100 * mainDigits[1];
                if (mainDigits[2] >= 0)
                    AMStation += 10 * mainDigits[2];
                if (mainDigits[3] >= 0)
                    AMStation += mainDigits[3];
            }
        }

        private void decodeCDTrack(CSMessage.ScreenDump dump) {
            if (isInCDMode() && dump.isLit(0, 8)) {
                CDTrack = decodeBigNumber();
            }
        }

        private void decodePresets(CSMessage.ScreenDump dump) {
            if (isInFMMode()) {
                FMPreset = decodeBigNumber();
                if (FMPreset != 0) {
                    FMPresetValue[FMPreset - 1] = FMStation;
                }
            } else if (isInAMMode()) {
                AMPreset = decodeBigNumber();
                if (AMPreset != 0) {
                    AMPresetValue[AMPreset - 1] = AMStation;
                }
            }
        }

        private int decodeBigNumber() {
            int preset = 0;
            if (bigDigits[0] >= 0)
                preset += 10 * bigDigits[0];
            if (bigDigits[1] >= 0)
                preset += bigDigits[1];
            return preset;
        }

        private void decodeFMStation(CSMessage.ScreenDump dump) {
            if (dump.isLit(4, 9)) {
                FMStation = 0.0f;
                if (mainDigits[0] >= 0)
                    FMStation += 100f * mainDigits[0];
                if (mainDigits[1] >= 0)
                    FMStation += 10f * mainDigits[1];
                if (mainDigits[2] >= 0)
                    FMStation += 1f * mainDigits[2];
                if (mainDigits[3] >= 0)
                    FMStation += 0.1f * mainDigits[3];
            }
        }

        private void decodeCDPlayMode(CSMessage.ScreenDump dump) {
            if (isInCDMode()) {
                if (dump.isLit(0, 0)) {
                    CDPlayMode = 2; // playing
                } else if (dump.isLit(5, 0)) {
                    CDPlayMode = 3; // paused
                } else {
                    CDPlayMode = 1; // stopped
                }
            }
        }

        public boolean isCDStopped() {
            return (isInCDMode() && (CDPlayMode == 1));
        }

        public boolean isCDPlaying() {
            return (isInCDMode() && (CDPlayMode == 2));
        }

        public boolean isCDPaused() {
            return (isInCDMode() && (CDPlayMode == 3));
        }

        private void decodeOutputModeAndPowerState(CSMessage.ScreenDump dump) {
            // determine OutputMode
            if (dump.isLit(1, 15))
                OutputMode = 1;
            else if (dump.isLit(2, 12))
                OutputMode = 2;
            else if (dump.isLit(2, 8))
                OutputMode = 3;
            else if (dump.isLit(1, 7))
                OutputMode = 4;
            else
                OutputMode = -0;

            PowerState = (OutputMode > 0);
        }

        public boolean isInTapeMode() {
            return (OutputMode == 1);
        }

        public boolean isInCDMode() {
            return (OutputMode == 2);
        }

        public boolean isInAuxMode() {
            return (OutputMode == 3);
        }

        public boolean isInTunerMode() {
            return (OutputMode == 4);
        }

        private void decodeRadioBandState(CSMessage.ScreenDump dump) {
            if (dump.isLit(4, 12) || dump.isLit(4, 13)) {
                RadioBandState = dump.isLit(4, 13);
            }
        }

        public boolean isInFMMode() {
            return (isInTunerMode() && RadioBandState);
        }

        public boolean isInAMMode() {
            return (isInTunerMode() && !RadioBandState);
        }

        private void decodeDigits(CSMessage.ScreenDump dump) {
            bigDigits[0] = SegmentDecoder.decodeDigit(
                    dump.isLit(1, 8),
                    dump.isLit(1, 9),
                    dump.isLit(1, 12),
                    dump.isLit(1, 14),
                    dump.isLit(1, 13),
                    dump.isLit(1, 10),
                    dump.isLit(1, 11)
            );
            bigDigits[1] = SegmentDecoder.decodeDigit(
                    dump.isLit(1, 0),
                    dump.isLit(1, 1),
                    dump.isLit(1, 4),
                    dump.isLit(1, 6),
                    dump.isLit(1, 5),
                    dump.isLit(1, 2),
                    dump.isLit(1, 3)
            );
            mainDigits[0] = SegmentDecoder.decodeDigit(
                    dump.isLit(2, 0),
                    dump.isLit(2, 1),
                    dump.isLit(2, 4),
                    dump.isLit(2, 6),
                    dump.isLit(2, 5),
                    dump.isLit(2, 2),
                    dump.isLit(2, 3)
            );
            if (mainDigits[0] == -1) {
                mainDigits[0] = 0;
            }

            mainDigits[1] = SegmentDecoder.decodeDigit(
                    dump.isLit(3, 8),
                    dump.isLit(3, 9),
                    dump.isLit(3, 12),
                    dump.isLit(3, 14),
                    dump.isLit(3, 13),
                    dump.isLit(3, 10),
                    dump.isLit(3, 11)
            );
            mainDigits[2] = SegmentDecoder.decodeDigit(
                    dump.isLit(3, 0),
                    dump.isLit(3, 1),
                    dump.isLit(3, 4),
                    dump.isLit(3, 6),
                    dump.isLit(3, 5),
                    dump.isLit(3, 2),
                    dump.isLit(3, 3)
            );
            mainDigits[3] = SegmentDecoder.decodeDigit(
                    dump.isLit(4, 0),
                    dump.isLit(4, 1),
                    dump.isLit(4, 4),
                    dump.isLit(4, 6),
                    dump.isLit(4, 5),
                    dump.isLit(4, 2),
                    dump.isLit(4, 3)
            );
        }

        public Vector getAllStates() {
            return getChangedStates(null);
        }

        public Vector getChangedStates(State old) {
            boolean all = (old == null);
            Vector states = new Vector();

            if (all || (this.getPowerState() != old.getPowerState())) {
                states.addElement(S_POWER);
                states.addElement(getPowerStateString());
            }

            if (all || (this.getOutputMode() != old.getOutputMode())) {
                states.addElement(S_OUTPUT);
                states.addElement(getOutputModeString());
            }

            if (all || (this.getRadioBandState() != old.getRadioBandState())) {
                states.addElement(S_RADIO_BAND);
                states.addElement(getRadioBandStateString());
            }

            if (all || (this.getCDPlayMode() != old.getCDPlayMode())) {
                states.addElement(S_CD_PLAY_MODE);
                states.addElement(getCDPlayModeString());
            }

            if (all || (this.getFMStation() != old.getFMStation())) {
                if (this.getFMStation() > 0.0f) {
                    states.addElement(S_FM_STATION);
                    states.addElement(getFMStationString());
                }
            }

            if (all || (this.getAMStation() != old.getAMStation())) {
                if (this.getAMStation() > 0) {
                    states.addElement(S_AM_STATION);
                    states.addElement(getAMStationString());
                }
            }

            if (all || (this.getXBassState() != old.getXBassState())) {
                states.addElement(S_XBASS);
                states.addElement(getXBassStateString());
            }

            for (int i = 0; i < DiscAvail.length; i++) {
                boolean b = DiscAvail[i];
                if (all || (b != old.getDiscAvail(i + 1))) {
                    states.addElement("Disc" + (i + 1) + "Avail");
                    states.addElement(getDiscAvailString(i + 1));
                }
            }

            for (int i = 0; i < FMPresetValue.length; i++) {
                float v = FMPresetValue[i];
                if (all || (v != old.getFMPresetValue(i + 1))) {
                    states.addElement("FMPresetValue" + (i + 1));
                    states.addElement(getFMPresetValueString(i + 1));
                }
            }

            for (int i = 0; i < AMPresetValue.length; i++) {
                int v = AMPresetValue[i];
                if (all || (v != old.getAMPresetValue(i + 1))) {
                    states.addElement("AMPresetValue" + (i + 1));
                    states.addElement(getAMPresetValueString(i + 1));
                }
            }

            if (all || (this.getCDDiscActive() != old.getCDDiscActive())) {
                if (this.getCDDiscActive() != 0) {
                    states.addElement(S_CD_DISC);
                    states.addElement(getCDDiscActiveString());
                }
            }

            if (all || (this.getCDRandomState() != old.getCDRandomState())) {
                states.addElement(S_CD_RANDOM);
                states.addElement(getCDRandomStateString());
            }

            if (all || (this.getCDRepeatState() != old.getCDRepeatState())) {
                states.addElement(S_CD_REPEAT);
                states.addElement(getCDRepeatStateString());
            }

            if (all || (this.getAMPreset() != old.getAMPreset())) {
                states.addElement(S_AM_PRESET);
                states.addElement(getAMPresetString());
            }

            if (all || (this.getFMPreset() != old.getFMPreset())) {
                states.addElement(S_FM_PRESET);
                states.addElement(getFMPresetString());
            }

            if (all || (this.getCDTrack() != old.getCDTrack())) {
                states.addElement(S_CD_TRACK);
                states.addElement(getCDTrackString());
            }

            return states;
        }

        public int getCDTrack() {
            return CDTrack;
        }

        public String getCDTrackString() {
            return (new Integer(CDTrack)).toString();
        }

        public boolean getRadioBandState() {
            return RadioBandState;
        }

        public String getRadioBandStateString() {
            return (new Boolean(RadioBandState)).toString();
        }

        public boolean getPowerState() {
            return PowerState;
        }

        public String getPowerStateString() {
            return (new Boolean(PowerState)).toString();
        }

        public int getOutputMode() {
            return OutputMode;
        }

        public String getOutputModeString() {
            return (new Integer(OutputMode)).toString();
        }

        public int getCDPlayMode() {
            return CDPlayMode;
        }

        public String getCDPlayModeString() {
            return (new Integer(CDPlayMode)).toString();
        }

        public int getAMStation() {
            return AMStation;
        }

        public String getAMStationString() {
            return (new Integer(AMStation)).toString();
        }

        public float getFMStation() {
            return FMStation;
        }

        public String getFMStationString() {
            return (new Float(FMStation)).toString();
        }

        public boolean getXBassState() {
            return XBassState;
        }

        public String getXBassStateString() {
            return (new Boolean(XBassState)).toString();
        }

        public boolean getDiscAvail(int x) {
            return DiscAvail[x - 1];
        }

        public String getDiscAvailString(int x) {
            return (new Boolean(getDiscAvail(x))).toString();
        }

        public float getFMPresetValue(int x) {
            return FMPresetValue[x - 1];
        }

        public String getFMPresetValueString(int x) {
            return (new Float(getFMPresetValue(x))).toString();
        }

        public int getAMPresetValue(int x) {
            return AMPresetValue[x - 1];
        }

        public String getAMPresetValueString(int x) {
            return (new Integer(getAMPresetValue(x))).toString();
        }

        public int getCDDiscActive() {
            return CDDiscActive;
        }

        public String getCDDiscActiveString() {
            return (new Integer(CDDiscActive)).toString();
        }

        public boolean getCDRandomState() {
            return CDRandomState;
        }

        public String getCDRandomStateString() {
            return (new Boolean(CDRandomState)).toString();
        }

        public int getCDRepeatState() {
            return CDRepeatState;
        }

        public String getCDRepeatStateString() {
            return (new Integer(CDRepeatState)).toString();
        }

        public int getFMPreset() {
            return FMPreset;
        }

        public String getFMPresetString() {
            return (new Integer(FMPreset)).toString();
        }

        public int getAMPreset() {
            return AMPreset;
        }

        public String getAMPresetString() {
            return (new Integer(AMPreset)).toString();
        }

        public String toString() {
            String msg = "";
            msg += "CSDevice.State:";
            msg += "\n\t" + S_POWER + " = " + getPowerStateString();
            msg += "\n\t" + S_OUTPUT + " = " + getOutputModeString();
            msg += "\n\t" + S_RADIO_BAND + " = " + getRadioBandStateString();
            msg += "\n\t" + S_CD_PLAY_MODE + " = " + getCDPlayModeString();
            msg += "\n\t" + S_AM_STATION + " = " + getAMStationString();
            msg += "\n\t" + S_FM_STATION + " = " + getFMStationString();
            msg += "\n\t" + S_XBASS + " = " + getXBassStateString();
            msg += "\n\tDisc1Avail = " + getDiscAvailString(1);
            msg += "\n\tDisc2Avail = " + getDiscAvailString(2);
            msg += "\n\tDisc3Avail = " + getDiscAvailString(3);
            msg += "\n\tDisc4Avail = " + getDiscAvailString(4);
            msg += "\n\tDisc5Avail = " + getDiscAvailString(5);
            msg += "\n\t" + S_CD_DISC + " = " + getCDDiscActiveString();
            msg += "\n\t" + S_CD_RANDOM + " = " + getCDRandomStateString();
            msg += "\n\t" + S_CD_REPEAT + " = " + getCDRepeatStateString();
            msg += "\n\t" + S_FM_PRESET + " = " + getFMPresetString();
            msg += "\n\t" + S_AM_PRESET + " = " + getAMPresetString();
            msg += "\n\t" + S_CD_TRACK + " = " + getCDTrackString();
            return msg;
        }
    }
}
