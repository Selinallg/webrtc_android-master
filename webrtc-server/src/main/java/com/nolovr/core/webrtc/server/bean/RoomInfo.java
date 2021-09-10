package com.nolovr.core.webrtc.server.bean;

import java.util.concurrent.CopyOnWriteArrayList;

public class RoomInfo {
    // roomId
    private String roomId;
    // 创建人Id
    private String userId;
    // 房间里的人
    private CopyOnWriteArrayList<UserBean> userBeans = new CopyOnWriteArrayList<>();
    // 房间大小
    private int maxSize;
    // 现有人数
    private int currentSize;

    public RoomInfo() {
    }


    public CopyOnWriteArrayList<UserBean> getUserBeans() {
        return userBeans;
    }

    
    public void setUserBeans(CopyOnWriteArrayList<UserBean> userBeans) {
        this.userBeans = userBeans;
        setCurrentSize(this.userBeans.size());
    }

    
    public int getMaxSize() {
        return maxSize;
    }

    
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    
    public String getRoomId() {
        return roomId;
    }

    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    
    public String getUserId() {
        return userId;
    }

    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }
}
