package com.dx168.patchtool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dx168.patchtool.utils.FileUtils;
import com.dx168.patchtool.utils.HttpUtils;
import com.dx168.patchtool.utils.Utils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DebugReceiver.OnReceiveListener {
    private static final String PATCH_DIR_NAME = "com.dx168.patchtool";
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mTvContent;
    private View mBtnScan;
    private View mBtnClear;
    private Button mBtnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DebugReceiver.register(this);
        mBtnScan = findViewById(R.id.btn_scan);
        mBtnScan.setOnClickListener(this);
        mBtnClear = findViewById(R.id.btn_clear);
        mBtnUpdate = (Button) findViewById(R.id.btn_update);
        mBtnClear.setOnClickListener(this);
        mBtnUpdate.setOnClickListener(this);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        checkUpdate();
    }

    private void checkUpdate() {
        OkHttpUtils
                .get()
                .url("http://api.fir.im/apps/latest/com.dx168.patchtool/?api_token=ce5ab187df9e366390dba9f9315c8292&type=android")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, response);
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(response);
                        } catch (JSONException e) {
                            return;
                        }
                        String versionShort = obj.optString("versionShort");
                        if (TextUtils.isEmpty(versionShort)
                                || BuildConfig.VERSION_NAME.equals(versionShort)
                                || TextUtils.isEmpty(obj.optString("installUrl"))
                                || !obj.optString("installUrl").startsWith("http")) {
                            return;
                        }

                        mBtnUpdate.setVisibility(View.VISIBLE);
                        mBtnUpdate.setTag(obj.optString("installUrl"));
                    }
                });
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
                        .setCancelable(false)
                        .setMessage("确定清除全部补丁吗?")
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
            case R.id.btn_update: {
                if (mBtnUpdate.getTag() == null) {
                    return;
                }
                Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_LONG).show();
                String downloadUrl = (String) mBtnUpdate.getTag();

                final File apkFile = new File(Environment.getExternalStorageDirectory().getPath(), getPackageName() + "_patchTool.apk");
                OkHttpUtils.get().url(downloadUrl).build().execute(new FileCallBack(apkFile.getParent(), apkFile.getName()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        if (e != null) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.this, "下载失败!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(File response, int id) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + apkFile.getAbsolutePath()), "application/vnd.android.package-archive");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//4.0以上系统弹出安装成功打开界面

                        mBtnUpdate.setText("下载完成，点击安装");
                        mBtnUpdate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(intent);
                            }
                        });

                        new AlertDialog.Builder(MainActivity.this)
                                .setCancelable(false)
                                .setMessage("下载完成是否安装?")
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface anInterface, int which) {

                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface anInterface, int which) {
                                        startActivity(intent);
                                    }
                                }).show();
                    }
                });
            }
            break;
        }
    }

    private void install(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//4.0以上系统弹出安装成功打开界面
        startActivity(intent);
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
                                                    .setCancelable(false)
                                                    .setMessage("下载补丁成功\n" + patchPath + "\n\n是否立即应用?")
                                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface anInterface, int which) {
                                                        }
                                                    })
                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface anInterface, int which) {
                                                            Intent intent = new Intent("com.dx168.patchsdk.DebugReceiver.APPLY_PATCH");
                                                            intent.putExtra("packageName", packageName);
                                                            intent.putExtra("what", 1);
                                                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                                            sendBroadcast(intent);
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
        DebugReceiver.unregister(this);
        super.onDestroy();
    }

    private void showDialog(String msg) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(msg)
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface anInterface, int which) {

                    }
                }).show();
    }

    @Override
    public void onReceive(Intent intent) {
        final Bundle data = intent.getExtras();
        if (data == null) {
            return;
        }
        boolean success = data.getBoolean("success", false);
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("补丁应用成功，是否重启App?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface anInterface, int which) {
                        Intent intent = new Intent("com.dx168.patchsdk.DebugReceiver.RESTART");
                        intent.putExtra("what", 2);
                        intent.putExtra("packageName", data.getString("packageName"));
                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(intent);
                    }
                }).show();
    }
}
