package com.chiclaim.android.downloader

enum class DownloadMode {
    /**
     * 使用内置的 HttpUrlConnection 下载
     */
    EMBED,

    /**
     * 优先使用系统的 DownloadManager 组件下载，如果该组件不可用，则使用 [EMBED] 模式
     */
    DOWNLOAD_MANAGER
}