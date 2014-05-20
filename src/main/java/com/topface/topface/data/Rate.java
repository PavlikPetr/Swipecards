package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;

public class Rate extends AbstractData {
    // Data
    public int money; // количество монет текущего пользователя
    public int likes; // текущее значение энергии пользователя
    public int average; // средняя оценка пользователя

    public static Rate parse(ApiResponse response) {
        Rate doRate = new Rate();

        try {
            doRate.likes = response.jsonResult.optInt("likes");
            Debug.log("Likes", "likes: " + doRate.likes);
            doRate.money = response.jsonResult.optInt("money");
            doRate.average = response.jsonResult.optInt("average");
        } catch (Exception e) {
            Debug.error("Rate.class: Wrong response parsing", e);
        }

        return doRate;
    }
}
