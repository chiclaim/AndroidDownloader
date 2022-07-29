package com.chiclaim.android.updater.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.FileProvider
import com.chiclaim.android.updater.DownloadListener
import java.io.File


internal object Utils {

    private const val DOWNLOAD_COMPONENT_PACKAGE = "com.android.providers.downloads"

    fun startInstall(context: Context, apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addCategory(Intent.CATEGORY_DEFAULT)

        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", apkFile)
        } else {
            Uri.fromFile(apkFile)
        }
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }

    fun startInstall(context: Context, uri: Uri?) {
        val install = Intent(Intent.ACTION_VIEW)
        install.setDataAndType(uri, "application/vnd.android.package-archive")
        if (context !is Activity) {
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
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
        return SpHelper.get(context).getLong("${MD5.md5(url)}-id", -1L)
    }

    fun saveDownloadId(context: Context, url: String, id: Long) {
        SpHelper.get(context).putLong("${MD5.md5(url)}-id", id)
    }

    fun saveFileSize(context: Context, url: String, size: Long) {
        SpHelper.get(context).putLong("${MD5.md5(url)}-size", size)
    }

    fun getFileSize(context: Context, url: String, defaultValue: Long): Long {
        return SpHelper.get(context).getLong("${MD5.md5(url)}-size", defaultValue)
    }

    fun removeFileSize(context: Context, url: String) {
        return SpHelper.get(context).remove("${MD5.md5(url)}-size")
    }

    inline fun getValueFromCursor(cursor: Cursor, column: String, block: (index: Int) -> Unit) {
        val index = cursor.getColumnIndex(column)
        if (index != -1) block(index)
    }

    inline fun <reified T> Cursor.getValue(column: String): T? {
        val index = getColumnIndex(column)
        if (index == -1) return null
        return getValueByType(this, index, T::class.java) as T?
    }

    private fun getValueByType(cursor: Cursor, index: Int, klass: Class<*>): Any? {
        return when (klass) {
            java.lang.String::class.java -> cursor.getString(index)
            java.lang.Long::class.java -> cursor.getLong(index)
            java.lang.Integer::class.java -> cursor.getInt(index)
            java.lang.Short::class.java -> cursor.getShort(index)
            java.lang.Float::class.java -> cursor.getFloat(index)
            java.lang.Double::class.java -> cursor.getDouble(index)
            ByteArray::class.java -> cursor.getBlob(index)
            else -> null
        }
    }

    internal fun getPercent(totalSize: Long, downloadedSize: Long) = if (totalSize <= 0) 0 else
        (downloadedSize / totalSize.toDouble() * 100).toInt()


    fun getDownloadPath(context: Context): File = context.externalCacheDir ?: context.filesDir
}