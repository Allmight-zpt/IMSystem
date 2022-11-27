package com.example.imsystem.Server;

import com.example.imsystem.Bean.Client;
import com.example.imsystem.provider.AIProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@ServerEndpoint(value = "/socketserver/{accountId}")
@Component
public class SocketServer {

    /**
     * 打印日志
     * */
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    /**
     * 存放用户连接信息，使用CopyOnWriteArraySet保证线程安全
     */
    private static CopyOnWriteArraySet<Client> clients = new CopyOnWriteArraySet<Client>();
    /**
     * 备份聊天记录
     * */
    public static Map<String, List<String> > allChatData = new HashMap<String, List<String> >();
    /**
     * webSocket封装好的session，用来进行信息的推送和接收
     * */
    private Session session;

    /**
     * 与AISystem进行通讯
     * */

    private AIProvider aiProvider = new AIProvider();

    /**
     * 用户连接时触发，也就是访问这个endpoint时，即访问ws://localhost:8082/socketserver/{accountId}的时候
     * */
    @OnOpen
    public void open(Session session, @PathParam(value = "accountId")String accountId){
        Map<String, List<String>> map = session.getRequestParameterMap();
        List<String> username = map.get("username");
        this.session = session;
        Client client = new Client(accountId, username.get(0), session);
        List<String> chatData = allChatData.get(accountId);
        if(chatData != null){
            client.setChatData(chatData);
            clients.add(client);
        }else{
            clients.add(client);
            sendMessage("【系统消息】欢迎使用本站的即时通讯系统~下面是即时通讯系统的使用规则。<br><br>1.消息默认发送给本站服务器后台。<br>2.以【@All 】开头可以将消息发送给全站用户。<br>3.以【@用户名 】开头可以将消息私发给【互关】的好友。<br>4.发送【ID】获取您聊天使用的ID。<br>5.发送【help】在此弹出该帮助信息。<br>6.其他功能还在开发中，尽情期待！",client.getAccountId());
        }
        logger.info("客户端【{}】连接成功！",accountId);
    }

    /**
     * 收到客户端的信息时触发
     * */
    @OnMessage
    public void onMessage(String message){
        //发送消息的时候也会再次调用open函数，也就是当前的session也会刷新
        Client client = clients.stream().filter(cli -> cli.getSession() == session)
                .collect(Collectors.toList()).get(0);
        client.addChatData("client:" + message);
        logger.info("收到客户端【{}】的信息【{}】",client.getAccountId(),message);
        //获取发送对象
        String target = message.split(" ")[0];
        /**
         * 1. 存在发送对象时
         * */
        if(target!=null && target.charAt(0) == '@'){
            /**
             * 1.1. 发送全站消息
             * */
            if(target.equals("@All")){
                sendAll("【" + client.getUsername() + "】的全站消息: " + message);
            }
            /**
             * 1.2. 与机器人客户对话
             */
            else if(target.equals("@robot0")){
                String response = aiProvider.chitChat("robot0",message);
                sendMessage(response, client.getAccountId());
            }
            else if(target.equals("@robot1")){
                String response = aiProvider.chitChat("robot1",message);
                sendMessage(response, client.getAccountId());
            }
            else if(target.equals("@robot2")){
                String response = aiProvider.chitChat("robot2",message);
                sendMessage(response, client.getAccountId());
            }
            /**
             * 1.3. 与用户对话
             * */
            else{
                String username = target.substring(1);
                List<Client> targetClients = clients.stream().filter(cli -> cli.getUsername().equals(username))
                        .collect(Collectors.toList());
                if(targetClients.size() != 0){
                    sendMessage("【" + client.getUsername() + "】的私发消息: " + message,targetClients.get(0).getAccountId());
                }else{
                    sendMessage("您发送的用户暂时未上线！",client.getAccountId());
                }
            }
        }
        /**
         * 2. 获取自己ID
         * */
        else if(message.equals("ID")){
            sendMessage("【系统消息】您的ID为: " + client.getAccountId(),client.getAccountId());
        }
        /**
         * 3. 获取帮助信息
         * */
        else if(message.equals("help")){
            sendMessage("【系统消息】欢迎使用本站的即时通讯系统~下面是即时通讯系统的使用规则。<br><br>1.消息默认发送给本站服务器后台。<br>2.以【@All 】开头可以将消息发送给全站用户。<br>3.以【@用户名 】开头可以将消息私发给【互关】的好友。<br>4.发送【|ID|】获取您聊天使用的ID。<br>5.发送【|help|】在此弹出该帮助信息。<br>6.其他功能还在开发中，尽情期待！",client.getAccountId());
        }
        /**
         * 4. 后台留言功能
         * */
        else{
            sendMessage("【系统消息】您的留言以记录在本站后台，感谢您的留言！",client.getAccountId());
        }
    }

    /**
     * 连接关闭时触发
     * */
    @OnClose
    public void onClose(){
        clients.forEach(cli -> {
            if(cli.getSession().getId().equals(session.getId())){
                logger.info("客户端【{}】断开连接",cli.getAccountId());
                allChatData.put(cli.getAccountId(),cli.getChatData());
                clients.remove(cli);
            }
        });
    }

    /**
     * 发生错误时调用
     * */
    @OnError
    public void onError(Throwable error){
        clients.forEach(cli -> {
            logger.info("客户端【{}】发生异常断开连接",cli.getAccountId());
            clients.remove(cli);
            error.printStackTrace();
        });
    }

    /**
     * 给所有用户发送消息
     * */
    public synchronized void sendAll(String message) {
        clients.forEach(cli -> {
            if(!cli.getSession().equals(session)){
                try {
                    cli.getSession().getBasicRemote().sendText(message);
                    cli.addChatData("server:" + message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        logger.info("服务器推送所有客户端如下信息:【{}】",message);
    }

    /**
     * 给服务器发送消息
     * */
    public synchronized void sendMessage(String message, String username) {
        clients.forEach(cli -> {
            if(username.equals(cli.getAccountId())){
                try {
                    cli.getSession().getBasicRemote().sendText(message);
                    cli.addChatData("server:" + message);
                    logger.info("服务器推送给客户端【{}】如下信息:【{}】",username,message);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    /***********待实现***********/

    /**
     * 获取在线用户数
     * */

    /**
     * 获取在线用户名字
     * */



}
