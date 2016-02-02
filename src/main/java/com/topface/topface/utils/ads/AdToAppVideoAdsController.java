package com.topface.topface.utils.ads;

import android.app.Activity;

import com.appintop.init.AdToApp;
import com.appintop.interstitialads.DefaultInterstitialListener;
import com.topface.framework.utils.Debug;
import com.topface.topface.banners.ad_providers.AdToAppProvider;

/**
 * Created by Петр on 02.02.2016.
 * Контроллер для показа видеорекламы
 */
public class AdToAppVideoAdsController {

    private static final String LOG = "AdToAppVideoAdsController";

    private Activity mActivity;
    private boolean isNeedLoadAds;
    private boolean isVideoStart;
    private boolean isVideoFinish;
    private OnVideoAdsListener mOnVideoAdsListener;
    private DefaultInterstitialListener mVideoAdsListener = new DefaultInterstitialListener() {
        @Override
        public void onFirstInterstitialLoad(String s, String s1) {
            Debug.error(LOG + " onFirstInterstitialLoad " + s + " " + s1);
            if (isNeedLoadAds && AdToApp.isSDKInitialized()) {
                AdToApp.showInterstitialAd();
            }
        }

        @Override
        public void onInterstitialStarted(String s, String s1) {
            if (mOnVideoAdsListener != null) {
                mOnVideoAdsListener.onVideoStart();
            }
            isVideoStart = true;
            Debug.error(LOG + " onInterstitialStarted " + s + " " + s1);
        }

        @Override
        public void onInterstitialClicked(String s, String s1) {
            if (mOnVideoAdsListener != null) {
                mOnVideoAdsListener.onClicked();
            }
            Debug.error(LOG + " onInterstitialClicked " + s + " " + s1);
        }

        @Override
        public void onInterstitialClosed(String s, String s1) {
            if (isVideoStart && mOnVideoAdsListener != null) {
                mOnVideoAdsListener.onVideoWatched();
            }
            Debug.error(LOG + " onInterstitialClosed " + s + " " + s1);
        }

        @Override
        public void onRewardedCompleted(String adProvider, String currencyName, String currencyValue) {
            super.onRewardedCompleted(adProvider, currencyName, currencyValue);
            if (mOnVideoAdsListener != null) {
                mOnVideoAdsListener.onRewardedCompleted(adProvider, currencyName, currencyValue);
            }
            Debug.error(LOG + " onRewardedCompleted " + adProvider + " " + currencyName + " " + currencyValue);
        }
    };

    public AdToAppVideoAdsController(Activity activity, OnVideoAdsListener listener) {
        mActivity = activity;
        mOnVideoAdsListener = listener;
    }

    public void showAd() {
        if (mActivity == null) {
            Debug.error(LOG + " activity = null, advertising can not be show");
            return;
        }
        // Инициализацию необходимо выполнять при каждом показе, т.к. AdToApp мог был быть
        // проинициализирован с другой маской
        AdToApp.initializeSDK(mActivity, AdToAppProvider.ADTOAPP_APP_KEY, AdToApp.MASK_VIDEO);
        AdToApp.setInterstitialListener(mVideoAdsListener);
        if (AdToApp.isAvailableAd(AdToApp.VIDEO)) {
            AdToApp.showInterstitialAd();
            isNeedLoadAds = false;
        } else {
            isNeedLoadAds = true;
        }
    }

    public interface OnVideoAdsListener {
        void onVideoWatched();

        void onVideoStart();

        void onClicked();

        void onRewardedCompleted(String adProvider, String currencyName, String currencyValue);
    }
}
