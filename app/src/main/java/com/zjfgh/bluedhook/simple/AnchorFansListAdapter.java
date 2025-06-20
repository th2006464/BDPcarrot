package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.XModuleResources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AnchorFansListAdapter extends RecyclerView.Adapter<AnchorFansListAdapter.MyViewHolder> {
    protected Context context;
    private final List<AnchorFansListBean> data;
    private final XModuleResources modRes;

    public AnchorFansListAdapter(Context context, List<AnchorFansListBean> data, XModuleResources modRes) {
        this.context = context;
        this.data = data;
        this.modRes = modRes;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 返回一个空的 ViewHolder
        View view = new View(context);
        return new MyViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // 不执行任何绑定逻辑
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public static class AnchorFansListBean {
        public int status = 0;
        public int gift_count;
        public String message;
        public long anchor;
        public int relation;
        public String anchor_name;
        public String name;
        public int level;
        public int relation_level;
        public int level_next;
        public int next_level_relation;
        public int relation_limit;
        public int relation_today;
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
