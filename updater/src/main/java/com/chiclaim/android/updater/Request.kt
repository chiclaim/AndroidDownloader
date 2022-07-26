package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri

/**
 *
 * @author by chiclaim@google.com
 */
interface Request {


    fun setNotificationTitle(title: CharSequence): Request

    fun setNotificationDescription(description: CharSequence): Request

    fun allowScanningByMediaScanner(): Request

    fun setDestinationInExternalFilesDir(
        context: Context,
        dirType: String?,
        subPath: String?
    ): Request

    fun setDestinationDir(uri: Uri): Request


    fun setMimeType(mimeType: String): Request

    fun setNotificationVisibility(visibility: Int): Request

    fun setAllowedNetworkTypes(flags: Int): Request

    fun setAllowedOverRoaming(allowed: Boolean): Request

    fun setAllowedOverMetered(allow: Boolean): Request


    fun buildDownloader(context: Context): Downloader
}