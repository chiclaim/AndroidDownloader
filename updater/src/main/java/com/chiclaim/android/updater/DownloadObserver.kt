package com.chiclaim.android.updater

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import java.util.concurrent.*


/**
 *
 * @author by chiclaim@google.com
 */
class DownloadObserver(
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


    companion object {
        private val execute = ThreadPoolExecutor(
            1,
            1,
            2,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(100),
            { r -> Thread(r, "DownloadProgressTask") },
            ThreadPoolExecutor.DiscardOldestPolicy()
        )

        init {
            execute.allowCoreThreadTimeOut(true)
        }
    }


    override fun onChange(selfChange: Boolean) {

        super.onChange(selfChange)
        execute.execute {
            val info = FileDownloadManager(context).getDownloadInfo(downloadId)
            info?.let {
                handler.post {
                    listener?.onProgressUpdate(info.status, info.totalSize, info.downloadedSize)
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