package com.nolovr.core.webrtc.server;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.nolovr.core.webrtc.server.bean.RoomInfo;
import com.nolovr.core.webrtc.server.bean.UserBean;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class UserControl {

    @RequestMapping("/")
    public String index() {
        return "welcome to my webRTC demo";
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @RequestMapping("/roomList")
    public static List<RoomInfo> roomList() {
        ConcurrentHashMap<String, RoomInfo> rooms   = MemCons.rooms;
        Collection<RoomInfo>                values  = rooms.values();
        ArrayList<RoomInfo>                 objects = new ArrayList<>();
        values.forEach(roomInfo -> {
            if (roomInfo.getMaxSize() > 2) {
                objects.add(roomInfo);
            }
        });
        return objects;
    }

    @RequestMapping("/userList")
    public static List<UserBean> userList() {
        ConcurrentHashMap<String, UserBean> userBeans = MemCons.userBeans;
        Collection<UserBean>                values    = userBeans.values();
        return new ArrayList<>(values);
    }

}
