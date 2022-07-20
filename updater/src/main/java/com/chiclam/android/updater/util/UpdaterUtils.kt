package com.chiclam.android.updater.util

import android.content.Context
import com.chiclam.android.updater.util.e
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import com.chiclam.android.updater.util.UpdaterUtils
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.chiclam.android.updater.util.SpUtils
import java.io.File
import java.lang.Exception

/**
 * Created by chiclaim on 2016/05/18
 */
object UpdaterUtils {

    private const val KEY_DOWNLOAD_ID = "downloadId"

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
            if (cursor != null) {
                try {
                    cursor.moveToFirst()
                    val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    return cursor.getString(index)
                    /*for (String name : cursor.getColumnNames()) {
                    int index = cursor.getColumnIndex(name);
                    String value = cursor.getString(index);
                    Logger.get().e("key:" + name + "; value:" + value);
                }*/
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    cursor.close()
                }
            }
        } else {
            return contentURI.path
        }
        return null
    }

    /**
     * 下载的apk和当前程序版本比较
     *
     * @param context Context 当前运行程序的Context
     * @param uri     apk file's location
     * @return 如果当前应用版本小于apk的版本则返回true；如果当前没有安装也返回true
     */
    fun compare(context: Context, uri: Uri): Boolean {
        val realPathUri = getRealPathFromURI(context, uri)
        val apkInfo = getApkInfo(context, realPathUri) ?: return false
        try {
            val currentPackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            e(
                "apk file packageName=" + apkInfo.packageName +
                        ",versionName=" + apkInfo.versionName
            )
            e(
                "current app packageName=" + currentPackageInfo.packageName +
                        ",versionName=" + currentPackageInfo.versionName
            )
            //String appName = pm.getApplicationLabel(appInfo).toString();
            //Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息

            //如果下载的apk包名和当前应用不同，则不执行更新操作
            if (apkInfo.packageName == currentPackageInfo.packageName) {
                if (apkInfo.versionCode > currentPackageInfo.versionCode) {
                    return true
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return true //如果程序没有安装
        }
        return false
    }

    /**
     * 获取apk程序信息[packageName,versionName...]
     *
     * @param context Context
     * @param path    apk path
     */
    private fun getApkInfo(context: Context, path: String?): PackageInfo? {
        val file = File(path)
        if (!file.exists()) {
            return null
        }
        val pm = context.packageManager
        return pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
    }

    /**
     * 要启动的intent是否可用
     *
     * @return boolean
     */
    fun intentAvailable(context: Context, intent: Intent): Boolean {
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
                context.packageManager.getApplicationEnabledSetting("com.android.providers.downloads")
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
        val packageName = "com.android.providers.downloads"
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        if (intentAvailable(context, intent)) {
            context.startActivity(intent)
        }
    }

    fun getLocalDownloadId(context: Context?): Long {
        return SpUtils.getInstance(context).getLong(KEY_DOWNLOAD_ID, -1L)
    }

    fun saveDownloadId(context: Context?, id: Long) {
        SpUtils.getInstance(context).putLong(KEY_DOWNLOAD_ID, id)
    }
}