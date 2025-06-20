package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {
    private final List<RecordItem> records = new ArrayList<>();
    private final Context context;

    public RecordAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 返回一个空 ViewHolder
        return new RecordViewHolder(new View(context), this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        // 不绑定任何数据
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class RecordItem {
        public String name;
        public int count;
        public long timestamp;
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        public RecordViewHolder(@NonNull View itemView, RecordAdapter adapter) {
            super(itemView);
        }
    }
}
