package com.act.quzhibo.bean;

/**
 * Created by weiminglin on 17/9/28.
 */

public class RecordVideoEvent {

    public String videoUri;
    public String videoScreenshot;

    public RecordVideoEvent(String videoUri, String videoScreenshot) {
        this.videoUri = videoUri;
        this.videoScreenshot = videoScreenshot;
    }
}
