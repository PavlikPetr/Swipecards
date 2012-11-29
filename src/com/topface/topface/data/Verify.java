package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Verify extends Confirmation {
    // Data
    public int money; // количество монет пользователя
    public int power; // количество энергии пользователя
    public boolean premium; // количество энергии пользователя
    public String order; // идентификатор верифицированного заказа

    public Verify(ApiResponse response) {
        super(response);
    }

    public static Verify parse(ApiResponse response) {
        Verify verify = new Verify(response);

        try {
            verify.money = response.jsonResult.optInt("money");
            int power = response.jsonResult.optInt("power");
            verify.power = (int) (power * 0.01);
            verify.order = response.jsonResult.optString("order");
            verify.premium = response.jsonResult.optBoolean("premium");
        } catch (Exception e) {
            Debug.log("Verify.class", "Wrong response parsing: " + e);
        }

        return verify;
    }
}
