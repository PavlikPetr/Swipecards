package com.topface.topface.state;

import com.topface.topface.App;
import com.topface.topface.data.CountersData;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class CountersDataProvider implements Action1<CountersData> {

    private Subscription mSubscription;
    private ICountersUpdater mUpdater;
    @Inject
    TopfaceAppState mAppState;

    public CountersDataProvider(ICountersUpdater updater) {
        mUpdater = updater;
        App.get().inject(this);
        mSubscription = mAppState.getObservable(CountersData.class).subscribe(this);
    }

    @Override
    public void call(CountersData countersData) {
        if (mUpdater != null) {
            mUpdater.onUpdateCounters(countersData);
        }
    }

    public void unsubscribe() {
        mSubscription.unsubscribe();
    }

    public interface ICountersUpdater {
        void onUpdateCounters(CountersData countersData);
    }

}