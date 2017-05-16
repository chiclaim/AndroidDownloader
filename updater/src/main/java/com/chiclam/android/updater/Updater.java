package com.chiclam.android.updater;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import static com.chiclam.android.updater.UpdaterUtils.startInstall;

/**
 * Description：
 * <br/>
 * Created by chiclaim on 2017/5/16.
 */

public class Updater {

    private static final String TAG = "Updater";


    private static Updater mInstance;

    private boolean mShowLog;

    public synchronized static Updater get() {
        if (mInstance == null) {
            mInstance = new Updater();
        }
        return mInstance;
    }

    public Updater log(boolean log) {
        mShowLog = log;
        return this;
    }


    public void download(Context context, String url, String title) {

        if (!UpdaterUtils.checkDownloadState(context)) {
            Toast.makeText(context, R.string.system_download_component_disable, Toast.LENGTH_SHORT).show();
            UpdaterUtils.showDownloadSetting(context);
            return;
        }

        long downloadId = UpdaterUtils.getLocalDownloadId(context);
        if (downloadId != -1L) {
            FileDownloadManager fdm = FileDownloadManager.get(context);
            //获取下载状态
            int status = fdm.getDownloadStatus(downloadId);
            switch (status) {
                //下载成功
                case DownloadManager.STATUS_SUCCESSFUL:
                    Uri uri = fdm.getDownloadUri(downloadId);
                    if (uri != null) {
                        if (UpdaterUtils.compare(context, uri.getPath())) {
                            startInstall(context, uri);
                            return;
                        } else {
                            fdm.getDm().remove(downloadId);
                        }
                    }
                    startDownload(context, url, title);
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    startDownload(context, url, title);
                    break;
                default:
                    if (mShowLog) {
                        Log.d(TAG, "apk is already downloading");
                    }
                    break;
            }
        } else {
            startDownload(context, url, title);
        }
    }

    private void startDownload(Context context, String url, String title) {
        long id = FileDownloadManager.get(context).startDownload(url, title,
                context.getResources().getString(R.string.system_download_description));
        if (mShowLog) {
            Log.d(TAG, "apk download start, downloadId is " + id);
        }
    }


}
