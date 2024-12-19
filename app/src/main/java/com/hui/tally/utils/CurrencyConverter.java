package com.hui.tally.utils;

import android.util.Log;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;

public class CurrencyConverter {
    private static final String TAG = "CurrencyConverter";
    private static final String API_HOST = "https://tysjhlcx.market.alicloudapi.com";
    private static final String API_PATH = "/exchange_rate/convert";
    private static String APP_CODE = "fb540c07df404866b7bc86207908ad82"; // 需要设置实际的AppCode
    private static final OkHttpClient client = new OkHttpClient();

    public static void setAppCode(String appCode) {
        APP_CODE = appCode;
    }

    public static float convertToCNY(float amount, String fromCurrency) {
        if (fromCurrency.equals("CNY")) {
            return amount;
        }

        try {
            // 构建URL
            HttpUrl.Builder urlBuilder = HttpUrl.parse(API_HOST + API_PATH).newBuilder();
            urlBuilder.addQueryParameter("fromCode", fromCurrency);
            urlBuilder.addQueryParameter("toCode", "CNY");
            urlBuilder.addQueryParameter("money", String.valueOf(amount));

            // 构建请求
            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", "APPCODE " + APP_CODE)
                .get()
                .build();

            // 执行请求
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            
            // 解析响应
            JSONObject jsonResponse = new JSONObject(responseBody);
            if (jsonResponse.getBoolean("success")) {
                JSONObject data = jsonResponse.getJSONObject("data");
                return (float) data.getDouble("money");
            } else {
                Log.e(TAG, "Currency conversion failed: " + jsonResponse.getString("msg"));
                return amount;
            }
        } catch (Exception e) {
            Log.e(TAG, "Currency conversion failed", e);
            return amount;
        }
    }

    public static String[] getAvailableCurrencies() {
        return new String[] {
            "CNY", // 人民币
            "USD", // 美元
            "EUR", // 欧元
            "GBP", // 英镑
            "JPY", // 日元
            "KRW", // 韩元
            "HKD"  // 港币
        };
    }
} 