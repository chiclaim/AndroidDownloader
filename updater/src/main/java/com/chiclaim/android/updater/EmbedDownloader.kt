package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.chiclaim.android.updater.util.MD5
import com.chiclaim.android.updater.util.Utils.postFailed
import com.chiclaim.android.updater.util.Utils.postProgress
import com.chiclaim.android.updater.util.Utils.postSuccess
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * @author by chiclaim@google.com
 */
class EmbedDownloader(context: Context, private val request: EmbedDownloadRequest) : Downloader {

    companion object {
        private const val HTTP_TEMP_REDIRECT = 307
        private const val HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416
        private const val MAX_REDIRECTS = 5  // can't be more than 7.
    }

    private var context: Context


    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    init {
        this.context = context.applicationContext
    }

    private fun prepareConnection(uri: URL?, currentLength: Long): HttpURLConnection {

        val url = uri ?: URL(request.url)
        val conn = url.openConnection() as HttpURLConnection

        conn.requestMethod = "GET"
        conn.connectTimeout = 10 * 1000
        // Defeat transparent gzip compression, since it doesn't allow us to
        // easily resume partial downloads.
        conn.setRequestProperty("Accept-Encoding", "identity")
        // Only splice in user agent when not already defined
        if (conn.getRequestProperty("User-Agent") == null) {
            conn.addRequestProperty("User-Agent", "AndroidDownloader/1.0")
        }

        val resuming = !request.isIgnoreLocal() && currentLength > 0
        if (resuming) {
            // continue last download
            conn.setRequestProperty("Range", "bytes=$currentLength-")
        }
        return conn
    }

    private fun prepareDestinationFile(urlHash: String): File {
        val dir = File(
            request.destinationDir.path
                ?: throw NullPointerException("request must set destinationDir")
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val ext: String = request.url.substringAfterLast(".", "").run {
            if (length > 10) "" else ".${this}"
        }
        return File(dir, "$urlHash$ext")
    }

    private fun prepareRecord(): DownloadRecord {
        val urlHash = MD5.md5(request.url)
        var record = DownloadRecord(hashUrl = urlHash)
        record = record.queryByUrlHash(context).firstOrNull().run {
            if (this == null) {
                val rowId = record.insert(context)
                if (rowId != -1L) {
                    record.queryByUrlHash(context).firstOrNull() ?: record
                } else {
                    record
                }
            } else {
                this
            }
        }
        return record
    }

    private fun checkComplete(record: DownloadRecord, currentLength: Long): Boolean {
        return !request.isIgnoreLocal() && record.totalBytes > 0 && record.totalBytes == currentLength
    }


    override fun startDownload(listener: DownloadListener?) {
        DownloadExecutor.execute {
            var url: URL? = null
            var conn: HttpURLConnection? = null
            var redirectCount = 0
            while (true) {
                try {
                    if (redirectCount >= MAX_REDIRECTS) {
                        throw IllegalStateException("too many redirect times")
                    }
                    val record = prepareRecord()
                    val destinationFile = prepareDestinationFile(record.hashUrl!!)

                    val currentLength =
                        if (destinationFile.exists()) destinationFile.length() else 0
                    if (checkComplete(record, currentLength)) {
                        listener?.postSuccess(handler, destinationFile)
                        return@execute
                    }

                    conn = prepareConnection(url, currentLength)

                    // local function to reuse code
                    fun writeFile(append: Boolean = false) {
                        var wroteLength = if (append) currentLength else 0
                        conn.inputStream.use { input ->
                            FileOutputStream(destinationFile, append).use { fos ->
                                val data = ByteArray(8 * 1024)
                                var count: Int
                                while (input.read(data).also { len -> count = len } != -1) {
                                    fos.write(data, 0, count)
                                    wroteLength += count
                                    listener?.postProgress(
                                        handler, STATUS_RUNNING, record.totalBytes,
                                        wroteLength
                                    )
                                }
                            }
                        }
                        listener?.postSuccess(handler, destinationFile)
                    }

                    val resuming = conn.getRequestProperty("Range")?.isNotEmpty() ?: false

                    when (conn.responseCode) {
                        // 200 正常下载
                        HttpURLConnection.HTTP_OK -> {
                            if (resuming) {
                                throw IllegalStateException("Expected partial, but received OK")
                            }
                            record.totalBytes = conn.contentLength.toLong()
                            record.update(context)
                            writeFile()
                        }
                        // 206 断点续传
                        HttpURLConnection.HTTP_PARTIAL -> {
                            if (!resuming) {
                                throw IllegalStateException("Expected OK, but received partial")
                            }
                            writeFile(true)
                        }
                        // 301,302,303,307 重定向
                        HttpURLConnection.HTTP_MOVED_PERM,
                        HttpURLConnection.HTTP_MOVED_TEMP,
                        HttpURLConnection.HTTP_SEE_OTHER,
                        HTTP_TEMP_REDIRECT -> {
                            conn.disconnect()
                            val locationUrl: String =
                                conn.getHeaderField("Location")
                                    ?: throw IllegalStateException("missing Location in redirect")
                            // 获取最新的地址后，重新下载
                            url = URL(url, locationUrl)
                            redirectCount++
                            continue
                        }
                        // 416 服务器无法处理所请求的数据区间
                        HTTP_REQUESTED_RANGE_NOT_SATISFIABLE -> {
                            conn.disconnect()
                            record.delete(context)
                            request.setIgnoreLocal(true)
                            continue
                        }
                        // unmatched case
                        else -> {
                            throw IOException("Download Failed: ${conn.responseCode}")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    listener?.postFailed(handler, e)
                } finally {
                    conn?.disconnect()
                }
                break
            }
        }
    }
}