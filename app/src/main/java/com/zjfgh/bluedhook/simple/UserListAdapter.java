package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class UserListAdapter extends ListAdapter<User, UserListAdapter.UserViewHolder> {
    private final SQLiteManagement dbManager = SQLiteManagement.getInstance();
    private final Context context;
    private String currentCheckingUid = null;

    public interface OnUserDeleteListener {
        void onUserDelete(User user) throws JSONException;
    }

    private final OnUserDeleteListener deleteListener;

    protected UserListAdapter(@NonNull Context context, OnUserDeleteListener deleteListener) {
        super(new UserDiffCallback());
        this.context = context;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = getResId(context, "anchor_monitor_item_layout", "layout");
        View view = LayoutInflater.from(context).inflate(
                AppContainer.getInstance().getModuleRes().getLayout(layoutId), parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = getItem(position);
        try {
            holder.bind(user);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCurrentCheckingUid(String uid) {
        this.currentCheckingUid = uid;
        notifyDataSetChanged();
    }

    private int getResId(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        LinearLayout anchorMonitorItemRoot;
        ImageView avatar;
        TextView userName, liveId, uid, uuid, encUid;
        CheckBox strongRemind, voiceRemind, joinLive, avatarDownload;

        UserViewHolder(View itemView) {
            super(itemView);
            anchorMonitorItemRoot = itemView.findViewById(getResId(context, "anchor_monitor_item_root", "id"));
            avatar = itemView.findViewById(getResId(context, "avatar", "id"));
            userName = itemView.findViewById(getResId(context, "user_name", "id"));
            uid = itemView.findViewById(getResId(context, "uid", "id"));
            uuid = itemView.findViewById(getResId(context, "uuid", "id"));
            encUid = itemView.findViewById(getResId(context, "enc_uid", "id"));
            liveId = itemView.findViewById(getResId(context, "live_id", "id"));
            strongRemind = itemView.findViewById(getResId(context, "strong_remind", "id"));
            voiceRemind = itemView.findViewById(getResId(context, "voice_remind", "id"));
            joinLive = itemView.findViewById(getResId(context, "join_live", "id"));
            avatarDownload = itemView.findViewById(getResId(context, "avatar_download", "id"));
        }

        @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables", "DiscouragedApi"})
        void bind(User user) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            strongRemind.setOnCheckedChangeListener(null);
            voiceRemind.setOnCheckedChangeListener(null);
            joinLive.setOnCheckedChangeListener(null);
            avatarDownload.setOnCheckedChangeListener(null);

            int iconUserFace = getResId(context, "icon_user_face", "drawable");
            Drawable checkBgDrawable;
            if (user.getUid().equals(currentCheckingUid)) {
                checkBgDrawable = new Gradient()
                        .setColorLeft("#FFA500")
                        .setColorRight("#FF8C00")
                        .setRadius(14f)
                        .build();
            } else {
                checkBgDrawable = AppContainer.getInstance().getModuleRes().getDrawable(iconUserFace, null);
            }
            anchorMonitorItemRoot.setBackground(checkBgDrawable);

            Glide.with(context)
                    .load(user.getAvatar())
                    .placeholder(iconUserFace)
                    .error(0)
                    .into(avatar);

            userName.setText(user.getName());
            liveId.setText("直播ID " + user.getLive());
            uid.setText("注册ID " + user.getUid());
            uuid.setText("用户ID " + user.getUnion_uid());

            if (user.getEnc_uid().isEmpty()) {
                encUid.setVisibility(View.GONE);
            } else {
                encUid.setVisibility(View.VISIBLE);
                encUid.setText("加密ID " + user.getEnc_uid());
            }

            strongRemind.setChecked(user.isStrongRemind());
            voiceRemind.setChecked(user.isVoiceRemind());
            joinLive.setChecked(user.isJoinLive());
            avatarDownload.setChecked(user.isAvatarDownload());

            strongRemind.setOnCheckedChangeListener((buttonView, isChecked) -> {
                user.setStrongRemind(isChecked);
                dbManager.updateUserStrongRemind(user.getUid(), isChecked);
            });

            voiceRemind.setOnCheckedChangeListener((buttonView, isChecked) -> {
                user.setVoiceRemind(isChecked);
                dbManager.updateUserVoiceRemind(user.getUid(), isChecked);
            });

            joinLive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                user.setJoinLive(isChecked);
                dbManager.updateUserJoinLive(user.getUid(), isChecked);
            });

            avatarDownload.setOnCheckedChangeListener((buttonView, isChecked) -> {
                user.setAvatarDownload(isChecked);
                dbManager.updateUserAvatarDownload(user.getUid(), isChecked);
            });

            itemView.setOnLongClickListener(v -> {
                DeleteConfirmationDialog.show(context, user.getName(), new DeleteConfirmationDialog.DeleteConfirmationListener() {
                    @Override
                    public void onConfirmDelete() {
                        try {
                            deleteListener.onUserDelete(user);
                        } catch (JSONException e) {
                            Log.e("UserListAdapter", "delAnchor error", e);
                        }
                    }

                    @Override
                    public void onCancel() {
                        // 用户取消操作，不做任何处理
                    }
                });
                return true;
            });
        }
    }

    static class UserDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUid().equals(newItem.getUid());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getLive().equals(newItem.getLive())
                    && oldItem.isStrongRemind() == newItem.isStrongRemind()
                    && oldItem.isVoiceRemind() == newItem.isVoiceRemind()
                    && oldItem.isJoinLive() == newItem.isJoinLive()
                    && oldItem.isAvatarDownload() == newItem.isAvatarDownload();
        }
    }
}
