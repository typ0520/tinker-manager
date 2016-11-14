package com.dx168.patchsdk.bean;

/**
 * Created by jianjun.lin on 2016/10/26.
 */
public class PatchInfo {

    private int code;
    private String message;
    private Data data;

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

    public class Data {

        private String versionName;
        private String patchUid;
        private String patchVersion;
        private String downloadUrl;
        private long patchSize;
        private String hash;

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

        public String getPatchUid() {
            return patchUid;
        }

        public void setPatchUid(String patchUid) {
            this.patchUid = patchUid;
        }

        @Override
        public String toString() {
            return "{patchVersion:" + patchVersion + ", patchSize:" + patchSize + ", downloadUrl:" + downloadUrl + ", hash:" + hash + ", patchUid:" + patchUid + "}";
        }
    }

    @Override
    public String toString() {
        String dataString = data == null ? null : data.toString();
        return "{code:" + code + ", message:" + message + ", data:" + dataString + "}";
    }
}
