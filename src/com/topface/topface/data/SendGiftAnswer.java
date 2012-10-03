package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class SendGiftAnswer extends AbstractData {
    public int power;
    public int money;

    public static SendGiftAnswer parse(ApiResponse response) {
        SendGiftAnswer sendGift = new SendGiftAnswer();

        try {
            sendGift.power = response.mJSONResult.optInt("power");
            sendGift.money = response.mJSONResult.optInt("money");
            sendGift.power = (int) (sendGift.power * 0.01);
        } catch (Exception e) {
            Debug.log("SendGift.class", "Wrong response parsing: " + e);
        }

        return sendGift;
    }
}
