package com.chiclaim.android.updater.app

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chiclaim.android.updater.*
import com.chiclaim.android.updater.util.UpdaterUtils.showDownloadSetting
import com.chiclaim.android.updater.util.d
import java.math.BigDecimal
import java.math.RoundingMode

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
    }

    fun download(view: View) {
        var url = editText!!.text.toString()
        if (TextUtils.isEmpty(editText!!.text.toString())) {
            url = APK_URL
        }
        val request = DownloadRequest(url)
            .setNotificationTitle(resources.getString(R.string.app_name))
            .setNotificationDescription(getString(R.string.system_download_description))
            .allowScanningByMediaScanner()
            .setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_MOBILE
                        or DownloadManager.Request.NETWORK_WIFI
            )
        // 定义下载目录，默认为 /data/user/0/com.android.providers.downloads/cache/your_download_file_name
        //.setDestinationDir(Uri.fromFile(applicationContext.externalCacheDir))
        Downloader(applicationContext).start(request, object : DownloadListener {
            override fun onProgressUpdate(status: Int, totalSize: Long, downloadedSize: Long) {
                val progress =
                    if (totalSize <= 0) 0 else
                        BigDecimal((downloadedSize / totalSize.toDouble() * 100).toString()).setScale(
                            0,
                            RoundingMode.HALF_UP
                        ).toString()
                d("下载进度：$progress%")
            }

            override fun onComplete(uri: Uri?) {
                Toast.makeText(this@MainActivity, "下载完成=$uri", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun setting(view: View) {
        //如果没有停用,先去停用,然后点击下载按钮. 测试用户关闭下载服务
        showDownloadSetting(this)
    }

    fun showUpdateDialog(view: View) {
        UpgradeDialogActivity.launch(this, UpdaterDialogInfo().apply {
            url = APK_URL
            title = "发现新版本"
            description = "1. 修复已知问题\n2. 修复已知问题"
        })
    }

}