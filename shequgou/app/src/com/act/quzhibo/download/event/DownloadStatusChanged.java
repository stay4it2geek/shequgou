package com.act.quzhibo.download.event;


import cn.woblog.android.downloader.domain.DownloadInfo;


public class DownloadStatusChanged {

  public  DownloadInfo downloadInfo;

    public DownloadStatusChanged(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }
}
