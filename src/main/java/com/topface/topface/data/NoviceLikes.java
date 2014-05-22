package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;

public class NoviceLikes extends AbstractData {
    // Data
    public int increment; // мгновенное значение энергии пользователя

    public static NoviceLikes parse(ApiResponse response) {
        NoviceLikes novice = new NoviceLikes();

        try {
            novice.increment = response.jsonResult.optInt("increment", 0);
        } catch (Exception e) {
            Debug.error("NoviceLikes.class: Wrong response parsing", e);
        }

        return novice;
    }
}
