package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.utils.social.OkRequest;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import ru.ok.android.sdk.Odnoklassniki;

/**
 * Created by Петр on 03.04.2016.
 * Get user data in background
 */
public class ReportPayment extends OkRequest {

    private static final String SERVICE_NAME = "users.getCurrentUser";
    private static final String TRANSACTION_ID_PARAM = "trx_id";
    private static final String AMOUNT_PARAM = "amount";
    private static final String CURRENCY_PARAM = "currency";

    public ReportPayment(@NotNull Odnoklassniki ok, String trxId, double price, String currency) {
        super(ok, SERVICE_NAME, getParams(trxId, price, currency));
        App.from(App.getContext()).inject(this);
    }

    private static Map<String, String> getParams(String trxId, double price, String currency) {
        Map<String, String> param = new HashMap<>();
        param.put(TRANSACTION_ID_PARAM, trxId);
        param.put(AMOUNT_PARAM, String.valueOf(price));
        param.put(CURRENCY_PARAM, currency);
        return param;
    }
}
