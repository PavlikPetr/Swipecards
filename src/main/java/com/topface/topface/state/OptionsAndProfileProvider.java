package com.topface.topface.state;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class OptionsAndProfileProvider {

    private IStateDataUpdater mUpdater;
    @Inject
    public TopfaceAppState appState;
    private Subscription mProfileSubscription;
    private Subscription mOptionsSubscription;

    public OptionsAndProfileProvider(IStateDataUpdater updater) {
        mUpdater = updater;
        App.from(App.getContext()).inject(this);
        mOptionsSubscription = appState.getObservable(Options.class).subscribe(new Action1<Options>() {
            @Override
            public void call(Options options) {
                if (mUpdater != null) {
                    mUpdater.onOptionsUpdate(options);
                }
            }
        });
        mProfileSubscription = appState.getObservable(Profile.class).subscribe(new Action1<Profile>() {
            @Override
            public void call(Profile profile) {
                if (mUpdater != null) {
                    mUpdater.onProfileUpdate(profile);
                }
            }
        });
    }

    public void unsubscribe() {
        mUpdater = null;
        if (mProfileSubscription != null) {
            mProfileSubscription.unsubscribe();
        }
        if (mOptionsSubscription != null) {
            mOptionsSubscription.unsubscribe();
        }
    }

}
