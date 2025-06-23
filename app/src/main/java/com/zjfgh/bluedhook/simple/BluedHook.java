package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.Objects;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BluedHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public static WSServerManager wsServerManager;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) {
        if (param.packageName.equals("com.soft.blued")) {
            XposedHelpers.findAndHookMethod("com.soft.blued.StubWrapperProxyApplication", param.classLoader, "initProxyApplication", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context bluedContext = (Context) param.args[0];
                    AppContainer.getInstance().setBluedContext(bluedContext);
                    AppContainer.getInstance().setClassLoader(bluedContext.getClassLoader());
                    Toast.makeText(bluedContext, "外挂成功！", Toast.LENGTH_LONG).show();
                    try {
                        VoiceTTS.getInstance(bluedContext);
                    } catch (Exception e) {
                        Log.e("BluedHook", "语音合成模块异常：\n" + e);
                    }
                    NetworkManager.getInstance();
                    UserInfoFragmentNewHook.getInstance(bluedContext, AppContainer.getInstance().getModuleRes());
                    LiveHook.getInstance(bluedContext);
                    PlayingOnLiveBaseModeFragmentHook.getInstance(bluedContext, AppContainer.getInstance().getModuleRes());
                    FragmentMineNewBindingHook.getInstance(bluedContext, AppContainer.getInstance().getModuleRes());
                    LiveMultiBoyItemViewHook.getInstance();
                    ChatHook.getInstance(bluedContext, AppContainer.getInstance().getModuleRes());
                    NearbyPeopleFragment_ViewBindingHook.getInstance(AppContainer.getInstance().getBluedContext(), AppContainer.getInstance().getClassLoader());
                    HornViewNewHook.autoHornViewNew();
                    LikeFollowModel.getInstance(bluedContext, AppContainer.getInstance().getModuleRes());
                    wsServerManager = new WSServerManager(new WSServerManager.WSServerListener() {
                        @Override
                        public void onServerStarted(int port) {
                            ModuleTools.showBluedToast("WS服务已启动在" + port + "端口上");
                        }

                        @Override
                        public void onServerStopped() {
                            ModuleTools.showBluedToast("WS服务已停止");
                        }

                        @Override
                        public void onServerError(String error) {
                            ModuleTools.showBluedToast("WS服务发生了错误：" + error);
                        }

                        @Override
                        public void onClientConnected(String address) {
                        }

                        @Override
                        public void onClientDisconnected(String address) {
                        }

                        @Override
                        public void onMessageReceived(WebSocket conn, String message) {
                            if (message.equals("同步数据")) {
                                try {
                                    JSONObject response = new JSONObject();
                                    response.put("msgType", 1995);
                                    JSONObject msgExtra = new JSONObject();
                                    msgExtra.put("msgType", "lotteryRecords");
                                    JSONObject recordsData = new FileToJsonConverter().convertFilesToJson();
                                    msgExtra.put("msgExtra", recordsData);
                                    response.put("msgExtra", msgExtra);
                                    String jsonResponse = response.toString();
                                    Log.d("WebSocketServer", "Broadcasting records: " + jsonResponse);
                                    wsServerManager.broadcastMessage(jsonResponse);
                                } catch (Exception e) {
                                    Log.e("WebSocketServer", "Error processing sync request", e);
                                }
                            }
                        }
                    });
                    // Hook 设置界面的视图创建
                    hookSettingsFragment(param.classLoader);
                }
            });
        }
    }

    private void hookSettingsFragment(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(
                "com.soft.blued.ui.setting.fragment.SettingFragment",
                classLoader,
                "onViewCreated",
                View.class,
                android.os.Bundle.class,
                new XC_MethodHook() {
                    @SuppressLint("ResourceType")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        View fragmentView = (View) param.args[0];
                        Context bluedContext = AppContainer.getInstance().getBluedContext();
                        int scrollView1ID = bluedContext.getResources().getIdentifier("scrollView1", "id", bluedContext.getPackageName());
                        ScrollView scrollView = fragmentView.findViewById(scrollView1ID);
                        if (scrollView == null) {
                            Log.e("BluedHook", "scrollView1 not found");
                            return;
                        }
                        LinearLayout scrollLinearLayout = (LinearLayout) scrollView.getChildAt(0);
                        if (scrollLinearLayout == null) {
                            Log.e("BluedHook", "scrollLinearLayout not found");
                            return;
                        }

                        // 动态创建“复制授权信息”布局
                        LinearLayout mySettingsLayoutAu = createSettingsItemLayout(bluedContext);
                        TextView auCopyTitleTv = mySettingsLayoutAu.findViewById(R.id.settings_name);
                        auCopyTitleTv.setText("复制授权信息(请勿随意泄漏)");
                        mySettingsLayoutAu.setOnClickListener(v -> AuthManager.auHook(true, AppContainer.getInstance().getClassLoader(), bluedContext));

                        // 动态创建“外挂模块设置”布局
                        LinearLayout moduleSettingsLayout = createSettingsItemLayout(bluedContext);
                        TextView moduleSettingsTitleTv = moduleSettingsLayout.findViewById(R.id.settings_name);
                        moduleSettingsTitleTv.setText("外挂模块设置");
                        moduleSettingsLayout.setOnClickListener(view -> {
                            AlertDialog dialog = getAlertDialog(bluedContext);
                            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.CENTER);
                            dialog.getWindow().setLayout(100, 300);
                            dialog.setOnShowListener(dialogInterface -> {
                                View parentView = dialog.getWindow().getDecorView();
                                parentView.setBackgroundColor(Color.parseColor("#F7F6F7"));
                            });
                            dialog.show();
                        });

                        // 添加到 ScrollView
                        scrollLinearLayout.addView(mySettingsLayoutAu, 0);
                        scrollLinearLayout.addView(moduleSettingsLayout, 1);
                    }
                }
        );
    }

    private LinearLayout createSettingsItemLayout(Context context) {
        // 创建 LinearLayout 容器
        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(
                ModuleTools.dpToPx(16),
                ModuleTools.dpToPx(12),
                ModuleTools.dpToPx(16),
                ModuleTools.dpToPx(12)
        );

        // 创建 TextView
        TextView titleTextView = new TextView(context);
        titleTextView.setId(R.id.settings_name);
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setTextSize(16);
        titleTextView.setPadding(
                ModuleTools.dpToPx(8),
                ModuleTools.dpToPx(8),
                ModuleTools.dpToPx(8),
                ModuleTools.dpToPx(8)
        );

        // 设置背景
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(ModuleTools.dpToPx(8));
        layout.setBackground(background);

        // 添加 TextView 到 LinearLayout
        layout.addView(titleTextView);
        return layout;
    }

    private AlertDialog getAlertDialog(Context context) {
        SettingsViewCreator creator = new SettingsViewCreator(context);
        View settingsView = creator.createSettingsView();
        creator.setOnSwitchCheckedChangeListener((functionId, isChecked) -> {
            if (functionId == SettingsViewCreator.ANCHOR_MONITOR_LIVE_HOOK) {
                LiveHook.getInstance(AppContainer.getInstance().getBluedContext()).setAnchorMonitorIvVisibility(isChecked);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(settingsView);
        return builder.create();
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        AppContainer.getInstance().setModulePath(startupParam.modulePath);
    }
}
