package com.chiclaim.android.downloader

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.chiclaim.android.downloader.util.Utils
import com.chiclaim.android.downloader.util.Utils.getRealPathFromURI
import com.chiclaim.android.downloader.util.checkDownloadComponentEnable
import com.chiclaim.android.downloader.util.showDownloadComponentSetting
import com.chiclaim.android.downloader.util.startInstall
import java.io.File

internal class SystemDownloader(context: Context, request: SystemDownloadRequest) :
    Downloader<SystemDownloadRequest>(context.applicationContext, request) {

    private var observer: DownloadObserver? = null

    private val downloader: SystemDownloadManager by lazy {
        SystemDownloadManager(this.context)
    }

    override fun startDownload() {
        super.onStart()
        if (!checkDownloadComponentEnable(context)) {
            Toast.makeText(
                context,
                R.string.downloader_component_disable,
                Toast.LENGTH_SHORT
            ).show()
            showDownloadComponentSetting(context)
            return
        }

        if (request.ignoreLocal) {
            download(request)
            return
        }

        val downloadId = Utils.getLocalDownloadId(context, request.url)
        if (downloadId != -1L) {
            val downloadInfo = downloader.getDownloadInfo(downloadId)
            if (downloadInfo == null) {
                download(request)
                return
            }
            //获取下载状态
            when (downloadInfo.status) {
                STATUS_SUCCESSFUL -> {
                    val uri = downloader.getDownloadedFileUri(downloadId)
                    if (uri != null) {
                        val path = getRealPathFromURI(context, uri)
                        path?.let {
                            val file = File(it)
                            if (file.exists() && file.length() == downloadInfo.totalSize) {
                                onComplete(uri)
                                if (request.needInstall) startInstall(context, file)
                                return
                            }
                        }
                    }
                    //重新下载
                    download(request)
                }
                STATUS_RUNNING -> {
                    registerListener(downloadId)
                }
                STATUS_FAILED, STATUS_UNKNOWN -> {
                    download(request)
                }
                else -> printDownloadStatus(downloadId, downloadInfo.status)
            }
        } else {
            download(request)
        }
    }

    private fun registerListener(downloadId: Long) {
        if (observer != null) {
            return
        }
        val ob = DownloadObserver(context.applicationContext, downloadId, this)
        observer = ob
        context.contentResolver.registerContentObserver(
            Uri.parse("content://downloads/my_downloads/$downloadId"),
            true,
            ob
        )
    }

    private fun download(request: SystemDownloadRequest) {
        val downloadId = downloader.download(request.getRequest())
        Utils.saveDownloadId(context, request.url, downloadId)
        registerListener(downloadId)
    }

}