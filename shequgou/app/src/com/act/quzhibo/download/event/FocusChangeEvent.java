package com.act.quzhibo.download.event;

public class FocusChangeEvent {
    public boolean focus;
    public int  position;
    public String  type;

    public FocusChangeEvent(boolean focus, int  position,String  type) {
        this.focus = focus;
        this.position = position;
        this.type = type;

    }
}
