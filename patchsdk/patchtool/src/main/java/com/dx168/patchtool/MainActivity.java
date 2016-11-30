package com.dx168.patchtool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.dx168.patchtool.utils.FileUtils;
import com.dx168.patchtool.utils.HttpUtils;
import com.dx168.patchtool.utils.Utils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_scan).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_scan: {
                new IntentIntegrator(this)
                        .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                        .initiateScan();
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        final IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            try {
                String[] strings = intentResult.getContents().split(";");
                String protocol = strings[0];
                if (TextUtils.equals(protocol, "ldpv1")) {
                    final String packageName = strings[1];
                    final String versionName = strings[2];
                    final String patchVersion = strings[3];
                    String url = strings[4];
                    String appVersionName = Utils.getVersionName(this, packageName);
                    if (!TextUtils.equals(versionName, appVersionName)) {
                        showDialog("补丁versionName=" + versionName + "\n" + "App versionName=" + appVersionName);
                        return;
                    }
                    HttpUtils.request(url, null, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, byte[] bytes) {
                            if (code == 200) {
                                try {
                                    File filesDir = getExternalFilesDir("");
                                    String patchPath = filesDir.getAbsolutePath() + File.separator + packageName + "_" + versionName + "_patch_" + patchVersion + ".apk";
                                    FileUtils.writeToDisk(bytes, patchPath);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog("补丁下载成功");
                                        }
                                    });
                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog("下载patch出错\n" + intentResult.getContents());
                                        }
                                    });
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDialog("访问patch url出错\n" + intentResult.getContents());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog("访问patch url出错\n" + intentResult.getContents());
                                }
                            });
                        }
                    });
                } else {
                    showDialog(intentResult.getContents());
                }
            } catch (Exception e) {
                showDialog(intentResult.getContents());
            }
        }
    }

    private void showDialog(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface anInterface, int which) {

                    }
                }).show();
    }

}
