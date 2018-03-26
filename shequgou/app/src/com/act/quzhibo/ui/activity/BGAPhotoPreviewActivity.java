
package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.act.quzhibo.common.Constants;
import com.act.quzhibo.download.activity.DownloadManagerActivity;
import com.act.quzhibo.download.db.DBController;
import com.act.quzhibo.download.bean.MediaInfo;
import com.act.quzhibo.download.bean.MediaInfoLocal;
import com.act.quzhibo.util.FileUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.FragmentDialog;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import cn.bingoogolapple.androidcommon.adapter.BGAOnNoDoubleClickListener;
import cn.bingoogolapple.photopicker.activity.BGAPPToolbarActivity;
import cn.bingoogolapple.photopicker.adapter.BGAPhotoPageAdapter;
import cn.bingoogolapple.photopicker.util.BGAAsyncTask;
import cn.bingoogolapple.photopicker.util.BGASavePhotoTask;
import cn.bingoogolapple.photopicker.widget.BGAHackyViewPager;
import cn.woblog.android.downloader.DownloadService;
import cn.woblog.android.downloader.callback.DownloadListener;
import cn.woblog.android.downloader.callback.DownloadManager;
import cn.woblog.android.downloader.domain.DownloadInfo;
import cn.woblog.android.downloader.exception.DownloadException;
import uk.co.senab.photoview.PhotoViewAttacher;

import static cn.woblog.android.downloader.domain.DownloadInfo.STATUS_WAIT;


public class BGAPhotoPreviewActivity extends BGAPPToolbarActivity implements PhotoViewAttacher.OnViewTapListener, BGAAsyncTask.Callback<Void> {
    private static final String EXTRA_CURRENT_POSITION = "EXTRA_CURRENT_POSITION";
    private static final String MEDIA_INFO_LIST = "MEDIA_INFO_LIST";
    private TextView mTitleTv;
    private ImageView mDownloadIv;
    private BGAHackyViewPager mContentHvp;
    private BGAPhotoPageAdapter mAdapter;
    private boolean mIsHidden = false;
    private BGASavePhotoTask mSavePhotoTask;
    private DownloadManager downloadManager;
    private DBController dbController;
    private ArrayList<MediaInfo> mediaInfos=new ArrayList<>();
    private long mLastShowHiddenTime;

    /**
     * 获取查看图片的intent
     */
    public static Intent newIntent(Context context, ArrayList<MediaInfo> mMediaInfos, int currentPosition, boolean isShowDownLoad) {
        Intent intent = new Intent(context, BGAPhotoPreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MEDIA_INFO_LIST, mMediaInfos);//
        intent.putExtras(bundle);
        intent.putExtra(EXTRA_CURRENT_POSITION, currentPosition);
        intent.putExtra("isShowDownLoad", isShowDownLoad);

        return intent;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {

        setNoLinearContentView(cn.bingoogolapple.photopicker.R.layout.bga_pp_activity_photo_preview);

        mContentHvp = getViewById(cn.bingoogolapple.photopicker.R.id.hvp_photo_preview_content);
        mediaInfos = getIntent().getParcelableArrayListExtra(MEDIA_INFO_LIST);
        if (mediaInfos != null && mediaInfos.size() > 0) {
            downloadManager = DownloadService.getDownloadManager(getApplicationContext());
            try {
                dbController = DBController.getInstance(this.getApplicationContext());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else{
            ToastUtil.showToast(this,"数据为空");
        }
    }

    @Override
    protected void setListener() {
        mContentHvp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                renderTitleTv();
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

        ArrayList<String> previewImages = new ArrayList<>();
        previewImages.clear();
        if (mediaInfos != null && mediaInfos.size() > 0) {
            for (MediaInfo mediaInfo : mediaInfos) {
                previewImages.add(mediaInfo.getUrl());
            }
        }else{
            ToastUtil.showToast(this,"图片数量为空");
        }
        int currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);
        mAdapter = new BGAPhotoPageAdapter(this, this, previewImages);
        mContentHvp.setAdapter(mAdapter);
        mContentHvp.setCurrentItem(currentPosition);
        mToolbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                hiddenTitleBar();
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(cn.bingoogolapple.photopicker.R.menu.bga_pp_menu_photo_preview, menu);
        MenuItem menuItem = menu.findItem(cn.bingoogolapple.photopicker.R.id.item_photo_preview_title);
        View actionView = menuItem.getActionView();

        mTitleTv = (TextView) actionView.findViewById(cn.bingoogolapple.photopicker.R.id.tv_photo_preview_title);
        mDownloadIv = (ImageView) actionView.findViewById(cn.bingoogolapple.photopicker.R.id.iv_photo_preview_download);
        if (getIntent().getBooleanExtra("isShowDownLoad", false)) {
            mDownloadIv.setOnClickListener(new BGAOnNoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    if (mSavePhotoTask == null) {
                        savePic();
                    }
                }
            });
        } else {
            mDownloadIv.setVisibility(View.GONE);
        }

        renderTitleTv();

        return true;
    }

    private void renderTitleTv() {
        if (mTitleTv == null || mAdapter == null) {
            return;
        }

        if (mediaInfos.size() == 1) {
            mTitleTv.setText("1/1");
        } else {
            mTitleTv.setText((mContentHvp.getCurrentItem() + 1) + "/" + mAdapter.getCount());
        }
    }

    @Override
    public void onViewTap(View view, float x, float y) {
        if (System.currentTimeMillis() - mLastShowHiddenTime > 500) {
            mLastShowHiddenTime = System.currentTimeMillis();
            if (mIsHidden) {
                showTitleBar();
            } else {
                hiddenTitleBar();
            }
        }
    }

    private void showTitleBar() {
        if (mToolbar != null) {
            ViewCompat.animate(mToolbar).translationY(0).setInterpolator(new DecelerateInterpolator(2)).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    mIsHidden = false;
                }
            }).start();
        }
    }

    private void hiddenTitleBar() {
        if (mToolbar != null) {
            ViewCompat.animate(mToolbar).translationY(-mToolbar.getHeight()).setInterpolator(new DecelerateInterpolator(2)).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    mIsHidden = true;
                }
            }).start();
        }
    }

    File mSaveImgDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), Constants.PHOTO_DOWNLOAD);

    private synchronized void savePic() {

        MediaInfo mediaInfo = mediaInfos.get(mContentHvp.getCurrentItem());
        String url = mediaInfo.getUrl();
        DownloadInfo downloadInfo = downloadManager.getDownloadById(url.hashCode());

        if (FileUtil.checkSdCard()) {
            if (!mSaveImgDir.exists()) {
                mSaveImgDir.mkdirs();
            }
            String path = mSaveImgDir.getAbsolutePath().concat("/").concat(mediaInfo.getName());
            File localFile = new File(path);

            if (localFile.exists() && localFile.isFile() && downloadInfo != null) {
                switch (downloadInfo.getStatus()) {
                    case DownloadInfo.STATUS_NONE:
                    case DownloadInfo.STATUS_PAUSED:
                    case DownloadInfo.STATUS_ERROR:
                        downloadManager.resume(downloadInfo);
                        break;
                    case DownloadInfo.STATUS_DOWNLOADING:
                    case DownloadInfo.STATUS_PREPARE_DOWNLOAD:
                    case STATUS_WAIT:
                        downloadManager.pause(downloadInfo);
                        break;
                    case DownloadInfo.STATUS_COMPLETED:
                        ToastUtil.showToast(BGAPhotoPreviewActivity.this, "您已经保存过该照片");
                        break;
                }
            } else {
                if (downloadManager.findAllDownloading().size() > 10) {
                    ToastUtil.showToast(BGAPhotoPreviewActivity.this, "下载任务最多10个,请稍后下载");
                    if (downloadManager.findAllDownloaded().size() > 20) {
                        FragmentDialog.newInstance(false, "已下载任务最多20个，请清除掉一些吧", "", "确定", "取消", "", "", false, new FragmentDialog.OnClickBottomListener() {
                            @Override
                            public void onPositiveClick(Dialog dialog, boolean needDelete) {
                                Intent photoIntent = new Intent();
                                photoIntent.putExtra(Constants.DOWN_LOAD_TYPE, Constants.PHOTO_ALBUM);
                                photoIntent.setClass(BGAPhotoPreviewActivity.this, DownloadManagerActivity.class);
                                startActivity(photoIntent);
                                dialog.dismiss();
                            }

                            @Override
                            public void onNegtiveClick(Dialog dialog) {
                                dialog.dismiss();
                            }
                        }).show(getSupportFragmentManager(), "");
                    }
                } else {
                    createDownload(mediaInfo, url);
                }
            }
        }

    }

    @Override
    public void onPostExecute(Void aVoid) {
        mSavePhotoTask = null;
    }

    @Override
    public void onTaskCancelled() {
        mSavePhotoTask = null;
    }

    @Override
    protected void onDestroy() {
        if (mSavePhotoTask != null) {
            mSavePhotoTask.cancelTask();
            mSavePhotoTask = null;
        }
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    class MyDownloadListener implements DownloadListener {
        DownloadInfo downloadInfo;

        public MyDownloadListener(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onWaited() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onDownloading(long progress, long size) {

        }

        @Override
        public void onRemoved() {
            downloadInfo = null;
        }

        @Override
        public void onDownloadSuccess() {
            mAdapter.notifyDataSetChanged();
            ToastUtil.showToast(BGAPhotoPreviewActivity.this, "图片已保存在" + downloadInfo.getPath() + "文件夹");
        }

        @Override
        public void onDownloadFailed(DownloadException e) {
            ToastUtil.showToast(BGAPhotoPreviewActivity.this, "保存失败，原因是：" + e.getMessage());
        }
    }

    private DownloadInfo createDownload(MediaInfo mediaInfo, String url) {
        DownloadInfo downloadInfo = null;
        if (FileUtil.checkSdCard()) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), Constants.PHOTO_DOWNLOAD);
            if (!file.exists()) {
                file.mkdirs();
            }
            String path = file.getAbsolutePath().concat("/").concat(mediaInfo.getName());
            File localFile = new File(path);

            if (localFile.isFile() && localFile.exists()) {
                file.delete();
            }
            downloadInfo = new DownloadInfo.Builder().setUrl(url).setPath(path).build();
            downloadInfo.setDownloadListener(new BGAPhotoPreviewActivity.MyDownloadListener(downloadInfo));
            downloadManager.download(downloadInfo);
            //save extra info to my database.
            MediaInfoLocal myBusinessInfLocal = new MediaInfoLocal(
                    mediaInfo.getUrl().hashCode(), mediaInfo.getName(),
                    mediaInfo.getIcon(), mediaInfo.getUrl(), mediaInfo.getType(), mediaInfo.getTitle(), mediaInfo.getLocalPath());
            try {
                dbController.createOrUpdateMyDownloadInfo(myBusinessInfLocal);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return downloadInfo;
    }
}