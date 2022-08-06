package com.chiclaim.android.downloader

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import com.chiclaim.android.downloader.util.DownloadEngine
import com.chiclaim.android.downloader.util.NotifierVisibility
import com.chiclaim.android.downloader.util.Utils
import com.chiclaim.android.downloader.util.e
import java.io.File

/**
 *
 * @author by chiclaim@google.com
 */
class DownloadRequest(
    val context: Context,
    val url: String,
    @DownloadEngine
    val engine: Int = DOWNLOAD_ENGINE_EMBED
) {
    /**
     * 文件下载的目标地址
     *
     * 如果下载引擎为 DownloadManager，那么[destinationUri]必须是外部存储路径，不能是当前应用的黑盒目录（因为是系统应用来下载）
     */
    var destinationUri: Uri? = null
        private set
        get() {
            return if (field == null) {
                Uri.fromFile(File(Utils.getDownloadDir(context), url.substringAfterLast("/")))
            } else {
                field
            }
        }

    /**
     * Whether to ignore the local file, if true, it will be downloaded again
     */
    var ignoreLocal = false
        private set

    /**
     * 本次下载是否为更新当前的APP，如果是，则会自动处理弹出安装界面
     */
    var needInstall = false
        private set

    var notificationVisibility = NOTIFIER_VISIBLE_NOTIFY_COMPLETED
        private set

    var notificationSmallIcon = -1
        private set
        get() {
            return if (field == -1) context.applicationInfo.icon else field
        }

    var notificationTitle: CharSequence? = null
        private set
        get() {
            return if (field == null) getDefaultTitle() else field
        }
    var notificationContent: CharSequence? = null
        private set

    /**
     * 当通知栏被禁用，是否提示
     */
    var showNotificationDisableTip = false
        private set

    internal var fromNotifier = false
        private set

    internal fun setFromNotifier(isFromNotifier: Boolean): DownloadRequest {
        this.fromNotifier = isFromNotifier
        return this
    }

    fun setDestinationUri(uri: Uri): DownloadRequest {
        this.destinationUri = uri
        return this
    }

    fun setShowNotificationDisableTip(show: Boolean): DownloadRequest {
        this.showNotificationDisableTip = show
        return this
    }

    fun setNotificationSmallIcon(@DrawableRes smallIcon: Int): DownloadRequest {
        this.notificationSmallIcon = smallIcon
        return this
    }

    fun setIgnoreLocal(ignore: Boolean): DownloadRequest {
        this.ignoreLocal = ignore
        return this
    }


    fun setNeedInstall(need: Boolean): DownloadRequest {
        this.needInstall = need
        return this
    }

    fun setNotificationTitle(title: CharSequence): DownloadRequest {
        this.notificationTitle = title
        return this
    }

    fun setNotificationContent(content: CharSequence): DownloadRequest {
        this.notificationContent = content
        return this
    }


    /**
     * 设置通知栏可见性，默认为 [NOTIFIER_VISIBLE_NOTIFY_COMPLETED]
     * @see [NOTIFIER_VISIBLE]
     * @see [NOTIFIER_HIDDEN]
     * @see [NOTIFIER_VISIBLE_NOTIFY_COMPLETED]
     * @see [NOTIFIER_VISIBLE_NOTIFY_ONLY_COMPLETION]
     */
    fun setNotificationVisibility(@NotifierVisibility visibility: Int): DownloadRequest {
        notificationVisibility = visibility
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DownloadRequest) return false

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    private fun getDefaultTitle(): String {
        return context.getString(
            R.string.downloader_notifier_title_placeholder,
            context.applicationInfo.loadLabel(context.packageManager)
        )
    }

    private var listeners: MutableSet<DownloadListener>? = null

    private fun initListenerList() {
        if (listeners == null) {
            synchronized(this) {
                if (listeners == null) {
                    listeners = mutableSetOf()
                }
            }
        }
    }

    /**
     * 注册下载监听。当下载完成或下载失败会自动移除监听
     */
    fun registerListener(listener: DownloadListener): DownloadRequest {
        initListenerList()
        listeners?.add(listener)
        return this
    }

    fun unregisterListener(listener: DownloadListener) {
        if (listeners == null) return
        listeners?.remove(listener)
    }

    internal fun onStart() {
        this.listeners?.forEach {
            it.onDownloadStart()
        }
    }

    internal fun onComplete(uri: Uri) {
        this.listeners?.forEach {
            it.onDownloadComplete(uri)
        }
        DownloaderManager.remove(this)
        listeners?.clear()
    }

    internal fun onFailed(e: Throwable) {
        this.listeners?.forEach {
            it.onDownloadFailed(e)
        }
        DownloaderManager.remove(this)
        listeners?.clear()
    }

    internal fun onProgressUpdate(percent: Int) {
        this.listeners?.forEach {
            it.onProgressUpdate(percent)
        }
    }

    private fun createDownloader(@DownloadEngine engine: Int): Downloader {
        return when (engine) {
            DOWNLOAD_ENGINE_SYSTEM_DM -> SystemDownloader(this)
            else -> EmbedDownloader(this)
        }
    }

    internal fun download() {
        val downloader = createDownloader(engine)
        downloader.download()
    }


    /**
     * 开始下载（运行在 Service 中）
     * 如果下载任务已经在运行，则返回 false，否则返回 true
     */
    fun startDownload(): Boolean {
        if (DownloaderManager.isRunning(this)) {
            if (BuildConfig.DEBUG) e("下载任务已经存在")
            return false
        }
        if (notificationVisibility != NOTIFIER_HIDDEN && showNotificationDisableTip) {
            Utils.checkNotificationsEnabled(context)
        }
        val downloader = createDownloader(engine)
        downloader.startDownload()
        return true
    }

}