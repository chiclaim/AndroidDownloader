package com.chiclaim.android.updater

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException

/**
 *
 * @author by chiclaim@google.com
 */
class DownloadRecord {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_URI = "uri"
        const val COLUMN_FILENAME = "fileName"
        const val COLUMN_HASH_URL = "hashUrl"
        const val COLUMN_TOTAL_BYTES = "totalBytes"
        const val COLUMN_STATUS = "status"
    }

    var id = 0L
    var uri: String? = null
    var fileName: String? = null
    var hashUrl: String? = null
    var totalBytes = 0L
    var status = 0


    fun queryByUrlHash(context: Context) {
        val db = DBManager.getDB(context).readableDatabase
        db.query(
            DBManager.DOWNLOAD_DB_NAME,
            arrayOf(
                COLUMN_ID,
                COLUMN_URI,
                COLUMN_FILENAME,
                COLUMN_HASH_URL,
                COLUMN_TOTAL_BYTES,
                COLUMN_STATUS
            ),
            " $COLUMN_HASH_URL=? ",
            arrayOf(hashUrl), null, null, null
        )?.use {
            while (it.moveToNext()) {
                var index = it.getColumnIndex(COLUMN_ID)
                if (index != -1) {
                    it.getLong(index)
                }
            }
        }

    }


    fun add(context: Context): Long {
        return try {
            val values = ContentValues().apply {
                put(COLUMN_URI, uri)
                put(COLUMN_FILENAME, fileName)
                put(COLUMN_HASH_URL, hashUrl)
                put(COLUMN_TOTAL_BYTES, totalBytes)
                put(COLUMN_STATUS, status)
            }
            val db = DBManager.getDB(context).writableDatabase
            db.insert(DBManager.DOWNLOAD_DB_NAME, null, values)
        } catch (e: SQLiteException) {
            e.printStackTrace()
            -1
        }
    }

    fun update(context: Context): Int {
        return try {
            val values = ContentValues().apply {
                put(COLUMN_URI, uri)
                put(COLUMN_FILENAME, fileName)
                put(COLUMN_HASH_URL, hashUrl)
                put(COLUMN_TOTAL_BYTES, totalBytes)
                put(COLUMN_STATUS, status)
            }
            val db = DBManager.getDB(context).writableDatabase
            db.update(DBManager.DOWNLOAD_DB_NAME, values, " $COLUMN_ID=? ", arrayOf(id.toString()))
        } catch (e: SQLiteException) {
            e.printStackTrace()
            -1
        }
    }

    fun delete(context: Context): Int {
        return try {
            val db = DBManager.getDB(context).writableDatabase
            db.delete(DBManager.DOWNLOAD_DB_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        } catch (e: SQLiteException) {
            e.printStackTrace()
            -1
        }
    }


}