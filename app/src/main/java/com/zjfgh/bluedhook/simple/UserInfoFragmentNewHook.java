package com.zjfgh.bluedhook.simple;

import android.animation.LinearInterpolator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSON;
import com.zjfgh.bluedhook.simple.module.UserCardResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.ProjCoordinate;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserInfoFragmentNewHook {
    private static final String USER_INFO_ENTITY_CLASS = "com.soft.blued.ui.user.model.UserInfoEntity";
    private static final String TARGET_CLASS = "com.soft.blued.ui.user.fragment.UserInfoFragmentNew";
    private static final String TARGET_METHOD = "c";
    private static final String COLOR_PRIMARY = "#33539E";
    private static final String COLOR_SECONDARY = "#6E89A2";
    private static final int CORNER_RADIUS = 50;
    private static final int PADDING_HORIZONTAL = 30;
    private static final int PADDING_VERTICAL = 20;
    private static final int MARGIN_HORIZONTAL = 30;
    private static final double initialLat = 39.909088605597;
    private static final double initialLng = 116.39745423747772;
    private static UserInfoFragmentNewHook instance;
    private final WeakReference<Context> contextRef;
    private final ClassLoader classLoader;
    private final XModuleResources modRes;

    public static synchronized UserInfoFragmentNewHook getInstance(Context context, XModuleResources modRes) {
        if (instance == null) {
            instance = new UserInfoFragmentNewHook(context, modRes);
        }
        return instance;
    }

    private UserInfoFragmentNewHook(Context context, XModuleResources modRes) {
        this.contextRef = new WeakReference<>(context);
        this.classLoader = context.getClassLoader();
        this.modRes = modRes;
        hookAnchorMonitorAddButton();
        hookPhotoProtection();
        hookRemoveWatermark();
    }

    private ImageButton ibvClean;
    private ObjectAnimator rotateAnim;
    private final Handler handler = new Handler();

    public void hookAnchorMonitorAddButton() {
        final SQLiteManagement dbManager = SQLiteManagement.getInstance();
        XposedHelpers.findAndHookMethod(TARGET_CLASS, classLoader, TARGET_METHOD,
                XposedHelpers.findClass(USER_INFO_ENTITY_CLASS, classLoader), new XC_MethodHook() {
                    private View lastView = null;
                    private final GradientDrawable defaultBackground = createGradientDrawable(COLOR_PRIMARY);
                    private final GradientDrawable activeBackground = createGradientDrawable(COLOR_SECONDARY);
                    TagLayout tlTitle;

                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Object userInfoEntity = param.args[0];
                        String uid = (String) XposedHelpers.getObjectField(userInfoEntity, "uid");
                        FrameLayout flFeedFragmentContainer = (FrameLayout) XposedHelpers.getObjectField(param.thisObject, "b");
                        int flow_my_vip_tagsId = getSafeContext().getResources().getIdentifier("flow_my_vip_tags", "id", getSafeContext().getPackageName());
                        ViewGroup flow_my_vip_tags = flFeedFragmentContainer.findViewById(flow_my_vip_tagsId);
                        flow_my_vip_tags.setVisibility(View.VISIBLE);
                        tlTitle = new TagLayout(flFeedFragmentContainer.getContext());
                        NetworkManager.getInstance().getAsync(NetworkManager.getBluedPicSaveStatusApi(uid), AuthManager.auHook(false, classLoader, getSafeContext()), new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                try {
                                    JSONObject response1 = new JSONObject(response.body().string());
                                    int code = response1.getInt("code");
                                    String message = response1.getString("message");
                                    JSONArray dataArray = response1.getJSONArray("data");
                                    if (dataArray.length() > 0) {
                                        JSONObject dataObj = dataArray.getJSONObject(0);
                                        int albumBanSave = dataObj.getInt("album_ban_save");
                                        int feedPicBanSave = dataObj.getInt("feed_pic_ban_save");
                                        if (albumBanSave > 0 || feedPicBanSave > 0) {
                                            tlTitle.post(() -> tlTitle.addTextView("相册保护已解除", 9, modRes.getDrawable(R.drawable.bg_gradient_orange, null)));
                                        }
                                    }
                                } catch (JSONException e) {
                                    Log.e("BluedHook：", e.getMessage());
                                }
                            }
                        });
                        int privacyPhotosHasLocked = XposedHelpers.getIntField(userInfoEntity, "privacy_photos_has_locked");
                        if (privacyPhotosHasLocked == 0) {
                            XposedHelpers.setIntField(userInfoEntity, "privacy_photos_has_locked", 1);
                            tlTitle.addTextView("隐私相册已解除", 9, modRes.getDrawable(R.drawable.bg_green_rounded, null));
                        }
                        tlTitle.addTextView("保存图片去水印", 9, modRes.getDrawable(R.drawable.bg_rounded, null));
                        flow_my_vip_tags.addView(tlTitle);
                    }

                    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
                        SettingItem settingItem = SQLiteManagement.getInstance().getSettingByFunctionId(SettingsViewCreator.ANCHOR_MONITOR_LIVE_HOOK);
                        if (settingItem.isSwitchOn()) {
                            View currentView = (View) XposedHelpers.getObjectField(param.thisObject, "U");
                            if (currentView != lastView) {
                                lastView = currentView;
                                return;
                            }
                            Object userInfoEntity = param.args[0];
                            String uid = (String) XposedHelpers.getObjectField(userInfoEntity, "uid");
                            int isAnchor = XposedHelpers.getIntField(userInfoEntity, "anchor");
                            int isHideLastOperate = XposedHelpers.getIntField(userInfoEntity, "is_hide_last_operate");
                            int isHideLastDistance = XposedHelpers.getIntField(userInfoEntity, "is_hide_distance");
                            String name = (String) XposedHelpers.getObjectField(userInfoEntity, "name");
                            FrameLayout flFeedFragmentContainer = (FrameLayout) XposedHelpers.getObjectField(param.thisObject, "b");
                            int fl_contentID = getSafeContext().getResources().getIdentifier("fl_content", "id", getSafeContext().getPackageName());
                            LinearLayout fl_content = flFeedFragmentContainer.findViewById(fl_contentID);

                            // 动态创建 userInfoFragmentNewExtra
                            LinearLayout userInfoFragmentNewExtra = createUserInfoFragmentNewExtra(fl_content.getContext(), isHideLastDistance);
                            int v_userinfo_card_bgID = getSafeContext().getResources().getIdentifier("v_userinfo_card_bg", "id", getSafeContext().getPackageName());
                            View v_userinfo_card_bg = flFeedFragmentContainer.findViewById(v_userinfo_card_bgID);
                            ViewGroup viewGroup = (ViewGroup) v_userinfo_card_bg.getParent();
                            ViewGroup user_info_profile_card = (ViewGroup) viewGroup.getParent();
                            int ll_all_basic_infoID = getSafeContext().getResources().getIdentifier("ll_all_basic_info", "id", getSafeContext().getPackageName());
                            ViewGroup ll_all_basic_info = flFeedFragmentContainer.findViewById(ll_all_basic_infoID);
                            user_info_profile_card.addView(userInfoFragmentNewExtra);
                            int cl_user_info_card_rootID = getSafeContext().getResources().getIdentifier("cl_user_info_card_root", "id", getSafeContext().getPackageName());
                            ViewGroup cl_user_info_card_root = flFeedFragmentContainer.findViewById(cl_user_info_card_rootID);
                            ll_all_basic_info.post(() -> {
                                int extraHeight = userInfoFragmentNewExtra.getMeasuredHeight();
                                cl_user_info_card_root.setPadding(0, extraHeight, 0, 0);
                                ll_all_basic_info.invalidate();
                                ll_all_basic_info.requestLayout();
                            });

                            int fl_buttonsID = getSafeContext().getResources().getIdentifier("fl_buttons", "id", getSafeContext().getPackageName());
                            FrameLayout flButtons = flFeedFragmentContainer.findViewById(fl_buttonsID);
                            LinearLayout followView = (LinearLayout) flButtons.getChildAt(1);
                            TextView specialFollowButton = createSpecialFollowButton(currentView.getContext());
                            updateButtonState(specialFollowButton, dbManager.getUserByUid(uid) != null);
                            setupButtonClickListener(specialFollowButton, userInfoEntity);
                            followView.addView(specialFollowButton, 0);

                            if (isHideLastOperate == 1 && isAnchor == 1) {
                                int rl_basic_info_rootID = getSafeContext().getResources().getIdentifier("rl_basic_info_root", "id", getSafeContext().getPackageName());
                                ViewGroup rlBasicInfoRoot = flFeedFragmentContainer.findViewById(rl_basic_info_rootID);
                                HorizontalScrollView horizontalScrollView = (HorizontalScrollView) rlBasicInfoRoot.getChildAt(0);
                                LinearLayout linearLayout = (LinearLayout) horizontalScrollView.getChildAt(0);
                                TextView tvLastOperateAnchor = new TextView(currentView.getContext());
                                tvLastOperateAnchor.setTextColor(Color.parseColor("#00FFA3"));
                                linearLayout.addView(tvLastOperateAnchor);
                                NetworkManager.getInstance().getAsync(NetworkManager.getBluedLiveSearchAnchorApi(name), AuthManager.auHook(false, classLoader, fl_content.getContext()), new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {}

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        try {
                                            if (response.code() == 200 && !response.body().toString().isEmpty()) {
                                                JSONObject jsonResponse = new JSONObject(response.body().string());
                                                JSONArray usersArray = jsonResponse.getJSONArray("data");
                                                for (int i = 0; i < usersArray.length(); i++) {
                                                    JSONObject user = usersArray.getJSONObject(i);
                                                    user.getInt("anchor");
                                                    user.getString("avatar");
                                                    long lastOperate = user.getLong("last_operate");
                                                    user.getString("name");
                                                    int anchorUid = user.getInt("uid");
                                                    if (String.valueOf(anchorUid).equals(uid)) {
                                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                                        String date = sdf.format(new Date(lastOperate * 1000L));
                                                        tvLastOperateAnchor.post(() -> tvLastOperateAnchor.setText("(上线时间：" + date + ")"));
                                                    }
                                                }
                                            }
                                        } catch (JSONException e) {
                                            Log.e("UserInfoFragmentNewHook", "JSONException" + e);
                                        }
                                    }
                                });
                            } else {
                                Log.i("BluedHook", "非主播无法显示保密上线时间");
                            }
                            String registrationTimeEncrypt = (String) XposedHelpers.getObjectField(userInfoEntity, "registration_time_encrypt");
                            String registrationTime = ModuleTools.AesDecrypt(registrationTimeEncrypt);
                            TextView tvUserRegTime = userInfoFragmentNewExtra.findViewById(R.id.tv_user_reg_time);
                            if (!registrationTime.isEmpty()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                String formattedDate = sdf.format(new Date(Long.parseLong(registrationTime) * 1000L));
                                tvUserRegTime.setText("注册时间：" + formattedDate);
                                tvUserRegTime.setTextSize(13f);
                                tvUserRegTime.setVisibility(View.VISIBLE);
                            } else {
                                tvUserRegTime.setVisibility(View.GONE);
                            }
                        }
                    }

                    private LinearLayout createUserInfoFragmentNewExtra(Context context, int isHideLastDistance) {
                        LinearLayout layout = new LinearLayout(context);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        layout.setLayoutParams(params);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        // 创建定位按钮
                        Button userInfoExtraLocate = new Button(context);
                        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        userInfoExtraLocate.setLayoutParams(buttonParams);
                        userInfoExtraLocate.setText("定位");
                        userInfoExtraLocate.setBackground(modRes.getDrawable(R.drawable.bg_tech_tag, null));
                        if (isHideLastDistance == 1) {
                            userInfoExtraLocate.setVisibility(View.GONE);
                        }
                        userInfoExtraLocate.setOnClickListener(v -> showAMapPopup(context, layout));
                        layout.addView(userInfoExtraLocate);

                        // 创建注册时间 TextView
                        TextView tvUserRegTime = new TextView(context);
                        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        tvUserRegTime.setLayoutParams(textParams);
                        tvUserRegTime.setId(R.id.tv_user_reg_time);
                        layout.addView(tvUserRegTime);

                        return layout;
                    }

                    private void showAMapPopup(Context context, LinearLayout parentLayout) {
                        LinearLayout userInfoExtraAMap = createUserInfoExtraAMap(context, parentLayout);
                        CustomPopupWindow aMapPopupWindow = new CustomPopupWindow((Activity) context, userInfoExtraAMap, Color.parseColor("#FF0A121F"));
                        aMapPopupWindow.setBackgroundDrawable(modRes.getDrawable(R.drawable.bg_tech_space, null));
                        aMapPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
                        aMapPopupWindow.showAtCenter();
                        aMapPopupWindow.setOnDismissListener(() -> {
                            AMapHookHelper aMapHelper = (AMapHookHelper) userInfoExtraAMap.getTag();
                            if (aMapHelper != null) {
                                aMapHelper.onPause();
                                aMapHelper.onDestroy();
                            }
                        });
                    }

                    private LinearLayout createUserInfoExtraAMap(Context context, LinearLayout parentLayout) {
                        LinearLayout layout = new LinearLayout(context);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        layout.setLayoutParams(params);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

                        // 创建地图容器
                        LinearLayout llAMap = new LinearLayout(context);
                        LinearLayout.LayoutParams mapParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                dpToPx(200)
                        );
                        llAMap.setLayoutParams(mapParams);
                        AMapHookHelper aMapHelper = new AMapHookHelper(context, context.getClassLoader());
                        View aMapView = aMapHelper.createMapView();
                        llAMap.addView(aMapView);
                        layout.addView(llAMap);
                        layout.setTag(aMapHelper);

                        // 创建位置数据容器
                        LinearLayout llLocationData = new LinearLayout(context);
                        LinearLayout.LayoutParams dataParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        llLocationData.setLayoutParams(dataParams);
                        llLocationData.setBackground(modRes.getDrawable(R.drawable.bg_tech_tag, null));
                        layout.addView(llLocationData);

                        // GPS 图标
                        ImageView ivGpsIcon = new ImageView(context);
                        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                                dpToPx(24), dpToPx(24)
                        );
                        ivGpsIcon.setLayoutParams(iconParams);
                        ivGpsIcon.setImageDrawable(modRes.getDrawable(R.drawable.gps_location_icon1, null));
                        llLocationData.addView(ivGpsIcon);

                        // 位置根布局
                        LinearLayout llLocationRoot = new LinearLayout(context);
                        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        llLocationRoot.setLayoutParams(rootParams);
                        llLocationRoot.setBackground(modRes.getDrawable(R.drawable.bg_tech_item_inner, null));
                        llLocationData.addView(llLocationRoot);

                        // 用户名
                        TextView tvUsername = new TextView(context);
                        LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        tvUsername.setLayoutParams(usernameParams);
                        tvUsername.setId(R.id.tv_username);
                        llLocationRoot.addView(tvUsername);

                        // 自动追踪按钮
                        TextView tvAutoLocation = new TextView(context);
                        LinearLayout.LayoutParams autoParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        tvAutoLocation.setLayoutParams(autoParams);
                        tvAutoLocation.setId(R.id.tv_auto_location);
                        tvAutoLocation.setBackground(modRes.getDrawable(R.drawable.bg_auto_location_button, null));
                        llLocationRoot.addView(tvAutoLocation);

                        // 纬度
                        TextView tvLatitude = new TextView(context);
                        LinearLayout.LayoutParams latParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        tvLatitude.setLayoutParams(latParams);
                        tvLatitude.setId(R.id.tv_latitude);
                        llLocationRoot.addView(tvLatitude);

                        // 经度
                        TextView tvLongitude = new TextView(context);
                        LinearLayout.LayoutParams lonParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        tvLongitude.setLayoutParams(lonParams);
                        tvLongitude.setId(R.id.tv_longitude);
                        llLocationRoot.addView(tvLongitude);

                        // 位置信息
                        TextView tvLocation = new TextView(context);
                        LinearLayout.LayoutParams locParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        tvLocation.setLayoutParams(locParams);
                        tvLocation.setId(R.id.tv_location);
                        llLocationRoot.addView(tvLocation);

                        // 用户与自己的距离
                        TextView tvUserWithSelfDistance = new TextView(context);
                        LinearLayout.LayoutParams distParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        tvUserWithSelfDistance.setLayoutParams(distParams);
                        tvUserWithSelfDistance.setId(R.id.tv_user_with_self_distance);
                        tvUserWithSelfDistance.setVisibility(View.GONE);
                        llLocationRoot.addView(tvUserWithSelfDistance);

                        // 清理图标
                        ibvClean = new ImageButton(context);
                        LinearLayout.LayoutParams cleanParams = new LinearLayout.LayoutParams(
                                dpToPx(40), dpToPx(40)
                        );
                        ibvClean.setLayoutParams(cleanParams);
                        ibvClean.setId(R.id.iv_clean_icon);
                        ibvClean.setBackground(modRes.getDrawable(R.drawable.tech_button_bg, null));
                        ibvClean.setImageDrawable(modRes.getDrawable(R.drawable.ic_refresh, null));
                        rotateAnim = ObjectAnimator.ofFloat(ibvClean, "rotation", 0f, 360f);
                        rotateAnim.setDuration(800);
                        rotateAnim.setInterpolator(new LinearInterpolator());
                        rotateAnim.setRepeatCount(ObjectAnimator.INFINITE);
                        ibvClean.setOnClickListener(v -> {
                            startRefreshAnimation();
                            handler.postDelayed(() -> {
                                aMapHelper.clearAllOverlays();
                                stopRefreshAnimation();
                            }, 500);
                        });
                        llLocationRoot.addView(ibvClean);

                        aMapHelper.onCreate(null);
                        aMapHelper.onResume();
                        aMapHelper.moveCamera(initialLat, initialLng, 5f);
                        aMapHelper.addMarker(initialLat, initialLng, "天安门");

                        return layout;
                    }

                    private TextView createSpecialFollowButton(Context context) {
                        TextView button = new TextView(context);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(
                                ModuleTools.dpToPx(MARGIN_HORIZONTAL),
                                0,
                                ModuleTools.dpToPx(MARGIN_HORIZONTAL),
                                0
                        );
                        button.setLayoutParams(params);
                        button.setTextColor(Color.WHITE);
                        button.setPadding(
                                ModuleTools.dpToPx(PADDING_HORIZONTAL),
                                ModuleTools.dpToPx(PADDING_VERTICAL),
                                ModuleTools.dpToPx(PADDING_HORIZONTAL),
                                ModuleTools.dpToPx(PADDING_VERTICAL)
                        );
                        button.setBackground(defaultBackground);
                        return button;
                    }

                    private void updateButtonState(TextView button, boolean isFollowing) {
                        button.setTag(isFollowing);
                        button.setText(isFollowing ? "取消特关" : "特别关注");
                        button.setBackground(isFollowing ? activeBackground : defaultBackground);
                    }

                    private void setupButtonClickListener(TextView button, Object userInfoEntity) {
                        button.setOnClickListener(v -> {
                            boolean isSpFollow = (boolean) v.getTag();
                            String uid = (String) XposedHelpers.getObjectField(userInfoEntity, "uid");
                            String unionUid = (String) XposedHelpers.getObjectField(userInfoEntity, "bluedIdentifyId");
                            String name = (String) XposedHelpers.getObjectField(userInfoEntity, "name");
                            String live = (String) XposedHelpers.getObjectField(userInfoEntity, "live");
                            String avatar = (String) XposedHelpers.getObjectField(userInfoEntity, "avatar");
                            final String[] enc_uid = {"获取失败"};
                            Map<String, String> authMap = AuthManager.auHook(false, AppContainer.getInstance().getClassLoader(), AppContainer.getInstance().getBluedContext());
                            if (isSpFollow) {
                                UserPopupWindow.getInstance().delAnchor(uid, name);
                            } else {
                                NetworkManager.getInstance().getAsync(NetworkManager.getBluedLiveUserCard(uid), authMap, new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        ModuleTools.showBluedToast("获取加密UID失败(onFailure)");
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        UserCardResponse userCardResponse = JSON.parseObject(response.body().string(), UserCardResponse.class);
                                        if (userCardResponse.getCode() == 200) {
                                            String link = userCardResponse.getData().get(0).getContract().getLink();
                                            enc_uid[0] = ModuleTools.getParamFromUrl(link, "uid");
                                        } else {
                                            ModuleTools.showBluedToast("添加加密UID失败(" + userCardResponse.getCode() + ")");
                                            Log.i("BluedHook", "响应内容：" + response.body().string());
                                        }
                                        User user = new User();
                                        user.setUid(uid);
                                        user.setUnion_uid(unionUid);
                                        user.setName(name);
                                        user.setLive(live);
                                        user.setAvatar(avatar);
                                        user.setEnc_uid(enc_uid[0]);
                                        UserPopupWindow.getInstance().addAnchor(user);
                                    }
                                });
                            }
                            updateButtonState(button, !isSpFollow);
                        });
                    }

                    private GradientDrawable createGradientDrawable(String color) {
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setCornerRadius(CORNER_RADIUS);
                        drawable.setColor(Color.parseColor(color));
                        return drawable;
                    }

                    private int dpToPx(int dp) {
                        float density = getSafeContext().getResources().getDisplayMetrics().density;
                        return (int) (dp * density + 0.5f);
                    }
                });
    }

    private void startRefreshAnimation() {
        rotateAnim.start();
        ibvClean.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(200)
                .start();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void stopRefreshAnimation() {
        rotateAnim.cancel();
        ibvClean.setRotation(0f);
        ibvClean.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .start();
        ibvClean.setImageDrawable(modRes.getDrawable(R.drawable.ic_done, null));
        handler.postDelayed(() -> ibvClean.setImageDrawable(modRes.getDrawable(R.drawable.ic_refresh, null)), 1000);
    }

    private void hookPhotoProtection() {
        this.hookPhotoProtection(classLoader,
                "com.soft.blued.ui.photo.fragment.ShowAlbumFragment",
                "已解除动态相册保护功能，可直接下载");
        this.hookPhotoProtection(classLoader,
                "com.soft.blued.ui.photo.fragment.ShowPhotoFragment",
                "已解除相册保护功能，可直接下载");
    }

    private void hookPhotoProtection(ClassLoader classLoader, String className, String toastMessage) {
        XposedHelpers.findAndHookMethod(
                className,
                classLoader,
                "a",
                Object[].class,
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String albumBanSave = (String) param.args[1];
                        String feedPicBanSave = (String) param.args[2];
                        boolean hasHook = false;
                        if ("1".equals(albumBanSave)) {
                            param.args[1] = "0";
                            hasHook = true;
                        }
                        if ("1".equals(feedPicBanSave)) {
                            param.args[2] = "0";
                            hasHook = true;
                        }
                        if (hasHook) {
                            ModuleTools.showBluedToast(toastMessage);
                        }
                    }
                });
    }

    public void hookRemoveWatermark() {
        Class<?> UserInfoEntity = XposedHelpers.findClass("com.soft.blued.ui.user.model.UserInfoEntity", classLoader);
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.user.fragment.UserInfoFragmentNew", classLoader, "j", UserInfoEntity, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.soft.blued.ui.user.fragment.UserInfoFragmentNew", classLoader, "o", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
        XposedHelpers.findAndHookMethod(
                "com.soft.blued.ui.photo.fragment.BasePhotoFragment",
                classLoader,
                "a",
                File.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        File file = (File) param.args[0];
                        Log.i("BluedHook", "paramparam" + param.args[0]);
                        if (!ChatHelperV4.a(file)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                            if (bitmap != null) {
                                Class<?> ImageUtils = XposedHelpers.findClass("com.blued.android.framework.utils.ImageUtils", classLoader);
                                XposedHelpers.callStaticMethod(ImageUtils, "a", bitmap);
                                param.setResult(null);
                            }
                        }
                    }
                });
    }

    public Context getSafeContext() {
        Context context = contextRef.get();
        if (context == null) {
            throw new IllegalStateException("Context was garbage collected");
        }
        return context;
    }
}
