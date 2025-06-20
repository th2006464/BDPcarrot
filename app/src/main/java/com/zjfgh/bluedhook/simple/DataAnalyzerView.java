package com.zjfgh.bluedhook.simple;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class DataAnalyzerView extends androidx.recyclerview.widget.RecyclerView {
    public DataAnalyzerView(@NonNull Context context) {
        super(context);
    }

    public DataAnalyzerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DataAnalyzerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRecordsData(org.json.JSONObject recordsData) {
        // Empty implementation
    }

    public void onDestroy() {
        // Empty implementation
    }

    public static class RecordItem {
        public RecordItem(String time, String giftType, String user, String gift,
                          String beans, String count, String total, String toAnchor) {
            // Empty implementation
        }
    }
}
