package com.coolweather.android.util;

import android.util.Log;

import com.coolweather.android.gson.WeatherForecast;

import com.google.gson.Gson;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {

    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static void snedRequest(final String locationId, final String kind, final HandleResult handleResult){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://free-api.heweather.net/s6/weather/"+kind+"?location="+locationId+"&key=2d133c6c978a4213863a635f6d48ebee")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    handleResult.OnSuccess(responseData);
                } catch (IOException e) {
                    e.printStackTrace();
                    handleResult.OnFailed();
                    Log.d("<<><>>","Error");
                }
            }
        }).start();
    }

}