package com.act.quzhibo.bean;

import java.util.ArrayList;

public class ProvinceAndCityEntity {
    public int proId;// 7,
    public String name;//
    public int ProSort; ///5,
    public String ProRemark;//
    public ArrayList<CitySub> citySub;

    public class CitySub {
        public int cityId;
        public String name;
        public int CitySort;
    }
}
