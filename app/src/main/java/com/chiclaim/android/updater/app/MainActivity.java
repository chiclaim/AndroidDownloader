package com.chiclaim.android.updater.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.chiclam.android.updater.Updater;

public class MainActivity extends AppCompatActivity {

    private static final String APK_URL = "http://releases.b0.upaiyun.com/hoolay.apk";

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.et_download);
        editText.setText(APK_URL);
        //如果没有停用,先去停用,然后点击下载按钮. 测试用户关闭下载服务
        //UpdaterUtils.showDownloadSetting(this);
    }

    public void download(View view) {
        String url = editText.getText().toString();
        if (TextUtils.isEmpty(editText.getText().toString())) {
            url = APK_URL;
        }
        Updater.get().log(true).download(this, url, getResources().getString(R.string.app_name));
    }

}