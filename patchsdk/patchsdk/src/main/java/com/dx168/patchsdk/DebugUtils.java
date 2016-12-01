package com.dx168.patchsdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;

import com.dx168.patchsdk.bean.AppInfo;

import java.io.File;
import java.util.Random;

/**
 * Created by jianjun.lin on 2016/12/1.
 */

public class DebugUtils {

    private static final String DEBUG_PATCH_DIR_NAME = "com.dx168.patchtool";

    public static File findDebugPatch(AppInfo appInfo) {
        File patchDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DEBUG_PATCH_DIR_NAME);
        if (patchDir.exists() && patchDir.listFiles() != null) {
            String prefix = appInfo.getPackageName() + "_" + appInfo.getVersionName() + "_";
            for (File patch : patchDir.listFiles()) {
                if (patch.getName().startsWith(prefix) && patch.getName().endsWith(".apk")) {
                    return patch;
                }
            }
        }
        return null;
    }

    public static void sendNotify(Context context, String text) {
        //消息通知栏
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder//设置通知栏标题
                .setContentTitle(context.getApplicationInfo().loadLabel(context.getPackageManager()))
                .setContentText(text) //设置通知栏显示内容
                .setAutoCancel(true)
                .setTicker(text) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                //.setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                .setSmallIcon(context.getApplicationInfo().icon);
        mNotificationManager.notify(new Random().nextInt(), mBuilder.build());
    }

}
