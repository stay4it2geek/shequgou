package com.act.quzhibo.ui.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.act.quzhibo.BuildConfig;
import com.act.quzhibo.R;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.download.activity.DownloadManagerActivity;
import com.act.quzhibo.luban_compress.Luban;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.FileUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.CircleImageView;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.SelfDialog;
import com.bumptech.glide.Glide;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import me.leefeng.promptlibrary.PromptButton;
import me.leefeng.promptlibrary.PromptButtonListener;
import me.leefeng.promptlibrary.PromptDialog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MineActivity extends BaseActivity {

    static final int REQUEST_CAPTURE = 100;
    static final int REQUEST_PICK = 101;
    static final int REQUEST_CROP_PHOTO = 102;
    static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 103;
    static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 104;
    File tempFile;
    RootUser rootUser;
    PromptDialog promptDialog;
    @Bind(R.id.myfocus_shower)
    RelativeLayout myfocus_shower;
    @Bind(R.id.myfocus_person)
    RelativeLayout myfocus_person;
    @Bind(R.id.who_see_me)
    RelativeLayout who_see_me;
    @Bind(R.id.myPostlayout)
    RelativeLayout myPostlayout;
    @Bind(R.id.myVideo_download_layout)
    RelativeLayout myVideo_download_layout;
    @Bind(R.id.registerLayout)
    RelativeLayout registerLayout;
    @Bind(R.id.logout)
    RelativeLayout logout;
    @Bind(R.id.loginLayout)
    RelativeLayout loginLayout;
    @Bind(R.id.nickName)
    TextView nickName;
    @Bind(R.id.sexAndAge)
    TextView sexAndAge;
    @Bind(R.id.circleAvatar)
    CircleImageView circleAvatar;
    @Bind(R.id.uploadImgText)
    TextView uploadImgText;
    @Bind(R.id.vipTagImg)
    ImageView vipTagImg;
    SelfDialog selfDialog;


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && !selfDialog.isShowing()) {
            selfDialog.show();
        }
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        promptDialog = new PromptDialog(MineActivity.this);
        selfDialog = new SelfDialog(MineActivity.this, false);
        selfDialog.setTitle("客官再看一会儿呗");
        selfDialog.setMessage("还是留下来再看看吧");
        selfDialog.setYesOnclickListener("再欣赏下", new SelfDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                selfDialog.dismiss();

            }
        });
        selfDialog.setNoOnclickListener("有事要忙", new SelfDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                RootUser user = new RootUser();
                if (user != null) {
                    user.lastLoginTime = System.currentTimeMillis() + "";
                    user.update(BmobUser.getCurrentUser(RootUser.class).getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                selfDialog.dismiss();
                                ToastUtil.showToast(MineActivity.this, "退出时间" + System.currentTimeMillis());
                                finish();
                            }
                        }
                    });
                } else {
                    finish();
                }
            }
        });
    }

    @OnClick({R.id.circleAvatar,R.id.videofocusshower, R.id.avaterlayout,
            R.id.loginLayout, R.id.uploadImgText, R.id.getVipLayout,
            R.id.vip_order_listlayout, R.id.who_see_me, R.id.who_focus_me,
            R.id.myfocus_person, R.id.myfocus_shower, R.id.settingDetailayout,
            R.id.myVideo_download_layout, R.id.myPostlayout, R.id.logout, R.id.registerLayout})
    public void buttonClicks(final View view) {
        if (view.getId() == R.id.registerLayout) {
            startActivity(view, RegisterNormalActivity.class);
            return;
        } else if (view.getId() == R.id.getVipLayout) {
            startActivity(view, GetVipPayActivity.class);
            return;
        } else {
            if (rootUser == null) {
                if (R.id.avaterlayout == view.getId() ||
                        R.id.circleAvatar == view.getId() ||
                        R.id.uploadImgText == view.getId()) {
                    startActivity(new Intent(MineActivity.this, LoginActivity.class));
                } else {
                    startActivity(view, LoginActivity.class);
                }
                return;
            } else {
                if (R.id.avaterlayout == view.getId() ||
                        R.id.circleAvatar == view.getId() ||
                        R.id.uploadImgText == view.getId()) {
                    FragmentDialog.newInstance(false, "是否上传头像？", "亲，真的想要替换吗",
                            "我要替换", "取消替换", "", "", false, new FragmentDialog.OnClickBottomListener() {
                                @Override
                                public void onPositiveClick(Dialog dialog, boolean deleteFileSource) {
                                    dialog.dismiss();
                                    uploadHeadImage();
                                }

                                @Override
                                public void onNegtiveClick(Dialog dialog) {
                                    dialog.dismiss();
                                }
                            }).show(getSupportFragmentManager(), "");

                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.setBackgroundColor(getResources().getColor(R.color.colorbg));
                            view.setBackgroundColor(getResources().getColor(R.color.white));
                            switch (view.getId()) {
                                case R.id.videofocusshower:
                                    startActivity(new Intent(MineActivity.this, MyFocusGirlsActvity.class));
                                    break;
                                case R.id.who_see_me:
                                    Intent seeIntent = new Intent(MineActivity.this, WhoLikeThenSeeMeActivity.class);
                                    seeIntent.putExtra("userType", Constants.SEE_ME_FLAG);
                                    startActivity(seeIntent);
                                    break;
                                case R.id.who_focus_me:
                                    Intent focusIntent = new Intent(MineActivity.this, WhoLikeThenSeeMeActivity.class);
                                    focusIntent.putExtra("userType", Constants.FOCUS_ME_FLAG);
                                    startActivity(focusIntent);
                                    break;
                                case R.id.myfocus_shower:
                                    startActivity(new Intent(MineActivity.this, MyFocusShowerActivity.class));
                                    break;
                                case R.id.myfocus_person:
                                    startActivity(new Intent(MineActivity.this, MyFocusPersonActivity.class));
                                    break;
                                case R.id.myVideo_download_layout:
                                    Intent videoIntent = new Intent();
                                    videoIntent.putExtra(Constants.DOWN_LOAD_TYPE, Constants.VIDEO_ALBUM);
                                    videoIntent.setClass(MineActivity.this, DownloadManagerActivity.class);
                                    startActivity(videoIntent);
                                    break;
                                case R.id.settingDetailayout:
                                    startActivity(new Intent(MineActivity.this, SettingMineInfoActivity.class));
                                    break;
                                case R.id.myPostlayout:
                                    startActivity(new Intent(MineActivity.this, MyPostListActivity.class));
                                    break;
                                case R.id.logout:
                                    rootUser.logOut();
                                    startActivity(new Intent(MineActivity.this, LoginActivity.class));
                                    break;

                                default:
                                    break;
                            }
                        }
                    }, 300);
                }
            }

        }
    }


    <T> void startActivity(final View view, Class<T> activity) {
        startActivity(new Intent(MineActivity.this, activity));
        view.setBackgroundColor(getResources().getColor(R.color.colorbg));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setBackgroundColor(getResources().getColor(R.color.white));
            }
        }, 300);
    }


    @Override
    public void onResume() {
        super.onResume();
        rootUser = BmobUser.getCurrentUser(RootUser.class);
        if (rootUser != null) {
            CommonUtil.fecth(MineActivity.this);
            registerLayout.setVisibility(View.GONE);
            logout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            nickName.setText(TextUtils.isEmpty(rootUser.getUsername()) ? "未设置昵称" : rootUser.getUsername());
            sexAndAge.setText(TextUtils.isEmpty(rootUser.sex) ? "性别" : rootUser.sex + "性" + "/" + (TextUtils.isEmpty(rootUser.age) ? "年龄" : rootUser.age + "岁"));
            if (rootUser.isVip) {
                vipTagImg.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(rootUser.photoFileUrl)) {
                uploadImgText.setVisibility(View.GONE);
                Glide.with(MineActivity.this).load(rootUser.photoFileUrl).skipMemoryCache(true).into(circleAvatar);
            } else {
                circleAvatar.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.default_head));
            }
        } else {
            uploadImgText.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.VISIBLE);
            registerLayout.setVisibility(View.VISIBLE);
            logout.setVisibility(View.GONE);
            nickName.setText("未设置昵称");
            sexAndAge.setText("性别/年龄");
        }
    }


    void uploadHeadImage() {
        promptDialog.getAlertDefaultBuilder().sheetCellPad(5).round(10);
        PromptButton cancle = new PromptButton("取消", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton promptButton) {
                promptDialog.dismiss();
            }
        });

        PromptButton btnCarema = new PromptButton("拍照", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton promptButton) {
                if (ContextCompat.checkSelfPermission(MineActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MineActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {
                    gotoCamera();
                }
            }
        });
        PromptButton btnPhoto = new PromptButton("从相册选取", new PromptButtonListener() {
            @Override
            public void onClick(PromptButton promptButton) {

                if (ContextCompat.checkSelfPermission(MineActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MineActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {

                    gotoPhoto();
                }
            }
        });
        cancle.setTextColor(Color.parseColor("#0076ff"));
        promptDialog.showAlertSheet("", true, cancle, btnCarema, btnPhoto);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotoCamera();
            }
        } else if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotoPhoto();
            }
        }
    }


    void gotoPhoto() {
        //跳转到调用系统图库
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "请选择图片"), REQUEST_PICK);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_CAPTURE: //调用系统相机返回
                if (resultCode == RESULT_OK) {
                    gotoClipActivity(Uri.fromFile(tempFile));
                }
                break;
            case REQUEST_PICK:  //调用系统相册返回
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    gotoClipActivity(uri);
                }
                break;
            case REQUEST_CROP_PHOTO:  //剪切图片返回
                if (resultCode == RESULT_OK) {
                    final Uri uri = intent.getData();
                    if (uri == null) {
                        return;
                    }
                    String cropImagePath = FileUtil.getRealFilePathFromUri(getApplicationContext(), uri);
                    promptDialog.showLoading("正在上传", true);
                    Luban.get(MineActivity.this)
                            .load(new File(cropImagePath))
                            .putGear(Luban.THIRD_GEAR)
                            .asObservable()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnError(new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    throwable.printStackTrace();
                                }
                            })
                            .onErrorResumeNext(new Func1<Throwable, Observable<? extends File>>() {
                                @Override
                                public Observable<? extends File> call(Throwable throwable) {
                                    return Observable.empty();
                                }
                            })
                            .subscribe(new Action1<File>() {
                                @Override
                                public void call(File file) {
                                    if (file != null) {
                                        Glide.with(MineActivity.this).load(file.getAbsolutePath()).into(circleAvatar);
                                        uploadImgText.setVisibility(View.GONE);
                                        final BmobFile bmobFile = new BmobFile(new File(file.getAbsolutePath()));
                                        if (!TextUtils.isEmpty(rootUser.photoFileUrl)) {
                                            final BmobFile bmobOldFile = new BmobFile();
                                            bmobOldFile.setUrl(rootUser.photoFileUrl);
                                            bmobOldFile.delete(new UpdateListener() {
                                                @Override
                                                public void done(BmobException e) {
                                                    if (e == null) {
                                                        uploadAvaterBlock(bmobFile);
                                                    } else {
                                                        promptDialog.showError("源头像删除失败", true);
                                                    }
                                                }
                                            });

                                        } else {
                                            uploadAvaterBlock(bmobFile);
                                        }
                                    }
                                }
                            });
                }
                break;
        }
    }

    /**
     * 上传头像
     *
     * @param bmobFile
     */
    private void uploadAvaterBlock(final BmobFile bmobFile) {
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    RootUser updateUser = new RootUser();
                    updateUser.photoFileUrl = bmobFile.getFileUrl();
                    updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                CommonUtil.fecth(MineActivity.this);
                                promptDialog.showSuccess("头像上传成功", true);
                            } else {
                                promptDialog.showError("头像上传失败", true);
                            }
                        }
                    });
                } else {
                    promptDialog.showError("头像上传失败", true);
                }
                promptDialog.dismiss();
            }

            @Override
            public void onProgress(Integer value) {

            }
        });
    }

    public void gotoClipActivity(Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(MineActivity.this, ClipImageActivity.class);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }

    void gotoCamera() {
        //创建拍照存储的图片文件
        tempFile = new File(FileUtil.checkDirPath(Environment.getExternalStorageDirectory().getPath() + "/image/"), System.currentTimeMillis() + ".jpg");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //设置7.0中共享文件，分享路径定义在xml/file_paths.xml
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(MineActivity.this, BuildConfig.APPLICATION_ID + ".FileProvider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        }
        startActivityForResult(intent, REQUEST_CAPTURE);
    }


}
