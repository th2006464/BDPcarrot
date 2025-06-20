package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PlayingOnLiveBaseModeFragmentHook {
    private static PlayingOnLiveBaseModeFragmentHook instance;

    public static PlayingOnLiveBaseModeFragmentHook getInstance(Context context, Object moduleRes) {
        if (instance == null) {
            instance = new PlayingOnLiveBaseModeFragmentHook();
        }
        return instance;
    }

    // 用户信息类（仅保留字段定义）
    public static class User {
        public String name;
        public String uid;
        public String live;
        public String avatar;

        public String getName() { return name; }
        public String getUid() { return uid; }
        public String getLive() { return live; }
    }

    // 获取当前主播信息
    public User getWatchingAnchor() {
        return new User();
    }

    // 注册 Hook 方法（空壳）
    public void registerHook(XC_LoadPackage.LoadPackageParam lpparam, Context bluedContext, Object moduleRes) {
        // 不做任何 Hook 操作
    }

    // 示例：屏蔽所有直播消息处理
    public void processLiveMessage() {
        // 不执行任何逻辑
    }
}
