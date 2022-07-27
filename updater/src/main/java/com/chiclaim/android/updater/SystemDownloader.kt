package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.chiclaim.android.updater.util.Utils

internal class SystemDownloader(context: Context, private val request: SystemDownloadRequest) : Downloader {

    private var context: Context
    private val downloader: SystemDownloadManager

    init {
        this.context = context.applicationContext
        downloader = SystemDownloadManager(this.context)
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
        val downloadId = Utils.getLocalDownloadId(context, request.url)
        if (downloadId != -1L) {
            //获取下载状态
            when (val status = downloader.getDownloadStatus(downloadId)) {
                STATUS_SUCCESSFUL -> {
                    /*val uri = downloader.getDownloadedFileUri(downloadId)
                    if (uri != null) {
                        listener?.onComplete(uri)
                        //本地的版本大于当前程序的版本直接安装
                        if (request.installDownloadApk && UpdaterUtils.compare(context, uri)) {
                            UpdaterUtils.startInstall(context, uri)
                        }
                        return
                    }*/
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