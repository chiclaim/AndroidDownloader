package com.chiclaim.android.downloader.sample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.chiclaim.android.downloader.*
import com.chiclaim.android.downloader.util.goNotificationSettings
import com.chiclaim.android.downloader.util.settingPackageInstall

class MainActivity : AppCompatActivity(), DownloadListener {

    companion object {
        private const val APK_URL = "https://app.2dfire.com/fandian/tv/tv_release_2010300.apk"
    }

    private var fileUrl: String = APK_URL
    private var mode = DownloadMode.EMBED
    private var ignoreLocalFile = false
    private var needInstall = false
    private var notifierDisableTip = false
    private var notifierVisibility: Int = 0
    private var isForceUpdate = false
    private var isBackgroundDownload = false


    private var downloader: Downloader<*>? = null

    private var editText: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.et_download) as? EditText
        editText?.setText(APK_URL)
    }

    private fun initDownloadParameters() {
        fileUrl = editText!!.text.toString()
        if (TextUtils.isEmpty(editText!!.text.toString())) {
            fileUrl = APK_URL
        }
        findViewById<RadioGroup>(R.id.rg_engine).run {
            fun setMode(id: Int) {
                when (id) {
                    R.id.rb_engine_dm -> mode = DownloadMode.DOWNLOAD_MANAGER
                    R.id.rb_engine_embed -> mode = DownloadMode.EMBED
                }
            }

            val checkBtnId = checkedRadioButtonId
            setMode(checkBtnId)
            setOnCheckedChangeListener { _, checkedId ->
                setMode(checkedId)
            }
        }

        findViewById<CheckBox>(R.id.cb_ignore_local).run {
            ignoreLocalFile = isChecked
            setOnCheckedChangeListener { _, isChecked ->
                ignoreLocalFile = isChecked
            }
        }

        findViewById<CheckBox>(R.id.cb_auto_install).run {
            needInstall = isChecked
            setOnCheckedChangeListener { _, isChecked ->
                needInstall = isChecked
            }
        }

        findViewById<CheckBox>(R.id.cb_disable_notifier_tip).run {
            notifierDisableTip = isChecked
            setOnCheckedChangeListener { _, isChecked ->
                notifierDisableTip = isChecked
            }
        }


        findViewById<RadioGroup>(R.id.rg_notification).run {
            fun setNotifierVisibility(id: Int) {
                notifierVisibility = when (id) {
                    R.id.rb_always_notification -> NOTIFIER_VISIBLE_NOTIFY_COMPLETED
                    R.id.rb_hide_notification -> NOTIFIER_HIDDEN
                    R.id.rb_only_complete_notification -> NOTIFIER_VISIBLE_NOTIFY_ONLY_COMPLETION
                    R.id.rb_only_downloading_notification -> NOTIFIER_VISIBLE
                    else -> NOTIFIER_VISIBLE_NOTIFY_COMPLETED
                }
            }
            setNotifierVisibility(checkedRadioButtonId)
            setOnCheckedChangeListener { _, checkedId ->
                setNotifierVisibility(checkedId)
            }
        }

        findViewById<CheckBox>(R.id.cb_force_update).run {
            isForceUpdate = isChecked
            setOnCheckedChangeListener { _, isChecked ->
                isForceUpdate = isChecked
            }
        }

        findViewById<CheckBox>(R.id.cb_background_download).run {
            isBackgroundDownload = isChecked
            setOnCheckedChangeListener { _, isChecked ->
                isBackgroundDownload = isChecked
            }
        }

    }

    private fun createCommonRequest(url: String): Request =
        DownloadRequest.newRequest(url, mode)
            .setNotificationTitle(resources.getString(R.string.app_name))
            .setNotificationContent(getString(R.string.downloader_notifier_description))
            .allowScanningByMediaScanner()
            .setIgnoreLocal(ignoreLocalFile)
            .setNeedInstall(needInstall)
            .setNotificationVisibility(notifierVisibility)
            .setNotificationSmallIcon(R.mipmap.ic_launcher)
            .setShowNotificationDisableTip(notifierDisableTip)


    fun download(view: View) {
        initDownloadParameters()
        downloader = createCommonRequest(fileUrl)
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
        initDownloadParameters()
        UpgradeDialogActivity.launch(this, UpgradeDialogInfo().also {
            it.url = fileUrl
            it.ignoreLocal = ignoreLocalFile
            it.title = if (isForceUpdate) "重要安全升级" else "发现新版本"
            it.description = "1. 修复已知问题\n2. 修复已知问题"
            it.forceUpdate = isForceUpdate
            it.negativeText = if (isForceUpdate) "退出程序" else "取消"
            it.notifierSmallIcon = R.mipmap.ic_launcher
            it.backgroundDownload = isBackgroundDownload
            it.needInstall = needInstall
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