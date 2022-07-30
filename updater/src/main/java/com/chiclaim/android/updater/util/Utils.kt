package com.chiclaim.android.updater.util

import android.content.Context
import android.database.Cursor
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.chiclaim.android.updater.R
import java.io.File


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
            Toast.makeText(context, R.string.updater_notification_disable, Toast.LENGTH_SHORT)
                .show()
        }
    }
}