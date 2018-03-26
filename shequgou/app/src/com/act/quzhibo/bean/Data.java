package com.act.quzhibo.bean;

import android.os.Parcel;
import android.os.Parcelable;


public class Data implements Parcelable {
        public String cover;
        public String id;

        public Data(Parcel in) {
            cover = in.readString();
            id = in.readString();
        }

        public static final Creator<Data> CREATOR = new Creator<Data>() {
            @Override
            public Data createFromParcel(Parcel in) {
                return new Data(in);
            }

            @Override
            public Data[] newArray(int size) {
                return new Data[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(cover);
            dest.writeString(id);

        }
}
