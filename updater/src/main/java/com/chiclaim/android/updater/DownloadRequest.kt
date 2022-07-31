package com.chiclaim.android.updater


class DownloadRequest private constructor() {

    companion object {
        @JvmStatic
        fun newRequest(url: String, mode: DownloadMode = DownloadMode.EMBED): Request =
            when (mode) {
                DownloadMode.EMBED -> EmbedDownloadRequest(url)
                DownloadMode.DOWNLOAD_MANAGER -> SystemDownloadRequest(url)
            }
    }
}