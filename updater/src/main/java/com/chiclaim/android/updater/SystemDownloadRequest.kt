package com.chiclaim.android.updater

import android.app.DownloadManager
import android.content.Context
import android.net.Uri


class SystemDownloadRequest(url: String) : Request(url) {

    private var rawRequest = DownloadManager.Request(Uri.parse(url))

    override fun setNotificationTitle(title: CharSequence): Request {
        rawRequest.setTitle(title)
        return this
    }

    override fun setNotificationDescription(description: CharSequence): Request {
        rawRequest.setDescription(description)
        return this
    }

    override fun allowScanningByMediaScanner(): Request {
        rawRequest.allowScanningByMediaScanner()
        return this
    }

    override fun setDestinationInExternalFilesDir(
        context: Context,
        dirType: String?,
        subPath: String?
    ): SystemDownloadRequest {
        rawRequest.setDestinationInExternalFilesDir(context, dirType, subPath)
        return this
    }

    override fun setDestinationDir(uri: Uri): Request {
        rawRequest.setDestinationUri(uri)
        return this
    }


    override fun setMimeType(mimeType: String): Request {
        rawRequest.setMimeType(mimeType)
        return this
    }

    override fun setNotificationVisibility(visibility: Int): Request {
        rawRequest.setNotificationVisibility(visibility)
        return this
    }

    override fun setAllowedNetworkTypes(flags: Int): Request {
        rawRequest.setAllowedNetworkTypes(flags)
        return this
    }

    override fun setAllowedOverRoaming(allowed: Boolean): Request {
        rawRequest.setAllowedOverRoaming(allowed)
        return this
    }

    override fun setAllowedOverMetered(allow: Boolean): Request {
        rawRequest.setAllowedOverMetered(allow)
        return this
    }

    fun getRequest(): DownloadManager.Request = rawRequest

    override fun buildDownloader(context: Context): Downloader =
        SystemDownloader(context.applicationContext, this)

}