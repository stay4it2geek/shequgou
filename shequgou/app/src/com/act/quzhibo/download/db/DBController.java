package com.act.quzhibo.download.db;

import android.content.Context;

import com.act.quzhibo.download.bean.MediaInfoLocal;
import com.act.quzhibo.download.bean.MyDownloadInfLocal;
import com.act.quzhibo.download.bean.MyDownloadThreadInfoLocal;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.woblog.android.downloader.db.DownloadDBController;
import cn.woblog.android.downloader.domain.DownloadInfo;
import cn.woblog.android.downloader.domain.DownloadThreadInfo;


import static cn.woblog.android.downloader.domain.DownloadInfo.STATUS_COMPLETED;
import static cn.woblog.android.downloader.domain.DownloadInfo.STATUS_PAUSED;


public class DBController implements DownloadDBController {

    static DBController instance;
    Context context;
    DBHelper dbHelper;
    Dao<MediaInfoLocal, Integer> myBusinessInfoLocalsDao;
    Dao<MyDownloadInfLocal, Integer> myDownloadInfLocalDao;
    Dao<MyDownloadThreadInfoLocal, Integer> myDownloadThreadInfoLocalDao;

    public DBController(Context context) throws SQLException {
        this.context = context;
        dbHelper = new DBHelper(context);
        try {
            myBusinessInfoLocalsDao = dbHelper.getDao(MediaInfoLocal.class);
            myDownloadInfLocalDao = dbHelper.getDao(MyDownloadInfLocal.class);
            myDownloadThreadInfoLocalDao = dbHelper.getDao(MyDownloadThreadInfoLocal.class);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static DBController getInstance(Context context) throws SQLException {
        if (instance == null) {
            instance = new DBController(context);
        }
        return instance;
    }


    public void createOrUpdateMyDownloadInfo(MediaInfoLocal downloadInfoLocal)
            throws SQLException {
        myBusinessInfoLocalsDao.createOrUpdate(downloadInfoLocal);
    }

    public int deleteMyDownloadInfo(int id)
            throws SQLException {
        return myBusinessInfoLocalsDao.deleteById(id);
    }

    public MediaInfoLocal findMyDownloadInfoById(int id)
            throws SQLException {
        return myBusinessInfoLocalsDao.queryForId(id);
    }

    @Override
    public List<DownloadInfo> findAllDownloading() {
        try {
            List<MyDownloadInfLocal> myDownloadInfLocals = myDownloadInfLocalDao.queryBuilder().where()
                    .ne("status", STATUS_COMPLETED).query();
            return convertDownloadInfos(myDownloadInfLocals);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    @Override
    public List<DownloadInfo> findAllDownloaded() {
        try {
            List<MyDownloadInfLocal> myDownloadInfLocals = myDownloadInfLocalDao.queryBuilder().where()
                    .eq("status", STATUS_COMPLETED).query();
            return convertDownloadInfos(myDownloadInfLocals);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public DownloadInfo findDownloadedInfoById(int id) {
        try {
            return convertDownloadInfo(myDownloadInfLocalDao.queryForId(id));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void pauseAllDownloading() {

        try {
            UpdateBuilder<MyDownloadInfLocal, Integer> myDownloadInfLocalIntegerUpdateBuilder = myDownloadInfLocalDao
                    .updateBuilder();
            myDownloadInfLocalIntegerUpdateBuilder.updateColumnValue("status", STATUS_PAUSED).where()
                    .ne("status", STATUS_COMPLETED);
            myDownloadInfLocalIntegerUpdateBuilder.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createOrUpdate(DownloadInfo downloadInfo) {
        try {
            myDownloadInfLocalDao.createOrUpdate(convertDownloadInfo(downloadInfo));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createOrUpdate(DownloadThreadInfo downloadThreadInfo) {
        try {
            myDownloadThreadInfoLocalDao.createOrUpdate(convertDownloadThreadInfo(downloadThreadInfo));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(DownloadInfo downloadInfo) {
        try {
            myDownloadInfLocalDao.deleteById(downloadInfo.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(DownloadThreadInfo download) {
        try {
            myDownloadThreadInfoLocalDao.deleteById(download.getDownloadInfoId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    List<DownloadInfo> convertDownloadInfos(List<MyDownloadInfLocal> infos) {
        List<DownloadInfo> downloadInfos = new ArrayList<>();
        for (MyDownloadInfLocal downloadInfLocal : infos) {
            downloadInfos.add(convertDownloadInfo(downloadInfLocal));
        }
        return downloadInfos;
    }


    DownloadInfo convertDownloadInfo(MyDownloadInfLocal downloadInfLocal) {
        if (downloadInfLocal == null) {
            return null;
        }
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setCreateAt(downloadInfLocal.getCreateAt());
        downloadInfo.setUri(downloadInfLocal.getUri());
        downloadInfo.setPath(downloadInfLocal.getPath());
        downloadInfo.setSize(downloadInfLocal.getSize());
        downloadInfo.setProgress(downloadInfLocal.getProgress());
        downloadInfo.setStatus(downloadInfLocal.getStatus());
        downloadInfo.setSupportRanges(downloadInfLocal.getSupportRanges());
        downloadInfo.setDownloadThreadInfos(
                converDownloadThreadInfos1(downloadInfLocal.getDownloadThreadInfos()));
        return downloadInfo;
    }

    List<DownloadThreadInfo> converDownloadThreadInfos1(
            List<MyDownloadThreadInfoLocal> downloadThreadInfosLocal) {
        List<DownloadThreadInfo> downloadThreadInfos = new ArrayList<>();
        if (downloadThreadInfosLocal != null) {
            for (MyDownloadThreadInfoLocal d : downloadThreadInfosLocal
                    ) {
                downloadThreadInfos.add(convertDownloadThreadInfo(d));
            }
        }

        return downloadThreadInfos;
    }

    List<MyDownloadThreadInfoLocal> convertDownloadThreadInfos(
            List<DownloadThreadInfo> downloadThreadInfos) {
        if (downloadThreadInfos == null) {
            return null;
        }
        List<MyDownloadThreadInfoLocal> downloadThreadInfosLocal = new ArrayList<>();
        for (DownloadThreadInfo d : downloadThreadInfos
                ) {
            downloadThreadInfosLocal.add(convertDownloadThreadInfo(d));
        }
        return downloadThreadInfosLocal;
    }


    DownloadThreadInfo convertDownloadThreadInfo(MyDownloadThreadInfoLocal d) {
        DownloadThreadInfo downloadThreadInfo = new DownloadThreadInfo();
        downloadThreadInfo.setProgress(d.getId());
        downloadThreadInfo.setThreadId(d.getThreadId());
        downloadThreadInfo.setDownloadInfoId(d.getDownloadInfoId());
        downloadThreadInfo.setUri(d.getUri());
        downloadThreadInfo.setStart(d.getStart());
        downloadThreadInfo.setEnd(d.getEnd());
        downloadThreadInfo.setProgress(d.getProgress());
        return downloadThreadInfo;
    }

    MyDownloadThreadInfoLocal convertDownloadThreadInfo(DownloadThreadInfo d) {
        MyDownloadThreadInfoLocal downloadThreadInfo = new MyDownloadThreadInfoLocal();
        downloadThreadInfo.setProgress(d.getId());
        downloadThreadInfo.setThreadId(d.getThreadId());
        downloadThreadInfo.setDownloadInfoId(d.getDownloadInfoId());
        downloadThreadInfo.setUri(d.getUri());
        downloadThreadInfo.setStart(d.getStart());
        downloadThreadInfo.setEnd(d.getEnd());
        downloadThreadInfo.setProgress(d.getProgress());
        return downloadThreadInfo;
    }

    MyDownloadInfLocal convertDownloadInfo(DownloadInfo downloadInfo) {
        MyDownloadInfLocal downloadInfLocal = new MyDownloadInfLocal();
        downloadInfLocal.setCreateAt(downloadInfo.getCreateAt());
        downloadInfLocal.setUri(downloadInfo.getUri());
        downloadInfLocal.setPath(downloadInfo.getPath());
        downloadInfLocal.setSize(downloadInfo.getSize());
        downloadInfLocal.setProgress(downloadInfo.getProgress());
        downloadInfLocal.setStatus(downloadInfo.getStatus());
        downloadInfLocal.setSupportRanges(downloadInfo.getSupportRanges());
        downloadInfLocal
                .setDownloadThreadInfos(convertDownloadThreadInfos(downloadInfo.getDownloadThreadInfos()));
        return downloadInfLocal;
    }
}
