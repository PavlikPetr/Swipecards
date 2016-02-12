package com.topface.topface.utils.ads;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.appintop.init.AdToApp;
import com.appintop.interstitialads.DefaultInterstitialListener;
import com.topface.framework.utils.Debug;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Created by Петр on 03.02.2016.
 * Контроллер для работы с рекламной сетью AdToApp
 */
public class AdToAppController {

    public static final String ADTOAPP_APP_KEY = "361e95a8-3cf4-494d-89de-1a0f57f25ab3:b8942ef1-6fe1-4c7b-ab3d-2814072cedf3";

    private HashMap<String, IAdToAppListener> mAdToAppListeners = new HashMap<>();
    private HashMap<AdsMasks, AdsAvailableListener> mAdsAvailableMap = new HashMap<>();
    private boolean mIsVideoStart;
    private DefaultInterstitialListener mInterstitialListener = new DefaultInterstitialListener() {
        @Override
        public void onFirstInterstitialLoad(String adsType, String adsProvider) {
            addAdsState(adsType, true);
        }

        @Override
        public void onInterstitialStarted(String adsType, String adsProvider) {
            if (adsType.equals(AdsMasks.VIDEO.getType())) {
                mIsVideoStart = true;
            }
            for (IAdToAppListener listener : mAdToAppListeners.values()) {
                if (listener != null) {
                    listener.onVideoStart();
                }
            }
        }

        @Override
        public void onInterstitialClicked(String adsType, String adsProvider) {
            for (IAdToAppListener listener : mAdToAppListeners.values()) {
                if (listener != null) {
                    listener.onClicked();
                    if (mIsVideoStart && adsType.equals(AdsMasks.VIDEO.getType())) {
                        listener.onVideoWatched();
                    }
                }
            }
        }

        @Override
        public void onInterstitialClosed(String adsType, String adsProvider) {
            for (IAdToAppListener listener : mAdToAppListeners.values()) {
                if (listener != null) {
                    listener.onClosed();
                    if (mIsVideoStart && adsType.equals(AdsMasks.VIDEO.getType())) {
                        listener.onVideoWatched();
                    }
                }
            }
        }

        @Override
        public boolean onInterstitialFailedToShow(String adsType) {
            addAdsState(adsType, false);
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

    public void initSdk(@NotNull Activity activity) {
        if (!AdToApp.isSDKInitialized()) {
            AdToApp.initializeSDK(activity, ADTOAPP_APP_KEY, getAdsMask());
            AdToApp.setInterstitialListener(mInterstitialListener);
            AdToApp.setLogging(Debug.isDebugLogsEnabled());
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

    @Nullable
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