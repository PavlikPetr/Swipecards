package com.topface.topface.utils;

import android.content.Context;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.RenewalOfSubscriptionData;
import com.topface.topface.data.ReportPaymentData;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.OkGetRenewalOfSubscriptionsRequest;
import com.topface.topface.requests.OkMarkRenewalAsSentRequest;
import com.topface.topface.statistics.ReportPaymentStatistics;
import com.topface.topface.utils.social.OkAuthorizer;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ppavlik on 05.04.16.
 * methods for purchases state
 */
public class PurchasesEvents {

    public void purchaseSuccess(int productsCount, String productType, String productId, String currencyCode, double price, String transactionId) {
        new ReportPaymentRequest(
                new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds()),
                transactionId,
                price,
                currencyCode)
                .getObservable()
                .subscribe(new Action1<ReportPaymentData>() {
                    @Override
                    public void call(ReportPaymentData result) {
                        Debug.log("ReportPaymentRequest success " + result.isSuccess());
                        if (result.isSuccess()) {
                            ReportPaymentStatistics.sendSuccess();
                        } else {
                            ReportPaymentStatistics.sendFail();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Debug.error("ReportPaymentRequest error " + throwable);
                        ReportPaymentStatistics.sendFail();
                    }
                });
    }

    public void checkRenewSubscription(final Context context) {
        new OkGetRenewalOfSubscriptionsRequest(context).callback(new DataApiHandler<RenewalOfSubscriptionData>() {
            @Override
            public void fail(int codeError, IApiResponse response) {
                Debug.log("OkGetRenewalOfSubscriptionsRequest return fail");
            }

            @Override
            protected void success(RenewalOfSubscriptionData data, IApiResponse response) {
                if (data != null && data.renewals != null && data.renewals.size() > 0) {
                    Observable.from(data.renewals)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(Schedulers.newThread())
                            .filter(new Func1<RenewalOfSubscriptionData.SubscriptionData, Boolean>() {
                                @Override
                                public Boolean call(RenewalOfSubscriptionData.SubscriptionData subscriptionData) {
                                    return subscriptionData != null;
                                }
                            })
                            .subscribe(new Action1<RenewalOfSubscriptionData.SubscriptionData>() {
                                @Override
                                public void call(final RenewalOfSubscriptionData.SubscriptionData subscriptionData) {
                                    new ReportPaymentRequest(
                                            new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds()),
                                            subscriptionData.orderId,
                                            subscriptionData.amount,
                                            subscriptionData.currency)
                                            .getObservable()
                                            .subscribe(new Action1<ReportPaymentData>() {
                                                @Override
                                                public void call(ReportPaymentData reportPaymentData) {
                                                    Debug.log("ReportPaymentRequest success " + reportPaymentData.isSuccess());
                                                    if (reportPaymentData.isSuccess()) {
                                                        ReportPaymentStatistics.sendSuccess();
                                                    } else {
                                                        ReportPaymentStatistics.sendFail();
                                                    }
                                                    new OkMarkRenewalAsSentRequest(context, subscriptionData.orderId).exec();
                                                }
                                            }, new Action1<Throwable>() {
                                                @Override
                                                public void call(Throwable throwable) {

                                                }
                                            });
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    Debug.error("ReportPaymentRequest error " + throwable);
                                    ReportPaymentStatistics.sendFail();
                                }
                            });
                }
            }

            @Override
            protected RenewalOfSubscriptionData parseResponse(ApiResponse response) {
                return JsonUtils.fromJson(response.jsonResult.toString(), RenewalOfSubscriptionData.class);
            }
        }).exec();
    }
}
