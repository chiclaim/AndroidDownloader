package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.chiclaim.android.updater.util.MD5
import com.chiclaim.android.updater.util.d
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

    private var context: Context


    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    init {
        this.context = context.applicationContext
    }


    override fun startDownload(listener: DownloadListener?) {
        DownloadExecutor.execute {
            var conn: HttpURLConnection? = null
            try {

                val urlHash = MD5.md5(request.url)
                val dir = File(request.destinationDir.path ?: return@execute)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val ext: String = request.url.substringAfterLast(".", "").run {
                    if (length > 10) "" else ".${this}"
                }
                val destinationFile = File(dir, "$urlHash$ext")
                d(destinationFile.absolutePath)


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

                d(record.toString())


                if (record.totalBytes > 0 && destinationFile.exists() && record.totalBytes == destinationFile.length()) {
                    handler.post {
                        listener?.onComplete(Uri.fromFile(destinationFile))
                    }
                    return@execute
                }

                val url = URL(request.url)
                conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "GET"
                conn.connectTimeout = 10 * 1000
                // Defeat transparent gzip compression, since it doesn't allow us to
                // easily resume partial downloads.
                conn.setRequestProperty("Accept-Encoding", "identity")
                // Only splice in user agent when not already defined
                if (conn.getRequestProperty("User-Agent") == null) {
                    conn.addRequestProperty("User-Agent", "AndroidDownloader/1.0")
                }

                val currentLength = if (destinationFile.exists()) destinationFile.length() else 0
                val resuming = currentLength > 0
                if (resuming) {
                    // continue last download
                    conn.setRequestProperty("Range", "bytes=$currentLength-")
                }

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
                                listener?.let {
                                    handler.post {
                                        listener.onProgressUpdate(
                                            STATUS_RUNNING,
                                            record.totalBytes,
                                            wroteLength
                                        )
                                    }
                                }
                            }
                        }
                    }
                    listener?.let {
                        handler.post {
                            listener.onComplete(Uri.fromFile(destinationFile))
                        }
                    }
                }

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
                    // 301,302,303 重定向
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_SEE_OTHER -> {
                        conn.disconnect()
                        // 获取真实下载地址
                        val locationUrl: String = conn.getHeaderField("Location") ?: return@execute
                    }
                    // error
                    else -> {
                        throw IOException("Download Failed: ${conn.responseCode}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listener?.let {
                    handler.post {
                        listener.onFailed(e)
                    }
                }
            } finally {
                conn?.disconnect()
            }
        }

    }
}