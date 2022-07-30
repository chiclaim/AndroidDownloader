package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.chiclaim.android.updater.util.Utils
import com.chiclaim.android.updater.util.checkDownloadComponentEnable
import com.chiclaim.android.updater.util.showDownloadComponentSetting
import com.chiclaim.android.updater.util.startInstall
import java.io.File

internal class SystemDownloader(context: Context, request: SystemDownloadRequest) :
    Downloader<SystemDownloadRequest>(context.applicationContext, request) {

    private val downloader: SystemDownloadManager by lazy {
        SystemDownloadManager(this.context)
    }

    override fun startDownload(listener: DownloadListener?) {
        if (!checkDownloadComponentEnable(context)) {
            Toast.makeText(
                context,
                R.string.system_download_component_disable,
                Toast.LENGTH_SHORT
            ).show()
            showDownloadComponentSetting(context)
            return
        }

        if (request.ignoreLocal) {
            download(request, listener)
            return
        }

        val downloadId = Utils.getLocalDownloadId(context, request.url)
        if (downloadId != -1L) {
            val downloadInfo = downloader.getDownloadInfo(downloadId)
            if (downloadInfo == null) {
                download(request, listener)
                return
            }
            //获取下载状态
            when (downloadInfo.status) {
                STATUS_SUCCESSFUL -> {
                    val uri = downloader.getDownloadedFileUri(downloadId)
                    uri?.path?.let {
                        val file = File(it)
                        if (file.exists() && file.length() == downloadInfo.totalSize) {
                            listener?.onComplete(uri)
                            if (request.needInstall) startInstall(context, uri)
                            return
                        }
                    }
                    //重新下载
                    download(request, listener)
                }
                STATUS_FAILED, STATUS_UNKNOWN -> {
                    download(request, listener)
                }
                else -> printDownloadStatus(downloadId, downloadInfo.status)
            }
        } else {
            download(request, listener)
        }
    }

    private fun download(request: SystemDownloadRequest, listener: DownloadListener?) {
        val downloadId = downloader.download(request.getRequest())
        Utils.saveDownloadId(context, request.url, downloadId)
        context.contentResolver.registerContentObserver(
            Uri.parse("content://downloads/my_downloads/$downloadId"),
            true,
            DownloadObserver(context.applicationContext, downloadId, listener)
        )
    }

}