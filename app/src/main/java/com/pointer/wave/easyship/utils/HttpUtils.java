package com.pointer.wave.easyship.utils;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {

    private final OkHttpClient okHttpClient;
    private final String[] headers;

    public HttpUtils(String... headers){
        okHttpClient = new OkHttpClient();
        this.headers = headers;
    }

    public Call get(String url, String[] params){
        Request request = new Request.Builder().url(params != null ? url + toGet(params) : url).build();
        return okHttpClient.newCall(request);
    }

    public String sync_get(String url, String[] params){
        Request request = new Request.Builder().url(params != null ? url + toGet(params) : url).build();
        try {
            return okHttpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Call post(String url, String[] params){
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null){
            for (String param : params) {
                String[] kv = param.split("=");
                String value = "";
                try {
                    value = kv[1];
                }catch (Exception e){
                    e.printStackTrace();
                }
                builder.add(kv[0], value);
            }
        }
        Request request = new Request.Builder().post(builder.build()).url(url).build();
        return okHttpClient.newCall(request);
    }

    public String sync_post(String url, String[] params){
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null){
            for (String param : params) {
                String[] kv = param.split("=");
                String value = "";
                try {
                    value = kv[1];
                }catch (Exception e){
                    e.printStackTrace();
                }
                builder.add(kv[0], value);
            }
        }
        Request request = new Request.Builder().post(builder.build()).url(url).build();
        try {
            return okHttpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String toGet(String[] params){
        StringBuilder stringBuilder = new StringBuilder("?");
        for (int i = 0; i < params.length; i++) {
            stringBuilder.append(params[i]);
            if (i < params.length - 1) stringBuilder.append("&");
        }
        return stringBuilder.toString();
    }

    public boolean isGoodJson(String json) {
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonParseException e) {
            return false;
        }
    }

    /**
     * 同步请求
     * @param url 请求地址
     * @param type 请求类型
     * @param params 请求参数
     * @return 响应结果
     */
    public String syncHttp(String url, String type, String... params){
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null){
            for (String param : params) {
                String[] kv = param.split("=");
                String value = "";
                try {
                    value = kv[1];
                }catch (Exception e){
                    e.printStackTrace();
                }
                builder.add(kv[0], value);
            }
        }
        Request request;
        switch (type){
            case "post":
                request = new Request.Builder().post(builder.build()).url(url).build();
                break;
            case "get":
                request = new Request.Builder().url(params != null ? url + toGet(params) : url).build();
                break;
            case "put":
                request = new Request.Builder().put(builder.build()).url(url).build();
                break;
            case "delete":
                request = new Request.Builder().delete(builder.build()).url(url).build();
                break;
            default:
                request = new Request.Builder().url(params != null ? url + toGet(params) : url).build();
        }

        request.header("");

        try {
            return okHttpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 异步请求
     * @param url 请求地址
     * @param type 请求类型
     * @param params 请求参数
     * @return Call对象
     */
    public Call asyncHttp(String url, String type, String... params){
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null){
            for (String param : params) {
                String[] kv = param.split("=");
                String value = "";
                try {
                    value = kv[1];
                }catch (Exception e){
                    e.printStackTrace();
                }
                builder.add(kv[0], value);
            }
        }

        Request request;
        switch (type.toLowerCase()){
            case "post":
                request = new Request.Builder().post(builder.build()).url(url).build();
                break;
            case "get":
                request = new Request.Builder().url(params != null ? url + toGet(params) : url).build();
                break;
            case "put":
                request = new Request.Builder().put(builder.build()).url(url).build();
                break;
            case "delete":
                request = new Request.Builder().delete(builder.build()).url(url).build();
                break;
            default:
                request = new Request.Builder().url(params != null ? url + toGet(params) : url).build();
        }

        return okHttpClient.newCall(request);
    }

    public Call asyncPostJson(String url, String json){
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        return okHttpClient.newCall(request);
    }

}
