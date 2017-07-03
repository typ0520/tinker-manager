package com.dx168.patchsdk.component;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.dx168.patchsdk.R;
import com.dx168.patchsdk.utils.DownloadUtil;
import java.io.File;

/**
 * Created by tong on 17/6/30.
 */
public class FullUpdateActivity extends Activity {
    private static final String TAG = FullUpdateActivity.class.getSimpleName();

    private boolean forceUpdate;
    private View patchsdk_download;
    private View patchsdk_content;
    private View patchsdk_install;
    private TextView patchsdk_tv_title;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_update);

        forceUpdate = getIntent().getBooleanExtra("forceUpdate",false);
        patchsdk_download = findViewById(R.id.patchsdk_download);
        patchsdk_content = findViewById(R.id.patchsdk_content);
        patchsdk_install = findViewById(R.id.patchsdk_install);
        pb = (ProgressBar) findViewById(R.id.patchsdk_pb);

        patchsdk_tv_title = (TextView) findViewById(R.id.patchsdk_tv_title);
        TextView patchsdk_tv_latest_version = (TextView) findViewById(R.id.patchsdk_tv_latest_version);
        TextView patchsdk_tv_update_time = (TextView) findViewById(R.id.patchsdk_tv_update_time);
        TextView patchsdk_tv_desc = (TextView) findViewById(R.id.patchsdk_tv_desc);

        findViewById(R.id.patchsdk_btn_install).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String apkPath = (String) patchsdk_install.getTag();
                if (TextUtils.isEmpty(apkPath)) {
                    return;
                }

                installApk(apkPath);
            }
        });

        String title = getIntent().getStringExtra("title");
        final String latestVersion = getIntent().getStringExtra("latestVersion");
        String description = getIntent().getStringExtra("description");
        String updatedAt = getIntent().getStringExtra("updatedAt");

        if (TextUtils.isEmpty(title)) {
            patchsdk_tv_title.setText(getApplicationName());
        }
        else {
            patchsdk_tv_title.setText(title);
        }
        patchsdk_tv_latest_version.setText("版本:" + latestVersion);
        patchsdk_tv_update_time.setText("更新时间:" + updatedAt);
        patchsdk_tv_desc.setText(description);

        Button patchsdk_btn_not_update = (Button) findViewById(R.id.patchsdk_btn_not_update);
        patchsdk_btn_not_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        View patchsdk_view_split = findViewById(R.id.patchsdk_view_split);
        Button patchsdk_btn_update = (Button) findViewById(R.id.patchsdk_btn_update);
        patchsdk_btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadApk(getIntent().getStringExtra("downloadUrl"),latestVersion);
            }
        });

        if (forceUpdate) {
            patchsdk_btn_not_update.setVisibility(View.GONE);
            patchsdk_view_split.setVisibility(View.GONE);
        }
    }

    private void installApk(String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    @Override
    public void onBackPressed() {
        if (!forceUpdate) {
            super.onBackPressed();
        }
    }

    private void cancel() {
        DownloadUtil.cancel();
    }

    @Override
    protected void onDestroy() {
        cancel();
        super.onDestroy();
    }

    private void downloadApk(String downloadUrl, String latestVersion) {
        cancel();
        final boolean localForceUpdate = forceUpdate;

        patchsdk_content.setVisibility(View.GONE);
        patchsdk_download.setVisibility(View.VISIBLE);

        pb.setMax(100);
        pb.setProgress(0);

        forceUpdate = true;
        patchsdk_tv_title.setText("正在下载");
        DownloadUtil.download(downloadUrl, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case DownloadUtil.DOWNLOAD_ERROR://失败
                        Log.e("DL", "onError :" + msg.obj);
                    {
                        forceUpdate = localForceUpdate;
                        patchsdk_content.setVisibility(View.VISIBLE);
                        patchsdk_download.setVisibility(View.GONE);

                        Toast.makeText(FullUpdateActivity.this, "下载失败", Toast.LENGTH_LONG).show();

                        String title = getIntent().getStringExtra("title");
                        if (TextUtils.isEmpty(title)) {
                            patchsdk_tv_title.setText(getApplicationName());
                        }
                        else {
                            patchsdk_tv_title.setText(title);
                        }
                    }
                    break;
                    case DownloadUtil.DOWNLOAD_CANCEL://取消
                    {
                        Log.e("DL", "cancel thread id:" + msg.obj);
                    }
                    break;
                    case DownloadUtil.DOWNLOAD_FINISH:
                    {
                        //完成
                        Log.e("DL", "finish filepath:" + msg.obj);
                        pb.setMax(100);
                        pb.setProgress(100);

                        patchsdk_install.setVisibility(View.VISIBLE);
                        patchsdk_install.setTag((String) msg.obj);

                        String title = getIntent().getStringExtra("title");
                        if (TextUtils.isEmpty(title)) {
                            patchsdk_tv_title.setText(getApplicationName());
                        }
                        else {
                            patchsdk_tv_title.setText(title);
                        }
                        installApk((String) msg.obj);

                    }
                    break;
                    case DownloadUtil.DOWNLOADING: {
                        //下载中
                        Log.e("DL", "downloading:" + msg.obj + "%");
                        pb.setProgress((Integer) msg.obj);
                    }
                    break;
                }
            }
        });
    }
}
