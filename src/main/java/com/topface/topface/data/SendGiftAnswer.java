package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class SendGiftAnswer extends AbstractData {
    public History history;

    public static SendGiftAnswer parse(ApiResponse response) {
        SendGiftAnswer sendGift = new SendGiftAnswer();

        try {
            sendGift.history = new History(response);
        } catch (Exception e) {
            Debug.error("SendGift.class: Wrong response parsing", e);
        }

        return sendGift;
    }
}
