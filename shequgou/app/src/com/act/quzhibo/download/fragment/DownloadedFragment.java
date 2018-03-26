package com.act.quzhibo.download.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.fragment.BaseFragment;
import com.act.quzhibo.download.adapter.DownLoadedListAdapterDowload;
import com.act.quzhibo.download.callback.OnDeleteListner;
import com.act.quzhibo.download.db.DBController;
import com.act.quzhibo.download.bean.MediaInfoLocal;
import com.act.quzhibo.download.event.DownloadStatusChanged;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.LoadNetView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.woblog.android.downloader.DownloadService;
import cn.woblog.android.downloader.callback.DownloadManager;
import cn.woblog.android.downloader.domain.DownloadInfo;


public class DownloadedFragment extends BaseFragment {

    private RecyclerView recycler_view;
    private DownLoadedListAdapterDowload downLoadListAdapter;
    private DownloadManager downloadManager;
    private List<DownloadInfo> uiDownLoadInfos = new ArrayList<>();
    private LoadNetView loadNetView;
    private DBController dbController;

    @Override
    protected View getLayoutView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, null);
    }

    @Override
    protected void initView() {
        super.initView();
        loadNetView = (LoadNetView) getView().findViewById(R.id.loadview);
        recycler_view = (RecyclerView) getView().findViewById(R.id.recyclerview);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        try {
            dbController = DBController.getInstance(getActivity().getApplicationContext());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData() {
        super.initData();
        EventBus.getDefault().register(this);
        downloadManager = DownloadService.getDownloadManager(getActivity().getApplicationContext());
        downLoadListAdapter = new DownLoadedListAdapterDowload(getActivity());
        recycler_view.setAdapter(downLoadListAdapter);
        setData();
    }

    @Subscribe
    public void onEventMainThread(DownloadStatusChanged event) {
        setData();
    }

    private void setData() {
        uiDownLoadInfos.clear();
        if (getArguments() != null && getArguments().getString(Constants.DOWN_LOAD_TYPE) != null) {
            for (DownloadInfo downloadInfo : downloadManager.findAllDownloaded()) {
                try {
                    MediaInfoLocal myDownloadInfoById = dbController.findMyDownloadInfoById(downloadInfo.getUri().hashCode());
                    if (myDownloadInfoById != null && myDownloadInfoById.getType().equals(getArguments().getString(Constants.DOWN_LOAD_TYPE))) {
                        uiDownLoadInfos.add(downloadInfo);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (uiDownLoadInfos != null && uiDownLoadInfos.size() > 0) {
                downLoadListAdapter.setData(uiDownLoadInfos);
                downLoadListAdapter.notifyAdapter();
                loadNetView.setVisibility(View.GONE);
            } else {
                loadNetView.setVisibility(View.VISIBLE);
                loadNetView.setlayoutVisily(Constants.NO_DOWN_DATA);
            }
            downLoadListAdapter.setOnDeleteListner(new OnDeleteListner() {
                @Override
                public void onDelete(DownloadInfo downloadInfo, int position, boolean needDelete) {
                    if (needDelete) {
                        File localFile = new File(downloadInfo.getPath());
                        if (localFile.isFile() && localFile.exists()) {
                            localFile.delete();
                            ToastUtil.showToast(getActivity(), "本地文件删除成功" + downloadInfo == null ? "null" : "nonull");
                        }
                    }
                    uiDownLoadInfos.remove(position);
                    downLoadListAdapter.setData(uiDownLoadInfos);
                    downLoadListAdapter.notifyAdapter();
                    if (uiDownLoadInfos.size() == 0) {
                        loadNetView.setVisibility(View.VISIBLE);
                        loadNetView.setlayoutVisily(Constants.NO_DOWN_DATA);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (downLoadListAdapter != null) {
            downLoadListAdapter.setData(uiDownLoadInfos);
            if (uiDownLoadInfos.size() == 0) {
                loadNetView.setVisibility(View.VISIBLE);
                loadNetView.setlayoutVisily(Constants.NO_DOWN_DATA);
            }
        }
    }
}
