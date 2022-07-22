package com.chiclaim.android.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chiclaim.android.updater.util.UpdaterUtils

/**
 *
 * @author by chiclaim@google.com
 */
class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        require(intent != null && context != null)
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
            val downloadApkId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadApkId != -1L) {
                installApk(context, downloadApkId)
            }
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED == intent.action) {
            //如果还未完成下载，用户点击 Notification
            val viewDownloadIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            viewDownloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(viewDownloadIntent)
        }
    }

    private fun installApk(context: Context, downloadApkId: Long) {
        val dManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: return
        val downloadFileUri = dManager.getUriForDownloadedFile(downloadApkId)
        if (downloadFileUri != null) {
            UpdaterUtils.startInstall(context, downloadFileUri)
        }
    }

}