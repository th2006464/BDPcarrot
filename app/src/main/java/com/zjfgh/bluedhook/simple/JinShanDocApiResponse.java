package com.zjfgh.bluedhook.simple;

import java.util.Collections;
import java.util.List;

public class JinShanDocApiResponse {
    private Data data;

    public Data getData() {
        return data != null ? data : new Data();
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private String result;

        public String getResult() {
            return result != null ? result : "";
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    public static class ResultData {
        private int code;
        private List<User> data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public List<User> getData() {
            return data != null ? data : Collections.emptyList();
        }

        public void setData(List<User> data) {
            this.data = data;
        }
    }
}
