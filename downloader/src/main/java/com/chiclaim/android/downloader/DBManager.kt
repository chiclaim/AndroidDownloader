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
    private lateinit var dbHelper: DBHelper

    internal class DBHelper(context: Context) :
        SQLiteOpenHelper(
            context,
            DOWNLOAD_DB_NAME, null,
            DB_VERSION
        ) {

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(
                """
            CREATE TABLE ${DownloadRecord.TABLE_NAME}(
                ${DownloadRecord.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, 
                ${DownloadRecord.COLUMN_URI} TEXT, 
                ${DownloadRecord.COLUMN_FILENAME} TEXT, 
                ${DownloadRecord.COLUMN_HASH_URL} TEXT, 
                ${DownloadRecord.COLUMN_TOTAL_BYTES} INTEGER, 
                ${DownloadRecord.COLUMN_STATUS} INTEGER)
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
    fun getDB(context: Context): DBHelper {
        if (!this::dbHelper.isInitialized) {
            dbHelper = DBHelper(context)
        }
        return dbHelper
    }


    /**
     * close database when you no longer need the database
     */
    fun close() {
        if (this::dbHelper.isInitialized) {
            dbHelper.close()
        }
    }


}