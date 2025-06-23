package com.zjfgh.bluedhook.simple;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
                        @SuppressLint("DiscouragedApi") int flow_my_vip_tagsId = getSafeContext().getResources().getIdentifier("flow_my_vip_tags", "id", getSafeContext().getPackageName());
                        ViewGroup flow_my_vip_tags = flFeedFragmentContainer.findViewById(flow_my_vip_tagsId);
                        flow_my_vip_tags.setVisibility(View.VISIBLE);
                        tlTitle = new TagLayout(flFeedFragmentContainer.getContext());
                        NetworkManager.getInstance().getAsync(NetworkManager.getBluedPicSaveStatusApi(uid), AuthManager.auHook(false, classLoader, getSafeContext()), new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            }

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
                                            tlTitle.post(() -> {
                                                Drawable bg = null;
                                                try {
                                                    bg = modRes.getDrawable(R.drawable.bg_gradient_orange, null);
                                                } catch (Exception e) {
                                                    Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                                    bg = new GradientDrawable();
                                                    ((GradientDrawable) bg).setColor(Color.parseColor("#FFA500"));
                                                    ((GradientDrawable) bg).setCornerRadius(10);
                                                }
                                                tlTitle.addTextView("相册保护已解除", 9, bg);
                                            });
                                        }
                                    }
                                } catch (JSONException e) {
                                    Log.e("BluedHook", e.getMessage());
                                }
                            }
                        });
                        int privacyPhotosHasLocked = XposedHelpers.getIntField(userInfoEntity, "privacy_photos_has_locked");
                        if (privacyPhotosHasLocked == 0) {
                            XposedHelpers.setIntField(userInfoEntity, "privacy_photos_has_locked", 1);
                            Drawable bg = null;
                            try {
                                bg = modRes.getDrawable(R.drawable.bg_green_rounded, null);
                            } catch (Exception e) {
                                Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                bg = new GradientDrawable();
                                ((GradientDrawable) bg).setColor(Color.parseColor("#00FF00"));
                                ((GradientDrawable) bg).setCornerRadius(10);
                            }
                            tlTitle.addTextView("隐私相册已解除", 9, bg);
                        }
                        Drawable bg = null;
                        try {
                            bg = modRes.getDrawable(R.drawable.bg_rounded, null);
                        } catch (Exception e) {
                            Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                            bg = new GradientDrawable();
                            ((GradientDrawable) bg).setColor(Color.parseColor("#CCCCCC"));
                            ((GradientDrawable) bg).setCornerRadius(10);
                        }
                        tlTitle.addTextView("保存图片去水印", 9, bg);
                        flow_my_vip_tags.addView(tlTitle);
                    }

                    @SuppressLint({"SetTextI18n", "DiscouragedApi"})
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
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
                            LinearLayout userInfoFragmentNewExtra = createUserInfoExtraLayout(fl_content.getContext());
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

                            // 获取定位按钮和注册时间 TextView
                            Button userInfoExtraLocate = (Button) userInfoFragmentNewExtra.findViewById(R.id.user_locate_bt);
                            TextView tvUserRegTime = (TextView) userInfoFragmentNewExtra.findViewById(R.id.tv_user_reg_time);
                            if (isHideLastDistance == 1) {
                                userInfoExtraLocate.setVisibility(View.GONE);
                            }
                            Drawable locateBg = null;
                            try {
                                locateBg = modRes.getDrawable(R.drawable.bg_tech_tag, null);
                            } catch (Exception e) {
                                Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                locateBg = new GradientDrawable();
                                ((GradientDrawable) locateBg).setColor(Color.parseColor("#333333"));
                                ((GradientDrawable) locateBg).setCornerRadius(10);
                            }
                            userInfoExtraLocate.setBackground(locateBg);
                            userInfoExtraLocate.setOnClickListener(v -> {
                                // 动态创建 userInfoExtraAMap
                                LinearLayout userInfoExtraAMap = createUserInfoExtraAMapLayout(fl_content.getContext());
                                LinearLayout llAMap = (LinearLayout) userInfoExtraAMap.findViewById(R.id.ll_aMap);
                                LinearLayout llLocationData = (LinearLayout) userInfoExtraAMap.findViewById(R.id.ll_location_data);
                                ImageView ivGpsIcon = (ImageView) userInfoExtraAMap.findViewById(R.id.iv_gps_icon);
                                LinearLayout llLocationRoot = (LinearLayout) userInfoExtraAMap.findViewById(R.id.ll_location_root);
                                ibvClean = (ImageButton) userInfoExtraAMap.findViewById(R.id.iv_clean_icon);
                                AMapHookHelper aMapHelper = new AMapHookHelper(fl_content.getContext(), fl_content.getContext().getClassLoader());
                                Drawable gpsIcon = null;
                                try {
                                    gpsIcon = modRes.getDrawable(R.drawable.gps_location_icon1, null);
                                } catch (Exception e) {
                                    Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                }
                                ivGpsIcon.setImageDrawable(gpsIcon);
                                Drawable locationDataBg = null;
                                try {
                                    locationDataBg = modRes.getDrawable(R.drawable.bg_tech_tag, null);
                                } catch (Exception e) {
                                    Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                    locationDataBg = new GradientDrawable();
                                    ((GradientDrawable) locationDataBg).setColor(Color.parseColor("#333333"));
                                    ((GradientDrawable) locationDataBg).setCornerRadius(10);
                                }
                                llLocationData.setBackground(locationDataBg);
                                Drawable locationRootBg = null;
                                try {
                                    locationRootBg = modRes.getDrawable(R.drawable.bg_tech_item_inner, null);
                                } catch (Exception e) {
                                    Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                    locationRootBg = new GradientDrawable();
                                    ((GradientDrawable) locationRootBg).setColor(Color.parseColor("#222222"));
                                    ((GradientDrawable) locationRootBg).setCornerRadius(10);
                                }
                                llLocationRoot.setBackground(locationRootBg);
                                Drawable cleanBg = null;
                                try {
                                    cleanBg = modRes.getDrawable(R.drawable.tech_button_bg, null);
                                } catch (Exception e) {
                                    Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                    cleanBg = new GradientDrawable();
                                    ((GradientDrawable) cleanBg).setColor(Color.parseColor("#444444"));
                                    ((GradientDrawable) cleanBg).setCornerRadius(10);
                                }
                                ibvClean.setBackground(cleanBg);
                                Drawable refreshIcon = null;
                                try {
                                    refreshIcon = modRes.getDrawable(R.drawable.ic_refresh, null);
                                } catch (Exception e) {
                                    Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                }
                                ibvClean.setImageDrawable(refreshIcon);
                                rotateAnim = ObjectAnimator.ofFloat(ibvClean, "rotation", 0f, 360f);
                                rotateAnim.setDuration(800);
                                rotateAnim.setInterpolator(new LinearInterpolator());
                                rotateAnim.setRepeatCount(ObjectAnimator.INFINITE);
                                ibvClean.setOnClickListener(v1 -> {
                                    startRefreshAnimation();
                                    handler.postDelayed(() -> {
                                        aMapHelper.clearAllOverlays();
                                        stopRefreshAnimation();
                                    }, 500);
                                });
                                View aMapView = aMapHelper.createMapView();
                                llAMap.addView(aMapView);
                                TextView tv_username = (TextView) userInfoExtraAMap.findViewById(R.id.tv_username);
                                tv_username.setText(name);
                                TextView tvAutoLocation = (TextView) userInfoExtraAMap.findViewById(R.id.tv_auto_location);
                                Drawable autoLocationBg = null;
                                try {
                                    autoLocationBg = modRes.getDrawable(R.drawable.bg_auto_location_button, null);
                                } catch (Exception e) {
                                    Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                    autoLocationBg = new GradientDrawable();
                                    ((GradientDrawable) autoLocationBg).setColor(Color.parseColor("#555555"));
                                    ((GradientDrawable) autoLocationBg).setCornerRadius(10);
                                }
                                tvAutoLocation.setBackground(autoLocationBg);
                                TextView tvLongitude = (TextView) userInfoExtraAMap.findViewById(R.id.tv_longitude);
                                TextView tvLatitude = (TextView) userInfoExtraAMap.findViewById(R.id.tv_latitude);
                                TextView tvLocation = (TextView) userInfoExtraAMap.findViewById(R.id.tv_location);
                                String location = (String) XposedHelpers.getObjectField(userInfoEntity, "location");
                                tvLocation.setText("真实位置(距离)：" + location);
                                TextView tvUserWithSelfDistance = (TextView) userInfoExtraAMap.findViewById(R.id.tv_user_with_self_distance);
                                tvUserWithSelfDistance.setVisibility(View.GONE);
                                aMapHelper.onCreate(null);
                                aMapHelper.onResume();
                                aMapHelper.moveCamera(initialLat, initialLng, 5f);
                                aMapHelper.addMarker(initialLat, initialLng, "天安门");
                                tvAutoLocation.setOnClickListener(v1 -> {
                                    if (tvAutoLocation.getText().equals("自动追踪中...")) {
                                        return;
                                    } else {
                                        tvAutoLocation.setText("自动追踪中...");
                                    }
                                    NetworkManager.getInstance().getAsync(NetworkManager.getBluedSetUsersLocationApi(initialLat, initialLng), AuthManager.auHook(false, classLoader, fl_content.getContext()), new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                                            NetworkManager.getInstance().getAsync(NetworkManager.getBluedUserBasicAPI(uid), AuthManager.auHook(false, classLoader, fl_content.getContext()), new Callback() {
                                                @Override
                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                }

                                                @Override
                                                public void onResponse(@NonNull Call call, @NonNull Response response) {
                                                    try {
                                                        if (response.code() == 200 && !response.body().toString().isEmpty()) {
                                                            String jsonStr = response.body().string();
                                                            JSONObject json = new JSONObject(jsonStr);
                                                            String message = json.getString("message");
                                                            Log.i("BluedHook", "message：" + message);
                                                            JSONArray dataArray = json.getJSONArray("data");
                                                            if (dataArray.length() > 0) {
                                                                JSONObject userData = dataArray.getJSONObject(0);
                                                                int isHideDistance = userData.getInt("is_hide_distance");
                                                                double distanceKm = userData.getDouble("distance");
                                                                if (isHideDistance == 0) {
                                                                    aMapHelper.addCircle(initialLat, initialLat, DistanceConverter.kmToMeters(distanceKm), "#003399FF", "#603399FF");
                                                                    tvUserWithSelfDistance.post(() -> {
                                                                        tvUserWithSelfDistance.setText("当前虚拟距离：" + DistanceConverter.formatDistance(distanceKm));
                                                                        tvUserWithSelfDistance.setVisibility(View.VISIBLE);
                                                                    });
                                                                    LocationTracker tracker = new LocationTracker(aMapHelper, uid, classLoader, fl_content);
                                                                    tracker.startTracking(initialLat, initialLng, distanceKm, 15, new LocationTracker.LocationTrackingCallback() {
                                                                        @Override
                                                                        public void onInitialLocation(double lat, double lng, double distanceKm) {
                                                                            Log.d("LocationTracker", String.format("初始位置: %.6f, %.6f, 距离: %.3fkm", lat, lng, distanceKm));
                                                                        }

                                                                        @Override
                                                                        public void onProbeLocation(double lat, double lng) {
                                                                            Log.d("LocationTracker", String.format("探测点位置: %.6f, %.6f", lat, lng));
                                                                        }

                                                                        @Override
                                                                        public void onProbeDistance(double distanceKm) {
                                                                            Log.d("LocationTracker", String.format("探测点距离: %.3fkm", distanceKm));
                                                                        }

                                                                        @Override
                                                                        public void onIntersectionLocation(double lat, double lng) {
                                                                            Log.d("LocationTracker", String.format("交点位置: %.6f, %.6f", lat, lng));
                                                                        }

                                                                        @Override
                                                                        public void onIntersectionDistance(double lat, double lng, double distanceKm) {
                                                                            Log.d("LocationTracker", String.format("交点距离: %.6f, %.6f, 距离: %.3fkm", lat, lng, distanceKm));
                                                                        }

                                                                        @Override
                                                                        public void onNewCenterLocation(double lat, double lng, double distanceKm) {
                                                                            tvLatitude.post(() -> {
                                                                                tvLatitude.setText("纬度：" + lat);
                                                                                tvLongitude.setText("经度：" + lng);
                                                                                tvUserWithSelfDistance.setText("当前虚拟距离：" + DistanceConverter.formatDistance(distanceKm));
                                                                            });
                                                                            Log.d("LocationTracker", String.format("新中心点: %.6f, %.6f, 距离: %.3fkm", lat, lng, distanceKm));
                                                                        }

                                                                        @Override
                                                                        public void onFinalLocation(double lat, double lng, double distanceKm) {
                                                                            Log.d("LocationTracker", String.format("最终位置: %.6f, %.6f, 距离: %.3fkm", lat, lng, distanceKm));
                                                                            tvLatitude.post(() -> {
                                                                                tvLatitude.setText("经度：" + lat);
                                                                                tvLongitude.setText("纬度：" + lng);
                                                                                tvAutoLocation.setText("追踪完成");
                                                                            });
                                                                        }

                                                                        @NonNull
                                                                        private CoordinateTransform getCoordinateTransform() {
                                                                            CRSFactory crsFactory = new CRSFactory();
                                                                            CoordinateReferenceSystem wgs84 = crsFactory.createFromName("EPSG:4326");
                                                                            CoordinateReferenceSystem gcj02 = crsFactory.createFromParameters("GCJ02", "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext +no_defs");
                                                                            return new BasicCoordinateTransform(wgs84, gcj02);
                                                                        }

                                                                        @Override
                                                                        public void onError(String message) {
                                                                            Log.e("LocationTracker", "错误: " + message);
                                                                        }
                                                                    });
                                                                } else {
                                                                    Log.i("BluedHook", "用户隐藏了距离信息");
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e("UserInfoFragmentNewHook", "Hook位置\nhookAnchorMonitorAddButton.获取用户距离异常：\n" + e);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                });
                                aMapHelper.setOnMapClickListener((lat, lng) -> {
                                    aMapHelper.addMarker(lat, lng, "纬度：" + lat + "\n经度：" + lng);
                                    tvLatitude.setText("纬度：" + lat);
                                    tvLongitude.setText("经度：" + lng);
                                    NetworkManager.getInstance().getAsync(NetworkManager.getBluedSetUsersLocationApi(lat, lng), AuthManager.auHook(false, classLoader, fl_content.getContext()), new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                                            NetworkManager.getInstance().getAsync(NetworkManager.getBluedUserBasicAPI(uid), AuthManager.auHook(false, classLoader, fl_content.getContext()), new Callback() {
                                                @Override
                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                }

                                                @Override
                                                public void onResponse(@NonNull Call call, @NonNull Response response) {
                                                    try {
                                                        if (response.code() == 200 && !response.body().toString().isEmpty()) {
                                                            String jsonStr = response.body().string();
                                                            JSONObject json = new JSONObject(jsonStr);
                                                            String message = json.getString("message");
                                                            Log.i("BluedHook", "message：" + message);
                                                            JSONArray dataArray = json.getJSONArray("data");
                                                            if (dataArray.length() > 0) {
                                                                JSONObject userData = dataArray.getJSONObject(0);
                                                                int isHideDistance = userData.getInt("is_hide_distance");
                                                                double distanceKm = userData.getDouble("distance");
                                                                if (isHideDistance == 0) {
                                                                    aMapHelper.addCircle(lat, lng, DistanceConverter.kmToMeters(distanceKm), "#003399FF", "#603399FF");
                                                                    tvUserWithSelfDistance.post(() -> {
                                                                        tvUserWithSelfDistance.setText("当前虚拟距离：" + DistanceConverter.formatDistance(distanceKm));
                                                                        tvUserWithSelfDistance.setVisibility(View.VISIBLE);
                                                                    });
                                                                } else {
                                                                    Log.i("BluedHook", "用户隐藏了距离信息");
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e("UserInfoFragmentNewHook", "Hook位置\nhookAnchorMonitorAddButton.获取用户距离异常：\n" + e);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                });
                                CustomPopupWindow aMapPopupWindow = new CustomPopupWindow((Activity) fl_content.getContext(), userInfoExtraAMap, Color.parseColor("#FF0A121F"));
                                Drawable popupBg = null;
                                try {
                                    popupBg = modRes.getDrawable(R.drawable.bg_tech_space, null);
                                } catch (Exception e) {
                                    Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                    popupBg = new GradientDrawable();
                                    ((GradientDrawable) popupBg).setColor(Color.parseColor("#0A121F"));
                                }
                                aMapPopupWindow.setBackgroundDrawable(popupBg);
                                aMapPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
                                aMapPopupWindow.showAtCenter();
                                aMapPopupWindow.setOnDismissListener(() -> {
                                    aMapHelper.onPause();
                                    aMapHelper.onDestroy();
                                });
                            });

                            // 设置注册时间
                            String registrationTimeEncrypt = (String) XposedHelpers.getObjectField(userInfoEntity, "registration_time_encrypt");
                            String registrationTime = ModuleTools.AesDecrypt(registrationTimeEncrypt);
                            Drawable regTimeBg = null;
                            try {
                                regTimeBg = modRes.getDrawable(R.drawable.bg_tech_tag, null);
                            } catch (Exception e) {
                                Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
                                regTimeBg = new GradientDrawable();
                                ((GradientDrawable) regTimeBg).setColor(Color.parseColor("#333333"));
                                ((GradientDrawable) regTimeBg).setCornerRadius(10);
                            }
                            tvUserRegTime.setBackground(regTimeBg);
                            if (!registrationTime.isEmpty()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                String formattedDate = sdf.format(new Date(Long.parseLong(registrationTime) * 1000L));
                                tvUserRegTime.setText("注册时间：" + formattedDate);
                                tvUserRegTime.setTextSize(13f);
                                tvUserRegTime.setVisibility(View.VISIBLE);
                            } else {
                                tvUserRegTime.setVisibility(View.GONE);
                            }

                            // 特别关注按钮
                            int fl_buttonsID = getSafeContext().getResources().getIdentifier("fl_buttons", "id", getSafeContext().getPackageName());
                            FrameLayout flButtons = flFeedFragmentContainer.findViewById(fl_buttonsID);
                            LinearLayout followView = (LinearLayout) flButtons.getChildAt(1);
                            TextView specialFollowButton = createSpecialFollowButton(currentView.getContext());
                            updateButtonState(specialFollowButton, dbManager.getUserByUid(uid) != null);
                            setupButtonClickListener(specialFollowButton, userInfoEntity);
                            followView.addView(specialFollowButton, 0);

                            // 主播上线时间
                            if (isHideLastOperate == 1 && isAnchor == 1) {
                                ViewGroup rlBasicInfoRoot = flFeedFragmentContainer.findViewById(getSafeContext().getResources().getIdentifier("rl_basic_info_root", "id", getSafeContext().getPackageName()));
                                HorizontalScrollView horizontalScrollView = (HorizontalScrollView) rlBasicInfoRoot.getChildAt(0);
                                LinearLayout linearLayout = (LinearLayout) horizontalScrollView.getChildAt(0);
                                TextView tvLastOperateAnchor = new TextView(currentView.getContext());
                                tvLastOperateAnchor.setTextColor(Color.parseColor("#00FFA3"));
                                linearLayout.addView(tvLastOperateAnchor);
                                NetworkManager.getInstance().getAsync(NetworkManager.getBluedLiveSearchAnchorApi(name), AuthManager.auHook(false, classLoader, fl_content.getContext()), new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    }

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
                        }
                    }

                    private LinearLayout createUserInfoExtraLayout(Context context) {
                        LinearLayout layout = new LinearLayout(context);
                        layout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(16, 16, 16, 16);

                        // 定位按钮
                        Button locateButton = new Button(context);
                        locateButton.setId(R.id.user_locate_bt);
                        locateButton.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        locateButton.setText("定位");
                        locateButton.setTextColor(Color.WHITE);
                        locateButton.setPadding(16, 8, 16, 8);
                        locateButton.setTextSize(14);

                        // 注册时间 TextView
                        TextView regTimeTextView = new TextView(context);
                        regTimeTextView.setId(R.id.tv_user_reg_time);
                        regTimeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        regTimeTextView.setTextColor(Color.WHITE);
                        regTimeTextView.setPadding(16, 8, 16, 8);
                        regTimeTextView.setTextSize(13);
                        regTimeTextView.setVisibility(View.GONE);

                        layout.addView(locateButton);
                        layout.addView(regTimeTextView);
                        return layout;
                    }

                    private LinearLayout createUserInfoExtraAMapLayout(Context context) {
                        LinearLayout rootLayout = new LinearLayout(context);
                        rootLayout.setId(R.id.ll_location_root);
                        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        ));
                        rootLayout.setOrientation(LinearLayout.VERTICAL);
                        rootLayout.setPadding(16, 16, 16, 16);

                        // 地图容器
                        LinearLayout mapLayout = new LinearLayout(context);
                        mapLayout.setId(R.id.ll_aMap);
                        mapLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                0,
                                1.0f
                        ));
                        mapLayout.setOrientation(LinearLayout.VERTICAL);

                        // 位置数据容器
                        LinearLayout locationDataLayout = new LinearLayout(context);
                        locationDataLayout.setId(R.id.ll_location_data);
                        locationDataLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        locationDataLayout.setOrientation(LinearLayout.VERTICAL);
                        locationDataLayout.setPadding(8, 8, 8, 8);

                        // GPS 图标
                        ImageView gpsIcon = new ImageView(context);
                        gpsIcon.setId(R.id.iv_gps_icon);
                        gpsIcon.setLayoutParams(new LinearLayout.LayoutParams(
                                ModuleTools.dpToPx(24),
                                ModuleTools.dpToPx(24)
                        ));

                        // 用户名
                        TextView usernameTextView = new TextView(context);
                        usernameTextView.setId(R.id.tv_username);
                        usernameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        usernameTextView.setTextColor(Color.WHITE);
                        usernameTextView.setTextSize(16);
                        usernameTextView.setPadding(8, 4, 8, 4);

                        // 自动定位按钮
                        TextView autoLocationTextView = new TextView(context);
                        autoLocationTextView.setId(R.id.tv_auto_location);
                        autoLocationTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        autoLocationTextView.setText("自动定位");
                        autoLocationTextView.setTextColor(Color.WHITE);
                        autoLocationTextView.setTextSize(14);
                        autoLocationTextView.setPadding(16, 8, 16, 8);

                        // 经度
                        TextView longitudeTextView = new TextView(context);
                        longitudeTextView.setId(R.id.tv_longitude);
                        longitudeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        longitudeTextView.setTextColor(Color.WHITE);
                        longitudeTextView.setTextSize(14);
                        longitudeTextView.setPadding(8, 4, 8, 4);

                        // 纬度
                        TextView latitudeTextView = new TextView(context);
                        latitudeTextView.setId(R.id.tv_latitude);
                        latitudeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        latitudeTextView.setTextColor(Color.WHITE);
                        latitudeTextView.setTextSize(14);
                        latitudeTextView.setPadding(8, 4, 8, 4);

                        // 位置
                        TextView locationTextView = new TextView(context);
                        locationTextView.setId(R.id.tv_location);
                        locationTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        locationTextView.setTextColor(Color.WHITE);
                        locationTextView.setTextSize(14);
                        locationTextView.setPadding(8, 4, 8, 4);

                        // 虚拟距离
                        TextView distanceTextView = new TextView(context);
                        distanceTextView.setId(R.id.tv_user_with_self_distance);
                        distanceTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        ));
                        distanceTextView.setTextColor(Color.WHITE);
                        distanceTextView.setTextSize(14);
                        distanceTextView.setPadding(8, 4, 8, 4);

                        // 刷新按钮
                        ImageButton cleanButton = new ImageButton(context);
                        cleanButton.setId(R.id.iv_clean_icon);
                        LinearLayout.LayoutParams cleanParams = new LinearLayout.LayoutParams(
                                ModuleTools.dpToPx(40),
                                ModuleTools.dpToPx(40)
                        );
                        cleanParams.gravity = android.view.Gravity.END;
                        cleanButton.setLayoutParams(cleanParams);
                        cleanButton.setPadding(8, 8, 8, 8);

                        // 添加到位置数据容器
                        locationDataLayout.addView(gpsIcon);
                        locationDataLayout.addView(usernameTextView);
                        locationDataLayout.addView(autoLocationTextView);
                        locationDataLayout.addView(longitudeTextView);
                        locationDataLayout.addView(latitudeTextView);
                        locationDataLayout.addView(locationTextView);
                        locationDataLayout.addView(distanceTextView);

                        // 添加到根布局
                        rootLayout.addView(cleanButton);
                        rootLayout.addView(mapLayout);
                        rootLayout.addView(locationDataLayout);
                        return rootLayout;
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
                        return new Gradient()
                                .setRadius(CORNER_RADIUS)
                                .setColorLeft(color)
                                .setColorRight(color)
                                .build();
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
        Drawable doneIcon = null;
        try {
            doneIcon = modRes.getDrawable(R.drawable.ic_done, null);
        } catch (Exception e) {
            Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
        }
        ibvClean.setImageDrawable(doneIcon);
        handler.postDelayed(() -> {
            Drawable refreshIcon = null;
            try {
                refreshIcon = modRes.getDrawable(R.drawable.ic_refresh, null);
            } catch (Exception e) {
                Log.e("UserInfoFragmentNewHook", "Failed to load drawable: " + e.getMessage());
            }
            ibvClean.setImageDrawable(refreshIcon);
        }, 1000);
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
