package com.topface.topface.state;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.data.CountersData;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class CountersDataProvider implements Action1<CountersData> {

    public static final String COUNTERS_DATA = "counters_data";
    public static final int COUNTERS_DATA_UPDATED = 39310;
    private Subscription mSubscription;
    private Fragment mFragment;
    @Inject
    TopfaceAppState mAppState;

    public CountersDataProvider(Fragment fragment) {
        mFragment = fragment;
        App.from(fragment.getActivity()).inject(this);
        mSubscription = mAppState.getObservable(CountersData.class).subscribe(this);
    }

    @Override
    public void call(CountersData countersData) {
        if (mFragment.isAdded()) {
            Intent result = new Intent();
            result.putExtra(COUNTERS_DATA, countersData);
            mFragment.onActivityResult(COUNTERS_DATA_UPDATED, Activity.RESULT_OK, result);
        }
    }

    public void unsubscribe() {
        mSubscription.unsubscribe();
    }

}