package com.act.quzhibo.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.act.quzhibo.R;
import com.act.quzhibo.bean.CardBean;
import com.act.quzhibo.bean.RootUser;
import com.act.quzhibo.lock_view.LockIndicatorView;
import com.act.quzhibo.lock_view.LockViewGroup;
import com.act.quzhibo.util.CommonUtil;
import com.act.quzhibo.util.FileUtil;
import com.act.quzhibo.util.ToastUtil;
import com.act.quzhibo.widget.FragmentDialog;
import com.act.quzhibo.widget.TitleBarView;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.listener.CustomListener;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class SettingMineInfoActivity extends BaseActivity {

    OptionsPickerView ageOptions;
    OptionsPickerView sexOptions;
    RootUser rootUser = BmobUser.getCurrentUser(RootUser.class);
    RootUser updateUser = new RootUser();
    ArrayList<CardBean> ageItems = new ArrayList<>();
    ArrayList<CardBean> sexItems = new ArrayList<>();
    ArrayList<CardBean> candateThingiItems = new ArrayList<>();
    ArrayList<CardBean> disPurposeItems = new ArrayList<>();
    ArrayList<CardBean> disMariStateItems = new ArrayList<>();

    OptionsPickerView disPurposeOptions;
    OptionsPickerView disMariStateOption;
    OptionsPickerView candateThingOptions;

    @Bind(R.id.sex_rl)
    RelativeLayout sex_rl;
    @Bind(R.id.backbuttonLayout)
    RelativeLayout backbuttonLayout;
    @Bind(R.id.next)
    TextView nextButton;
    @Bind(R.id.age_txt)
    TextView age_txt;
    @Bind(R.id.openSecret_switch)
    CheckBox openSecret_switch;
    @Bind(R.id.openSecret_txt)
    TextView openSecret_txt;
    @Bind(R.id.sex_txt)
    TextView sex_txt;
    @Bind(R.id.disMariState_txt)
    TextView disMariState_txt;
    @Bind(R.id.disPurpose_txt)
    TextView disPurpose_txt;
    @Bind(R.id.candateThing_txt)
    TextView candateThing_txt;
    @Bind(R.id.arealocation_rl)
    RelativeLayout arealocation_rl;
    @Bind(R.id.arealocation_txt)
    TextView arealocation_txt;
    @Bind(R.id.secret_view)
    LinearLayout secretView;
    @Bind(R.id.indicator)
    LockIndicatorView mLockIndicator;
    @Bind(R.id.tv_tips)
    TextView mTvTips;
    @Bind(R.id.lockgroup)
    LockViewGroup mLockViewGroup;
    @Bind(R.id.titlebar)
    TitleBarView titlebar;
    AMapLocationClient locationClient;
    AMapLocationClientOption locationOption = new AMapLocationClientOption();
    AMapLocation amlocation;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_mine_info);
        titlebar.setBarTitle("资 料 设 置");
        titlebar.setBackButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSexData();
        getAgeData();
        getdisPurposeData();
        getdisMariStateData();
        getCanDatingThingData();
        initSexOptionPicker();
        initAgeOptionPicker();
        initCanDatingThingOptionPicker();
        initdisPurposeOptionPicker();
        initdisMariStateOptionPicker();
        initLockLogic();
        initLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (rootUser != null) {
            CommonUtil.fecth(this);
            openSecret_switch.setChecked(rootUser.secretScan);
            sex_txt.setText(TextUtils.isEmpty(rootUser.sex) ? "您的性别未设置" : "您的性别是" + rootUser.sex + "性");
            if (!TextUtils.isEmpty(rootUser.sex)) {
                sex_txt.setTextColor(Color.LTGRAY);
                sex_rl.setVisibility(View.GONE);
            }
            openSecret_txt.setText(rootUser.secretScan ? "私密访问已开启" : "私密访问未开启");
            age_txt.setText(TextUtils.isEmpty(rootUser.age) ? "年龄未设置" : "您的年龄是" + rootUser.age + "岁");
            disMariState_txt.setText(TextUtils.isEmpty(rootUser.disMariState) ? "情感状态未设置" : "您现在是" + rootUser.disMariState);
            disPurpose_txt.setText(TextUtils.isEmpty(rootUser.disPurpose) ? "交友想法未设置" : "您想要" + rootUser.disPurpose);
            candateThing_txt.setText(TextUtils.isEmpty(rootUser.canDateThing) ? "是否可约未设置" : "您可以" + rootUser.canDateThing);
            if (!rootUser.hasSetting) {
                backbuttonLayout.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
            }
        }
    }


    @OnCheckedChanged(R.id.openSecret_switch)
    void onCheckedChanged(boolean isChecked) {
        if (rootUser != null) {
            mLockIndicator.setAnswer(new int[]{});
            if (isChecked) {
                if (!rootUser.secretScan) {
                    mLockViewGroup.setAnswer(null);
                    secretView.setVisibility(View.VISIBLE);
                    secretView.setAnimation(AnimationUtils.makeInAnimation(SettingMineInfoActivity.this, true));
                }
            } else {
                if (secretView.getVisibility() == View.VISIBLE) {
                    secretView.setVisibility(View.INVISIBLE);
                    secretView.setAnimation(AnimationUtils.makeOutAnimation(SettingMineInfoActivity.this, true));
                }
                updateUser.secretScan = false;
                updateUser.secretPassword = "";
                updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            CommonUtil.fecth(SettingMineInfoActivity.this);
                            ToastUtil.showToast(SettingMineInfoActivity.this, "私密访问已关闭");
                            rootUser = BmobUser.getCurrentUser(RootUser.class);
                        } else {
                            if (e.getErrorCode() == 206) {
                                tokenOutOfTime();
                            }
                        }
                    }
                });
            }
        }
    }

    @OnClick({R.id.clear_cache_txt, R.id.modifyPSWlayout, R.id.sex_rl, R.id.disMariState_txt,
            R.id.disPurpose_txt, R.id.candateThing_txt, R.id.next})
    void buttonClicks(View view) {
        switch (view.getId()) {
            case R.id.clear_cache_txt:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(SettingMineInfoActivity.this).clearMemory();
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(SettingMineInfoActivity.this).clearDiskCache();
                    }
                }).start();
                cleanInternalCache(SettingMineInfoActivity.this);
                cleanExternalCache(SettingMineInfoActivity.this);
                ToastUtil.showToast(SettingMineInfoActivity.this, "清除完成！");
                break;
            case R.id.modifyPSWlayout:
                startActivity(new Intent(SettingMineInfoActivity.this, ResetPasswordActivity.class));
                break;
            case R.id.sex_rl:
                sexOptions.show();
                break;
            case R.id.disMariState_txt:
                disMariStateOption.show();
                break;
            case R.id.candateThing_txt:
                candateThingOptions.show();
                break;
            case R.id.disPurpose_txt:
                disPurposeOptions.show();
                break;
            case R.id.age_txt:
                ageOptions.show();
                break;
            case R.id.next:
                if (sex_txt.getText().toString().contains("未设置")) {
                    ToastUtil.showToast(SettingMineInfoActivity.this, "必须设置性别才能查看更多画面哦，老司机才懂的！");
                } else if (
                        age_txt.getText().toString().contains("未设置")) {
                    ToastUtil.showToast(SettingMineInfoActivity.this, "必须设置年龄才能查看更多画面哦，老司机才懂的！");
                } else if (
                        disMariState_txt.getText().toString().contains("未设置")) {
                    ToastUtil.showToast(SettingMineInfoActivity.this, "必须设置情感状态才能查看更多画面哦，老司机才懂的！");
                } else if (
                        disPurpose_txt.getText().toString().contains("未设置")) {
                    ToastUtil.showToast(SettingMineInfoActivity.this, "必须设置交友想法才能查看更多画面哦，老司机才懂的！");
                } else if (
                        candateThing_txt.getText().toString().contains("未设置")) {
                    ToastUtil.showToast(SettingMineInfoActivity.this, "必须设置是否可约才能查看更多画面哦，老司机才懂的！");
                } else {
                    updateUser.hasSetting = true;
                    updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                finish();
                            }
                        }
                    });
                }
                break;
        }
    }


    void tokenOutOfTime() {
        FragmentDialog.newInstance(false, "权限确认", "缓存即将过期，请退出重新登录后修改", "去登录", "取消修改", "", "", false, new FragmentDialog.OnClickBottomListener() {
            @Override
            public void onPositiveClick(Dialog dialog, boolean needDelete) {
                rootUser.logOut();
                dialog.dismiss();
                SettingMineInfoActivity.this.finish();
                startActivity(new Intent(SettingMineInfoActivity.this, LoginActivity.class));
            }

            @Override
            public void onNegtiveClick(Dialog dialog) {
                dialog.dismiss();
            }
        }).show(getSupportFragmentManager(), "");
    }

    /**
     * * 清除本应用内部缓存(/data/data/com.xxx.xxx/cache) * *
     *
     * @param context
     */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }

    /**
     * * 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
     *
     * @param context
     */
    public static void cleanExternalCache(Context context) {
        if (FileUtil.checkSdCard()) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }


    /**
     * * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理 * *
     *
     * @param directory
     */
    static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    void initAgeOptionPicker() {
        ageOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                age_txt.setText("您的年龄是" + ageItems.get(options1).getPickerViewText() + "岁");
                if (rootUser != null) {
                    updateUser.age = ageItems.get(options1).getPickerViewText() + "";
                    updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                CommonUtil.fecth(SettingMineInfoActivity.this);
                                ToastUtil.showToast(SettingMineInfoActivity.this, "年龄更新成功");
                            } else {
                                if (e.getErrorCode() == 206) {
                                    FragmentDialog.newInstance(false, "权限确认", "缓存已过期，请退出重新登录后修改", "去登录", "取消修改", "", "", false, new FragmentDialog.OnClickBottomListener() {
                                        @Override
                                        public void onPositiveClick(Dialog dialog, boolean needDelete) {
                                            rootUser.logOut();
                                            dialog.dismiss();
                                            SettingMineInfoActivity.this.finish();
                                            startActivity(new Intent(SettingMineInfoActivity.this, LoginActivity.class));
                                        }

                                        @Override
                                        public void onNegtiveClick(Dialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show(getSupportFragmentManager(), "");
                                }
                            }
                        }
                    });
                }
            }
        }).setDividerColor(Color.BLACK)
                .setTitleText("年龄选择")
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(22)
                .setLayoutRes(R.layout.pickerview_custom_options, new CustomListener() {
                    @Override
                    public void customLayout(View v) {
                        v.findViewById(R.id.tv_finish).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ageOptions.returnData();
                                ageOptions.dismiss();
                            }
                        });
                        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ageOptions.dismiss();
                            }
                        });
                    }
                }).isDialog(true).build();
        ageOptions.setPicker(ageItems);
    }

    void initCanDatingThingOptionPicker() {
        candateThingOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                candateThing_txt.setText("您可以" + candateThingiItems.get(options1).getPickerViewText());
                if (rootUser != null) {
                    updateUser.canDateThing = candateThingiItems.get(options1).getPickerViewText() + "";
                    updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                CommonUtil.fecth(SettingMineInfoActivity.this);
                                ToastUtil.showToast(SettingMineInfoActivity.this, "是否可约更新成功");
                            } else {
                                if (e.getErrorCode() == 206) {
                                    FragmentDialog.newInstance(false, "权限确认", "缓存已过期，请退出重新登录后修改", "去登录", "取消修改", "", "", false, new FragmentDialog.OnClickBottomListener() {
                                        @Override
                                        public void onPositiveClick(Dialog dialog, boolean needDelete) {
                                            rootUser.logOut();
                                            dialog.dismiss();
                                            SettingMineInfoActivity.this.finish();
                                            startActivity(new Intent(SettingMineInfoActivity.this, LoginActivity.class));
                                        }

                                        @Override
                                        public void onNegtiveClick(Dialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show(getSupportFragmentManager(), "");
                                }
                            }
                        }
                    });
                }
            }
        }).setDividerColor(Color.BLACK)
                .setTitleText("年龄选择")
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(22)
                .setLayoutRes(R.layout.pickerview_custom_options, new CustomListener() {
                    @Override
                    public void customLayout(View v) {

                        v.findViewById(R.id.tv_finish).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                candateThingOptions.returnData();
                                candateThingOptions.dismiss();
                            }
                        });
                        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                candateThingOptions.dismiss();
                            }
                        });
                    }
                }).isDialog(true).build();
        candateThingOptions.setPicker(candateThingiItems);
    }

    void initSexOptionPicker() {
        sexOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                sex_txt.setText("您的性别是" + sexItems.get(options1).getPickerViewText() + "性");

                if (rootUser != null) {
                    updateUser.sex = sexItems.get(options1).getPickerViewText() + "";
                    updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                sex_txt.setTextColor(Color.LTGRAY);
                                sex_rl.setVisibility(View.GONE);
                                CommonUtil.fecth(SettingMineInfoActivity.this);
                                ToastUtil.showToast(SettingMineInfoActivity.this, rootUser.sex + "性更新成功");
                            } else {
                                if (e.getErrorCode() == 206) {
                                    tokenOutOfTime();
                                }
                            }
                        }
                    });
                }
            }
        }).setDividerColor(Color.BLACK)
                .setTitleText("性别选择")
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(22).
                        setLayoutRes(R.layout.pickerview_custom_options, new CustomListener() {
                            @Override
                            public void customLayout(View v) {
                                v.findViewById(R.id.tv_finish).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sexOptions.returnData();
                                        sexOptions.dismiss();
                                    }
                                });
                                v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sexOptions.dismiss();
                                    }
                                });
                            }
                        }).isDialog(true).build();
        sexOptions.setPicker(sexItems);
    }


    void initdisPurposeOptionPicker() {
        disPurposeOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                disPurpose_txt.setText("交友想法是" + disPurposeItems.get(options1).getPickerViewText());
                if (rootUser != null) {
                    updateUser.disPurpose = disPurposeItems.get(options1).getPickerViewText() + "";
                    updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                CommonUtil.fecth(SettingMineInfoActivity.this);
                                ToastUtil.showToast(SettingMineInfoActivity.this, "交友想法更新成功");
                            } else {
                                if (e.getErrorCode() == 206) {
                                    tokenOutOfTime();
                                }
                            }
                        }
                    });
                }
            }
        }).setDividerColor(Color.BLACK)
                .setTitleText("交友想法选择")
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(22)
                .setLayoutRes(R.layout.pickerview_custom_options, new CustomListener() {
                    @Override
                    public void customLayout(View v) {
                        v.findViewById(R.id.tv_finish).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                disPurposeOptions.returnData();
                                disPurposeOptions.dismiss();
                            }
                        });
                        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                disPurposeOptions.dismiss();
                            }
                        });
                    }
                }).isDialog(true).build();
        disPurposeOptions.setPicker(disPurposeItems);
    }


    void initdisMariStateOptionPicker() {
        disMariStateOption = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                disMariState_txt.setText("您的状态是" + disMariStateItems.get(options1).getPickerViewText());
                if (rootUser != null) {
                    updateUser.disMariState = disMariStateItems.get(options1).getPickerViewText() + "";
                    updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                CommonUtil.fecth(SettingMineInfoActivity.this);
                                ToastUtil.showToast(SettingMineInfoActivity.this, "情感状态更新成功");
                            } else {
                                if (e.getErrorCode() == 206) {
                                    tokenOutOfTime();
                                }
                            }
                        }
                    });
                }
            }
        }).setDividerColor(Color.BLACK)
                .setTitleText("情感状态选择")
                .setTextColorCenter(Color.BLACK)
                .setContentTextSize(22)
                .setLayoutRes(R.layout.pickerview_custom_options, new CustomListener() {
                    @Override
                    public void customLayout(View v) {

                        v.findViewById(R.id.tv_finish).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                disMariStateOption.returnData();
                                disMariStateOption.dismiss();
                            }
                        });
                        v.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                disMariStateOption.dismiss();
                            }
                        });
                    }
                }).isDialog(true).build();
        disMariStateOption.setPicker(disMariStateItems);//添加数据
    }


    void getdisMariStateData() {
        disMariStateItems.add(new CardBean("已经离异了"));
        disMariStateItems.add(new CardBean("已经结婚了"));
        disMariStateItems.add(new CardBean("刚刚交往中"));
        disMariStateItems.add(new CardBean("正在分手期"));
        disMariStateItems.add(new CardBean("正在热恋期"));
        disMariStateItems.add(new CardBean("未婚单身狗"));
    }


    void getdisPurposeData() {
        disPurposeItems.add(new CardBean("找异性闺蜜"));
        disPurposeItems.add(new CardBean("认真婚恋"));
        disPurposeItems.add(new CardBean("极易兴奋"));
        disPurposeItems.add(new CardBean("来者不拒"));
        disPurposeItems.add(new CardBean("长期交往"));
        disPurposeItems.add(new CardBean("短期交往"));
        disPurposeItems.add(new CardBean("其他"));
    }

    void getCanDatingThingData() {

        candateThingiItems.add(new CardBean("见面一起做爱做的事"));
        candateThingiItems.add(new CardBean("先在软件里聊天试试"));
        candateThingiItems.add(new CardBean("不想理任何人"));
    }


    void getAgeData() {
        for (int index = 1; index <= 100; index++) {
            ageItems.add(new CardBean("" + index));
        }
    }

    void getSexData() {
        sexItems.add(new CardBean("男"));
        sexItems.add(new CardBean("女"));
    }

    void initLockLogic() {
        mLockViewGroup.setMaxTryTimes(5);
        mLockViewGroup.setOnLockListener(new LockViewGroup.OnLockListener() {

            @Override
            public void onLockSelected(int id) {

            }

            @Override
            public void onLess4Points() {
                mLockViewGroup.clear2ResetDelay(1200L); //清除错误
                mTvTips.setTextColor(Color.RED);
                mTvTips.setText("至少连接4个点 , 请重新输入");
            }

            @Override
            public void onSaveFirstAnswer(int[] answer) {
                mTvTips.setTextColor(Color.GRAY);
                mTvTips.setText("再次绘制 , 确认解锁图案");
                // 设置给指示器view
                mLockIndicator.setAnswer(answer);

            }

            @Override
            public void onSucessed(int[] answers) {
                mTvTips.setTextColor(Color.BLACK);
                mTvTips.setText("验证成功");
                if (rootUser != null) {
                    updateUser.secretScan = true;
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int answer : answers) {
                        stringBuilder.append(answer).append(";");
                    }
                    updateUser.secretPassword = stringBuilder.toString();
                    updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                openSecret_txt.setText("私密访问已开启");
                                CommonUtil.fecth(SettingMineInfoActivity.this);
                                rootUser = BmobUser.getCurrentUser(RootUser.class);
                                ToastUtil.showToast(SettingMineInfoActivity.this, "私密访问开启成功,请牢记密码");
                                secretView.setVisibility(View.INVISIBLE);
                                secretView.setAnimation(AnimationUtils.makeOutAnimation(SettingMineInfoActivity.this, true));
                            } else {
                                openSecret_switch.setChecked(false);
                                if (e.getErrorCode() == 206) {
                                    FragmentDialog.newInstance(false, "权限确认", "缓存已过期，请退出重新登录后修改", "去登录", "取消修改", "", "", false, new FragmentDialog.OnClickBottomListener() {
                                        @Override
                                        public void onPositiveClick(Dialog dialog, boolean needDelete) {
                                            rootUser.logOut();
                                            dialog.dismiss();
                                            startActivity(new Intent(SettingMineInfoActivity.this, LoginActivity.class));
                                        }

                                        @Override
                                        public void onNegtiveClick(Dialog dialog) {
                                            dialog.dismiss();
                                        }
                                    }).show(getSupportFragmentManager(), "");
                                }

                            }
                        }
                    });
                }

            }

            @Override
            public void onFailed(int mTryTimes) {
                mLockViewGroup.clear2ResetDelay(1400L); //清除错误
                mLockViewGroup.setHapticFeedbackEnabled(true); //手机振动
                mTvTips.setTextColor(Color.RED);
                mTvTips.setText("与上一次绘制不一致 , 请重新绘制");

                if (mTryTimes > 0) {
                    Toast.makeText(SettingMineInfoActivity.this, "剩余尝试机会: " + mTryTimes + " 次", Toast.LENGTH_SHORT).show();
                } else {
                    ToastUtil.showToast(getApplicationContext(), "设置失败");
                    finish();
                }
                Animation shakeAnimation = AnimationUtils.loadAnimation(SettingMineInfoActivity.this, R.anim.shake);
                mTvTips.startAnimation(shakeAnimation);
            }

            @Override
            public void onSetAnswerInit() {
                mTvTips.setText("绘制解锁图案");
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (nextButton.getVisibility() == View.VISIBLE) {
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sex_txt.getText().toString().contains("未设置") ||
                            age_txt.getText().toString().contains("未设置") ||
                            disMariState_txt.getText().toString().contains("未设置") ||
                            disPurpose_txt.getText().toString().contains("未设置") ||
                            candateThing_txt.getText().toString().contains("未设置")) {
                        ToastUtil.showToast(SettingMineInfoActivity.this, "设置完资料才能查看更多画面哦，老司机才懂的！");
                    } else {
                        updateUser.hasSetting = true;
                        updateUser.update(rootUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    finish();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void initLocation() {
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationClient.setLocationOption(getDefaultOption());

        AMapLocationListener locationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation loc) {
                if (null != loc) {
                    arealocation_rl.setVisibility(View.VISIBLE);
                    amlocation = loc;
                    arealocation_txt.setText(amlocation.getProvince() + amlocation.getCity() + amlocation.getDistrict() + amlocation.getStreet());
                }
            }
        };
        locationClient.setLocationListener(locationListener);
        startLocation();
    }

    /**
     * 默认的定位参数
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }


    /**
     * 开始定位
     */
    private void startLocation() {
        locationClient.setLocationOption(locationOption);
        locationClient.startLocation();
    }

    /**
     * 停止定位
     */
    private void stopLocation() {
        locationClient.stopLocation();
    }

    /**
     * 销毁定位
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
        destroyLocation();

        if (null != locationClient) {
            locationClient.onDestroy();
            locationClient = null;
        }

    }
}
