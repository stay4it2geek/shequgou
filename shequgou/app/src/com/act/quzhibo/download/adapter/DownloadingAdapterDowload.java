package com.act.quzhibo.download.adapter;

import android.app.Dialog;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.adapter.base.DowloadBaseRvAdapter;
import com.act.quzhibo.download.callback.MyDownloadListener;
import com.act.quzhibo.download.callback.OnDeleteListner;
import com.act.quzhibo.download.db.DBController;
import com.act.quzhibo.download.bean.MediaInfoLocal;
import com.act.quzhibo.download.event.DownloadStatusChanged;
import com.act.quzhibo.download.util.FileUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.SoftReference;
import java.sql.SQLException;

import cn.woblog.android.downloader.domain.DownloadInfo;

import static cn.woblog.android.downloader.DownloadService.downloadManager;

public class DownloadingAdapterDowload extends DowloadBaseRvAdapter<DownloadInfo, DownloadingAdapterDowload.ViewHolder> {
    private DBController dbController;
    private OnDeleteListner deleteListner;
    private FragmentActivity activity;

    public DownloadingAdapterDowload(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
        try {
            dbController = DBController.getInstance(context.getApplicationContext());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setOnDeleteListner(OnDeleteListner deleteListner) {
        this.deleteListner = deleteListner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownloadingAdapterDowload.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_download_info, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        DownloadInfo data = getData(position);
        try {
            MediaInfoLocal myDownloadInfoById = dbController.findMyDownloadInfoById(data.getUri().hashCode());
            if (myDownloadInfoById != null) {
                holder.bindBaseInfo(myDownloadInfoById);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        holder.bindData(data, position);
        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iv_icon;
        private final TextView tv_size;
        private final TextView tv_status;
        private final ProgressBar pb;
        private final TextView tv_title;
        private final Button bt_action;
        private final Button bt_delete;
        private DownloadInfo downloadInfo;

        public ViewHolder(View view) {
            super(view);
            itemView.setClickable(true);
            iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            tv_size = (TextView) view.findViewById(R.id.tv_size);
            tv_status = (TextView) view.findViewById(R.id.tv_status);
            pb = (ProgressBar) view.findViewById(R.id.pb);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            bt_action = (Button) view.findViewById(R.id.bt_action);
            bt_delete = (Button) view.findViewById(R.id.bt_delete);
        }

        @SuppressWarnings("unchecked")
        public void bindData(final DownloadInfo data, final int position) {
            // Get download task status.
            downloadInfo = data;
            if (downloadInfo != null) {
                downloadInfo.setDownloadListener(
                        new MyDownloadListener(new SoftReference(DownloadingAdapterDowload.ViewHolder.this)) {
                            //  Call interval about one second.
                            @Override
                            public void onRefresh() {
                                notifyDownloadStatus();
                                if (getUserTag() != null && getUserTag().get() != null) {
                                    DownloadingAdapterDowload.ViewHolder viewHolder = (DownloadingAdapterDowload.ViewHolder) getUserTag().get();
                                    viewHolder.refresh(position);
                                }
                            }
                        });

            }

            refresh(position);


            bt_action.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (downloadInfo != null) {
                        switch (downloadInfo.getStatus()) {
                            case DownloadInfo.STATUS_NONE:
                            case DownloadInfo.STATUS_PAUSED:
                            case DownloadInfo.STATUS_ERROR:
                                downloadManager.resume(downloadInfo);
                                break;

                            case DownloadInfo.STATUS_DOWNLOADING:
                            case DownloadInfo.STATUS_PREPARE_DOWNLOAD:
                            case DownloadInfo.STATUS_WAIT:
                                downloadManager.pause(downloadInfo);
                                break;
                            case DownloadInfo.STATUS_COMPLETED:
                                publishDownloadSuccessStatus();
                                break;
                        }
                    }
                }
            });

        }

        private void refresh(final int position) {
            if (downloadInfo == null) {
                tv_size.setText("");
                pb.setProgress(0);
                bt_action.setText("下载");
                tv_status.setText("");
            } else {
                bt_action.setVisibility(View.VISIBLE);
                switch (downloadInfo.getStatus()) {
                    case DownloadInfo.STATUS_NONE:
                        bt_action.setText("下载");
                        tv_status.setText("");
                        break;
                    case DownloadInfo.STATUS_PAUSED:
                    case DownloadInfo.STATUS_ERROR:
                        bt_action.setText("继续");
                        tv_status.setText("暂停");
                        bt_delete.setVisibility(View.VISIBLE);
                        bt_delete.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FragmentDialog.newInstance(false,"确定删除?", "删除后不可恢复!", "确定", "取消","","", false, new FragmentDialog.OnClickBottomListener() {
                                    @Override
                                    public void onPositiveClick(Dialog dialog ,boolean needDelete) {
                                        deleteListner.onDelete(downloadInfo, position ,true);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                downloadManager.remove(downloadInfo);
                                            }
                                        }, 1000);
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onNegtiveClick(Dialog dialog) {
                                        dialog.dismiss();
                                    }
                                }).show(activity.getSupportFragmentManager(), "");
                            }
                        });
                        try {
                            pb.setProgress((int) (downloadInfo.getProgress() * 100.0 / downloadInfo.getSize()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tv_size.setText(FileUtil.formatFileSize(downloadInfo.getProgress()) + "/" + FileUtil
                                .formatFileSize(downloadInfo.getSize()));
                        break;

                    case DownloadInfo.STATUS_DOWNLOADING:
                    case DownloadInfo.STATUS_PREPARE_DOWNLOAD:
                        bt_action.setText("暂停");
                        bt_delete.setVisibility(View.GONE);
                        try {
                            pb.setProgress((int) (downloadInfo.getProgress() * 100.0 / downloadInfo.getSize()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tv_size.setText(FileUtil.formatFileSize(downloadInfo.getProgress()) + "/" + FileUtil
                                .formatFileSize(downloadInfo.getSize()));
                        tv_status.setText("下载中");
                        break;
                    case DownloadInfo.STATUS_COMPLETED:
                        publishDownloadSuccessStatus();
                        break;
                    case DownloadInfo.STATUS_REMOVED:
                        ToastUtil.showToast(context, "STATUS_REMOVED");
                        break;
                    case DownloadInfo.STATUS_WAIT:
                        tv_size.setText("");
                        pb.setProgress(0);
                        bt_action.setText("暂停");
                        tv_status.setText("等待");
                        break;
                }
            }
        }

        private void publishDownloadSuccessStatus() {
            EventBus.getDefault().post(new DownloadStatusChanged(downloadInfo));
        }

        public void bindBaseInfo(MediaInfoLocal mediaInfoLocal) {
            if (mediaInfoLocal.getType().equals(Constants.VIDEO_ALBUM)) {
                Glide.with(context).load(mediaInfoLocal.getIcon()).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(iv_icon);
            } else {
                Glide.with(context).load(mediaInfoLocal.getUrl()).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(iv_icon);
            }
            tv_title.setText(mediaInfoLocal.getTitle());
        }

        private void notifyDownloadStatus() {

            if (downloadInfo.getStatus() == DownloadInfo.STATUS_REMOVED) {
                try {
                    ToastUtil.showToast(context, "delete");
                    dbController.deleteMyDownloadInfo(downloadInfo.getUri().hashCode());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
