package com.chiclaim.android.updater.util

import androidx.annotation.IntDef
import com.chiclaim.android.updater.*

/**
 *
 * @author by chiclaim@google.com
 */
@IntDef(
    STATUS_PENDING,
    STATUS_PAUSED,
    STATUS_RUNNING,
    STATUS_SUCCESSFUL,
    STATUS_FAILED,
    STATUS_UNKNOWN
)
@Retention(AnnotationRetention.SOURCE)
annotation class DownloadStatus
