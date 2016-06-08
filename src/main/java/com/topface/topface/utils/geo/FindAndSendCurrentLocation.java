package com.topface.topface.utils.geo;

import android.location.Location;
import android.os.Looper;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.state.TopfaceAppState;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscriber;
import rx.schedulers.Schedulers;
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
        mSubscription.add(mAppState.getObservable(Location.class)
                .subscribeOn(Schedulers.newThread())
                .skip(1)
                .timeout(WAIT_LOCATION_DELAY, TimeUnit.SECONDS)
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        stop();
                        sendLocation(GeoLocationManager.getCurrentLocation());
                    }

                    @Override
                    public void onNext(Location location) {
                        stop();
                        sendLocation(location);
                    }
                }));
    }

    private void stop() {
        if (mGeoLocationManager != null) {
            mGeoLocationManager.unregisterProvidersChangedActionReceiver();
            mGeoLocationManager.stopLocationListener();
        }
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    public void sendLocation(final @Nullable Location location) {
        if (location != null) {
            new BackgroundThread(Thread.MIN_PRIORITY) {
                @Override
                public void execute() {
                    App.setLastKnownLocation(location);
                    Looper.prepare();
                    SettingsRequest settingsRequest = new SettingsRequest(App.getContext());
                    settingsRequest.location = location;
                    settingsRequest.exec();
                    Looper.loop();
                }
            };
        }
    }
}
