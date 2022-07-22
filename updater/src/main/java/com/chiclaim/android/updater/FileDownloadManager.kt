package com.chiclaim.android.updater

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.chiclaim.android.updater.util.SpHelper

/**
 * 文件下载管理
 * @author created by chiclaim@gmail.com
 */
internal class FileDownloadManager(context: Context) {


    private val context: Context

    init {
        this.context = context.applicationContext
    }

    private val downloadManager: DownloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }


    fun download(request: DownloadRequest): Long {
        return downloadManager.enqueue(request.rawRequest)
    }


    /**
     * 获取文件保存的路径
     *
     * @param downloadId an ID for the download, unique across the system.
     * This ID is used to make future calls related to this download.
     * @return file path
     * @see FileDownloadManager.getDownloadedFileUri
     */
    private fun getDownloadPath(downloadId: Long): String? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val c: Cursor? = downloadManager.query(query)
        c?.use {
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            }
        }
        return null
    }

    /**
     * 获取保存文件的地址
     *
     * @param downloadId an ID for the download, unique across the system.
     * This ID is used to make future calls related to this download.
     * @see FileDownloadManager.getDownloadPath
     */
    fun getDownloadedFileUri(downloadId: Long): Uri? {
        return downloadManager.getUriForDownloadedFile(downloadId)
    }


    /**
     * 获取下载状态
     *
     * @param downloadId an ID for the download, unique across the system.
     * This ID is used to make future calls related to this download.
     * @return int
     * @see STATUS_PENDING
     *
     * @see STATUS_PAUSED
     *
     * @see STATUS_RUNNING
     *
     * @see STATUS_SUCCESSFUL
     *
     * @see STATUS_UNKNOWN
     */
    fun getDownloadStatus(downloadId: Long): Int {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val c: Cursor? = downloadManager.query(query)
        c?.use {
            if (it.moveToFirst()) {
                return c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))
            }
        }
        return STATUS_UNKNOWN
    }

    fun removeTask(downloadId: Long): Int {
        return downloadManager.remove(downloadId)
    }


    fun getDownloadLocalUri(downloadId: Long): String? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val c: Cursor? = downloadManager.query(query)
        c?.use {
            if (c.moveToFirst()) {
                val index = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                if (index != -1) return c.getString(index)
            }
        }
        return null
    }

    fun getDownloadInfo(downloadId: Long): DownloadInfo? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val c: Cursor? = downloadManager.query(query)
        c?.use {
            if (c.moveToFirst()) {
                val info = DownloadInfo(downloadId)

                var index = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                if (index != -1) info.downloadedSize = c.getLong(index)

                index = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                if (index != -1) info.totalSize = c.getLong(index)

                index = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (index != -1) info.status = c.getInt(index)

                index = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                if (index != -1) {
                    c.getString(index)?.let {
                        info.uri = Uri.parse(it)
                    }
                }
                return info
            }
        }
        return null
    }

    fun clearLocalDownloadIds(context: Context) {
        SpHelper.get(context).clear()
    }


}