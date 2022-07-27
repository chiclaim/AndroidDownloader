//package com.chiclaim.android.updater
//
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import com.chiclaim.android.updater.DownloadRecord.Companion.COLUMN_FILENAME
//import com.chiclaim.android.updater.DownloadRecord.Companion.COLUMN_HASH_URL
//import com.chiclaim.android.updater.DownloadRecord.Companion.COLUMN_ID
//import com.chiclaim.android.updater.DownloadRecord.Companion.COLUMN_STATUS
//import com.chiclaim.android.updater.DownloadRecord.Companion.COLUMN_TOTAL_BYTES
//import com.chiclaim.android.updater.DownloadRecord.Companion.COLUMN_URI
//
//internal class DBHelper(context: Context) : SQLiteOpenHelper(context, DOWNLOAD_DB_NAME, null, DB_VERSION) {
//
//    companion object {
//        const val DOWNLOAD_DB_NAME = "download_db"
//        private const val DB_VERSION = 1
//    }
//
//    override fun onCreate(db: SQLiteDatabase?) {
//        db?.execSQL(
//            """
//            CREATE TABLE t_download(
//                ${DownloadRecord.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
//                ${DownloadRecord.COLUMN_URI} TEXT,
//                ${DownloadRecord.COLUMN_FILENAME} TEXT,
//                ${DownloadRecord.COLUMN_HASH_URL} TEXT,
//                ${DownloadRecord.COLUMN_TOTAL_BYTES} INTEGER,
//                ${DownloadRecord.COLUMN_STATUS} INTEGER)
//        """.trimIndent()
//        )
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//    }
//
//}
