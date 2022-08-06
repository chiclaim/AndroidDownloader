package com.chiclaim.android.downloader

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper

/**
 *
 * @author by chiclaim@google.com
 */
internal class DownloadService : Service(), DownloadListener {
    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_FROM = "from"
        const val FROM_NOTIFIER = 2
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val c = super.onStartCommand(intent, flags, startId)
        val url = intent?.getStringExtra(EXTRA_URL) ?: return c
        // 来自通知栏
        if (intent?.getIntExtra(EXTRA_FROM, -1) == FROM_NOTIFIER) {
            DownloadRequest(applicationContext, url)
                .registerListener(this)
                .setFromNotifier(true)
                .download()
        } else {
            val downloader: Downloader? =
                DownloaderManager.getDownloader(DownloadRequest(applicationContext, url))
            downloader?.request?.registerListener(this)
            downloader?.download()
        }
        return c
    }

    override fun onDownloadStart() {
    }

    override fun onProgressUpdate(percent: Int) {
    }

    override fun onDownloadComplete(uri: Uri) {
        tryStopService()
    }

    override fun onDownloadFailed(e: Throwable) {
        tryStopService()
    }

    private fun tryStopService() {
        if (DownloaderManager.runningCount() == 0) {
            stopSelf()
        }
    }
}