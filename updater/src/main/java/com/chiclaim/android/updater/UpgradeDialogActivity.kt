package com.chiclaim.android.updater

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chiclaim.android.updater.util.Utils.getTipFromException
import com.chiclaim.android.updater.util.e
import java.io.File

/**
 *
 * @author by chiclaim@google.com
 */
class UpgradeDialogActivity : AppCompatActivity(), DownloadListener {

    private var progressBar: ProgressBar? = null
    private var downloader: Downloader<*>? = null
    private var visiable = false

    companion object {

        private const val EXTRA_DIALOG_INFO = "EXTRA_DIALOG_INFO"
        private const val EXTRA_DOWNLOAD_MODE = "EXTRA_DOWNLOAD_MODE"


        @JvmStatic
        fun launch(
            context: Context,
            info: UpgradeDialogInfo,
            mode: DownloadMode = DownloadMode.EMBED
        ) {
            val intent = Intent(context, UpgradeDialogActivity::class.java)
            intent.putExtra(EXTRA_DIALOG_INFO, info)
            when (mode) {
                DownloadMode.EMBED -> intent.putExtra(EXTRA_DOWNLOAD_MODE, 1)
                DownloadMode.DOWNLOAD_MANAGER -> intent.putExtra(EXTRA_DOWNLOAD_MODE, 2)
            }
            if (context is Application) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrade_dialog_layout)

        val dialogInfo: UpgradeDialogInfo = intent.getParcelableExtra(EXTRA_DIALOG_INFO)
            ?: error("need $EXTRA_DIALOG_INFO parameter")

        val mode = when (intent.getIntExtra(EXTRA_DOWNLOAD_MODE, 1)) {
            2 -> DownloadMode.DOWNLOAD_MANAGER
            else -> DownloadMode.EMBED
        }


        progressBar = findViewById(R.id.pb_updater)

        findViewById<TextView>(R.id.tv_updater_title).text =
            dialogInfo.title ?: getString(R.string.updater_title)

        findViewById<TextView>(R.id.tv_updater_desc).text =
            dialogInfo.description ?: getString(R.string.updater_desc_default)

        findViewById<TextView>(R.id.tv_updater_cancel).text =
            dialogInfo.negativeText ?: getString(R.string.updater_cancel)

        findViewById<TextView>(R.id.tv_updater_confirm).text =
            dialogInfo.positiveText ?: getString(R.string.updater_ok)

        findViewById<View>(R.id.tv_updater_cancel).setOnClickListener {
            finish()
        }

        val appName = applicationInfo.loadLabel(packageManager)

        findViewById<View>(R.id.tv_updater_confirm).setOnClickListener {
            progressBar?.isIndeterminate = true
            progressBar?.visibility = View.VISIBLE
            val url = dialogInfo.url ?: return@setOnClickListener
            val request = DownloadRequest.newRequest(url, mode)
                .setNotificationSmallIcon(dialogInfo.notifierSmallIcon)
                .setIgnoreLocal(dialogInfo.ignoreLocal)
                .setNotificationTitle(appName)
                .setNotificationContent(getString(R.string.system_download_description))
                .setNeedInstall(true)
                .allowScanningByMediaScanner()
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_MOBILE
                            or DownloadManager.Request.NETWORK_WIFI
                )

            dialogInfo.destinationPath?.let {
                request.setDestinationUri(Uri.fromFile(File(it)))
            }
            downloader = request.buildDownloader(applicationContext)
                .registerListener(this)
            downloader?.startDownload()
        }

    }

    override fun onDownloadStart() {
    }

    override fun onProgressUpdate(
        percent: Int
    ) {
        progressBar?.isIndeterminate = percent == 0
        progressBar?.progress = percent
        if (BuildConfig.DEBUG) e("下载$percent%...")
    }

    override fun onDownloadComplete(uri: Uri) {
        finish()
        if (BuildConfig.DEBUG) e("下载完成...")

    }

    override fun onDownloadFailed(e: Throwable) {
        if (visiable) {
            val msg = getTipFromException(applicationContext, e)
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        visiable = hasFocus
    }

    override fun onDestroy() {
        super.onDestroy()
        downloader?.unregisterListener(this)
    }
}