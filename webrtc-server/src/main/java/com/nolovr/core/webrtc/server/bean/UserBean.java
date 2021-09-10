package com.nolovr.core.webrtc.server.bean;

import org.java_websocket.WebSocket;

public class UserBean {

    private String userId;
    private String avatar;

    private boolean isPhone;

    private DeviceSession pcSession;
    private DeviceSession phoneSession;


    public UserBean(String userId, String avatar) {
        this.userId = userId;
        this.avatar = avatar;
    }

    public void setPhoneSession(WebSocket session, int device) {
        if (session == null) {
            this.phoneSession = null;
            return;
        }
        this.phoneSession = new DeviceSession(session, device);
    }

    public void setPcSession(WebSocket session, int device) {
        if (session == null) {
            this.pcSession = null;
            return;
        }
        this.pcSession = new DeviceSession(session, device);
    }

    public WebSocket getPhoneSession() {
        return phoneSession == null ? null : phoneSession.getSession();
    }

    public WebSocket getPcSession() {
        return pcSession == null ? null : pcSession.getSession();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        UserBean user = (UserBean) obj;
        return this.userId.equals(user.getUserId());
    }

    public boolean isPhone() {
        return isPhone;
    }

    public void setPhone(boolean phone) {
        isPhone = phone;
    }
}
