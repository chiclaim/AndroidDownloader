package com.chiclaim.android.updater


class DownloadRequest private constructor() {

    companion object {
        @JvmStatic
        fun newRequest(url: String, mode: DownloadMode = DownloadMode.DOWNLOAD_MANAGER): Request =
            when (mode) {
                DownloadMode.EMBED -> SystemDownloadRequest(url)
                DownloadMode.DOWNLOAD_MANAGER -> SystemDownloadRequest(url)
            }
    }
}