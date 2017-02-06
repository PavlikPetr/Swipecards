package com.topface.topface.state;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.rx.RxUtils;

import rx.subscriptions.CompositeSubscription;

public class OptionsAndProfileProvider {

    private IStateDataUpdater mUpdater;
    private TopfaceAppState mAppState;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    public OptionsAndProfileProvider(IStateDataUpdater updater) {
        mAppState = App.getAppComponent().appState();
        mUpdater = updater;
        mSubscription.add(mAppState.getObservable(Options.class).subscribe(new RxUtils.ShortSubscription<Options>() {
            @Override
            public void onNext(Options options) {
                if (mUpdater != null) {
                    mUpdater.onOptionsUpdate(options);
                }
            }
        }));
        mSubscription.add(mAppState.getObservable(Profile.class).subscribe(new RxUtils.ShortSubscription<Profile>() {
            @Override
            public void onNext(Profile profile) {
                if (mUpdater != null) {
                    mUpdater.onProfileUpdate(profile);
                }
            }
        }));
    }

    public void unsubscribe() {
        RxUtils.safeUnsubscribe(mSubscription);
        mUpdater = null;
    }

}
