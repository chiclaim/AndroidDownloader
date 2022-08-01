package com.chiclaim.android.downloader.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.chiclaim.android.downloader.DownloadException
import com.chiclaim.android.downloader.R
import java.io.File
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


internal object Utils {

    fun getLocalDownloadId(context: Context, url: String): Long {
        return SpHelper.get(context).getLong("${MD5.md5(url)}-id", -1L)
    }

    fun saveDownloadId(context: Context, url: String, id: Long) {
        SpHelper.get(context).putLong("${MD5.md5(url)}-id", id)
    }

    fun saveFileSize(context: Context, url: String, size: Long) {
        SpHelper.get(context).putLong("${MD5.md5(url)}-size", size)
    }

    fun getFileSize(context: Context, url: String, defaultValue: Long): Long {
        return SpHelper.get(context).getLong("${MD5.md5(url)}-size", defaultValue)
    }

    fun removeFileSize(context: Context, url: String) {
        return SpHelper.get(context).remove("${MD5.md5(url)}-size")
    }

    inline fun getValueFromCursor(cursor: Cursor, column: String, block: (index: Int) -> Unit) {
        val index = cursor.getColumnIndex(column)
        if (index != -1) block(index)
    }

    inline fun <reified T> Cursor.getValue(column: String): T? {
        val index = getColumnIndex(column)
        if (index == -1) return null
        return getValueByType(this, index, T::class.java) as T?
    }

    private fun getValueByType(cursor: Cursor, index: Int, klass: Class<*>): Any? {
        return when (klass) {
            java.lang.String::class.java -> cursor.getString(index)
            java.lang.Long::class.java -> cursor.getLong(index)
            java.lang.Integer::class.java -> cursor.getInt(index)
            java.lang.Short::class.java -> cursor.getShort(index)
            java.lang.Float::class.java -> cursor.getFloat(index)
            java.lang.Double::class.java -> cursor.getDouble(index)
            ByteArray::class.java -> cursor.getBlob(index)
            else -> null
        }
    }

    internal fun getPercent(totalSize: Long, downloadedSize: Long) = if (totalSize <= 0) 0 else
        (downloadedSize / totalSize.toDouble() * 100).toInt()


    fun getDownloadDir(context: Context): File {
        val dir = context.externalCacheDir
        // a file named cache
        if (dir?.isDirectory == true) {
            return dir
        }
        return context.filesDir
    }

    fun checkNotificationsEnabled(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Toast.makeText(context, R.string.downloader_notification_disable, Toast.LENGTH_SHORT)
                .show()
        }
    }


    fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cursor = context.contentResolver.query(
                contentURI, null,
                null, null, null
            )
            cursor?.use {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (index != -1) return cursor.getString(index)
            }
        } else {
            return contentURI.path
        }
        return null
    }

     fun getTipFromException(context: Context, exception: Throwable): String {
        if (exception is DownloadException) {
            return when (exception.errorType) {
                DownloadException.ERROR_NO_NETWORK ->
                    context.getString(R.string.downloader_notifier_content_without_network)
                DownloadException.ERROR_CANNOT_RESUME ->
                    context.getString(R.string.downloader_notifier_content_partial_error)
                DownloadException.ERROR_TOO_MANY_REDIRECTS ->
                    context.getString(R.string.downloader_notifier_content_too_many_redirects)
                DownloadException.ERROR_MISSING_LOCATION_WHEN_REDIRECT ->
                    context.getString(R.string.downloader_notifier_content_missing_location)
                else ->
                    context.getString(
                        R.string.downloader_notifier_content_unhandled_err,
                        exception.responseCode
                    )
            }
        } else {
            return when (exception) {
                is SocketTimeoutException ->
                    context.getString(R.string.downloader_notifier_content_network_timeout)
                is SocketException -> context.getString(R.string.downloader_notifier_content_without_network)
                is ConnectException -> context.getString(R.string.downloader_notifier_content_without_network)
                is UnknownHostException -> context.getString(R.string.downloader_notifier_content_without_network)
                else -> context.getString(
                    R.string.downloader_notifier_content_err_placeholder,
                    exception.message ?: exception::class.java.name
                )
            }
        }
    }


}