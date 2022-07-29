package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.chiclaim.android.updater.DownloadException.Companion.ERROR_CANNOT_RESUME
import com.chiclaim.android.updater.DownloadException.Companion.ERROR_MISSING_LOCATION_WHEN_REDIRECT
import com.chiclaim.android.updater.DownloadException.Companion.ERROR_TOO_MANY_REDIRECTS
import com.chiclaim.android.updater.DownloadException.Companion.ERROR_UNHANDLED
import com.chiclaim.android.updater.util.MD5
import com.chiclaim.android.updater.util.NotifierUtils
import com.chiclaim.android.updater.util.Utils.getPercent
import com.chiclaim.android.updater.util.Utils.startInstall
import java.io.File
import java.io.FileOutputStream
import java.net.*

/**
 *
 * @author by chiclaim@google.com
 */
class EmbedDownloader(context: Context, request: EmbedDownloadRequest) :
    Downloader<EmbedDownloadRequest>(context.applicationContext, request) {

    companion object {
        private const val HTTP_TEMP_REDIRECT = 307
        private const val HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416
        private const val MAX_REDIRECTS = 5  // can't be more than 7.
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    // 避免下载期间，创建大量的通知对象
    private var lastNotifyTime = 0L

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

        val resuming = !request.ignoreLocal && currentLength > 0
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
        return !request.ignoreLocal && record.totalBytes > 0 && record.totalBytes == currentLength
    }

    private fun getDefaultTitle(): String {
        return context.getString(
            R.string.updater_notifier_title_placeholder,
            context.applicationInfo.loadLabel(context.packageManager)
        )
    }

    private fun postPercent(percent: Int, listener: DownloadListener?) {
        handler.post {
            listener?.onProgressUpdate(percent)
            if (request.notificationVisibility == NOTIFIER_VISIBLE_NOTIFY_COMPLETED) {
                NotifierUtils.showNotification(
                    context,
                    request.url.hashCode(),
                    request.notificationSmallIcon,
                    percent,
                    request.notificationTitle ?: getDefaultTitle(),
                    request.notificationContent,
                    STATUS_RUNNING,
                )
            }
        }

    }

    private fun postSuccessful(destinationFile: File, listener: DownloadListener?) {
        handler.post {
            if (request.notificationVisibility == NOTIFIER_VISIBLE_NOTIFY_COMPLETED
                || request.notificationVisibility == NOTIFIER_VISIBLE_NOTIFY_ONLY_COMPLETION
            ) {
                NotifierUtils.showNotification(
                    context,
                    request.url.hashCode(),
                    request.notificationSmallIcon,
                    100,
                    request.notificationTitle ?: getDefaultTitle(),
                    context.getString(R.string.updater_notifier_success_to_install),
                    STATUS_SUCCESSFUL
                )
            }
            listener?.onComplete(Uri.fromFile(destinationFile))
        }
    }

    private fun postFailed(e: Exception, listener: DownloadListener?) {
        handler.post {
            if (request.notificationVisibility != NOTIFIER_HIDDEN
            ) {
                NotifierUtils.showNotification(
                    context,
                    request.url.hashCode(),
                    request.notificationSmallIcon,
                    -1,
                    request.notificationTitle ?: getDefaultTitle(),
                    getContentFromException(e),
                    STATUS_FAILED
                )
            }
            listener?.onFailed(e)
        }
    }

    private fun getContentFromException(exception: Exception): String {
        if (exception is DownloadException) {
            return when (exception.errorType) {
                ERROR_CANNOT_RESUME ->
                    context.getString(R.string.updater_notifier_content_partial_error)
                ERROR_TOO_MANY_REDIRECTS ->
                    context.getString(R.string.updater_notifier_content_too_many_redirects)
                ERROR_MISSING_LOCATION_WHEN_REDIRECT ->
                    context.getString(R.string.updater_notifier_content_missing_location)
                else ->
                    context.getString(
                        R.string.updater_notifier_content_unhandled_err,
                        exception.responseCode
                    )
            }
        } else {
            return when (exception) {
                is SocketTimeoutException ->
                    context.getString(R.string.updater_notifier_content_network_timeout)
                is SocketException -> context.getString(R.string.updater_notifier_content_without_network)
                is ConnectException -> context.getString(R.string.updater_notifier_content_without_network)
                is UnknownHostException -> context.getString(R.string.updater_notifier_content_without_network)
                else -> context.getString(R.string.updater_notifier_content_unknown_err)
            }
        }
    }


    override fun startDownload(listener: DownloadListener?) {
        DownloadExecutor.execute {
            var url: URL? = null
            var conn: HttpURLConnection? = null
            var redirectCount = 0
            while (true) {
                try {
                    if (redirectCount >= MAX_REDIRECTS) {
                        throw DownloadException(ERROR_TOO_MANY_REDIRECTS, "too many redirect times")
                    }
                    val record = prepareRecord()
                    val destinationFile = prepareDestinationFile(record.hashUrl!!)

                    val currentLength =
                        if (destinationFile.exists()) destinationFile.length() else 0
                    if (checkComplete(record, currentLength)) {
                        postSuccessful(destinationFile, listener)
                        return@execute
                    }

                    conn = prepareConnection(url, currentLength)

                    // local function to reuse code
                    fun writeFile(append: Boolean = false) {
                        var wroteLength: Long = if (append) currentLength else 0
                        conn.inputStream.use { input ->
                            FileOutputStream(destinationFile, append).use { fos ->
                                val data = ByteArray(8 * 1024)
                                var count: Int
                                while (input.read(data).also { len -> count = len } != -1) {
                                    fos.write(data, 0, count)
                                    wroteLength += count
                                    fos.flush()
                                    val now = SystemClock.elapsedRealtime()
                                    val notifyInterval = now - lastNotifyTime
                                    if (notifyInterval > 700 || record.totalBytes == wroteLength) {
                                        val percent = getPercent(record.totalBytes, wroteLength)
                                        postPercent(percent, listener)
                                        lastNotifyTime = now
                                    }
                                }
                            }
                        }
                        postSuccessful(destinationFile, listener)
                    }

                    val resuming = conn.getRequestProperty("Range")?.isNotEmpty() ?: false

                    when (conn.responseCode) {
                        // 200 正常下载
                        HttpURLConnection.HTTP_OK -> {
                            if (resuming) {
                                throw DownloadException(
                                    ERROR_CANNOT_RESUME,
                                    "Expected partial, but received OK"
                                )
                            }
                            record.totalBytes = conn.contentLength.toLong()
                            record.update(context)
                            writeFile()
                        }
                        // 206 断点续传
                        HttpURLConnection.HTTP_PARTIAL -> {
                            if (!resuming) {
                                throw DownloadException(
                                    ERROR_CANNOT_RESUME,
                                    "Expected OK, but received partial"
                                )
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
                                    ?: throw DownloadException(
                                        ERROR_MISSING_LOCATION_WHEN_REDIRECT,
                                        "missing Location in redirect"
                                    )
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
                            throw DownloadException(
                                ERROR_UNHANDLED,
                                "Download Failed: ${conn.responseCode}",
                                conn.responseCode
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    postFailed(e, listener)
                    break // break while on exception
                } finally {
                    conn?.disconnect()
                }
                break // break while on normal
            }
        }
    }
}