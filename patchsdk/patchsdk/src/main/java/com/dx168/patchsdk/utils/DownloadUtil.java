package com.dx168.patchsdk.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by lichen on 16/7/26.
 */
public class DownloadUtil {
    public static final int DOWNLOAD_ERROR = 0;//错误
    public static final int DOWNLOAD_CANCEL = 1;//取消
    public static final int DOWNLOAD_FINISH = 2;//完成
    public static final int DOWNLOADING = 3;//下载中
    private static boolean isCancel = false;
    private static int downlaodState = 0;

    public static void download(final String url, final Handler handler) {
        isCancel = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(30 * 1000);
                    long filesize = conn.getContentLength();
                    int code = conn.getResponseCode();
                    if (code == 200 &&filesize > 0) {
                        File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                        if (!downloadDir.exists()) {
                            downloadDir.mkdir();
                        }
                        String filePath = new File(downloadDir,getFileNameFromUrl(url) + ".tmp").getAbsolutePath();
                        File file = new File(filePath);
                        InputStream in = conn.getInputStream();
                        FileOutputStream out = new FileOutputStream(file);
                        int len = 0;
                        long downSize = 0;
                        Integer i = 0;//百分比
                        byte[] buffer = new byte[1024];
                        while (!isCancel && (len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                            downSize += len;
                            if (downSize >= (filesize / 100) && i < 100) {//每下载1%通知一次
                                sendMessage(handler, DOWNLOADING, ++i);
                                downlaodState = DOWNLOADING;
                                downSize = 0;
                            }
                        }
                        out.flush();
                        out.close();
                        in.close();
                        sendMessage(handler, DOWNLOADING, 100);
                        downlaodState = DOWNLOADING;
                        if (file.length() == filesize) {
                            //下载完成
                            String finalpath = new File(downloadDir,getFileNameFromUrl(url)).getAbsolutePath();
                            File finalFile = new File(finalpath);
                            if(file.renameTo(finalFile)){
                                sendMessage(handler, DOWNLOAD_FINISH, finalpath);
                            };
                            downlaodState = DOWNLOAD_FINISH;
                        } else {//下载取消
                            sendMessage(handler, DOWNLOAD_CANCEL, "" + Thread.currentThread().getId());
                            downlaodState = DOWNLOAD_CANCEL;
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    } else {
                        sendMessage(handler, DOWNLOAD_ERROR, "http code" + code);
                        downlaodState = DOWNLOAD_ERROR;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(handler, DOWNLOAD_ERROR, getStackTraceAsString(e));
                    downlaodState = DOWNLOAD_ERROR;
                }
            }
        }).start();
    }

    public static void cancel() {
        isCancel = true;
    }

    public static int getDownlaodState() {
        return downlaodState;
    }

    public static void setDownlaodState(int downlaodState) {
        DownloadUtil.downlaodState = downlaodState;
    }

    private static void sendMessage(Handler handler, int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    public static String getFileNameFromUrl(String url) {
        Pattern p = Pattern.compile("[^/]*$");
        String[] str = p.split(url);
        String before = str[0];
        String after = url.substring(str[0].length());
        return after == null ? "" : after;
    }

    /**
     * getStackTraceAsString:(get ex to string)
     */
    private static String getStackTraceAsString(Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        return stringWriter.getBuffer().toString().replace("\t", "");
    }
}
