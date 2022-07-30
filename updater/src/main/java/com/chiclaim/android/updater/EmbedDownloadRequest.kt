package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import com.chiclaim.android.updater.util.Utils.getDownloadDir
import java.io.File

/**
 *
 * @author by chiclaim@google.com
 */
class EmbedDownloadRequest(url: String) : Request(url) {

    lateinit var destinationUri: Uri
        private set

    override fun allowScanningByMediaScanner(): Request {
        return this
    }

    override fun setDestinationInExternalFilesDir(
        context: Context, dirType: String?, subPath: String?
    ): Request {
        return this
    }

    override fun setDestinationUri(uri: Uri): Request {
        this.destinationUri = uri
        return this
    }

    override fun buildDownloader(context: Context): Downloader<*> {
        if (notificationVisibility != NOTIFIER_HIDDEN && notificationSmallIcon == -1)
            throw IllegalArgumentException("must set notification small icon")

        if (!this::destinationUri.isInitialized) {
            val filename = url.substringAfterLast("/")
            destinationUri = Uri.fromFile(File(getDownloadDir(context), filename))
        }
        return EmbedDownloader(context, this)
    }
}