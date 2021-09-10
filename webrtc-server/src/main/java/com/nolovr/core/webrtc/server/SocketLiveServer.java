package com.nolovr.core.webrtc.server;

import static com.nolovr.core.webrtc.server.MemCons.rooms;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nolovr.core.webrtc.server.bean.EventData;
import com.nolovr.core.webrtc.server.bean.RoomInfo;
import com.nolovr.core.webrtc.server.bean.UserBean;
import com.nolovr.core.webrtc.server.bean.UserInfo;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 录制端
 */
public class SocketLiveServer {

    private static final String TAG = "SocketLiveServer";

    private        String userId;
    private static Gson   gson   = new Gson();
    private static String avatar = "p1.jpeg";
    int     port          = 5000;
    boolean serverRunning = false;
    private static SocketLiveServer sockentInstance = null;

    private ExecutorService mThreadPool;

    private SocketLiveServer(int port) {
        this.port = port;

        init();

    }

    public static SocketLiveServer getEngine(int port) {
        synchronized (SocketLiveServer.class) {
            if (sockentInstance == null) {
                sockentInstance = new SocketLiveServer(port);
            }
        }
        return sockentInstance;
    }

    public void start() {


        try {
            if (!serverRunning && mWebSocketServer != null) {
                serverRunning = true;
                mWebSocketServer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "start:  2 ", e);
        }
    }

    public void close() {

        serverRunning = false;
        try {
            if (mWebSocketServer != null) {
                mWebSocketServer.stop();
                mWebSocketServer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void init() {


        if (mThreadPool == null) {
            mThreadPool = Executors.newFixedThreadPool(1);
        }

        if (mWebSocketServer != null) {
            mWebSocketServer.setReuseAddr(true);
            mWebSocketServer.setConnectionLostTimeout(60 * 60);
        }

        Log.d(TAG, "init: sucess");


    }

    public void release() {

        try {
            if (mThreadPool != null) {
                mThreadPool.shutdown();
                mThreadPool = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    WebSocketServer mWebSocketServer = new WebSocketServer(new InetSocketAddress(port)) {
        @Override
        public void onOpen(WebSocket session, ClientHandshake clientHandshake) {
            try {
                try {
                    Log.d(TAG, "onOpen: -------------mWebSocketServer---------------" + session.getRemoteSocketAddress().getAddress());


                } catch (Exception e) {
                    e.printStackTrace();
                }
                String descriptor = clientHandshake.getResourceDescriptor();
                Log.d(TAG, "onOpen: " + descriptor);


                if (!TextUtils.isEmpty(descriptor)) {
                    if (descriptor.contains("room")) {
                        return;
                    }
                }
                String[] goodList = descriptor.split("\\/");

                String userId = goodList[1];
                String de     = goodList[2];

                if (TextUtils.isEmpty(de)) {
                    Log.d(TAG, "onOpen: -----1");
                    return;
                }

                if (TextUtils.isEmpty(userId)) {
                    Log.d(TAG, "onOpen: -----2");
                    return;
                }

                // /roomList
                // /userList

                int      device   = Integer.parseInt(de);
                UserBean userBean = MemCons.userBeans.get(userId);
                if (userBean == null) {
                    userBean = new UserBean(userId, avatar);
                }
                if (device == 0) {
                    userBean.setPhoneSession(session, device);
                    userBean.setPhone(true);
                    Log.e(TAG, "Phone用户登陆:" + userBean.getUserId() + ",session:" + session.getRemoteSocketAddress().getAddress());
                } else {
                    userBean.setPcSession(session, device);
                    userBean.setPhone(false);
                    Log.e(TAG, "PC用户登陆:" + userBean.getUserId() + ",session:" + session.getRemoteSocketAddress().getAddress());
                }
                SocketLiveServer.this.userId = userId;

                //加入列表
                MemCons.userBeans.put(userId, userBean);

                // 登陆成功，返回个人信息
                EventData send = new EventData();
                send.setEventName("__login_success");
                Map<String, Object> map = new HashMap<>();
                map.put("userID", userId);
                map.put("avatar", avatar);
                send.setData(map);
                session.send(gson.toJson(send));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onOpen: ", e);
            }


        }

        @Override
        public void onClose(WebSocket webSocket, int i, String s, boolean b) {
            Log.i(TAG, "onClose: 关闭 socket ");

            System.out.println(webSocket.getRemoteSocketAddress().getAddress() + "-->onClose......");
            // 根据用户名查出房间,
            UserBean userBean = MemCons.userBeans.get(userId);
            if (userBean != null) {
                if (userBean.isPhone()) {
                    WebSocket phoneSession = userBean.getPhoneSession();
                    if (phoneSession != null) {
                        try {
                            phoneSession.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        userBean.setPhoneSession(null, 0);
                        MemCons.userBeans.remove(userId);
                    }
                    Log.e(TAG, "Phone用户离开:" + userBean.getUserId());
                } else {
                    WebSocket pcSession = userBean.getPcSession();
                    if (pcSession != null) {
                        try {
                            pcSession.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        userBean.setPcSession(null, 0);
                        MemCons.userBeans.remove(userId);
                        Log.e(TAG, "PC用户离开:" + userBean.getUserId());
                    }
                }
            }

        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onMessage(WebSocket webSocket, String message) {
            Log.d(TAG, "onMessage: " + message);
            handleMessage(message);
        }

        @Override
        public void onError(WebSocket webSocket, Exception e) {
            try {
                Log.i(TAG, "onError:  " + e.toString());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onStart() {

        }
    };


    // 发送各种消息
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleMessage(String message) {
        EventData data;
        try {
            data = gson.fromJson(message, EventData.class);
        } catch (JsonSyntaxException e) {
            System.out.println("json解析错误：" + message);
            return;
        }
        switch (data.getEventName()) {
            case "__create":
                createRoom(message, data.getData());
                break;
            case "__invite":
                invite(message, data.getData());
                break;
            case "__ring":
                ring(message, data.getData());
                break;
            case "__cancel":
                cancel(message, data.getData());
                break;
            case "__reject":
                reject(message, data.getData());
                break;
            case "__join":
                join(message, data.getData());
                break;
            case "__ice_candidate":
                iceCandidate(message, data.getData());
                break;
            case "__offer":
                offer(message, data.getData());
                break;
            case "__answer":
                answer(message, data.getData());
                break;
            case "__leave":
                leave(message, data.getData());
                break;
            case "__audio":
                transAudio(message, data.getData());
                break;
            case "__disconnect":
                disconnet(message, data.getData());
                break;
            case "__queryRooms":
                responseRooms(message, data.getData());
                break;
            case "__queryUsers":
                responseUsers(message, data.getData());
                break;
            default:
                break;
        }

    }

    // 创建房间
    private void createRoom(String message, Map<String, Object> data) {
        String room   = (String) data.get("room");
        String userId = (String) data.get("userID");

        System.out.println(String.format("createRoom:%s ", room));

        RoomInfo roomParam = rooms.get(room);
        // 没有这个房间
        if (roomParam == null) {
            int size = (int) Double.parseDouble(String.valueOf(data.get("roomSize")));
            // 创建房间
            RoomInfo roomInfo = new RoomInfo();
            roomInfo.setMaxSize(size);
            roomInfo.setRoomId(room);
            roomInfo.setUserId(userId);
            // 将房间储存起来
            rooms.put(room, roomInfo);


            CopyOnWriteArrayList<UserBean> copy = new CopyOnWriteArrayList<>();
            // 将自己加入到房间里
            UserBean my = MemCons.userBeans.get(userId);
            copy.add(my);
            rooms.get(room).setUserBeans(copy);
            EventData send = new EventData();
            send.setEventName("__peers");
            Map<String, Object> map = new HashMap<>();
            map.put("connections", "");
            map.put("you", userId);
            map.put("roomSize", size);
            send.setData(map);
            System.out.println(gson.toJson(send));
            sendMsg(my, -1, gson.toJson(send));

        }

    }

    // 首次邀请
    private void invite(String message, Map<String, Object> data) {
        String   userList  = (String) data.get("userList");
        String   room      = (String) data.get("room");
        String   inviteId  = (String) data.get("inviteID");
        boolean  audioOnly = (boolean) data.get("audioOnly");
        String[] users     = userList.split(",");

        System.out.println(String.format("room:%s,%s invite %s audioOnly:%b", room, inviteId, userList, audioOnly));
        // 给其他人发送邀请
        for (String user : users) {
            UserBean userBean = MemCons.userBeans.get(user);
            if (userBean != null) {
                sendMsg(userBean, -1, message);
            }
        }


    }

    // 响铃回复
    private void ring(String message, Map<String, Object> data) {
        String room     = (String) data.get("room");
        String inviteId = (String) data.get("toID");

        UserBean userBean = MemCons.userBeans.get(inviteId);
        if (userBean != null) {
            sendMsg(userBean, -1, message);
        }
    }

    // 取消拨出
    private void cancel(String message, Map<String, Object> data) {
        String   room     = (String) data.get("room");
        String   userList = (String) data.get("userList");
        String[] users    = userList.split(",");
        for (String userId : users) {
            UserBean userBean = MemCons.userBeans.get(userId);
            if (userBean != null) {
                sendMsg(userBean, -1, message);
            }
        }

        if (rooms.get(room) != null) {
            rooms.remove(room);
        }


    }

    // 拒绝接听
    private void reject(String message, Map<String, Object> data) {
        String   room     = (String) data.get("room");
        String   toID     = (String) data.get("toID");
        UserBean userBean = MemCons.userBeans.get(toID);
        if (userBean != null) {
            sendMsg(userBean, -1, message);
        }
        RoomInfo roomInfo = rooms.get(room);
        if (roomInfo != null) {
            if (roomInfo.getMaxSize() == 2) {
                rooms.remove(room);
            }
        }


    }

    // 加入房间
    private void join(String message, Map<String, Object> data) {
        String room   = (String) data.get("room");
        String userID = (String) data.get("userID");

        RoomInfo roomInfo = rooms.get(room);

        int                            maxSize       = roomInfo.getMaxSize();
        CopyOnWriteArrayList<UserBean> roomUserBeans = roomInfo.getUserBeans();

        //房间已经满了
        if (roomUserBeans.size() >= maxSize) {
            return;
        }
        UserBean my = MemCons.userBeans.get(userID);
        // 1. 將我加入到房间
        roomUserBeans.add(my);
        roomInfo.setUserBeans(roomUserBeans);
        rooms.put(room, roomInfo);

        // 2. 返回房间里的所有人信息
        EventData send = new EventData();
        send.setEventName("__peers");
        Map<String, Object> map = new HashMap<>();

        String[] cons = new String[roomUserBeans.size()];
        for (int i = 0; i < roomUserBeans.size(); i++) {
            UserBean userBean = roomUserBeans.get(i);
            if (userBean.getUserId().equals(userID)) {
                continue;
            }
            cons[i] = userBean.getUserId();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cons.length; i++) {
            if (cons[i] == null) {
                continue;
            }
            sb.append(cons[i]).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        map.put("connections", sb.toString());
        map.put("you", userID);
        map.put("roomSize", roomInfo.getMaxSize());
        send.setData(map);
        sendMsg(my, -1, gson.toJson(send));


        EventData newPeer = new EventData();
        newPeer.setEventName("__new_peer");
        Map<String, Object> sendMap = new HashMap<>();
        sendMap.put("userID", userID);
        newPeer.setData(sendMap);

        // 3. 给房间里的其他人发送消息
        for (UserBean userBean : roomUserBeans) {
            if (userBean.getUserId().equals(userID)) {
                continue;
            }
            sendMsg(userBean, -1, gson.toJson(newPeer));
        }


    }

    // 切换到语音接听
    private void transAudio(String message, Map<String, Object> data) {
        String   userId   = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("用户 " + userId + " 不存在");
            return;
        }
        sendMsg(userBean, -1, message);
    }

    // 意外断开
    private void disconnet(String message, Map<String, Object> data) {
        String   userId   = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("用户 " + userId + " 不存在");
            return;
        }
        sendMsg(userBean, -1, message);
    }

    // 发送offer
    private void offer(String message, Map<String, Object> data) {
        String   userId   = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        sendMsg(userBean, -1, message);
    }

    // 发送answer
    private void answer(String message, Map<String, Object> data) {
        String   userId   = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("用户 " + userId + " 不存在");
            return;
        }
        sendMsg(userBean, -1, message);

    }

    // 发送ice信息
    private void iceCandidate(String message, Map<String, Object> data) {
        String   userId   = (String) data.get("userID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("用户 " + userId + " 不存在");
            return;
        }
        sendMsg(userBean, -1, message);
    }

    // 离开房间
    private void leave(String message, Map<String, Object> data) {
        String room   = (String) data.get("room");
        String userId = (String) data.get("fromID");
        if (userId == null) return;
        RoomInfo                       roomInfo          = rooms.get(room);
        CopyOnWriteArrayList<UserBean> roomInfoUserBeans = roomInfo.getUserBeans();
        Iterator<UserBean>             iterator          = roomInfoUserBeans.iterator();
        while (iterator.hasNext()) {
            UserBean userBean = iterator.next();
            if (userId.equals(userBean.getUserId())) {
                continue;
            }
            sendMsg(userBean, -1, message);

            if (roomInfoUserBeans.size() == 1) {
                System.out.println("房间里只剩下一个人");
                if (roomInfo.getMaxSize() == 2) {
                    rooms.remove(room);
                }
            }

            if (roomInfoUserBeans.size() == 0) {
                System.out.println("房间无人");
                rooms.remove(room);
            }
        }
    }

    // 发送查询 User
    private void responseUsers(String message, Map<String, Object> data) {
        String   userId   = (String) data.get("fromID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("用户 " + userId + " 不存在");
            return;
        }


        try {
            List<UserBean> userBeans = UserControl.userList();
            JSONObject     root      = new JSONObject();

            List<UserInfo> persons = new ArrayList<UserInfo>();

            // String str = gson.toJson(userBeans);
            // Log.d(TAG, "responseUsers: str="+str);

            for (UserBean userBean1 : userBeans) {
                UserInfo userInfo = new UserInfo();
                userInfo.setUserId(userBean1.getUserId());
                userInfo.setAvatar(userBean1.getAvatar());
                userInfo.setNickName(userBean1.getUserId());
                persons.add(userInfo);
            }
            String str = gson.toJson(persons);
            root.put("data", str);
            root.put("eventName", "__queryUsers");
            sendMsg(userBean, -1, root.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "responseUsers: ", e);
        }

    }

    // 发送查询 房间信息
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void responseRooms(String message, Map<String, Object> data) {
        String   userId   = (String) data.get("fromID");
        UserBean userBean = MemCons.userBeans.get(userId);
        if (userBean == null) {
            System.out.println("用户 " + userId + " 不存在");
            return;
        }

        try {
            List<RoomInfo> roomInfos = UserControl.roomList();
            JSONObject     root      = new JSONObject();


            List<RoomBean> persons = new ArrayList<RoomBean>();
            for (RoomInfo roomInfo1 : roomInfos) {
                RoomBean roomBean = new RoomBean();
                roomBean.setUserId(roomInfo1.getUserId());
                roomBean.setRoomId(roomInfo1.getRoomId());
                roomBean.setCurrentSize(roomInfo1.getCurrentSize());
                roomBean.setMaxSize(roomInfo1.getMaxSize());
                persons.add(roomBean);
            }
            String str = gson.toJson(persons);
            root.put("data", str);
            root.put("eventName", "__queryRooms");
            sendMsg(userBean, -1, root.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private static final Object object = new Object();

    // 给不同设备发送消息
    private void sendMsg(UserBean userBean, int device, String str) {
        Log.d(TAG, "sendMsg: " + str);
        if (device == 0) {
            WebSocket phoneSession = userBean.getPhoneSession();
            if (phoneSession != null) {
                synchronized (object) {
                    phoneSession.send(str);
                }
            }
        } else if (device == 1) {
            WebSocket pcSession = userBean.getPcSession();
            if (pcSession != null) {
                synchronized (object) {
                    pcSession.send(str);
                }
            }
        } else {
            WebSocket phoneSession = userBean.getPhoneSession();
            if (phoneSession != null) {
                synchronized (object) {
                    phoneSession.send(str);
                }
            }
            WebSocket pcSession = userBean.getPcSession();
            if (pcSession != null) {
                synchronized (object) {
                    pcSession.send(str);
                }
            }

        }

    }

}
