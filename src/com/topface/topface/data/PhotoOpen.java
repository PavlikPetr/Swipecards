package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class PhotoOpen extends Confirmation {
    // Data
    public int money; // количество монет текущего пользователя

    public PhotoOpen(ApiResponse response) {
        super(response);
    }

    public static PhotoOpen parse(ApiResponse response) {
        PhotoOpen open = new PhotoOpen(response);

        try {
            open.money = response.mJSONResult.optInt("money");
        } catch (Exception e) {
            Debug.log("PhotoOpen.class", "Wrong response parsing: " + e);
        }

        return open;
    }
}
