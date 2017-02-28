package com.topface.topface.state;

import com.topface.topface.App;
import com.topface.topface.data.CountersData;

import rx.Subscriber;
import rx.functions.Action1;

public class CountersDataProvider extends Subscriber<CountersData> {

    private ICountersUpdater mUpdater;

    public CountersDataProvider(ICountersUpdater updater) {
        mUpdater = updater;
        App.getAppComponent().appState().getObservable(CountersData.class)
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