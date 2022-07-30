package com.chiclaim.android.updater.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.jvm.Synchronized

internal class SpHelper private constructor(context: Context) {


    private val sp: SharedPreferences by lazy {
        context.applicationContext.getSharedPreferences(
            "dfire_download_sp",
            Context.MODE_PRIVATE
        )
    }

    companion object {

        private lateinit var spUtils: SpHelper

        @Synchronized
        fun get(context: Context): SpHelper {
            if (!this::spUtils.isInitialized) spUtils = SpHelper(context)
            return spUtils
        }

    }


    fun putInt(key: String, value: Int): SpHelper {
        sp.edit().putInt(key, value).apply()
        return this
    }

    fun getInt(key: String, dValue: Int): Int {
        return sp.getInt(key, dValue)
    }

    fun putLong(key: String, value: Long): SpHelper {
        sp.edit().putLong(key, value).apply()
        return this
    }

    fun getLong(key: String, dValue: Long): Long {
        return sp.getLong(key, dValue)
    }

    fun putFloat(key: String, value: Float): SpHelper {
        sp.edit().putFloat(key, value).apply()
        return this
    }

    fun getFloat(key: String, dValue: Float): Float {
        return sp.getFloat(key, dValue)
    }

    fun putBoolean(key: String, value: Boolean): SpHelper {
        sp.edit().putBoolean(key, value).apply()
        return this
    }

    fun getBoolean(key: String, dValue: Boolean): Boolean {
        return sp.getBoolean(key, dValue)
    }

    fun putString(key: String, value: String?): SpHelper {
        sp.edit().putString(key, value).apply()
        return this
    }

    fun getString(key: String, dValue: String?): String? {
        return sp.getString(key, dValue)
    }

    fun remove(key: String) {
        if (isExist(key)) {
            val editor = sp.edit()
            editor.remove(key)
            editor.apply()
        }
    }

    private fun isExist(key: String): Boolean {
        return sp.contains(key)
    }

    fun clear() {
        sp.edit().clear().apply()
    }


}