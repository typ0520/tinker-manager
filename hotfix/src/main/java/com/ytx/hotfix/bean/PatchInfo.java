package com.ytx.hotfix.bean;

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
        private String patchVersion;
        private String url;

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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "{versionName:" + versionName + ", patchVersion:" + patchVersion + ", url:" + url + "}";
        }
    }

    @Override
    public String toString() {
        return "{code:" + code + ", message:" + message + ", data:" + data == null ? null : data.toString() + "}";
    }
}
