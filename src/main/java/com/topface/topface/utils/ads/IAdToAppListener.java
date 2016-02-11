package com.topface.topface.utils.ads;

public interface IAdToAppListener {
    void onVideoWatched();

    void onVideoStart();

    void onClicked();

    void onClosed();

    void onFailed();

    void onRewardedCompleted(String adProvider, String currencyName, String currencyValue);

}
