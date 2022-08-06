package com.chiclaim.android.downloader

import java.util.concurrent.ConcurrentHashMap

internal object DownloaderManager {


    private val downloadingList = ConcurrentHashMap<DownloadRequest, Downloader>()

    fun addIfAbsent(downloader: Downloader) {
        downloadingList[downloader.request] = downloader
    }

    fun remove(vararg requests: DownloadRequest) {
        requests.forEach {
            downloadingList.remove(it)
        }
    }

    fun runningCount() = downloadingList.size

    fun isRunning(request: DownloadRequest) = downloadingList.containsKey(request)

    fun getDownloader(request: DownloadRequest): Downloader? = downloadingList[request]

}