@file:JvmName("Logger")

package com.chiclaim.android.updater.util

import android.util.Log

/**
 *
 * @author by chiclaim@google.com
 */

private const val LOG_TAG = "AndroidUpdater"

internal fun i(message: String) {
    Log.i(LOG_TAG, message)
}

internal fun d(message: String) {
    Log.d(LOG_TAG, message)
}

internal fun e(message: String) {
    Log.e(LOG_TAG, message)
}

