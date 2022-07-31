package com.chiclaim.android.updater

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import com.chiclaim.android.updater.util.NotifierVisibility

/**
 *
 * @author by chiclaim@google.com
 */
abstract class Request(val url: String) {

    var destinationUri: Uri? = null
        protected set

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

    var notificationTitle: CharSequence? = null
        private set
    var notificationContent: CharSequence? = null
        private set

    /**
     * 当通知栏被禁用，是否提示
     */
    var showNotificationDisableTip = false
        private set

    open fun setDestinationUri(uri: Uri): Request {
        this.destinationUri = uri
        return this
    }

    fun setShowNotificationDisableTip(show: Boolean): Request {
        this.showNotificationDisableTip = show
        return this
    }

    fun setNotificationSmallIcon(@DrawableRes smallIcon: Int): Request {
        this.notificationSmallIcon = smallIcon
        return this
    }

    fun setIgnoreLocal(ignore: Boolean): Request {
        this.ignoreLocal = ignore
        return this
    }


    fun setNeedInstall(need: Boolean): Request {
        this.needInstall = need
        return this
    }

    open fun setNotificationTitle(title: CharSequence): Request {
        this.notificationTitle = title
        return this
    }

    open fun setNotificationContent(content: CharSequence): Request {
        this.notificationContent = content
        return this
    }

    open fun allowScanningByMediaScanner(): Request = this

    open fun setDestinationInExternalFilesDir(
        context: Context,
        dirType: String?,
        subPath: String?
    ): Request = this


    open fun setMimeType(mimeType: String): Request = this

    /**
     * 设置通知栏可见性，默认为 [NOTIFIER_VISIBLE_NOTIFY_COMPLETED]
     * @see [NOTIFIER_VISIBLE]
     * @see [NOTIFIER_HIDDEN]
     * @see [NOTIFIER_VISIBLE_NOTIFY_COMPLETED]
     * @see [NOTIFIER_VISIBLE_NOTIFY_ONLY_COMPLETION]
     */
    open fun setNotificationVisibility(@NotifierVisibility visibility: Int): Request {
        notificationVisibility = visibility
        return this
    }

    open fun setAllowedNetworkTypes(flags: Int): Request = this

    open fun setAllowedOverRoaming(allowed: Boolean): Request = this

    open fun setAllowedOverMetered(allow: Boolean): Request = this


    abstract fun buildDownloader(context: Context): Downloader<*>
}