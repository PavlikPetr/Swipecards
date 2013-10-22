package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

import org.json.JSONObject;

public class Verify extends AbstractData {
    // Data
    public int money; // количество монет пользователя
    public int likes; // количество энергии пользователя
    public boolean premium; // количество энергии пользователя
    public String order; // идентификатор верифицированного заказа

    public Verify(ApiResponse data) {
        if (data != null) {
            fillData(data.getJsonResult());
        }
    }

    protected void fillData(JSONObject data) {
        if (data != null) {
            try {
                money = data.optInt("money");
                likes = data.optInt("likes");
                order = data.optString("order");
                premium = data.optBoolean("premium");
            } catch (Exception e) {
                Debug.error("Verify.class: Wrong response parsing", e);
            }
        }
    }
}
