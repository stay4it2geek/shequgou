package com.act.quzhibo.download.callback;

import cn.woblog.android.downloader.callback.DownloadManager;
import cn.woblog.android.downloader.domain.DownloadInfo;

/**
 * Created by weiminglin on 17/9/7.
 */

public interface OnDeleteListner {
    void onDelete(DownloadInfo downloadInfo, int position ,boolean needDelete);
}
