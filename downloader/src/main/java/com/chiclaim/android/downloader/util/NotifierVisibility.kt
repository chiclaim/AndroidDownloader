package com.chiclaim.android.downloader.util

import androidx.annotation.IntDef
import com.chiclaim.android.downloader.NOTIFIER_HIDDEN
import com.chiclaim.android.downloader.NOTIFIER_VISIBLE
import com.chiclaim.android.downloader.NOTIFIER_VISIBLE_NOTIFY_COMPLETED
import com.chiclaim.android.downloader.NOTIFIER_VISIBLE_NOTIFY_ONLY_COMPLETION

/**
 *
 * @author by chiclaim@google.com
 */
@IntDef(
    NOTIFIER_VISIBLE,
    NOTIFIER_HIDDEN,
    NOTIFIER_VISIBLE_NOTIFY_COMPLETED,
    NOTIFIER_VISIBLE_NOTIFY_ONLY_COMPLETION
)
@Retention(AnnotationRetention.SOURCE)
annotation class NotifierVisibility
