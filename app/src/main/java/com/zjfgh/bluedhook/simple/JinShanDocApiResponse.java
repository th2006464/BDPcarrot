package com.zjfgh.bluedhook.simple;

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

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }
}
