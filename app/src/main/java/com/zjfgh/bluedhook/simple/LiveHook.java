package com.zjfgh.bluedhook.simple;

import android.content.Context;

public class LiveHook {
    private static LiveHook instance;
    private final Context appContext;
    private final ClassLoader classLoader;

    public static synchronized LiveHook getInstance(Context appContext) {
        if (instance == null) {
            instance = new LiveHook(appContext);
        }
        return instance;
    }

    private LiveHook(Context appContext) {
        this.appContext = appContext;
        this.classLoader = appContext.getClassLoader();
    }

    public void anchorMonitorHook() {
        // Empty implementation
    }

    public void joinLiveHook() {
        // Empty implementation
    }

    public void setAnchorMonitorIvVisibility(boolean isVisibility) {
        // Empty implementation
    }
}
