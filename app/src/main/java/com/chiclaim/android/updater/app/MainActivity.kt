package com.chiclaim.android.updater.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.chiclaim.android.updater.*
import com.chiclaim.android.updater.util.goNotificationSettings
import com.chiclaim.android.updater.util.settingPackageInstall

class MainActivity : AppCompatActivity(), DownloadListener {

    companion object {
        private const val APK_URL =
            "https://download.2dfire.com/app2/1002/da8470cd64a42247304276858b7b461_2Dfire_Manager_6076.apk"
    }

    private var mode = DownloadMode.EMBED
    private var downloader: Downloader<*>? = null

    private var editText: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.et_download) as? EditText
        editText?.setText(APK_URL)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mode_system -> {
                mode = DownloadMode.DOWNLOAD_MANAGER
                return true
            }
            R.id.mode_embed -> {
                mode = DownloadMode.EMBED
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun download(view: View) {
        var url = editText!!.text.toString()
        if (TextUtils.isEmpty(editText!!.text.toString())) {
            url = APK_URL
        }


        downloader = DownloadRequest.newRequest(url, mode)
            .setNotificationTitle(resources.getString(R.string.app_name))
            .setNotificationContent(getString(R.string.system_download_description))
            .allowScanningByMediaScanner()
            .setIgnoreLocal(true)
            .setNeedInstall(true)
            .setNotificationVisibility(NOTIFIER_VISIBLE)
            .setNotificationSmallIcon(R.mipmap.ic_launcher)
            .setShowNotificationDisableTip(true)
            //.setAllowedNetworkTypes(
            //    DownloadManager.Request.NETWORK_MOBILE
            //            or DownloadManager.Request.NETWORK_WIFI
            //)
            // DownloadMode.DOWNLOAD_MANAGER，默认为 /data/user/0/com.android.providers.downloads/cache/your_download_file_name
            //.setDestinationDir(Uri.fromFile(applicationContext.externalCacheDir))
            .buildDownloader(applicationContext).registerListener(this)
        downloader?.startDownload()
    }

    override fun onDownloadStart() {

    }

    var callbackCount = 0
    override fun onProgressUpdate(percent: Int) {

        Log.d("MainActivity", "${++callbackCount} - 下载进度：$percent%")
    }

    override fun onDownloadComplete(uri: Uri) {
        Log.d("MainActivity", "下载完成")
    }

    override fun onDownloadFailed(e: Throwable) {
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
            ignoreLocal = false
            title = "发现新版本"
            description = "1. 修复已知问题\n2. 修复已知问题"
            notifierSmallIcon = R.mipmap.ic_launcher
        }, mode)
    }

    fun settingInstallPermission(view: View) {
        settingPackageInstall(this, 100)
    }

    fun goSettingNotification(view: View) {
        goNotificationSettings(this)
    }

    fun checkWritePermission(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            for (str in permissions) {
                if (checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permissions, 100)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloader?.unregisterListener(this)
    }

}