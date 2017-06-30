package com.dx168.patchsdk.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.dx168.patchsdk.component.FullUpdateActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        new AlertDialog.Builder(this).setMessage("Hello Tinker!").show();

        Intent intent = new Intent(this,FullUpdateActivity.class);
        intent.putExtra("latestVersion","4.0.0");
        intent.putExtra("needUpdate",true);
        intent.putExtra("downloadUrl","http://192.168.27.15:8000/b.apk");
        intent.putExtra("title","");
        intent.putExtra("description","我是内容");
        intent.putExtra("forceUpdate",true);
        intent.putExtra("lowestSupportVersion","2.0.0");
        intent.putExtra("updatedAt","2017-06-30 10:42:06");

        startActivity(intent);
    }
}
