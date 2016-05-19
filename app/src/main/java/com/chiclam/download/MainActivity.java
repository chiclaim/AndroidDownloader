package com.chiclam.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.et_download);
    }

    public void download(View view) {
        String url;
        if (TextUtils.isEmpty(editText.getText().toString())) {
            url = "http://www.chunyuyisheng.com/download/chunyu/latest/?vendor=chunyu&app=7";
        } else {
            url = editText.getText().toString();
        }
        ApkUpdateUtils.download(this, url, getResources().getString(R.string.app_name));
    }

}
