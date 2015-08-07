package com.topface.topface.data;

import org.json.JSONObject;

public class BuyButtonData extends BuyButtonBaseData {

    public BuyButtonData(JSONObject json) {
        super(json);
    }

    public void setTitleByPrice(String price) {
        title = titleTemplate.replace(Products.PRICE, price);
    }
}
