package com.dx168.patchsdk.bean;

import org.json.JSONObject;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public class PatchInfo {

    private int code;
    private String message;
    private Data data;
    private JSONObject fullUpdateInfo;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public JSONObject getFullUpdateInfo() {
        return fullUpdateInfo;
    }

    public void setFullUpdateInfo(JSONObject fullUpdateInfo) {
        this.fullUpdateInfo = fullUpdateInfo;
    }

    public static class Data {

        private String versionName;
        private String uid;
        private String patchVersion;
        private String downloadUrl;
        private long patchSize;
        private String hash;
        private String hashJiagu;
        private String downloadUrlJiagu;

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getPatchVersion() {
            return patchVersion;
        }

        public void setPatchVersion(String patchVersion) {
            this.patchVersion = patchVersion;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public long getPatchSize() {
            return patchSize;
        }

        public void setPatchSize(long patchSize) {
            this.patchSize = patchSize;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getHashJiagu() {
            return hashJiagu;
        }

        public void setHashJiagu(String hashJiagu) {
            this.hashJiagu = hashJiagu;
        }

        public String getDownloadUrlJiagu() {
            return downloadUrlJiagu;
        }

        public void setDownloadUrlJiagu(String downloadUrlJiagu) {
            this.downloadUrlJiagu = downloadUrlJiagu;
        }

        @Override
        public String toString() {
            return "{patchVersion:" + patchVersion + ", patchSize:" + patchSize + ", downloadUrl:" + downloadUrl + ", hash:" + hash + ", downloadUrlJiagu:" + downloadUrlJiagu + ", hashJiagu:" + hashJiagu + ", uid:" + uid + "}";
        }
    }

    @Override
    public String toString() {
        String dataString = data == null ? null : data.toString();
        return "{code:" + code + ", message:" + message + ", data:" + dataString + "}";
    }
}
