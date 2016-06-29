package com.topface.topface.ui.external_libs.offers;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.topface.topface.App;

import java.io.IOException;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;

public class OffersUtils {

    public static Observable<GoogleAdParams> getGoogleAdParamsObservable() {
        return Observable.create(new Observable.OnSubscribe<GoogleAdParams>() {
            @Override
            public void call(Subscriber<? super GoogleAdParams> subscriber) {
                try {
                    AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(App.getContext());
                    subscriber.onNext(new GoogleAdParams(info.getId(), info.isLimitAdTrackingEnabled()));
                    subscriber.onCompleted();
                } catch (IOException
                        | GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Retrofit getRequestInstance(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }
}
