package com.act.quzhibo.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by asus-pc on 2018/3/21.
 */

public class GirlVideoListInfo extends VideoShowBase implements Serializable{
    public Data data;

    public class Data implements Serializable{
        public String id;
        public String nickname;
        public String isFocus;
        public Avatar avatar;
        public String topic;
        public ArrayList<Tag> tags;
    }
    public class Avatar implements Serializable{
        public String id;
        public int width;
        public int height;
        public int sn;
        public String url;}

    public class Tag implements Serializable{
        public String id;
        public String type;
        public String name;
        public String color;
        public String cdate;
        public String isuse;
        public String sn ;
        public String is_love;
        public String category;
    }
}
