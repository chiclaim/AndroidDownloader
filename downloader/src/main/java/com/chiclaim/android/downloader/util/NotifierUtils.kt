package com.chiclaim.android.downloader.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.chiclaim.android.downloader.R
import com.chiclaim.android.downloader.STATUS_RUNNING
import com.chiclaim.android.downloader.STATUS_SUCCESSFUL
import java.io.File


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
            @DrawableRes notifierSmallIcon: Int,
            percent: Int,
            title: CharSequence,
            content: CharSequence?,
            @DownloadStatus status: Int,
            file: File? = null
        ) {
            val notificationManager = getNotificationManager(context)
            // 在 Android 8.0 及更高版本上，需要在系统中注册应用的通知渠道
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.downloader_notifier_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(notifierSmallIcon)
                .setContentTitle(title)
                .setAutoCancel(status == STATUS_SUCCESSFUL) // canceled when it is clicked by the user.
                .setOngoing(percent != 100)

            if (percent >= 0) {
                builder.setSubText( // don't use setContentInfo(deprecated in API level 24)
                    context.getString(
                        R.string.downloader_notifier_subtext_placeholder,
                        percent
                    )
                )
            }

            if (status == STATUS_SUCCESSFUL) {
                // click to install
                file?.let {
                    val clickIntent = createInstallIntent(context, it)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        clickIntent,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE
                        else PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    builder.setContentIntent(pendingIntent)
                }
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