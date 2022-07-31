package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri

abstract class Downloader<T : Request>(val context: Context, internal val request: T) {

    private var listeners: MutableList<DownloadListener>? = null

    internal fun onStart() {
        DownloaderManager.addDownload(this)
        this.listeners?.forEach {
            it.onDownloadStart()
        }
    }

    internal fun onComplete(uri: Uri) {
        DownloaderManager.removeDownload(request)
        this.listeners?.forEach {
            it.onDownloadComplete(uri)
        }
    }

    internal fun onFailed(e: Throwable) {
        DownloaderManager.removeDownload(request)
        this.listeners?.forEach {
            it.onDownloadFailed(e)
        }
    }

    internal fun onProgressUpdate(percent: Int) {
        this.listeners?.forEach {
            it.onProgressUpdate(percent)
        }
    }


    open fun startDownload() {
        onStart()
    }


    private fun initListenerList() {
        if (listeners == null) {
            synchronized(this) {
                if (listeners == null) {
                    listeners = mutableListOf()
                }
            }
        }
    }

    fun registerListener(listener: DownloadListener): Downloader<*> {
        initListenerList()
        listeners?.add(listener)
        return this
    }

    fun unregisterListener(listener: DownloadListener) {
        if (listeners == null) return
        listeners?.remove(listener)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Downloader<*>) return false

        if (request.url != other.request.url) return false

        return true
    }

    override fun hashCode(): Int {
        return request.url.hashCode()
    }
}