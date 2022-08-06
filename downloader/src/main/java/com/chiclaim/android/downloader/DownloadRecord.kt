package com.chiclaim.android.downloader

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.chiclaim.android.downloader.util.Utils.getValue
import com.chiclaim.android.downloader.util.d

/**
 * 'download' table record
 * @author by chiclaim@google.com
 */
class DownloadRecord(
    var id: Long = 0L,
    var url: String? = null,
    var fileName: String? = null,
    var destinationUri: String? = null,
    var ignoreLocal: Boolean = false,
    var needInstall: Boolean = false,
    var notificationVisibility: Int = NOTIFIER_VISIBLE_NOTIFY_COMPLETED,
    var notificationTitle: String? = null,
    var notificationContent: String? = null,
    var totalBytes: Long = 0L,
    var status: Int = STATUS_UNKNOWN
) {

    companion object {
        const val TABLE_NAME = "t_download"
        const val COLUMN_ID = "id"
        const val COLUMN_URL = "url"
        const val COLUMN_FILENAME = "fileName"
        const val COLUMN_DESTINATION_URI = "dest_uri"
        const val COLUMN_IGNORE_LOCAL = "ignore_local"
        const val COLUMN_NEED_INSTALL = "need_install"
        const val COLUMN_NOTIFICATION_VISIBILITY = "notifier_visibility"
        const val COLUMN_NOTIFICATION_TITLE = "notifier_title"
        const val COLUMN_NOTIFICATION_CONTENT = "notifier_content"
        const val COLUMN_TOTAL_BYTES = "totalBytes"
        const val COLUMN_STATUS = "status"
    }


    private fun createFromCursor(cursor: Cursor): DownloadRecord {
        val record = DownloadRecord()
        cursor.getValue<Long>(COLUMN_ID)?.let {
            record.id = it
        }
        cursor.getValue<String>(COLUMN_URL)?.let {
            record.url = it
        }
        cursor.getValue<String>(COLUMN_FILENAME)?.let {
            record.fileName = it
        }
        cursor.getValue<String>(COLUMN_DESTINATION_URI)?.let {
            record.destinationUri = it
        }
        cursor.getValue<Boolean>(COLUMN_IGNORE_LOCAL)?.let {
            record.ignoreLocal = it
        }
        cursor.getValue<Boolean>(COLUMN_NEED_INSTALL)?.let {
            record.needInstall = it
        }
        cursor.getValue<Int>(COLUMN_NOTIFICATION_VISIBILITY)?.let {
            record.notificationVisibility = it
        }
        cursor.getValue<String>(COLUMN_NOTIFICATION_TITLE)?.let {
            record.notificationTitle = it
        }
        cursor.getValue<String>(COLUMN_NOTIFICATION_CONTENT)?.let {
            record.notificationContent = it
        }
        cursor.getValue<Long>(COLUMN_TOTAL_BYTES)?.let {
            record.totalBytes = it
        }
        cursor.getValue<Int>(COLUMN_STATUS)?.let {
            record.status = it
        }
        return record
    }

    fun queryByUrl(context: Context): List<DownloadRecord> {
        return buildList {
            val db: SQLiteDatabase
            try {
                db = DBManager.getDB(context).readableDatabase
            } catch (e: SQLiteException) {
                e.printStackTrace()
                return this
            }
            db.query(
                TABLE_NAME,
                arrayOf(
                    COLUMN_ID,
                    COLUMN_URL,
                    COLUMN_FILENAME,
                    COLUMN_DESTINATION_URI,
                    COLUMN_IGNORE_LOCAL,
                    COLUMN_NEED_INSTALL,
                    COLUMN_NOTIFICATION_VISIBILITY,
                    COLUMN_NOTIFICATION_TITLE,
                    COLUMN_NOTIFICATION_CONTENT,
                    COLUMN_TOTAL_BYTES,
                    COLUMN_STATUS
                ),
                " $COLUMN_URL=? ",
                arrayOf(url), null, null, null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    add(createFromCursor(cursor))
                }
            }
        }
    }


    private fun toContentValues(): ContentValues {
        return ContentValues().apply {
            put(COLUMN_URL, url)
            put(COLUMN_FILENAME, fileName)
            put(COLUMN_DESTINATION_URI, destinationUri)
            put(COLUMN_IGNORE_LOCAL, if (ignoreLocal) 1 else 0)
            put(COLUMN_NEED_INSTALL, if (needInstall) 1 else 0)
            put(COLUMN_NOTIFICATION_VISIBILITY, notificationVisibility)
            put(COLUMN_NOTIFICATION_TITLE, notificationTitle)
            put(COLUMN_NOTIFICATION_CONTENT, notificationContent)
            put(COLUMN_TOTAL_BYTES, totalBytes)
            put(COLUMN_STATUS, status)
        }
    }

    /**
     * return row number
     */
    fun insert(context: Context): Long {
        val rowId = try {
            val db = DBManager.getDB(context).writableDatabase
            db.insert(TABLE_NAME, null, toContentValues())
        } catch (e: SQLiteException) {
            e.printStackTrace()
            -1
        }
        if (rowId > 0) {
            d("record insert success $this")
        } else {
            d("record insert failed $rowId $this")
        }
        return rowId
    }

    /**
     * return the number of rows affected
     */
    fun update(context: Context): Int {
        val result = try {
            val db = DBManager.getDB(context).writableDatabase
            db.update(
                TABLE_NAME,
                toContentValues(),
                " $COLUMN_ID=? ",
                arrayOf(id.toString())
            )
        } catch (e: SQLiteException) {
            e.printStackTrace()
            -1
        }
        if (result > 0) {
            d("record update success $this")
        } else {
            d("record update failed $result $this")
        }
        return result
    }

    /**
     * return the number of rows affected
     */
    fun delete(context: Context): Int {
        val rows = try {
            val db = DBManager.getDB(context).writableDatabase
            db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        } catch (e: SQLiteException) {
            e.printStackTrace()
            -1
        }
        if (rows > 0) {
            d("record delete success $this")
        } else {
            d("record delete failed $this")
        }
        return rows
    }

    override fun toString(): String {
        return "DownloadRecord(id=$id, url=$url, fileName=$fileName, destinationUri=$destinationUri, ignoreLocal=$ignoreLocal, needInstall=$needInstall, notificationVisibility=$notificationVisibility, notificationTitle=$notificationTitle, notificationContent=$notificationContent, totalBytes=$totalBytes, status=$status)"
    }


}