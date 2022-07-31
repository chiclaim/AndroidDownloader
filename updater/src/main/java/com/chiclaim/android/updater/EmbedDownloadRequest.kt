package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import com.chiclaim.android.updater.util.Utils.checkNotificationsEnabled
import com.chiclaim.android.updater.util.Utils.getDownloadDir
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
        if (notificationVisibility != NOTIFIER_HIDDEN) {
            if (showNotificationDisableTip)
                checkNotificationsEnabled(context)
            if (notificationSmallIcon == -1)
                throw IllegalArgumentException("must set notification small icon")
        }

        if (destinationUri == null) {
            val filename = url.substringAfterLast("/")
            setDestinationUri(Uri.fromFile(File(getDownloadDir(context), filename)))
        }
        return EmbedDownloader(context, this)
    }
}