package com.topface.topface.state;

import com.topface.topface.App;
import com.topface.topface.data.CountersData;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

public class CountersDataProvider extends Subscriber<CountersData> {

    private Subscription mSubscription;
    private ICountersUpdater mUpdater;
    @Inject
    TopfaceAppState mAppState;

    public CountersDataProvider(ICountersUpdater updater) {
        mUpdater = updater;
        App.get().inject(this);
        mSubscription = mAppState.getObservable(CountersData.class)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                })
                .subscribe(this);
    }

    @Override
    public void onCompleted() {
        unsubscribe();
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onNext(CountersData countersData) {
        if (mUpdater != null) {
            mUpdater.onUpdateCounters(countersData);
        }
    }

    public interface ICountersUpdater {
        void onUpdateCounters(CountersData countersData);
    }

}