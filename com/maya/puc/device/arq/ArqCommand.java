/*
 * $Id: ArqCommand.java,v 1.2 2002/03/29 00:28:51 maya Exp $
 */

package com.maya.puc.device.arq;

import com.retrovirus.arq.arqlib.ArqButtonListener;
import com.retrovirus.arq.arqlib.ArqMessage;


public abstract class ArqCommand {
    ArqButtonListener bl = null;
    private static int RETRY_TIMES = 15;

    public abstract boolean execute(ArqDevice.State state);

/*
    public static class ChangeDisc extends ArqCommand {
        private int currDisc = 0;
        private int targetDisc = 0;
        private int retry = RETRY_TIMES;

        public ChangeDisc(CSButtonListener bl, int disc) {
            this.bl = bl;
            this.targetDisc = disc;
        }

        public boolean execute(CSDevice.State state) {
            if (!state.isInCDMode()) return true;
            if ((targetDisc < 1) || (targetDisc > 5)) return true;
            if (!state.getDiscAvail(targetDisc)) return true;
            if (state.getCDDiscActive() == targetDisc) return true;
            if (state.getCDDiscActive() == 0) return false;
            if ((state.getCDDiscActive() == currDisc) && (retry > 0)) {
                retry--;
                return false;
            }

            currDisc = state.getCDDiscActive();
            retry = RETRY_TIMES;
            bl.buttonPressed(CSMessage.RemoteCommand.CMD_DISC);
            return false;
        }

        public String toString() {
            return "CSCommand.ChangeDisc(" + currDisc + " --> " + targetDisc + ")";
        }
    }
*/
    public static class ChangeMode extends ArqCommand {
        private int lastMode = 0;
        private int targetMode = 0;
        private int retry = RETRY_TIMES;

        public ChangeMode(ArqButtonListener bl, int targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
        }

        public boolean execute(ArqDevice.State state) {
            if (!state.getPowerState()) return true; // stereo is turned off
            if ((targetMode < 1) || (targetMode > 2)) return true;
            if (state.getViewState() == targetMode) return true;
            if ((state.getViewState() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }


            lastMode = state.getViewState();
            retry = RETRY_TIMES;
            bl.buttonPressed(ArqMessage.RemoteCommand.B_MODE);
            return false;
        }

        public String toString() {
            return "ArqCommand.ChangeMode(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }

/*
    public static class ChangeRepeat extends ArqCommand {
        private int lastMode = 0;
        private int targetMode = 0;
        private int retry = RETRY_TIMES;

        public ChangeRepeat(CSButtonListener bl, int targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
        }

        public boolean execute(CSDevice.State state) {
            if (!state.isInCDMode()) return true; // not in CD mode
            if ((targetMode < 1) || (targetMode > 5)) return true;
            if (state.getCDRandomState() && ((targetMode == 4) || (targetMode == 5))) return true;
            if (!state.getCDRandomState() && (targetMode == 3)) return true;
            if (state.getCDRepeatState() == targetMode) return true;
            if ((state.getCDRepeatState() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }


            lastMode = state.getCDRepeatState();
            retry = RETRY_TIMES;
            bl.buttonPressed(CSMessage.RemoteCommand.CMD_REPEAT);
            return false;
        }

        public String toString() {
            return "CSCommand.ChangeRepeat(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }

    public static class ChangePlay extends ArqCommand {
        private int lastMode = 0;
        private int targetMode = 0;
        private int retry = RETRY_TIMES;

        public ChangePlay(CSButtonListener bl, int targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
        }

        public boolean execute(CSDevice.State state) {
            if (!state.isInCDMode()) return true; // not in CD mode
            if ((targetMode < 1) || (targetMode > 3)) return true;
            if (state.getCDPlayMode() == targetMode) return true;
            if ((state.getCDPlayMode() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }


            lastMode = state.getCDPlayMode();
            retry = RETRY_TIMES;
            if (targetMode == 1) {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_STOP);
            } else {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_PLAY);
            }

            return false;
        }

        public String toString() {
            return "CSCommand.ChangePlay(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }

    public static class ChangeFMStation extends ArqCommand {
        public static final float MIN_FREQ = 87.5f;
        public static final float MAX_FREQ = 108.0f;
        private float lastFreq = 0;
        private float targetFreq = 0;
        private int retry = RETRY_TIMES;
        private boolean wrap = true;

        public ChangeFMStation(CSButtonListener bl, float targetFreq, boolean allowWraparound) {
            this.bl = bl;
            this.targetFreq = targetFreq;
            this.wrap = allowWraparound;
        }

        public boolean execute(CSDevice.State state) {
            if (!state.isInFMMode()) return true;
            if ((targetFreq < MIN_FREQ) || (targetFreq > MAX_FREQ)) return true;
            if (state.getFMStation() == targetFreq) return true;
            if ((state.getFMStation() == lastFreq) && (retry > 0)) {
                retry--;
                return false;
            }

            lastFreq = state.getFMStation();
            retry = RETRY_TIMES;
            if (isUpFaster()) {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_TUNE_UP);
            } else {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_TUNE_DOWN);
            }
            return false;
        }

        private boolean isUpFaster() {
            if (!wrap) {
                return (targetFreq > lastFreq);
            }

            float noWrapDist = Math.abs(targetFreq - lastFreq);
            float wrapUpDist = Math.abs((MAX_FREQ - lastFreq) + (targetFreq - MIN_FREQ));
            float wrapDownDist = Math.abs((lastFreq - MIN_FREQ) + (MAX_FREQ - targetFreq));

            if ((noWrapDist < wrapUpDist) && (noWrapDist < wrapDownDist)) {
                return (targetFreq > lastFreq);
            }

            return (wrapUpDist < wrapDownDist);
        }

        public String toString() {
            return "CSCommand.ChangeFMStation(" + lastFreq + " --> " + targetFreq + ", retry = " + retry + ")";
        }
    }

    public static class ChangeAMStation extends ArqCommand {
        public static final int MIN_FREQ = 530;
        public static final int MAX_FREQ = 1710;
        private int lastFreq = 0;
        private int targetFreq = 0;
        private int retry = RETRY_TIMES;
        private boolean wrap = true;

        public ChangeAMStation(CSButtonListener bl, int targetFreq, boolean allowWrapAround) {
            this.bl = bl;
            this.targetFreq = targetFreq;
            this.wrap = allowWrapAround;
        }

        public boolean execute(CSDevice.State state) {
            if (!state.isInAMMode()) return true;
            if ((targetFreq < MIN_FREQ) || (targetFreq > MAX_FREQ)) return true;
            if (state.getAMStation() == targetFreq) return true;
            if ((state.getAMStation() == lastFreq) && (retry > 0)) {
                retry--;
                return false;
            }

            lastFreq = state.getAMStation();
            retry = RETRY_TIMES;
            if (isUpFaster()) {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_TUNE_UP);
            } else {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_TUNE_DOWN);
            }
            return false;
        }

        private boolean isUpFaster() {
            if (!wrap) {
                return (targetFreq > lastFreq);
            }

            int noWrapDist = Math.abs(targetFreq - lastFreq);
            int wrapUpDist = Math.abs((MAX_FREQ - lastFreq) + (targetFreq - MIN_FREQ));
            int wrapDownDist = Math.abs((lastFreq - MIN_FREQ) + (MAX_FREQ - targetFreq));

            if ((noWrapDist < wrapUpDist) && (noWrapDist < wrapDownDist)) {
                return (targetFreq > lastFreq);
            }

            return (wrapUpDist < wrapDownDist);
        }

        public String toString() {
            return "CSCommand.ChangeAMStation(" + lastFreq + " --> " + targetFreq + ", retry = " + retry + ")";
        }
    }

    public static class ChangeAMPreset extends ArqCommand {
        public static final int MIN_PRESET = 1;
        public static final int MAX_PRESET = CSDevice.NUM_FM_PRESETS;
        private int lastPreset = 0;
        private int targetPreset = 0;
        private int retry = RETRY_TIMES;
        private boolean wrap = true;

        public ChangeAMPreset(CSButtonListener bl, int targetPreset, boolean allowWrapAround) {
            this.bl = bl;
            this.targetPreset = targetPreset;
            wrap = allowWrapAround;
        }

        public boolean execute(CSDevice.State state) {
            if (!state.isInAMMode()) return true;
            if ((targetPreset < 1) || (targetPreset > CSDevice.NUM_AM_PRESETS)) return true;
            if (state.getAMPreset() == targetPreset) return true;
            if ((state.getAMPreset() == lastPreset) && (retry > 0)) {
                retry--;
                return false;
            }

            lastPreset = state.getAMPreset();
            retry = RETRY_TIMES;
            if (isUpFaster()) {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_NEXT);
            } else {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_PREV);
            }
            return false;
        }

        private boolean isUpFaster() {
            if (!wrap) {
                return (targetPreset > lastPreset);
            }

            int noWrapDist = Math.abs(targetPreset - lastPreset);
            int wrapUpDist = Math.abs((MAX_PRESET - lastPreset) + (targetPreset - MIN_PRESET));
            int wrapDownDist = Math.abs((lastPreset - MIN_PRESET) + (MAX_PRESET - targetPreset));

            if ((noWrapDist < wrapUpDist) && (noWrapDist < wrapDownDist)) {
                return (targetPreset > lastPreset);
            }

            return (wrapUpDist < wrapDownDist);
        }

        public String toString() {
            return "CSCommand.ChangeAMPreset(" + lastPreset + " --> " + targetPreset + ", retry = " + retry + ")";
        }
    }

    public static class ChangeFMPreset extends ArqCommand {
        public static final int MIN_PRESET = 1;
        public static final int MAX_PRESET = CSDevice.NUM_FM_PRESETS;
        private int lastPreset = 0;
        private int targetPreset = 0;
        private int retry = RETRY_TIMES;
        private boolean wrap = true;

        public ChangeFMPreset(CSButtonListener bl, int targetPreset, boolean allowWrapAround) {
            this.bl = bl;
            this.targetPreset = targetPreset;
            this.wrap = allowWrapAround;
        }

        public boolean execute(CSDevice.State state) {
            if (!state.isInFMMode()) return true;
            if ((targetPreset < 1) || (targetPreset > CSDevice.NUM_FM_PRESETS)) return true;
            if (state.getFMPreset() == targetPreset) return true;
            if ((state.getFMPreset() == lastPreset) && (retry > 0)) {
                retry--;
                return false;
            }

            lastPreset = state.getFMPreset();
            retry = RETRY_TIMES;
            if (isUpFaster()) {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_NEXT);
            } else {
                bl.buttonPressed(CSMessage.RemoteCommand.CMD_PREV);
            }
            return false;
        }

        private boolean isUpFaster() {
            if (!wrap) {
                return (targetPreset > lastPreset);
            }

            int noWrapDist = Math.abs(targetPreset - lastPreset);
            int wrapUpDist = Math.abs((MAX_PRESET - lastPreset) + (targetPreset - MIN_PRESET));
            int wrapDownDist = Math.abs((lastPreset - MIN_PRESET) + (MAX_PRESET - targetPreset));

            if ((noWrapDist < wrapUpDist) && (noWrapDist < wrapDownDist)) {
                return (targetPreset > lastPreset);
            }

            return (wrapUpDist < wrapDownDist);
        }

        public String toString() {
            return "CSCommand.ChangeFMPreset(" + lastPreset + " --> " + targetPreset + ", retry = " + retry + ")";
        }
    }

*/
    public static class ChangePower extends ArqCommand {
        private boolean lastMode;
        private boolean targetMode;
        private int retry = RETRY_TIMES;

        public ChangePower(ArqButtonListener bl, boolean targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
            retry = 0;
        }

        public boolean execute(ArqDevice.State state) {
            if (state.getPowerState() == targetMode) return true;
            if ((state.getPowerState() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }

            lastMode = state.getPowerState();
            retry = RETRY_TIMES;
            bl.buttonPressed(ArqMessage.RemoteCommand.B_POWER);
            return false;
        }

        public String toString() {
            return "ArqCommand.ChangePower(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }

    public static class ChangeRepeat extends ArqCommand {
        private boolean lastMode;
        private boolean targetMode;
        private int retry = RETRY_TIMES;

        public ChangeRepeat(ArqButtonListener bl, boolean targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
            retry = 0;
        }

        public boolean execute(ArqDevice.State state) {
            if (state.getRepeatState() == targetMode) return true;
            if ((state.getRepeatState() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }

            lastMode = state.getRepeatState();
            retry = RETRY_TIMES;
            bl.buttonPressed(ArqMessage.RemoteCommand.B_REPEAT);
            return false;
        }

        public String toString() {
            return "ArqCommand.ChangeRepeat(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }

    public static class ChangeShuffle extends ArqCommand {
        private boolean lastMode;
        private boolean targetMode;
        private int retry = RETRY_TIMES;

        public ChangeShuffle(ArqButtonListener bl, boolean targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
            retry = 0;
        }

        public boolean execute(ArqDevice.State state) {
            if (state.getShuffleState() == targetMode) return true;
            if ((state.getShuffleState() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }

            lastMode = state.getShuffleState();
            retry = RETRY_TIMES;
            bl.buttonPressed(ArqMessage.RemoteCommand.B_SHUFFLE);
            return false;
        }

        public String toString() {
            return "ArqCommand.ChangeShuffle(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }

    public static class ChangeIntro extends ArqCommand {
        private boolean lastMode;
        private boolean targetMode;
        private int retry = RETRY_TIMES;

        public ChangeIntro(ArqButtonListener bl, boolean targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
            retry = 0;
        }

        public boolean execute(ArqDevice.State state) {
            if (state.getIntroState() == targetMode) return true;
            if ((state.getIntroState() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }

            lastMode = state.getIntroState();
            retry = RETRY_TIMES;
            bl.buttonPressed(ArqMessage.RemoteCommand.B_ALT);
            bl.buttonPressed(ArqMessage.RemoteCommand.B_ZERO);
            return false;
        }

        public String toString() {
            return "ArqCommand.ChangeIntro(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }


    public static class ChangePlay extends ArqCommand {
        private int lastMode = 0;
        private int targetMode = 0;
        private int retry = RETRY_TIMES;

        public ChangePlay(ArqButtonListener bl, int targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
        }

        public boolean execute(ArqDevice.State state) {
            if ((targetMode < 1) || (targetMode > 3)) return true;
            if (state.getPlayMode() == targetMode) return true;
            if ((state.getPlayMode() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }


            lastMode = state.getPlayMode();
            retry = RETRY_TIMES;
            if (targetMode == 1) {
                bl.buttonPressed(ArqMessage.RemoteCommand.B_STOP);
            } else if (targetMode == 3) {
                bl.buttonPressed(ArqMessage.RemoteCommand.B_PAUSE);
            } else {
                bl.buttonPressed(ArqMessage.RemoteCommand.B_PLAY);
            }

            return false;
        }

        public String toString() {
            return "ArqCommand.ChangePlay(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }


/*
    public static class ChangeBand extends ArqCommand {
        private boolean lastMode;
        private boolean targetMode;
        private int retry = RETRY_TIMES;

        public ChangeBand(CSButtonListener bl, boolean targetMode) {
            this.bl = bl;
            this.targetMode = targetMode;
            retry = 0;
        }

        public boolean execute(CSDevice.State state) {
            if (!state.isInTunerMode()) return true;
            if (state.getRadioBandState() == targetMode) return true;
            if ((state.getRadioBandState() == lastMode) && (retry > 0)) {
                retry--;
                return false;
            }

            lastMode = state.getRadioBandState();
            retry = RETRY_TIMES;
            bl.buttonPressed(CSMessage.RemoteCommand.CMD_BAND);
            return false;
        }

        public String toString() {
            return "CSCommand.ChangeBand(" + lastMode + " --> " + targetMode + ", retry = " + retry + ")";
        }
    }
*/
}

