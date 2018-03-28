package com.act.quzhibo.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.act.quzhibo.data_db.VirtualUserInfoDao;
import com.act.quzhibo.data_db.VirtualUserInfo;
import com.act.quzhibo.widget.MyStandardVideoController;
import com.act.quzhibo.R;
import com.act.quzhibo.common.Constants;
import com.act.quzhibo.common.MyApplicaition;
import com.act.quzhibo.bean.InterestPost;
import com.act.quzhibo.bean.InterestPostPageCommentDetail;
import com.act.quzhibo.bean.InterestPostPageDetailAndComments;
import com.act.quzhibo.bean.PostContentAndImageDesc;
import com.act.quzhibo.ui.activity.InfoInterestPersonActivity;
import com.act.quzhibo.util.CommonUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.devlin_n.videoplayer.player.IjkVideoView;
import com.rockerhieu.emojicon.EmojiconTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPreviewActivity;
import cn.bingoogolapple.photopicker.widget.BGANinePhotoLayout;

public class InteretstPostDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BGANinePhotoLayout.Delegate {
    private final LayoutInflater mLayoutInflater;
    private final InterestPost post;
    private InterestPostPageDetailAndComments data;
    private Activity activity;
    File downloadDir;

    @Override
    public void onClickNinePhotoItem(BGANinePhotoLayout ninePhotoLayout, View view, int position, String model, List<String> models) {

        if (ninePhotoLayout.getItemCount() == 1) {
            // 预览单张图片
            activity.startActivity(BGAPhotoPreviewActivity.newIntent(activity, downloadDir, ninePhotoLayout.getCurrentClickItem()));
        } else if (ninePhotoLayout.getItemCount() > 1) {
            // 预览多张图片
            activity.startActivity(BGAPhotoPreviewActivity.newIntent(activity, downloadDir, ninePhotoLayout.getData(), ninePhotoLayout.getCurrentClickItemPosition()));
        }
    }

    public enum ITEM_TYPE {
        ITEM1,
        ITEM2
    }

    public InteretstPostDetailAdapter(InterestPost post, Activity context, InterestPostPageDetailAndComments data) {
        this.data = data;
        this.activity = context;
        this.post = post;
        mLayoutInflater = LayoutInflater.from(context);
        downloadDir = new File(Environment.getExternalStorageDirectory(), "PhotoPickerDownload");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE.ITEM1.ordinal();
        } else {
            return ITEM_TYPE.ITEM2.ordinal();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM1.ordinal()) {
            return new Item1ViewHolder(mLayoutInflater.inflate(R.layout.post_detail_header_layout, parent, false));
        } else {
            return new Item2ViewHolder(mLayoutInflater.inflate(R.layout.item_comment_layout, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ArrayList<String> pageImgeList = new ArrayList<>();
        ArrayList<String> contentList = new ArrayList<>();
        for (PostContentAndImageDesc des : data.detail.desc) {
            if (des.type != 1) {
                pageImgeList.add(des.value);
            } else {
                contentList.add(des.value);
            }
        }
        if (holder instanceof Item1ViewHolder) {
            Glide.with(activity).load(data.detail.user.photoUrl).asBitmap().placeholder(R.drawable.placehoder_img).error(R.drawable.error_img).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    ((Item1ViewHolder) holder).userImage.setBackgroundDrawable(new BitmapDrawable(resource));
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                    ((Item1ViewHolder) holder).userImage.setBackgroundDrawable(errorDrawable);
                }
            });


            ((Item1ViewHolder) holder).userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.POST, post);
                    intent.setClass(activity, InfoInterestPersonActivity.class);
                    activity.startActivity(intent);
                }
            });

            long l = System.currentTimeMillis() - data.detail.ctime;
            long day = l / (24 * 60 * 60 * 1000);
            long hour = (l / (60 * 60 * 1000) - day * 24);
            long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);

            VirtualUserInfoDao dao = VirtualUserInfoDao.getInstance(activity);
            if (dao.query(post.user.userId) != null) {
                String randomAge = dao.query(post.user.userId) != null ? dao.query(post.user.userId).userAge : "";
                ((Item1ViewHolder) holder).sexAndAge.setText(data.detail.user.sex.equals("2") ? "女 " + randomAge + "岁" : "男 " + randomAge + "岁");
            }

            if (day <= 1) {
                ((Item1ViewHolder) holder).createTime.setText(hour + "小时前" + min + "分钟前");
            } else if (day < 30) {
                ((Item1ViewHolder) holder).createTime.setText(day + "天" + hour + "小时前");
            } else if (day > 30) {
                ((Item1ViewHolder) holder).createTime.setText("1个月前");
            }

            String nick = data.detail.user.nick.replaceAll("\r|\n", "");
            ((Item1ViewHolder) holder).nickName.setText(nick);
            ((Item1ViewHolder) holder).title.setText(data.detail.title);

            StringBuffer sb = new StringBuffer();
            for (String s : contentList) {
                sb.append(s);
            }
            String newString = sb.toString();
            Pattern pattern = Pattern.compile("[a-z_]{1,}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(newString);
            while (matcher.find()) {
                newString = newString.replaceAll(":" + matcher.group().trim() + ":", "<img src='" + MyApplicaition.emotionsKeySrc.get(":" + matcher.group().trim() + ":") + "'>");
            }
            if (newString.contains("null")) {
                newString = newString.replaceAll("null", R.drawable.kissing_heart + "");
            }
            ((Item1ViewHolder) holder).content.setText(Html.fromHtml(newString, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    Drawable drawable = null;
                    if (!TextUtils.isEmpty(source) && !source.equals("null")) {
                        int id = Integer.parseInt(source);
                        drawable = activity.getResources().getDrawable(id);
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                                    drawable.getIntrinsicHeight());
                        }
                    }
                    return drawable;
                }
            }, null));

            if (!TextUtils.isEmpty(post.vedioUrl)) {
                ((Item1ViewHolder) holder).ijkVideoView.setVisibility(View.VISIBLE);
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        Bitmap bitmap = CommonUtil.createBitmapFromVideoPath(post.vedioUrl);
                        return bitmap;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        ((Item1ViewHolder) holder).controller.getThumb().setImageBitmap(bitmap);
                        ((Item1ViewHolder) holder).ijkVideoView
                                .enableCache()
                                .addToPlayerManager()
                                .setUrl(post.vedioUrl)
                                .setTitle(post.title)
                                .setVideoController(((Item1ViewHolder) holder).controller);

                    }
                }.execute();
            } else {
                ((Item1ViewHolder) holder).ijkVideoView.setVisibility(View.GONE);
            }
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String text = MyApplicaition.proKeySrc.get(data.detail.user.proCode) + MyApplicaition.cityKeySrc.get(data.detail.user.cityCode);
                    return text;
                }

                @Override
                protected void onPostExecute(String text) {
                    super.onPostExecute(text);
                    ((Item1ViewHolder) holder).arealocation.setText(text);
                }
            }.execute();
            ((Item1ViewHolder) holder).ninePhotoLayout.setDelegate(this);
            if (pageImgeList != null && pageImgeList.size() > 0) {
                ((Item1ViewHolder) holder).ninePhotoLayout.setData(pageImgeList);
            }
        } else if (holder instanceof Item2ViewHolder) {
            if (data.comments.size() == 0) {
                return;
            }
            final InterestPostPageCommentDetail commentDetail = data.comments.get(position - 1);

                Random random = new Random();
                String randomAge = (random.nextInt(15) + 20) + "";
                if (commentDetail.user.sex != null) {
                    VirtualUserInfoDao dao = VirtualUserInfoDao.getInstance(activity);
                    VirtualUserInfo virtualUserInfo = dao.query(commentDetail.user.userId);
                    if (virtualUserInfo != null) {
                        randomAge = virtualUserInfo.userAge;
                    } else {
                        VirtualUserInfo user=new VirtualUserInfo();
                        user.userAge=randomAge;
                        user.userId=commentDetail.user.userId;
                        user.onlineTime="";
                        user.seeMeTime="";
                        dao.add(user);
                    }
                    ((Item2ViewHolder) holder).sexAndAge.setText(commentDetail.user.sex.equals("2") ? "女 " + randomAge + "岁" : "男 " + randomAge + "岁");
                }

                ((Item2ViewHolder) holder).nickName.setText(commentDetail.user.nick);

                Glide.with(activity).load(commentDetail.user.photoUrl).asBitmap().placeholder(R.mipmap.default_head).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        ((Item2ViewHolder) holder).userImage.setBackground(new BitmapDrawable(resource));
                        ((Item2ViewHolder) holder).userImage.setTag(commentDetail.user.photoUrl);

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        ((Item2ViewHolder) holder).userImage.setBackgroundDrawable(errorDrawable);
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        ((Item2ViewHolder) holder).userImage.setBackgroundDrawable(placeholder);
                    }
                });
//            }




            long l = System.currentTimeMillis() - commentDetail.ctime;
            long day = l / (24 * 60 * 60 * 1000);
            long hour = (l / (60 * 60 * 1000) - day * 24);
            long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);


            if (day <= 1) {
                ((Item2ViewHolder) holder).createTime.setText(hour + "小时" + min + "分钟前");
            } else if (day < 30) {
                ((Item2ViewHolder) holder).createTime.setText(day + "天" + hour + "小时前");
            } else if (day > 30) {
                ((Item2ViewHolder) holder).createTime.setText("1个月前");
            }
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String newString = commentDetail.message;
                    Pattern pattern = Pattern.compile("[a-z_]{1,}", Pattern.CASE_INSENSITIVE);
                    final Matcher matcher = pattern.matcher(newString);
                    while (matcher.find()) {
                        newString = newString.replaceAll(":" + matcher.group().trim() + ":", "<img src='" + MyApplicaition.emotionsKeySrc.get(":" + matcher.group().trim() + ":") + "'>");
                    }
                    if (newString.contains("null")) {
                        newString = newString.replaceAll("null", R.drawable.kissing_heart + "");
                    }
                    return newString;
                }

                @Override
                protected void onPostExecute(String newString) {
                    super.onPostExecute(newString);

                    ((Item2ViewHolder) holder).content.setText(Html.fromHtml(newString, new Html.ImageGetter() {
                        @Override
                        public Drawable getDrawable(String source) {
                            Drawable drawable = null;
                            if (!TextUtils.isEmpty(source) && !source.equals("null")) {
                                int id = Integer.parseInt(source);
                                drawable = activity.getResources().getDrawable(id);
                                if (drawable != null) {
                                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                                            drawable.getIntrinsicHeight());
                                }
                            }
                            return drawable;
                        }
                    }, null));
                }
            }.execute();

            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String text = MyApplicaition.proKeySrc.get(commentDetail.user.proCode) + MyApplicaition.cityKeySrc.get(commentDetail.user.cityCode);
                    return text;
                }

                @Override
                protected void onPostExecute(String text) {
                    super.onPostExecute(text);
                    ((Item2ViewHolder) holder).arealocation.setText(text);
                }
            }.execute();

        }
    }

    @Override
    public int getItemCount() {
        return data.comments.size() + 1;
    }

    class Item1ViewHolder extends RecyclerView.ViewHolder {

        private TextView arealocation;
        private TextView createTime;
        private ImageView userImage;
        private TextView sexAndAge;
        private TextView nickName;
        private TextView title;
        private EmojiconTextView content;
        private IjkVideoView ijkVideoView;
        private MyStandardVideoController controller;
        private BGANinePhotoLayout ninePhotoLayout;

        public Item1ViewHolder(View view) {
            super(view);
            nickName = (TextView) view.findViewById(R.id.nickName);
            createTime = (TextView) view.findViewById(R.id.createTime);
            arealocation = (TextView) view.findViewById(R.id.location);
            userImage = (ImageView) view.findViewById(R.id.userImage);
            sexAndAge = (TextView) view.findViewById(R.id.sexAndAge);
            nickName = (TextView) view.findViewById(R.id.nickName);
            ijkVideoView = (IjkVideoView) itemView.findViewById(R.id.video_player);
            int widthPixels = activity.getResources().getDisplayMetrics().widthPixels;
            ijkVideoView.setLayoutParams(new LinearLayout.LayoutParams(widthPixels, widthPixels / 14 * 9));
            controller = new MyStandardVideoController(activity);
            controller.setInitData(false, false);
            ijkVideoView.setVideoController(controller);
            title = (TextView) view.findViewById(R.id.title);
            ninePhotoLayout = (BGANinePhotoLayout) view.findViewById(R.id.imglistview);
            content = (EmojiconTextView) view.findViewById(R.id.content);
        }
    }

    class Item2ViewHolder extends RecyclerView.ViewHolder {
        private TextView arealocation;
        private TextView createTime;
        private ImageView userImage;
        private TextView sexAndAge;
        private TextView nickName;
        private EmojiconTextView content;

        public Item2ViewHolder(View view) {
            super(view);
            nickName = (TextView) view.findViewById(R.id.nickName);
            createTime = (TextView) view.findViewById(R.id.createTime);
            arealocation = (TextView) view.findViewById(R.id.location);
            userImage = (ImageView) view.findViewById(R.id.userImage);
            sexAndAge = (TextView) view.findViewById(R.id.sexAndAge);
            content = (EmojiconTextView) view.findViewById(R.id.re_content);
        }

    }

}