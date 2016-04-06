package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.utils.social.OkRequest;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkRequestMode;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Петр on 03.04.2016.
 * Get user data in background
 */
public class ReportPayment extends OkRequest {

    private static final String SERVICE_NAME = "users.getCurrentUser";
    private static final String TRANSACTION_ID_PARAM = "trx_id";
    private static final String AMOUNT_PARAM = "amount";
    private static final String CURRENCY_PARAM = "currency";

    private String mTxId;
    private double mPrice;
    private String mCurrency;

    public ReportPayment(@NotNull Odnoklassniki ok, String trxId, double price, String currency) {
        super(ok);
        mTxId = trxId;
        mPrice = price;
        mCurrency = currency;
        App.from(App.getContext()).inject(this);
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
    protected void getObservable(Observable<String> observable) {
        observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                callFail();
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
    }
}
