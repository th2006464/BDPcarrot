package com.zjfgh.bluedhook.simple;

import android.content.Context;

public class PlayingOnLiveBaseModeFragmentHook {
    private static PlayingOnLiveBaseModeFragmentHook instance;
    private final User watchingAnchor = new User();

    public static synchronized PlayingOnLiveBaseModeFragmentHook getInstance(Context appContext, Object moduleRes) {
        if (instance == null) {
            instance = new PlayingOnLiveBaseModeFragmentHook();
        }
        return instance;
    }

    public User getWatchingAnchor() {
        return watchingAnchor;
    }
}
