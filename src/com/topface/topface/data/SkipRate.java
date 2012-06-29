package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class SkipRate extends AbstractData {
    // Data
    public int money; // мгновенное количество оставшихся монет текущего пользователя
    public int power; // мгновенное значение энергии текущего пользователя
    public boolean completed; // результат выполнения операции
    //---------------------------------------------------------------------------
    public static SkipRate parse(ApiResponse response) {
        SkipRate skip = new SkipRate();

        try {
            skip.money = response.mJSONResult.optInt("money");
            skip.power = response.mJSONResult.optInt("power");
            skip.completed = response.mJSONResult.optBoolean("completed");
        } catch(Exception e) {
            Debug.log("SkipRate.class", "Wrong response parsing: " + e);
        }

        return skip;
    }
    //---------------------------------------------------------------------------
}
