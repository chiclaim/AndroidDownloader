package com.chiclaim.android.downloader

import android.content.Context
import android.net.Uri
import com.chiclaim.android.downloader.util.Utils.checkNotificationsEnabled
import com.chiclaim.android.downloader.util.Utils.getDownloadDir
import com.chiclaim.android.downloader.util.e
import java.io.File

/**
 *
 * @author by chiclaim@google.com
 */
class EmbedDownloadRequest(url: String) : Request(url) {

    override fun allowScanningByMediaScanner(): Request {
        return this
    }

    override fun setDestinationInExternalFilesDir(
        context: Context, dirType: String?, subPath: String?
    ): Request {
        return this
    }

    override fun buildDownloader(context: Context): Downloader<*> {
        if (DownloaderManager.isRunning(this)) {
            if (BuildConfig.DEBUG) e("下载任务已经存在")
            return EmptyDownloader(context, this)
        }
        if (notificationVisibility != NOTIFIER_HIDDEN) {
            if (showNotificationDisableTip)
                checkNotificationsEnabled(context)
            if (notificationSmallIcon == -1 || notificationSmallIcon == 0)
                setNotificationSmallIcon(context.applicationInfo.icon)
        }

        if (destinationUri == null) {
            val filename = url.substringAfterLast("/")
            setDestinationUri(Uri.fromFile(File(getDownloadDir(context), filename)))
        }
        return EmbedDownloader(context, this)
    }
}