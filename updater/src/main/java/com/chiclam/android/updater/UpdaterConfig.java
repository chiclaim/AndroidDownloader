package com.chiclam.android.updater;

import android.content.Context;

/**
 * Description：下载器参数配置
 * <br/>
 * Created by kumu on 2017/5/17.
 */

public class UpdaterConfig {

    private boolean mIsLog;
    private String mTitle;
    private String mDescription;
    private String mDownloadPath;
    private String mFileUrl;
    private String mFilename;
    private boolean mIsShowDownloadUI = true;
    private int mNotificationVisibility;
    private boolean mCanMediaScanner;
    private boolean mAllowedOverRoaming;
    private int mAllowedNetworkTypes = ~0;// default to all network types allowed

    private Context mContext;


    private UpdaterConfig(Context context) {
        this.mContext = context;
    }

    public boolean isLog() {
        return mIsLog;
    }

    public void setIsLog(boolean mIsLog) {
        this.mIsLog = mIsLog;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getDownloadPath() {
        return mDownloadPath;
    }

    public void setDownloadPath(String mDownloadPath) {
        this.mDownloadPath = mDownloadPath;
    }

    public String getFileUrl() {
        return mFileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.mFileUrl = fileUrl;
    }

    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String mFilename) {
        this.mFilename = mFilename;
    }

    public boolean isShowDownloadUI() {
        return mIsShowDownloadUI;
    }

    public void setIsShowDownloadUI(boolean mIsShowDownloadUI) {
        this.mIsShowDownloadUI = mIsShowDownloadUI;
    }

    public int isIsNotificationVisibility() {
        return mNotificationVisibility;
    }

    public void setIsNotificationVisibility(int mIsNotificationVisibility) {
        this.mNotificationVisibility = mIsNotificationVisibility;
    }

    public boolean isCanMediaScanner() {
        return mCanMediaScanner;
    }

    public void setCanMediaScanner(boolean mCanMediaScanner) {
        this.mCanMediaScanner = mCanMediaScanner;
    }

    public boolean isAllowedOverRoaming() {
        return mAllowedOverRoaming;
    }

    public void setAllowedOverRoaming(boolean mAllowedOverRoaming) {
        this.mAllowedOverRoaming = mAllowedOverRoaming;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public int getAllowedNetworkTypes() {
        return mAllowedNetworkTypes;
    }

    public static class Builder {

        UpdaterConfig updaterConfig;

        public Builder(Context context) {
            updaterConfig = new UpdaterConfig(context.getApplicationContext());
        }

        public Builder setIsLog(boolean mIsLog) {
            updaterConfig.setIsLog(mIsLog);
            return this;
        }

        public Builder setTitle(String title) {
            updaterConfig.setTitle(title);
            return this;
        }

        public Builder setDescription(String description) {
            updaterConfig.setDescription(description);
            return this;
        }

        /**
         * 文件下载路径
         *
         * @param downloadPath
         * @return
         */
        public Builder setDownloadPath(String downloadPath) {
            updaterConfig.setDownloadPath(downloadPath);
            return this;
        }

        /**
         * 下载的文件名
         *
         * @param filename
         * @return
         */
        public Builder setFilename(String filename) {
            updaterConfig.setFilename(filename);
            return this;
        }

        /**
         * 文件网络地址
         *
         * @param url
         * @return
         */
        public Builder setFileUrl(String url) {
            updaterConfig.setFileUrl(url);
            return this;
        }

        public Builder setIsShowDownloadUI(boolean isShowDownloadUI) {
            updaterConfig.setIsShowDownloadUI(isShowDownloadUI);
            return this;
        }

        public Builder setNotificationVisibility(int notificationVisibility) {
            updaterConfig.mNotificationVisibility = notificationVisibility;
            return this;
        }

        /**
         * 能否被 MediaScanner 扫描
         *
         * @param canMediaScanner
         * @return
         */
        public Builder setCanMediaScanner(boolean canMediaScanner) {
            updaterConfig.mCanMediaScanner = canMediaScanner;
            return this;
        }

        /**
         * 移动网络是否允许下载
         *
         * @param allowedOverRoaming
         * @return
         */
        public Builder setAllowedOverRoaming(boolean allowedOverRoaming) {
            updaterConfig.mAllowedOverRoaming = allowedOverRoaming;
            return this;
        }

        public Builder setContext(Context context) {
            updaterConfig.mContext = context;
            return this;

        }

        /**
         * By default, all network types are allowed
         *
         * @param allowedNetworkTypes
         * @see AllowedNetworkType#NETWORK_MOBILE
         * @see AllowedNetworkType#NETWORK_WIFI
         */
        public Builder setAllowedNetworkTypes(int allowedNetworkTypes) {
            updaterConfig.mAllowedNetworkTypes = allowedNetworkTypes;
            return this;
        }


        public UpdaterConfig build() {
            return updaterConfig;
        }


    }

    public interface AllowedNetworkType {
        /**
         * Bit flag for {@link android.app.DownloadManager.Request#NETWORK_MOBILE}
         */
        int NETWORK_MOBILE = 1 << 0;

        /**
         * Bit flag for {@link android.app.DownloadManager.Request#NETWORK_WIFI}
         */
        int NETWORK_WIFI = 1 << 1;
    }


}
