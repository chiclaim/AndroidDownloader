package com.chiclam.android.updater;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;


/**
 * Description：
 * <br/>
 * Created by chiclaim on 2017/5/16.
 */

public class Updater {

    private static Updater mInstance;

    public synchronized static Updater get() {
        if (mInstance == null) {
            mInstance = new Updater();
        }
        return mInstance;
    }

    public Updater log(boolean log) {
        Logger.get().setShowLog(log);
        return this;
    }


    public void download(Context context, String url, String title) {

        if (!UpdaterUtils.checkDownloadState(context)) {
            Toast.makeText(context, R.string.system_download_component_disable, Toast.LENGTH_SHORT).show();
            UpdaterUtils.showDownloadSetting(context);
            return;
        }

        long downloadId = UpdaterUtils.getLocalDownloadId(context);
        Logger.get().d("local download id is " + downloadId);
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
                            UpdaterUtils.startInstall(context, uri);
                            return;
                        } else {
                            fdm.getDm().remove(downloadId);
                        }
                    }
                    Logger.get().d("download successful start install activity " + downloadId);
                    startDownload(context, url, title);
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    Logger.get().d("download failed " + downloadId);
                    startDownload(context, url, title);
                    break;
                default:
                    Logger.get().d("apk is already downloading" + downloadId);
                    break;
            }
        } else {
            startDownload(context, url, title);
        }
    }

    private void startDownload(Context context, String url, String title) {
        long id = FileDownloadManager.get(context).startDownload(url, title,
                context.getResources().getString(R.string.system_download_description));
        Logger.get().d("apk download start, downloadId is " + id);
    }


}
