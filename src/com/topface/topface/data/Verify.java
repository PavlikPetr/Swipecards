package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Verify extends Confirmation {
    // Data
    public int money; // количество монет пользователя
    public int power; // количество энергии пользователя
    public String order; // идентификатор верифицированного заказа

    public Verify(ApiResponse response) {
        super(response);
    }

    public static Verify parse(ApiResponse response) {
        Verify verify = new Verify(response);

        try {
            verify.money = response.mJSONResult.optInt("money");
            int power = response.mJSONResult.optInt("power");
            //if(power > 10000) power = 10000;
            verify.power = (int) (power * 0.01);
            verify.order = response.mJSONResult.optString("order");
        } catch (Exception e) {
            Debug.log("Verify.class", "Wrong response parsing: " + e);
        }

        return verify;
    }
}
