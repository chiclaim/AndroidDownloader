package com.chiclaim.android.updater

import android.net.Uri

/**
 *
 * @author by chiclaim@google.com
 */
interface DownloadListener {


    fun onProgressUpdate(percent:Int)


    fun onComplete(uri: Uri?)

    fun onFailed(e: Throwable)


}