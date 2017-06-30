package com.dx168.patchsdk;

import org.json.JSONObject;

/**
 * Created by tong on 17/6/30.
 */
public class FullUpdateHandler {
    public void handlerFullUpdate(JSONObject obj) {
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

        if (!obj.optBoolean("needUpdate",false)) {
            return;
        }

    }
}
