package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserPopupWindow {

    private PopupWindow popupWindow;
    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private Button anchorStartButton;

    @SuppressLint("StaticFieldLeak")
    private static UserPopupWindow instance;

    private Handler handler;
    private Runnable checkRunnable;
    private int currentCheckIndex = 0;
    private boolean isChecking = false;

    private int originalStatusBarColor;
    private int originalNavigationBarColor;
    private int originalSystemUiVisibility;
    private boolean originalLightStatusBars;
    private boolean originalLightNavBars;
    private boolean hasSavedOriginalState = false;

    private final SQLiteManagement dbManager = SQLiteManagement.getInstance();

    public static UserPopupWindow getInstance() {
        if (instance == null) {
            instance = new UserPopupWindow();
        }
        return instance;
    }

    public void show(Context context) {
        if (popupWindow != null && popupWindow.isShowing()) return;
        Activity activity = (Activity) context;

        if (!hasSavedOriginalState) {
            saveOriginalSystemBarState(activity);
            hasSavedOriginalState = true;
        }

        int layoutId = getResId(context, "anchor_monitor_layout", "layout");
        View rootView = LayoutInflater.from(context)
                .inflate(AppContainer.getInstance().getModuleRes().getLayout(layoutId), null);

        int listId = getResId(context, "anchor_monitor_list", "id");
        LinearLayout anchorMonitorList = rootView.findViewById(listId);

        int buttonId = getResId(context, "anchor_start_button", "id");
        anchorStartButton = rootView.findViewById(buttonId);

        recyclerView = new RecyclerView(context);
        LinearLayout.LayoutParams recyclerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        recyclerView.setLayoutParams(recyclerParams);
        anchorMonitorList.addView(recyclerView);

        adapter = new UserListAdapter(context, user -> delAnchor(user.getUid(), user.getName()));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        GradientDrawable transparentDivider = new GradientDrawable();
        transparentDivider.setSize(0, 10);
        transparentDivider.setColor(Color.TRANSPARENT);
        divider.setDrawable(transparentDivider);
        recyclerView.addItemDecoration(divider);

        int neonButtonRes = getResId(context, "neon_button", "drawable");
        Drawable buttonBg = AppContainer.getInstance().getModuleRes().getDrawable(neonButtonRes, null);
        anchorStartButton.setBackground(buttonBg);
        anchorStartButton.setText(isChecking ? "停止检测" : "开始定时检测");

        anchorStartButton.setOnClickListener(v -> {
            if (isChecking) {
                stopChecking();
                anchorStartButton.setText("开始定时检测");
            } else {
                startChecking();
                anchorStartButton.setText("停止检测");
            }
        });

        popupWindow = new PopupWindow(
                rootView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        popupWindow.setOnDismissListener(() -> restoreOriginalSystemBarState(activity));
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        updateSystemBarsForPopup(activity);

        UserDataManager.getInstance().getUserLiveData().observeForever(users -> {
            if (adapter != null) adapter.submitList(users);
        });
    }

    private int getResId(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }

    private void saveOriginalSystemBarState(Activity activity) {
        Window window = activity.getWindow();
        originalStatusBarColor = window.getStatusBarColor();
        originalNavigationBarColor = window.getNavigationBarColor();
        originalSystemUiVisibility = window.getDecorView().getSystemUiVisibility();
        originalLightStatusBars = (originalSystemUiVisibility & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0;
        originalLightNavBars = (originalSystemUiVisibility & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) != 0;
    }

    private void restoreOriginalSystemBarState(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(originalStatusBarColor);
        window.setNavigationBarColor(originalNavigationBarColor);
        int newVisibility = originalSystemUiVisibility;
        newVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        newVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        if (originalLightStatusBars) newVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (originalLightNavBars) newVisibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        window.getDecorView().setSystemUiVisibility(newVisibility);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(
                        originalLightStatusBars ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                insetsController.setSystemBarsAppearance(
                        originalLightNavBars ? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            }
        }
    }

    private void updateSystemBarsForPopup(Activity activity) {
        Window window = activity.getWindow();
        int newColor = Color.parseColor("#FF1A1A1A");
        window.setStatusBarColor(newColor);
        window.setNavigationBarColor(newColor);
        int newVisibility = window.getDecorView().getSystemUiVisibility();
        newVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        newVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        boolean isLightColor = isColorLight(newColor);
        if (isLightColor) {
            newVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            newVisibility |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        window.getDecorView().setSystemUiVisibility(newVisibility);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController = window.getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(
                        isLightColor ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                insetsController.setSystemBarsAppearance(
                        isLightColor ? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            }
        }
    }

    private boolean isColorLight(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            restoreOriginalSystemBarState((Activity) popupWindow.getContentView().getContext());
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    private void startChecking() {
        if (adapter == null || adapter.getCurrentList().isEmpty()) {
            Toast.makeText(popupWindow.getContentView().getContext(), "用户列表为空", Toast.LENGTH_SHORT).show();
            return;
        }
        isChecking = true;
        currentCheckIndex = 0;
        handler = new Handler(Looper.getMainLooper());
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isChecking || adapter == null) return;
                List<User> users = adapter.getCurrentList();
                if (currentCheckIndex >= users.size()) currentCheckIndex = 0;
                User currentUser = users.get(currentCheckIndex);
                adapter.setCurrentCheckingUid(currentUser.getUid());
                checkUserHomepage(currentUser);
                currentCheckIndex++;
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(checkRunnable);
    }

    private void stopChecking() {
        isChecking = false;
        anchorStartButton.setText("开始定时检测");
        if (handler != null && checkRunnable != null) handler.removeCallbacks(checkRunnable);
    }

    private void checkUserHomepage(User user) {
        String uid = user.getUid();
        Map<String, String> authMap = AuthManager.auHook(false,
                AppContainer.getInstance().getClassLoader(),
                AppContainer.getInstance().getBluedContext());
        NetworkManager.getInstance().getAsync(
                NetworkManager.getBluedUserBasicAPI(uid),
                authMap,
                new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("UserPopupWindow", "检测失败: " + e.getMessage());
                        clearCheckUid(uid);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            JSONObject data = JSON.parseObject(response.body().string());
                            int code = data.getIntValue("code");
                            if (code == 200) {
                                JSONArray dataArr = data.getJSONArray("data");
                                JSONObject userObj = dataArr.getJSONObject(0);
                                long live = userObj.getLongValue("live");
                                User sqlUser = dbManager.getUserByUid(uid);
                                if (live > 0 && Long.parseLong(sqlUser.getLive()) != live) {
                                    dbManager.updateUserLive(uid, live);
                                }
                                if (!sqlUser.isVoiceReminded() && sqlUser.isVoiceRemind()) {
                                    dbManager.updateUserVoiceReminded(uid, true);
                                    VoiceTTS.getInstance(AppContainer.getInstance().getBluedContext())
                                            .speakAdd(user.getName() + " 正在 Blued 直播。");
                                }
                            }
                        } catch (Exception e) {
                            Log.e("UserPopupWindow", "解析失败", e);
                        } finally {
                            clearCheckUid(uid);
                        }
                    }
                });
    }

    private void clearCheckUid(String uid) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (adapter != null && uid.equals(adapter.currentCheckingUid)) {
                adapter.setCurrentCheckingUid(null);
            }
        });
    }

    public void addAnchor(User user) {
        boolean localAddSuccess = dbManager.addOrUpdateUser(user);
        if (localAddSuccess) UserDataManager.getInstance().addUser(user);
    }

    public void delAnchor(String uid, String name) {
        boolean localDeleteSuccess = dbManager.deleteUser(uid);
        if (localDeleteSuccess) UserDataManager.getInstance().removeUser(uid);
        if (isChecking && uid.equals(adapter.currentCheckingUid)) stopChecking();
    }
}
