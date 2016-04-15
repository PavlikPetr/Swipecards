package com.topface.topface.utils;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.data.ReportPaymentData;
import com.topface.topface.utils.social.OkRequest;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkRequestMode;

/**
 * Created by Петр on 03.04.2016.
 * Get user data in background
 */
public class ReportPaymentRequest extends OkRequest<ReportPaymentData> {

    private static final String SERVICE_NAME = "sdk.reportPayment";
    private static final String TRANSACTION_ID_PARAM = "trx_id";
    private static final String AMOUNT_PARAM = "amount";
    private static final String CURRENCY_PARAM = "currency";

    private String mTxId;
    private double mPrice;
    private String mCurrency;

    public ReportPaymentRequest(@NotNull Odnoklassniki ok, String trxId, double price, String currency) {
        super(ok);
        mTxId = trxId;
        mPrice = price;
        mCurrency = currency;
    }


    private Map<String, String> getParams(String trxId, double price, String currency) {
        Map<String, String> param = new HashMap<>();
        param.put(TRANSACTION_ID_PARAM, trxId);
        param.put(AMOUNT_PARAM, String.valueOf(price));
        param.put(CURRENCY_PARAM, currency);
        return param;
    }

    @Override
    protected String getRequest(Odnoklassniki ok) throws IOException {
        return ok.request(SERVICE_NAME, getParams(mTxId, mPrice, mCurrency), OkRequestMode.DEFAULT);
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<ReportPaymentData>() {
        }.getType();
    }
}
