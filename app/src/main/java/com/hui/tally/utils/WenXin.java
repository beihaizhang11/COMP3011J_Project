package com.hui.tally.utils;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class WenXin {
    public static final String API_KEY = "NN0TRbsXrNqKLZjV28fi6Xuc";
    public static final String SECRET_KEY = "WbKCRqPYZXRsrtesjkLAMj7MSb9csokE";
    public JSONArray dialogueContent;
    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    public WenXin() {
        dialogueContent = new JSONArray();
    }

    public String getAnswer(String userMsg) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("role", "user");
        jsonObject.put("content", userMsg);
        dialogueContent.put(jsonObject);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"messages\":" + 
                dialogueContent.toString() + 
                ",\"system\":\"你是一个智能助手，帮助用户解答记账相关的问题, 你要用英语回答问题, 用户发给你的内容也会是英文\",\"disable_search\":false,\"enable_citation\":false}");

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" + getAccessToken())
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = HTTP_CLIENT.newCall(request).execute();
        JSONObject jsonFeedback = new JSONObject(response.body().string());
        
        if (jsonFeedback.has("error_code")) {
            return "抱歉，发生错误：" + jsonFeedback.getString("error_msg");
        }
        
        String result = jsonFeedback.getString("result");

        JSONObject assistantResponse = new JSONObject();
        assistantResponse.put("role", "assistant");
        assistantResponse.put("content", result);
        dialogueContent.put(assistantResponse);

        return result;
    }

    public String getAccessToken() throws IOException, JSONException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }
} 