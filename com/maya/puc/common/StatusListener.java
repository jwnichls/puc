package com.maya.puc.common;

public interface StatusListener {

    public void statusChanged( Device2 d, String sNewStatus );

    public void activeChanged( Device2 d );

}
