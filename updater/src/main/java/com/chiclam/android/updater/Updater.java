package com.chiclam.android.updater;

import android.app.DownloadManager;
import android.net.Uri;
import android.widget.Toast;


/**
 * Description：
 * <br/>
 * Created by chiclaim on 2017/5/16.
 */

public class Updater {

    /**
     * FileDownloadManager.getDownloadStatus如果没找到会返回-1
     */
    private static final int STATUS_UN_FIND = -1;

    private static Updater instance;

    private Updater() {
        //
    }

    public synchronized static Updater get() {
        if (instance == null) {
            instance = new Updater();
        }
        return instance;
    }

    public Updater showLog(boolean log) {
        Logger.get().setShowLog(log);
        return this;
    }

    public void download(UpdaterConfig updaterConfig) {

        if (!UpdaterUtils.checkDownloadState(updaterConfig.getContext())) {
            Toast.makeText(updaterConfig.getContext(), R.string.system_download_component_disable, Toast.LENGTH_SHORT).show();
            UpdaterUtils.showDownloadSetting(updaterConfig.getContext());
            return;
        }

        long downloadId = UpdaterUtils.getLocalDownloadId(updaterConfig.getContext());
        Logger.get().d("local download id is " + downloadId);
        if (downloadId != -1L) {
            FileDownloadManager fdm = FileDownloadManager.get();
            //获取下载状态
            int status = fdm.getDownloadStatus(updaterConfig.getContext(), downloadId);
            switch (status) {
                //下载成功
                case DownloadManager.STATUS_SUCCESSFUL:
                    Logger.get().d("downloadId=" + downloadId + " ,status = STATUS_SUCCESSFUL");
                    Uri uri = fdm.getDownloadUri(updaterConfig.getContext(), downloadId);
                    if (uri != null) {
                        //本地的版本大于当前程序的版本直接安装
                        if (UpdaterUtils.compare(updaterConfig.getContext(), uri.getPath())) {
                            Logger.get().d("start install UI");
                            UpdaterUtils.startInstall(updaterConfig.getContext(), uri);
                            return;
                        } else {
                            //从FileDownloadManager中移除这个任务
                            fdm.getDM(updaterConfig.getContext()).remove(downloadId);
                        }
                    }
                    //重新下载
                    startDownload(updaterConfig);
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    Logger.get().d("download failed " + downloadId);
                    startDownload(updaterConfig);
                    break;
                case DownloadManager.STATUS_RUNNING:
                    Logger.get().d("downloadId=" + downloadId + " ,status = STATUS_RUNNING");
                    break;
                case DownloadManager.STATUS_PENDING:
                    Logger.get().d("downloadId=" + downloadId + " ,status = STATUS_PENDING");
                    break;
                case DownloadManager.STATUS_PAUSED:
                    Logger.get().d("downloadId=" + downloadId + " ,status = STATUS_PAUSED");
                    break;
                case STATUS_UN_FIND:
                    Logger.get().d("downloadId=" + downloadId + " ,status = STATUS_UN_FIND");
                    startDownload(updaterConfig);
                    break;
                default:
                    Logger.get().d("downloadId=" + downloadId + " ,status = " + status);
                    break;
            }
        } else {
            startDownload(updaterConfig);
        }
    }

    private void startDownload(UpdaterConfig updaterConfig) {
        long id = FileDownloadManager.get().startDownload(updaterConfig);
        Logger.get().d("apk download start, downloadId is " + id);
    }

}
