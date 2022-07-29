package com.chiclaim.android.updater

import android.content.Context

abstract class Downloader<T : Request>(val context: Context, val request: T) {
    abstract fun startDownload(listener: DownloadListener?)
}