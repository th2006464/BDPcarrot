package com.zjfgh.bluedhook.simple;

import java.util.List;

public class JinShanDocApiResponse {

    public static class Data {
        private List<Log> logs;
        private String result;

        public List<Log> getLogs() { return null; }
        public void setLogs(List<Log> logs) {}

        public String getResult() { return ""; }
        public void setResult(String result) {}
    }

    public static class Log {
        private String filename;
        private String timestamp;
        private long unix_time;
        private String level;
        private List<String> args;

        public String getFilename() { return ""; }
        public void setFilename(String filename) {}

        public String getTimestamp() { return ""; }
        public void setTimestamp(String timestamp) {}

        public long getUnix_time() { return 0; }
        public void setUnix_time(long unix_time) {}

        public String getLevel() { return ""; }
        public void setLevel(String level) {}

        public List<String> getArgs() { return null; }
        public void setArgs(List<String> args) {}
    }

    public static class ResultData {
        private int code;
        private List<User> data;

        public int getCode() { return 0; }
        public void setCode(int code) {}

        public List<User> getData() { return null; }
        public void setData(List<User> data) {}
    }

    public static class User {
        private String name;
        private String uid;

        public String getName() { return ""; }
        public void setName(String name) {}

        public String getUid() { return ""; }
        public void setUid(String uid) {}
    }

    // 所有 Getter 返回默认值，Setter 不做任何操作
    public Data getData() { return new Data(); }
    public void setData(Data data) {}

    public String getError() { return ""; }
    public void setError(String error) {}

    public String getStatus() { return ""; }
    public void setStatus(String status) {}
}
