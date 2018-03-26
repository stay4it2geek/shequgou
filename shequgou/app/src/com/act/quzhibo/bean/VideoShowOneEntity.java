package com.act.quzhibo.bean;

public class VideoShowOneEntity extends VideoShowBase {
    public Data data;

    public class Data {
        public String id;
        public String vid;
        public String shareNum;
        public String url;
        public Avatar avatar;

        public class Avatar {
            public String id;
            public int width;
            public int height;
            public int sn;
            public String url;
        }
    }
}