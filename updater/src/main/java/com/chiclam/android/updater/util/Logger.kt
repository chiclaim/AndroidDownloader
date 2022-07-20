@file:JvmName("Logger")

package com.chiclam.android.updater.util

import android.util.Log

/**
 *
 * @author by chiclaim@google.com
 */

private const val LOG_TAG = "AndroidUpdater"

fun i(message: String) {
    Log.i(LOG_TAG, message)
}

fun d(message: String) {
    Log.d(LOG_TAG, message)
}

fun e(message: String) {
    Log.e(LOG_TAG, message)
}

