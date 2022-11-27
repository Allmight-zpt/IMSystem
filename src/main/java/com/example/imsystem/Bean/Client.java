package com.example.imsystem.Bean;


import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private String accountId;
    private String username;
    private Session session;
    private List<String> chatData = new ArrayList<String>();

    public List<String> getChatData() {
        return chatData;
    }

    public void addChatData(String data) {
        chatData.add(data);
    }

    public void setChatData(List<String> chatData) {
        this.chatData = chatData;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Client(String accountId, String username, Session session) {
        this.accountId = accountId;
        this.username = username;
        this.session = session;
    }
}
