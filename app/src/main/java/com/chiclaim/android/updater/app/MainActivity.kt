package com.chiclaim.android.updater.app

import android.Manifest
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chiclaim.android.updater.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val APK_URL = "https://app.2dfire.com/fandian/tv/tv_release_2010300.apk"
    }

    private var editText: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.et_download) as? EditText
        editText?.setText(APK_URL)
        requestPermission()
    }

    fun download(view: View) {
        var url = editText!!.text.toString()
        if (TextUtils.isEmpty(editText!!.text.toString())) {
            url = APK_URL
        }

        DownloadRequest.newRequest(url, DownloadMode.EMBED)
            .setNotificationTitle(resources.getString(R.string.app_name))
            .setNotificationDescription(getString(R.string.system_download_description))
            .allowScanningByMediaScanner()
            .setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_MOBILE
                        or DownloadManager.Request.NETWORK_WIFI
            )
            // DownloadMode.DOWNLOAD_MANAGER，默认为 /data/user/0/com.android.providers.downloads/cache/your_download_file_name
            //.setDestinationDir(Uri.fromFile(applicationContext.externalCacheDir))
            .buildDownloader(applicationContext).startDownload(object : DownloadListener {
                override fun onProgressUpdate(status: Int, totalSize: Long, downloadedSize: Long) {
                    val progress =
                        if (totalSize <= 0) 0 else
                            (downloadedSize / totalSize.toDouble() * 100).toInt()
                    Log.d("MainActivity", "下载进度：$progress%")
                }

                override fun onComplete(uri: Uri?) {
                    Toast.makeText(this@MainActivity, "下载完成=$uri", Toast.LENGTH_SHORT).show()
                }

                override fun onFailed(e: Throwable) {
                }
            })
    }

    fun setting(view: View) {
        //如果没有停用,先去停用,然后点击下载按钮. 测试用户关闭下载服务
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:com.android.providers.downloads")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    fun showUpdateDialog(view: View) {
        UpgradeDialogActivity.launch(this, UpgradeDialogInfo().apply {
            url = APK_URL
            title = "发现新版本"
            description = "1. 修复已知问题\n2. 修复已知问题"
        })
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            val REQUEST_CODE_PERMISSION_STORAGE = 100
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            for (str in permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, REQUEST_CODE_PERMISSION_STORAGE)
                }
            }
        }

    }

}