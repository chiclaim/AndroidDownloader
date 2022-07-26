package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.chiclaim.android.updater.util.MD5
import com.chiclaim.android.updater.util.d
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

/**
 *
 * @author by chiclaim@google.com
 */
class EmbedDownloader(context: Context, private val request: EmbedDownloadRequest) : Downloader {

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun startDownload(listener: DownloadListener?) {
        DownloadExecutor.execute {
            try {
                val url = URL(request.url)
                val conn = url.openConnection() as HttpURLConnection

                conn.requestMethod = "GET"
                conn.connectTimeout = 10 * 1000
                conn.setRequestProperty("Accept-Encoding", "identity")
                conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)"
                )
                val dir = File(request.destinationDir.path ?: return@execute)
                if (!dir.exists()) {
                    dir.mkdirs()
                }

                val ext: String = request.url.substringAfterLast(".", "").run {
                    if (length > 10) "" else ".${this}"
                }


                val destinationFile = File(dir, "${MD5.md5(request.url)}$ext")

                d(destinationFile.absolutePath)

                val contentLength = conn.contentLength.toLong()
                val currentLength = if (destinationFile.exists()) destinationFile.length() else 0
                if (contentLength > 0 && currentLength > 0 && contentLength > currentLength) {
                    conn.setRequestProperty("Range", "bytes=$currentLength-${contentLength}")
                }

                when (conn.responseCode) {
                    // 200 正常下载
                    HttpURLConnection.HTTP_OK -> {
                        var wroteLength = 0L
                        conn.inputStream.use { input ->
                            FileOutputStream(destinationFile).use { fos ->
                                val data = ByteArray(8 * 1024)
                                var count: Int
                                while (input.read(data).also { len -> count = len } != -1) {
                                    fos.write(data, 0, count)
                                    wroteLength += count
                                    listener?.let {
                                        handler.post {
                                            listener.onProgressUpdate(
                                                -1,
                                                contentLength,
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
                    // 断点续传
                    HttpURLConnection.HTTP_PARTIAL -> {

                    }
                    // 301,302 重定向
                    HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP -> {
                        conn.disconnect()
                        // 获取真实下载地址
                        val locationUrl = conn.getHeaderField("Location")
                    }
                    // error
                    else -> {

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}