package com.topface.topface.state;


import com.topface.topface.App;
import com.topface.topface.data.Options;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;


public class OptionsProvider implements Action1<Options> {

    private IOptionsUpdater mIOptionsUpdater;
    @Inject
    public TopfaceAppState appState;
    private Subscription mSubscription;

    public OptionsProvider() {
    }

    public OptionsProvider(IOptionsUpdater optionsUpdater) {
        mIOptionsUpdater = optionsUpdater;
        App.from(App.getContext()).inject(this);
        mSubscription = appState.getObservable(Options.class).subscribe(this);
    }

    @Override
    public void call(Options options) {
        if (mIOptionsUpdater != null) {
            mIOptionsUpdater.onOptionsUpdate(options);
        }
    }

    public void unsubscribe() {
        mSubscription.unsubscribe();
    }

    public interface IOptionsUpdater {

        void onOptionsUpdate(Options options);

        Options getOptions();
    }

}
