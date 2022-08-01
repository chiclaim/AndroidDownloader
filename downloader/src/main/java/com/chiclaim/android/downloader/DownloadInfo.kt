package com.chiclaim.android.downloader

import android.net.Uri

/**
 *
 * @author by chiclaim@google.com
 */
class DownloadInfo(var downloadId: Long = 0L) {


    var totalSize = 0L
    var downloadedSize = 0L
    var status = STATUS_UNKNOWN
    var reason: String? = null
    var uri: Uri? = null

}