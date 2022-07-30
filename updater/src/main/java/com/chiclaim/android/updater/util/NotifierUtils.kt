package com.chiclaim.android.updater.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.chiclaim.android.updater.R
import com.chiclaim.android.updater.STATUS_RUNNING


/**
 *
 * @author by chiclaim@google.com
 */
internal class NotifierUtils private constructor() {

    companion object {
        private const val CHANNEL_ID = "download_channel_normal"

        private fun getNotificationManager(context: Context): NotificationManager {
            return context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
        }

        fun showNotification(
            context: Context,
            id: Int,
            @DrawableRes drawable: Int,
            percent: Int,
            title: CharSequence,
            content: CharSequence?,
            @DownloadStatus status: Int
        ) {
            val notificationManager = getNotificationManager(context)
            // 在 Android 8.0 及更高版本上，需要在系统中注册应用的通知渠道
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.updater_notifier_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(drawable)
                .setContentTitle(title)
                .setAutoCancel(false)
                .setOngoing(percent != 100)

            if (percent >= 0) {
                builder.setSubText( // don't use setContentInfo(deprecated in API level 24)
                    context.getString(
                        R.string.updater_notifier_subtext_placeholder,
                        percent
                    )
                )
            }

            if (status == STATUS_RUNNING)
                builder.setProgress(100, percent, percent <= 0)

            content?.let {
                builder.setContentText(it)
            }
            notificationManager.notify(id, builder.build())
        }

        fun cancelNotification(context: Context, id: Int) {
            getNotificationManager(context).cancel(id)
        }

    }


}