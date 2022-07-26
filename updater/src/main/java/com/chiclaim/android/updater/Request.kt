package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri

/**
 *
 * @author by chiclaim@google.com
 */
abstract class Request(val url: String) {

    abstract fun setNotificationTitle(title: CharSequence): Request

    abstract fun setNotificationDescription(description: CharSequence): Request

    open fun allowScanningByMediaScanner(): Request = this

    open fun setDestinationInExternalFilesDir(
        context: Context,
        dirType: String?,
        subPath: String?
    ): Request = this

    open fun setDestinationDir(uri: Uri): Request = this


    open fun setMimeType(mimeType: String): Request = this

    open fun setNotificationVisibility(visibility: Int): Request = this

    open fun setAllowedNetworkTypes(flags: Int): Request = this

    open fun setAllowedOverRoaming(allowed: Boolean): Request = this

    open fun setAllowedOverMetered(allow: Boolean): Request = this


    abstract fun buildDownloader(context: Context): Downloader
}