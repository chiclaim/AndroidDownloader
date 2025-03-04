package com.chiclaim.android.downloader

import android.app.Activity

internal object ActivityStackManager {


    private val list = mutableListOf<Activity>()

    fun add(activity: Activity) {
        list.add(activity)
    }

    fun remove(activity: Activity) {
        list.remove(activity)
    }


    fun finishAll() {
        list.forEach {
            it.finish()
        }
    }
}