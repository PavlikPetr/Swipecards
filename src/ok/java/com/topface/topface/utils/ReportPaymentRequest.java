package com.topface.topface.utils;

import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.data.ReportPaymentData;
import com.topface.topface.utils.social.OkRequest;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import ru.ok.android.sdk.Odnoklassniki;

/**
 * Created by Петр on 03.04.2016.
 * Report payment transaction
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

    @Nullable
    @Override
    protected Map<String, String> getRequestParams() {
        Map<String, String> param = new HashMap<>();
        param.put(TRANSACTION_ID_PARAM, mTxId);
        param.put(AMOUNT_PARAM, String.valueOf(mPrice));
        param.put(CURRENCY_PARAM, mCurrency);
        return param;
    }

    @NotNull
    @Override
    protected String getRequestMethod() {
        return SERVICE_NAME;
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<ReportPaymentData>() {
        }.getType();
    }
}