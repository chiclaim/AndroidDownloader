@file:JvmName("InstallUtils")

package com.chiclaim.android.updater.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.content.FileProvider
import com.chiclaim.android.updater.BuildConfig
import com.chiclaim.android.updater.UpgradePermissionDialogActivity
import java.io.File

private const val DOWNLOAD_COMPONENT_PACKAGE = "com.android.providers.downloads"

fun hasInstallPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return context.packageManager.canRequestPackageInstalls()
    }
    return true
}

fun settingPackageInstall(activity: Activity, requestCode: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intentSetting = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:" + activity.packageName)
        )
        //intentSetting.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivityForResult(intentSetting, requestCode)
    }
}

fun startInstall(context: Context, apkFile: File) {
    val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}${BuildConfig.AUTHORITIES_SUFFIX}",
            apkFile
        )
    } else {
        Uri.fromFile(apkFile)
    }
    startInstall(context, uri)
}

fun startInstall(context: Context, uri: Uri) {
    if (!hasInstallPermission(context)) {
        UpgradePermissionDialogActivity.launch(context, uri.toString())
        return
    }
    val intent = Intent(Intent.ACTION_VIEW)
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    context.startActivity(intent)
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
private fun compare(context: Context, uri: Uri): Boolean {
    val realFilePath = getRealPathFromURI(context, uri) ?: return false
    val apkFileInfo = getApkFileSignature(context, realFilePath) ?: return false
    try {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_CONFIGURATIONS
        )
        if (BuildConfig.DEBUG) {
            e("apk file package=${apkFileInfo.packageName},versionCode=${apkFileInfo.versionCode}")
            e("current package=${packageInfo.packageName},versionCode=${packageInfo.versionCode}")
        }
        //String appName = pm.getApplicationLabel(appInfo).toString();
        //Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息

        //如果下载的apk包名和当前应用不同，则不执行更新操作
        if (apkFileInfo.packageName == packageInfo.packageName
            && apkFileInfo.versionCode > packageInfo.versionCode
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
private fun getApkFileSignature(context: Context, path: String): PackageInfo? {
    val file = File(path)
    if (!file.exists()) {
        return null
    }
    val pm = context.packageManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pm.getPackageArchiveInfo(
            path,
            PackageManager.GET_SIGNING_CERTIFICATES
        )

    } else {
        pm.getPackageArchiveInfo(
            path,
            PackageManager.GET_SIGNATURES
        )
    }
}

/**
 * 要启动的intent是否可用
 *
 * @return boolean
 */
private fun intentAvailable(context: Context, intent: Intent): Boolean {
    return intent.resolveActivity(context.packageManager) != null
}


fun showDownloadComponentSetting(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.parse("package:${DOWNLOAD_COMPONENT_PACKAGE}")
    if (intentAvailable(context, intent)) {
        context.startActivity(intent)
    }
}


/**
 * 系统的下载组件是否可用
 *
 * @return boolean
 */
fun checkDownloadComponentEnable(context: Context): Boolean {
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