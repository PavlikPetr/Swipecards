package com.topface.topface.utils.geo;

import android.location.Location;

import com.topface.topface.App;
import com.topface.topface.state.TopfaceAppState;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by ppetr on 21.03.16.
 * Получаем текущее местоположение пользователя и отправляем его на сервер
 */
public class FindAndSendCurrentLocation {

    private final static int WAIT_LOCATION_DELAY = 20;

    @Inject
    TopfaceAppState mAppState;
    private GeoLocationManager mGeoLocationManager;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    public FindAndSendCurrentLocation() {
        App.from(App.getContext()).inject(this);
        mGeoLocationManager = new GeoLocationManager();
        mGeoLocationManager.registerProvidersChangedActionReceiver();
        // пропускаем эмит из SharedPreff (BehaviorSubject), ждем WAIT_LOCATION_DELAY и отправляем
        // getLastKnownLocation если ранее не было получено значение от LocationManager
        mSubscription.add(mAppState.getObservable(Location.class).skip(1).timeout(WAIT_LOCATION_DELAY, TimeUnit.SECONDS).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                unsubscribe();
                App.sendLocation(GeoLocationManager.getCurrentLocation());
            }
        }).subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                unsubscribe();
                App.sendLocation(location);
            }
        }));
    }

    private void unsubscribe() {
        if (mGeoLocationManager != null) {
            mGeoLocationManager.unregisterProvidersChangedActionReceiver();
            mGeoLocationManager.stopLocationListener();
        }
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

}
