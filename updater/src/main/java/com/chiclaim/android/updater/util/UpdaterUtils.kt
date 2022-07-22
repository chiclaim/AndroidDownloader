package com.chiclaim.android.updater.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import java.io.File
import java.lang.Exception


object UpdaterUtils {

    private const val DOWNLOAD_COMPONENT_PACKAGE = "com.android.providers.downloads"

    fun startInstall(context: Context, uri: Uri?) {
        val install = Intent(Intent.ACTION_VIEW)
        install.setDataAndType(uri, "application/vnd.android.package-archive")
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(install)
    }

    private fun getRealPathFromURI(context: Context, contentURI: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cursor = context.contentResolver.query(
                contentURI, null,
                null, null, null
            )
            cursor?.use {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (index != -1) return cursor.getString(index)
            }
        } else {
            return contentURI.path
        }
        return null
    }

    /**
     * 下载的 apk 和当前程序版本比较
     *
     * - 首先会判断包名，程序的包名和apk包名是否一致
     * -
     * @param context Context 当前运行程序的Context
     * @param uri     apk file's location
     * @return true 可以安装；false 不需安装
     */
    fun compare(context: Context, uri: Uri): Boolean {
        val realPathUri = getRealPathFromURI(context, uri) ?: return false
        val apkInfo = getApkInfo(context, realPathUri) ?: return false
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            e("apk file package=${apkInfo.packageName},versionCode=${apkInfo.versionCode}")
            e("current package=${packageInfo.packageName},versionCode=${packageInfo.versionCode}")
            //String appName = pm.getApplicationLabel(appInfo).toString();
            //Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息

            //如果下载的apk包名和当前应用不同，则不执行更新操作
            if (apkInfo.packageName == packageInfo.packageName
                && apkInfo.versionCode > packageInfo.versionCode
            ) {
                return true
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return true
        }
        return false
    }

    /**
     * 获取apk程序信息[packageName,versionName...]
     *
     * @param context Context
     * @param path    apk path
     */
    private fun getApkInfo(context: Context, path: String): PackageInfo? {
        val file = File(path)
        if (!file.exists()) {
            return null
        }
        val pm = context.packageManager
        return pm.getPackageArchiveInfo(path, PackageManager.GET_CONFIGURATIONS)
    }

    /**
     * 要启动的intent是否可用
     *
     * @return boolean
     */
    private fun intentAvailable(context: Context, intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }

    /**
     * 系统的下载组件是否可用
     *
     * @return boolean
     */
    fun checkDownloadState(context: Context): Boolean {
        try {
            val state =
                context.packageManager.getApplicationEnabledSetting(DOWNLOAD_COMPONENT_PACKAGE)
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @JvmStatic
    fun showDownloadSetting(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$DOWNLOAD_COMPONENT_PACKAGE")
        if (intentAvailable(context, intent)) {
            context.startActivity(intent)
        }
    }

    fun getLocalDownloadId(context: Context, url: String): Long {
        return SpHelper.get(context).getLong(MD5.md5(url), -1L)
    }

    fun saveDownloadId(context: Context, url: String, id: Long) {
        SpHelper.get(context).putLong(MD5.md5(url), id)
    }
}