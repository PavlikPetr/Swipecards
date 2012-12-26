package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class NoviceLikes extends AbstractData {
    // Data
    public int likes; // мгновенное значение энергии пользователя

    public static NoviceLikes parse(ApiResponse response) {
        NoviceLikes novice = new NoviceLikes();

        try {
            novice.likes = response.jsonResult.optInt("likes", 0);
        } catch (Exception e) {
            Debug.error("NoviceLikes.class: Wrong response parsing", e);
        }

        return novice;
    }
}
