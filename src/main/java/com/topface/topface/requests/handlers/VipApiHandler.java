package com.topface.topface.requests.handlers;

import android.app.Activity;
import android.content.Context;

import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.PurchasesActivity;

public class VipApiHandler extends SimpleApiHandler {
    @Override
    public void success(IApiResponse response) {
        //Этот метод можно переопределить
    }

    @Override
    public void fail(int codeError, IApiResponse response) {
        if (codeError == ErrorCodes.PREMIUM_ACCESS_ONLY) {
            Context context = getContext();
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(
                        PurchasesActivity.createVipBuyIntent(null, "PremiumAccessOnly"),
                        PurchasesActivity.INTENT_BUY_VIP
                );
            }
        } else {
            super.fail(codeError, response);
        }
    }


    @Override
    public void always(IApiResponse response) {
        super.always(response);
    }
}
