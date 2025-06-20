package com.zjfgh.bluedhook.simple;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {
    private final List<RecordItem> records = new ArrayList<>();

    public RecordAdapter(List<RecordItem> records) {
        // 初始化为空列表，防止 NullPointerException
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 返回一个空的 ViewHolder
        return new RecordViewHolder(new View(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        // 不绑定任何数据
    }

    @Override
    public int getItemCount() {
        return records.size(); // 返回默认数量
    }

    public static class RecordItem {
        // 保留字段定义，方便未来恢复使用
        public String time;
        public String giftType;
        public String user;
        public String gift;
        public String beans;
        public String count;
        public String total;
        public String toAnchor;
    }

    private void refreshData() {
        // 不执行任何数据刷新操作
    }

    private void analyzeData() {
        // 不执行任何数据分析操作
    }

    public void setRecordsData(JSONObject recordsData) {
        // 不处理数据
    }
}

class RecordViewHolder extends RecyclerView.ViewHolder {

    public RecordViewHolder(@NonNull View itemView) {
        super(itemView);
        // 不初始化任何 View
    }

    public void bind(RecordItem item, int position) {
        // 不绑定任何数据
    }
}
