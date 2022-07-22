@file:JvmName("DownloadStatus.kt")

package com.chiclaim.android.updater

import android.app.DownloadManager
import com.chiclaim.android.updater.util.d

const val STATUS_PENDING = DownloadManager.STATUS_PENDING
const val STATUS_PAUSED = DownloadManager.STATUS_PAUSED
const val STATUS_RUNNING = DownloadManager.STATUS_RUNNING
const val STATUS_SUCCESSFUL = DownloadManager.STATUS_SUCCESSFUL
const val STATUS_FAILED = DownloadManager.STATUS_FAILED
const val STATUS_UNKNOWN = -1


fun printDownloadStatus(downloadId: Long, status: Int) {
    when (status) {
        STATUS_PENDING -> d("downloadId=$downloadId, status=STATUS_PENDING")
        STATUS_PAUSED -> d("downloadId=$downloadId, status=STATUS_PAUSED")
        STATUS_RUNNING -> d("downloadId=$downloadId, status=STATUS_RUNNING")
        STATUS_SUCCESSFUL -> d("downloadId=$downloadId, status=STATUS_SUCCESSFUL")
        STATUS_FAILED -> d("downloadId=$downloadId, status=STATUS_FAILED")
        STATUS_UNKNOWN -> d("downloadId=$downloadId, status=STATUS_UNKNOWN")
    }
}