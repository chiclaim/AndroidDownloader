package com.chiclam.android.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.List;

/**
 * Created by chiclaim on 2016/05/18
 */
public class UpdaterUtils {

    private static final String KEY_DOWNLOAD_ID = "downloadId";

    public static void startInstall(Context context, Uri uri) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(install);
    }

    private static String getRealPathFromURI(Context context, Uri contentURI) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Cursor cursor = context.getContentResolver().query(contentURI, null,
                    null, null, null);
            if (cursor != null) {
                try {
                    cursor.moveToFirst();
                    int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    return cursor.getString(index);
                /*for (String name : cursor.getColumnNames()) {
                    int index = cursor.getColumnIndex(name);
                    String value = cursor.getString(index);
                    Logger.get().e("key:" + name + "; value:" + value);
                }*/
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        } else {
            return contentURI.getPath();
        }
        return null;
    }


    /**
     * 下载的apk和当前程序版本比较
     *
     * @param context Context 当前运行程序的Context
     * @param uri     apk file's location
     * @return 如果当前应用版本小于apk的版本则返回true；如果当前没有安装也返回true
     */
    public static boolean compare(Context context, Uri uri) {

        String realPathUri = getRealPathFromURI(context, uri);

        PackageInfo apkInfo = getApkInfo(context, realPathUri);
        if (apkInfo == null) {
            return false;
        }

        try {
            PackageInfo currentPackageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Logger.get().getShowLog()) {
                Logger.get().e("apk file packageName=" + apkInfo.packageName +
                        ",versionName=" + apkInfo.versionName);
                Logger.get().e("current app packageName=" + currentPackageInfo.packageName +
                        ",versionName=" + currentPackageInfo.versionName);
                //String appName = pm.getApplicationLabel(appInfo).toString();
                //Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息
            }
            //如果下载的apk包名和当前应用不同，则不执行更新操作
            if (apkInfo.packageName.equals(currentPackageInfo.packageName)) {
                if (apkInfo.versionCode > currentPackageInfo.versionCode) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return true; //如果程序没有安装
        }
        return false;
    }


    /**
     * 获取apk程序信息[packageName,versionName...]
     *
     * @param context Context
     * @param path    apk path
     */
    private static PackageInfo getApkInfo(Context context, String path) {
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        PackageManager pm = context.getPackageManager();
        return pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
    }


    /**
     * 要启动的intent是否可用
     *
     * @return boolean
     */
    public static boolean intentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    /**
     * 系统的下载组件是否可用
     *
     * @return boolean
     */
    public static boolean checkDownloadState(Context context) {
        try {
            int state = context.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static void showDownloadSetting(Context context) {
        String packageName = "com.android.providers.downloads";
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        if (UpdaterUtils.intentAvailable(context, intent)) {
            context.startActivity(intent);
        }
    }

    public static long getLocalDownloadId(Context context) {
        return SpUtils.getInstance(context).getLong(KEY_DOWNLOAD_ID, -1L);
    }

    public static void saveDownloadId(Context context, long id) {
        SpUtils.getInstance(context).putLong(KEY_DOWNLOAD_ID, id);
    }


}


