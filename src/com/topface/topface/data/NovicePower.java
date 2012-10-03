package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class NovicePower extends AbstractData {
    // Data
    public int power; // мгновенное значение энергии пользователя

    public static NovicePower parse(ApiResponse response) {
        NovicePower novice = new NovicePower();

        try {
            int power = response.mJSONResult.optInt("power", 0);
            //if(power > 10000) power = 10000;
            novice.power = (int) (power * 0.01);
        } catch (Exception e) {
            Debug.log("NovicePower.class", "Wrong response parsing: " + e);
        }

        return novice;
    }
}
