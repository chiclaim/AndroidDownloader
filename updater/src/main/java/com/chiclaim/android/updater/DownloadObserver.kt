package com.chiclaim.android.updater

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import com.chiclaim.android.updater.util.Utils.getPercent


/**
 *
 * @author by chiclaim@google.com
 */
internal class DownloadObserver(
    context: Context,
    private val downloadId: Long,
    private var listener: DownloadListener?
) : ContentObserver(null) {


    private val context: Context

    init {
        this.context = context.applicationContext
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }


    override fun onChange(selfChange: Boolean) {

        super.onChange(selfChange)
        DownloadExecutor.execute {
            val info = SystemDownloadManager(context).getDownloadInfo(downloadId)
            info?.let {
                handler.post {
                    listener?.onProgressUpdate(getPercent(info.totalSize, info.downloadedSize))
                    if (info.hasComplete()) {
                        // 下载完毕后，移除观察者
                        context.contentResolver.unregisterContentObserver(this)
                        listener?.onComplete(info.uri)
                    }
                }
            }
        }
    }


}