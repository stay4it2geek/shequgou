package com.act.quzhibo.bean;

import java.io.Serializable;
import java.util.ArrayList;
public class InterestPostListInfoParentData implements Serializable {
    public boolean ok;
    public boolean relogin;
    public boolean needRegister;
    public boolean applePayRetry;
     public ArrayList<InterestPost>  result;
}
