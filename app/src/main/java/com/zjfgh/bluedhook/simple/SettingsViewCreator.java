package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.List;

public class SettingsViewCreator {
    private final SQLiteManagement dbManager;
    private final Context context;
    public static final int USER_INFO_FRAGMENT_NEW_HOOK = 0;
    public static final int ANCHOR_MONITOR_LIVE_HOOK = 1;
    public static final int PLAYING_ON_LIVE_BASE_MODE_FRAGMENT_HOOK = 2;
    public static final int LIVE_JOIN_HIDE_HOOK = 3;
    public static final int WS_SERVER = 4;
    public static final int REC_HEW_HORN = 5;
    public static final int SHIELD_LIKE = 6;
    public static final int AUTO_LIKE = 7;

    public SettingsViewCreator(Context context) {
        this.context = context;
        this.dbManager = SQLiteManagement.getInstance();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public View createSettingsView() {
        // 初始化示例设置数据
        initializeSettings();

        // 获取所有设置项
        List<SettingItem> settingsList = dbManager.getAllSettings();

        // 创建滚动视图作为根布局
        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // 创建主线性布局
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(16, 16, 16, 16);
        scrollView.addView(mainLayout);

        // 为每个设置项动态创建视图
        for (SettingItem setting : settingsList) {
            // 创建单个设置项的容器
            LinearLayout settingItemView = new LinearLayout(context);
            settingItemView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            settingItemView.setOrientation(LinearLayout.VERTICAL);
            settingItemView.setPadding(16, 16, 16, 16);

            // 创建功能名称 TextView
            TextView functionName = new TextView(context);
            functionName.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            functionName.setText(setting.getFunctionName());
            functionName.setTextSize(16);
            functionName.setTextColor(0xFF000000); // 黑色文字

            // 创建描述 TextView
            TextView description = new TextView(context);
            description.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            description.setText(setting.getDescription());
            description.setTextSize(14);
            description.setTextColor(0xFF666666); // 灰色文字

            // 创建开关
            @SuppressLint("UseSwitchCompatOrMaterialCode")
            Switch switchButton = new Switch(context);
            switchButton.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            switchButton.setChecked(setting.isSwitchOn());
            if (setting.getFunctionId() == WS_SERVER) {
                if (BluedHook.wsServerManager != null) {
                    switchButton.setChecked(BluedHook.wsServerManager.isServerRunning());
                }
            }

            // 创建额外数据输入框
            EditText extraData = new EditText(context);
            extraData.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            if (setting.getExtraDataHint().isEmpty()) {
                extraData.setVisibility(View.GONE);
            } else {
                extraData.setText(setting.getExtraData());
                extraData.setHint(setting.getExtraDataHint());
                extraData.setVisibility(setting.isSwitchOn() ? View.VISIBLE : View.GONE);
            }

            // 设置开关监听器
            switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                dbManager.updateSettingSwitchState(setting.getFunctionId(), isChecked);
                setting.setSwitchOn(isChecked);
                if (!setting.getExtraDataHint().isEmpty()) {
                    extraData.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
                switchListener.onSwitchChanged(setting.getFunctionId(), isChecked);
                if (setting.getFunctionId() == WS_SERVER) {
                    if (BluedHook.wsServerManager != null) {
                        if (isChecked) {
                            BluedHook.wsServerManager.startServer(Integer.parseInt(setting.getExtraData()));
                        } else {
                            BluedHook.wsServerManager.stopServer();
                        }
                    }
                }
            });

            // 设置额外数据输入监听器
            extraData.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    dbManager.updateSettingExtraData(setting.getFunctionId(), s.toString());
                    setting.setExtraData(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // 将组件添加到设置项容器
            settingItemView.addView(functionName);
            settingItemView.addView(description);
            settingItemView.addView(switchButton);
            settingItemView.addView(extraData);

            // 将设置项添加到主布局
            mainLayout.addView(settingItemView);
        }

        return scrollView;
    }

    private void initializeSettings() {
        dbManager.addOrUpdateSetting(new SettingItem(USER_INFO_FRAGMENT_NEW_HOOK,
                "个人主页信息扩展",
                true,
                "启用后个人主页将显示额外信息。",
                "",
                ""
        ));

        dbManager.addOrUpdateSetting(new SettingItem(ANCHOR_MONITOR_LIVE_HOOK,
                "主播开播提醒监听",
                true,
                "开启后直播页右上角将会有\"检\"字图标，可进入开播提醒用户列表页面；注：如果需要使用此功能，请先打开\"个人主页信息扩展\"功能，方可看到主播主页的\"特别关注\"按钮，点击\"特别关注\"按钮即可将需要提醒的主播添加到主播监听列表。",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(PLAYING_ON_LIVE_BASE_MODE_FRAGMENT_HOOK,
                "直播间信息扩展",
                true,
                "开启后直播间将显示额外信息，例如：显示主播的总豆，显示其他用户隐藏的资料信息等功能。",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(LIVE_JOIN_HIDE_HOOK,
                "进入直播间隐身",
                true,
                "开启后进入直播间将会隐身；注：直播间送礼物后可能会看见你的头像，但每次进入直播间不会有任何提示。",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(WS_SERVER,
                "开启WS实时通讯",
                false,
                "需要配合ws客户端",
                "7890",
                "请输入端口号"
        ));
        dbManager.addOrUpdateSetting(new SettingItem(REC_HEW_HORN,
                "记录飘屏",
                false,
                "记录抽奖飘屏",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(SHIELD_LIKE,
                "屏蔽点赞",
                false,
                "屏蔽直播间自己的点赞，以免误触导致主播看到你。\n" +
                        "注：仅屏蔽发送过程，不会屏蔽本地点赞特效或震动",
                "",
                ""
        ));
        dbManager.addOrUpdateSetting(new SettingItem(AUTO_LIKE,
                "直播间自动点赞",
                false,
                "进入直播间手动触发一次点赞后，会持续发送点赞消息。\n" +
                        "注：使用此功能需先关闭屏蔽点赞开关，如需停止自动点赞，请退出直播间或关闭小窗。",
                "",
                ""
        ));
    }

    public interface OnSwitchCheckedChangeListener {
        void onSwitchChanged(int functionId, boolean isChecked);
    }

    protected OnSwitchCheckedChangeListener switchListener;

    public void setOnSwitchCheckedChangeListener(OnSwitchCheckedChangeListener listener) {
        this.switchListener = listener;
    }
}
