package com.act.quzhibo.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.Toggle;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.OkHttpClientManager;
import com.act.quzhibo.bean.CardBean;
import com.act.quzhibo.bean.InterestPlates;
import com.act.quzhibo.bean.InterestPlatesParentData;
import com.act.quzhibo.bean.MyPost;
import com.act.quzhibo.bean.RecordVideoEvent;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.luban_compress.Luban;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.widget.TitleBarView;
import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.listener.CustomListener;
import com.mabeijianxi.smallvideorecord2.MediaRecorderActivity;
import com.mabeijianxi.smallvideorecord2.model.MediaRecorderConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGASortableNinePhotoLayout;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;
import me.leefeng.promptlibrary.PromptButton;
import me.leefeng.promptlibrary.PromptButtonListener;
import me.leefeng.promptlibrary.PromptDialog;
import permission.auron.com.marshmallowpermissionhelper.ActivityManagePermission;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PostAddActivity extends ActivityManagePermission implements BGASortableNinePhotoLayout.Delegate, View.OnClickListener {
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final int REQUEST_CODE_PHOTO_PREVIEW = 2;
    public static final String EXTRA_MOMENT = "EXTRA_MOMENT";
    private BGASortableNinePhotoLayout mPhotosSnpl;
    private EditText mContentEt;
    private EditText mTitleEt;
    private MyPost myPost;
    private ImageView videoThumb;
    private int postType = 0;
    private PromptDialog promptDialog;
    private String videoUrl = "";
    private OptionsPickerView interestPlatesOption;
    private TextView plateText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_add);
        promptDialog = new PromptDialog(PostAddActivity.this);
        postType = getIntent().getIntExtra("postType", 0);
        mTitleEt = (EditText) findViewById(R.id.et_moment_title);
        mContentEt = (EditText) findViewById(R.id.et_moment_add_content);
        plateText = (TextView) findViewById(R.id.plateText);
        mPhotosSnpl = (BGASortableNinePhotoLayout) findViewById(R.id.snpl_moment_add_photos);
        mPhotosSnpl.setMaxItemCount(9);
        mPhotosSnpl.setEditable(true);
        mPhotosSnpl.setPlusEnable(true);
        mPhotosSnpl.setSortable(true);
        mPhotosSnpl.setDelegate(this);
        TitleBarView titlebar = (TitleBarView) findViewById(R.id.titlebar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        videoThumb = (ImageView) findViewById(R.id.video_player);
        titlebar.setBarTitle("发 表 状 态");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostAddActivity.this.finish();
            }
        });
        findViewById(R.id.tv_moment_add_publish).setOnClickListener(this);
        findViewById(R.id.recordBtn).setOnClickListener(this);

        switch (postType) {
            case 1:
                mPhotosSnpl.setVisibility(View.GONE);
                break;
            case 2:
                record();
                findViewById(R.id.recordBtn).setVisibility(View.VISIBLE);
                break;
            case 3:
                mPhotosSnpl.setVisibility(View.VISIBLE);
                break;
        }
        EventBus.getDefault().register(this);
        myPost = new MyPost();
        getInterestPlatesData();

    }

    private ArrayList<CardBean> interestPlatesItems = new ArrayList<>();

    private void getInterestPlatesData() {
        BmobQuery<Toggle> query = new BmobQuery<>();
        query.findObjects(new FindListener<Toggle>() {
            @Override
            public void done(List<Toggle> toggles, BmobException e) {
                if (e == null && toggles.size() > 0) {
                    String url =toggles.get(0).squareInterestTab;
                    OkHttpClientManager.parseRequest(PostAddActivity.this, url, handler, Constants.REFRESH);
                }else{
                    handler.sendEmptyMessage(Constants.NetWorkError);
                }
            }
        });

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            InterestPlatesParentData interestPlatesParentData = CommonUtil.parseJsonWithGson((String) msg.obj, InterestPlatesParentData.class);
            for (InterestPlates plate : interestPlatesParentData.result.plates) {
                if (!plate.pName.contains("视频")) {
                    interestPlatesItems.add(new CardBean(plate.pName));
                }
            }
            initDisPurposeOptionPicker();
            plateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    interestPlatesOption.show();
                }
            });
        }
    };

    private void initDisPurposeOptionPicker() {
        interestPlatesOption = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                plateText.setText(interestPlatesItems.get(options1).getPickerViewText());

            }
        }).setDividerColor(Color.BLACK).setTitleText("版块选择").setTextColorCenter(Color.BLACK).setContentTextSize(22).setLayoutRes(R.layout.pickerview_custom_options, new CustomListener() {
            @Override
            public void customLayout(View v) {
                final TextView tvSubmit = (TextView) v.findViewById(R.id.tv_finish);
                TextView ivCancel = (TextView) v.findViewById(R.id.tv_cancel);
                tvSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        interestPlatesOption.returnData();
                        interestPlatesOption.dismiss();
                    }
                });
                ivCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        interestPlatesOption.dismiss();
                    }
                });
            }
        }).isDialog(true).build();
        interestPlatesOption.setPicker(interestPlatesItems);//添加数据
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe
    public void onEventMainThread(final RecordVideoEvent event) {
        findViewById(R.id.videoLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.recordBtn).setVisibility(View.GONE);
        videoUrl = event.videoUri;
        videoThumb.setImageBitmap(BitmapFactory.decodeFile(event.videoScreenshot));
        findViewById(R.id.videoLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(event.videoUri);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(PostAddActivity.this, getApplicationContext().getPackageName() + ".FileProvider", file);
                    intent.setDataAndType(contentUri, "video/*");
                } else {
                    uri = Uri.fromFile(file);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(uri, "video/*");
                }
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClickAddNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, ArrayList<String> models) {
        File takePhotoDir = new File(Environment.getExternalStorageDirectory(), "QuPhotoPickerTakePhoto");
        if (!takePhotoDir.exists()) {
            takePhotoDir.mkdirs();
        }
        startActivityForResult(BGAPhotoPickerActivity.newIntent(PostAddActivity.this, takePhotoDir, mPhotosSnpl.getMaxItemCount() - mPhotosSnpl.getItemCount(), null, false), REQUEST_CODE_CHOOSE_PHOTO);
    }

    @Override
    public void onClickDeleteNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        mPhotosSnpl.removeItem(position);
    }

    @Override
    public void onClickNinePhotoItem(BGASortableNinePhotoLayout sortableNinePhotoLayout, View view, int position, String model, ArrayList<String> models) {
        startActivityForResult(BGAPhotoPickerPreviewActivity.newIntent(this, mPhotosSnpl.getMaxItemCount(), models, models, position, false), REQUEST_CODE_PHOTO_PREVIEW);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
            ArrayList<File> fileList = new ArrayList<>();
            if (BGAPhotoPickerActivity.getSelectedImages(data) != null)
                for (String path : BGAPhotoPickerActivity.getSelectedImages(data)) {
                    fileList.add(new File(path));
                }
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.show();
            Luban.get(this)
                    .load(fileList)
                    .putGear(Luban.THIRD_GEAR)
                    .asList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    })
                    .onErrorResumeNext(new Func1<Throwable, Observable<? extends List<File>>>() {
                        @Override
                        public Observable<? extends List<File>> call(Throwable throwable) {
                            return Observable.empty();
                        }
                    })
                    .subscribe(new Action1<List<File>>() {
                        @Override
                        public void call(List<File> fileList) {
                            ArrayList<String> filePaths = new ArrayList<>();
                            for (int i = 0; i < fileList.size(); i++) {
                                filePaths.add(fileList.get(i).getAbsolutePath());
                            }
                            mPhotosSnpl.addMoreData(filePaths);
                        }
                    });

            dialog.dismiss();
        } else if (requestCode == REQUEST_CODE_PHOTO_PREVIEW) {
            mPhotosSnpl.setData(BGAPhotoPickerPreviewActivity.getSelectedImages(data));
        }
    }

    private void record() {

        promptDialog.getAlertDefaultBuilder().sheetCellPad(5).round(10);
        PromptButton cancle = new PromptButton("取消", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton promptButton) {
                promptDialog.dismiss();
            }
        });

        PromptButton btnCarema = new PromptButton("录制视频", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton promptButton) {
                MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
                        .fullScreen(true)
                        .smallVideoWidth(360)
                        .smallVideoHeight(480)
                        .recordTimeMax(8000)
                        .recordTimeMin(1500)
                        .maxFrameRate(20)
                        .videoBitrate(600000)
                        .captureThumbnailsTime(3)
                        .build();
                MediaRecorderActivity.goSmallVideoRecorder(PostAddActivity.this, RecordConfirmActivity.class.getName(), config);
            }
        });
        PromptButton btnPhoto = new PromptButton("选取本地视频", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton promptButton) {
                promptDialog.dismiss();
            }
        });
        btnPhoto.setTextColor(Color.parseColor("#D9D9D9"));
        cancle.setTextColor(Color.parseColor("#0076ff"));
        promptDialog.showAlertSheet("", true, cancle, btnCarema, btnPhoto);
    }

    @Override
    public void onClick(View view) {
        final String content = mContentEt.getText().toString().trim();
        final String title = mTitleEt.getText().toString().trim();
        if (view.getId() == R.id.recordBtn) {
            record();
        }

        if (view.getId() == R.id.tv_moment_add_publish) {
            if (title.length() == 0) {
                Toast.makeText(this, "必须填写这一刻想法的标题！", Toast.LENGTH_SHORT).show();
                return;
            } else if (content.length() == 0) {
                Toast.makeText(this, "必须填写这一刻的想法！", Toast.LENGTH_SHORT).show();
                return;
            } else if (plateText.getText().toString().equals("选择状态版块")) {
                Toast.makeText(this, "必须选择版块！", Toast.LENGTH_SHORT).show();
                return;
            } else {
                promptDialog.showLoading("正在发布");
            }
            myPost.type = postType + "";
            if (postType == 1) {
                myPost.title = title;
                myPost.absText = content;
                myPost.pageView = "0";
                myPost.totalComments = "0";
                myPost.totalImages = "0";
                myPost.rewards = "0";
                myPost.rewards = "0";
                myPost.user = BmobUser.getCurrentUser(RootUser.class);
                myPost.save(new SaveListener<String>() {
                    @Override
                    public void done(String objectId, BmobException e) {
                        promptDialog.dismiss();
                        if (e == null) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_MOMENT, myPost);
                            setResult(RESULT_OK, intent);
                            finish();
                            promptDialog.showSuccess("发布成功,小编审核中", true);
                        } else {
                            promptDialog.showError("发布失败", true);
                        }
                    }
                });
            } else if (postType == 3) {
                final String[] filePaths = mPhotosSnpl.getData().toArray(new String[mPhotosSnpl.getData().size()]);
                BmobFile.uploadBatch(filePaths, new UploadBatchListener() {
                    @Override
                    public void onSuccess(List<BmobFile> files, List<String> urls) {
                        if (urls.size() == filePaths.length) {
                            myPost.images = new ArrayList<>();
                            myPost.images.addAll(urls);
                            myPost.title = title;
                            myPost.absText = content;
                            myPost.pageView = "0";
                            myPost.totalComments = "0";
                            myPost.totalImages = urls.size() + "";
                            myPost.rewards = "0";
                            myPost.user = BmobUser.getCurrentUser(RootUser.class);
                            myPost.save(new SaveListener<String>() {
                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        promptDialog.dismiss();
                                        Intent intent = new Intent();
                                        intent.putExtra(EXTRA_MOMENT, myPost);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                        promptDialog.showSuccess("发布成功，小编审核中", true);
                                    } else {
                                        promptDialog.showError("发布失败", true);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int statuscode, String errormsg) {
                        promptDialog.showError("发布状态错误", true);
                    }

                    @Override
                    public void onProgress(int curIndex, int curPercent, int total, int totalPercent) {
                    }
                });
            } else if (postType == 2) {
                final String[] filePaths = new String[]{videoUrl};
                BmobFile.uploadBatch(filePaths, new UploadBatchListener() {
                    @Override
                    public void onSuccess(List<BmobFile> files, List<String> urls) {
                        if (urls.size() == filePaths.length) {
                            myPost.title = title;
                            myPost.vedioUrl = urls.get(0);
                            myPost.absText = content;
                            myPost.pageView = "0";
                            myPost.totalComments = "0";
                            myPost.totalImages = "0";
                            myPost.rewards = "0";
                            myPost.user = BmobUser.getCurrentUser(RootUser.class);
                            myPost.save(new SaveListener<String>() {
                                @Override
                                public void done(String objectId, BmobException e) {
                                    if (e == null) {
                                        Intent intent = new Intent();
                                        intent.putExtra(EXTRA_MOMENT, myPost);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                        promptDialog.showSuccess("发布成功，小编审核中", true);
                                        promptDialog.dismiss();
                                    } else {
                                        promptDialog.showError("发布失败", true);
                                        promptDialog.dismiss();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(int statuscode, String errormsg) {
                        promptDialog.showError("发布失败", true);
                        promptDialog.dismiss();
                    }

                    @Override
                    public void onProgress(int curIndex, int curPercent, int total, int totalPercent) {
                        promptDialog.showLoading("正在发布", true);
                        if (totalPercent == 100) {
                            promptDialog.dismiss();
                        }
                    }
                });
            }
        }

    }
}

