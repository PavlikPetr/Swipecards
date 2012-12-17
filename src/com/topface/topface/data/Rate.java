package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Rate extends AbstractData {
    // Data
    public int money; // количество монет текущего пользователя
    public int power; // текущее значение энергии пользователя
    public int average; // средняя оценка пользователя

    public static Rate parse(ApiResponse response) {
        Rate doRate = new Rate();

        try {
            int power = response.jsonResult.optInt("power");
            Debug.log("POWER", "power: " + power);
            //if(power > 10000) power = 10000;
            doRate.power = (int) (power * 0.01);
            doRate.money = response.jsonResult.optInt("money");
            doRate.average = response.jsonResult.optInt("average");
        } catch (Exception e) {
            Debug.error("DoRate.class: Wrong response parsing", e);
        }

        return doRate;
    }
}
