package com.chiclaim.android.downloader

import android.app.DownloadManager
import android.net.Uri
import android.widget.Toast
import com.chiclaim.android.downloader.util.Utils
import com.chiclaim.android.downloader.util.Utils.getRealPathFromURI
import com.chiclaim.android.downloader.util.checkDownloadComponentEnable
import com.chiclaim.android.downloader.util.showDownloadComponentSetting
import com.chiclaim.android.downloader.util.startInstall
import java.io.File

internal class SystemDownloader(request: DownloadRequest) : Downloader(request) {

    private var observer: DownloadObserver? = null

    private val downloader: SystemDownloadManager by lazy {
        SystemDownloadManager(request.context)
    }

    override fun startDownload() {
        if (!checkDownloadComponentEnable(request.context)) {
            Toast.makeText(
                request.context,
                R.string.downloader_component_disable,
                Toast.LENGTH_SHORT
            ).show()
            showDownloadComponentSetting(request.context)
            return
        }

        if (request.ignoreLocal) {
            download()
            return
        }

        val downloadId = Utils.getLocalDownloadId(request.context, request.url)
        if (downloadId != -1L) {
            val downloadInfo = downloader.getDownloadInfo(downloadId)
            if (downloadInfo == null) {
                download()
                return
            }
            //获取下载状态
            when (downloadInfo.status) {
                STATUS_SUCCESSFUL -> {
                    val uri = downloader.getDownloadedFileUri(downloadId)
                    if (uri != null) {
                        val path = getRealPathFromURI(request.context, uri)
                        path?.let {
                            val file = File(it)
                            if (file.exists() && file.length() == downloadInfo.totalSize) {
                                request.onComplete(uri)
                                if (request.needInstall) startInstall(request.context, file)
                                return
                            }
                        }
                    }
                    //重新下载
                    download()
                }
                STATUS_RUNNING -> {
                    registerListener(downloadId)
                }
                STATUS_FAILED, STATUS_UNKNOWN -> {
                    download()
                }
                else -> printDownloadStatus(downloadId, downloadInfo.status)
            }
        } else {
            download()
        }
    }

    private fun registerListener(downloadId: Long) {
        if (observer != null) {
            return
        }
        val ob = DownloadObserver(request.context.applicationContext, downloadId, this)
        observer = ob
        request.context.contentResolver.registerContentObserver(
            Uri.parse("content://downloads/my_downloads/$downloadId"),
            true,
            ob
        )
    }

    override fun download() {
        super.download()
        val dr = DownloadManager.Request(Uri.parse(request.url))
            .setTitle(request.notificationTitle)
            .setDescription(request.notificationContent)
            .setNotificationVisibility(request.notificationVisibility)
        //.setAllowedNetworkTypes(request.allowedNetworkTypes)
        val downloadId = downloader.download(dr)
        Utils.saveDownloadId(request.context, request.url, downloadId)
        registerListener(downloadId)
    }

}