package com.dx168.patchsdk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.dx168.patchsdk.component.FullUpdateActivity;
import org.json.JSONObject;

/**
 * Created by tong on 17/6/30.
 */
public class FullUpdateHandler {
    private static final String TAG = FullUpdateHandler.class.getSimpleName();

    /**
     * 处理app更新
     * @param context
     * @param obj
     */
    public void handleFullUpdate(Context context, JSONObject obj) {
//        {
//            "latestVersion": "4.0.0",
//                "needUpdate": true,
//                "downloadUrl": "http://static.tianxi66.cn/app-release-360-4.0.0.apk",
//                "description": "赶紧下载新版本",
//                "forceUpdate": true,
//                "title": "我是标题",
//                "lowestSupportVersion": "2.0.0",
//                "updatedAt": "2017-06-30 10:42:06"
//        }
        if (obj == null) {
            return;
        }
        Log.d(TAG,"handlerFullUpdate: " + obj.toString());
        if(obj.optBoolean("needUpdate",false)) {
            Intent intent = new Intent(context, FullUpdateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("latestVersion", obj.optString("latestVersion"));
            intent.putExtra("needUpdate", obj.optBoolean("needUpdate", false));
            intent.putExtra("downloadUrl", obj.optString("downloadUrl"));
            intent.putExtra("title", obj.optString("title"));
            intent.putExtra("description", obj.optString("description"));
            intent.putExtra("forceUpdate", obj.optBoolean("forceUpdate", false));
            intent.putExtra("lowestSupportVersion", obj.optString("lowestSupportVersion"));
            intent.putExtra("updatedAt", obj.optString("updatedAt"));
            context.startActivity(intent);
        }
    }

    public void handleError(Throwable e) {

    }
}
