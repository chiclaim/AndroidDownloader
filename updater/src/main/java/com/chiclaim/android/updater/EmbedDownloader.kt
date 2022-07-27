package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.chiclaim.android.updater.util.MD5
import com.chiclaim.android.updater.util.SpHelper
import com.chiclaim.android.updater.util.Utils
import com.chiclaim.android.updater.util.d
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
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

                val dir = File(request.destinationDir.path ?: return@execute)
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val ext: String = request.url.substringAfterLast(".", "").run {
                    if (length > 10) "" else ".${this}"
                }
                val fileName = MD5.md5(request.url)
                val destinationFile = File(dir, "$fileName$ext")
                d(destinationFile.absolutePath)

                var contentLength = Utils.getFileSize(context, request.url, 0)
                val currentLength = if (destinationFile.exists()) destinationFile.length() else 0
                if (currentLength > 0 && contentLength > 0) {
                    // continue last download
                    conn.setRequestProperty("Range", "bytes=$currentLength-")
                } else {
                    // first download
                    contentLength = conn.contentLength.toLong()
                    Utils.saveFileSize(context, request.url, contentLength)
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
                                            contentLength,
                                            wroteLength
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Utils.removeFileSize(context, request.url)
                    listener?.let {
                        handler.post {
                            listener.onComplete(Uri.fromFile(destinationFile))
                        }
                    }
                }

                when (conn.responseCode) {
                    // 200 正常下载
                    HttpURLConnection.HTTP_OK -> {
                        writeFile()
                    }
                    // 206 断点续传
                    HttpURLConnection.HTTP_PARTIAL -> {
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
                        throw IOException("Download Failed: ${conn.responseCode}}")
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