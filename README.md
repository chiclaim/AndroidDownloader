在Android App都会有版本更新的功能，以前我们公司是用友盟SDK更新功能，自己服务器没有这样的功能。版本检测、Apk下载都是使用友盟。最近看到友盟的版本更新SDK文档：十月份更新功能将会停止服务器，建议开发者迁移到自己的服务器中。

> 本文的主要逻辑：

```
第一次下载成功，弹出安装界面；

如果用户没有点击安装，而是按了返回键，在某个时候，又再次使用了我们的APP

如果下载成功，则判断本地的apk的包名是否和当前程序是相同的，并且本地apk的版本号大于当前程序的版本，如果都满足则直接启动安装程序。
```

> 下载功能，Google官方推荐使用 `DownloadManager` 服务。

### 经过测试的机型

| 厂商        | 机型    |  系统版本  |
| --------   | -----:   | :----: |
| 小米        | Redmi4     |   Android6.0.1/MIUI8.5    |
| 华为        | 荣耀7i      |   Android6.0.1/EMUI4.0.3  |
| 华为        | 荣耀V8      |   Android7.0/EMUI5.0  |


### 1.  如何使用DownloadManager

`FileDownloadManager.java`

```
    public long startDownload(String uri, String title, String description) {
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(uri));

        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //req.setAllowedOverRoaming(false);

        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //设置文件的保存的位置[三种方式]
        //第一种
        //file:///storage/emulated/0/Android/data/your-package/files/Download/update.apk
        req.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update.apk");
        //第二种
        //file:///storage/emulated/0/Download/update.apk
        //req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update.apk");
        //第三种 自定义文件路径
        //req.setDestinationUri()


        // 设置一些基本显示信息
        req.setTitle(title);
        req.setDescription(description);
        req.setMimeType("application/vnd.android.package-archive");

		//加入下载队列
        return dm.enqueue(req);

        //long downloadId = dm.enqueue(req);
        //Log.d("DownloadManager", downloadId + "");
        //dm.openDownloadedFile()
    }


```


Android自带的DownloadManager模块来下载, 我们通过通知栏知道, 该模块属于系统自带, 它已经帮我们处理了下载失败、重新下载等功能。


### 2. 如何检测下载完成，然后启动安装界面

DownloadManager下载完成后会发出一个广播 `android.intent.action.DOWNLOAD_COMPLETE` 新建一个广播接收者即可：

```
public class ApkInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            // @TODO SOMETHING
        }
    }
}
```


### 3. 完善App更新逻辑

#### 1> 第一次下载成功，弹出安装界面
这个逻辑在ApkInstallReceiver里做即可：

```

public class ApkInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
           installApk(context, downloadApkId);
        }
    }
    

    private static void installApk(Context context, long downloadApkId) {
        DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Intent install = new Intent(Intent.ACTION_VIEW);
        Uri downloadFileUri = dManager.getUriForDownloadedFile(downloadApkId);
        if (downloadFileUri != null) {
            Log.d("DownloadManager", downloadFileUri.toString());
            install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(install);
        } else {
            Log.e("DownloadManager", "download error");
        }
    }
}


```


#### 2> 如果用户没有点击安装，而是按了返回键
这个时候用户可能没有安装，而是退出了安装界面，用户退出了我们的APP，在某个时候，又再次使用了我们的APP，这个时候我们不应该去下载新版本，而是使用已经下载已经存在本地的APK。


第一次下载的 downloadManager.enqueue(req)会返回一个downloadId，把downloadId保存到本地，用户下次进来的时候，取出保存的downloadId，然后通过downloadId来获取下载的状态信息。

`ApkUpdateUtils.java`

```
public static void download(Context context, String url, String title) {
        long downloadId = SpUtils.getInstance(context).getLong(KEY_DOWNLOAD_ID, -1L);
        if (downloadId != -1L) {
            FileDownloadManager fdm = FileDownloadManager.getInstance(context);
            int status = fdm.getDownloadStatus(downloadId);
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                //启动更新界面
                Uri uri = fdm.getDownloadUri(downloadId);
                if (uri != null) {
                    if (compare(getApkInfo(context, uri.getPath()), context)) {
                        startInstall(context, uri);
                        return;
                    } else {
                        fdm.getDm().remove(downloadId);
                    }
                }
                start(context, url, title);
            } else if (status == DownloadManager.STATUS_FAILED) {
                start(context, url, title);
            } else {
                if (Config.DEV_MODE) {
                    Log.d(TAG, "apk is already downloading");
                }
            }
        } else {
            start(context, url, title);
        }
    }
```

`获取APK包名、版本号`

```
    /**
     * 获取apk程序信息[packageName,versionName...]
     *
     * @param context Context
     * @param path    apk path
     */
    private static PackageInfo getApkInfo(Context context, String path) {
        if (Config.DEV_MODE) {
            Log.d(TAG, "apk download path: " + path);
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            //String packageName = info.packageName;
            //String version = info.versionName;
            //Log.d(TAG, "packageName:" + packageName + ";version:" + version);
            //String appName = pm.getApplicationLabel(appInfo).toString();
            //Drawable icon = pm.getApplicationIcon(appInfo);//得到图标信息
            return info;
        }
        return null;
    }


```

`获取下载完成的APK地址`

```
    /**
     * 获取保存文件的地址
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @see FileDownloadManager#getDownloadPath(long)
     */
    public Uri getDownloadUri(long downloadId) {
        return dm.getUriForDownloadedFile(downloadId);
    }

    public DownloadManager getDm() {
        return dm;
    }

```


`获取下载状态信息`

```
    public int getDownloadStatus(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = dm.query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));

                }
            } finally {
                c.close();
            }
        }
        return -1;
    }

```

> 如果下载失败，则重新下载并且把downloadId存起来。

```
    private static void start(Context context, String url, String title) {
        long id = FileDownloadManager.getInstance(context).startDownload(url,
                title, "下载完成后点击打开");
        SpUtils.getInstance(context).putLong(KEY_DOWNLOAD_ID, id);
        if (Config.DEV_MODE) {
            Log.d(TAG, "apk start download " + id);
        }
    }

```


> 如果下载成功，则`判断本地的apk的包名是否和当前程序是相同的，并且本地apk的版本号大于当前程序的版本`，如果都满足则直接启动安装程序。

```
    /**
     * 下载的apk和当前程序版本比较
     *
     * @param apkInfo apk file's packageInfo
     * @param context Context
     * @return 如果当前应用版本小于apk的版本则返回true
     */
    private static boolean compare(PackageInfo apkInfo, Context context) {
        if (apkInfo == null) {
            return false;
        }
        String localPackage = context.getPackageName();
        if (apkInfo.packageName.equals(localPackage)) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(localPackage, 0);
                if (apkInfo.versionCode > packageInfo.versionCode) {
                    return true;
                } else {
                    if (Config.DEV_MODE) {
                        Log.d(TAG, "apk's versionCode <= app's versionCode");
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (Config.DEV_MODE) {
            Log.d(TAG, "apk's package not match app's package");
        }
        return false;
    }
```

`启动安装界面`

```
    public static void startInstall(Context context, Uri uri) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(uri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(install);
    }
```


> 为了严谨起见，在ApkInstallReceiver里不仅要对downloadId判断，还应当把当前程序和本地apk包名和版本号对比。


## 3> 如果用户停止了下载服务[也就是`下载管理程序`]

可以通过如下代码进入 启用/停止 `下载管理程序` 界面:

```
    String packageName = "com.android.providers.downloads";
    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    intent.setData(Uri.parse("package:" + packageName));
    startActivity(intent);
```

我们先停止下载管理程序,然后点击demo里的`Download` 按钮报出如下错误:

```
Caused by: java.lang.IllegalArgumentException: Unknown URL content://downloads/my_downloads
          at android.content.ContentResolver.insert(ContentResolver.java:1227)
          at android.app.DownloadManager.enqueue(DownloadManager.java:946)
          at com.chiclam.download.FileDownloadManager.startDownload(FileDownloadManager.java:61)
          at com.chiclam.download.ApkUpdateUtils.start(ApkUpdateUtils.java:47)
          at com.chiclam.download.ApkUpdateUtils.download(ApkUpdateUtils.java:42)
          at com.chiclam.download.MainActivity.download(MainActivity.java:34)
          at java.lang.reflect.Method.invoke(Native Method) 
          at android.support.v7.app.AppCompatViewInflater$DeclaredOnClickListener.onClick(AppCompatViewInflater.java:288) 
          at android.view.View.performClick(View.java:5204) 
          at android.view.View$PerformClick.run(View.java:21153) 
          at android.os.Handler.handleCallback(Handler.java:739) 
          at android.os.Handler.dispatchMessage(Handler.java:95) 
          at android.os.Looper.loop(Looper.java:148) 
          at android.app.ActivityThread.main(ActivityThread.java:5417) 
          at java.lang.reflect.Method.invoke(Native Method) 
          at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:726) 
          at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:616) 
```

也就是说如果停止了`下载管理程序` 调用dm.enqueue(req);就会上面的错误,从而程序闪退.

所以在使用该组件的时候,需要判断该组件是否可用:

```
    private boolean canDownloadState() {
        try {
            int state = this.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");

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

```

具体详情, 查看代码.