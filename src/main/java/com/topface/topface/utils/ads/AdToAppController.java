package com.topface.topface.utils.ads;

import android.app.Activity;

import com.appintop.init.AdToApp;
import com.appintop.interstitialads.DefaultInterstitialListener;
import com.topface.topface.banners.ad_providers.AdToAppProvider;

import java.util.HashMap;

/**
 * Created by Петр on 03.02.2016.
 * Контроллер для работы с рекламной сетью AdToApp
 */
public class AdToAppController {

    public static final String ADTOAPP_APP_KEY = "361e95a8-3cf4-494d-89de-1a0f57f25ab3:b8942ef1-6fe1-4c7b-ab3d-2814072cedf3";

    private HashMap<String, IAdToAppListener> mAdToAppListeners = new HashMap<>();
    private HashMap<AdsMasks, AdsAvailableListener> mAdsAvailableMap = new HashMap<>();
    private static AdToAppController mInstance;
    private static Activity mActivity;
    private boolean isVideoStart;
    private DefaultInterstitialListener mInterstitialListener = new DefaultInterstitialListener() {
        @Override
        public void onFirstInterstitialLoad(String s, String s1) {
            addAdsState(s, true);
        }

        @Override
        public void onInterstitialStarted(String s, String s1) {
            if (s.equals(AdsMasks.VIDEO.getType())) {
                isVideoStart = true;
            }
            for (IAdToAppListener listener : mAdToAppListeners.values()) {
                if (listener != null) {
                    listener.onVideoStart();
                }
            }
        }

        @Override
        public void onInterstitialClicked(String s, String s1) {
            for (IAdToAppListener listener : mAdToAppListeners.values()) {
                if (listener != null) {
                    listener.onClicked();
                }
            }
        }

        @Override
        public void onInterstitialClosed(String s, String s1) {
            for (IAdToAppListener listener : mAdToAppListeners.values()) {
                if (listener != null) {
                    listener.onClosed();
                    if (isVideoStart && s.equals(AdsMasks.VIDEO.getType())) {
                        listener.onVideoWatched();
                    }
                }
            }
        }

        @Override
        public boolean onInterstitialFailedToShow(String s) {
            addAdsState(s, false);
            for (IAdToAppListener listener : mAdToAppListeners.values()) {
                if (listener != null) {
                    listener.onFailed();
                }
            }
            return false;
        }

        @Override
        public void onRewardedCompleted(String adProvider, String currencyName, String currencyValue) {
            super.onRewardedCompleted(adProvider, currencyName, currencyValue);
            for (IAdToAppListener listener : mAdToAppListeners.values()) {
                if (listener != null) {
                    listener.onRewardedCompleted(adProvider, currencyName, currencyValue);
                }
            }
        }
    };

    public AdToAppController(Activity activity) {
        mActivity = activity;
    }

    public static AdToAppController getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new AdToAppController(activity);
            mInstance.initSdk();
        }
        if (mActivity == null) {
            mActivity = activity;
        }
        return mInstance;
    }

    private void initSdk() {
        if (!AdToApp.isSDKInitialized()) {
            AdToApp.initializeSDK(mActivity, ADTOAPP_APP_KEY, getAdsMask());
            AdToApp.setInterstitialListener(mInterstitialListener);
        }
    }

    public void addListener(IAdToAppListener listener, String key) {
        mAdToAppListeners.put(key, listener);
    }

    private int getAdsMask() {
        int mask = 0;
        for (AdsMasks item : AdsMasks.values()) {
            mask |= item.getMask();
        }
        return mask;
    }

    public enum AdsMasks {
        VIDEO(AdToApp.MASK_VIDEO, AdToApp.VIDEO),
        INTERSTITIAL(AdToApp.MASK_INTERSTITIAL, AdToApp.INTERSTITIAL),
        BANNER(AdToApp.MASK_BANNER, AdToApp.BANNER);

        private int mMask;
        private String mType;

        AdsMasks(int mask, String type) {
            mMask = mask;
            mType = type;
        }

        public int getMask() {
            return mMask;
        }

        public String getType() {
            return mType;
        }
    }

    private void addAdsState(String adsName, boolean isAvailable) {
        AdsMasks adsMask = getAdsMaskByType(adsName);
        if (adsMask != null) {
            if (mAdsAvailableMap.containsKey(adsMask)) {
                AdsAvailableListener listener = mAdsAvailableMap.get(adsMask);
                if (listener != null) {
                    listener.isAvailable(isAvailable);
                }
            }
        }
    }

    private AdsMasks getAdsMaskByType(String type) {
        for (AdsMasks item : AdsMasks.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }

    public void isAdsAvailable(AdsMasks adsMask, AdsAvailableListener listener) {
        if (!mAdsAvailableMap.containsKey(adsMask)) {
            mAdsAvailableMap.put(adsMask, listener);
        } else {
            listener.isAvailable(AdToApp.isAvailableAd(adsMask.getType()));
        }
    }

    public interface AdsAvailableListener {
        void isAvailable(boolean available);
    }

    public void showAds(AdsMasks adsMask) {
        switch (adsMask) {
            case VIDEO:
                AdToApp.showInterstitialAd(adsMask.getType());
                break;
            case INTERSTITIAL:
                AdToApp.showInterstitialAd(adsMask.getType());
                break;
            case BANNER:
                break;
        }
    }
}