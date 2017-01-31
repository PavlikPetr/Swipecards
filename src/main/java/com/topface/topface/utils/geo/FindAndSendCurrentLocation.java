package com.topface.topface.utils.geo;

import android.Manifest;
import android.location.Location;
import android.os.Looper;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.utils.extensions.PermissionsExtensionsKt;
import com.topface.topface.utils.rx.RxUtils;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
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
        App.get().inject(this);
        if (mAppState == null ||
                !PermissionsExtensionsKt.isGrantedPermissions(App.getContext(), Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return;
        }
        mGeoLocationManager = new GeoLocationManager();
        mGeoLocationManager.registerProvidersChangedActionReceiver();
        // пропускаем эмит из SharedPreff (BehaviorSubject), ждем WAIT_LOCATION_DELAY и отправляем
        // getLastKnownLocation если ранее не было получено значение от LocationManager
        mSubscription.add(mAppState.getObservable(Location.class)
                .subscribeOn(Schedulers.newThread())
                .skip(1)
                .subscribe(new RxUtils.ShortSubscription<Location>() {
                    @Override
                    public void onNext(Location location) {
                        stop();
                        sendLocation(location);
                    }
                }));
        mSubscription.add(Observable.timer(WAIT_LOCATION_DELAY, TimeUnit.SECONDS).first()
                .subscribe(new RxUtils.ShortSubscription<Long>() {
                    @Override
                    public void onNext(Long type) {
                        stop();
                        sendLocation(GeoLocationManager.getCurrentLocation());
                    }
                }));
    }

    private void stop() {
        if (mGeoLocationManager != null) {
            mGeoLocationManager.unregisterProvidersChangedActionReceiver();
            mGeoLocationManager.stopLocationListener();
        }
        RxUtils.safeUnsubscribe(mSubscription);
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
