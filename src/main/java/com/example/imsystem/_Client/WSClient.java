package com.example.imsystem._Client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;

public class WSClient extends WebSocketClient {
    public WSClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("连接成功");
    }

    @Override
    public void onMessage(String s) {
        System.out.println("收到消息: "+s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("连接关闭");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("发生错误");
    }
}
