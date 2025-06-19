package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.content.res.XModuleResources;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Timer;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public class LikeFollowModel {
    private static LikeFollowModel instance;
    private final WeakReference<Context> appContextRef;
    private final XModuleResources modRes;
    private final ClassLoader classLoader;

    private LikeFollowModel(Context appContext, XModuleResources modRes) {
        this.appContextRef = new WeakReference<>(appContext);
        this.classLoader = appContextRef.get().getClassLoader();
        this.modRes = modRes;
        shieldLikeHook();
    }

    // 获取单例实例
    public static synchronized LikeFollowModel getInstance(Context appContext, XModuleResources modRes) {
        if (instance == null) {
            instance = new LikeFollowModel(appContext, modRes);
        }
        return instance;
    }

    public void shieldLikeHook() {
        XposedHelpers.findAndHookMethod("com.martin.fastframe.ui.like.follow.LikeFollowModel", classLoader, "a", int.class, int.class, "android.view.ViewGroup", "com.martin.fastframe.ui.like.follow.ILikeFollowData", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                SettingItem shieldLikeSettingItem = SQLiteManagement.getInstance().getSettingByFunctionId(SettingsViewCreator.SHIELD_LIKE);
                if (shieldLikeSettingItem.isSwitchOn()) {
                    param.setResult(null);
                }
            }
        });
    }
}
