package com.nolovr.core.webrtc.server.bean;


import org.java_websocket.WebSocket;

public class DeviceSession {
    private WebSocket session;
    private int       device; // 0 phone 1 pc
    private int       statue; // 0 idle  1 inCall


    public DeviceSession(WebSocket session, int device) {
        this.session = session;
        this.device = device;
    }

    public WebSocket getSession() {
        return session;
    }

    public void setSession(WebSocket session) {
        this.session = session;
    }

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }

    public int getStatue() {
        return statue;
    }

    public void setStatue(int statue) {
        this.statue = statue;
    }
}
