package com.chiclaim.android.updater

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.chiclaim.android.updater.util.Utils.getValue
import com.chiclaim.android.updater.util.d

/**
 * 'download' table record
 * @author by chiclaim@google.com
 */
class DownloadRecord(
    var id: Long = 0L,
    private var uri: String? = null,
    private var fileName: String? = null,
    var hashUrl: String? = null,
    var totalBytes: Long = 0L,
    private var status: Int = 0
) {

    companion object {
        const val TABLE_NAME = "t_download"
        const val COLUMN_ID = "id"
        const val COLUMN_URI = "uri"
        const val COLUMN_FILENAME = "fileName"
        const val COLUMN_HASH_URL = "hashUrl"
        const val COLUMN_TOTAL_BYTES = "totalBytes"
        const val COLUMN_STATUS = "status"
    }


    private fun createFromCursor(cursor: Cursor): DownloadRecord {
        val record = DownloadRecord()
        cursor.getValue<Long>(COLUMN_ID)?.let {
            record.id = it
        }
        cursor.getValue<String>(COLUMN_URI)?.let {
            record.uri = it
        }
        cursor.getValue<String>(COLUMN_FILENAME)?.let {
            record.fileName = it
        }
        cursor.getValue<String>(COLUMN_HASH_URL)?.let {
            record.hashUrl = it
        }
        cursor.getValue<Long>(COLUMN_TOTAL_BYTES)?.let {
            record.totalBytes = it
        }
        cursor.getValue<Int>(COLUMN_STATUS)?.let {
            record.status = it
        }
        return record
    }

    fun queryByUrlHash(context: Context): List<DownloadRecord> {
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
                    COLUMN_URI,
                    COLUMN_FILENAME,
                    COLUMN_HASH_URL,
                    COLUMN_TOTAL_BYTES,
                    COLUMN_STATUS
                ),
                " $COLUMN_HASH_URL=? ",
                arrayOf(hashUrl), null, null, null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    add(createFromCursor(cursor))
                }
            }
        }
    }


    /**
     * return row number
     */
    fun insert(context: Context): Long {
        val rowId = try {
            val values = ContentValues().apply {
                put(COLUMN_URI, uri)
                put(COLUMN_FILENAME, fileName)
                put(COLUMN_HASH_URL, hashUrl)
                put(COLUMN_TOTAL_BYTES, totalBytes)
                put(COLUMN_STATUS, status)
            }
            val db = DBManager.getDB(context).writableDatabase
            db.insert(TABLE_NAME, null, values)
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
            val values = ContentValues().apply {
                put(COLUMN_URI, uri)
                put(COLUMN_FILENAME, fileName)
                put(COLUMN_HASH_URL, hashUrl)
                put(COLUMN_TOTAL_BYTES, totalBytes)
                put(COLUMN_STATUS, status)
            }
            val db = DBManager.getDB(context).writableDatabase
            db.update(
                TABLE_NAME,
                values,
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
        return try {
            val db = DBManager.getDB(context).writableDatabase
            db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        } catch (e: SQLiteException) {
            e.printStackTrace()
            -1
        }
    }

    override fun toString(): String {
        return "DownloadRecord(id=$id, uri=$uri, fileName=$fileName, hashUrl=$hashUrl, totalBytes=$totalBytes, status=$status)"
    }


}