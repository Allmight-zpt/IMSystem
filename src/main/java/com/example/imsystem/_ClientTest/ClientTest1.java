package com.example.imsystem._ClientTest;

import com.example.imsystem._Client.WSClient;
import org.java_websocket.WebSocket;

import java.net.URI;

public class ClientTest1 {
    private static String url = "ws://localhost:8082/socketserver/client1";

    public static void main(String[] args){
        try {
            WSClient myClient = new WSClient(new URI(url));
            myClient.connect();
            // 判断是否连接成功，未成功后面发送消息时会报错
            while (!myClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
                System.out.println("连接中···请稍后");
                Thread.sleep(1000);
            }
            myClient.send("@All client1 say something to all clients");
            System.out.println("发送成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
