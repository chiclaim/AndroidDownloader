package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import com.chiclaim.android.updater.util.Utils.getDownloadPath

/**
 *
 * @author by chiclaim@google.com
 */
class EmbedDownloadRequest(url: String) : Request(url) {

    lateinit var destinationDir: Uri
        private set

    override fun setNotificationTitle(title: CharSequence): Request {
        return this
    }

    override fun setNotificationDescription(description: CharSequence): Request {
        return this
    }

    override fun allowScanningByMediaScanner(): Request {
        return this
    }

    override fun setDestinationInExternalFilesDir(
        context: Context, dirType: String?, subPath: String?
    ): Request {
        return this
    }

    override fun setDestinationDir(uri: Uri): Request {
        this.destinationDir = uri
        return this
    }

    override fun buildDownloader(context: Context): Downloader<*> {
        if (!this::destinationDir.isInitialized) {
            destinationDir = Uri.fromFile(getDownloadPath(context))
        }
        return EmbedDownloader(context, this)
    }
}