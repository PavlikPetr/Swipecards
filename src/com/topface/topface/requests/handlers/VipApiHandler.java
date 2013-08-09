package com.topface.topface.requests.handlers;

import android.app.Activity;
import android.content.Context;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.ContainerActivity;

public class VipApiHandler extends SimpleApiHandler {
    @Override
    public void success(ApiResponse response) {
        //Этот метод можно переопределить
    }

    @Override
    public void fail(int codeError, ApiResponse response) {
        if (codeError == ApiResponse.PREMIUM_ACCESS_ONLY) {
            Context context = getContext();
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(
                        ContainerActivity.getVipBuyIntent(null, "PremiumAccessOnly"),
                        ContainerActivity.INTENT_BUY_VIP_FRAGMENT
                );
            }
        } else {
            super.fail(codeError, response);
        }
    }


    @Override
    public void always(ApiResponse response) {
        //Этот метод можно переопределить
    }
}
