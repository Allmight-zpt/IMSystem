package com.example.imsystem.provider;

import com.alibaba.fastjson.JSON;
import com.example.imsystem.Bean.Result;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AIProvider {

    private String AIChatUrl = "http://localhost:5000/chat";

    public String chitChat(String robotName, String message) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(AIChatUrl + "?robotName=" + robotName + "&message=" + message).build();
        //生成一个准备好请求的call对象
        Call call = okHttpClient.newCall(request);
        try {
            //execute会阻塞线程
            Response response = call.execute();
            Result result = JSON.parseObject(response.body().string(), Result.class);
            if(result.getCode().equals("200")){
                return result.getMessage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "我服了你了,我不知道要说啥了捏~";
    }
}
