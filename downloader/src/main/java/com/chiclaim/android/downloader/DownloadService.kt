package com.chiclaim.android.downloader

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 *
 * @author by chiclaim@google.com
 */
class DownloadService:Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}