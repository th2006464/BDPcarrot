package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.content.res.XModuleResources;

public class FragmentMineNewBindingHook {
    private static FragmentMineNewBindingHook instance;
    private final ClassLoader classLoader;
    private final XModuleResources modRes;
    private final Context context;

    private FragmentMineNewBindingHook(Context context, XModuleResources modRes) {
        this.context = context;
        this.classLoader = context.getClassLoader();
        this.modRes = modRes;
    }

    public static synchronized FragmentMineNewBindingHook getInstance(Context context, XModuleResources modRes) {
        if (instance == null) {
            instance = new FragmentMineNewBindingHook(context, modRes);
        }
        return instance;
    }

    public Context getSafeContext() {
        return context;
    }

    public void hook() {
        // Empty implementation
    }
}
