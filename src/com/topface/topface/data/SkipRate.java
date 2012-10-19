package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class SkipRate extends Confirmation {
    // Data
    public int money; // мгновенное количество оставшихся монет текущего пользователя
    public int power; // мгновенное значение энергии текущего пользователя
    public boolean completed; // результат выполнения операции

    public SkipRate(ApiResponse response) {
        super(response);
    }

    public static SkipRate parse(ApiResponse response) {
        SkipRate skip = new SkipRate(response);

        try {
            skip.money = response.jsonResult.optInt("money");
            skip.power = response.jsonResult.optInt("power");
            skip.power = (int) (skip.power * 0.01);
            skip.completed = response.jsonResult.optBoolean("completed");
        } catch (Exception e) {
            Debug.log("SkipRate.class", "Wrong response parsing: " + e);
        }

        return skip;
    }
}
