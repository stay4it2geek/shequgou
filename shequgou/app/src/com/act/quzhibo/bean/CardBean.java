package com.act.quzhibo.bean;

import com.bigkoo.pickerview.model.IPickerViewData;

/**
 * 滚动板item
 */

public class CardBean implements IPickerViewData {
    String beanStr;

    public CardBean(String beanStr) {
        this.beanStr = beanStr;
    }
    @Override
    public String getPickerViewText() {
        return beanStr;
    }

}

