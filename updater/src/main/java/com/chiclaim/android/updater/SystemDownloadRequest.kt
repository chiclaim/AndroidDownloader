package com.chiclaim.android.updater

import android.app.DownloadManager
import android.content.Context
import android.net.Uri


class SystemDownloadRequest(val url: String) : Request {

    private var rawRequest = DownloadManager.Request(Uri.parse(url))

    /**
     * 本次下载是否为更新当前的APP，如果是，则会自动处理弹出安装界面
     */
    var installDownloadApk = false

    override fun setNotificationTitle(title: CharSequence): SystemDownloadRequest {
        rawRequest.setTitle(title)
        return this
    }

    override fun setNotificationDescription(description: CharSequence): SystemDownloadRequest {
        rawRequest.setDescription(description)
        return this
    }

    override fun allowScanningByMediaScanner(): SystemDownloadRequest {
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

    override fun setDestinationDir(uri: Uri): SystemDownloadRequest {
        rawRequest.setDestinationUri(uri)
        return this
    }


    override fun setMimeType(mimeType: String): SystemDownloadRequest {
        rawRequest.setMimeType(mimeType)
        return this
    }

    override fun setNotificationVisibility(visibility: Int): SystemDownloadRequest {
        rawRequest.setNotificationVisibility(visibility)
        return this
    }

    override fun setAllowedNetworkTypes(flags: Int): SystemDownloadRequest {
        rawRequest.setAllowedNetworkTypes(flags)
        return this
    }

    override fun setAllowedOverRoaming(allowed: Boolean): SystemDownloadRequest {
        rawRequest.setAllowedOverRoaming(allowed)
        return this
    }

    override fun setAllowedOverMetered(allow: Boolean): SystemDownloadRequest {
        rawRequest.setAllowedOverMetered(allow)
        return this
    }

    fun getRequest():DownloadManager.Request = rawRequest

    override fun buildDownloader(context: Context): Downloader =
        SystemDownloader(context.applicationContext, this)

}