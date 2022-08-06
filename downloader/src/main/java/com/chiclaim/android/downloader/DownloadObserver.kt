package com.chiclaim.android.downloader

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import com.chiclaim.android.downloader.DownloadException.Companion.ERROR_DM_FAILED
import com.chiclaim.android.downloader.DownloadException.Companion.ERROR_MISSING_URI
import com.chiclaim.android.downloader.util.Utils.getPercent


/**
 *
 * @author by chiclaim@google.com
 */
internal class DownloadObserver(
    context: Context,
    private val downloadId: Long,
    private var downloader: Downloader
) : ContentObserver(null) {


    private val context: Context = context.applicationContext

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }


    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        DownloadExecutor.execute {
            val info = SystemDownloadManager(context).getDownloadInfo(downloadId) ?: return@execute
            handler.post {
                downloader.request.onProgressUpdate(getPercent(info.totalSize, info.downloadedSize))
                when (info.status) {
                    STATUS_SUCCESSFUL -> {
                        context.contentResolver.unregisterContentObserver(this)
                        val uri = info.uri
                        if (uri == null) {
                            downloader.request.onFailed(
                                DownloadException(
                                    ERROR_MISSING_URI,
                                    context.getString(R.string.downloader_notifier_failed_missing_uri)
                                )
                            )
                        } else {
                            downloader.request.onComplete(uri)
                        }
                    }
                    STATUS_FAILED -> {
                        context.contentResolver.unregisterContentObserver(this)
                        downloader.request.onFailed(
                            DownloadException(
                                ERROR_DM_FAILED,
                                context.getString(
                                    R.string.downloader_notifier_content_err_placeholder,
                                    info.reason ?: "-"
                                )
                            )
                        )
                    }
                }
            }
        }

    }
}