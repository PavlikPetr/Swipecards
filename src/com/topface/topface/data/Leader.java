package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

public class Leader extends AbstractData {
    public static int money;
    public static boolean completed;

    public static Rate parse(ApiResponse response) {
        Rate doRate = new Rate();

        try {
            money = response.mJSONResult.optInt("money");
            completed = response.mJSONResult.optBoolean("completed");
            CacheProfile.money = money;
        } catch (Exception e) {
            Debug.log("DoRate.class", "Wrong response parsing: " + e);
        }

        return doRate;
    }

}
