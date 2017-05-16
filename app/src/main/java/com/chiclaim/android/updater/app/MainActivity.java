package com.chiclaim.android.updater.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.chiclam.android.updater.Updater;

public class MainActivity extends AppCompatActivity {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.et_download);
        editText.setText("http://releases.b0.upaiyun.com/hoolay.apk");
        //如果没有停用,先去停用,然后点击下载按钮. 测试用户关闭下载服务
        //showDownloadSetting();
    }


    public void download(View view) {
        String url;
        if (TextUtils.isEmpty(editText.getText().toString())) {
            url = "http://releases.b0.upaiyun.com/hoolay.apk";
        } else {
            url = editText.getText().toString();
        }
        Updater.get().download(this, url, getResources().getString(R.string.app_name));
    }

}