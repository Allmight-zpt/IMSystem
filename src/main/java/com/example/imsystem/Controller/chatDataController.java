package com.example.imsystem.Controller;

import com.example.imsystem.Bean.Result;
import com.example.imsystem.Server.SocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class chatDataController {

    @Autowired
    SocketServer socketServer;

    /**
     * 获取历史聊天记录
     * */
    @ResponseBody
    @RequestMapping(value = "/chatData/{accountId}",method = RequestMethod.GET)
    public Result getChatDataByUsername(@PathVariable("accountId") String accountId){
        Map<String, List<String>> allChatData = socketServer.allChatData;
        List<String> chatData = allChatData.get(accountId);
        Result result = new Result();
        result.setData(chatData);
        if(chatData == null){
            result.setCode(String.valueOf(201));
            result.setMessage("历史记录为空");
        }else{
            result.setCode(String.valueOf(200));
            result.setMessage("历史记录不为空");
        }
        return result;
    }
}
