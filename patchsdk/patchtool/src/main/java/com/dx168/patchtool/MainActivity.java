package com.dx168.patchtool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dx168.patchtool.utils.FileUtils;
import com.dx168.patchtool.utils.HttpUtils;
import com.dx168.patchtool.utils.Utils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PATCH_DIR_NAME = "com.dx168.patchtool";

    private TextView mTvContent;
    private View mBtnScan;
    private View mBtnClear;
    private AppServer mAppServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnScan = findViewById(R.id.btn_scan);
        mBtnScan.setOnClickListener(this);
        mBtnClear = findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(this);
        mTvContent = (TextView) findViewById(R.id.tv_content);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PackageManager pm = getPackageManager();
        boolean hasPermission = (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", getPackageName()))
                && (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE", getPackageName()));
        if (!hasPermission) {
            String error = "PatchTool需要存储读写权限";
            showDialog(error);
            mTvContent.setText(error);
            mBtnScan.setEnabled(false);
            mBtnClear.setEnabled(false);
            return;
        }
        mBtnScan.setEnabled(true);
        mBtnClear.setEnabled(true);
        updateContent();
    }

    private void updateContent() {
        File patchDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PATCH_DIR_NAME);
        StringBuilder sb = new StringBuilder();
        if (patchDir.exists()) {
            File[] patches = patchDir.listFiles();
            if (patches != null) {
                for (File patch : patches) {
                    sb.append(patch.getName()).append("\n");
                }
            }
        }
        if (TextUtils.isEmpty(sb)) {
            mTvContent.setText("没有补丁");
        } else {
            mTvContent.setText(sb);
        }
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
            case R.id.btn_clear: {
                new AlertDialog.Builder(this)
                        .setMessage("确定清理全部补丁吗?")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface anInterface, int which) {

                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface anInterface, int which) {
                                File patchDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PATCH_DIR_NAME);
                                if (patchDir.exists() && patchDir.listFiles() != null && patchDir.listFiles().length > 0) {
                                    for (File patch : patchDir.listFiles()) {
                                        patch.delete();
                                    }
                                }
                                mTvContent.setText("没有补丁");
                            }
                        }).show();
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
                    Toast.makeText(getApplicationContext(), "正在下载补丁", Toast.LENGTH_LONG).show();
                    HttpUtils.request(url, null, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, byte[] bytes) {
                            if (code == 200) {
                                try {
                                    final String patchPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + PATCH_DIR_NAME
                                            + File.separator + packageName + "_" + versionName + "_" + patchVersion + ".apk";
                                    FileUtils.writeToDisk(bytes, patchPath);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setMessage("下载补丁成功\n" + patchPath + "\n\n是否立即应用?")
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface anInterface, int which) {
                                                        }
                                                    })
                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface anInterface, int which) {
                                                            if (mAppServer != null) {
                                                                mAppServer.destroy();
                                                            }
                                                            mAppServer = new AppServer(MainActivity.this, packageName);
                                                            mAppServer.applyPatch(new AppServer.Callback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                    showDialog("应用成功");
                                                                }

                                                                @Override
                                                                public void onFailure() {
                                                                    showDialog("应用失败");
                                                                }
                                                            });
                                                        }
                                                    }).show();
                                            updateContent();
                                        }
                                    });
                                } catch (final Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showDialog("下载补丁出错\n" + intentResult.getContents() + "\n" + e.toString());
                                        }
                                    });
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showDialog("访问补丁下载地址出错\n" + intentResult.getContents());
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
                                    showDialog("访问补丁下载地址出错\n" + intentResult.getContents());
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

    @Override
    protected void onDestroy() {
        HttpUtils.cancel();
        if (mAppServer != null) {
            mAppServer.destroy();
        }
        super.onDestroy();
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
