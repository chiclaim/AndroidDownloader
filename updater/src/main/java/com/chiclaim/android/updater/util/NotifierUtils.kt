package com.chiclaim.android.updater.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.chiclaim.android.updater.R


/**
 *
 * @author by chiclaim@google.com
 */
internal class NotifierUtils private constructor() {

    companion object {
        private const val CHANNEL_ID = "download_channel_normal"

        fun showNotification(context: Context, id: Int, @DrawableRes drawable: Int, percent: Int) {
            val notificationManager: NotificationManager =
                context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 在 Android 8.0 及更高版本上，需要在系统中注册应用的通知渠道
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.updater_notifier_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(drawable)
                .setContentTitle("Title")
                .setContentText("Content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOngoing(percent != 100)
                .setProgress(100, percent, percent <= 0)
                .setSubText( // don't use setContentInfo(deprecated in API level 24)
                    context.getString(
                        R.string.updater_notifier_subtext_placeholder,
                        percent
                    )
                )
            notificationManager.notify(id, builder.build())
        }

    }


}