package com.zjfgh.bluedhook.simple;

import okhttp3.*;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class NetworkManager {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 单例实例
    public static NetworkManager getInstance() {
        return new NetworkManager();
    }

    // 模拟 API 地址方法（返回空字符串）
    public static String getJinShanDocBluedAuthApi() { return ""; }
    public static String getJinShanDocBluedUsersApi() { return ""; }
    public static String getBluedUserCardAPI(String uid) { return ""; }
    public static String getBluedUserBasicAPI(String uid) { return ""; }
    public static String getBluedUserAPI(String uid) { return ""; }
    public static String getBluedLiveUserCard(String uid) { return ""; }
    public static String getBluedAnchorFansAPI(int page) { return ""; }
    public static String getAnchorFansFreeGoodsAPI() { return ""; }
    public static String getBuyGoodsApi() { return ""; }
    public static String getBluedSetUsersLocationApi(double latitude, double longitude) { return ""; }
    public static String getBluedLiveSearchAnchorApi(String content) { return ""; }
    public static String getBluedPicSaveStatusApi(String uid) { return ""; }
    public static String getBluedUserConnTypeApi(int start) { return ""; }
    public static String getUsersRecommendApi() { return ""; }

    // 获取 OkHttpClient 实例
    public OkHttpClient getClient() {
        return new OkHttpClient(); // 返回空实例
    }

    // 通用请求头管理
    public void addCommonHeader(String name, String value) {}
    public void removeCommonHeader(String name) {}
    public void clearCommonHeaders() {}

    // 同步 GET 请求
    public Response get(String url, Map<String, String> headers) throws IOException {
        return new Response.Builder().build(); // 返回空响应
    }

    public Response get(String url) throws IOException {
        return new Response.Builder().build();
    }

    // 异步 GET 请求
    public void getAsync(String url, Map<String, String> headers, Callback callback) {}
    public void getAsync(String url, Callback callback) {}

    // 同步 POST 请求
    public Response post(String url, String json, Map<String, String> headers) throws IOException {
        return new Response.Builder().build();
    }

    public Response post(String url, String json) throws IOException {
        return new Response.Builder().build();
    }

    // 异步 POST 请求
    public void postAsync(String url, String json, Map<String, String> headers, Callback callback) {}
    public void postAsync(String url, String json, Callback callback) {}

    // 添加请求头
    private void addHeaders(Request.Builder requestBuilder, Map<String, String> headers) {}
}
