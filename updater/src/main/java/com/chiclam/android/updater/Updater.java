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
                    Logger.get().d("status = STATUS_SUCCESSFUL");
                    Uri uri = fdm.getDownloadUri(downloadId);
                    if (uri != null) {
                        //本地的版本大于当前程序的版本直接安装
                        if (UpdaterUtils.compare(context, uri.getPath())) {
                            Logger.get().d("start install UI");
                            UpdaterUtils.startInstall(context, uri);
                            return;
                        } else {
                            //从FileDownloadManager中移除这个任务
                            fdm.getDm().remove(downloadId);
                        }
                    }

                    //重新下载
                    startDownload(context, url, title);
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    Logger.get().d("download failed " + downloadId);
                    startDownload(context, url, title);
                    break;
                case DownloadManager.STATUS_RUNNING:
                    Logger.get().d("status = STATUS_RUNNING");
                    break;
                case DownloadManager.STATUS_PENDING:
                    Logger.get().d("status = STATUS_PENDING");
                    break;
                case DownloadManager.STATUS_PAUSED:
                    Logger.get().d("status = STATUS_PAUSED");
                    break;
                default:
                    Logger.get().d("status = " + status);
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
