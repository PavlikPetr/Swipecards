package com.topface.topface.utils.ads;

/**
 * Created by Петр on 03.02.2016.
 * <p/>
 * Simplify AdToAppListener with only one mandatory callback
 */
public abstract class SimpleAdToAppListener implements IAdToAppListener {
    public abstract void onClosed();

    public void onVideoStart() {
    }

    public void onClicked() {
    }

    public void onVideoWatched() {
    }

    public void onFailed() {
    }

    public void onRewardedCompleted(String adProvider, String currencyName, String currencyValue) {
    }
}
