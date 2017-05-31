package com.lijunyan.blackmusic.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static String requestBingPic = "http://guolin.tech/api/bing_pic"; //郭霖开放的必应每日一图接口

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
