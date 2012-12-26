package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class SendGiftAnswer extends AbstractData {
    public int likes;
    public int money;

    public static SendGiftAnswer parse(ApiResponse response) {
        SendGiftAnswer sendGift = new SendGiftAnswer();

        try {
            sendGift.likes = response.jsonResult.optInt("likes");
            sendGift.money = response.jsonResult.optInt("money");
        } catch (Exception e) {
            Debug.error("SendGift.class: Wrong response parsing", e);
        }

        return sendGift;
    }
}
