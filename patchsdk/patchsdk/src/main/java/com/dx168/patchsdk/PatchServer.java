package com.dx168.patchsdk;

import com.dx168.patchsdk.utils.IgnoreNullHashMap;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by jianjun.lin on 16/7/14.
 */
class PatchServer {

    private static PatchServer instance;

    void free() {
        instance = null;
    }

    private PatchServer(String baseUrl) {
        this.baseUrl = baseUrl;
        this.threadPool = Executors.newSingleThreadExecutor();
    }

    private Executor threadPool;
    private String baseUrl;

    static void init(String baseUrl) {
        if (instance == null) {
            if (baseUrl.contains("/api/")) {
                baseUrl = baseUrl.substring(0, baseUrl.indexOf("/api/") + 1);
            }
            instance = new PatchServer(baseUrl);
        }
    }

    static PatchServer get() {
        if (instance == null) {
            throw new NullPointerException("PatchServer must be init before using");
        }
        return instance;
    }

    public void queryPatch(String appId, String token, String tag,
                           String versionName, int versionCode, String platform,
                           String osVersion, String model, String channel,
                           String sdkVersion, String deviceId,boolean withFullUpdateInfo,
                           PatchServerCallback callback) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appUid", appId);
        paramMap.put("token", token);
        paramMap.put("tag", tag);
        paramMap.put("versionName", versionName);
        paramMap.put("versionCode", versionCode);
        paramMap.put("platform", platform);
        paramMap.put("osVersion", osVersion);
        paramMap.put("model", model);
        paramMap.put("channel", channel);
        paramMap.put("sdkVersion", sdkVersion);
        paramMap.put("deviceId", deviceId);
        paramMap.put("withFullUpdateInfo", withFullUpdateInfo);
        request(baseUrl + "api/patch", paramMap, callback);
    }

    public void queryFullUpdateInfo(String appId, String token, String versionName, String channel, String sdkVersion, PatchServerCallback callback) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appUid", appId);
        paramMap.put("token", token);
        paramMap.put("versionName", versionName);
        paramMap.put("channel", channel);
        paramMap.put("sdkVersion", sdkVersion);
        request(baseUrl + "api/full_update", paramMap, callback);
    }

    public void report(String appId, String token, String tag,
                       String versionName, int versionCode, String platform,
                       String osVersion, String model, String channel,
                       String sdkVersion, String deviceId, String patchUid,
                       boolean applyResult,
                       PatchServerCallback callback) {
        Map<String, Object> paramMap = new IgnoreNullHashMap<>();
        paramMap.put("appUid", appId);
        paramMap.put("token", token);
        paramMap.put("tag", tag);
        paramMap.put("versionName", versionName);
        paramMap.put("versionCode", versionCode);
        paramMap.put("platform", platform);
        paramMap.put("osVersion", osVersion);
        paramMap.put("model", model);
        paramMap.put("channel", channel);
        paramMap.put("sdkVersion", sdkVersion);
        paramMap.put("deviceId", deviceId);
        paramMap.put("patchUid", patchUid);
        paramMap.put("applyResult", applyResult);
        request(baseUrl + "api/report", paramMap, callback);
    }

    public void downloadPatch(String url, PatchServerCallback callback) {
        request(url, null, callback);
    }

    public void request(final String url, final Map<String, Object> paramMap, final PatchServerCallback callback) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(30 * 1000);
                    conn.setDoInput(true);
                    if (paramMap != null && !paramMap.isEmpty()) {
                        conn.setDoOutput(true);
                        StringBuilder params = new StringBuilder();
                        for (String key : paramMap.keySet()) {
                            params.append(key).append("=").append(paramMap.get(key)).append("&");
                        }
                        outputStream = conn.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                        writer.write(params.toString());
                        writer.flush();
                        writer.close();
                    }
                    int code = conn.getResponseCode();
                    if (callback == null) {
                        return;
                    }

                    boolean redirect = false;
                    if (code != HttpURLConnection.HTTP_OK) {
                        if (code == HttpURLConnection.HTTP_MOVED_TEMP
                                || code == HttpURLConnection.HTTP_MOVED_PERM
                                || code == HttpURLConnection.HTTP_SEE_OTHER)
                            redirect = true;
                    }

                    if (redirect) {
                        // get redirect url from "location" header field
                        String url = conn.getHeaderField("Location");

                        // get the cookie if need, for login
                        String cookies = conn.getHeaderField("Set-Cookie");

                        conn = (HttpURLConnection) new URL(url).openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(30 * 1000);
                        conn.setDoInput(true);
                        conn.setRequestProperty("Cookie", cookies);

                        code = conn.getResponseCode();
                    }

                    if (code == 200) {
                        inputStream = conn.getInputStream();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int read;
                        while ((read = inputStream.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                        byte[] bytes = baos.toByteArray();
                        if (bytes == null || bytes.length == 0) {
                            callback.onFailure(new Exception("code=200, bytes is empty"));
                        } else {
                            callback.onSuccess(code, bytes);
                        }
                        baos.close();
                    } else {
                        callback.onFailure(new Exception("code=" + code));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    interface PatchServerCallback {
        void onSuccess(int code, byte[] bytes);

        void onFailure(Exception e);
    }

}
