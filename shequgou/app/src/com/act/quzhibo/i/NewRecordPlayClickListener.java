package com.act.quzhibo.i;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.act.quzhibo.R;
import com.act.quzhibo.util.FileUtil;
import com.act.quzhibo.util.ToastUtil;

import java.io.File;
import java.io.FileInputStream;

import cn.bmob.newim.bean.BmobIMAudioMessage;
import cn.bmob.newim.core.BmobDownloadManager;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;

public class NewRecordPlayClickListener implements View.OnClickListener {

    BmobIMAudioMessage message;
    ImageView iv_voice;
    private AnimationDrawable anim = null;
    Context mContext;
    String currentObjectId = "";
    MediaPlayer mediaPlayer = null;
    public static boolean isPlaying = false;
    public static NewRecordPlayClickListener currentPlayListener = null;
    static BmobIMAudioMessage currentMsg = null;
    boolean isInfo;


    public NewRecordPlayClickListener(Context context, BmobIMAudioMessage msg, ImageView voice, boolean isInfo) {
        this.iv_voice = voice;
        this.message = msg;
        this.isInfo = isInfo;
        this.mContext = context.getApplicationContext();
        currentMsg = msg;
        currentPlayListener = this;
        try {
            currentObjectId = BmobUser.getCurrentUser().getObjectId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("resource")
    public void startPlayRecord(String filePath, boolean isUseSpeaker) {
        if (!(new File(filePath).exists())) {
            return;
        }
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();
        if (isUseSpeaker) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }

        try {
            mediaPlayer.reset();
            // 单独使用此方法会报错播放错误:setDataSourceFD failed.: status=0x80000000
            // mediaPlayer.setDataSource(filePath);
            // 因此采用此方式会避免这种错误
            FileInputStream fis = new FileInputStream(new File(filePath));
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer arg0) {
                    isPlaying = true;
                    currentMsg = message;
                    arg0.start();
                    startRecordAnimation();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {stopPlayRecord();
                        }

                    });
            currentPlayListener = this;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlayRecord() {
        stopRecordAnimation();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
            if(mediaPlayer==null){
            }
        }
        isPlaying = false;
    }

    private void startRecordAnimation() {
        if (message.getFromId().equals(currentObjectId)) {
            iv_voice.setImageResource(R.drawable.anim_chat_voice_right);
        } else {
            iv_voice.setImageResource(R.drawable.anim_chat_voice_left);
        }
        anim = (AnimationDrawable) iv_voice.getDrawable();
        anim.start();
    }

    private void stopRecordAnimation() {
        if (message.getFromId().equals(currentObjectId)) {
            iv_voice.setImageResource(R.mipmap.voice_left3);
        } else {
            iv_voice.setImageResource(R.mipmap.voice_right3);
        }
        if (anim != null) {
            anim.stop();
        }
    }

    @Override
    public void onClick(View arg0) {
        if (isPlaying) {
            currentPlayListener.stopPlayRecord();
            if (isInfo) {
                return;
            } else {
                if (currentMsg != null&& currentMsg.hashCode() == message.hashCode()) {
                    currentMsg = null;
                    return;
                }
            }
        } else {
            if (message.getFromId().equals(currentObjectId)) {// 如果是自己发送的语音消息，则播放本地地址
                String localPath = message.getContent().split("&")[0];
                startPlayRecord(localPath, true);
            } else {// 如果是收到的消息，则需要先下载后播放
                if (isInfo) {// 如果是个人信息的audio
                    if (FileUtil.checkSdCard()) {
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "info_audio");
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        String path = file.getAbsolutePath().concat("/").concat(message.getFromId().concat(".amr"));
                        File localFile = new File(path);
                        if (localFile.exists()) {
                            startPlayRecord(path, true);
                        } else {
                            ToastUtil.showToast(mContext, "加载中...");
                            BmobFile bmobfile = new BmobFile("remoteAudio", "", message.getRemoteUrl());
                            bmobfile.download(localFile, new DownloadFileListener() {
                                @Override
                                public void done(String localPath, BmobException e) {
                                    if (e == null) {
                                        startPlayRecord(localPath, true);
                                    }
                                }

                                @Override
                                public void onProgress(Integer integer, long l) {

                                }
                            });
                        }
                    }
                } else {
                    String localPath = BmobDownloadManager.getDownLoadFilePath(message);
                    startPlayRecord(localPath, true);
                }
            }
        }

    }

}