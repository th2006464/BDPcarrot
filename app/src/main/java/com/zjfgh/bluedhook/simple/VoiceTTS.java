package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.util.Log;

public class VoiceTTS {
    private static final String TAG = "VoiceTTS";
    private static volatile VoiceTTS instance;
    private final WeakReference<Context> contextRef;

    private VoiceTTS(Context context) {
        this.contextRef = new WeakReference<>(context);
        Log.d(TAG, "VoiceTTS initialized");
    }

    public static VoiceTTS getInstance(Context context) {
        if (instance == null) {
            synchronized (VoiceTTS.class) {
                if (instance == null) {
                    instance = new VoiceTTS(context);
                }
            }
        }
        return instance;
    }

    public void speak(String text) {
        Log.d(TAG, "Speak requested: " + text);
    }

    public void speakAdd(String text) {
        Log.d(TAG, "SpeakAdd requested: " + text);
    }
}
