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
import androidx.appcompat.app.AppCompatActivity
import java.io.File

/**
 *
 * @author by chiclaim@google.com
 */
class UpgradeDialogActivity : AppCompatActivity() {

    private var progressBar: ProgressBar? = null

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
        setContentView(R.layout.activity_dialog_layout)

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
            dialogInfo.description ?: ""

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
                .allowScanningByMediaScanner()
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_MOBILE
                            or DownloadManager.Request.NETWORK_WIFI
                )

            dialogInfo.destinationPath?.let {
                request.setDestinationUri(Uri.fromFile(File(it)))
            }
            request.buildDownloader(applicationContext)
                .startDownload(object : DownloadListener {
                    override fun onProgressUpdate(
                        percent: Int
                    ) {
                        progressBar?.isIndeterminate = percent == 0
                        progressBar?.progress = percent
                    }

                    override fun onComplete(uri: Uri?) {
                        finish()
                    }

                    override fun onFailed(e: Throwable) {
                    }
                })
        }

    }
}