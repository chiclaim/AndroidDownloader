package com.chiclaim.android.downloader

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 *
 * @author by chiclaim@google.com
 */
internal object DBManager {

    const val DOWNLOAD_DB_NAME = "download_db"
    const val DB_VERSION = 1

    // share single database object
    private lateinit var dbHelper: com.chiclaim.android.downloader.DBManager.DBHelper

    internal class DBHelper(context: Context) :
        SQLiteOpenHelper(context,
            com.chiclaim.android.downloader.DBManager.DOWNLOAD_DB_NAME, null,
            com.chiclaim.android.downloader.DBManager.DB_VERSION
        ) {

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(
                """
            CREATE TABLE ${com.chiclaim.android.downloader.DownloadRecord.Companion.TABLE_NAME}(
                ${com.chiclaim.android.downloader.DownloadRecord.Companion.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, 
                ${com.chiclaim.android.downloader.DownloadRecord.Companion.COLUMN_URI} TEXT, 
                ${com.chiclaim.android.downloader.DownloadRecord.Companion.COLUMN_FILENAME} TEXT, 
                ${com.chiclaim.android.downloader.DownloadRecord.Companion.COLUMN_HASH_URL} TEXT, 
                ${com.chiclaim.android.downloader.DownloadRecord.Companion.COLUMN_TOTAL_BYTES} INTEGER, 
                ${com.chiclaim.android.downloader.DownloadRecord.Companion.COLUMN_STATUS} INTEGER)
        """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        }

    }


    /**
     * return single database object
     */
    @Synchronized
    fun getDB(context: Context): com.chiclaim.android.downloader.DBManager.DBHelper {
        if (!this::dbHelper.isInitialized) {
            com.chiclaim.android.downloader.DBManager.dbHelper =
                com.chiclaim.android.downloader.DBManager.DBHelper(context)
        }
        return com.chiclaim.android.downloader.DBManager.dbHelper
    }


    /**
     * close database when you no longer need the database
     */
    fun close() {
        if (this::dbHelper.isInitialized) {
            com.chiclaim.android.downloader.DBManager.dbHelper.close()
        }
    }


}