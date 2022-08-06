package com.chiclaim.android.downloader.util

import androidx.annotation.IntDef
import com.chiclaim.android.downloader.*

/**
 *
 * @author by chiclaim@google.com
 */
@IntDef(
    DOWNLOAD_ENGINE_EMBED,
    DOWNLOAD_ENGINE_SYSTEM_DM
)
@Retention(AnnotationRetention.SOURCE)
annotation class DownloadEngine
