package com.chiclaim.android.updater.app;

import android.app.DownloadManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.chiclam.android.updater.Downloader;
import com.chiclam.android.updater.DownloadRequest;
import com.chiclam.android.updater.util.UpdaterUtils;

public class MainActivity extends AppCompatActivity {

    private static final String APK_URL = "https://app.2dfire.com/fandian/tv/tv_release_2010300.apk";

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.et_download);
        editText.setText(APK_URL);

    }

    public void download(View view) {
        String url = editText.getText().toString();
        if (TextUtils.isEmpty(editText.getText().toString())) {
            url = APK_URL;
        }
        DownloadRequest request = new DownloadRequest(url)
                .setTitle(getResources().getString(R.string.app_name))
                .setDescription(getString(R.string.system_download_description))
                .allowScanningByMediaScanner()
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                        | DownloadManager.Request.NETWORK_WIFI);

        new Downloader(getApplicationContext()).start(request);
    }

    public void setting(View view) {
        //如果没有停用,先去停用,然后点击下载按钮. 测试用户关闭下载服务
        UpdaterUtils.showDownloadSetting(this);
    }

}