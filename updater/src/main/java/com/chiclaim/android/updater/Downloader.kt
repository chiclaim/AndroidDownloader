package com.chiclaim.android.updater

interface Downloader {
    fun startDownload(listener: DownloadListener?)
}