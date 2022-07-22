package com.chiclaim.android.updater

import android.app.DownloadManager
import android.content.Context
import android.net.Uri


class DownloadRequest(val fileUrl: String) {

    internal var rawRequest = DownloadManager.Request(Uri.parse(fileUrl))

    /**
     * 本次下载是否为更新当前的APP，如果是，则会自动处理弹出安装界面
     */
    var installDownloadApk = false

    fun setTitle(title: CharSequence): DownloadRequest {
        rawRequest.setTitle(title)
        return this
    }

    fun setDescription(description: CharSequence): DownloadRequest {
        rawRequest.setDescription(description)
        return this
    }

    fun allowScanningByMediaScanner(): DownloadRequest {
        rawRequest.allowScanningByMediaScanner()
        return this
    }

    fun setDestinationInExternalFilesDir(
        context: Context,
        dirType: String?,
        subPath: String?
    ): DownloadRequest {
        rawRequest.setDestinationInExternalFilesDir(context, dirType, subPath)
        return this
    }

    fun setDestinationDir(uri: Uri): DownloadRequest {
        rawRequest.setDestinationUri(uri)
        return this
    }


    fun setMimeType(mimeType: String): DownloadRequest {
        rawRequest.setMimeType(mimeType)
        return this
    }

    fun setNotificationVisibility(visibility: Int): DownloadRequest {
        rawRequest.setNotificationVisibility(visibility)
        return this
    }

    fun setAllowedNetworkTypes(flags: Int): DownloadRequest {
        rawRequest.setAllowedNetworkTypes(flags)
        return this
    }

    fun setAllowedOverRoaming(allowed: Boolean): DownloadRequest {
        rawRequest.setAllowedOverRoaming(allowed)
        return this
    }

    fun setAllowedOverMetered(allow: Boolean): DownloadRequest {
        rawRequest.setAllowedOverMetered(allow)
        return this
    }

}