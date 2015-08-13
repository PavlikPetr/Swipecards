package com.topface.topface.data;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;

import org.json.JSONObject;

import java.text.DecimalFormat;

public class BuyButtonBaseData {
    public String id;
    public String title;
    protected String titleTemplate;
    public int price;
    public int showType;
    public int amount;
    public String hint;
    public Products.ProductType type;
    public int discount;
    public String paymentwallLink;
    public String totalTemplate;

    public BuyButtonBaseData(JSONObject json) {
        if (json != null) {
            id = json.optString("id");
            title = json.optString("title");
            titleTemplate = json.optString("titleTemplate");
            totalTemplate = json.optString("totalTemplate");
            price = json.optInt("price");
            amount = json.optInt("amount");
            hint = json.optString("hint");
            showType = json.optInt("showType");
            type = Products.getProductTypeByName(json.optString("type"));
            discount = json.optInt("discount");
            paymentwallLink = json.optString("url");
            ProductsDetails productsDetails = CacheProfile.getMarketProductsDetails();
            if (type == Products.ProductType.PREMIUM) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                double tempPrice = price / amount;
                double pricePerItem = tempPrice / 100;
                if (titleTemplate.contains(Products.PRICE)) {
                    title = titleTemplate.replace(Products.PRICE, decimalFormat.format(pricePerItem) + App.getContext().getString(R.string.usd));

                } else if (titleTemplate.contains(Products.PRICE_PER_ITEM)) {
                    title = titleTemplate.replace(Products.PRICE_PER_ITEM, decimalFormat.format(pricePerItem) + App.getContext().getString(R.string.usd));

                }
            }
            if (productsDetails != null) {
                ProductsDetails.ProductDetail detail = productsDetails.getProductDetail(id);
                if (detail != null) {
                    double price = detail.price / ProductsDetails.MICRO_AMOUNT;
                    double pricePerItem = price / amount;
                    title = titleTemplate.replace(Products.PRICE, String.format("%.2f %s", price, detail.currency));
                    title = title.replace(Products.PRICE_PER_ITEM, String.format("%.2f %s", pricePerItem, detail.currency));
                }
            }
        }
    }
}
