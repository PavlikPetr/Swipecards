package com.topface.topface.ui.external_libs.offers;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.topface.topface.App;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;
import com.topface.topface.ui.external_libs.offers.Fyber.FyberOffersRequest;
import com.topface.topface.ui.external_libs.offers.Fyber.FyberOffersResponse;
import com.topface.topface.ui.external_libs.offers.Fyber.FyberOfferwallModel;
import com.topface.topface.ui.external_libs.offers.IronSource.IronSourceOffersRequest;
import com.topface.topface.ui.external_libs.offers.IronSource.IronSourceOffersResponse;
import com.topface.topface.ui.external_libs.offers.IronSource.IronSourceOfferwallModel;
import com.topface.topface.utils.ListUtils;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static com.topface.topface.ui.bonus.models.IOfferwallBaseModel.FYBER;
import static com.topface.topface.ui.bonus.models.IOfferwallBaseModel.IRON_SOURCE;

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

    public static Observable<ArrayList<IOfferwallBaseModel>> getFyberOffersObservable() {
        return new FyberOffersRequest().getRequestObservable()
                .map(new Func1<FyberOffersResponse, ArrayList<IOfferwallBaseModel>>() {
                    @Override
                    public ArrayList<IOfferwallBaseModel> call(FyberOffersResponse fyberOffersResponse) {
                        ArrayList<IOfferwallBaseModel> res = new ArrayList<>();
                        ArrayList<FyberOfferwallModel> initialList = fyberOffersResponse != null ?
                                fyberOffersResponse.getOffers() : new ArrayList<FyberOfferwallModel>();
                        if (ListUtils.isNotEmpty(initialList)) {
                            for (FyberOfferwallModel item : initialList) {
                                res.add(item);
                            }
                        }
                        return res;
                    }
                });
    }

    public static Observable<ArrayList<IOfferwallBaseModel>> getIronSourceOffersObservable() {
        return new IronSourceOffersRequest().getRequestObservable()
                .map(new Func1<IronSourceOffersResponse, ArrayList<IOfferwallBaseModel>>() {
                    @Override
                    public ArrayList<IOfferwallBaseModel> call(IronSourceOffersResponse ironSourceOffersResponse) {
                        ArrayList<IOfferwallBaseModel> res = new ArrayList<>();
                        ArrayList<IronSourceOfferwallModel> initialList = ironSourceOffersResponse != null
                                && ironSourceOffersResponse.getResponse() != null
                                ? ironSourceOffersResponse.getResponse().getOffers()
                                : new ArrayList<IronSourceOfferwallModel>();
                        if (ListUtils.isNotEmpty(initialList)) {
                            for (IronSourceOfferwallModel item : initialList) {
                                res.add(item);
                            }
                        }
                        return res;
                    }
                });
    }

    @Nullable
    public static Observable<ArrayList<IOfferwallBaseModel>> getOffersObservableByType(String type) {
        switch (type) {
            case FYBER:
                return getFyberOffersObservable();
            case IRON_SOURCE:
                return getIronSourceOffersObservable();
            default:
                return null;
        }
    }
}
