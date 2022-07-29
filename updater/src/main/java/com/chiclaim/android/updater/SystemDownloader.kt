package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.chiclaim.android.updater.util.Utils
import java.io.File

internal class SystemDownloader(context: Context, request: SystemDownloadRequest) :
    Downloader<SystemDownloadRequest>(context.applicationContext, request) {

    private val downloader: SystemDownloadManager by lazy {
        SystemDownloadManager(this.context)
    }

    override fun startDownload(listener: DownloadListener?) {
        if (!Utils.checkDownloadState(context)) {
            Toast.makeText(
                context,
                R.string.system_download_component_disable,
                Toast.LENGTH_SHORT
            ).show()
            Utils.showDownloadSetting(context)
            return
        }

        if (request.ignoreLocal) {
            download(request, listener)
            return
        }

        val downloadId = Utils.getLocalDownloadId(context, request.url)
        if (downloadId != -1L) {
            //获取下载状态
            when (val status = downloader.getDownloadStatus(downloadId)) {
                STATUS_SUCCESSFUL -> {
                    val uri = downloader.getDownloadedFileUri(downloadId)
                    if (uri != null) {
                        val path = uri.path
                        // 判断文件是否被删除
                        if (path != null && File(path).exists()) {
                            listener?.onComplete(uri)
                            //本地的版本大于当前程序的版本直接安装
                            if (request.needInstall && Utils.compare(context, uri)) {
                                Utils.startInstall(context, uri)
                            }
                            return
                        }
                    }
                    //重新下载
                    download(request, listener)
                }
                STATUS_FAILED, STATUS_UNKNOWN -> {
                    download(request, listener)
                }
                else -> printDownloadStatus(downloadId, status)
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