package com.chiclaim.android.downloader

import java.util.concurrent.ConcurrentHashMap

internal object DownloaderManager {


    private val downloadingList = ConcurrentHashMap<Request, Downloader<*>>()

    fun addDownload(downloader: Downloader<*>) {
        downloadingList[downloader.request] = downloader
    }

    fun removeDownload(vararg requests: Request) {
        requests.forEach {
            downloadingList.remove(it)
        }
    }

    fun runningCount() = downloadingList.size

    fun isRunning(request: Request) = downloadingList.containsKey(request)

}