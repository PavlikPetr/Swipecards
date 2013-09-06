package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class SkipRate extends Confirmation {
    // Data
    public int money; // мгновенное количество оставшихся монет текущего пользователя
    public int likes; // мгновенное значение симпатий текущего пользователя
    public boolean completed; // результат выполнения операции

    public SkipRate(ApiResponse response) {
        super(response);
    }

    public static SkipRate parse(ApiResponse response) {
        SkipRate skip = new SkipRate(response);

        try {
            skip.money = response.jsonResult.optInt("money");
            skip.likes = response.jsonResult.optInt("likes");
            skip.completed = response.jsonResult.optBoolean("completed");
        } catch (Exception e) {
            Debug.error("SkipRate.class: Wrong response parsing", e);
        }

        return skip;
    }
}
