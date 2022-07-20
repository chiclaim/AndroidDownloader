package com.chiclam.android.updater

import android.app.DownloadManager
import android.content.Context
import android.widget.Toast
import com.chiclam.android.updater.util.UpdaterUtils
import com.chiclam.android.updater.util.d

class Downloader(context: Context) {

    private var context: Context

    init {
        this.context = context.applicationContext
    }

    fun start(request: DownloadRequest) {
        if (!UpdaterUtils.checkDownloadState(context)) {
            Toast.makeText(
                context,
                R.string.system_download_component_disable,
                Toast.LENGTH_SHORT
            ).show()
            UpdaterUtils.showDownloadSetting(context)
            return
        }
        val downloadId = UpdaterUtils.getLocalDownloadId(context)
        d("local download id is $downloadId")
        if (downloadId != -1L) {
            val fdm = FileDownloadManager.get()
            //获取下载状态
            when (val status = fdm.getDownloadStatus(context, downloadId)) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    d("downloadId=$downloadId ,status = STATUS_SUCCESSFUL")
                    val uri = fdm.getDownloadUri(context, downloadId)
                    if (uri != null) {
                        //本地的版本大于当前程序的版本直接安装
                        if (UpdaterUtils.compare(context, uri)) {
                            d("start install UI with local apk")
                            UpdaterUtils.startInstall(context, uri)
                            return
                        } else {
                            //从FileDownloadManager中移除这个任务
                            fdm.getDM(context).remove(downloadId)
                        }
                    }
                    //重新下载
                    download(request)
                }
                DownloadManager.STATUS_FAILED -> {
                    d("download failed $downloadId")
                    download(request)
                }
                DownloadManager.STATUS_RUNNING -> d("downloadId=$downloadId ,status = STATUS_RUNNING")
                DownloadManager.STATUS_PENDING -> d("downloadId=$downloadId ,status = STATUS_PENDING")
                DownloadManager.STATUS_PAUSED -> d("downloadId=$downloadId ,status = STATUS_PAUSED")
                STATUS_NOT_FOUND -> {
                    d("downloadId=$downloadId ,status = STATUS_NOT_FOUND")
                    download(request)
                }
                else -> d("downloadId=$downloadId ,status = $status")
            }
        } else {
            download(request)
        }
    }

    private fun download(request: DownloadRequest) {
        val dm =
            context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request.rawRequest)
        UpdaterUtils.saveDownloadId(context, downloadId)
        d("file download start, downloadId is $downloadId")
    }

    companion object {
        /**
         * FileDownloadManager.getDownloadStatus 如果没找到会返回-1
         */
        private const val STATUS_NOT_FOUND = -1

    }
}