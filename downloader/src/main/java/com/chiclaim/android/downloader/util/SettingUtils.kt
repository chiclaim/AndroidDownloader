@file:JvmName("SettingUtils")

package com.chiclaim.android.downloader.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * 进入通知栏设置页面
 */
fun goNotificationSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } else {
        goAppDetailSettings(context)
    }
}

/**
 * 进入应用详情设置页面
 */
fun goAppDetailSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}