package com.maya.puc.device.arq;

import com.maya.puc.common.Device;
import com.maya.puc.common.StateListener;
import com.maya.puc.common.TextResource;
import com.retrovirus.arq.arqlib.*;

import java.util.Vector;
import java.awt.*;
import java.io.IOException;

public class ArqDevice implements Device, ArqConnectionListener, ArqButtonListener {
    private static String name = "Audio ReQuest Pro";
    public static final String CONFIG_FILE = "arq.cfg";
    public static final String SPEC_FILE = "arq.xml";
    private Vector listeners = new Vector();
    private State currentState = null;
    public static final String S_POWER = "PowerState";
    public static final String S_VIEW = "ViewState";
    public static final String S_REPEAT = "RepeatState";
    public static final String S_SHUFFLE = "ShuffleState";
    public static final String S_INTRO = "IntroState";
    public static final String S_LIST = "ListItem";
    public static final String S_PLAY_ITEM = "PlayListItem";
    public static final String S_LIST_SELECTION = "ListSelectionIndex";
    public static final String S_SONG = "CurrentSong";
    public static final String S_ARTIST = "CurrentArtist";
    public static final String S_ALBUM = "CurrentAlbum";
    public static final String S_PLAYLIST = "CurrentPlaylist";
    public static final String S_NEXT_SONG = "NextSong";
    public static final String S_PLAY_MODE = "PlayMode";
    public static final String S_CMD_RANDOM_SONG = "RandomSongCommand";
    public static final String S_CMD_PREV_SONG = "PreviousSongCommand";
    public static final String S_CMD_NEXT_SONG = "NextSongCommand";
    public static final String S_CMD_NAV_UP = "NavigateUpCommand";
    public static final String S_CMD_NAV_DOWN = "NavigateDownCommand";
    public static final String S_CMD_NAV_LEFT = "NavigateLeftCommand";
    public static final String S_CMD_NAV_RIGHT = "NavigateRightCommand";
    public static final String S_CMD_NAV_ENTER = "NavigateEnterCommand";
    public static final String S_CMD_PAGE_UP = "PageUpCommand";
    public static final String S_CMD_PAGE_DOWN = "PageDownCommand";
    private ArqConnectionConfigurator cc = null;
    private ArqButtonListener buttonListener = this;
    private Vector pendingCommands = new Vector();
    private ArqConnection conn = null;
    private String status = null;
    private int port = 5151;

    public ArqDevice() {
        cc = new ArqConnectionConfigurator(new Frame(), CONFIG_FILE);
    }

    public void requestStateChange(String state, String value) {
        System.out.println("SCR: " + state + ", " + value);

        if (currentState == null)
            return;

        State st = currentState;

        if (state.equals(S_POWER))
            changePowerState(value, st);
        if (state.equals(S_VIEW))
            changeViewMode(value, st);
        if (state.equals(S_PLAY_MODE))
            changePlayMode(value, st);
        if (state.equals(S_REPEAT))
            changeRepeatState(value, st);
        if (state.equals(S_SHUFFLE))
            changeShuffleState(value, st);
        if (state.equals(S_INTRO))
            changeIntroState(value, st);
    }

    private void changePowerState(String value, State st) {
        boolean newPower = Boolean.valueOf(value).booleanValue();
        addCommand(new ArqCommand.ChangePower(buttonListener, newPower));
    }

    private void changeViewMode(String value, State st) {
        int newView = Integer.valueOf(value).intValue();
        addCommand(new ArqCommand.ChangeMode(buttonListener, newView));
    }

    private void changePlayMode(String value, State st) {
        int newMode = Integer.valueOf(value).intValue();
        addCommand(new ArqCommand.ChangePlay(buttonListener, newMode));
    }

    private void changeRepeatState(String value, State st) {
        boolean newRepeat = Boolean.valueOf(value).booleanValue();
        addCommand(new ArqCommand.ChangeRepeat(buttonListener, newRepeat));
    }

    private void changeShuffleState(String value, State st) {
        boolean newShuffle = Boolean.valueOf(value).booleanValue();
        addCommand(new ArqCommand.ChangeShuffle(buttonListener, newShuffle));
    }

    private void changeIntroState(String value, State st) {
        boolean newIntro = Boolean.valueOf(value).booleanValue();
        addCommand(new ArqCommand.ChangeIntro(buttonListener, newIntro));
    }

    public void buttonPressed(byte cmd) {
        sendArqMessage(new ArqMessage.RemoteCommand(cmd));
    }

    public void sendArqMessage(ArqMessage msg) {
        try {
            conn.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
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
        if (command.equals(S_CMD_RANDOM_SONG)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_JUMP_DOWN);
        } else if (command.equals(S_CMD_PREV_SONG)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_ARROW_UP);
        } else if (command.equals(S_CMD_NEXT_SONG)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_ARROW_DOWN);
        } else if (command.equals(S_CMD_NAV_UP)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_ARROW_UP);
        } else if (command.equals(S_CMD_NAV_DOWN)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_ARROW_DOWN);
        } else if (command.equals(S_CMD_NAV_LEFT)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_ARROW_LEFT);
        } else if (command.equals(S_CMD_NAV_RIGHT)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_ARROW_RIGHT);
        } else if (command.equals(S_CMD_NAV_ENTER)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_ENTER);
        } else if (command.startsWith(S_PLAY_ITEM)) {
            int index = Integer.parseInt(command.substring(command.length() - 1));
            sendArqMessage(new ArqMessage.JumpTo(index));
        } else if (command.equals(S_CMD_PAGE_UP)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_JUMP_UP);
        } else if (command.equals(S_CMD_PAGE_DOWN)) {
            buttonListener.buttonPressed(ArqMessage.RemoteCommand.B_JUMP_DOWN);
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
        return false;
    }

    public void setGUIVisibility(boolean isVisible) {
    }

    public boolean isGUIVisible() {
        return false;
    }

    public void start() {
        if (cc.isPro()) {
            conn = new ArqConnection(cc.getConnIP(), cc.getCtlPort());
        } else {
            conn = new ArqConnection(cc.getConnIP(), cc.isUsingBroadcast(),
                    cc.getCtlPort(), cc.getArqName(), cc.getArqIP(), cc.isUsingDHCP());
        }
        conn.addArqConnectionListener(this);
        conn.connect();
        if (conn.isConnected()) {
            try {
                if (cc.isPro()) {
                    ArqMessage msg = new ArqMessage.CommandDataType(ArqMessage.CommandDataType.PRO_UPD_BOTH);
                    conn.send(msg);
/*
                    ArqMessage msg = new ArqMessage.CommandDataType(ArqMessage.CommandDataType.PRO_UPD_GUIDATA);
                    conn.send(msg);
                    msg = new ArqMessage.CommandDataType(ArqMessage.CommandDataType.PRO_UPD_DELIMGUI);
                    conn.send(msg);
*/
                } else {
                    ArqMessage msg = new ArqMessage.CommandDataType(ArqMessage.CommandDataType.BOTH);
                    conn.send(msg);
/*
                    ArqMessage msg = new ArqMessage.CommandDataType(ArqMessage.CommandDataType.GUI_ONLY);
                    conn.send(msg);
*/
                }
            } catch (IOException e) {
                System.err.println("Exception in ArqDevice.start()");
                e.printStackTrace();
            }
            status = null;
        } else {
            status = "Unable to connect.";
        }
        updateProxy();
    }

    private void updateProxy() {
        dispatchStateEvent(null, null);
    }

    public void stop() {
        if ((conn != null) && (conn.isConnected())) {
            conn.removeArqConnectionListener(this);
            conn.disconnect();
        }
        dispatchStateEvent(null,null);
    }

    public boolean isRunning() {
        return ((conn != null) && (conn.isConnected()));
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

    public void messagesWaiting(ArqConnectionEvent.MessagesWaiting waiting) {
        ArqMessage msg = conn.getMessage();
        while (msg != null) {
//            System.out.println(msg);
            update(msg);
            msg = conn.getMessage();
        }
    }

    public void update(ArqMessage msg) {
        State newState = new State(msg, currentState);

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

        executeNextCommand();
    }

    private void executeNextCommand() {
        if (pendingCommands.size() > 0)
            System.out.println("Commands: " + pendingCommands);
        ArqCommand cmd = getNextCommand();
        if (cmd != null) {
            boolean done = cmd.execute(currentState);
            if (done) discardFirstCommand();
        }
    }

    private void addCommand(ArqCommand cmd) {
        synchronized (pendingCommands) {
            pendingCommands.addElement(cmd);
        }
        executeNextCommand();
    }

    private ArqCommand getNextCommand() {
        synchronized (pendingCommands) {
            if (pendingCommands.size() <= 0) return null;
            return (ArqCommand) (pendingCommands.elementAt(0));
        }
    }

    private void discardFirstCommand() {
        synchronized (pendingCommands) {
            if (pendingCommands.size() <= 0) return;
            pendingCommands.removeElementAt(0);
        }
    }

    protected class State {
        public static final int NUM_LIST_ITEMS = 8;
        private boolean PowerState = false;
        private boolean RepeatState = false;
        private boolean ShuffleState = false;
        private boolean IntroState = false;
        private int ViewState = 0;
        private String[] ListItem = new String[NUM_LIST_ITEMS];
        private String CurrentSong = "";
        private String CurrentArtist = "";
        private String CurrentAlbum = "";
        private String CurrentPlaylist = "";
        private String NextSong = "";
        private int ListSelectionIndex = 0;
        private int PlayMode = 0;
        public static final int VIEW_SONG_LIST = 1;
        public static final int VIEW_PLAYER = 2;

        public State(ArqMessage msg, State oldState) {
            importOldValues(oldState);

            decodeLCDState(msg);
            decodeProPlayer(msg);
            decodeProNavigator(msg);
        }

        private void importOldValues(State oldState) {
            if (oldState != null) {
                PowerState = oldState.getPowerState();
                ViewState = oldState.getViewState();
                for (int i = 0; i < ListItem.length; i++) {
                    ListItem[i] = oldState.getListItem(i);
                }
                CurrentSong = oldState.getCurrentSong();
                CurrentArtist = oldState.getCurrentArtist();
                CurrentAlbum = oldState.getCurrentAlbum();
                CurrentPlaylist = oldState.getCurrentPlaylist();
                NextSong = oldState.getNextSong();
                PlayMode = oldState.getPlayMode();
                ListSelectionIndex = oldState.getListSelectionIndex();
                RepeatState = oldState.getRepeatState();
                ShuffleState = oldState.getShuffleState();
                IntroState = oldState.getIntroState();
            }
        }

        private void decodeLCDState(ArqMessage msg) {
            if (msg instanceof ArqMessage.LCDState) {
                ArqMessage.LCDState lcdState = (ArqMessage.LCDState) msg;
                PowerState = lcdState.getBacklight();
            }
        }

        private void decodeProNavigator(ArqMessage msg) {
            if (msg instanceof ArqGuiMessage.ProNavigator) {
                ArqGuiMessage.ProNavigator nav = (ArqGuiMessage.ProNavigator) msg;
                ViewState = VIEW_SONG_LIST;
                PowerState = true;
                ListSelectionIndex = nav.getLeftCursorPosition();
                final int lines = nav.getLeftWindowVectorValid();
                for (int i = 0; i < ListItem.length; i++) {
                    final boolean cursor = (i == ListSelectionIndex);
                    final boolean valid = (i < lines);
                    ListItem[i] = (cursor?">":"") + (valid?nav.getLeftWindowVectorString(i):"");
                }
            }
        }

        private void decodeProPlayer(ArqMessage msg) {
            if (msg instanceof ArqGuiMessage.ProPlayer) {
                ArqGuiMessage.ProPlayer plyr = (ArqGuiMessage.ProPlayer) msg;
                ViewState = VIEW_PLAYER;
                PowerState = true;
                CurrentSong = plyr.getSongTitleString();
                CurrentArtist = plyr.getArtistNameString();
                CurrentAlbum = plyr.getAlbumNameString();
                CurrentPlaylist = plyr.getPlaylistNameString();
                NextSong = plyr.getNextSongTitleString();
                PlayMode = (int)plyr.getPlayerState();
                RepeatState = plyr.isRepeatOn();
                ShuffleState = plyr.isShuffleOn();
                IntroState = plyr.isIntroOn();
            }
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

            if (all || (this.getViewState() != old.getViewState())) {
                states.addElement(S_VIEW);
                states.addElement(getViewStateString());
            }

            for (int i = 0; i < ListItem.length; i++) {
                String s = ListItem[i];
                if (all || !(old.getListItem(i).equals(s))) {
                    states.addElement(S_LIST + i);
                    states.addElement(getListItem(i));
                }
            }

            if (all || !(this.getCurrentSong().equals(old.getCurrentSong()))) {
                states.addElement(S_SONG);
                states.addElement(getCurrentSong());
            }

            if (all || !(this.getCurrentArtist().equals(old.getCurrentArtist()))) {
                states.addElement(S_ARTIST);
                states.addElement(getCurrentArtist());
            }

            if (all || !(this.getCurrentAlbum().equals(old.getCurrentAlbum()))) {
                states.addElement(S_ALBUM);
                states.addElement(getCurrentAlbum());
            }

            if (all || !(this.getCurrentPlaylist().equals(old.getCurrentPlaylist()))) {
                states.addElement(S_PLAYLIST);
                states.addElement(getCurrentPlaylist());
            }

            if (all || !(this.getNextSong().equals(old.getNextSong()))) {
                states.addElement(S_NEXT_SONG);
                states.addElement(getNextSong());
            }

            if (all || (this.getPlayMode() != old.getPlayMode())) {
                states.addElement(S_PLAY_MODE);
                states.addElement(getPlayModeString());
            }

            if (all || (this.getListSelectionIndex() != old.getListSelectionIndex())) {
                states.addElement(S_LIST_SELECTION);
                states.addElement(getListSelectionIndexString());
            }

            if (all || (this.getRepeatState() != old.getRepeatState())) {
                states.addElement(S_REPEAT);
                states.addElement(getRepeatStateString());
            }

            if (all || (this.getShuffleState() != old.getShuffleState())) {
                states.addElement(S_SHUFFLE);
                states.addElement(getShuffleStateString());
            }

            if (all || (this.getIntroState() != old.getIntroState())) {
                states.addElement(S_INTRO);
                states.addElement(getIntroStateString());
            }

            return states;
        }

        public boolean getPowerState() {
            return PowerState;
        }

        public String getPowerStateString() {
            return new Boolean(PowerState).toString();
        }

        public int getViewState() {
            return ViewState;
        }

        public String getViewStateString() {
            return new Integer(ViewState).toString();
        }

        public String getListItem(int i) {
            if (ListItem[i] == null)
                return new String();
            return ListItem[i];
        }

        public String getCurrentSong() {
            return CurrentSong;
        }

        public String getCurrentArtist() {
            return CurrentArtist;
        }

        public String getCurrentAlbum() {
            return CurrentAlbum;
        }

        public String getCurrentPlaylist() {
            return CurrentPlaylist;
        }

        public String getNextSong() {
            return NextSong;
        }

        public int getPlayMode() {
            return PlayMode;
        }

        public String getPlayModeString() {
            return new Integer(PlayMode).toString();
        }

        public int getListSelectionIndex() {
            return ListSelectionIndex;
        }

        public String getListSelectionIndexString() {
            return new Integer(ListSelectionIndex).toString();
        }

        public boolean getRepeatState() {
            return RepeatState;
        }

        public String getRepeatStateString() {
            return new Boolean(RepeatState).toString();
        }

        public boolean getShuffleState() {
            return ShuffleState;
        }

        public String getShuffleStateString() {
            return new Boolean(ShuffleState).toString();
        }

        public boolean getIntroState() {
            return IntroState;
        }

        public String getIntroStateString() {
            return new Boolean(IntroState).toString();
        }
    }
}