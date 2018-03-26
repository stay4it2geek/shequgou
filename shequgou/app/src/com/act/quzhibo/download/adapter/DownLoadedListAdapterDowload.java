package com.act.quzhibo.download.adapter;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.adapter.base.DowloadBaseRvAdapter;
import com.act.quzhibo.download.callback.OnDeleteListner;
import com.act.quzhibo.download.db.DBController;
import com.act.quzhibo.download.bean.MediaInfo;
import com.act.quzhibo.download.bean.MediaInfoLocal;
import com.act.quzhibo.ui.activity.BGAPhotoPreviewActivity;
import com.act.quzhibo.widget.FragmentDialog;
import com.bumptech.glide.Glide;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import cn.woblog.android.downloader.domain.DownloadInfo;

import static cn.woblog.android.downloader.DownloadService.downloadManager;

public class DownLoadedListAdapterDowload extends DowloadBaseRvAdapter<DownloadInfo, DownLoadedListAdapterDowload.MyViewHolder> {
    private FragmentActivity activity;
    private DBController dbController;
    private OnDeleteListner deleteListner;
    ArrayList<MediaInfo> mMediaInfos = new ArrayList<>();

    public DownLoadedListAdapterDowload(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
        try {
            dbController = DBController.getInstance(context.getApplicationContext());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        notifyAdapter();

    }

    public void notifyAdapter() {
        mMediaInfos.clear();
        for (DownloadInfo downloadInfo : getListData()) {
            final MediaInfoLocal myDownloadInfoById;
            try {
                myDownloadInfoById = dbController.findMyDownloadInfoById(downloadInfo.getUri().hashCode());
                final MediaInfo mediaInfo = new MediaInfo(myDownloadInfoById.getTitle(),
                        myDownloadInfoById.getName(),
                        myDownloadInfoById.getIcon(),
                        myDownloadInfoById.getUrl(),
                        myDownloadInfoById.getType(),
                        myDownloadInfoById.getLocalPath());
                mMediaInfos.add(mediaInfo);
            } catch (SQLException e) {
            }

        }
    }

    public void setOnDeleteListner(OnDeleteListner deleteListner) {
        this.deleteListner = deleteListner;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_downloaded_thumb, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final DownloadInfo downloadInfo = getData(position);
        if (downloadInfo != null && downloadInfo.getStatus() == DownloadInfo.STATUS_COMPLETED) {
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentDialog.newInstance(true, "确定删除?", "删除后不可恢复!", "确定", "取消", "", "", false, new FragmentDialog.OnClickBottomListener() {
                        @Override
                        public void onPositiveClick(Dialog dialog, boolean needDelete) {
                            deleteListner.onDelete(downloadInfo, position, needDelete);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    downloadManager.remove(downloadInfo);
                                    try {
                                        dbController.deleteMyDownloadInfo(downloadInfo.getUri().hashCode());
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, 500);
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegtiveClick(Dialog dialog) {
                            dialog.dismiss();
                        }
                    }).show(activity.getSupportFragmentManager(), "");
                }
            });


            if (mMediaInfos.size() > 0) {
                holder.download_item_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mMediaInfos.get(position).getType().equals(Constants.PHOTO_ALBUM)) {
                            Intent intent = new Intent();
                            intent.putExtra("EXTRA_CURRENT_POSITION", position);
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("MEDIA_INFO_LIST", mMediaInfos);//
                            intent.putExtras(bundle);
                            intent.setClass(activity, BGAPhotoPreviewActivity.class);
                            activity.startActivity(intent);
                            if (mMediaInfos.size() > 0) {
                                Log.e("mMediaInfos", mMediaInfos.size() + "");
                                activity.startActivity(BGAPhotoPreviewActivity.newIntent(activity, mMediaInfos, position, false));
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            File file = new File(mMediaInfos.get(position).getLocalPath());
                            Uri uri;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                Uri contentUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider", file);
                                intent.setDataAndType(contentUri, "video/*");
                            } else {
                                uri = Uri.fromFile(file);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setDataAndType(uri, "video/*");
                            }
                            context.startActivity(intent);
                        }
                    }
                });
                if (mMediaInfos.get(position).getType().equals(Constants.PHOTO_ALBUM)) {
                    Glide.with(activity).load(mMediaInfos.get(position).getUrl()).thumbnail(0.1f).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(holder.imgThumb);//加载网络图片
                } else {
                    holder.videoLayout.setVisibility(View.VISIBLE);
                    Glide.with(activity).load(mMediaInfos.get(position).getIcon()).skipMemoryCache(false).placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(holder.videoImg);//加载网络图片
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.delete.setVisibility(View.VISIBLE);
                }
            }, 500);
        }
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView delete;
        private ImageView videoImg;
        private ImageView imgThumb;
        private FrameLayout videoLayout;
        private FrameLayout download_item_layout;

        public MyViewHolder(View view) {
            super(view);
            imgThumb = (ImageView) view.findViewById(R.id.imgThumb);
            videoImg = (ImageView) view.findViewById(R.id.videoImg);
            delete = (TextView) view.findViewById(R.id.delete);
            videoLayout = (FrameLayout) view.findViewById(R.id.videoLayout);
            download_item_layout = (FrameLayout) view.findViewById(R.id.download_item_layout);

        }
    }
}
